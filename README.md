# Sale Point - Android APK (GitHub)

1. Create a new repository on GitHub.
2. Upload everything from this folder (including the `.github` folder) to the repository (branch `main`).
3. Open the **Actions** tab, choose **Build Sale Point APK** and press **Run workflow** (it also runs on every push).
4. When it finishes (about 5-8 minutes) open the run and download the **sale-point-apk** artifact. Unzip it and install `app-debug.apk` on the phone (allow "Install unknown apps").

## Background location tracking (6 AM - 8 PM)
Open Report > Total Area Covered. A setup card shows four items - tap Allow on each:
- Location
- Allow all the time (opens the app settings: Permissions > Location > Allow all the time)
- Tracking notification
- No battery restriction

After that the app records a point every minute from 6 AM to 8 PM, even when the app is closed.
If the phone is restarted, open the app once so tracking starts again.

## Updating the app
Replace `www/index.html` with the new version and push - the workflow builds a fresh APK.
