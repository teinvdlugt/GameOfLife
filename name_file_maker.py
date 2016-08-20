import os

directory = "C:\Users\Tein\Downloads\golpatterns\\"
filename_list = []
pattern_name_list = []
all_files = os.listdir(directory)

for filename in all_files:
    if not filename.endswith(".rle"):
        continue
    f = open(directory + filename)
    lines = f.readlines()
    f.close()
    for line in lines:
        if line.startswith('#N') or line.startswith('#n'):
            name = line[2:].strip().replace(",", "")
            break
    filename_list.append(filename)
    pattern_name_list.append(name)

print "Index done, saving names_file"

names_file = open("pattern_names.csv", 'w')
for i in range(len(filename_list)):
    names_file.write(filename_list[i] + "," + pattern_name_list[i] + "\n")
names_file.close()
