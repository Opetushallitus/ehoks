import csv
import json

from collections import defaultdict
from datetime import datetime
from enum import Enum


class DiffType(Enum):
    VALUES_DIFFER = 1
    MISSING_FROM_EHOKS = 2
    MISSING_FROM_HERATEPALVELU = 3


def determine_rahoituskausi(heratepvm):
    date = datetime.strptime(heratepvm, '%Y-%m-%d').date()
    if date.month >= 7:
        start_year = date.year
    else:
        start_year = date.year - 1
    end_year = start_year + 1
    return f'{start_year}-{end_year}'


def remove_ddb_attr_types(item):
    j = {}
    for key, val in item.items():
        if 'S' in val:
            j[key] = val['S']
        elif 'N' in val:
            j[key] = int(val['N'])
        elif 'B' in val:
            j[key] = val['B'] == 'true'
    return j


def get_ids(herate, keys):
    return tuple(map(lambda key: herate[key], keys))


def get_heratteet_and_duplicates(file_reader, filename, id_keys, format_func):
    heratteet = {}
    duplicate_heratteet = {}

    with open(filename, 'r') as file:
        heratteet_dict = file_reader(file)

        for herate in heratteet_dict:
            # Remove fields that have an empty string as a value
            for key, val in herate.copy().items():
                if val == '' or isinstance(val, dict) and \
                        next(iter(val.items())) == '':
                    del herate[key]
            herate = format_func(herate)
            ids = get_ids(herate, id_keys)
            if ids in heratteet:
                if ids in duplicate_heratteet:
                    duplicate_heratteet[ids].append(herate)
                else:
                    duplicate_heratteet[ids] = [heratteet[ids], herate]
            else:
                heratteet[ids] = herate

    return heratteet, duplicate_heratteet


def num_of_duplicates(duplicates_dict):
    return sum(map(lambda x: len(x) - 1, duplicates_dict.values()))


def duplicates_per_kj(duplicates_dict):
    kj_duplicates = {}
    for ids, dups in duplicates_dict.items():
        kj = dups[0]['koulutustoimija']
        if kj not in kj_duplicates:
            kj_duplicates[kj] = {}
        kj_duplicates[kj][ids] = dups
    return kj_duplicates


def determine_fieldnames(dicts):
    fieldnames = set()
    for d in dicts:
        fieldnames = fieldnames.union(d.keys())
    return list(fieldnames)


def key_diff_stats_to_csv(filename, key_diff_stats):
    print(f'Writing `{filename}`...')
    with open(filename, 'w') as file:
        writer = csv.writer(file)
        for key, val in key_diff_stats.items():
            writer.writerow([key, val])


def key_diff_stats_per_kj_to_csv(filename, key_diff_stats_per_kj):
    print(f'Writing `{filename}`...')
    with open(filename, 'w') as file:
        fieldnames = ['koulutustoimija'] + \
                determine_fieldnames(key_diff_stats_per_kj.values())
        writer = csv.DictWriter(file, fieldnames=fieldnames)
        writer.writeheader()
        for kj, d in key_diff_stats_per_kj.items():
            d['koulutustoimija'] = kj
            writer.writerow(d)


def missing_per_kj_to_csv(filename, missing_from_ehoks_kj,
                          missing_from_heratepalvelu_kj):
    print(f'Writing `{filename}`...')
    with open(filename, 'w') as file:
        writer = csv.writer(file)
        writer.writerow(['koulutustoimija', 'missing from ehoks',
                         'missing from hp'])
        for key in set().union(missing_from_ehoks_kj.keys(),
                               missing_from_heratepalvelu_kj.keys()):
            writer.writerow([key, missing_from_ehoks_kj[key],
                             missing_from_heratepalvelu_kj[key]])


def duplicates_per_kj_to_csv(filename, ehoks_duplicates,
                             heratepalvelu_duplicates):
    duplicates_per_kj = defaultdict(lambda: defaultdict(int))

    for dups in ehoks_duplicates.values():
        kj = dups[0]['koulutustoimija']
        duplicates_per_kj[kj]['ehoks'] += len(dups) - 1

    for dups in heratepalvelu_duplicates.values():
        kj = dups[0]['koulutustoimija']
        duplicates_per_kj[kj]['heratepalvelu'] += len(dups) - 1

    print(f'Writing `{filename}`...')
    with open(filename, 'w') as file:
        writer = csv.writer(file)
        writer.writerow(['koulutustoimija', 'dups in ehoks', 'dups in hp'])
        for kj, dups in duplicates_per_kj.items():
            writer.writerow([kj, dups['ehoks'], dups['heratepalvelu']])


