import csv
import json

import common


def format_ehoks_herate(herate):
    del herate['voimassa-loppupvm']
    herate['rahoituskausi'] = \
        common.determine_rahoituskausi(herate['heratepvm'])
    herate['ehoks-id'] = int(herate['ehoks-id'])
    return herate


ehoks_heratteet = {}
num_of_duplicates_in_ehoks = 0
id_keys = ['koulutustoimija', 'oppija-oid', 'kyselytyyppi', 'rahoituskausi']

ehoks_heratteet, ehoks_duplicates = common.get_heratteet_and_duplicates(
    lambda file: csv.DictReader(file),
    'outputs/ehoks_amis_kyselyt.csv',
    id_keys,
    format_ehoks_herate)

heratepalvelu_heratteet, heratepalvelu_duplicates = \
        common.get_heratteet_and_duplicates(
            lambda file: json.load(file)['Items'],
            'outputs/ddb_amis_kyselyt.json',
            id_keys,
            lambda x: common.remove_ddb_attr_types(x))

diff, ids_all_heratteet = \
    common.diffs_per_kj('amis', ehoks_heratteet, heratepalvelu_heratteet)
common.duplicates_to_json('amis', 'ehoks', ehoks_duplicates)
common.duplicates_to_json('amis', 'heratepalvelu', heratepalvelu_duplicates)
common.print_statistics(ids_all_heratteet, diff,
                        ehoks_duplicates, heratepalvelu_duplicates)
common.attr_diff_stats_to_csv('amis', diff)
common.missing_per_kj_to_csv('amis', diff)
common.duplicates_per_kj_to_csv('amis',
                                ehoks_duplicates,
                                heratepalvelu_duplicates)
