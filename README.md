# CARPOOL: Secure And Reliable Proof of Location

> **Abstract** Multiple authentication solutions are widely deployed, such as OTP/TOTP/HOTP codes, hardware tokens, PINs, or biometrics. However, in practice, one sometimes needs to authenticate not only the user but also their location. The current state-of-the-art secure localisation schemes are either unreliable or insecure, or require additional hardware to reliably prove the user's location. This paper proposes CARPOOL, a novel, secure, and reliable approach to affirm the location of the user by solely relying on location-bounded interactions with commercial off-the-shelf devices. Our solution does not require any additional hardware, leverages devices already present in a given environment, and can be integrated effortlessly with existing security components, such as identity and access control systems. To demonstrate the feasibility of our work and to show that it can be deployed in a realistic closed environment setting, we implemented a proof of concept realisation of CARPOOL on an Android phone and multiple Raspberry Pi boards and integrated CARPOOL with Amazon Web Services (AWS) Cognito.


This repository contains the code and resources of our proof-of-concept which supplement our work `CARPOOL`. Below is an overview of the repository contents and structure. Full version of the paper can be found at: https://ia.cr/2025/1502


## Repository Structure
This repository provides three main components:

1. **Back-end server code (Amazon EC2)**  
   - `server_nv1.py`  
     Runs on an Amazon EC2 instance to handle incoming connections, store data, and communicate with the IoT devices.

2. **Device-level code (Raspberry Pi 3)**  
   - `OfflineBeaconGUI_v2.py`  
   - `OfflineBeaconLeaToggle.py`  
   - `OnlineAudio.py`  
   - `OnlineQR.py`  
   These scripts are deployed on Raspberry Pi devices, enabling them to interact with beacons and sensors (offline or online), perform scans, and manage data transfer.

3. **Mobile-client code (Android application)**  
   - Located in the `CARPOOL/app` folder  
   - Buildable with Gradle; after installation on an Android phone, it supports multiple interactions (BLE beacons, QR scanning, and near-ultrasonic audio).  

### Directory Tree

The contents of this repository are as follows:
```bash
.
├── Back-end server code - Amazon EC2
│   └── server_nv1.py
├── Device-level code - Raspberry Pi 3
│   ├── OfflineBeaconGUI_v2.py
│   ├── OfflineBeaconLeaToggle.py
│   ├── OnlineAudio.py
│   └── OnlineQR.py
├── Mobile-client code - Android application
│   └── CARPOOL
│       ├── app
│       │   ├── build
│       │   └── src
│       │       ├── androidTest
│       │       │   └── java
│       │       │       └── com
│       │       │           └── example
│       │       │               └── CARPOOL
│       │       │                   └── ExampleInstrumentedTest.java
│       │       ├── main
│       │       │   ├── AndroidManifest.xml
│       │       │   ├── assets
│       │       │   │   └── roomspolicies.json
│       │       │   ├── java
│       │       │   │   └── com
│       │       │   │       └── example
│       │       │   │           └── CARPOOL
│       │       │   │               ├── Bluetooth
│       │       │   │               │   ├── BluetoothProcedure.java
│       │       │   │               │   └── EddystoneActivity.java
│       │       │   │               ├── CommunicationUtils
│       │       │   │               │   ├── CreateMessage.java
│       │       │   │               │   ├── CryptoUtils.java
│       │       │   │               │   ├── EndProcedure.java
│       │       │   │               │   ├── JnIDProcedure.java
│       │       │   │               │   ├── LoginProcedure.java
│       │       │   │               │   ├── NetworkUtils.java
│       │       │   │               │   ├── RedboxProcedure.java
│       │       │   │               │   ├── RegisterProcedure.java
│       │       │   │               │   └── UDPClient.java
│       │       │   │               ├── DeviceHandlers
│       │       │   │               │   ├── Offline
│       │       │   │               │   │   ├── OfflineBluetoothPairedActivity.java
│       │       │   │               │   │   ├── OfflineLEbeaconActivity.java
│       │       │   │               │   │   ├── OfflineScannerActivity.java
│       │       │   │               │   │   └── OfflineScannerActivityV2.java
│       │       │   │               │   └── Online
│       │       │   │               │       ├── AudioActivity.java
│       │       │   │               │       ├── BluetoothPairedActivity.java
│       │       │   │               │       ├── LEbeaconActivity.java
│       │       │   │               │       └── ScannerActivity.java
│       │       │   │               ├── ErrorActivity.java
│       │       │   │               ├── IoTDevices
│       │       │   │               │   ├── AudioDevice.java
│       │       │   │               │   ├── BLEDevice.java
│       │       │   │               │   ├── Device.java
│       │       │   │               │   ├── PairedBLDevice.java
│       │       │   │               │   └── VisualDevice.java
│       │       │   │               ├── MainActivity.java
│       │       │   │               ├── PolicyStart.java
│       │       │   │               ├── RegisterActivity.java
│       │       │   │               ├── RoomAndPolicy
│       │       │   │               │   ├── ReadRP.java
│       │       │   │               │   └── Room.java
│       │       │   │               ├── SyncAddrActivity.java
│       │       │   │               └── TryForInternetActivity.java
│       │       │   └── res
│       │       └── test
│       │           └── java
│       │               └── com
│       │                   └── example
│       │                       └── CARPOOL
│       │                           └── ExampleUnitTest.java
│       ├── build.gradle
│       ├── gradle
│       │   └── wrapper
│       │       ├── gradle-wrapper.jar
│       │       └── gradle-wrapper.properties
│       ├── gradle.properties
│       ├── gradlew
│       ├── gradlew.bat
│       ├── local.properties
│       └── settings.gradle
└── README.md
```

## Android App Life Cycle

![Android App Life Cycle Diagram](/CARPOOL_Android_App.png)

The CARPOOL Android application follows these steps:
1. **Login Screen (a)** – The user signs in and selects a “room” or environment.  
2. **BLE Interaction (b)** – The app automatically listens for a Bluetooth beacon packet and proceeds when one arrives (no extra user input needed).  
3. **QR Scanning (c)** – The user is prompted to scan a QR code if needed.  
4. **Audio Mode (d)** – The app finally listens for near-ultrasonic audio to complete the handshake.


## Citation

If you find this work useful, please consider citing the following paper:

```bibtex
@misc{cryptoeprint:2025/1502,
      author = {Sayon Duttagupta and Dave Singelée and Xavier Carpent and Volkan Guler and Takahito Yoshizawa and Seyed Farhad Aghili and Aysajan Abidin and Bart Preneel},
      title = {{CARPOOL}: Secure And Reliable Proof of Location},
      howpublished = {Cryptology {ePrint} Archive, Paper 2025/1502},
      year = {2025},
      url = {https://eprint.iacr.org/2025/1502}
}

```
