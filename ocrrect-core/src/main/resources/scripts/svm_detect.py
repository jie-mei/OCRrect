#!/usr/bin/env python
# -*- coding: utf-8 -*-

import pandas as pd
import numpy as np
from sklearn import svm
from sklearn.externals import joblib
import sys


def train(data_path, model_path):
    """ Receive TSV training data from file and train a SVM model.
    """
    df = pd.read_table(data_path, header=None)
    x = df.loc[:, 0:df.shape[1] - 2].as_matrix()
    y = np.squeeze(np.asarray((df.loc[:, df.shape[1] - 1].as_matrix())))
    lm = svm.SVC()
    lm.fit(x, y)
    joblib.dump(lm, model_path)


def predict(data_path, model_path):
    """ Receive TSV data file and predict according labels using a pretrained
    model. The predicted labels are printed to `stdout` as a TSV string.
    """
    x = pd.read_table(data_path, header=None).as_matrix()
    lm = joblib.load(model_path)
    print('\t'.join(str(v) for v in lm.predict(x).tolist()))


if __name__ == '__main__':
    locals()[sys.argv[1]](*sys.argv[2:])

