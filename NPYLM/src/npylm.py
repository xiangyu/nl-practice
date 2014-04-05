# -*- coding: utf-8 -*-

from collections import defaultdict
from copy import deepcopy
from random import randint
from numpy.random import multinomial
import math


class NPYLM_trainer(object):

    BOS = u"##<BOS>##"
    EOS = u"##<EOS>##"

    # "sentences" is a list of list of words, "n" is gram number
    # e.g. [["This", "is", "a", "pen", "."], ["Hello", "world", "!"]]
    def train(self, sentences, n, iter_n):
        ngram_count = self._count_token(sentences, n)
        restaurants = self._init_restaurants(ngram_count, n)

        self._gibbs_sampling(restaurants, iter_n)

        model = {
            "n": n,
            "d": [0.2, 0.2, 0.2],
            "theta": [0.1, 0.1, 0.1],
            "restaurants": restaurants,
            "base_distribution": Base_distribution()
        }

        return model
            

    def _count_token(self, sentences, n):
        ngram_count = defaultdict(lambda: 0)

        for sentence in sentences:
            _sentence = [self.BOS + str(i) for i in range(n - 2, -1, -1)] + sentence[:] + [self.EOS + str(i) for i in range(0, n - 1)]
            for i in xrange(0, len(_sentence) - (n - 1)):
                ngram_count[tuple(_sentence[i:i+n])] += 1

        return ngram_count

    def _init_restaurants(self, ngram_count, n):
        restaurants = defaultdict(lambda: Restaurant(0, 0))

        for ngram, count in ngram_count.items():
            word = ngram[n - 1]
            context = ngram[0:-1]
            restaurant = restaurants[context]
            restaurant.append_customers(count, word)

        return restaurants
            
    def _gibbs_sampling(self, restaurants, iter_num):
        parent_restaurants = defaultdict(lambda: Restaurant(0, 0))
        for i in xrange(iter_num):
            print i
            for context, restaurant in restaurants.items():
                c_list = restaurant.customers_list()
                for word, c_num in c_list.items():
                    for j in xrange(c_num):
                        #print "  ", j
                        self._remove_customer(context, restaurant, word, parent_restaurants)
                        self._add_customer(context, restaurant, word, parent_restaurants)


    def _remove_customer(self, base_context, base_restaurant, word, parent_restaurants):
        context = base_context
        restaurant = base_restaurant
        while True:
            flag = self.__remove_customer(context, restaurant, word)
            if flag:
                break
            context = context[1:]
            restaurant = parent_restaurants[context]

    def __remove_customer(self, context, restaurant, word):
        w_tables = restaurant.w_tables(word)
        if len(w_tables) == 0:
            return True

        rand_num = randint(0, len(w_tables) - 1)
        table_index = w_tables[rand_num]

        table = restaurant.tables[table_index]
        table["customer"] -= 1
        restaurant.total_c -= 1

        if table["customer"] != 0:
            return True
        elif table["customer"] == 0 and len(context) != 0:
            del restaurant.tables[table_index]
            restaurant.total_t -= 1
            return False
        else:  # table["customer"] == 0 and len(context) == 0
            del restaurant.tables[table_index]
            restaurant.total_t -= 1
            return True


    def _add_customer(self, base_context, base_restaurant, word, parent_restaurants):
        context = base_context
        restaurant = base_restaurant
        while True:
            flag = self.__add_customer(context, restaurant, word)
            if flag:
                break
            context = context[1:]
            restaurant = parent_restaurants[context]

    def __add_customer(self, context, restaurant, word):
        table_info = restaurant.w_tables_info(word)

        if len(table_info) != 0:
            tables_index, prob_mass = zip(*table_info.items())
        else:
            tables_index, prob_mass = ((),())
        prob_mass = [max(0, mass - 0.2) for mass in prob_mass] + [0.2 + 0.2 * restaurant.total_t]
        nmlz = 1 / sum(prob_mass)
        probs = [mass * nmlz for mass in prob_mass]

        random_index = multinomial(1, probs).tolist().index(1)
        if random_index >= len(tables_index):
            table_index = restaurant.table_next_index
            table = {"dish": word, "customer": 1}
            restaurant.tables[table_index] = table
            restaurant.table_next_index += 1
            restaurant.total_t += 1
            restaurant.total_c += 1
            return False
        else:
            table_index = tables_index[random_index]
            table = restaurant.tables[table_index]
            table["customer"] += 1
            restaurant.total_c += 1
            return True


    # transfer restaurant in parent_restaurants to restaurants
    def _transfer_restaurant(self, restaurants, parent_restaurants):
        for context, restaurant in parent_restaurants.items():
            restaurants[context] = restaurant


