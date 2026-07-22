<div align="center">
  <img width="100%" alt="Cash App Banner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
  
  <br/>
  <br/>
  
  # 💸 Cash
  **Your Intelligent Expense Tracker powered by AI**
  
  [![Material 3](https://img.shields.io/badge/Material-3-blueviolet?style=for-the-badge&logo=materialdesign)](https://m3.material.io/)
  [![Kotlin](https://img.shields.io/badge/Kotlin-100%25-blue?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Android-green?style=for-the-badge&logo=android)](https://developer.android.com/jetpack/compose)

  <br/>
  <br/>

  <a href="https://github.com/solomonrajan/Cash/releases/latest">
    <img src="https://img.shields.io/badge/Get%20it%20on-GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="Get it on GitHub" height="48" />
  </a>
</div>

<br/>

## ✨ About The App

**Cash** is a smart, beautiful, and intuitive expense tracking application designed to help you stay on top of your finances. Leveraging the power of Google's Gemini AI, Cash makes categorizing and understanding your spending effortless.

### 🎯 Who is this for?
Cash is built for **everyone**—from students managing their daily allowance to professionals tracking their monthly budgets. If you want a seamless, ad-free, and intelligent way to visualize where your money goes without the clutter, Cash is the app for you.

---

## 🎨 Design & Language

Built with modern aesthetics and performance in mind, Cash strictly follows **Google Material Design 3 (M3)** guidelines to provide a stunning visual experience.

- **Dynamic Colors:** Adapts to your device's personalized theme and wallpaper (Material You).
- **Fluid Animations:** Smooth transitions and beautiful micro-interactions powered by Jetpack Compose.
- **Language:** Written **100% in [Kotlin](https://kotlinlang.org/)**, ensuring a robust, safe, and concise codebase.

---

## 🏗️ App Structure

The application follows the recommended **Modern Android Development (MAD)** architecture, ensuring scalability and maintainability:

- **UI Layer:** A fully declarative UI using **Jetpack Compose** for building native Android interfaces efficiently.
- **State Management:** Utilizes modern `ViewModel` and Kotlin Coroutines/Flows for reactive UI updates.
- **Local Database:** Robust on-device storage for your financial data using **Room** & **SQLite**.
- **Network & API:** **Retrofit** and **OkHttp** handle secure, asynchronous network requests.
- **AI Integration:** Integrated with Firebase AI and Google's **Gemini API** for intelligent expense analysis and smart categorization.

---

## 🚀 Getting Started

Follow these steps to run the app locally on your machine.

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Latest Version)
- A Gemini API Key

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/solomonrajan/Cash.git
   ```
2. **Open the project** in Android Studio.
3. Allow Android Studio to sync the Gradle build and fix any incompatibilities.
4. **Setup Environment Variables:**
   Create a file named `.env` in the root project directory and set your API key (refer to `.env.example`):
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
5. **Update Build Configuration:**
   Remove this line from the app's `app/build.gradle.kts` file if you run into signing issues on debug:
   ```kotlin
   signingConfig = signingConfigs.getByName("debugConfig")
   ```
6. **Run the App** on an Android emulator or a physical device!

> **Note:** If you have already published your app in AI Studio, please [request an upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in the Google Play Console if needed.
