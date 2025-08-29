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
library, so the pre-builts are provided in `jniLibs`. A script is also included for building these
fresh, but that should not be necessary.

## Signing the APK

For the purposes of this project, a key is generated at runtime and used to sign the APK. This is
not the approach to take in production, but the watch face APK must be signed, and it doesn't so
much matter what key is used to do it.