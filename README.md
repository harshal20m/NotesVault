# 🔐 Vault — Secure Notes & Password Manager

A beautiful, fully offline, encrypted notes and password vault app for Android.
Built with **Kotlin 2.0 + Jetpack Compose** (latest modern stack).

---

## ✨ Features

### 📝 Notes & Checklists
- **Rich Text Editor**: Support for Bold, Italic, Underline, Strikethrough, Code, and Bulleted lists using a custom span-based engine.
- **Checklist Mode**: Structured interactive checklists with progress tracking.
- **Media Attachments**: Attach multiple images from gallery or capture directly from the camera.
- **Reminders**: Set time-based reminders for your notes (integrated with `WorkManager`).
- **Organization**: Pin notes to the top, add hashtags, and categorize with 13 vibrant note colors.
- **Locked Notes**: Protect individual notes with your PIN/Biometrics.
- **Trash System**: Soft-delete notes with the ability to restore or permanently delete them.

### 🔑 Password Vault
- **AES-256-GCM Encryption**: All passwords are encrypted using the hardware-backed Android Keystore.
- **Password Strength Meter**: Real-time evaluation of password complexity.
- **Strong Password Generator**: Create secure, random passwords with a single tap.
- **Categories**: Organize logins into Social, Finance, Entertainment, Work, Shopping, etc.
- **Favorites**: Quick access to your most-used credentials.

### 🎨 Personalization & UX
- **12 Dynamic Themes**: Midnight, Cloud, Forest, Rose, Ocean, Amber, Violet, Abyss, Mono, Sunset, Cherry, and Arctic.
- **Floating Navigation**: Modern, shadow-driven bottom navigation bar for seamless screen switching.
- **Smart Search**: Filter content by title, text, tags, or specific states (Pinned, Media, Locked).
- **Home Widget**: Quick-add functionality via a Compose-based Glance widget.

---

## 🏗️ Architecture & Stack

- **UI**: Jetpack Compose (100%), Material 3, Custom Theming System.
- **Concurrency**: Kotlin Coroutines & Flow for reactive UI updates.
- **Dependency Injection**: Hilt (Dagger) for modular and testable code.
- **Local Storage**: 
    - **Room DB**: Structured storage for Notes and Passwords.
    - **DataStore**: Encrypted preferences and app settings.
- **Security**:
    - **Android Keystore**: Hardened encryption keys.
    - **Biometric API**: Seamless fingerprint/face unlock integration.
    - **SHA-256 Hashing**: PINs are never stored in plaintext.
- **Media**: Coil for high-performance image loading.
- **Worker**: WorkManager for reliable reminder notifications.
- **Serialization**: GSON for complex data types (spans, media lists).

---

## 🔒 Security First

Vault is designed with a **Zero-Trust** local-only approach:
- **No Network Access**: The app is intentionally built without network permissions to ensure your data never leaves your device.
- **Hardware Encryption**: Encryption keys are generated and stored in the device's Secure Element/TEE.
- **Auto-Lock**: Configurable timeout (Immediate, 1 min, 5 min, etc.) to keep your vault safe even if you leave the app open.
- **Encrypted Backups**: Export your data as an encrypted JSON file for safe-keeping.

---

## 🚀 Getting Started

### Requirements
- **Android Studio Ladybug (2024.2.1)** or newer.
- **JDK 17** & **Android SDK 35**.
- **Min SDK 26** (Android 8.0+).

### Setup
1. Clone the repository.
2. Open in Android Studio and perform a Gradle Sync.
3. Run on a physical device (recommended for Biometrics) or an emulator with Google Play Services.

---

## 🎨 Project Structure

```text
VaultApp/
├── data/
│   ├── local/          # Room DAOs, Database, PreferencesManager
│   ├── model/          # Note, PasswordEntry, AppSettings, Enums
│   └── repository/     # Repository pattern (Flow-based)
├── ui/
│   ├── screens/        # Compose screens (Home, NoteEdit, Vault, Settings, etc.)
│   │   └── editor/     # Custom Rich Text & Checklist engine
│   ├── components/     # Reusable UI widgets (Toasts, Dialogs, etc.)
│   └── theme/          # Custom VaultColors system & Material3 wrappers
├── util/
│   ├── CryptoManager   # Keystore-backed AES-256 & SHA-256 logic
│   ├── BackupManager   # Encrypted JSON import/export
│   └── ShareHelper     # WhatsApp text + rendered image sharing
└── widget/
    └── QuickAddWidget  # Glance-based Home Screen widget
```

---

## 📦 Roadmap

- [x] **Phase 1**: Core Architecture, Encryption, and Basic Note/Password management.
- [x] **Phase 2**: Rich Text Spans, Media Attachments, and Reminders.
- [ ] **Phase 3**: Trash restore UI, Tag management dashboard, and Backup auto-scheduling.
- [ ] **Phase 4**: WearOS companion app and Desktop (Compose Multiplatform) support.

---

## 📝 Developer Notes
- `CryptoManager.encrypt/decrypt` uses per-encryption IVs stored with the ciphertext.
- All note content is stored as serialized JSON to preserve rich text formatting and checklist state while maintaining Room compatibility.
- The app uses `network_security_config.xml` to strictly enforce no-network policies at the OS level.
