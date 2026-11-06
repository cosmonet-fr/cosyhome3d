#!/bin/sh
# Builds Cosy Home 3D application bundle and DMG for macOS, on a Mac.
# The architecture of the result (Apple Silicon arm64 or Intel x86_64) is the one
# of the JDK used: its runtime is embedded in the application.
# Requires: JDK 17 or later (with jpackage), and SweetHome3D/install/CosyHome3D-<version>.jar
# built beforehand with "ant jarExecutable" in SweetHome3D folder.
set -e
cd "$(dirname "$0")/../.."
VERSION=$(sed -n 's/.*<property name="version" value="\([^"]*\)".*/\1/p' SweetHome3D/build.xml)
ARCH=$(uname -m)
JAR=SweetHome3D/install/CosyHome3D-$VERSION.jar
WORK=build-macos
APP="$WORK/Cosy Home 3D.app"
DMG=dist/CosyHome3D-$VERSION-macos-$ARCH.dmg

rm -rf "$WORK" dist
mkdir -p "$WORK/input" dist
cp "$JAR" "$WORK/input/CosyHome3D.jar"

# jpackage refuses a version starting with 0 on macOS:
# the bundle is created with 1.0.0, then the real version is written in Info.plist
jpackage --type app-image \
  --name "Cosy Home 3D" \
  --app-version 1.0.0 \
  --vendor "Cosy Home 3D" \
  --copyright "Copyright 2026 Stéphane BILLOIS, based on Sweet Home 3D" \
  --input "$WORK/input" \
  --main-jar CosyHome3D.jar \
  --icon SweetHome3D/install/macosx/CosyHome3D/Contents/Resources/CosyHome3D.icns \
  --mac-package-identifier org.cosyhome3d.CosyHome3D \
  --mac-package-name "Cosy Home 3D" \
  --file-associations tools/macos/sh3d.properties \
  --java-options "-Xmx2048m" \
  --java-options "--add-opens=java.desktop/java.awt=ALL-UNNAMED" \
  --java-options "--add-opens=java.desktop/sun.awt=ALL-UNNAMED" \
  --java-options "--add-opens=java.desktop/com.apple.eawt=ALL-UNNAMED" \
  --java-options "--add-opens=java.desktop/com.apple.eio=ALL-UNNAMED" \
  --java-options "--enable-native-access=ALL-UNNAMED" \
  --java-options "-Dcom.eteks.sweethome3d.applicationId=CosyHome3D#macOS" \
  --dest "$WORK"

plutil -replace CFBundleShortVersionString -string "$VERSION" "$APP/Contents/Info.plist"
plutil -replace CFBundleVersion -string "$VERSION" "$APP/Contents/Info.plist"

# Ad hoc signature (no Apple developer account): required to run on Apple Silicon,
# but macOS will still ask user to confirm the opening of the application
codesign --force --deep --sign - "$APP"

hdiutil create -volname "Cosy Home 3D" -srcfolder "$APP" -ov -format UDZO "$DMG"
echo "$DMG ready"
