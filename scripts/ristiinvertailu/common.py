import csv
import json

from collections import defaultdict
from datetime import datetime


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


def duplicates_to_json(kyselytyyppi, palvelu, duplicates):
    filename = f'outputs/{kyselytyyppi}_{palvelu}_duplicates.json'
    new_duplicates = {}
    for k, v in duplicates.items():
        new_duplicates[','.join(str(k))] = v

    print(f'Writing `{filename}`...')
    with open(filename, 'w') as file:
        json.dump(new_duplicates, file)


def diffs_per_kj(kyselytyyppi, ehoks_heratteet, heratepalvelu_heratteet):

    diffs = {'values differ': defaultdict(list),
             'missing from ehoks': defaultdict(list),
             'missing from heratepalvelu': defaultdict(list)}
    ids_all_heratteet = set().union(ehoks_heratteet.keys(),
                                    heratepalvelu_heratteet.keys())

    for ids in ids_all_heratteet:

        if ids in ehoks_heratteet and ids in heratepalvelu_heratteet:
            herate_diff = {}
            ehoks_herate = ehoks_heratteet[ids]
            heratepalvelu_herate = heratepalvelu_heratteet[ids]

            for key in set().union(ehoks_herate.keys(),
                                   heratepalvelu_herate.keys()):
                ehoks_value = \
                    ehoks_herate[key] if key in ehoks_herate else None
                heratepalvelu_value = \
                    heratepalvelu_herate[key] if key in heratepalvelu_herate \
                    else None

                if key in ehoks_herate and key in heratepalvelu_herate:
                    if ehoks_value != heratepalvelu_value:
                        herate_diff[key] = {
                            'ehoks': ehoks_value,
                            'heratepalvelu': heratepalvelu_value
                        }

            if herate_diff:
                kj = ehoks_herate['koulutustoimija'] \
                    if 'koulutustoimija' in ehoks_herate \
                    else heratepalvelu_herate['koulutustoimija']
                diffs['values differ'][kj].append(herate_diff)

        elif ids in ehoks_heratteet:
            ehoks_herate = ehoks_heratteet[ids]
            kj = ehoks_herate['koulutustoimija']
            diffs['missing from heratepalvelu'][kj].append(ehoks_herate)

        else:
            heratepalvelu_herate = heratepalvelu_heratteet[ids]
            kj = heratepalvelu_herate['koulutustoimija']
            diffs['missing from ehoks'][kj].append(heratepalvelu_herate)

    filename = f'outputs/{kyselytyyppi}_diff.json'
    print(f'Writing `{filename}`...')
    with open(filename, 'w') as file:
        json.dump(diffs, file, indent=4)

    return diffs, ids_all_heratteet


def print_statistics(ids_all_heratteet, diff,
                     ehoks_duplicates, heratepalvelu_duplicates):
    heratteet_total = len(ids_all_heratteet)
    values_differ = sum(map(lambda x: len(x), diff['values differ'].values()))
    missing_from_ehoks = sum(map(lambda x: len(x),
                                 diff['missing from ehoks'].values()))
    missing_from_heratepalvelu = sum(map(lambda x: len(x),
                                         diff['missing from heratepalvelu']
                                         .values()))

    print('\n--- Statistics ---')
    print(f'Total number of heratteet: {heratteet_total}')
    print(f'Heratteet where values differ: {values_differ}')
    print('Heratteet found in palaute-backend but missing from ' +
          f'Herätepalvelu: {missing_from_heratepalvelu}')
    print('Heratteet found in Herätepalvelu but missing from ' +
          f'palaute-backend: {missing_from_ehoks}')
    print('Number of duplicates in palaute-backend: ' +
          str(num_of_duplicates(ehoks_duplicates)))
    print('Number of duplicates in Herätepalvelu: ' +
          str(num_of_duplicates(heratepalvelu_duplicates)))
    print()


def to_csv(filename, fields, items):
    print(f'Writing `{filename}`...')
    with open(filename, 'w') as file:
        writer = csv.writer(file)
        writer.writerow(fields)
        for attr, val in items.items():
            writer.writerow([attr, val])


def csv_filename(part):
    return f'outputs/{part}.csv'


def attr_diff_stats_to_csv(kyselytyyppi, diffs):
    attr_diff_stats = defaultdict(int)
    attr_diff_stats_per_kj = defaultdict(lambda: defaultdict(int))
    for kj, kj_diffs in diffs['values differ'].items():
        for palaute_diff in kj_diffs:
            for key in palaute_diff.keys():
                attr_diff_stats[key] += 1
                attr_diff_stats_per_kj[kj][key] += 1

    filename = f'outputs/{kyselytyyppi}_attr_diff_stats.csv'
    print(f'Writing `{filename}`...')
    with open(filename, 'w') as file:
        writer = csv.writer(file)
        writer.writerow(['attribute', 'value'])
        for attr, val in attr_diff_stats.items():
            writer.writerow([attr, val])

    filename = f'outputs/{kyselytyyppi}_attr_diff_stats_per_kj.csv'
    print(f'Writing `{filename}`...')
    with open(filename, 'w') as file:
        fieldnames = ['koulutustoimija'] + \
                determine_fieldnames(attr_diff_stats_per_kj.values())
        writer = csv.DictWriter(file, fieldnames=fieldnames)
        writer.writeheader()
        for kj, d in attr_diff_stats_per_kj.items():
            d['koulutustoimija'] = kj
            writer.writerow(d)


def missing_per_kj_to_csv(kyselytyyppi, diff):
    missing_from_ehoks_per_kj = defaultdict(int)
    for kj, diffs in diff['missing from ehoks'].items():
        missing_from_ehoks_per_kj[kj] = len(diffs)

    missing_from_heratepalvelu_per_kj = defaultdict(int)
    for kj, diffs in diff['missing from heratepalvelu'].items():
        missing_from_heratepalvelu_per_kj[kj] = len(diffs)

    filename = f'outputs/{kyselytyyppi}_missing_per_kj.csv'
    print(f'Writing `{filename}`...')
    with open(filename, 'w') as file:
        writer = csv.writer(file)
        writer.writerow(['koulutustoimija', 'missing from ehoks',
                         'missing from heratepalvelu'])
        for key in set().union(missing_from_ehoks_per_kj.keys(),
                               missing_from_heratepalvelu_per_kj.keys()):
            writer.writerow([key, missing_from_ehoks_per_kj[key],
                             missing_from_heratepalvelu_per_kj[key]])


def duplicates_per_kj_to_csv(kyselytyyppi, ehoks_duplicates,
                             heratepalvelu_duplicates):
    duplicates_per_kj = defaultdict(lambda: defaultdict(int))

    for dups in ehoks_duplicates.values():
        kj = dups[0]['koulutustoimija']
        duplicates_per_kj[kj]['ehoks'] += len(dups) - 1

    for dups in heratepalvelu_duplicates.values():
        kj = dups[0]['koulutustoimija']
        duplicates_per_kj[kj]['heratepalvelu'] += len(dups) - 1

    filename = f'outputs/{kyselytyyppi}_duplicates_per_kj.csv'
    print(f'Writing `{filename}`...')
    with open(filename, 'w') as file:
        writer = csv.writer(file)
        writer.writerow(['koulutustoimija', 'dups in ehoks',
                         'dups in heratepalvelu'])
        for kj, dups in duplicates_per_kj.items():
            writer.writerow([kj, dups['ehoks'], dups['heratepalvelu']])
