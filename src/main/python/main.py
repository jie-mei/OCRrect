#!/bin/sh

import codecs
from sklc import data
from sklc import model

TEMP_DIR = 'tmp/model/'

# The pathname to the generated suggestion data.
DATA_PATH = 'tmp/suggestion.top.3.txt'

# The pathname to the ground truth error list.
GT_PATH = 'data/ocr/error.gt.txt'

# The splitting ratio of the training, validation and testing set.
TRAIN_SPLITS_RATIO = 0.8
TEST_SPLITS_RATIO  = 0.2

# Models:       model class                   override weighted  seralization path  customized grid
MODEL_CONFIG = {model.RandomForestModel     :(False,   True,     TEMP_DIR + 'rf',   None)
               ,model.KernelRidgeModel      :(False,   True,     TEMP_DIR + 'kr',   None)
               ,model.ExtraTreesModel       :(False,   True,     TEMP_DIR + 'et',   None)
               ,model.AdaBoostModel         :(False,   True,     TEMP_DIR + 'ab',   None)
               ,model.GradientBoostingModel :(False,   True,     TEMP_DIR + 'gb',   None)
               ,model.SupportVectorModel    :(False,   True,     TEMP_DIR + 'svm',  None)
               }


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
            return_complement=True)


def main():
    train_data, test_data = data_split(DATA_PATH, GT_PATH,
            TRAIN_SPLITS_RATIO, TEST_SPLITS_RATIO)

    for md, config in MODEL_CONFIG.items():
        if config[3] != None:
            md(train_data, override=config[0], weighted=config[1],
                    pkl_path=config[2], para_grid=config[3])
        else:
            md(train_data, override=config[0], weighted=config[1],
                    pkl_path=config[2])


if __name__ == '__main__':
    main()

print("hello, world!")
