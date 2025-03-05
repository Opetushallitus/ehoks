import json

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


def diff(filename, ehoks_heratteet, heratepalvelu_heratteet,
         num_of_duplicates_in_ehoks, num_of_duplicates_in_hp,
         num_of_missing_yt_in_ehoks=None, num_of_missing_yt_in_hp=None):
    diff = {}
    key_diff_stats = {}
    heratteet_total = 0
    values_differ = 0
    missing_from_ehoks = 0
    missing_from_heratepalvelu = 0

    for ids in set().union(ehoks_heratteet.keys(), heratepalvelu_heratteet.keys()):
        heratteet_total += 1
        if ids in ehoks_heratteet and ids in heratepalvelu_heratteet:
            herate_diff = {}
            ehoks_herate = ehoks_heratteet[ids]
            heratepalvelu_herate = heratepalvelu_heratteet[ids]

            for key in set().union(ehoks_herate.keys(), heratepalvelu_herate.keys()):
                ehoks_value = ehoks_herate[key] if key in ehoks_herate else None
                heratepalvelu_value = heratepalvelu_herate[key] if key in heratepalvelu_herate else None

                if key in ehoks_herate and key in heratepalvelu_herate:
                    if ehoks_value != heratepalvelu_value:
                        if key in key_diff_stats:
                            key_diff_stats[key] += 1
                        else:
                            key_diff_stats[key] = 1

                        herate_diff[key] = {'ehoks': ehoks_value,
                                            'heratepalvelu': heratepalvelu_value}

            if herate_diff:
                diff[ids] = {'type': str(DiffType.VALUES_DIFFER),
                             'herate': herate_diff}
                values_differ += 1

        elif ids in ehoks_heratteet:
            diff[ids] = {'type': str(DiffType.MISSING_FROM_HERATEPALVELU),
                         'herate': ehoks_heratteet[ids]}
            missing_from_heratepalvelu += 1
        else:
            diff[ids] = {'type': str(DiffType.MISSING_FROM_EHOKS),
                         'herate': heratepalvelu_heratteet[ids]}
            missing_from_ehoks += 1

    for key in diff.copy().keys():
        diff[f'{key[0]},{key[1]}'] = diff[key]
        del diff[key]

    with open(filename, 'w+') as file:
        json.dump(diff, file, indent=2)

    print('\n--- Statistics ---')
    print(f"Total number of heratteet: {heratteet_total}")
    print(f"Heratteet where values differ: {values_differ}")
    print(f"Heratteet found in palaute-backend but missing from Herätepalvelu: {missing_from_heratepalvelu}")
    print(f"Heratteet found in Herätepalvelu but missing from palaute-backend: {missing_from_ehoks}")
    print(f"Number of duplicates in palaute-backend: {num_of_duplicates_in_ehoks}")
    print(f"Number of duplicates in Herätepalvelu: {num_of_duplicates_in_hp}")
    if num_of_missing_yt_in_ehoks is not None:
        print(f"Number of missing yksiloiva tunniste in palaute-backend: {num_of_missing_yt_in_ehoks}")
    if num_of_missing_yt_in_hp is not None:
        print(f"Number of missing yksiloiva tunniste in Herätepalvelu: {num_of_missing_yt_in_hp}")



    print('\n--- Differences in attributes ---')
    for key, val in key_diff_stats.items():
        print(f'{key}: {val}')
