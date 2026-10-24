# Supabase SQL Runner

Personal single-user Android database management tool to execute SQL queries via the Supabase Management API.

## Features
- **Supabase Credentials**: Input and store your Supabase Management API Token and Project Ref.
- **SQL Editor**: Text area to write, paste, and format SQL queries with quick-snippet templates and query history.
- **Run Query**: Executes SQL via `https://api.supabase.com/v1/projects/{ref}/database/query`.
- **Results Viewer**: Displays execution time, row count, formatted data table (with horizontal/vertical scrolling), raw JSON view, and error messages.

## Building the APK

### 1. In Google AI Studio
- Use the Settings/Export menu in AI Studio to download the compiled APK or AAB directly.

### 2. Via GitHub Actions
- This repository includes `.github/workflows/build-apk.yml`.
- Pushing to `main` will automatically build the APK and upload it as a downloadable artifact under the **Actions** tab on GitHub.

### 3. Locally on your machine
To build the debug APK using the Gradle wrapper:
```bash
# On Linux/macOS
./gradlew assembleDebug

# On Windows
gradlew.bat assembleDebug
```
The generated APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`
