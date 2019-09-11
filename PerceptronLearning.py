import numpy as np


class Sample:
    x = None
    y = 0

    def __init__(self, x, y):
        self.x = x
        self.y = y


samples = list()
samples.append(Sample(np.mat([3, 3]), 1))
samples.append(Sample(np.mat([4, 3]), 1))
samples.append(Sample(np.mat([1, 1]), -1))
w = np.mat([[0, 0]])
b = 0
eta = 1
while True:
    ww = w.copy()
    bb = b
    for s in samples:
        while s.y * (w * s.x.T + b) <= 0:
            w = w + eta * s.y * s.x
            b = b + eta * s.y
            print(w, b)
    if (ww == w).all() and bb == b:
        break
