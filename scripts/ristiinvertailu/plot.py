import sys
import json
import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import datetime as dt
from datetime import datetime


from collections import defaultdict

matplotlib.use('Qt5Agg')

heratepvm_key = 'heratepvm' if sys.argv[1] == 'amis' else 'jakso_loppupvm'


def plot_missing(missing, diff, total_herate_per_pvm):
    kyselytyyppi = sys.argv[1]
    missing_per_heratepvm = {date: 0 for date in date_range()}

    for kj_heratteet in diff[missing].values():
        for herate in kj_heratteet:
            try:
                missing_per_heratepvm[herate[heratepvm_key]] += 1
            except KeyError:
                print(herate)

    for date in missing_per_heratepvm.copy().keys():
        if date not in total_herate_per_pvm:
            del missing_per_heratepvm[date]

    names = list(missing_per_heratepvm.keys())
    values = list(missing_per_heratepvm.values())
    bottom = np.zeros(len(names))

    fix, ax = plt.subplots()
    ax.bar(names, values, label='missing', bottom=bottom)
    bottom += values
    ax.bar(names, list(total_herate_per_pvm.values()), label='total', bottom=bottom)
    ax.legend()
    plt.title(f'{kyselytyyppi}, {missing}')
    plt.show()


def plot_duplicates(palvelu, duplicates, total_herate_per_pvm):
    kyselytyyppi = sys.argv[1]
    duplicates_per_heratepvm = {date: 0 for date in date_range()}

    for dups in duplicates.values():
        for d in dups:
            heratepvm = d[heratepvm_key]
            if heratepvm in duplicates_per_heratepvm:
                duplicates_per_heratepvm[heratepvm] += 1

    for date in duplicates_per_heratepvm.copy().keys():
        if date not in total_herate_per_pvm:
            del duplicates_per_heratepvm[date]

    names = list(duplicates_per_heratepvm.keys())
    values = list(duplicates_per_heratepvm.values())
    bottom = np.zeros(len(names))

    fix, ax = plt.subplots()
    ax.bar(names, values, label='duplicates', bottom=bottom)
    ax.legend()
    plt.title(f'{kyselytyyppi}, {palvelu} duplicates')
    plt.show()


def date_range():
    alkupvm = datetime.strptime(sys.argv[2], '%Y-%m-%d')
    loppupvm = datetime.strptime(sys.argv[3], '%Y-%m-%d')
    dates = [datetime.strftime(alkupvm + dt.timedelta(days=x), '%Y-%m-%d')
             for x in range((loppupvm - alkupvm).days + 1)]
    return dates


kyselytyyppi = sys.argv[1]

with open(f'outputs/{kyselytyyppi}_diff.json', 'r') as file:
    diff = json.load(file)

with open(f'outputs/ddb_{kyselytyyppi}_kyselyt.json', 'r') as file:
    total_herate_per_pvm = defaultdict(int)
    for item in json.load(file)['Items']:
        total_herate_per_pvm[item[heratepvm_key]['S']] += 1

    total_herate_per_pvm = dict(sorted(total_herate_per_pvm.items()))

with open(f'outputs/{kyselytyyppi}_ehoks_duplicates.json', 'r') as file:
    ehoks_duplicates = json.load(file)

with open(f'outputs/{kyselytyyppi}_heratepalvelu_duplicates.json', 'r') as file:
    heratepalvelu_duplicates = json.load(file)

plot_missing('missing from ehoks', diff, total_herate_per_pvm)
plot_missing('missing from heratepalvelu', diff, total_herate_per_pvm)
plot_duplicates('ehoks', ehoks_duplicates, total_herate_per_pvm)
plot_duplicates('heratepalvelu', heratepalvelu_duplicates, total_herate_per_pvm)