def data_to_csv(filename, data):
    print(f'Writing `{filename}`...')
    for key in data.copy().keys():
        data[','.join(map(str, key))] = data[key]
        del data[key]

    with open(filename, 'w') as file:
        json.dump(data, file, indent=2)


def print_statistics_and_output_to_csvs(kyselytyyppi, ehoks_heratteet,
                                        ehoks_duplicates,
                                        heratepalvelu_heratteet,
                                        heratepalvelu_duplicates):
    diff = {}
    key_diff_stats_per_kj = defaultdict(lambda: defaultdict(int))
    key_diff_stats = defaultdict(int)
    missing_from_ehoks_kj = defaultdict(int)
    missing_from_heratepalvelu_kj = defaultdict(int)
    heratteet_total = 0
    values_differ = 0
    missing_from_ehoks_total = 0
    missing_from_heratepalvelu_total = 0

    for ids in set().union(ehoks_heratteet.keys(),
                           heratepalvelu_heratteet.keys()):
        heratteet_total += 1

        if ids in ehoks_heratteet and ids in heratepalvelu_heratteet:
            herate_diff = {}
            ehoks_herate = ehoks_heratteet[ids]
            heratepalvelu_herate = heratepalvelu_heratteet[ids]
            kj = ehoks_herate['koulutustoimija']

            for key in set().union(ehoks_herate.keys(),
                                   heratepalvelu_herate.keys()):
                ehoks_value = \
                    ehoks_herate[key] if key in ehoks_herate else None
                heratepalvelu_value = \
                    heratepalvelu_herate[key] if key in heratepalvelu_herate \
                    else None

                if key in ehoks_herate and key in heratepalvelu_herate:
                    if ehoks_value != heratepalvelu_value:
                        key_diff_stats[key] += 1
                        key_diff_stats_per_kj[kj][key] += 1
                        herate_diff[key] = {
                            'ehoks': ehoks_value,
                            'heratepalvelu': heratepalvelu_value
                        }

            if herate_diff:
                diff[ids] = {'type': str(DiffType.VALUES_DIFFER),
                             'herate': herate_diff}
                values_differ += 1

        elif ids in ehoks_heratteet:
            ehoks_herate = ehoks_heratteet[ids]
            diff[ids] = {'type': str(DiffType.MISSING_FROM_HERATEPALVELU),
                         'herate': ehoks_herate}
            missing_from_heratepalvelu_total += 1
            missing_from_heratepalvelu_kj[ehoks_herate['koulutustoimija']] += 1

        else:
            heratepalvelu_herate = heratepalvelu_heratteet[ids]
            diff[ids] = {'type': str(DiffType.MISSING_FROM_EHOKS),
                         'herate': heratepalvelu_herate}
            missing_from_ehoks_total += 1
            missing_from_ehoks_kj[heratepalvelu_herate['koulutustoimija']] += 1

    print('\n--- Statistics ---')
    print(f'Total number of heratteet: {heratteet_total}')
    print(f'Heratteet where values differ: {values_differ}')
    print('Heratteet found in palaute-backend but missing from ' +
          f'Herätepalvelu: {missing_from_heratepalvelu_total}')
    print('Heratteet found in Herätepalvelu but missing from ' +
          f'palaute-backend: {missing_from_ehoks_total}')
    print('Number of duplicates in palaute-backend: ' +
          str(num_of_duplicates(ehoks_duplicates)))
    print('Number of duplicates in Herätepalvelu: ' +
          str(num_of_duplicates(heratepalvelu_duplicates)))
    print()

    data_to_csv(f'outputs/{kyselytyyppi}_diff.json', diff)
    data_to_csv(f'outputs/{kyselytyyppi}_ehoks_duplicates.json',
                ehoks_duplicates)
    data_to_csv(f'outputs/{kyselytyyppi}_heratepalvelu_duplicates.json',
                heratepalvelu_duplicates)

    key_diff_stats_to_csv(f'outputs/{kyselytyyppi}_key_diff_stats.csv',
                          key_diff_stats)
    key_diff_stats_per_kj_to_csv(
        f'outputs/{kyselytyyppi}_key_diff_stats_per_kj.csv',
        key_diff_stats_per_kj)
    missing_per_kj_to_csv(f'outputs/{kyselytyyppi}_missing_per_kj.csv',
                          missing_from_ehoks_kj, missing_from_heratepalvelu_kj)
    duplicates_per_kj_to_csv(f'outputs/{kyselytyyppi}_duplicates_per_kj.csv',
                             ehoks_duplicates, heratepalvelu_duplicates)
