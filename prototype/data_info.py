

from os import listdir
from os.path import isfile, join


def files(mypath):
    onlyfiles = [f for f in listdir(mypath) if isfile(join(mypath, f))]
    return onlyfiles

labels_dict = {'0': 0, '1':1, '2':2,'3':3,'4':4,'5':5,'6':6,'7':7,'8':8,'9':9,'dot':10,'minus':11,'plus':12,'w':13,'x':14,'y':15,'z':16,'slash':17}




def labels_file_names(files):
    labels = []
    for f in files:
        id = f.index('-')
        label = labels_dict[f[0:id]]
        labels.append(label)
    return labels





labels = files('swilk-data')
labels = labels_file_names(labels)
d = {}
for l in labels:
    if l not in d:
        d[l]=0
    d[l] += 1
new = {}
for k,v in labels_dict.items():
    new[v] = k

file = open("data_info.txt",'w')


for k,v in d.items():
    file.write('%s  %s \n' % (new[k],v))