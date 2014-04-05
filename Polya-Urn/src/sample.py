# -*- coding: utf-8 -*-

from dirichlet import Dirichlet

def main():
    di = Dirichlet(4, [5, 30, 10, 3])

    ss = []
    for i in xrange(3000):
        ss.append(di.sample())
    sss = zip(*ss)

    mean = [sum(seq) / len(seq) for seq in sss]
    print mean



if __name__ == "__main__":
    main()
