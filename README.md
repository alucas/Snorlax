# Snorlax [![Build Status](https://travis-ci.org/alucas/Snorlax.svg?branch=master)](https://travis-ci.org/alucas/Snorlax) [![License](https://img.shields.io/badge/license-apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://github.com/alucas/Snorlax#donation) [![Github All Releases](https://img.shields.io/github/downloads/alucas/snorlax/total.svg)](https://github.com/alucas/Snorlax/releases) [![Github Releases](https://img.shields.io/github/downloads/alucas/snorlax/latest/total.svg)](https://github.com/alucas/Snorlax/releases/latest) [![GitHub release](https://img.shields.io/github/release/alucas/snorlax.svg)](https://github.com/alucas/Snorlax/releases/latest)

This module acts as a [man in the middle](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) on Pokémon Go's communications with the server, and allows us to add extra *features* to the game.

- It does not require your account or any information about you.
- It does not communicate with the server at any time, so it's pretty safe against detection.
- It does not require any right (no data usage, ...)

### Features:
- Prevent PokemonGo from detecting GPS is being spoofed (similar to Mock Mock Locations)
- Replace pokemons name with custom formats ([details](https://github.com/alucas/Snorlax/wiki/Rename-formats)) :
  - Name, Nickname
  - Level
  - Type
  - Moves (name, type, power)
  - IV (%, attack, defence, stamina)
  - HP (current, max)
  - CP (current, evolution, last evolution, min, max)
  - <img src="https://cloud.githubusercontent.com/assets/498613/20669445/8f9b44ec-b574-11e6-91f7-81c2036a79bd.png" width="300px">
  - <img src="https://cloud.githubusercontent.com/assets/498613/20310033/ad7431c4-ab4a-11e6-9ad2-e0c84b9f9b5a.png" width="300px">
- Show pokemon stats as a system notification when you encounter them :
  - <img src="https://cloud.githubusercontent.com/assets/498613/20669600/5f94c1a0-b575-11e6-98e3-6bc64f75851d.png" width="300px">
  - <img src="https://cloud.githubusercontent.com/assets/498613/20669603/62ad4cfe-b575-11e6-81c0-122492dd6f5f.png" width="300px">
- Show catch results (*Success*, *Missed*, *Flee*) as soon as you throw your pokeball
- Show lure's remaining time
  - <img src="https://cloud.githubusercontent.com/assets/498613/20309733/6732152e-ab49-11e6-9f8c-a7276da34080.png" width="300px">
- Show notification when a pokemon is ejected from a Gym :
  - <img src="https://cloud.githubusercontent.com/assets/498613/21016969/f58cdc70-bd67-11e6-995d-940f47e11507.png" width="300px">
  - <img src="https://cloud.githubusercontent.com/assets/498613/21093558/cb89eefc-c052-11e6-9ea2-a3d3e386ddb2.png" width="300px">
- Show a notification when it's time to collect your defender bonus :
  - *add image*

### Dev features:
- Broadcast [Intent](https://developer.android.com/reference/android/content/Intent.html) with nearby pokemon informations
- Log network traffic (use Protobuf's ```protoc``` to decode it)
- Track and notify update of the GAME_MASTER file

# For users:
## Installation requirements
- Android (4.4 or +, Nougat is not supported for the moment)
- Your device must be rooted
- Xposed framekwork must be installed. You can download it [here](http://repo.xposed.info/module/de.robv.android.xposed.installer).
- You need to hide Xposed and root to Pokemon Go to be able to play. It's the hard part and it's different for every devices, so Google is your friend. It can be done with tools like *suhide*, *root switch*, ...

## Installation
 - From the Xposed installer, look for the [*Snorlax Reloaded*](http://repo.xposed.info/module/com.alucas.snorlax) module
 - OR manually install [the latest APK](https://github.com/alucas/Snorlax/releases/latest)

# For devs:
## Libraries and tools
- Android's support libraries
- Reactive extensions: [RxJava](https://github.com/ReactiveX/RxJava), [RxAndroid](https://github.com/ReactiveX/RxAndroid), [RxBinding](https://github.com/JakeWharton/RxBinding) and [RxRelay](https://github.com/JakeWharton/RxRelay)
- Dependency injector: [Dagger 2](http://google.github.io/dagger/)
- Android's views binding: [Butterknife](https://github.com/JakeWharton/butterknife)
- Logging utility: [Timber](https://github.com/JakeWharton/timber)
- Generated immutable value classes: [AutoValue](https://github.com/google/auto/tree/master/value)
- [Xposed framework](https://github.com/rovo89/XposedBridge)
- [Protobuf](https://github.com/google/protobuf-gradle-plugin)

## Build requirements
- JDK 1.8
- [Android SDK](http://developer.android.com/sdk/index.html)
- Android N [(API 24) ](http://developer.android.com/tools/revisions/platforms.html)
- Latest Android SDK Tools and build tools
- Lombok plugin for Android Studio / IntelliJ IDEA
- Your device must be rooted and the xposed framekwork must be installed. You can download it [here](http://repo.xposed.info/module/de.robv.android.xposed.installer).

## Build source
Clone the repository (you must have Git installed)
```
git clone --recursive https://github.com/alucas/Snorlax.git; cd Snorlax
```

Build
```
.\gradlew build
```

To build a release APK
```
.\gradlew assembleRelease
```

To install directly to the phone connected via ADB
```
.\gradlew installRelease
```

## Contributing
  - Fork it!
  - Create your feature branch: `git checkout -b my-new-feature`
  - Commit your changes: `git commit -am 'Useful information about your new features'`
  - Push to the branch: `git push origin my-new-feature`

# Epilogue:
## Special thanks
Snorlax would not have been possible without:
- [chuparCh0pper](https://github.com/chuparCh0pper/PoGoIV_xposed) for his awesome [module](https://github.com/chuparCh0pper/PoGoIV_xposed) which Snorlax is inspired on
- [ELynx](https://github.com/ELynx) for the method to MITM PokemonGo communications, check his [repo](https://github.com/ELynx/pokemon-go-xposed-mitm) too
- And last but not least, [AeonLucid](https://github.com/AeonLucid) for the [PokemonGo's protos](https://github.com/AeonLucid/POGOProtos)

## Donation
I hope you are liking Snorlax!

If you do, you can support this project to keep coming with new features by buying me a cup of coffee (so i can code by night) or a BigMac at McDonalds. Click the link below and thanks you!

[Donate (igoticecream)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=A9PPGNDJEC33E) - Original dev (no longer active)

[Donate (alucas)](https://www.paypal.me/antoinelucas)

## License
    Copyright (c) 2016. Pedro Diaz <igoticecream@gmail.com>

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
