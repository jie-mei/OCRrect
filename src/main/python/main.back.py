#!/bin/sh

import codecs
from sklc import data
from sklc import model
from sklearn import feature_selection
import sys


TEMP_DIR = 'tmp/'

MODEL_DIR = TEMP_DIR + 'model/'

# The pathname to the generated suggestion data.
DATA_PATH = TEMP_DIR + 'suggestion.top.3'

# The pathname to the ground truth error list.
GT_PATH = 'data/error.gt.tsv'

# The splitting ratio of the training, validation and testing set.
TRAIN_SPLITS_RATIO = 0.8
TEST_SPLITS_RATIO  = 0.2

SUFFIX = 'top3'


# The default settings.
DEFAULT_CONFIG = lambda name: \
        (False,                          # override
         True,                           # weighted 
         MODEL_DIR + name + '.' + SUFFIX, # seralization path
         None,                           # customized grid
         None)                           # selector
DEFAULT_SK_CONFIG = lambda name, sk_model_class: \
        (False,
         True,
         MODEL_DIR + name + '.' + SUFFIX,
         {'estimator__' + k: v for k, v in sk_model_class.DEFAULT_PARAM_GRID.items()},
         feature_selection.RFECV(sk_model_class.ESTIMATOR, step=1, cv=10))


TRAIN_SETTINGS = [
        (model.SKLModel              , DEFAULT_SK_CONFIG('rf.refcv', model.RandomForestModel)),
        (model.SKLModel              , DEFAULT_SK_CONFIG('et.refcv', model.ExtraTreesModel)),
        (model.SKLModel              , DEFAULT_SK_CONFIG('ab.refcv', model.AdaBoostModel)),
        (model.SKLModel              , DEFAULT_SK_CONFIG('gb.refcv', model.GradientBoostingModel)),
        (model.SKLModel              , DEFAULT_SK_CONFIG('mlp.refcv', model.MLPModel)),
        (model.RandomForestModel     , DEFAULT_CONFIG('rf')),
        (model.ExtraTreesModel       , DEFAULT_CONFIG('et')),
        (model.AdaBoostModel         , DEFAULT_CONFIG('ab')),
        (model.GradientBoostingModel , DEFAULT_CONFIG('gb')),
        (model.MLPModel              , DEFAULT_CONFIG('mlp')),
        (model.RandomForestModel     , (False, False, MODEL_DIR + 'rf.unweight.' + SUFFIX, None, None)),
        (model.SupportVectorModel    , DEFAULT_CONFIG('svr')),
        (model.SKLModel              , DEFAULT_SK_CONFIG('svr.refcv', model.SupportVectorModel))
       ]


def data_split(data_path, gt_path, train_ratio, test_ratio):
    """ Split data by the position of the original text at the given ratio.
    """
    if train_ratio + test_ratio != 1.0:
        raise ValueError

    # Get the split positions according to the ratios.
    poss = [int(l.split('\t')[0]) for l in codecs.open(gt_path, 'r', 'utf-8')]
    spos = poss[int(len(poss) * train_ratio)]

    # Splits data according to the ratios.
    dataset = data.Dataset.read(DATA_PATH)
    return dataset.subset(filt=lambda e: e.position < spos,
            return_complement=True), dataset


def main():
    (train_data, test_data), total_data = data_split(DATA_PATH, GT_PATH,
            TRAIN_SPLITS_RATIO, TEST_SPLITS_RATIO)


    def data_eval(data):
        denorm = len(data.errors)
        norm = sum([1 for err in data.errors if sum(err.labels) > 0])
        print(float(norm) / denorm)
    data_eval(train_data)
    data_eval(test_data)
    data_eval(total_data)

    #for md, (_override, _weighted, _path, _grid, _estimator) in TRAIN_SETTINGS:
    #    try:
    #        if md == model.SKLModel:
    #            md(_estimator, _grid, train_data, override=_override,
    #                    weighted=_weighted, pkl_path=_path)
    #        elif _grid != None:
    #            md(train_data, override=_override, weighted=_weighted,
    #                    pkl_path=_path, para_grid=_grid)
    #        else:
    #            md(train_data, override=_override, weighted=_weighted,
    #                    pkl_path=_path)
    #    except Exception as e:
    #        #print('Error')
    #        #print(str(e))
    #        #print()
    #        #sys.exit(1)
    #        pass # skip error model

    """
    for md, (_override, _weighted, _path, _grid, _estimator) in \
            TRAIN_SETTINGS[5:9] + [TRAIN_SETTINGS[10]]:
        print(md)
        
        try:
            lm = md(_estimator, _grid, train_data, pkl_path=_path) if md == model.SKLModel else md(train_data, pkl_path=_path)
            lm.predict(test_data)
            print('1: {}\n3: {}\n5: {}\n10: {}\nA: {}\n'
                .format(test_data.precision_at(1),
                      test_data.precision_at(1),
                      test_data.precision_at(3),
                      test_data.precision_at(5),
                      test_data.precision_at(10),
                      test_data.precision_at()))
        except Exception as e:
            raise e
            pass
    """


if __name__ == '__main__':
    main()
