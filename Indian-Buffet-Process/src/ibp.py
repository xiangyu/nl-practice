# -*- coding: utf-8 -*-

import sys
import numpy
from random import uniform


def main(alpha, customer_n, one=None, zero=None):
    binary_matrix = ibp_sample(alpha, customer_n)
    if one == None:
        print_matrix(binary_matrix)
    else:
        print_matrix_with_symbol(binary_matrix, one, zero)


def ibp_sample(alpha, customer_n):
    binary_matrix = []
    dish_history = []
    next_table_n = 0

    for customer in xrange(customer_n):
        binary_matrix.append([])
        for i in xrange(next_table_n):
            b_s = bernoulli(dish_history[i], customer + 1)
            if b_s == 1:
                binary_matrix[customer].append(1)
                dish_history[i] += 1
            else:
                binary_matrix[customer].append(0)
        s = poisson(alpha / (customer + 1.0))
        binary_matrix[-1].extend([1] * s)
        dish_history.extend([1] * s)
        next_table_n += s

    return binary_matrix


def bernoulli(nomer, denom):
    s = uniform(0.0, denom)
    if s < nomer:
        return 1
    else:
        return 0


def poisson(lam):
    s = numpy.random.poisson(lam, 1)

    return s[0]


def print_matrix(binary_matrix):
    for elem in binary_matrix:
        line = " ".join(str(feature) for feature in elem)
        print line

def print_matrix_with_symbol(binary_matrix, one, zero):
    for elem in binary_matrix:
        line = " ".join(one if feature == 1 else zero for feature in elem)
        print line



if __name__ == "__main__":
    if len(sys.argv) == 3:
        alpha      = float(sys.argv[1])
        customer_n = int(sys.argv[2])
        main(alpha, customer_n)
    elif len(sys.argv) == 5:
        alpha      = float(sys.argv[1])
        customer_n = int(sys.argv[2])
        one        = sys.argv[3]
        zero       = sys.argv[4]
        main(alpha, customer_n, one, zero)
    else:
        print "usage: python ibp.y alpha c_num [zero_symbol one_symbol]"
