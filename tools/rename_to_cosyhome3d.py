#!/usr/bin/env python3
"""Renames FSH3D to Cosy Home 3D in text files, byte per byte to keep encodings.

- "FSH3D" used as a word in a text becomes "Cosy Home 3D"
- "FSH3D" in a file name or identifier (FSH3D.jar, lib/FSH3D, FSH3DDarkLaf,
  FSH3D#Installer...) becomes "CosyHome3D"
- "io.fsh3d.FSH3D" becomes "org.cosyhome3d.CosyHome3D", other "fsh3d" become "cosyhome3d"
"""
import re
import subprocess
import sys

SKIP_PREFIXES = ('flatpak-build/', 'CHANGELOG.txt', 'tools/rename_to_cosyhome3d.py')

IDENTIFIER = re.compile(rb'(?<=[/\\A-Za-z0-9_])FSH3D|FSH3D(?=[-_.#]?[A-Za-z0-9])')

def rename(data):
    data = data.replace(b'io.fsh3d.FSH3D', b'org.cosyhome3d.CosyHome3D')
    data = IDENTIFIER.sub(b'CosyHome3D', data)
    data = data.replace(b'FSH3D', b'Cosy Home 3D')
    data = data.replace(b'fsh3d', b'cosyhome3d')
    return data

def main():
    files = subprocess.run(['git', 'ls-files', '-co', '--exclude-standard'],
                           capture_output=True, check=True).stdout.decode().splitlines()
    changed = 0
    for path in files:
        if path.startswith(SKIP_PREFIXES):
            continue
        try:
            data = open(path, 'rb').read()
        except (IsADirectoryError, FileNotFoundError):
            continue
        if b'\0' in data[:8000]:
            continue
        new = rename(data)
        if new != data:
            open(path, 'wb').write(new)
            changed += 1
    print(changed, 'files changed')

if __name__ == '__main__':
    sys.exit(main())
