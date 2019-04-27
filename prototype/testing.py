import os
import sys
import matplotlib.pyplot as plt
import numpy as np
import cv2
import extraction
from classifier import Classifier

DEFAULT_MANY_PATH = "./data/equations"
SPECIAL_SYMBOLS_MAP = {"plus": "+", "minus": "-", "dot": "*", "slash": "/"}


def test_many(directory):
    for file in os.listdir(directory):
        file = f'{directory}/{file}'
        test(file)


def test(file):
    data = extraction.extract(file)

    boxes_mask = np.zeros(data.img_org.shape, np.uint8)
    for box, symbol in data.symbols:
        x, y, w, h = box
        cv2.rectangle(boxes_mask, (x, y), (x + w, y + h), (255, 255, 255), cv2.FILLED)
    boxes_mask = cv2.cvtColor(boxes_mask, cv2.COLOR_BGR2GRAY)
    # plt.imshow(boxes_mask, cmap='gray')
    # plt.show()

    symbols_copy = data.symbols.copy()
    equations = []
    in_equation = False
    for y in range(0, data.img_org.shape[0]):
        any_box = False
        for x in range(0, data.img_org.shape[1]):
            any_box = any_box or (boxes_mask[y][x] == 255)
        if any_box and not in_equation:
            in_equation = True
        if in_equation and not any_box:
            in_equation = False
            equation_boxes = []
            for box, symbol in symbols_copy.copy():
                xb, yb, wb, hb = box
                if yb + hb <= y:
                    equation_boxes.append((box, symbol))
            for bs in equation_boxes:
                symbols_copy.remove(bs)
            equations.append(equation_boxes)

    # for equation in equations:
    #     boxes_mask = np.zeros(data.img_org.shape, np.uint8)
    #     for box, symbol in equation:
    #         x, y, w, h = box
    #         cv2.rectangle(boxes_mask, (x, y), (x + w, y + h), (255, 255, 255), cv2.FILLED)
    #     plt.imshow(boxes_mask, cmap='gray')
    #     plt.show()

    plt.subplot(2, 1, 1)
    plt.imshow(data.img_binary, cmap='gray')

    plot_title = ''
    img_predictions = data.img_org.copy()
    for equation in equations:
        predictions = []
        for box, symbol in equation:
            prediction = classifier.predict_raw_img(symbol)
            predictions.append(prediction)
            x, y, w, h = box
            cv2.rectangle(img_predictions, (x, y), (x + w, y + h), (0, 255, 0), 2)
            cv2.putText(img_predictions, prediction, (x, y - 5), cv2.FONT_HERSHEY_SIMPLEX, 0.75, (0, 255, 0), 2)

        expression = ''
        i = 0
        while i < len(predictions):
            if predictions[i] == 'minus' and i + 1 < len(predictions) and predictions[i + 1] == 'minus':
                expression += "="
                i += 1
            else:
                expression += SPECIAL_SYMBOLS_MAP.get(predictions[i], predictions[i])
            i += 1
        plot_title += '\n' + expression

    plt.subplot(2, 1, 2)
    plt.title(f'Znaleziono {len(equations)} równań:\n' + plot_title)
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
