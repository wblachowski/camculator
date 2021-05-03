import argparse

import tensorflow as tf

parser = argparse.ArgumentParser(description='Convert model from h5 to tflite format.')
parser.add_argument('-i', '--input', default='models/model.h5', help='Path to h5 model')
parser.add_argument('-o', '--output', default='models/model.tflite', help='Target path to tflite model')


def convert(input_path, output_path):
    converter = tf.lite.TFLiteConverter.from_keras_model_file(input_path)
    tflite_model = converter.convert()
    file = open(output_path, 'wb')
    file.write(tflite_model)
    file.close()


if __name__ == "__main__":
    args = parser.parse_args()
    convert(args.input, args.output)
