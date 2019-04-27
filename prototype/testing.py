import os
import argparse
import matplotlib.pyplot as plt
import extraction
import interpretation
from classifier import Classifier

DEFAULT_MANY_PATH = './data/equations'
DEFAULT_MODEL_MATH = 'Sieci/with_slash1.h5'

parser = argparse.ArgumentParser(description='Test equations recognition')
parser.add_argument('-s', '--source', nargs='?', default=DEFAULT_MANY_PATH,
                    help=f'Source for recognition. Can be either a dictionary or a single file. The default value is \'{DEFAULT_MANY_PATH}\'.')
parser.add_argument('-m', '--model', nargs='?', default=DEFAULT_MODEL_MATH,
                    help=f'Path for .h5 file with neural net model. The default value is \'{DEFAULT_MODEL_MATH}\'.')
parser.add_argument('-d', '--display', action='store_true',
                    help='Flag saying whether output image should be shown. False by default.')


def test_many(directory, display):
    for file in os.listdir(directory):
        file = f'{directory}/{file}'
        test(file, display)


def test(file, display):
    data = extraction.extract(file)
    expressions, img_predictions = interpretation.get_expressions(data, classifier, createImage=display)
    print("=======")
    print(*expressions)
    print("=======")
    if display:
        plt.title(f'Found {len(expressions)} equation(s) in {file}:\n' + '\n'.join(expressions))
        plt.imshow(img_predictions, cmap='gray')
        plt.show()


if __name__ == "__main__":
    args = parser.parse_args()
    classifier = Classifier(args.model)
    if os.path.isdir(args.source):
        test_many(args.source, args.display)
    elif os.path.isfile(args.source):
        test(args.source, args.display)
    else:
        print("Wrong source!")
