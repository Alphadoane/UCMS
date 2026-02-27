import zipfile
import sys
import xml.etree.ElementTree as ET

def extract_text(docx_path):
    try:
        with zipfile.ZipFile(docx_path) as docx:
            xml_content = docx.read('word/document.xml')
            root = ET.fromstring(xml_content)
            
            # XML namespaces are annoying, let's just inspect tags
            paragraphs = []
            
            # Recursive function or just finding all 'p' tags? 
            # Namespace string is usually long.
            # Namespaces are usually:
            # w = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            
            # Let's find all tags that end with 'p' (paragraph)
            # Actually, we can just iterate.
            
            ns = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
            
            # findall with namespace
            # If this fails due to different xmlns, we can fallback to manual walk
            
            for p in root.findall('.//w:p', ns):
                texts = []
                for t in p.findall('.//w:t', ns):
                    if t.text:
                        texts.append(t.text)
                paragraphs.append("".join(texts))
            
            return "\n".join(paragraphs)
            
    except Exception as e:
        return f"Error reading {docx_path}: {e}"

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python extract_text.py <docx_file> <output_file>")
        sys.exit(1)
    
    file_path = sys.argv[1]
    output_path = sys.argv[2]
    content = extract_text(file_path)
    
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(content)

