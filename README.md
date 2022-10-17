# Sms Classification

A simple Android SMS classification app that implement LSTM model on classifying Indonesian text messages that still under development. With this app you can:

* Receive and/or send text message.
* Classify incoming sms (real-time classification) using LSTM model into three classes (normal/personal, scam/fraud, promotions/advertisement).
* Move/change conversation label in case of misclassification.
* More features to come...

## Display
### Light Theme
#### Splash Screen and Start Screen
<img src="images/splash_light.png" alt="splash_light" width="200"> <img src="images/start_light.png" alt="start_light" width="200"> <img src="images/load_light.png" alt="load_light" width="200">
#### Conversation List, Messages List, Contact List, Settings
<img src="images/conversations_light.png" alt="conversations_light" width="200"> <img src="images/messages_light.png" alt="messages_light" width="200"> </br>
<img src="images/contact_light.png" alt="contact_light" width="200"> <img src="images/settings_light.png" alt="settings_light" width="200">

### Dark Theme
#### Splash Screen and Start Screen
<img src="images/splash_dark.png" alt="splash_dark" width="200"> <img src="images/start_dark.png" alt="start_dark" width="200"> <img src="images/load_dark.png" alt="load_dark" width="200">
#### Conversation List, Messages List, Contact List, Settings
<img src="images/conversations_dark.png" alt="conversations_dark" width="200"> <img src="images/messages_dark.png" alt="messages_dark" width="200"> </br>
<img src="images/contact_dark.png" alt="contact_dark" width="200"> <img src="images/settings_dark.png" alt="settings_dark" width="200">

## Library
* [Gson](https://github.com/google/gson/)
* [Coroutine](https://developer.android.com/kotlin/coroutines)
* [Glide](https://github.com/bumptech/glide/)
* [Jetpack-Navigation](https://developer.android.com/guide/navigation/navigation-getting-started)
* [Dagger-Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
* [Room Presistence Library](https://developer.android.com/training/data-storage/room)
* [SMS-MMS Sending Library](https://github.com/klinker41/android-smsmms)
* [LibPhoneNumber-android](https://github.com/MichaelRocks/libphonenumber-android)
* [Turbine](https://github.com/cashapp/turbine)
* [TensorFlowLite](https://www.tensorflow.org/lite)
* [Roboletric](https://github.com/robolectric/robolectric)
* [Mockito](https://github.com/mockito/mockito)
* [Espresso](https://developer.android.com/training/testing/espresso)

## App Architecture
* MVVM
