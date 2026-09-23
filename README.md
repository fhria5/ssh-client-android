# SSH Client Android - Ringan & Stabil

SSH client Android optimized for stability (anti-disconnect) and lightweight APK.

## Fitur
- Full terminal emulator (ANSI color, key handling)
- Multi-session
- Connection manager (CRUD)
- Key management (generate, import, delete)
- Background foreground service + notification

## Anti-Disconnect (Real Keep-Alive)
- PARTIAL_WAKE_LOCK - CPU tetap awake selama SSH aktif
- SSH keep-alive packets - kirim setiap N detik via SSHJ keepAlive
- Auto-reconnect - 3x retry dengan jeda 5 detik
- START_STICKY - service restart otomatis jika killed by OS
- Foreground service - notification persisten, Android tidak kill

## Ringan
- Hapus SSHJ OSGI (ganti core saja)
- Hapus jediterm-core (custom terminal view)
- Hapus slf4j (tidak perlu)
- MinSdk 24, TargetSdk 33
- ProGuard enabled di release
- Shrink resources enabled

## Build Requirements
- Android Studio Hedgehog+ (2023.1.1+)
- Android SDK 33
- JDK 17
- Gradle 8.x

## Build
cd ssh-client-android
./gradlew assembleDebug

## Project Structure
app/src/main/
├── java/com/sshclient/
│   ├── ui/          # MainActivity, TerminalView, ConnectionManager, KeyManager, SftpBrowser
│   ├── service/     # SshService (foreground + keep-alive + wake lock)
│   ├── model/       # ConnectionConfig, SshSession
│   └── util/        # StorageUtil, ConnectionConfigSerializer
├── res/             # Layouts, strings, themes
└── AndroidManifest.xml