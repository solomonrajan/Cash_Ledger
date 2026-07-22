<div align="center">
  # 💸 Cash Ledger
  **Your Personal Expense Tracker**
  
  [![Material 3](https://img.shields.io/badge/Material-3-blueviolet?style=for-the-badge&logo=materialdesign)](https://m3.material.io/)
  [![Kotlin](https://img.shields.io/badge/Kotlin-100%25-blue?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Android-green?style=for-the-badge&logo=android)](https://developer.android.com/jetpack/compose)

  <a href="https://github.com/solomonrajan/Cash/releases/latest">
    <img src="https://raw.githubusercontent.com/rubenpgrady/get-it-on-github/refs/heads/main/get-it-on-github.png" alt="Get it on GitHub" width="220" />
  </a>
</div>

## ✨ About The App

**Cash Ledger** is a beautiful, intuitive, and highly functional expense tracking application designed to help you stay on top of your finances. It makes categorizing, tracking, and understanding your daily spending completely effortless.

### 🎯 Who is this for?
Cash Ledger is built for **everyone**—from students managing their daily allowance to professionals tracking their monthly budgets. If you want a seamless, ad-free way to visualize where your money goes without the clutter, Cash Ledger is the app for you.

---

## 🎨 Design & Language

Built with modern aesthetics and performance in mind, Cash Ledger strictly follows **Google Material Design 3 (M3)** guidelines to provide a stunning visual experience.

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

---

## 🚀 Getting Started

Follow these steps to run the app locally on your machine.

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Latest Version)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/solomonrajan/Cash.git
   ```
2. **Open the project** in Android Studio.
3. Allow Android Studio to sync the Gradle build and fix any incompatibilities.
4. **Update Build Configuration:**
   Remove this line from the app's `app/build.gradle.kts` file if you run into signing issues on debug:
   ```kotlin
   signingConfig = signingConfigs.getByName("debugConfig")
   ```
5. **Run the App** on an Android emulator or a physical device!
