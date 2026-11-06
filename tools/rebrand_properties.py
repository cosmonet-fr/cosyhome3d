"""Retire les liens et mentions Sweet Home 3D des fichiers de traduction de l'application."""
import glob, os, sys
sys.path.insert(0, os.path.dirname(__file__))
import props

SRC = os.path.join(os.path.dirname(__file__), '..', 'SweetHome3D', 'src', 'com', 'eteks', 'sweethome3d')

ABOUT_EN = r'''<html><font face="sans-serif"><center><font size="+2"><b>Cosy Home 3D</b></font>\
    <br><b>Version %1$s</b>\
    <p><font size="-1">%4$s version %2$s<br>provided by %5$s\
    <br>Java 3D version %3$s\
    <br>PDF print provided by iText\
    <br>SVG export provided by <a href="https://github.com/freehep/freehep-vectorgraphics">FreeHEP</a>\
    <br>SVG path compilation provided by <a href="https://xmlgraphics.apache.org/batik/">Batik</a>\
    <br>Math expressions support provided by JeksParser\
    <br>Photo rendering provided by SunFlow and <a href="https://github.com/YafaRay">YafaRay</a></font></p>\
    <p>Cosy Home 3D is a fork of Sweet Home 3D 7.5,\
    <br>created by Emmanuel Puybaret.</p>\
    <p><font size="-2">\u00a9 Copyright 2026 Stéphane BILLOIS\
    <br>Original code \u00a9 Copyright 2024 Space Mushrooms\
    <br>Distributed under <a href="https://www.gnu.org/licenses/old-licenses/gpl-2.0.html">GNU General Public License</a></font></p>'''

ABOUT_FR = r'''<html><font face="sans-serif"><center><font size="+2"><b>Cosy Home 3D</b></font>\
    <br><b>Version %1$s</b>\
    <p><font size="-1">%4$s version %2$s<br>fourni par %5$s\
    <br>Java 3D version %3$s\
    <br>Impression PDF fournie par iText\
    <br>Export SVG fourni par <a href="https://github.com/freehep/freehep-vectorgraphics">FreeHEP</a>\
    <br>Compilation des chemins SVG fournie par <a href="https://xmlgraphics.apache.org/batik/">Batik</a>\
    <br>Calcul des expressions math\u00e9matiques fourni par JeksParser\
    <br>Rendu photo fourni par SunFlow et <a href="https://github.com/YafaRay">YafaRay</a></font></p>\
    <p>Cosy Home 3D est un fork de Sweet Home 3D 7.5,\
    <br>cr\u00e9\u00e9 par Emmanuel Puybaret.</p>\
    <p><font size="-2">\u00a9 Copyright 2026 St\u00e9phane BILLOIS\
    <br>Code d'origine \u00a9 Copyright 2024 Space Mushrooms\
    <br>Distribu\u00e9 sous licence <a href="https://www.gnu.org/licenses/old-licenses/gpl-2.0.html">GNU General Public License</a></font></p>'''

# Clés supprimées : sans elles, les boutons et liens vers sweethome3d.com disparaissent
REMOVED = ['HomePane.findMoreExamples.url', 'HomePane.findMoreExamplesMessage.text',
           'ImportedFurnitureWizardStepsPanel.findModelsButton.url',
           'ImportedFurnitureWizardStepsPanel.findModelsMessage.text',
           'ImportedTextureWizardStepsPanel.findImagesButton.url',
           'ImportedTextureWizardStepsPanel.findImagesMessage.text']

def main():
    report = []
    for path in sorted(glob.glob(os.path.join(SRC, 'swing', 'package*.properties'))):
        name = os.path.basename(path)
        text = props.load(path)
        orig = text
        for key in REMOVED:
            text, _ = props.remove(text, key)
        # Bouton "Trouver plus d'exemples" désactivé (valeur vide)
        text, _ = props.set_value(text, 'HomePane.newHomeFromExample.findMoreExamples', '')
        # En-tête des exports OBJ sans l'URL de Sweet Home 3D
        header = props.get(text, 'HomePane.exportToOBJ.header')
        if header and 'sweethome3d.com' in header:
            value = header.split('=', 1)[1].rstrip('\r\n').replace('\\nhttp://www.sweethome3d.com/', '')
            text, _ = props.set_value(text, 'HomePane.exportToOBJ.header', value)
        if name == 'package.properties':
            text, ok = props.set_value(text, 'HomePane.about.message', ABOUT_EN)
            assert ok
        elif name == 'package_fr.properties':
            text, ok = props.set_value(text, 'HomePane.about.message', ABOUT_FR)
            if not ok:
                anchor = props.get(text, 'HomePane.about.showLibraries') or props.get(text, 'HomePane.about.close')
                i = text.index(anchor) + len(anchor)
                text = text[:i] + 'HomePane.about.message=' + ABOUT_FR + props.eol(text) + text[i:]
        else:
            # Les messages "À propos" traduits sont obsolètes : retour au message anglais
            text, _ = props.remove(text, 'HomePane.about.message')
        if text != orig:
            props.save(path, text)
            report.append(name)
    print('Modifiés :', ', '.join(report))

if __name__ == '__main__':
    main()
