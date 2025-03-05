import csv
import json

import common


with open('outputs/ddb_tep_kyselyt.json', 'r') as file:
    ddb_jaksot_list = json.load(file)['Items']


ehoks_jaksot = {}
num_of_duplicates_in_ehoks = 0
num_of_missing_yt_in_ehoks = 0

with open('outputs/ehoks_tep_kyselyt.csv', 'r') as file:
    csv_reader = csv.DictReader(file)
    for jakso in csv_reader:
        if 'yksiloiva_tunniste' not in jakso:
            num_of_missing_yt_in_ehoks += 1
        else:
            ids = (int(jakso['hoks_id']), jakso['yksiloiva_tunniste'])
            if ids in ehoks_jaksot:
                num_of_duplicates_in_ehoks += 1
            else:
                new_jakso = jakso.copy()
                for key, val in jakso.items():
                    if val == '':
                        del new_jakso[key]

                if 'osa_aikaisuus' in new_jakso:
                    new_jakso['osa_aikaisuus'] = int(new_jakso['osa_aikaisuus'])
                new_jakso['hoks_id'] = int(new_jakso['hoks_id'])
                if 'tutkintonimike' in new_jakso:
                    if new_jakso['tutkintonimike'] == '("")':
                        del new_jakso['tutkintonimike']
                    else:
                        new_jakso['tutkintonimike'] = new_jakso['tutkintonimike'].replace(",", " ")

                new_jakso['rahoituskausi'] = common.determine_rahoituskausi(new_jakso['jakso_loppupvm'])
                if ids in ehoks_jaksot:
                    num_of_duplicates_in_ehoks += 1

                ehoks_jaksot[ids] = new_jakso

heratepalvelu_jaksot = {}
num_of_duplicates_in_hp = 0
num_of_missing_yt_in_hp = 0

for jakso in ddb_jaksot_list:
    if 'yksiloiva_tunniste' not in jakso:
        num_of_missing_yt_in_hp += 1
    else:
        ids = (int(jakso['hoks_id']['N']), jakso['yksiloiva_tunniste']['S'])
        if ids in heratepalvelu_jaksot:
            num_of_duplicates_in_hp += 1
        else:
            new_jakso = jakso.copy()
            for key, val in jakso.items():
                if val == '':
                    del new_jakso[key]

            if 'kesto' in new_jakso: del new_jakso['kesto']
            if 'kesto_vanha' in new_jakso: del new_jakso['kesto_vanha']
            if 'nollakeston_syy' in new_jakso: del new_jakso['nollakeston_syy']
            del new_jakso['tyopaikan_normalisoitu_nimi']
            del new_jakso['hankkimistapa_id']
            del new_jakso['ohjaaja_ytunnus_kj_tutkinto']
            del new_jakso['niputuspvm']
            del new_jakso['tpk-niputuspvm']
            del new_jakso['request_id']
            del new_jakso['tallennuspvm']
            if 'tunnus' in new_jakso: del new_jakso['tunnus']
            if 'rahoitusryhma' in new_jakso: del new_jakso['rahoitusryhma']
            if 'mitatoity' in new_jakso: del new_jakso['mitatoity']
            if 'osaamisala' in new_jakso: del new_jakso['osaamisala']

            heratepalvelu_jaksot[(int(new_jakso['hoks_id']['N']), new_jakso['yksiloiva_tunniste']['S'])] = common.remove_ddb_attr_types(new_jakso)

common.diff('outputs/tep_diff.json',
            ehoks_jaksot,
            heratepalvelu_jaksot,
            num_of_duplicates_in_ehoks,
            num_of_duplicates_in_hp,
            num_of_missing_yt_in_ehoks,
            num_of_missing_yt_in_hp)
