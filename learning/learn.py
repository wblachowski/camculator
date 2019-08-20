from os import listdir
from os.path import isfile, join

import cv2
import numpy as np

batch_size = 128
num_classes = 18
epochs = 18
import keras
from keras.models import Sequential
from keras.layers import Dense, Dropout, Flatten
from keras.layers import Conv2D, MaxPooling2D
import sklearn.model_selection

def split_data(X,y,test_size):
    X_train, X_test, y_train, y_test = sklearn.model_selection.train_test_split(X, y, test_size=test_size)
    return X_train, X_test, y_train, y_test


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


def convert(filename,path):
    image = cv2.imread(path+'\\'+filename)
    image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    _,image = cv2.threshold(image,127,255,cv2.THRESH_BINARY)
    image = np.reshape(image,(28,28,-1))
    image = np.reshape(image,(28,28,1)).astype('float32')
    return image




def get_model():
    model = Sequential()
    model.add(Conv2D(32, kernel_size=(3, 3),
                     activation='relu',
                     input_shape=(28, 28,1)))
    model.add(Conv2D(64, (3, 3), activation='relu'))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    model.add(Dropout(0.25))
    model.add(Flatten())
    model.add(Dense(128, activation='relu'))
    model.add(Dropout(0.5))
    model.add(Dense(num_classes, activation='softmax'))

    model.compile(loss=keras.losses.categorical_crossentropy,
                  optimizer=keras.optimizers.Adadelta(),
                  metrics=['accuracy'])
    return model

if __name__ == '__main__':
    path = 'data/symbols'
    files_names = files(path)
    labels = labels_file_names(files_names)
    labels = keras.utils.to_categorical(labels, num_classes)

    dataset = [convert(file,path) for file in files_names]
    dataset = np.array(dataset)
    dataset = dataset.astype('float32')
    dataset /= 255
    x_train, x_test, y_train, y_test = split_data(dataset,labels,0.2)

    model = get_model()
    print(x_train.shape)
    model.fit(x_train, y_train,
              batch_size=batch_size,
              epochs=epochs,
              verbose=2,
              validation_data=(x_test, y_test))
    score = model.evaluate(x_test, y_test, verbose=1)

    print('Test loss:', score[0])
    print('Test accuracy:', score[1])
    model.save('models\\model.h5')