import cv2
import numpy as np

SPECIAL_SYMBOLS_MAP = {"plus": "+", "minus": "-", "dot": "*", "slash": "/"}


def get_expressions(data, classifier, createImage=False):
    boxes_mask = get_boxes_mask(data.img_org.shape, data.symbols)
    equations = get_equations(data.img_org.shape, data.symbols, boxes_mask)

    expressions = []
    img_predictions = data.img_org.copy() if createImage else np.empty(data.img_org.shape)
    for equation in equations:
        predictions = []
        boxes = []
        for box, symbol in equation:
            prediction = classifier.predict_raw_img(symbol)
            predictions.append(prediction)
            boxes.append(box)
            x, y, w, h = box
            if createImage:
                cv2.rectangle(img_predictions, (x, y), (x + w, y + h), (0, 255, 0), 2)
                cv2.putText(img_predictions, prediction, (x, y - 5), cv2.FONT_HERSHEY_SIMPLEX, 0.75, (0, 255, 0), 2)

        expression = ''
        i = 0
        while i < len(predictions):
            if predictions[i] == 'minus' and i + 1 < len(predictions) and predictions[i + 1] == 'minus' and is_equals(
                    boxes[i], boxes[i + 1]):
                expression += "="
                i += 1
            elif i > 0 and is_power(boxes[i - 1], boxes[i]):
                expression += '**' + SPECIAL_SYMBOLS_MAP.get(predictions[i], predictions[i])
            else:
                expression += SPECIAL_SYMBOLS_MAP.get(predictions[i], predictions[i])
            i += 1
        expressions.append(expression)
    return expressions, img_predictions


def get_boxes_mask(shape, symbols):
    boxes_mask = np.zeros((shape[0], shape[1]), np.uint8)
    for box, symbol in symbols:
        x, y, w, h = box
        boxes_mask[y:y + h, x:x + w] = 255
    return boxes_mask


def get_equations(shape, symbols, boxes_mask):
    symbols_copy = symbols.copy()
    equations = []
    in_equation = False
    for y in range(0, shape[0]):
        any_box = False
        for x in range(0, shape[1]):
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
    return equations


def is_equals(box1, box2):
    x1, y1, w1, h1 = box1
    x2, y2, w2, h2 = box2
    return abs(x1 - x2) < max(w1, w2)


def is_power(box_base, box_power):
    xbase, ybase, wbase, hbase = box_base
    xpower, ypower, wpower, hpower = box_power
    return ypower < ybase and ypower + hpower < ybase + 0.5 * hbase and xpower > xbase + 0.5 * wbase
