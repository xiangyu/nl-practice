# -*- coding: utf-8 -*-

from npylm import NPYLM_trainer, NPYLM_decoder

def main():
    lm = NPYLM_trainer()

    pre_sentences = [
        u"There are more than one way to do it .",
        u"There are more than one way to do it .",
        u"There are only one way to do it .",
        u"Unix is zen ."
    ]
    sentences = [sent.split(u" ") for sent in pre_sentences]

    model = lm.train(sentences, 3, 800)

    decoder = NPYLM_decoder(model)
    prob = decoder.decode(sentences[0])
    print prob
    prob = decoder.decode(u"There is just two way to just do it ! !".split(" "))
    print prob


if __name__ == "__main__":
    main()
