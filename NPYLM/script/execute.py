# -*- coding: utf-8 -*-

import sys
sys.path.append("../src")
from npylm import NPYLM_trainer, NPYLM_decoder

def main():
    n = int(sys.argv[1])
    iter_n = int(sys.argv[2])

    train_path = sys.argv[3]
    test_path  = sys.argv[4]

    sys.stderr.write("reading data...\n")
    train_data = read_data(train_path)
    test_data  = read_data(test_path)
    sys.stderr.write("reading end!\n\n")

    sys.stderr.write("training...\n")
    model = train(train_data, n, iter_n)
    sys.stderr.write("training end\n\n")

    sys.stderr.write("testing...\n")
    test(test_data, model)
    sys.stderr.write("testing end\n")


def train(train_data, n, iter_n):
    lm = NPYLM_trainer()
    model = lm.train(train_data, n, iter_n)

    return model


def test(test_data, model):
    decoder = NPYLM_decoder(model)
    for sentence in test_data:
        prob = decoder.decode(sentence)
        print prob


def read_data(f_path):
    f_obj = open(f_path, "r")
    sentences = []
    for raw_line in f_obj:
        line = raw_line.rstrip("\n\r").decode("utf-8")
        sentences.append(line.split(u" "))
    f_obj.close()

    return sentences

if __name__ == "__main__":
    main()
