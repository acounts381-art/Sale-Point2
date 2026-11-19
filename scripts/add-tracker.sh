#!/bin/bash
set -e
DEST=android/app/src/main/java/com/ptc/salepoint
mkdir -p "$DEST"
cp native/MainActivity.java native/TrackerPlugin.java native/TrackerService.java "$DEST/"
python3 scripts/patch_manifest.py
echo "Tracker added"
