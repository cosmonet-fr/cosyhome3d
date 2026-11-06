#!/bin/sh
# Cosy Home 3D launcher - sets up Java environment and launches the application
exec /app/jre/bin/java \
  --add-opens java.desktop/java.awt=ALL-UNNAMED \
  --add-opens java.desktop/sun.awt=ALL-UNNAMED \
  --enable-native-access=ALL-UNNAMED \
  -Dcom.eteks.sweethome3d.checkUpdates=false \
  -jar /app/share/cosyhome3d/CosyHome3D.jar \
  "$@"
