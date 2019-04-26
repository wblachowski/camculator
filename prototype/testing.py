import os
import sys
import matplotlib.pyplot as plt
import cv2
import extraction
from classifier import Classifier

DEFAULT_MANY_PATH = "./equations"
SPECIAL_SYMBOLS_MAP = {"plus": "+", "minus": "-", "dot": "*", "slash": "/"}


def test_many(directory):
    for file in os.listdir(directory):
        file = f'{directory}/{file}'
        test(file)


def test(file):
    data = extraction.extract(file)

    plt.subplot(2, 1, 1)
    plt.imshow(data.img_binary, cmap='gray')

    predictions = []
    img_predictions = data.img_org.copy()
    for box, symbol in data.symbols:
        prediction = classifier.predict_raw_img(symbol)
        predictions.append(prediction)
        x, y, w, h = box
        cv2.rectangle(img_predictions, (x, y), (x + w, y + h), (0, 255, 0), 2)
        cv2.putText(img_predictions, prediction, (x, y - 5), cv2.FONT_HERSHEY_SIMPLEX, 0.75, (0, 255, 0), 2)

    equation = ''
    i = 0
    while i < len(predictions):
        if predictions[i] == 'minus' and i + 1 < len(predictions) and predictions[i + 1] == 'minus':
            equation += "="
            i += 1
        else:
            equation += SPECIAL_SYMBOLS_MAP.get(predictions[i], predictions[i])
        i += 1

    plt.subplot(2, 1, 2)
    plt.title(equation)
    plt.imshow(img_predictions, cmap='gray')
    plt.show()


if __name__ == "__main__":
    classifier = Classifier('Sieci\\with_slash1.h5')
    if len(sys.argv) == 2:
        if os.path.isdir(sys.argv[1]):
            test_many(sys.argv[1])
        elif os.path.isfile(sys.argv[1]):
            test(sys.argv[1])
        else:
            print("Wrong argument! Pass file or directory")
    else:
        test_many(DEFAULT_MANY_PATH)
