from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import MaxPooling2D,Dense,Dropout,Flatten,Conv2D,BatchNormalization,Activation
from main import files, split_data, labels_file_names,labels_dict,convert
from tensorflow.keras.losses import categorical_crossentropy

from tensorflow.keras.optimizers import Adam
from tensorflow.keras.utils import to_categorical
from  tensorflow.train import Saver


import cv2
import matplotlib.pyplot as plt
import numpy as np
batch_size = 128
num_classes = 18
epochs = 30


def create_model():
    model = Sequential()
    model.add(Conv2D(32, (3, 3), input_shape=(28, 28, 1)))
    model.add(Activation('relu'))
    BatchNormalization(axis=-1)
    model.add(Conv2D(32, (3, 3)))
    model.add(Activation('relu'))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    BatchNormalization(axis=-1)
    model.add(Conv2D(64, (3, 3)))
    model.add(Activation('relu'))
    BatchNormalization(axis=-1)
    model.add(Conv2D(64, (3, 3)))
    model.add(Activation('relu'))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    model.add(Flatten())
    # Fully connected layer
    BatchNormalization()
    model.add(Dense(512))
    model.add(Activation('relu'))
    BatchNormalization()
    model.add(Dropout(0.2))
    model.add(Dense(18))
    model.add(Activation('softmax'))
    model.compile(loss=categorical_crossentropy,
                  optimizer=Adam(),
                  metrics=['accuracy'])
    return model


if __name__ == '__main__':
    model = create_model()
    path = 'data/symbols'
    files_names = files(path)
    labels = labels_file_names(files_names)
    labels = to_categorical(labels, num_classes)

    dataset = [convert(file,path) for file in files_names]
    dataset = np.array(dataset)
    dataset = dataset.astype('float32')
    dataset /= 255
    x_train, x_test, y_train, y_test = split_data(dataset,labels,0.2)

    model.fit(x_train, y_train,
              batch_size=batch_size,
              epochs=epochs,
              verbose=2,
              validation_data=(x_test, y_test))
    score = model.evaluate(x_test, y_test, verbose=1)
    print('Test loss:', score[0])
    print('Test accuracy:', score[1])

    model.save('tensorflow_model\\test.pb')