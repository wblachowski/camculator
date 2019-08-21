import argparse

import tensorflow as tf

parser = argparse.ArgumentParser(description='Convert model from h5 to tflite format.')
parser.add_argument('-i', '--input', default='models/model.h5', help='path to h5 model')
parser.add_argument('-o', '--output', default='models/model.tflite', help='target path to tflite model')


def convert(input_path, output_path):
    converter = tf.lite.TFLiteConverter.from_keras_model_file(input_path)
    tflite_model = converter.convert()
    open(output_path, 'wb').write(tflite_model)


if __name__ == "__main__":
    args = parser.parse_args()
    convert(args.input, args.output)
