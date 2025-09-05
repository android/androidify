// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

use base64::{engine::general_purpose, Engine};
use jni::{
    objects::{JClass, JObject, JObjectArray, JString},
    sys::{jstring},
    JNIEnv
};
use pack_api::{compile_apk, FileResource, Package};

// Name (MUST) follow Java_packageName_className_methodName
/// # Safety
/// Function must be unsafe because it is called via Java JNI
#[no_mangle]
pub unsafe extern "C" fn Java_com_android_developers_androidify_watchface_creator_PackPackage_nativeCompilePackage(
    mut env: JNIEnv,
    _this: JClass,
    manifest_jstring: JString,
    resources: JObjectArray
) -> jstring {
    let manifest: String = env.get_string(&manifest_jstring).unwrap().into();

    let mut pack_resources = vec![];
    let resource_len = env.get_array_length(&resources).unwrap();
    for index in 0..resource_len {
        let resource = env.get_object_array_element(&resources, index).unwrap();
        let name = get_string_field_from_java_class(&mut env, &resource, "name");
        let subdirectory = get_string_field_from_java_class(&mut env, &resource, "subdirectory");
        let contents_b64 = get_string_field_from_java_class(&mut env, &resource, "contentsBase64");
        let contents = b64_to_bytes(&contents_b64);

        let pack_resource = FileResource::new(subdirectory, name, contents);
        pack_resources.push(pack_resource);
    }

    let package = Package {
        android_manifest: manifest.as_bytes().to_vec(),
        resources: pack_resources
    };

    let finished_package = compile_apk(&package).unwrap();
    let pkg_b64 = bytes_to_b64(&finished_package);

    env.new_string(pkg_b64).unwrap().into_raw()
}

fn b64_to_bytes(b64: &str) -> Vec<u8> {
    general_purpose::STANDARD.decode(b64.as_bytes()).unwrap()
}

fn bytes_to_b64(bytes: &Vec<u8>) -> String {
    general_purpose::STANDARD.encode(bytes)
}

const JAVA_STRING_TYPE: &str = "Ljava/lang/String;";

fn get_string_field_from_java_class(env: &mut JNIEnv, class: &JObject, field_name: &str) -> String {
    let field_object = env
        .get_field(class, field_name, JAVA_STRING_TYPE)
        .unwrap()
        .l()
        .unwrap();
    env.get_string(&field_object.into()).unwrap().into()
}