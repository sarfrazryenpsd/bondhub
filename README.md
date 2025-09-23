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
- 🎨 Modern UI with Material 3 Design
- 🖼️ Emoji Picker Support
- 📱 Responsive Design for All Screen Sizes

---

## 🛠️ Tech Stack

| Category      | Tools                                      |
|---------------|--------------------------------------------|
| Language      | Kotlin                                     |
| UI            | Jetpack Compose                            |
| Architecture  | MVVM + Clean Architecture                  |
| Backend       | Firebase Auth, Firestore, Firebase Storage |
| Notifications | Firebase Cloud Messaging (FCM)             |
| Caching       | Room Database                              |
| DI            | Dagger Hilt                                |
| Media         | Coil (Image Loading)                       |
| Reactive      | Coroutines, Flow, StateFlow, SharedFlow    |
| Navigation    | Compose Navigation                         |
| Asynchronous  | Kotlin Coroutines                          |

---

## 🏗️ Architecture Overview

> Clean Architecture with separation of concerns  
> Domain ↔ Data ↔ Presentation

```text
bondhub/
├── data/           # Data layer: repositories, data sources
├── di/             # Dependency Injection modules (Hilt)
├── domain/         # Domain layer: models, use cases
├── notifications/  # Notification handling (FCM)
└── presentation/   # Presentation layer: UI components, viewmodels
```

### Domain Layer

The domain layer contains the business logic and is independent of any framework. It defines:

- **Models**: Core data structures like `User`, `Chat`, `ChatMessage`, `ChatConnection`
- **Repositories**: Interfaces for data operations
- **Use Cases**: Application-specific business rules encapsulated in use cases

### Data Layer

Responsible for data retrieval and storage:

- **Remote Data Source**: Handles communication with Firebase Firestore
- **Local Data Source**: Uses Room database for offline persistence
- **Mappers**: Converts between different data representations (Firestore documents, Room entities,
  domain models)
- **Repositories Implementation**: Implements repository interfaces using data sources

### Presentation Layer

Built with Jetpack Compose following MVVM pattern:

- **Screens**: UI components organized by feature
- **Components**: Reusable UI elements
- **State**: UI state management models
- **ViewModels**: Connects UI with domain layer use cases

## 🚀 Technical Implementation Details

### 🔐 Authentication

- Firebase Authentication with email/password provider
- Secure token management for user sessions
- Profile setup flow after initial registration

### 💬 Real-Time Messaging

- **Dual Document Architecture**: Each chat participant has their own chat document in Firestore for
  optimized querying
- **Message Synchronization**: Real-time updates using Firestore snapshot listeners
- **Offline-First Approach**: Messages cached locally with Room database and synced when online
- **Message Status Tracking**: SENDING, SENT, DELIVERED, READ, FAILED statuses
- **Push Notifications**: Firebase Cloud Messaging with deep linking to specific chats

### 👥 Social Features

- **Friend Connections**: Request, accept, reject friend connections
- **User Discovery**: Search for users by display name
- **Profile Management**: Complete profile setup with display name, bio, and profile picture

### 📱 UI/UX Features

- **Material 3 Design**: Modern, consistent UI following Material Design guidelines
- **Responsive Layouts**: Adapts to different screen sizes and orientations
- **Smooth Animations**: Compose animations for transitions and UI elements
- **Emoji Picker**: In-app emoji selection for enhanced messaging experience

---

## 📁 Project Structure

```text
bondhub/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ryen/bondhub/
│   │   │   │   ├── BHApplication.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/     # Room database entities and DAOs
│   │   │   │   │   ├── mappers/   # Data conversion between layers
│   │   │   │   │   ├── remote/    # Firebase Firestore data sources
│   │   │   │   │   └── repository/ # Repository implementations
│   │   │   │   ├── di/
│   │   │   │   │   └── module/    # Hilt dependency injection modules
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/     # Domain models
│   │   │   │   │   ├── repository/ # Repository interfaces
│   │   │   │   │   └── useCases/  # Business logic use cases
│   │   │   │   ├── notifications/ # FCM service and notification handling
│   │   │   │   ├── presentation/
│   │   │   │   │   ├── components/  # Reusable UI components
│   │   │   │   │   ├── contents/    # Screen content composables
│   │   │   │   │   ├── event/        # UI events
│   │   │   │   │   ├── screens/      # Navigation screens
│   │   │   │   │   ├── state/        # UI state models
│   │   │   │   │   └── theme/        # App theme and styling
│   │   │   │   └── util/             # Utility functions
│   │   │   └── res/                  # Resources (drawables, strings, etc.)
│   │   └── build.gradle.kts         # App module build configuration
├── functions/                       # Firebase Cloud Functions
│   └── index.js                     # Server-side notification logic
├── gradle/                          # Gradle configuration
│   └── libs.versions.toml          # Version catalog for dependencies
├── build.gradle.kts                 # Project-level build configuration
└── settings.gradle.kts              # Project settings and module declarations
```

