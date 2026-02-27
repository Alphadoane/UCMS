import os

def count_lines_in_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
            return sum(1 for _ in f)
    except Exception:
        return 0

def get_language(extension):
    ext_map = {
        '.kt': 'Kotlin',
        '.java': 'Java',
        '.xml': 'XML',
        '.js': 'JavaScript',
        '.ts': 'TypeScript',
        '.html': 'HTML',
        '.css': 'CSS',
        '.py': 'Python',
        '.json': 'JSON',
        '.md': 'Markdown',
        '.bat': 'Batch',
        '.sh': 'Shell',
        '.gradle': 'Gradle',
        '.kts': 'Kotlin Script',
        '.properties': 'Properties'
    }
    return ext_map.get(extension, 'Other')

def main():
    root_dir = os.getcwd()
    ignored_dirs = {'.git', 'node_modules', 'build', '.gradle', '.idea', 'captures', '.cxx', 'dist', '.parcel-cache', 'coverage'}
    
    language_counts = {}
    total_lines = 0

    for dirpath, dirnames, filenames in os.walk(root_dir):
        # Modify dirnames in-place to skip ignored directories
        dirnames[:] = [d for d in dirnames if d not in ignored_dirs]
        
        for filename in filenames:
            ext = os.path.splitext(filename)[1].lower()
            if not ext:
                continue
            
            language = get_language(ext)
            filepath = os.path.join(dirpath, filename)
            lines = count_lines_in_file(filepath)
            
            if lines > 0:
                if language not in language_counts:
                    language_counts[language] = 0
                language_counts[language] += lines
                total_lines += lines

    print(f"{'Language':<20} {'Lines':<10} {'Percentage':<10}")
    print("-" * 40)
    
    sorted_languages = sorted(language_counts.items(), key=lambda item: item[1], reverse=True)
    
    for language, count in sorted_languages:
        percentage = (count / total_lines) * 100
        print(f"{language:<20} {count:<10} {percentage:.2f}%")
        
    print("-" * 40)
    print(f"{'Total':<20} {total_lines:<10} 100.00%")

if __name__ == "__main__":
    main()
