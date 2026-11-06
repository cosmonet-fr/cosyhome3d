"""Outils d'édition des fichiers .properties sans toucher à leur encodage.

Les fichiers sont lus et réécrits octet par octet (décodage latin-1, sans perte),
ce qui préserve ISO-8859-1, UTF-8 et les fins de ligne CRLF d'origine.
"""
import re


def load(path):
    with open(path, 'rb') as f:
        return f.read().decode('latin-1')


def save(path, text):
    with open(path, 'wb') as f:
        f.write(text.encode('latin-1'))


def entries(text):
    """Renvoie la liste (clé, début, fin) des entrées logiques, continuations comprises."""
    lines = text.splitlines(keepends=True)
    result, pos, i = [], 0, 0
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()
        start = pos
        if not stripped or stripped[0] in '#!':
            pos += len(line); i += 1
            continue
        key = re.split(r'(?<!\\)[=:]', stripped, maxsplit=1)[0].strip()
        while True:
            pos += len(line); i += 1
            body = line.rstrip('\r\n')
            backslashes = len(body) - len(body.rstrip('\\'))
            if backslashes % 2 == 0 or i >= len(lines):
                break
            line = lines[i]
        result.append((key, start, pos))
    return result


def eol(text):
    return '\r\n' if '\r\n' in text else '\n'


def set_value(text, key, value):
    """Remplace la valeur d'une clé existante. Renvoie (texte, trouvé)."""
    for k, start, end in entries(text):
        if k == key:
            return text[:start] + key + '=' + value + eol(text) + text[end:], True
    return text, False


def remove(text, key):
    for k, start, end in entries(text):
        if k == key:
            return text[:start] + text[end:], True
    return text, False


def get(text, key):
    for k, start, end in entries(text):
        if k == key:
            return text[start:end]
    return None
