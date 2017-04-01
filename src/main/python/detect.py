#!/bin/sh
#
# Detect error token. Error detection is a recall oriented task.
#
# vim Nread/Nwrite path:
#   scp://jmei@cgm6.research.cs.dal.ca:/raid6/user/jmei/Learn2correct/src/main/python/detect.py
#
# see: http://scikit-learn.org/stable/modules/svm.html#svm-classification

import codecs
from sklc import data
from sklc import model
from sklearn import feature_selection
import sys


TEMP_DIR = 'tmp/'

MODEL_DIR = TEMP_DIR + 'model/'

# The pathname to the generated suggestion data.
DATA_PATH = TEMP_DIR + 'suggestion.top.10.txt'

# The pathname to the ground truth error list.
GT_PATH = 'data/error.gt.tsv'

# The splitting ratio of the training, validation and testing set.
TRAIN_SPLITS_RATIO = 0.8
TEST_SPLITS_RATIO  = 0.2

SUFFIX = 'top3'




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


def data_preproc():
    """ Preprocess the data for detection purpose.
    """
    # Process test
    def parse(line):
        """ Get the range of each error. """
        sp = line.strip().split('\t')
        str_pos = int(sp[0])
        end_pos = str_pos + len(sp[1])
        return (str_pos, end_pos)
    errs = [parse(l) for l in codecs.open(GT_PATH, 'r', 'utf-8')]

    def candidate_preproc():


def main():
    (train_data, test_data), total_data = data_split(DATA_PATH, GT_PATH,
            TRAIN_SPLITS_RATIO, TEST_SPLITS_RATIO)


    """
    for md, (_override, _weighted, _path, _grid, _estimator) in TRAIN_SETTINGS:
        try:
            if md == model.SKLModel:
                md(_estimator, _grid, train_data, override=_override,
                        weighted=_weighted, pkl_path=_path)
            elif _grid != None:
                md(train_data, override=_override, weighted=_weighted,
                        pkl_path=_path, para_grid=_grid)
            else:
                md(train_data, override=_override, weighted=_weighted,
                        pkl_path=_path)
        except Exception as e:
            #print('Error')
            #print(str(e))
            #print()
            #sys.exit(1)
            pass # skip error model
    """

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
