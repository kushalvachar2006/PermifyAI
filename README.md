<div align="center">

# <a name="permifyai"></a>PermifyAI

**PermifyAI** is an advanced, AI-powered privacy assistant for Android designed to help users identify and manage apps that request unnecessary or dangerous permissions. By leveraging the intelligence of **Google Gemini AI**, the app analyzes the *purpose* of an application and compares its requirements against the permissions actually granted by the user.

• [Features](#features) • [Tech Stack](#tech-stack) • [Backend Architecture](#architecture) • [Application Flow](#flow) • [Installation & Setup](#setup)

</div>

---

## <a name="features"></a><a name="features"></a>🚀 Features

*   **<a name="ai-scan"></a>AI-Powered Deep Scan**: Uses Gemini 1.5 Flash to determine if an app's permission requests are appropriate for its category (e.g., why does a Calculator need SMS access?).
*   **<a name="privacy-score"></a>Dynamic Privacy Score**: A real-time health meter (0-100) with a smooth Red-to-Green gradient visualization.
*   **<a name="trust-system"></a>User Trust System**: Ability to manually mark daily-use apps as "Trusted" to personalize your risk profile.
*   **<a name="detailed-insights"></a>Intelligent Risk Insights**: Provides a formatted explanation: *"As per GEMINI, [App Name] requests the following permissions..."*.
*   **<a name="app-manager"></a>Direct Action**: Integrated uninstallation feature to immediately remove high-risk applications.
*   **<a name="smart-filtering"></a>Smart Permission Filtering**: Focuses only on "Dangerous" permissions that actually impact your privacy.

---

## <a name="tech-stack"></a><a name="tech-stack"></a>🛠 Tech Stack

*   **<a name="language"></a>Language**: Java / Kotlin (Android)
*   **<a name="database"></a>Database**: SQLite via **Room Persistence Library** for local data management.
*   **<a name="networking"></a>Networking**: **Retrofit 2** & OkHttp for secure API communication.
*   **<a name="ai-model"></a>AI Model**: **Google Gemini 1.5 Flash** for intelligent permission profiling.
*   **<a name="background-tasks"></a>Background Tasks**: **WorkManager** for reliable scanning even when the app is in the background.
*   **<a name="ui-ux"></a>UI/UX**: **Material 3 Design** with ViewBinding for a modern, reactive interface.

---

## <a name="architecture"></a><a name="architecture"></a>🏗 Backend Architecture

The application follows a clean, modular architecture:

1.  **<a name="data-layer"></a>Data Layer**:
    *   `AppDatabase`: The central Room database hub.
    *   `DAOs`: Interfaces (`AppDao`, `PermissionDao`, `AiCacheDao`) defining SQL operations.
    *   `Entities`: Database models for Apps, Permissions, and AI Caches.
2.  **<a name="remote-layer"></a>Remote Layer**:
    *   `ApiClient`: Configures Retrofit with secure API key injection via `BuildConfig`.
    *   `AiService`: Defines the Gemini AI endpoints.
3.  **<a name="worker-layer"></a>Worker Layer**:
    *   `ScanWorker`: Orchestrates the entire scanning process, comparison logic, and risk tagging.

---

## <a name="flow"></a><a name="flow"></a>🔄 Application Flow

1.  **<a name="flow-1"></a>Refresh**: When "Scan Now" is clicked, the app clears previous data for a fresh analysis.
2.  **<a name="flow-2"></a>Discovery**: The system discovers all user-installed apps.
3.  **<a name="flow-3"></a>AI Consultation**: For each app, the backend checks the local cache or queries Gemini AI for a typical permission profile.
4.  **<a name="flow-4"></a>Comparison**: The logic extracts core keywords (e.g., "CAMERA", "LOCATION") and compares them against granted permissions.
5.  **<a name="flow-5"></a>Tagging**: Apps with unexplained dangerous permissions are flagged as **High** or **Critical**.
6.  **<a name="flow-6"></a>Reactive Update**: The Dashboard observes the database using `LiveData`, updating the Score and Risky List in real-time as the worker progresses.

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

## <a id="contributing"></a> Contributing

We welcome contributions from the community! PermifyAI is open-source and we encourage you to fork the repository and extend it with new features or enhancements.

### How to Contribute

1. **Fork the Repository**
   - Click the 'Fork' button at the top right of this repository

2. **Clone Your Fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/PermifyAI.git
   cd PermifyAI
   ```

3. **Create a Feature Branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```

4. **Make Your Changes**
   - Write clean, documented code
   - Follow the existing code style
   - Add comments where necessary

5. **Commit Your Changes**
   ```bash
   git add .
   git commit -m "Add: amazing new feature"
   ```

6. **Push to Your Fork**
   ```bash
   git push origin feature/amazing-feature
   ```

7. **Open a Pull Request**
   - Go to the original repository
   - Click 'New Pull Request'
   - Select your feature branch
   - Describe your changes in detail
     
---

## Support & Contact

- **GitHub Issues**: [Report bugs or request features](https://github.com/kushalvachar2006/PermifyAI/issues)
- **Email**: kushalv1306@gmail.com
- **Discussions**: [Join our discussions](https://github.com/kushalvachar2006/PermifyAI/discussions)

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
