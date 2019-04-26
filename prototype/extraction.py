import cv2
import math
import matplotlib.pyplot as plt
import numpy as np


class data:
    def __init__(self, img_org, img_binary, img_boxes, boxes, symbols):
        self.img_org = img_org
        self.img_binary = img_binary
        self.img_boxes = img_boxes
        self.boxes = boxes
        self.symbols = symbols


def extract(filename, display=False):
    img_org = cv2.imread(filename)
    img_boxes, img_binary, boxes = process_img(img_org, display)
    symbols = extract_symbols(img_binary, boxes)
    return data(img_org, img_binary, img_boxes, boxes, symbols)


def extract_symbols(img, boxes, size=28):
    symbols = []
    for box in boxes:
        x, y, w, h = box
        symbol = img[y:y + h, x:x + w]
        if h >= w:
            newx = size * w // h
            if newx == 0:
                break
            symbol = cv2.resize(symbol, (newx, size))
            rest = size - newx
            rest_left = int(math.ceil(rest / 2))
            rest_right = int(math.floor(rest / 2))
            symbol = np.hstack((np.full((size, rest_left), 255), symbol, np.full((size, rest_right), 255)))
        else:
            newy = size * h // w
            if newy == 0:
                break
            symbol = cv2.resize(symbol, (size, newy))
            rest = size - newy
            rest_up = int(math.ceil(rest / 2))
            rest_down = int(math.floor(rest / 2))
            symbol = np.vstack((np.full((rest_up, size), 255), symbol, np.full((rest_down, size), 255)))
        symbols.append((box, symbol))
    return symbols


def process_img(org_img, display=False):
    img = org_img.copy()
    img_height = img.shape[0]
    img_width = img.shape[1]

    if display:
        display_plot(img, 'Original image', None)

    # Convert to grayscale and denoise
    img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    img = cv2.fastNlMeansDenoising(img, h=13)
    if display:
        display_plot(img, 'Grayscale and denoised', 'gray')

    # Binarize image
    block_size = max(img_height, img_width) // 7
    block_size = block_size + 1 if block_size % 2 == 0 else block_size
    img = cv2.adaptiveThreshold(img, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, block_size, 5)
    if display:
        display_plot(img, 'With binary threshold applied', 'gray')

    # Morphological opening
    kernel_size = max(img_height, img_width) // 100
    img = cv2.morphologyEx(img, cv2.MORPH_OPEN,
                           cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (kernel_size, kernel_size)))
    if display:
        display_plot(img, 'With Morphological opening', 'gray')
    img_binary = img.copy()

    # Find contours
    contours, hierarchy = cv2.findContours(img, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    if display:
        img_contours = cv2.drawContours(img, contours, -1, (0, 255, 0), 3)
        display_plot(img_contours, 'Contours', 'gray')

    # Add limit to bounding boxes size
    img_size = img_height * img_width
    max_area = 0.5 * img_size

    # Find bounding boxes
    boxes = []
    for contour in contours:
        x, y, w, h = cv2.boundingRect(contour)
        if max_area > (w * h):
            boxes.append((x, y, w, h))

    # Remove boxes wholly contained in other boxes
    for box1 in boxes.copy():
        for box2 in boxes.copy():
            if contains(box2, box1):
                if box1 in boxes:
                    boxes.remove(box1)

    # sort boxes horizontally
    boxes = sorted(boxes, key=lambda b: b[0])

    img_boxes = org_img.copy()
    for box in boxes:
        x, y, w, h = box
        cv2.rectangle(img_boxes, (x, y), (x + w, y + h), (0, 255, 0), 2)
    if display:
        display_plot(img_boxes, 'Bounding boxes', 'gray')

    return img_boxes, img_binary, boxes


def display_plot(image, title='', cmap=None):
    plt.figure(figsize=(15, 7))
    plt.imshow(image, cmap=cmap)
    plt.title(title)
    plt.show()


def contains(box1, box2):
    b1x, b1y, b1w, b1h = box1
    b2x, b2y, b2w, b2h = box2
    return b1x < b2x and b1y < b2y and (b1x + b1w) > (b2x + b2w) and (b1y + b1h) > (b2y + b2h)
