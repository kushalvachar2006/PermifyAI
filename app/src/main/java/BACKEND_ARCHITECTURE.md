# PermifyAI Backend Architecture Documentation

This document provides a detailed explanation of the backend architecture for PermifyAI, explaining how the application manages data, performs AI analysis, and handles background processing.

## 1. Data Layer (Local Storage)
The app uses the **Room Persistence Library** (an abstraction over SQLite) to maintain state and provide a reactive UI experience.

### `data/local/AppDatabase.java`
The central hub of the database. It:
- Defines the database configuration and versioning.
- Connects the three main entities (tables) to the system.
- Provides access to the Data Access Objects (DAOs).
- Implements `fallbackToDestructiveMigration()` to ensure schema changes don't crash the app during development.

### `data/local/entities/` (Database Tables)
- **`AppEntity.java`**: Represents an installed application. Stores critical metadata like the calculated `riskLevel`, `riskExplanation`, and `isUserConsented` (tracking if the user manually marked the app as trusted).
- **`PermissionEntity.java`**: Stores specific details about the permissions granted to each app, including whether they are classified as "Dangerous" by Android.
- **`AiCacheEntity.java`**: A performance-optimization table that caches Gemini AI responses for 7 days. This reduces API costs and enables offline analysis for previously scanned apps.

### `data/local/dao/` (Data Access Objects)
- **`AppDao.java`**: Contains SQL queries for the apps table. Features include fetching risky apps, updating user consent, and clearing the database for a fresh scan.
- **`PermissionDao.java`**: Manages the storage and retrieval of specific permissions linked to an application's package name.
- **`AiCacheDao.java`**: Handles the storage and lookup of AI-provided permission recommendations based on app names.

---

## 2. Remote Layer (Cloud Intelligence)
The app integrates with **Google Gemini AI** to provide human-like intelligence for privacy analysis.

### `data/remote/ApiClient.java`
- Configures the **Retrofit 2** HTTP client.
- Centralizes the Gemini API Key.
- Includes a logging interceptor for debugging network traffic during production development.

### `data/remote/AiService.java`
- Defines the endpoint for the Gemini `generateContent` API.
- Contains the JSON request/response models required to communicate with the Generative Language API.

---

## 3. Core Logic & Background Processing

### `workers/ScanWorker.java`
The "brain" of the application. Running via **WorkManager**, it performs the following:
1. **System Discovery**: Iterates through all non-system apps on the device.
2. **Permission Extraction**: Communicates with the Android Package Manager to retrieve only the permissions actually *granted* by the user.
3. **AI-Driven Profiling**: Sends app names to Gemini to get a "typical" permission profile.
4. **Semantic Comparison**: Uses a sophisticated keyword mapping system to compare granted permissions against AI recommendations.
5. **Risk Tagging**: Flags apps as High/Critical risk if they possess dangerous permissions that the AI deems unnecessary for that app category.

---

## 4. Utilities

### `utils/PermissionUtils.java`
- Provides static helper methods to interact with the Android OS.
- Specifically handles the logic to filter for `REQUESTED_PERMISSION_GRANTED` flags, ensuring the app only reports on active threats.

### `utils/Constants.java`
- Maintains consistency across the app by defining intent keys and standardized Risk Level strings (`Low`, `Medium`, `High`, `Critical`).

---

## Backend Execution Flow Summary
1. **Trigger**: User clicks "Scan Now".
2. **Cleanup**: `AppDao` clears the local database for a fresh start.
3. **Background**: `ScanWorker` starts. For each app, it checks the local `AiCache` or calls the `AiService`.
4. **Analysis**: Local comparison logic flags unusual permissions.
5. **Storage**: Results are saved into Room entities.
6. **Reactive UI**: Because the UI observes the DAOs via `LiveData`, the Dashboard updates in real-time as the worker completes each app's analysis.
