#!/bin/sh
# FSH3D launcher - sets up Java environment and launches the application
exec /app/jre/bin/java \
  --add-opens java.desktop/java.awt=ALL-UNNAMED \
  --add-opens java.desktop/sun.awt=ALL-UNNAMED \
  -jar /app/share/fsh3d/FSH3D.jar \
  "$@"
