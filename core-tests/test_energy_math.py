#!/usr/bin/env python3
import math
MAX_ENERGY = 100
def after_fast(energy, gain, count=1):
    return min(MAX_ENERGY, energy + max(0, gain) * count)
def after_charged(energy, cost):
    assert energy >= cost
    return energy - cost
def remaining(energy, cost, fast_gain):
    missing=max(0,cost-energy)
    return 0 if missing==0 else math.ceil(missing/max(1,fast_gain))
assert after_charged(80,45) == 35
assert after_fast(95,8) == 100
assert remaining(35,50,8) == 2
assert remaining(50,50,8) == 0
print('Energy math: OK')
