import os
import sys
import matplotlib.pyplot as plt
import extraction
import interpretation
from classifier import Classifier

DEFAULT_MANY_PATH = "./data/equations"


def test_many(directory):
    for file in os.listdir(directory):
        file = f'{directory}/{file}'
        test(file)


def test(file):
    data = extraction.extract(file)
    expressions, img_predictions = interpretation.get_expressions(data, classifier)

    plt.title(f'Znaleziono {len(expressions)} równań:\n' + '\n'.join(expressions))
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
