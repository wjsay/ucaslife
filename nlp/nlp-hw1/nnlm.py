#encoding:utf-8
import sys, importlib
importlib.reload(sys)
# sys.setdefaultencoding('utf8')

from input_data import *

import numpy as np
import tensorflow as tf
import argparse
import time
import math
from tensorflow.python.platform import gfile

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--data_dir', type=str, default='data/',
                       help='data directory containing input.txt')
    parser.add_argument('--batch_size', type=int, default=120,
                       help='minibatch size')
    parser.add_argument('--win_size', type=int, default=5,
                       help='context sequence length')
    parser.add_argument('--hidden_num', type=int, default=64,
                       help='number of hidden layers')
    parser.add_argument('--word_dim', type=int, default=50,
                       help='number of word embedding')
    parser.add_argument('--num_epochs', type=int, default=10,
                       help='number of epochs')
    parser.add_argument('--grad_clip', type=float, default=10.,
                       help='clip gradients at this value')

    args = parser.parse_args() #参数集合

    #准备训练数据
    data_loader = TextLoader(args.data_dir, args.batch_size, args.win_size)
    args.vocab_size = data_loader.vocab_size
	
    #模型定义
    graph = tf.Graph()
    with graph.as_default():
        #定义训练数据
        input_data = tf.placeholder(tf.int32, [args.batch_size, args.win_size])
        targets = tf.placeholder(tf.int64, [args.batch_size, 1])
		
        #模型参数
        with tf.variable_scope('nnlm' + 'embedding'):
            embeddings = tf.Variable(tf.random_uniform([args.vocab_size, args.word_dim], -1.0, 1.0))
            embeddings = tf.nn.l2_normalize(embeddings, 1)

        with tf.variable_scope('nnlm' + 'weight'):
            weight_h = tf.Variable(tf.truncated_normal([args.win_size * args.word_dim + 1, args.hidden_num],
                            stddev=1.0 / math.sqrt(args.hidden_num)))
            softmax_w = tf.Variable(tf.truncated_normal([args.win_size * args.word_dim, args.vocab_size],
                            stddev=1.0 / math.sqrt(args.win_size * args.word_dim)))
            softmax_u = tf.Variable(tf.truncated_normal([args.hidden_num + 1, args.vocab_size],
                            stddev=1.0 / math.sqrt(args.hidden_num)))          


        #TODO，构造计算图
        def infer_output(input_data):
            # [4557,50], [120, 5] => [120, 5, 50]
            selected_embed = tf.nn.embedding_lookup(embeddings, input_data)
            # [120, 250]
            selected_embed = tf.reshape(selected_embed, [-1, args.win_size * args.word_dim])
            b = tf.stack([tf.shape(input_data)[0], 1])
            b = tf.ones(b) # [120, 1]
            # [120, 251]
            x = tf.concat([selected_embed, b],1)
            # step 1: hidden = tanh(x * H + d)
            # [120, 251]*[251, 64]=[120, 64]            
            hidden = tf.nn.tanh(tf.matmul(x, weight_h)) 
            # [120, 65]
            hidden = tf.concat([hidden, b], 1)
            # step 2: outputs = softmax(x * W + hidden * U + b)
            # [120, 250]*[250, 4577]+[120, 65]*[65, 4577]=[120, 4577]
            outputs = tf.matmul(selected_embed, softmax_w) + tf.matmul(hidden, softmax_u)
            outputs = tf.clip_by_value(outputs, 0.0, args.grad_clip)
            outputs = tf.nn.softmax(outputs)
            return outputs

        outputs = infer_output(input_data)
        one_hot_targets = tf.one_hot(tf.squeeze(targets), args.vocab_size, 1.0, 0.0)

        loss = -tf.reduce_mean((tf.log(outputs) * one_hot_targets, 1))
        optimizer = tf.train.AdagradOptimizer(0.1).minimize(loss)

        #输出词向量
        embeddings_norm = tf.sqrt(tf.reduce_sum(tf.square(embeddings), 1, keep_dims=True))
        normalized_embeddings = embeddings / embeddings_norm

    #模型训练
    with tf.Session(graph=graph) as sess:
        tf.global_variables_initializer().run()
        for e in range(args.num_epochs):
            data_loader.reset_batch_pointer()
            for b in range(data_loader.num_batches):
                start = time.time()
                x, y = data_loader.next_batch()
                feed = {input_data: x, targets: y}
                train_loss,  _ = sess.run([loss, optimizer], feed)
                end = time.time()
                print("{}/{} (epoch {}), train_loss = {:.3f}, time/batch = {:.3f}" .format(
                        b, data_loader.num_batches,
                        e, train_loss, end - start))
			
			# 保存词向量至nnlm_word_embeddings.npy文件
            np.save('nnlm_word_embeddings.en', normalized_embeddings.eval())
        

if __name__ == '__main__':
    main()
