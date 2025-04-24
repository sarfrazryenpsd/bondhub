# 💬 BondHub - Next-Gen Android Chat App
A real-time, secure, and modern messaging app built with Jetpack Compose, Firebase & Clean Architecture.

🎥 App Preview (BondHub in Action)
Watch the full app demo series (5 short videos) on YouTube:

[![Watch the demo](https://img.youtube.com/vi/tdbVj3AGl8M/0.jpg)](https://www.youtube.com/watch?v=tdbVj3AGl8M&list=PLUiXo0vwFX2tunJjeu0dAxfd8yRGOPiuj&index=4)

## ✨ Features

- 🔐 Firebase Authentication (Email/Password)
- 👤 User Profile Setup & Update
- 👫 Connection Requests (Send / Accept / Reject)
- 💬 Real-Time Messaging (Dual Firestore Chat Documents)
- 📥 Offline Support with Room Persistence
- 📲 Deep Link Push Notifications using FCM
- 🔔 Unread Count & Message Status
- ⚡ Reactive UI with StateFlow and SharedFlow
- 🧠 Clean Architecture (Domain/Data/Presentation)
- 💉 Dependency Injection with Hilt

---

## 🛠️ Tech Stack

| Category | Tools |
|---------|-------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| Backend | Firebase Auth, Firestore, Firebase Storage |
| Notifications | Firebase Cloud Messaging (FCM) |
| Caching | Room Database |
| DI | Dagger Hilt |
| Media | Coil (Image Loading) |
| Reactive | Coroutines, Flow, StateFlow, SharedFlow |

---

## 🏗️ Architecture Overview

> Clean Architecture with separation of concerns  
> Domain ↔ Data ↔ Presentation

```text
bondhub/
├── data/           # Data layer: repositories, data sources
├── di/             # Dependency Injection modules (Hilt)
├── domain/         # Domain layer: models, use cases
├── notification/   # Notification handling (FCM)
└── presentation/   # Presentation layer: UI components, viewmodels
```

🚀 Getting Started
Clone the repository

git clone [https://github.com/yourusername/bondhub.git](https://github.com/sarfrazryenpsd/bondhub)
cd bondhub
Open in Android Studio

Connect your Firebase project

Replace the google-services.json file in the app/ folder

Run the app on an emulator or physical device

## 🛣️ Roadmap
✅ Basic Authentication & Chat Messaging

✅ Push Notifications with Deep Linking

✅ Dual Chat Documents & Message Deletion

✅ Clean Architecture + Offline Sync

🔄 Media Message Support (Images, Videos)

🔄 Voice Message Recording

🔜 End-to-End Encryption (E2EE)

🔜 Message Reactions & Typing Indicator

🔜 Online/Offline Status Visibility

## 💡 Why BondHub is Special
Not just another chat app — this is a complete ecosystem with real-world patterns:

🔁 Dual Firestore chat model → messages stored per user

🌐 Deep linked push notifications → open chats directly

📶 Offline-first → Room syncing with Firestore

🔄 Reactive UI using StateFlow & SharedFlow

🧼 Modular, testable, and scalable project setup

## 🤝 Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

## 📝 License
This project is licensed under the MIT License – see the LICENSE file for details.

## 🙋‍♂️ Creator
Developed with 💙 by Sarfraz
🔗 [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/yourusername/)
Feel free to connect for collaborations, feedback, or just to chat!

