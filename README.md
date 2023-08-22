# easy_firebase_auth
Wrapper arround firebase authentication to ease the implementation of firebase auth

### how to
 - add `apply plugin "com.google.gms.google-services"` or `id("com.google.gms.google-services")` in plugin {} at your app level project
 - add the SHA1 key in the project at firebase console
 - download `google-services.json` from firebase console and place inside `app` directory in the `project`
 - clone this module in your project
 - add `include (":easy_firebase_auth")` at the end of `settings.gradle` file in the root of your `project`
 - add the dependancy `implementation(project(":easy_firebase_auth"))`
 - **Congradulations you are done**
