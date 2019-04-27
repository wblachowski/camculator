import os
import argparse
from sympy.abc import x, y, w, z
from sympy.parsing.sympy_parser import parse_expr
from sympy import solve, Eq
import matplotlib.pyplot as plt
import extraction
import interpretation
from classifier import Classifier

DEFAULT_MANY_PATH = './data/equations'
DEFAULT_MODEL_MATH = 'models/model.h5'

parser = argparse.ArgumentParser(description='Test equations recognition')
parser.add_argument('-s', '--source', nargs='?', default=DEFAULT_MANY_PATH,
                    help=f'Source for recognition. Can be either a dictionary or a single file. The default value is \'{DEFAULT_MANY_PATH}\'.')
parser.add_argument('-m', '--model', nargs='?', default=DEFAULT_MODEL_MATH,
                    help=f'Path for .h5 file with neural net model. The default value is \'{DEFAULT_MODEL_MATH}\'.')
parser.add_argument('-d', '--display', action='store_true',
                    help='Flag saying whether output image should be shown. False by default.')


def test_many(directory, display):
    correct_map = get_correct_map(directory)
    correct = 0
    count = 0
    for file in os.listdir(directory):
        if file.endswith('.txt'):
            continue
        file_fullpath = f'{directory}/{file}'
        expressions = test(file_fullpath, display)
        count += 1
        if expressions == correct_map[file]:
            correct += 1
        else:
            print(f'Recognized {" ".join(expressions)}')
            print(f'Expected {" ".join(correct_map[file])}')
    print(f'Correctly recognized: {100*correct / count}%')


def test(file, display):
    data = extraction.extract(file)
    expressions, img_predictions = interpretation.get_expressions(data, classifier, createImage=display)

    print("=======")
    print(*expressions)
    try:
        sympy_equations = []
        for expression in expressions:
            parts = expression.split('=')
            sympy_equations.append(Eq(parse_expr(parts[0]), parse_expr(parts[1])))
        result = solve(sympy_equations, x, y, w, z)
    except:
        result = 'Unknown'
    print("Result:", result)

    if display:
        plt.title(
            f'Found {len(expressions)} equation(s) in {file}:\n' + '\n'.join(expressions) + f'\n\nResult:{result}')
        plt.imshow(img_predictions, cmap='gray')
        plt.show()
    return expressions


def get_correct_map(directory):
    map = {}
    correct_file = f'{directory}/correct.txt'
    lines = [line.rstrip('\n') for line in open(correct_file)]
    for line in lines:
        parts = line.split(' ')
        map[parts[0]] = parts[1].split(',')
    return map


if __name__ == "__main__":
    args = parser.parse_args()
    classifier = Classifier(args.model)
    if os.path.isdir(args.source):
        test_many(args.source, args.display)
    elif os.path.isfile(args.source):
        test(args.source, args.display)
    else:
        print("Wrong source!")
