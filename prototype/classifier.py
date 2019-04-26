import keras
import cv2
import numpy as np


class Classifier:
    class_labels = {0: '0', 1: '1', 2: '2', 3: '3', 4: '4', 5: '5', 6: '6', 7: '7', 8: '8', 9: '9', 10: 'dot',
                    11: 'minus', 12: 'plus', 13: 'w', 14: 'x', 15: 'y', 16: 'z', 17: 'slash'}

    def __init__(self, model_file):
        self.model = keras.models.load_model(model_file)

    def predict_raw_img(self, symbol):
        symbol = self.convert2(symbol)
        return self.predict(symbol)

    def predict(self, symbol):
        y_prob = self.model.predict(symbol)
        y_class = y_prob.argmax(axis=-1)
        return self.class_labels[y_class[0]]

    def convert2(self, image):
        _, image = cv2.threshold(image.astype(np.uint8), 127, 255, cv2.THRESH_BINARY)
        image = np.reshape(image, (28, 28, -1))
        image = np.reshape(image, (1, 28, 28, 1))
        return image
