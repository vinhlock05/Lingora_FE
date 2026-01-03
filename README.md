# Lingora Mobile

**Đồ án 1 - UIT**  
**Nhóm sinh viên thực hiện:**
- Tôn Vĩnh Lộc - 23520867
- Dương Khánh Ngọc - 23521020

## Description
- Android mobile application for **Lingora** - An intelligent English learning app with AI-powered features, vocabulary learning with SRS (Spaced Repetition System), IELTS exam practice, and community features.
- You can download the latest APK from GitHub Releases:
[⬇️ Download latest APK](https://github.com/vinhlock05/Lingora_FE/releases/download/v1.0.0/lingora.apk)

## 🚀 Tech Stack

### Core
- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 15)
- **Build System**: Gradle with Kotlin DSL

### UI Framework
- **Jetpack Compose** - Modern declarative UI
- **Material 3** - Material Design 3 components
- **Navigation Compose** - Type-safe navigation
- **Accompanist** - System UI controller, Flow Layout

### Architecture & DI
- **MVVM** - Model-View-ViewModel architecture
- **Hilt** - Dependency Injection
- **Arrow** - Functional programming (Either for error handling)
- **Kotlin Coroutines & Flow** - Async programming

### Networking
- **Retrofit** - REST API client
- **OkHttp** - HTTP client with logging interceptor
- **Gson** - JSON serialization

### Local Storage
- **DataStore** - Preferences storage (JWT tokens, settings)

### Media & UI
- **Coil** - Image loading
- **ExoPlayer (Media3)** - Audio/video playback
- **MPAndroidChart** - Charts & statistics
- **Compose Markdown** - Markdown rendering (for AI responses)

### Real-time & Auth
- **Socket.io Client** - Real-time notifications
- **Google Sign-In** - OAuth authentication

## ✨ Features

### 🔐 Authentication
- Email/Password login & registration
- Google Sign-In
- Email verification
- JWT token management (Access/Refresh)

### 📚 Vocabulary Learning
- **Categories → Topics → Words** hierarchy
- Flashcard learning with flip animations
- 6 Quiz types:
  - Nghe điền từ (Listen & Fill)
  - Nghe chọn từ (Listen & Choose)
  - Đúng/Sai (True/False)
  - Nhìn từ chọn nghĩa (Word → Meaning)
  - Nhìn nghĩa chọn từ (Meaning → Word)
  - Luyện phát âm (Pronunciation - Speech Recognition)
- SRS-based review system
- Progress tracking per topic

### 📝 IELTS Exam Practice
- Full test & section practice
- Reading, Listening, Writing, Speaking sections
- Attempt history & score tracking
- Timer with auto-submit

### 🤖 AI Chatbot
- RAG-powered English learning assistant
- Grammar & vocabulary explanations
- Markdown response rendering
- Conversation history

### 📖 Dictionary
- Word lookup with pronunciation
- Auto-suggest while typing
- Audio playback for pronunciation
- Example sentences

### 👥 Community (Forum)
- Study set sharing
- Comments & discussions
- Like/bookmark study sets

### 💰 Premium Features
- VNPay payment integration
- Subscription management
- Withdrawal requests

### 🔔 Notifications
- Real-time notifications via Socket.io
- Push notification support

### 👤 Profile
- User profile management
- Avatar upload (Cloudinary)
- Learning statistics

### 🛡️ Admin Panel
- User management (CRUD, Ban/Suspend)
- Course/Topic/Word management
- Dashboard with analytics

## 🛠 Development Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11+
- Android device/emulator (API 24+)

### 1. Clone & Open
```bash
git clone https://github.com/vinhlock05/Lingora_FE.git
cd Lingora_FE
```
Open the project in Android Studio.

### 2. Configure Backend URL
Edit `app/src/main/java/com/example/lingora_fe/util/Constant.kt`:

```kotlin
object Constant {
    // For local development with ngrok
    const val BASE_URL = "https://your-ngrok-url.ngrok-free.app"
}
```

### 3. Build & Run
```bash
./gradlew assembleDebug
```
Or use Android Studio's Run button.

### 4. Development with Local Backend
1. Start backend via Docker:
   ```bash
   docker compose up -d
   ```
2. Expose backend with ngrok:
   ```bash
   ngrok http 4000
   ```
3. Update `BASE_URL` with ngrok HTTPS URL.

## 📂 Project Structure

```
app/src/main/java/com/example/lingora_fe/
├── admin/                  # Admin module (CRUD management)
│   ├── common/             # Shared admin components
│   ├── course/             # Course management
│   ├── dashboard/          # Admin dashboard
│   ├── topic/              # Topic management
│   ├── user/               # User management
│   └── word/               # Word management
├── auth/                   # Authentication module
│   ├── data/               # API, DTOs, Repository
│   ├── di/                 # Hilt modules
│   ├── domain/             # Models, Repository interfaces
│   └── presentation/       # Screens, ViewModels
├── core/                   # Core utilities
│   ├── network/            # API response handling
│   └── ui/                 # Theme, shared components
├── di/                     # App-level DI modules
├── navigation/             # Navigation graph
├── user/                   # User-facing modules
│   ├── adaptivetest/       # Adaptive placement test
│   ├── chatbot/            # AI chatbot
│   ├── dictionary/         # Dictionary lookup
│   ├── exam/               # IELTS exam practice
│   ├── forum/              # Community/Forum
│   ├── notification/       # Notifications
│   ├── practice/           # Review practice
│   ├── profile/            # User profile
│   ├── studyset/           # Study sets
│   ├── vocabulary/         # Vocabulary learning
│   └── withdrawal/         # Premium withdrawals
└── util/                   # Utility classes
```

### Clean Architecture Layers (per module)
```
module/
├── data/
│   ├── datasource/         # Remote/Local data sources
│   ├── remote/
│   │   ├── api/            # Retrofit API interfaces
│   │   └── dto/            # Data Transfer Objects
│   └── repository/         # Repository implementations
├── di/                     # Hilt module
├── domain/
│   ├── model/              # Domain models
│   └── repository/         # Repository interfaces
└── presentation/
    ├── components/         # Reusable UI components
    ├── screen/             # Composable screens
    └── viewmodel/          # ViewModels & UI State
```

## 🔧 Key Dependencies

| Category | Library | Version |
|----------|---------|---------|
| UI | Jetpack Compose BOM | Latest |
| DI | Hilt | 2.48+ |
| Network | Retrofit | 2.9+ |
| Local DB | Room | 2.6+ |
| Images | Coil | 2.5+ |
| Media | ExoPlayer (Media3) | 1.2.1 |
| Auth | Google Sign-In | Latest |
| Realtime | Socket.io Client | 2.0+ |
| Functional | Arrow | 1.2+ |

## 📄 License

This project is developed for educational purposes as part of **Đồ án 1** at UIT.

---

> **Backend Repository**: [Lingora Backend](https://github.com/Ngoc95/Lingora-BE.git)
