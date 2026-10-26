# Expense App (Android WebView wrapper)

Your HTML runs offline inside the app (localStorage works, Excel/PDF exports save to Downloads).
Icon = spending.png (adaptive launcher icon). Package: com.tarun.expense, Android 10+.

## Option A - Android Studio (easiest)
1. Run `./get_libs.sh` (or manually download the 3 files listed in it into app/src/main/assets/).
2. File > Open > this folder, let Gradle sync.
3. Build > Build APK(s). Output: app/build/outputs/apk/debug/app-debug.apk

## Option B - GitHub (no installs)
Push this folder to a new GitHub repo > Actions > "Build APK" > Run workflow > download the artifact.

## Option C - command line
Needs JDK 17, Android SDK (platform 34), Gradle 8.7:
    ./get_libs.sh && gradle assembleDebug
