# Watch face generation

Some details about how this module is used for watch face generation.

## Modifying the watch face

The `assets` directory holds the necessary Watch Face Format resources, namely AndroidManifest.xml,
watchface.xml, watch_face_info.xml and so on. These files are exactly as output by a tool such as
the Figma plugin, Watch Face Designer.

In order to modify these to show the Androidify bot, adjust an `<Image>` tag to point to the "bot"
resource, e.g. `<Image resource="bot"/>`: In compiling the APK, the `bot.png` file will be added, so
this resource will be available to the watch face.

You should ensure that any unnecessary images are removed from `res/drawable`, for example if Watch
Face Designer outputted a placeholder image that you are replacing for `bot.png`, remove the
placeholder image (it's likely large!). Furthermore, ensure that the images are optimized, for
example, using `pngquant` on all images, to help keep the watch face size to a minimum.

## Packaging the watch face

To package the watch face, the [Pack](https://github.com/google/pack) is used. This is a native
library, so the pre-builts are provided in `jniLibs`.

## Building native libraries

A script is also included for building these fresh, but that is generally not necessary when
building Androidify.

However, if you do wish to build these, follow these steps in order to run the script:

1. [Install][install-ndk] the Android NDK.
2. [Install][install-rust] Rust and Cargo.
3. Make a copy of `watchface/pack-java/.cargo-example` as
    `watchface/pack-java/.cargo`.
4. Open `watchface/pack-java/.cargo/config.toml`.
5. Adjust all the `linker` and `ar` paths, replacing `<NDK_PATH>` with the absolute path to your
   Android NDK installation. Note that each linker must support API level 26 and above, to match
   the Androidify `minSdk`. For example: `armv7a-linux-androideabi26-clang`.
6. Execute the `watchface/provide-libraries-to-androidify-project.sh` script. This will build the native
   libraries and copy them to the `jniLibs` directory.

[install-ndk]: https://developer.android.com/studio/projects/install-ndk#default-version
[install-rust]: https://rust-lang.org/tools/install/