"""Renomme Sweet Home 3D en FSH3D dans les modèles d'installeurs (install/) et deploy/.

Les identifiants techniques précédés d'un point (com.eteks.sweethome3d.SweetHome3D, UTI macOS)
sont conservés pour la compatibilité. Les en-têtes de copyright d'origine aussi.
"""
import os, re

ROOT = os.path.join(os.path.dirname(__file__), '..', 'SweetHome3D')
COPY = 'Copyright (c) 2026 Stéphane BILLOIS'

def process(path, text):
    nl = '\r\n' if '\r\n' in text else '\n'
    name = os.path.basename(path)
    # En-têtes déformés par le premier renommage
    text = text.replace('FSH3D, Copyright (c) 2024 Space Mushrooms', 'FSH3D, based on Sweet Home 3D, Copyright (c) 2024 Space Mushrooms')
    text = text.replace('FSH3D Viewer, Copyright (c) 2024 Space Mushrooms', 'FSH3D Viewer, based on Sweet Home 3D, Copyright (c) 2024 Space Mushrooms')
    # Métadonnées Windows (launch4j, Inno Setup)
    text = text.replace('<copyright>Copyright (c) 2024 Space Mushrooms</copyright>', '<copyright>' + COPY + '</copyright>')
    text = text.replace('<companyName>Space Mushrooms</companyName>', '<companyName>FSH3D</companyName>')
    text = re.sub(r'^(AppCopyright|VersionInfoCopyright)=Copyright \(c\) 2024 Space Mushrooms', r'\1=' + COPY, text, flags=re.M)
    text = re.sub(r'^(AppPublisher|VersionInfoCompany)=Space Mushrooms', r'\1=FSH3D', text, flags=re.M)
    text = re.sub(r'^AppPublisherURL=http://www\.sweethome3d\.com\r?\n', '', text, flags=re.M)
    text = text.replace(' /du $qhttp://www.sweethome3d.com/$q', '')
    # macOS
    text = re.sub(r'Sweet Home 3D [0-9.]+ Copyright 2024 Space Mushrooms', 'FSH3D 0.1, ' + COPY.replace('(c) ', ''), text)
    text = re.sub(r'[ \t]*<key>UTTypeReferenceURL</key>\s*<string>http://www\.sweethome3d\.com/</string>\r?\n', '', text)
    # README portable
    if name == 'README.TXT' and 'portable' in path:
        text = re.sub(r'^https?://www\.sweethome3d\.com/\S*\r?\n', '', text, flags=re.M)
        text = re.sub(r'^Visit http://www\.sweethome3d\.com[^\n]*\n', '', text, flags=re.M)
        text = text.replace('\\eTeks\\FSH3D', '\\FSH3D\\FSH3D').replace('/eTeks/FSH3D', '/FSH3D/FSH3D')
        text = text.replace('FSH3D, Copyright (c) 2024 Space Mushrooms', 'FSH3D, based on Sweet Home 3D, Copyright (c) 2024 Space Mushrooms')
    # Noms : on garde les en-têtes "Sweet Home 3D, Copyright ..." et les mentions "based on"
    out = []
    for line in text.split('\n'):
        if not re.search(r'Copyright|based on Sweet Home 3D', line):
            line = line.replace('Sweet Home 3D', 'FSH3D')
            line = re.sub(r'(?<![.\w])SweetHome3D', 'FSH3D', line)
        out.append(line)
    return '\n'.join(out)

def main():
    changed = []
    for base in ('install', 'deploy'):
        for d, _, files in os.walk(os.path.join(ROOT, base)):
            for f in files:
                p = os.path.join(d, f)
                if f.endswith(('.jar', '.ico', '.icns', '.png', '.gif', '.jpg', '.isl')) or '/build' in p:
                    continue
                raw = open(p, 'rb').read()
                if b'\0' in raw:
                    continue
                t = raw.decode('latin-1')
                n = process(p, t)
                if n != t:
                    open(p, 'wb').write(n.encode('latin-1'))
                    changed.append(os.path.relpath(p, ROOT))
    print('\n'.join(sorted(changed)))

if __name__ == '__main__':
    main()
