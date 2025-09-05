/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.developers.androidify.watchface.creator

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.android.apksig.ApkSigner
import com.android.apksig.KeyConfig
import com.android.apksig.util.DataSink
import com.android.apksig.util.DataSource
import com.android.apksig.util.DataSources
import com.android.apksig.util.ReadableDataSink
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Calendar
import java.util.Date

private const val KEY_ALIAS = "com.android.developers.androidify.ApkSigningKey"
private const val CERT_ALIAS = "com.android.developers.androidify.Cert"
private const val ANDROID_KEYSTORE = "AndroidKeyStore"

/**
 * Signs an APK file using a key pair and certificate retrieved from or created in the
 * Android KeyStore.
 *
 * This function first retrieves or creates a signing key pair and a corresponding certificate.
 * It then uses these to sign the provided unsigned APK byte array.
 *
 * @param unsignedApk The byte array of the unsigned APK file.
 * @return A byte array representing the signed APK file.
 */
fun signApk(unsignedApk: ByteArray): ByteArray {
    val keyPair = getOrCreateSigningKeyPair()
    val certificate = getOrCreateCertificate(keyPair)
    return signApkWithCredentials(unsignedApk, keyPair.private, certificate)
}

/**
 * Retrieves an existing signing key pair from the Android KeyStore or creates a new one
 * if it doesn't exist.
 *
 * The key pair is stored under a specific alias in the Android KeyStore.
 * If the key pair does not exist, it generates a new RSA key pair with SHA256withRSA
 * signature padding.
 *
 * @return The [KeyPair] containing the public and private keys.
 */
private fun getOrCreateSigningKeyPair(): KeyPair {
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    if (keyStore.containsAlias(KEY_ALIAS)) {
        val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
        return KeyPair(entry.certificate.publicKey, entry.privateKey)
    }

    val parameterSpec = KeyGenParameterSpec.Builder(
        KEY_ALIAS,
        KeyProperties.PURPOSE_SIGN,
    ).run {
        setDigests(KeyProperties.DIGEST_SHA256)
        setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
        build()
    }

    val keyPairGenerator = KeyPairGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_RSA,
        ANDROID_KEYSTORE,
    ).apply {
        initialize(parameterSpec)
    }

    return keyPairGenerator.generateKeyPair()
}

/**
 * Creates a self-signed X.509 certificate using the provided key pair.
 *
 * The certificate is configured with:
 * - A validity period of 25 years from the current date.
 * - A randomly generated 64-bit serial number.
 * - The subject and issuer name set to "CN=Androidify".
 * - A SHA256WithRSA signature algorithm.
 *
 * After creation, the certificate is stored in the Android KeyStore.
 *
 * @param keyPair The [KeyPair] containing the public and private keys to be used for signing
 *                and embedding in the certificate.
 * @return The generated [X509Certificate].
 */
private fun createCertificate(keyPair: KeyPair): X509Certificate {
    val now = Date()
    val expiryDate = Calendar.getInstance().apply {
        time = now
        add(Calendar.YEAR, 25)
    }.time

    val serialNumber = BigInteger(64, SecureRandom())
    val subjectName = X500Name("CN=Androidify")

    val certificateBuilder = JcaX509v3CertificateBuilder(
        subjectName,
        serialNumber,
        now,
        expiryDate,
        subjectName,
        keyPair.public,
    )

    val contentSigner = JcaContentSignerBuilder("SHA256WithRSA")
        .build(keyPair.private)

    val certificateHolder = certificateBuilder.build(contentSigner)
    val certificate = JcaX509CertificateConverter().getCertificate(certificateHolder)

    storeCertificate(certificate)

    return certificate
}

/**
 * Stores an X509Certificate in the Android Keystore.
 *
 * @param certificate The X509Certificate object to be stored.
 * @return True if storage was successful, false otherwise.
 */
private fun storeCertificate(certificate: X509Certificate): Boolean {
    return try {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
        keyStore.setCertificateEntry(CERT_ALIAS, certificate)
        true
    } catch (e: Exception) {
        // Log the exception for debugging
        e.printStackTrace()
        false
    }
}

/**
 * Retrieves an X509Certificate from the Android Keystore.
 *
 * @return The X509Certificate if found, or null if it doesn't exist or an error occurs.
 */
private fun getOrCreateCertificate(keyPair: KeyPair): X509Certificate {
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }
    return (keyStore.getCertificate(CERT_ALIAS) ?: createCertificate(keyPair)) as X509Certificate
}

/**
 * Signs an APK file with the provided private key and certificate.
 *
 * This function uses the `ApkSigner` library to sign the APK.
 * It enables both v2 and v3 signing schemes.
 *
 * @param unsignedApkBytes The byte array of the unsigned APK file.
 * @param privateKey The private key to use for signing.
 * @param certificate The certificate corresponding to the private key.
 * @return A byte array representing the signed APK.
 */
private fun signApkWithCredentials(
    unsignedApkBytes: ByteArray,
    privateKey: PrivateKey,
    certificate: X509Certificate,
): ByteArray {
    val keyConfig = KeyConfig.Jca(privateKey)
    val signerConfig = ApkSigner.SignerConfig.Builder(
        "CERT",
        keyConfig,
        listOf(certificate),
    ).build()
    val dataSource = DataSources.asDataSource(ByteBuffer.wrap(unsignedApkBytes))
    val dataSink = InMemoryDataSink()

    ApkSigner.Builder(listOf(signerConfig)).run {
        setInputApk(dataSource)
        setOutputApk(dataSink)
        setV2SigningEnabled(true)
        setV3SigningEnabled(true)
        build()
    }.sign()

    return dataSink.toByteArray()
}

/**
 * A custom DataSink that writes all consumed data into an in-memory byte array.
 */
private class InMemoryDataSink : ReadableDataSink {
    private var buffer: ByteArray = ByteArray(0)

    override fun consume(buf: ByteArray, offset: Int, length: Int) {
        val newBuffer = ByteArray(buffer.size + length)
        System.arraycopy(buffer, 0, newBuffer, 0, buffer.size)
        System.arraycopy(buf, offset, newBuffer, buffer.size, length)
        buffer = newBuffer
    }

    override fun consume(buf: ByteBuffer) {
        val newBuffer = ByteArray(buffer.size + buf.remaining())
        System.arraycopy(buffer, 0, newBuffer, 0, buffer.size)
        buf.get(newBuffer, buffer.size, buf.remaining())
        buffer = newBuffer
    }

    override fun copyTo(offset: Long, size: Int, dest: ByteBuffer) {
        dest.put(buffer, offset.toInt(), size)
    }

    override fun size(): Long {
        return buffer.size.toLong()
    }

    override fun feed(offset: Long, size: Long, sink: DataSink) {
        sink.consume(buffer, offset.toInt(), size.toInt())
    }

    override fun getByteBuffer(offset: Long, size: Int): ByteBuffer {
        return ByteBuffer.wrap(buffer, offset.toInt(), size)
    }

    override fun slice(offset: Long, size: Long): DataSource {
        val sliceBuffer = ByteBuffer.wrap(buffer, offset.toInt(), size.toInt())
        return com.android.apksig.util.DataSources.asDataSource(sliceBuffer)
    }

    fun toByteArray(): ByteArray {
        return buffer
    }
}
