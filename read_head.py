
filename = r"d:\Android\app\src\main\java\com\example\android\ui\screens\HomeScreen.kt"
try:
    with open(filename, 'r', encoding='utf-8', errors='ignore') as f:
        lines = f.readlines()
        print(''.join(lines[:100]))
except Exception as e:
    print(e)
