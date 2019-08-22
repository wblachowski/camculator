import argparse
from os import listdir
from os.path import isfile, join

import cv2
import keras
import numpy as np
import sklearn.model_selection
from keras.layers import Conv2D, MaxPooling2D
from keras.layers import Dense, Dropout, Flatten
from keras.models import Sequential

LABELS_DICT = {'0': 0, '1': 1, '2': 2, '3': 3, '4': 4, '5': 5, '6': 6, '7': 7, '8': 8, '9': 9, 'dot': 10, 'minus': 11,
               'plus': 12, 'w': 13, 'x': 14, 'y': 15, 'z': 16, 'slash': 17}

parser = argparse.ArgumentParser(description='Train NN on 28x28 pictures')
parser.add_argument('-i', '--input', default='data/symbols', help='Path to data directory')
parser.add_argument('-o', '--output', default='models/model.h5', help='Target path to h5 model')
parser.add_argument('-b', '--batch', type=int, default=128, help='Batch size')
parser.add_argument('-e', '--epochs', type=int, default=18, help='Number of epochs')


def get_files(path):
    return [f for f in listdir(path) if isfile(join(path, f))]


def get_files_labels(files):
    return [LABELS_DICT[f[0:f.index('-')]] for f in files]


def convert(path, filename):
    image = cv2.imread(join(path, filename))
    image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    _, image = cv2.threshold(image, 127, 255, cv2.THRESH_BINARY)
    image = np.reshape(image, (28, 28, -1))
    image = np.reshape(image, (28, 28, 1)).astype('float32')
    return image


def get_model():
    num_classes = len(LABELS_DICT)
    model = Sequential()
    model.add(Conv2D(32, kernel_size=(3, 3),
                     activation='relu',
                     input_shape=(28, 28, 1)))
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
    args = parser.parse_args()

    files = get_files(args.input)
    labels = get_files_labels(files)
    labels = keras.utils.to_categorical(labels, len(LABELS_DICT))

    dataset = [convert(args.input, file) for file in files]
    dataset = np.array(dataset).astype('float32') / 255

    x_train, x_test, y_train, y_test = sklearn.model_selection.train_test_split(dataset, labels, test_size=0.2)

    model = get_model()
    model.fit(x_train, y_train,
              batch_size=args.batch,
              epochs=args.epochs,
              verbose=2,
              validation_data=(x_test, y_test))
    score = model.evaluate(x_test, y_test, verbose=1)

    print('Test loss:', score[0])
    print('Test accuracy:', score[1])
    model.save(args.output)
