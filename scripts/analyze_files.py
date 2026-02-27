import sys
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
    # Read filenames from stdin
    files = [line.strip() for line in sys.stdin if line.strip()]
    
    language_counts = {}
    total_lines = 0
    file_counts = {}

    for filepath in files:
        if not os.path.isfile(filepath):
            continue
            
        ext = os.path.splitext(filepath)[1].lower()
        if not ext:
            continue
        
        language = get_language(ext)
        lines = count_lines_in_file(filepath)
        
        if lines > 0:
            if language not in language_counts:
                language_counts[language] = 0
                file_counts[language] = 0
            language_counts[language] += lines
            file_counts[language] += 1
            total_lines += lines

    with open('language_stats_utf8.txt', 'w', encoding='utf-8') as f:
        f.write(f"{'Language':<20} {'Files':<8} {'Lines':<10} {'Percentage':<10}\n")
        f.write("-" * 50 + "\n")
        
        sorted_languages = sorted(language_counts.items(), key=lambda item: item[1], reverse=True)
        
        for language, count in sorted_languages:
            percentage = (count / total_lines) * 100
            f_count = file_counts[language]
            f.write(f"{language:<20} {f_count:<8} {count:<10} {percentage:.2f}%\n")
            
        f.write("-" * 50 + "\n")
        f.write(f"{'Total':<20} {sum(file_counts.values()):<8} {total_lines:<10} 100.00%\n")
    
    # Also print to stdout for good measure
    print("Analysis complete. Results saved to language_stats_utf8.txt")

if __name__ == "__main__":
    main()
