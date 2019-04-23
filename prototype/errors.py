import keras
from keras.datasets import mnist
from keras.models import Sequential
from keras.layers import Dense, Dropout, Flatten, Activation
from keras.layers import Conv2D, MaxPooling2D
from keras import backend as K
from keras.layers.normalization import BatchNormalization

from main import files
import numpy as np

from main import convert

import cv2

labels_dict = {'0': 0, '1':1, '2':2,'3':3,'4':4,'5':5,'6':6,'7':7,'8':8,'9':9,'dot':10,'minus':11,'plus':12,'w':13,'x':14,'y':15,'z':16}
class_labels = {0:'0',1:'1',2:'2',3:'3',4:'4',5:'5',6:'6',7:'7',8:'8',9:'9',10:'dot',11:'minus',12:'plus',13:'w',14:'x',15:'y',16:'z'}


def convert2(filename):
    image = cv2.imread(filename)
    image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    _, image = cv2.threshold(image, 127, 255, cv2.THRESH_BINARY)
    image = np.reshape(image, (28, 28, -1))
    image = np.reshape(image, (1, 28, 28, 1))
    return image



if __name__ == '__main__':
    model = keras.models.load_model('swilk1.h5')
    files_names = files('final')
    error = {}
    for filename in files_names:
        image = convert2('final'+'\\'+filename)
        corr_label = labels_dict[filename[0:filename.index('-')]]
        y_prob = model.predict(image)
        y_class = y_prob.argmax(axis=-1)
        if(corr_label!=y_class[0]):
            error[filename] = [y_class[0],corr_label]
    file = open("errors.txt",'w')
    for k,v in error.items():
        value = class_labels[int(v[0])]
        should = class_labels[int(v[1])]
        file.write('%s : found: %s should: %s \n' %(k,value,should))