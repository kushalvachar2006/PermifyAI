<div align="center">
   
# <a name="permifyai"></a>PermifyAI

**PermifyAI** is an advanced, AI-powered privacy assistant for Android designed to help users identify and manage apps that request unnecessary or dangerous permissions. By leveraging the intelligence of **Google Gemini AI**, the app analyzes the *purpose* of an application and compares its requirements against the permissions actually granted by the user.

• [Features](#features) • [Tech Stack](#tech-stack) • [Backend Architecture](#architecture) • [Application Flow](#flow)
• [Installation & Setup](#setup)

</div>

---

## <a name="features"></a><a name="features"></a>🚀 Features

*   **AI-Powered Deep Scan**: Uses Gemini 1.5 Flash to determine if an app's permission requests are appropriate for its category (e.g., why does a Calculator need SMS access?).
*   **Dynamic Privacy Score**: A real-time health meter (0-100) with a smooth Red-to-Green gradient visualization.
*   **User Trust System**: Ability to manually mark daily-use apps as "Trusted" to personalize your risk profile.
*   **Intelligent Risk Insights**: Provides a formatted explanation: *"As per GEMINI, [App Name] requests the following permissions..."*.
*   **Direct Action**: Integrated uninstallation feature to immediately remove high-risk applications.
*   **Smart Permission Filtering**: Focuses only on "Dangerous" permissions that actually impact your privacy.

---

## <a name="tech-stack"></a><a name="tech-stack"></a>🛠 Tech Stack

*   **Language**: Java / Kotlin (Android)
*   **Database**: SQLite via **Room Persistence Library** for local data management.
*   **Networking**: **Retrofit 2** & OkHttp for secure API communication.
*   **AI Model**: **Google Gemini 1.5 Flash** for intelligent permission profiling.
*   **Background Tasks**: **WorkManager** for reliable scanning even when the app is in the background.
*   **UI/UX**: **Material 3 Design** with ViewBinding for a modern, reactive interface.

---

## <a name="architecture"></a><a name="architecture"></a>🏗 Backend Architecture

The application follows a clean, modular architecture:

1.  **Data Layer**:
    *   `AppDatabase`: The central Room database hub.
    *   `DAOs`: Interfaces (`AppDao`, `PermissionDao`, `AiCacheDao`) defining SQL operations.
    *   `Entities`: Database models for Apps, Permissions, and AI Caches.
2.  **Remote Layer**:
    *   `ApiClient`: Configures Retrofit with secure API key injection via `BuildConfig`.
    *   `AiService`: Defines the Gemini AI endpoints.
3.  **Worker Layer**:
    *   `ScanWorker`: Orchestrates the entire scanning process, comparison logic, and risk tagging.

---

## <a name="flow"></a><a name="flow"></a>🔄 Application Flow

1.  **Refresh**: When "Scan Now" is clicked, the app clears previous data for a fresh analysis.
2.  **Discovery**: The system discovers all user-installed apps.
3.  **AI Consultation**: For each app, the backend checks the local cache or queries Gemini AI for a typical permission profile.
4.  **Comparison**: The logic extracts core keywords (e.g., "CAMERA", "LOCATION") and compares them against granted permissions.
5.  **Tagging**: Apps with unexplained dangerous permissions are flagged as **High** or **Critical**.
6.  **Reactive Update**: The Dashboard observes the database using `LiveData`, updating the Score and Risky List in real-time as the worker progresses.

---

## <a name="setup"></a><a name="setup"></a>⚙️ Installation & Setup

1.  Clone the repository.
2.  Obtain a Gemini API Key from [Google AI Studio](https://aistudio.google.com/).
3.  Add the key to your `local.properties` file:
    ```properties
    GEMINI_API_KEY=your_api_key_here
    ```
4.  Sync Gradle and run the app.

---
## Support & Contact

- **GitHub Issues**: [Report bugs or request features](https://github.com/kushalvachar2006/EventsHub/issues)
- **Email**: kushalvachar2006@gmail.com
- **Discussions**: [Join our discussions](https://github.com/kushalvachar2006/EventsHub/discussions)

---

## Author
<div align="center" style="font-size:28px; font-weight:700; padding:8px 16px; border-radius:10px; background:#1f2937; color:#ffffff;">
  Kushal V Achar
</div>



- GitHub: [@kushalvachar2006](https://github.com/kushalvachar2006)
- LinkedIn: [Connect with me](https://www.linkedin.com/in/kushal-v-achar-796049317/)

---
<div align="center">

[⬆ Back to Top](#top)


</div>
