# Androidify on Android

![androidify banner](/art/androidify_banner.webp)

The Android bot is a beloved mascot for Android users and developers, with previous versions of the
bot builder being very popular - we decided that this year we’d rebuild the bot maker from the
ground up, using the latest technology backed by Gemini. Today we are releasing a new open source
app, Androidify, for learning how to build powerful AI driven experiences on Android using the
latest technologies such as Jetpack Compose, Gemini API through Firebase AI Logic SDK, CameraX, and
Navigation 3.

Note: This app is still under development. This sample app is currently using a standard Imagen
model, but we've been working on a fine-tuned model that's trained specifically on all of the pieces
that make the Android bot cute and fun; we'll share that version later this summer. In the meantime,
don't be surprised if the sample app puts out some interesting looking examples!

For the full blog post on app, [read here](https://android-developers.googleblog.com/2025/05/androidify-building-ai-driven-experiences-jetpack-compose-gemini-camerax.html). 

## Under the hood
The app combines a variety of different Google technologies, such as:
* Gemini API - through Firebase AI Logic SDK, for accessing the underlying Imagen and Gemini models.
* Jetpack Compose - for building the UI with delightful animations and making the app adapt to different screen sizes.
* Navigation 3 - the latest navigation library for building up Navigation graphs with Compose.
* CameraX and Media3 Compose - for building up a custom camera with custom UI controls (rear camera support, zoom support, tap-to-focus) and playing the promotional video.

## Setup and installation

1. Clone the repository.
2. Create a [Firebase project](https://firebase.google.com/products/firebase-ai-logic) and
   generate a `google-services.json` file.
   Place the file in the app folder: `app/google-services.json`. Be sure to enable [Vertex AI API](https://console.cloud.google.com/apis/library/aiplatform.googleapis.com).
   Ensure to also enable [AppCheck](https://console.firebase.google.com/project/_/appcheck) on your Firebase project to prevent API abuse.
3. This project makes use of remote config on Firebase. In [`remote_config_defaults.xml`](core/network/src/main/res/xml/remote_config_defaults.xml), update the value of [`use_imagen`](core/network/src/main/res/xml/remote_config_defaults.xml#L40) to `true`, then import the [Firebase Remote config](https://firebase.google.com/docs/remote-config) settings from 
   [`remote_config_defaults.xml`](core/network/src/main/res/xml/remote_config_defaults.xml).
   - Navigate to your Firebase project console.
   - In the left-hand menu, under the "Run" section, select "Remote Config".
   - Click on the "Upload template" button.
   - Browse and select the `[setup/remote_config_defaults.json](setup/remote_config_defaults.json)` file from your local repository.
   - Click "Publish" to apply the settings.
4. If you'd like to change the font that the app renders with, an optional spec can be placed in
   `~/.gradle/gradle.properties` file:

```properties
fontName="Roboto Flex"
```

For Googlers, get this info from go/androidify-api-setup

## Availability
Due to the background vibe feature using 
`gemini-2.0-flash-preview-image-generation`, its not currently supported in a number of countries in Europe, Middle East & Africa.
See [this](https://ai.google.dev/gemini-api/docs/models#gemini-2.0-flash-preview-image-generation) doc for more information. 

## Contributing

See [Contributing](CONTRIBUTING.md).

## License

Androidify 2.0 is licensed under the [Apache License 2.0](LICENSE). See the `LICENSE` file for
details.
