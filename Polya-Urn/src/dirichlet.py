# -*- coding: utf-8 -*-

from numpy.random import multinomial


class Dirichlet:

    def __init__(self, dim, alphas):
        self.dim = dim
        self.alphas = alphas

    def sample(self, iter_n = 4000):
        prob_mass = self.alphas[:]
        for i in range(iter_n):
            nmlz = 1 / sum(prob_mass)
            prob = [mass * nmlz for mass in prob_mass]
            ball_color = multinomial(1, prob).tolist().index(1)
            prob_mass[ball_color] += 1.0

        nmlz = 1 / sum(prob_mass)
        return [mass * nmlz for mass in prob_mass]

