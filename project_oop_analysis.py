import pathlib, re, json
root = pathlib.Path(r'c:\Users\Giovanni\OneDrive\Documents\GitHub\PlatformerGame#\src')
files = sorted(root.rglob('*.java'))

counts = {
    'java_files': len(files),
    'classes': 0,
    'abstract_classes': 0,
    'interfaces': 0,
    'enums': 0,
    'methods': 0,
    'constructors': 0,
    'getters': 0,
    'setters': 0,
    'try_blocks': 0,
    'catch_blocks': 0,
    'switch_statements': 0,
    'case_labels': 0,
    'public_modifiers': 0,
    'private_modifiers': 0,
    'protected_modifiers': 0,
    'default_modifiers': 0,
    'fields': 0,
}

all_types = {}
class_names = {}

method_pattern = re.compile(r'^(?P<prefix>\s*(?:public|protected|private|static|final|synchronized|abstract|native|transient|volatile|\s)+)?\s*(?P<type>[\w<>\[\]]+)\s+(?P<name>[A-Za-z_][A-Za-z0-9_]*)\s*\([^;{]*\)\s*(?:throws\s+[^{]+)?\s*\{', re.MULTILINE)
constructor_pattern = re.compile(r'^(?P<prefix>\s*(?:public|protected|private)?\s*)?(?P<name>[A-Za-z_][A-Za-z0-9_]*)\s*\([^;{]*\)\s*\{', re.MULTILINE)
class_pattern = re.compile(r'^(?:\s*(public|protected|private)?\s*)?(abstract\s+)?(class|interface|enum)\s+([A-Za-z_][A-Za-z0-9_]*)', re.MULTILINE)
field_pattern = re.compile(r'^(?P<prefix>\s*(?:public|protected|private|static|final|transient|volatile|synchronized|\s)+)?\s*(?P<type>[A-Za-z0-9_<>,\[\].]+)\s+(?P<name>[A-Za-z_][A-Za-z0-9_]*)\s*(?:=|;|,)', re.MULTILINE)

def normalize_type(t):
    t = t.strip()
    t = re.sub(r'\s+', ' ', t)
    t = re.sub(r'<.*>', lambda m: '<' + re.sub(r'\s+', ' ', m.group(0)[1:-1]) + '>', t)
    return t

for file in files:
    text = file.read_text(encoding='utf-8')
    text_nocomments = re.sub(r'//.*|/\*.*?\*/', '', text, flags=re.S)
    class_names[file] = []
    for m in class_pattern.finditer(text_nocomments):
        kind = m.group(3)
        name = m.group(4)
        if kind == 'class':
            if m.group(2):
                counts['abstract_classes'] += 1
            else:
                counts['classes'] += 1
        elif kind == 'interface':
            counts['interfaces'] += 1
        elif kind == 'enum':
            counts['enums'] += 1
        class_names[file].append(name)

    counts['try_blocks'] += len(re.findall(r'\btry\b', text_nocomments))
    counts['catch_blocks'] += len(re.findall(r'\bcatch\b', text_nocomments))
    counts['switch_statements'] += len(re.findall(r'\bswitch\b', text_nocomments))
    counts['case_labels'] += len(re.findall(r'\bcase\b', text_nocomments))
    counts['public_modifiers'] += len(re.findall(r'\bpublic\b', text_nocomments))
    counts['private_modifiers'] += len(re.findall(r'\bprivate\b', text_nocomments))
    counts['protected_modifiers'] += len(re.findall(r'\bprotected\b', text_nocomments))

    constructors = 0
    methods = 0
    for m in method_pattern.finditer(text_nocomments):
        name = m.group('name')
        ret = m.group('type')
        methods += 1
        if name.startswith('get') or name.startswith('is'):
            counts['getters'] += 1
        if name.startswith('set'):
            counts['setters'] += 1
        if ret:
            t = normalize_type(ret)
            all_types[t] = all_types.get(t, 0) + 1

    for m in constructor_pattern.finditer(text_nocomments):
        name = m.group('name')
        if name in class_names[file]:
            constructors += 1

    counts['methods'] += methods
    counts['constructors'] += constructors

    for m in field_pattern.finditer(text_nocomments):
        t = normalize_type(m.group('type'))
        all_types[t] = all_types.get(t, 0) + 1
        counts['fields'] += 1

# count default modifier occurrences for classes/methods/fields without explicit public/private/protected in declarations
# approximate by checking lines with 'class' or method/field patterns lacking access modifier

with open('project_oop_summary.txt', 'w', encoding='utf-8') as f:
    f.write('PlatformerGame OOP Analysis\n')
    f.write('===========================\n\n')
    f.write(f'Total Java files: {counts["java_files"]}\n')
    f.write(f'Classes: {counts["classes"]}\n')
    f.write(f'Abstract classes: {counts["abstract_classes"]}\n')
    f.write(f'Interfaces: {counts["interfaces"]}\n')
    f.write(f'Enums: {counts["enums"]}\n\n')
    f.write(f'Methods: {counts["methods"]}\n')
    f.write(f'Constructors: {counts["constructors"]}\n')
    f.write(f'Getters: {counts["getters"]}\n')
    f.write(f'Setters: {counts["setters"]}\n')
    f.write(f'Try blocks: {counts["try_blocks"]}\n')
    f.write(f'Catch blocks: {counts["catch_blocks"]}\n')
    f.write(f'Switch statements: {counts["switch_statements"]}\n')
    f.write(f'Case labels: {counts["case_labels"]}\n\n')
    f.write('Access modifiers counts:\n')
    f.write(f'  public: {counts["public_modifiers"]}\n')
    f.write(f'  private: {counts["private_modifiers"]}\n')
    f.write(f'  protected: {counts["protected_modifiers"]}\n')
    f.write('\nData types used (unique types and occurrences):\n')
    for t, c in sorted(all_types.items(), key=lambda x: (-x[1], x[0])):
        f.write(f'  {t}: {c}\n')

print('Generated project_oop_summary.txt')
