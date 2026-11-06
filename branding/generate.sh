#!/bin/sh
# Génère toutes les images de marque FSH3D à partir des SVG de ce dossier.
# Dépendances : rsvg-convert (librsvg), magick (ImageMagick 7), inkscape, python3 + Pillow, police DejaVu Sans.
set -e
cd "$(dirname "$0")"
ROOT=..
RES=$ROOT/SweetHome3D/src/com/eteks/sweethome3d
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

png() { # png <svg> <taille> <sortie>
  rsvg-convert -w "$2" -h "$2" "$1" -o "$3"
}

# Icônes de l'application
png icon-small.svg 16  $RES/resources/frameIcon.png
png icon-small.svg 32  $RES/resources/frameIcon32x32.png
png icon.svg       128 $RES/resources/frameIcon128x128.png
png icon.svg       128 $RES/swing/resources/aboutIcon.png
png icon.svg       128 $RES/viewcontroller/resources/help/images/applicationIcon.png
png icon-small.svg 16  $RES/viewcontroller/resources/help/images/applicationIcon16.png
png icon-small.svg 32  $RES/viewcontroller/resources/help/images/applicationIcon32.png

# Splash screen
rsvg-convert -w 600  -h 300 splash.svg -o "$TMP/splash.png"
rsvg-convert -w 1200 -h 600 splash.svg -o "$TMP/splash2x.png"
magick "$TMP/splash.png"   -quality 92 $RES/resources/splashScreen.jpg
magick "$TMP/splash2x.png" -quality 92 $RES/resources/splashScreen@2x.jpg

# Flatpak
png icon-small.svg 32  $ROOT/flatpak/io.fsh3d.FSH3D-32.png
png icon.svg       128 $ROOT/flatpak/io.fsh3d.FSH3D.png
png icon.svg       256 $ROOT/flatpak/io.fsh3d.FSH3D-256.png
png icon.svg       512 $ROOT/flatpak/io.fsh3d.FSH3D-512.png
# SVG avec texte converti en tracés, pour ne pas dépendre des polices installées
inkscape icon.svg --export-text-to-path --export-plain-svg --export-filename=$ROOT/flatpak/io.fsh3d.FSH3D.svg 2>/dev/null

# Installeurs (Windows, macOS) et déploiement web historique
for s in 16 32 48; do png icon-small.svg $s "$TMP/i$s.png"; done
png icon.svg 256 "$TMP/i256.png"
magick "$TMP/i16.png" "$TMP/i32.png" "$TMP/i48.png" "$TMP/i256.png" $ROOT/SweetHome3D/install/windows/FSH3D.ico
png icon.svg 512 "$TMP/i512.png"
# ImageMagick ne sait pas écrire le format ICNS : on passe par Pillow
# (les icônes de documents reprennent provisoirement l'icône de l'application)
for f in 10.4/FSH3D/Contents/Resources/FSH3D.icns FSH3D/Contents/Resources/FSH3D.icns \
         FSH3D/Contents/Resources/FSH3DDocument.icns FSH3D/Contents/Resources/FSH3DFurnitureLibrary.icns \
         FSH3D/Contents/Resources/FSH3DLanguageLibrary.icns FSH3D/Contents/Resources/FSH3DPlugin.icns \
         FSH3D/Contents/Resources/FSH3DTexturesLibrary.icns; do
  python3 -c 'import sys; from PIL import Image; Image.open(sys.argv[1]).save(sys.argv[2], format="ICNS")' \
    "$TMP/i512.png" "$ROOT/SweetHome3D/install/macosx/$f"
done
png icon-small.svg 32 $ROOT/SweetHome3D/deploy/FSH3DIcon32x32.png
png icon-small.svg 48 $ROOT/SweetHome3D/deploy/FSH3DIcon48x48.png
png icon.svg 128 "$TMP/i128.png"
magick "$TMP/i128.png" $ROOT/SweetHome3D/deploy/FSH3DIcon.gif
cp $RES/resources/splashScreen.jpg $ROOT/SweetHome3D/deploy/FSH3DSplashScreen.jpg

echo "Images FSH3D générées."