## 🔄 Data Flow & Architecture Patterns

### Clean Architecture Implementation

1. **Domain Layer** (core business logic)
    - Models: `User`, `Chat`, `ChatMessage`, etc.
    - Repository interfaces defining contracts
    - Use cases encapsulating specific business operations

2. **Data Layer** (data sources and implementations)
    - Repository implementations
    - Remote data sources (Firebase)
    - Local data sources (Room database)
    - Mappers for data conversion between layers

3. **Presentation Layer** (UI components)
    - ViewModels exposing UI state via StateFlow
    - Compose screens and components
    - Navigation handling

### Dual Firestore Chat Model

Instead of storing messages in a single document per chat, BondHub implements a dual model:

- Each participant has their own chat document in the `chats` collection
- Message synchronization happens through a shared `messages` subcollection
- Improves query performance and enables personalized unread counts

### Offline Support Implementation

- **Room Database**: Persistent local storage for chats and messages
- **Caching Strategy**: Data fetched from local storage first, then updated from remote
- **Sync Mechanism**: Background synchronization when connectivity is restored

### Push Notification with Deep Linking

- **Firebase Cloud Functions**: Server-side logic to trigger notifications on new messages
- **Deep Linking**: Notifications include intent data to navigate directly to the chat
- **Notification Actions**: Mark as read functionality directly from notification

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

🔁 Dual Firestore chat model → messages stored per user for optimized querying

🌐 Deep linked push notifications → open chats directly from notifications

📶 Offline-first → Room syncing with Firestore for seamless user experience

🔄 Reactive UI using StateFlow & SharedFlow → efficient state management

🧼 Modular, testable, and scalable project setup following Clean Architecture principles

🎨 Modern Material 3 design with smooth animations and intuitive UX

## 🤝 Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

## 📝 License
This project is licensed under the MIT License – see the LICENSE file for details.

## 📚 Additional Documentation

### Setup & Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/bondhub.git
   ```
2. **Open in Android Studio**
   - Ensure you have the latest stable version.
   - Import the project as a Gradle project.

3. **Firebase Configuration**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/).
   - Download `google-services.json` and place it in `app/src/main`.

4. **Run Firebase Emulators (For Local Development)**
   - Install the [Firebase CLI](https://firebase.google.com/docs/cli).
   - Run `firebase emulators:start` for local emulation.

5. **Build & Run**
   - Run the app on an emulator or Android device (API Level 23+ recommended).

6. **Deploy Cloud Functions**
   - In the `functions/` directory, configure and deploy your custom notification handlers.

### Frequently Asked Questions (FAQ)

**Q: How is privacy ensured?**  
A: All chat data transmits via secure HTTPS. End-to-end encryption is planned for a future release.

**Q: Can I extend the app for new features?**  
A: Yes! The clean modular structure supports easy addition of features like reactions, voice, or
media.

**Q: Are pull requests welcome?**  
A: Absolutely. See the [Contributing](#contributing) section for guidelines.

**Q: Is there support for internationalization (i18n)?**  
A: Multi-language support is planned for future updates.

### Known Issues / Limitations

- End-to-end encryption is not yet available.
- Media sharing is partially implemented; support for video/voice is on the roadmap.
- Some Material 3 features may behave differently on older devices.

### Community & Contact

- Twitter: [@bondhubapp](https://twitter.com/bondhubapp)
- LinkedIn: [Sarfraz](https://www.linkedin.com/in/yourusername/)
- Discussions: Use GitHub Issues for questions, feature requests, or bug reports.

---

## 🙋‍♂️ Creator

Developed with 💙 by Sarfraz  
🔗 [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/yourusername/)  
Feel free to connect for collaborations, feedback, or just to chat!

