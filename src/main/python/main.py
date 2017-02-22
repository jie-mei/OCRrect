#!/bin/sh

from sklc import data


# The pathname to the generated suggestion data.
DATA_PATH = 'tmp/suggestion.top.3.txt'


def main():
    dataset = data.Dataset.read(DATA_PATH)
    cand = dataset.errors[0].candidates[0]
    print(cand.name)
    print(cand.feature_values)
    print(cand.label)
    print(cand.confidence)


if __name__ == '__main__':
    main()

print("hello, world!")
