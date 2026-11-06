#!/bin/sh
# Generates the Breeze and Breeze Dark variants of action icons used by the
# light and dark themes, from the Breeze icon theme installed on the system
# (package breeze-icons, KDE). Icons without a Breeze equivalent keep their
# original version.
# Dependencies: rsvg-convert, Breeze icon theme in /usr/share/icons.
set -e
cd "$(dirname "$0")/.."
SRC=${BREEZE_DIR:-/usr/share/icons}
DEST=SweetHome3D/src/com/eteks/sweethome3d/swing/resources/actions

# Cosy Home 3D icon name -> Breeze icon (folder/name)
MAPPING="
document-new actions/document-new
document-open actions/document-open
document-open-recent actions/document-open-recent
document-save actions/document-save
document-save-as actions/document-save-as
document-close actions/document-close
document-print actions/document-print
document-print-preview actions/document-print-preview
document-print-pdf actions/viewpdf
application-exit actions/application-exit
application-preferences actions/configure
edit-undo actions/edit-undo
edit-redo actions/edit-redo
edit-cut actions/edit-cut
edit-copy actions/edit-copy
edit-paste actions/edit-paste
edit-paste-style actions/edit-paste-style
edit-clear actions/edit-clear
edit-find actions/edit-find
edit-select-all actions/edit-select-all
edit-select-all-at-all-levels actions/edit-select-all-layers
help-about actions/help-about
help-browser actions/help-contents
plan-zoom-in actions/zoom-in
plan-zoom-out actions/zoom-out
format-text-bold actions/format-text-bold
format-text-italic actions/format-text-italic
format-text-increase-size actions/format-font-size-more
format-text-decrease-size actions/format-font-size-less
plan-select actions/tool-pointer
plan-pan actions/transform-move
plan-create-polylines actions/draw-polyline
plan-create-rooms actions/draw-polygon
plan-create-dimension-lines actions/measure
plan-create-labels actions/draw-text
plan-locked actions/object-locked
plan-unlocked actions/object-unlocked
plan-flip-horizontally actions/object-flip-horizontal
plan-flip-vertically actions/object-flip-vertical
plan-import-background-image actions/insert-image
plan-add-level actions/layer-new
plan-delete-level actions/layer-delete
plan-make-level-viewable actions/view-visible
plan-make-level-unviewable actions/view-hidden
furniture-add actions/list-add
furniture-import actions/document-import
furniture-export-csv actions/document-export-table
group actions/object-group
ungroup actions/object-ungroup
align-horizontal-left actions/align-horizontal-left
align-horizontal-right actions/align-horizontal-right
align-vertical-top actions/align-vertical-top
align-vertical-bottom actions/align-vertical-bottom
distribute-horizontally actions/distribute-horizontal
distribute-vertically actions/distribute-vertical
sort-by actions/view-sort
display-column actions/view-file-columns
camera-photo devices/camera-photo
camera-video devices/camera-video
media-record actions/media-record
media-playback-start actions/media-playback-start
media-playback-pause actions/media-playback-pause
media-seek-forward actions/media-seek-forward
media-seek-backward actions/media-seek-backward
media-skip-forward actions/media-skip-forward
media-skip-backward actions/media-skip-backward
go-up actions/go-up
go-down actions/go-down
go-next actions/go-next
go-previous actions/go-previous
"

for theme in breeze breeze-dark; do
  mkdir -p "$DEST/$theme"
  echo "$MAPPING" | while read -r name breeze; do
    [ -z "$name" ] && continue
    svg="$SRC/$theme/${breeze%%/*}/16/${breeze#*/}.svg"
    if [ ! -e "$svg" ]; then
      echo "missing $svg" >&2
      continue
    fi
    # Keep SVG sources, required by Breeze license (LGPL 3)
    mkdir -p "branding/breeze/$theme"
    cp -L "$svg" "branding/breeze/$theme/$name.svg"
    rsvg-convert -w 16 -h 16 "$svg" -o "$DEST/$theme/$name.png"
    rsvg-convert -w 32 -h 32 "$svg" -o "$DEST/$theme/$name@2x.png"
  done
done
# License notice in icon folders, as asked by Breeze authors
for theme in breeze breeze-dark; do
  { echo "Icons of this folder come from the Breeze icon theme of KDE"
    echo "(https://invent.kde.org/frameworks/breeze-icons), renamed and converted to PNG,"
    echo "except plan-create-walls drawn for Cosy Home 3D in Breeze style."
    echo "SVG sources are in branding/breeze folder of Cosy Home 3D source code."
    echo
    cat branding/breeze-COPYING-ICONS.txt
  } > "$DEST/$theme/COPYING-ICONS.TXT"
done
echo "Breeze icons generated in $DEST/breeze and $DEST/breeze-dark"

# Cosy Home 3D icons drawn in Breeze style for actions without Breeze equivalent
TMP_ICONS=$(mktemp -d)
for svg in branding/icons/*.svg; do
  name=$(basename "$svg" .svg)
  cp "$svg" $TMP_ICONS/light.svg
  sed 's/#232629/#fcfcfc/' "$svg" > $TMP_ICONS/dark.svg
  rsvg-convert -w 16 -h 16 $TMP_ICONS/light.svg -o "$DEST/breeze/$name.png"
  rsvg-convert -w 32 -h 32 $TMP_ICONS/light.svg -o "$DEST/breeze/$name@2x.png"
  rsvg-convert -w 16 -h 16 $TMP_ICONS/dark.svg -o "$DEST/breeze-dark/$name.png"
  rsvg-convert -w 32 -h 32 $TMP_ICONS/dark.svg -o "$DEST/breeze-dark/$name@2x.png"
  rm -f "$TMP_ICONS"/light.svg "$TMP_ICONS"/dark.svg
done
rmdir "$TMP_ICONS"
