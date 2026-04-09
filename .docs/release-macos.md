# Releasing Ofis for macOS

Ofis is distributed as a DMG file hosted on GitHub Releases. This guide covers two phases: a quick unsigned release you can do right now, and a properly signed + notarized release for when you need a frictionless user experience.

The automated path (GitHub Actions) handles everything on a tag push — the sections below also describe how to do each step manually from your Mac.

---

## Phase 1 — Unsigned release (no Apple account required)

Unsigned apps work fine, but macOS Gatekeeper blocks them by default. Users can bypass this manually:

- **macOS 13–14 (Ventura / Sonoma):** right-click the app → **Open**
- **macOS 15+ (Sequoia):** System Settings → Privacy & Security → scroll to Security → **Open Anyway**

This is acceptable for early distribution. The friction increases with newer macOS versions, which is why Phase 2 exists.

> **Note on ad-hoc signing:** `codesign -s -` (ad-hoc) does *not* help here. Ad-hoc signatures are tied to the build machine and are rejected on any other Mac.

### Prerequisites (one-time)

1. Install required tools:
   ```sh
   brew install librsvg create-dmg gh
   ```
2. Authenticate the GitHub CLI:
   ```sh
   gh auth login
   ```

### Steps

1. Bump the version in `src/macosMain/resources/Info.plist`:
   ```xml
   <key>CFBundleShortVersionString</key>
   <string>1.2.0</string>
   ```

2. Commit and create a version tag:
   ```sh
   git add src/macosMain/resources/Info.plist
   git commit -m "bump version to 1.2.0"
   git tag v1.2.0
   git push origin master --tags
   ```

3. Build the DMG:
   ```sh
   make macos_dmg
   # → build/Ofis.dmg
   ```

4. Publish to GitHub Releases:
   ```sh
   make macos_release
   ```

5. Share the release URL with users and include bypass instructions from the intro above.

---

## Phase 2 — Signed + Notarized release (requires Apple Developer account)

Signing alone is **not enough** — both signing *and* notarization are required together. An app signed with a Developer ID but not notarized still triggers a Gatekeeper warning. Notarization requires an [Apple Developer Program](https://developer.apple.com/programs/) membership ($99/year).

### One-time setup

1. Enroll in the Apple Developer Program at [developer.apple.com](https://developer.apple.com).

2. Create a **Developer ID Application** certificate:
   - In Xcode: Settings → Accounts → your Apple ID → Manage Certificates → `+` → Developer ID Application
   - Or via the portal: [developer.apple.com/account/resources/certificates](https://developer.apple.com/account/resources/certificates)

3. The certificate is automatically added to your Keychain. Confirm with:
   ```sh
   security find-identity -v -p codesigning | grep "Developer ID"
   ```
   It will show something like `Developer ID Application: Your Name (ABCD1234EF)` — note the name and Team ID in parentheses.

4. Generate an **app-specific password** for notarization:
   - Go to [appleid.apple.com](https://appleid.apple.com) → Sign-In and Security → App-Specific Passwords
   - Create one labeled `ofis-notarize` and save it somewhere safe

5. Find your **Team ID** at [developer.apple.com/account](https://developer.apple.com/account) under Membership Details.

### Steps

1. Follow Phase 1 steps 1–3 (bump version, tag, build DMG).

2. Sign the app:
   ```sh
   SIGN_ID="Developer ID Application: Your Name (ABCD1234EF)" make macos_sign
   ```

3. Notarize and staple:
   ```sh
   APPLE_ID="you@example.com" \
   APPLE_TEAM_ID="ABCD1234EF" \
   APPLE_APP_PASSWORD="xxxx-xxxx-xxxx-xxxx" \
   make macos_notarize
   ```
   This submits the DMG to Apple's notary service and waits for the result (typically 2–5 minutes). The notarization ticket is then stapled to the DMG so it works offline.

4. Publish:
   ```sh
   make macos_release
   ```

Users will now see a calm first-launch prompt with no malware warnings.

### Adding signing to GitHub Actions

To automate signing in CI, add these as **repository secrets** (Settings → Secrets and variables → Actions):

| Secret | Value |
|---|---|
| `SIGN_ID` | `Developer ID Application: Your Name (TEAMID)` |
| `APPLE_ID` | your Apple ID email |
| `APPLE_TEAM_ID` | your Team ID |
| `APPLE_APP_PASSWORD` | the app-specific password |
| `MACOS_CERTIFICATE` | base64-encoded .p12 certificate export |
| `MACOS_CERTIFICATE_PWD` | password for the .p12 export |

Then update `.github/workflows/release.yml` — replace `make macos_dmg` with the signing steps:

```yaml
- name: Import certificate
  run: |
    echo "${{ secrets.MACOS_CERTIFICATE }}" | base64 --decode > cert.p12
    security create-keychain -p "" build.keychain
    security import cert.p12 -k build.keychain -P "${{ secrets.MACOS_CERTIFICATE_PWD }}" -T /usr/bin/codesign
    security list-keychains -s build.keychain
    security set-keychain-settings build.keychain
    security unlock-keychain -p "" build.keychain
    security set-key-partition-list -S apple-tool:,apple: -s -k "" build.keychain

- name: Build, sign, and notarize
  env:
    SIGN_ID: ${{ secrets.SIGN_ID }}
    APPLE_ID: ${{ secrets.APPLE_ID }}
    APPLE_TEAM_ID: ${{ secrets.APPLE_TEAM_ID }}
    APPLE_APP_PASSWORD: ${{ secrets.APPLE_APP_PASSWORD }}
  run: make macos_notarize
```
