import csv
import json

import common

with open('outputs/ddb_amis_kyselyt.json', 'r') as file:
    ddb_heratteet_list = json.load(file)['Items']


ehoks_heratteet = {}
num_of_duplicates_in_ehoks = 0

with open('outputs/ehoks_amis_kyselyt.csv', 'r') as file:
    csv_reader = csv.DictReader(file)
    for herate in csv_reader:
        rahoituskausi = common.determine_rahoituskausi(herate['heratepvm'])
        ids = (int(herate['ehoks-id']), herate['kyselytyyppi'], rahoituskausi)
        if ids in ehoks_heratteet:
            num_of_duplicates_in_ehoks += 1
        else:
            new_herate = herate.copy()
            for key, val in herate.items():
                if val == '':
                    del new_herate[key]

            del new_herate['voimassa-loppupvm']
            new_herate['ehoks-id'] = int(new_herate['ehoks-id'])
            new_herate['rahoituskausi'] = rahoituskausi
            ehoks_heratteet[ids] = new_herate

heratepalvelu_heratteet = {}
num_of_duplicates_in_hp = 0

for herate in ddb_heratteet_list:
    rahoituskausi = common.determine_rahoituskausi(herate['heratepvm']['S'])
    ids = (int(herate['ehoks-id']['N']), herate['kyselytyyppi']['S'], rahoituskausi)
    if ids in heratepalvelu_heratteet:
        num_of_duplicates_in_hp += 1
    else:
        new_herate = herate.copy()
        for key, val in herate.items():
            if val == '':
                del new_herate[key]

        heratepalvelu_heratteet[ids] = \
            common.remove_ddb_attr_types(new_herate)

common.diff('outputs/amis_diff.json',
            ehoks_heratteet,
            heratepalvelu_heratteet,
            num_of_duplicates_in_ehoks,
            num_of_duplicates_in_hp)
