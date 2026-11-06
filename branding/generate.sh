#!/bin/sh
# Génère toutes les images de marque Cosy Home 3D à partir des SVG de ce dossier.
# icon-tiny.svg sert pour 16 px, icon-small.svg de 22 à 48 px, icon.svg à partir de 64 px :
# chaque taille a son propre rendu, pour rester net dans les lanceurs.
# Dépendances : rsvg-convert (librsvg), magick (ImageMagick 7), inkscape, python3 + Pillow, police DejaVu Sans.
set -e
cd "$(dirname "$0")"
ROOT=..
RES=$ROOT/SweetHome3D/src/com/eteks/sweethome3d
MAC=$ROOT/SweetHome3D/install/macosx
FLATPAK_ICONS=$ROOT/flatpak/icons
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

icon() { # icon <taille> <sortie> : choisit la variante adaptée à la taille
  if [ "$1" -le 16 ]; then svg=icon-tiny.svg
  elif [ "$1" -le 48 ]; then svg=icon-small.svg
  else svg=icon.svg
  fi
  rsvg-convert -w "$1" -h "$1" "$svg" -o "$2"
}

# Icônes de l'application
icon 16  $RES/resources/frameIcon.png
icon 32  $RES/resources/frameIcon32x32.png
icon 128 $RES/resources/frameIcon128x128.png
icon 128 $RES/swing/resources/aboutIcon.png
icon 128 $RES/viewcontroller/resources/help/images/applicationIcon.png
icon 16  $RES/viewcontroller/resources/help/images/applicationIcon16.png
icon 32  $RES/viewcontroller/resources/help/images/applicationIcon32.png

# Splash screen
rsvg-convert -w 600  -h 300 splash.svg -o "$TMP/splash.png"
rsvg-convert -w 1200 -h 600 splash.svg -o "$TMP/splash2x.png"
magick "$TMP/splash.png"   -quality 92 $RES/resources/splashScreen.jpg
magick "$TMP/splash2x.png" -quality 92 $RES/resources/splashScreen@2x.jpg

# Flatpak : une icône par taille standard hicolor, plus la version vectorielle
mkdir -p $FLATPAK_ICONS
for s in 16 22 24 32 48 64 128 256 512; do
  icon $s $FLATPAK_ICONS/org.cosyhome3d.CosyHome3D-$s.png
done
# SVG avec texte converti en tracés, pour ne pas dépendre des polices installées
inkscape icon.svg --export-text-to-path --export-plain-svg --export-filename=$FLATPAK_ICONS/org.cosyhome3d.CosyHome3D.svg 2>/dev/null

# Installeurs (Windows, macOS) et déploiement web historique
for s in 16 24 32 48 64 256 512; do icon $s "$TMP/i$s.png"; done
magick "$TMP/i16.png" "$TMP/i24.png" "$TMP/i32.png" "$TMP/i48.png" "$TMP/i64.png" "$TMP/i256.png" \
  $ROOT/SweetHome3D/install/windows/CosyHome3D.ico
# ImageMagick ne sait pas écrire le format ICNS : on passe par Pillow
# (les icônes de documents reprennent provisoirement l'icône de l'application)
for f in 10.4/CosyHome3D/Contents/Resources/CosyHome3D.icns \
         CosyHome3D/Contents/Resources/CosyHome3D.icns \
         CosyHome3D/Contents/Resources/CosyHome3DDocument.icns \
         CosyHome3D/Contents/Resources/CosyHome3DFurnitureLibrary.icns \
         CosyHome3D/Contents/Resources/CosyHome3DLanguageLibrary.icns \
         CosyHome3D/Contents/Resources/CosyHome3DPlugin.icns \
         CosyHome3D/Contents/Resources/CosyHome3DTexturesLibrary.icns; do
  python3 -c 'import sys; from PIL import Image; Image.open(sys.argv[1]).save(sys.argv[2], format="ICNS")' \
    "$TMP/i512.png" "$MAC/$f"
done
icon 32 $ROOT/SweetHome3D/deploy/CosyHome3DIcon32x32.png
icon 48 $ROOT/SweetHome3D/deploy/CosyHome3DIcon48x48.png
icon 128 "$TMP/i128.png"
magick "$TMP/i128.png" $ROOT/SweetHome3D/deploy/CosyHome3DIcon.gif
cp $RES/resources/splashScreen.jpg $ROOT/SweetHome3D/deploy/CosyHome3DSplashScreen.jpg

echo "Images Cosy Home 3D générées."
