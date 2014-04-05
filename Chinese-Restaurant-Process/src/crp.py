# -*- coding: utf-8 -*-

import sys
from random import uniform


def main(alpha, customer_n):
    partition = crp_sample(alpha, customer_n)
    dump_partition(partition)


def crp_sample(alpha, customer_n):
    tables    = [1]
    partition = [[0]]

    for i in xrange(1, customer_n):
        table_index = _sample(tables, alpha)
        if table_index == -1:
            tables.append(1)
            partition.append([i])
        else:
            tables[table_index] += 1
            partition[table_index].append(i)

    return partition


def _sample(tables, alpha):
    nmlz = sum(tables) + alpha

    total = 0.0
    rdm_sample = uniform(0, nmlz)
    for i, num in enumerate(tables):
        total += num
        if rdm_sample < total:
            return i

    return -1

def dump_partition(partition):
    for table in partition:
        line = " ".join(str(i) for i in table)
        print line


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print "usege:  python crp.py alpha c_num"
        sys.exit()

    alpha      = float(sys.argv[1])
    customer_n = int(sys.argv[2])

    main(alpha, customer_n)
