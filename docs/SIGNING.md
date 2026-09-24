# Beta signing

Vibe Check beta APKs use one persistent beta key. It is not a Play production
key. Losing or rotating it prevents future APKs from updating installed beta
apps; keep an encrypted backup under separate access control.

## Local storage

- Keystore: `~/Library/Application Support/Vibe Check/signing/vibe-check-beta.jks`
- File permissions: owner read/write only (`chmod 600`)
- Alias Keychain item: service `Vibe Check Beta Signing`, account `key-alias`
- Store-password Keychain item: service `Vibe Check Beta Signing`, account `store-password`
- Key-password Keychain item: service `Vibe Check Beta Signing`, account `key-password`

The keystore and credentials must never be copied into the repository,
`local.properties`, build logs or release notes.

## GitHub Actions secrets

- `VIBE_CHECK_BETA_KEYSTORE_BASE64`
- `VIBE_CHECK_BETA_STORE_PASSWORD`
- `VIBE_CHECK_BETA_KEY_ALIAS`
- `VIBE_CHECK_BETA_KEY_PASSWORD`

The manual `Signed beta release` workflow runs only from `main`, decodes the
keystore into runner temporary storage, builds the non-debug APK, verifies its
signature, removes the temporary keystore and uploads the candidate artifact.
Pull requests and forks cannot publish or receive signing secrets.

## Local signed build

Load credentials without placing their values on the command line or in shell
history:

```sh
export VIBE_CHECK_BETA_KEYSTORE_PATH="$HOME/Library/Application Support/Vibe Check/signing/vibe-check-beta.jks"
export VIBE_CHECK_BETA_KEY_ALIAS="$(security find-generic-password -s 'Vibe Check Beta Signing' -a key-alias -w)"
export VIBE_CHECK_BETA_STORE_PASSWORD="$(security find-generic-password -s 'Vibe Check Beta Signing' -a store-password -w)"
export VIBE_CHECK_BETA_KEY_PASSWORD="$(security find-generic-password -s 'Vibe Check Beta Signing' -a key-password -w)"
gradle :app:assembleRelease
unset VIBE_CHECK_BETA_KEY_ALIAS VIBE_CHECK_BETA_STORE_PASSWORD VIBE_CHECK_BETA_KEY_PASSWORD
```

Verify an APK with the Android SDK `apksigner` tool:

```sh
apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk
```

To replace GitHub secrets after an authorised rotation, retrieve values from
the Keychain and pipe them to `gh secret set`; never paste values into a logged
command. Rotation requires users to uninstall the previous beta and loses its
app-private data unless they exported it first.
