
filename = r"d:\Android\app\src\main\java\com\example\android\ui\screens\HomeScreen.kt"
try:
    with open(filename, 'r', encoding='latin-1') as f:
        print(f.read())
except Exception as e:
    print(e)
