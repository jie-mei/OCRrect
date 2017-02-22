#!/bin/sh

from sklc import data


# The pathname to the generated suggestion data.
DATA_PATH = 'tmp/suggestion.top.3.txt'


def main():
    dataset = data.Data.read(DATA_PATH)


if __name__ == '__main__':
    main()

print("hello, world!")
