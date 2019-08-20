import tensorflow as tf

converter = tf.lite.TFLiteConverter.from_keras_model_file( 'models/model.h5' )
tflite_model = converter.convert()
open( 'tensorflow_lite_model/model.tflite' , 'wb' ).write( tflite_model )