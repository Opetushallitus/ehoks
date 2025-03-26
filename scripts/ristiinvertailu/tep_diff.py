import csv
import json

import common


def format_ehoks_herate(herate):
    herate['hoks_id'] = int(herate['hoks_id'])
    herate['rahoituskausi'] = \
        common.determine_rahoituskausi(herate['jakso_loppupvm'])

    if 'osa_aikaisuus' in herate:
        if herate['osa_aikaisuus'] == '':
            del herate['osa_aikaisuus']
        else:
            herate['osa_aikaisuus'] = int(herate['osa_aikaisuus'])

    if 'tutkintonimike' in herate:
        if herate['tutkintonimike'] == '("")':
            del herate['tutkintonimike']
        else:
            herate['tutkintonimike'] = \
                herate['tutkintonimike'].replace(",", " ")

    return herate


def format_heratepalvelu_herate(herate):
    if 'kesto' in herate:
        del herate['kesto']
    if 'kesto_vanha' in herate:
        del herate['kesto_vanha']
    if 'nollakeston_syy' in herate:
        del herate['nollakeston_syy']
    del herate['tyopaikan_normalisoitu_nimi']
    del herate['hankkimistapa_id']
    del herate['ohjaaja_ytunnus_kj_tutkinto']
    del herate['niputuspvm']
    del herate['tpk-niputuspvm']
    del herate['request_id']
    del herate['tallennuspvm']
    if 'tunnus' in herate:
        del herate['tunnus']
    if 'rahoitusryhma' in herate:
        del herate['rahoitusryhma']
    if 'mitatoity' in herate:
        del herate['mitatoity']
    if 'osaamisala' in herate:
        del herate['osaamisala']

    return common.remove_ddb_attr_types(herate)


ehoks_jaksot, ehoks_duplicates = common.get_heratteet_and_duplicates(
    lambda file: csv.DictReader(file),
    'outputs/ehoks_tep_kyselyt.csv',
    ['hoks_id', 'yksiloiva_tunniste'],
    format_ehoks_herate)


heratepalvelu_jaksot, heratepalvelu_duplicates = \
        common.get_heratteet_and_duplicates(
            lambda file: json.load(file)['Items'],
            'outputs/ddb_tep_kyselyt.json',
            ['hoks_id', 'yksiloiva_tunniste'],
            format_heratepalvelu_herate)

diff, ids_all_heratteet = \
    common.diffs_per_kj('tep', ehoks_jaksot, heratepalvelu_jaksot)
common.duplicates_to_json('tep', 'ehoks', ehoks_duplicates)
common.duplicates_to_json('tep', 'heratepalvelu', heratepalvelu_duplicates)
common.print_statistics(ids_all_heratteet, diff,
                        ehoks_duplicates, heratepalvelu_duplicates)
common.attr_diff_stats_to_csv('tep', diff)
common.missing_per_kj_to_csv('tep', diff)
common.duplicates_per_kj_to_csv('tep',
                                ehoks_duplicates,
                                heratepalvelu_duplicates)
