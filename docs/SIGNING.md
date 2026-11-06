# Release signing with GitHub Secrets

The workflow signs release builds using four repository secrets. Nothing secret is stored in the repository, and `*.jks` / `*.keystore` files are git-ignored.

## 1. Create a keystore (once, on your computer)

```bash
keytool -genkeypair -v \
  -keystore stackpad-release.jks \
  -alias stackpad \
  -keyalg RSA -keysize 4096 -validity 10000
```

Choose strong passwords and remember them. **Back up the `.jks` file and passwords somewhere safe.** If you lose them you cannot publish updates to the same app.

## 2. Convert the keystore to text

macOS / Linux:

```bash
base64 -w 0 stackpad-release.jks > keystore.b64     # on macOS use: base64 -i stackpad-release.jks -o keystore.b64
```

Windows (PowerShell):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("stackpad-release.jks")) | Out-File keystore.b64
```

## 3. Add the secrets

In GitHub: **Repository → Settings → Secrets and variables → Actions → New repository secret**.

| Secret name | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Contents of `keystore.b64` |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | `stackpad` (the alias you chose) |
| `ANDROID_KEY_PASSWORD` | Key password |

Delete `keystore.b64` afterwards. Never commit it.

## 4. How it works

- `.github/workflows/android.yml` decodes `ANDROID_KEYSTORE_BASE64` into a temporary file on the runner.
- It passes the file path and passwords to Gradle as environment variables.
- `app/build.gradle.kts` reads them and signs the `release` build type only when they are present.
- The keystore file is deleted at the end of the run.

Without the secrets (for example on pull requests from forks), the workflow still runs the tests and builds the debug APK, and skips the release steps.

## Using the signed build

Open the workflow run → **Artifacts** → `stackpad-release` for the signed APK and AAB.

## Signing locally (optional)

```bash
export ANDROID_KEYSTORE_PATH=/path/to/stackpad-release.jks
export ANDROID_KEYSTORE_PASSWORD=...
export ANDROID_KEY_ALIAS=stackpad
export ANDROID_KEY_PASSWORD=...
./gradlew assembleRelease
```

## Safety rules

- Never paste secrets into code, issues, commit messages or logs.
- GitHub masks secret values in logs, but do not print them anyway.
- If a secret leaks, delete it in GitHub and create a new keystore if the keystore itself leaked.
