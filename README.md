# ✈️ TripSync

A smart travel planner app for Android developed in Kotlin. TripSync helps users organize, sync, and manage their trips effortlessly — from itinerary planning to shared checklists.

## 🌟 Features

- **Trip Management**: Create, edit, and view detailed trip itineraries  
- **Collaborative Sync**: Share trip plans and checklists with other users in real-time  
- **Smart Reminders**: Get notified before key travel events or departures  
- **Offline Support**: Access and update trip data even without internet  
- **Interactive UI**: Sleek, user-friendly interface with intuitive navigation  
- **Local Storage**: Persistent data with Room database  
- **Secure Syncing**: Cloud sync via Firebase Authentication & Firestore  

## 🛠️ Installation

### Clone the Repository

```bash
git clone https://github.com/t2ne/tripsync.git
```

### Open the Project

1. Launch Android Studio
2. Open the `tripsync` project directory
3. Sync Gradle files
4. Build and run on your emulator or device

### Requirements

- Android Studio Electric Eel or newer
- Minimum SDK: Android 21 (Lollipop)
- Target SDK: Android 34
- Kotlin version: 1.9.0 or newer

## 📂 Project Structure

- **app**: Main application module  
  - **src/main**
    - **java/com/example/tripsync**
      - `MainActivity.kt`: App launcher and navigation controller
      - `SplashActivity.kt`: Optional splash screen logic
      - **data**
        - `TripDatabase.kt`: Room database setup
        - `TripDao.kt`: Data access layer
        - `TripEntity.kt`: Trip model definitions
      - **ui**
        - `TripListFragment.kt`: Overview of planned trips
        - `TripDetailFragment.kt`: View/edit trip details
        - `ChecklistFragment.kt`: Shared trip checklist
        - `SettingsFragment.kt`: App preferences and sync options
      - **utils**
        - `NotificationScheduler.kt`: Handles reminders
        - `SyncManager.kt`: Manages Firebase sync operations
    - **res**
      - **layout**: Layout XMLs
      - **navigation**: Navigation graph
      - **values**: Strings, themes, styles

## 🔧 Usage

1. Open the app and create a new trip  
2. Add destinations, dates, and notes to build your itinerary  
3. Invite collaborators to view or edit shared plans  
4. Access trip checklists and receive timely notifications  
5. Use the app offline — syncs when you're back online  
6. Manage multiple trips with ease from a central hub  

## 📱 Supported Devices

- Android 5.0+ (Lollipop and above)  
- Phones and tablets  
- Internet required for cloud sync features  

## 🔐 Permissions

- `android.permission.INTERNET`: For Firebase syncing  
- `android.permission.VIBRATE`: For notifications  

## 📚 Libraries Used

- Firebase Authentication & Firestore  
- Room Database  
- Material Design Components  
- AndroidX Navigation  
- ViewModel & LiveData  
- Kotlin Coroutines  

## 👤 Author

t2ne - cyzuko

## 🙏 Acknowledgments

- Firebase for Android  
- Android Jetpack Libraries  
- Material Design  
- Kotlin Community