class NPYLM_decoder(object):

    BOS = u"##<BOS>##"
    EOS = u"##<EOS>##"

    def __init__(self, model):
        self.n = model["n"]
        self.d = model["d"]
        self.theta = model["theta"]
        self.restaurants = model["restaurants"]
        self.base_distribution = model["base_distribution"]


    def decode(self, sentence):
        _sentence = [self.BOS + str(i) for i in range(self.n - 2, -1, -1)] + sentence[:] + [self.EOS + str(i) for i in range(0, self.n - 1)]
        log_prob = 0.0

        for i in xrange(0, len(_sentence) - (self.n - 1)):
            log_prob += self.word_prob(_sentence[i:i+self.n])

        return log_prob

    def word_prob(self, ngram):
        word = ngram[self.n - 1]
        base_context = ngram[0:-1]
        log_probs = []

        log_coef = math.log(1.0)
        for i in range(0, self.n):
            context    = tuple(base_context[i:])
            restaurant = self.restaurants[context]

            denom_log = math.log(self.theta[i] + restaurant.total_c)
            tw = restaurant.tw_sum(word)
            if tw != 0:
                cw = restaurant.cw_sum(word)
                log_probs.append(log_coef + math.log(cw - self.d[i] * tw) - denom_log)
            log_coef = math.log(self.theta[i] + self.d[i] * restaurant.total_t) - denom_log

        log_probs.append(self.base_distribution[word])

        return Utils.log_sum_exp(log_probs)



class Restaurant(object):

    def __init__(self, total_c = 0, total_t = 0):
        self.total_c = 0
        self.total_t = 0
        self.tables = {}  # each table consists of number of custmers, served dish
        self.table_next_index = 0

    # used in initializeing restaurants
    def append_customers(self, c_num, word):
        for i, table in self.tables.items():
            if table["dish"] == word:
                table["customer"] += c_num
                self.total_c += c_num
                return
        table = {"dish": word, "customer": c_num}
        self.tables[self.table_next_index] = table
        self.table_next_index += 1
        self.total_t += 1
        self.total_c += c_num

    def customers_list(self):
        c_list = defaultdict(lambda:0)
        for table in self.tables.values():
            c_list[table["dish"]] += table["customer"]

        return c_list

    # return a list of table index which serves "word" as a dish.
    def _table_w(self, word):
        t_index = []
        for i, table in self.tables.items():
            if table["dish"] == word:
                t_index.append(i)

        return t_index

    # return a number of customer sitting a table which serves word
    def cw_sum(self, word):
        t_index = self._table_w(word)

        return sum(self.tables[i]["customer"] for i in t_index)


    # return a number of tables which serves word
    def tw_sum(self, word):
        t_index = self._table_w(word)

        return len(t_index)

    # return a table index list
    def w_tables(self, word):
        t_index = []
        for i, table in self.tables.items():
            if table["dish"] == word:
                t_index.extend( [i for _ in range(table["customer"])] )
        return t_index

    def w_tables_info(self, word):
        t_info = {}
        for i, table in self.tables.items():
            if table["dish"] == word:
                t_info[i] = table["customer"]
        return t_info


class Base_distribution(object):

    def __getitem__(self, word):
        return  1.0 - math.log(1000000)


class Utils:

    @classmethod
    def log_sum_exp(cls, log_probs):
        max_log_prob = max(log_probs)
        exp_sum = sum(math.exp(log_prob - max_log_prob) for log_prob in log_probs)

        return max_log_prob + math.log(exp_sum)

