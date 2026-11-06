"""Renomme Sweet Home 3D en Cosy Home 3D dans l'aide HTML intégrée et retire les liens vers leur site.

Travaille octet par octet (latin-1 sans perte) pour préserver l'encodage de chaque langue.
"""
import glob, os, re

HELP = os.path.join(os.path.dirname(__file__), '..', 'SweetHome3D', 'src', 'com', 'eteks',
                    'sweethome3d', 'viewcontroller', 'resources', 'help')
SITE = r'https?://www\.sweethome3d\.(?:com|eu)[^"]*'
# Mentions volontairement conservées (sans lien), marquées pour que le script reste rejouable
KEEP = '<span class="upstream">Sweet Home 3D</span>'

def process(text, name):
    # Lien "www.sweethome3d.com" sous le titre de l'index
    text = re.sub(r'<p>\s*<a class="bread" href="' + SITE + r'">[^<]*</a>\s*(<br\s*/?>)?\s*</p>', '', text, flags=re.I)
    text = re.sub(r'<P ALIGN=CENTER>www\.sweethome3d\.com[^<]*</P>', '', text, flags=re.I)
    # Bloc "Et sur le site web de Sweet Home 3D : FAQ / Guide / Vidéo / Astuces"
    text = re.sub(r'(<br>|<p>)?((?:(?!<[pP]>|<br>|</td>).){0,120}?)<a href="' + SITE + r'">[^<]*</a>'
                  r'(?:(?!</td>).)*?<a href="' + SITE + r'">[^<]*</a>(?:\s*/\s*<a href="' + SITE + r'">[^<]*</a>)*',
                  '', text, flags=re.S | re.I)
    # Autres liens : on garde le texte sans le lien. Dans importingFurniture, le site
    # de Sweet Home 3D reste cité (sans lien) car les bibliothèques SH3F y sont hébergées.
    def unlink(m):
        inner = m.group(1)
        if name == 'importingFurniture.html':
            return re.sub(r'Sweet\s+Home(?:\s|&nbsp;)+3D', KEEP, inner)
        return inner
    text = re.sub(r'<a\s+href="' + SITE + r'">(.*?)</a>', unlink, text, flags=re.S | re.I)
    parts = text.split(KEEP)
    parts = [re.sub(r'3D Sweet Home|Sweet\s+Home(?:(?:\s|&nbsp;)+3D)?', 'Cosy Home 3D', p) for p in parts]
    return KEEP.join(parts)

def main():
    changed = 0
    for path in sorted(glob.glob(os.path.join(HELP, '*', '*.html'))):
        raw = open(path, 'rb').read().decode('latin-1')
        new = process(raw, os.path.basename(path))
        if new != raw:
            open(path, 'wb').write(new.encode('latin-1'))
            changed += 1
    print(changed, 'fichiers modifiés')

if __name__ == '__main__':
    main()
