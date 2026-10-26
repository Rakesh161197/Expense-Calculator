#!/usr/bin/env bash
# Downloads the 3 JS libraries your HTML loads from /assets/ into the app.
set -e
D="$(dirname "$0")/app/src/main/assets"
mkdir -p "$D"
curl -fsSL -o "$D/exceljs.min.js"                 https://cdn.jsdelivr.net/npm/exceljs@4.4.0/dist/exceljs.min.js
curl -fsSL -o "$D/jspdf.umd.min.js"               https://cdn.jsdelivr.net/npm/jspdf@2.5.1/dist/jspdf.umd.min.js
curl -fsSL -o "$D/jspdf.plugin.autotable.min.js"  https://cdn.jsdelivr.net/npm/jspdf-autotable@3.8.2/dist/jspdf.plugin.autotable.min.js
echo "Libraries downloaded to $D"
