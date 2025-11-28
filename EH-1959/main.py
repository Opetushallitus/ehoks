import argparse
import getpass
import json
import os
import pickle
import sys

import psycopg

INTERMEDIATE_RESULTS_FILENAME = 'results.pickle'
FINAL_RESULTS_FILENAME = 'results.json'

PG_DB_NAME = 'ehoks'
PG_DB_HOST = 'localhost'
PG_DB_PORT = 55432

# Read SQL queries
with open('select_vastaajatunnukset.sql', 'r') as file:
    select_vastaajatunnukset_query = file.read()

with open('select_tutkinnon_osa_tiedot.sql', 'r') as file:
    select_tutkinnon_osa_tiedot = file.read()


def save_intermediate_results(results):
    print(f'Saving the intermediate results to `{INTERMEDIATE_RESULTS_FILENAME}`')
    with open(INTERMEDIATE_RESULTS_FILENAME, 'wb') as file:
        pickle.dump(results, file)


def load_intermediate_results():
    if not os.path.exists(INTERMEDIATE_RESULTS_FILENAME):
        return {}
    try:
        with open(INTERMEDIATE_RESULTS_FILENAME, 'rb') as file:
            results = pickle.load(file)
            print(f'Loaded {len(results)} intermediate results from {INTERMEDIATE_RESULTS_FILENAME}.')
            return results
    except Exception as e:
        print(f'Warning: failed to load intermediate results: {e}')
        return {}


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('--start-date', dest='start_date', required=True)
    parser.add_argument('--end-date', dest='end_date', required=True)
    parser.add_argument('--db-username', dest='db_username', required=True)
    parser.add_argument('--db-password', dest='db_password',
                        help='If not provided, password is prompted.')
    return parser.parse_args()


if __name__ == '__main__':
    args = parse_args()

    if args.db_password:
        db_password = args.db_password
    else:
        db_password = getpass.getpass('Postgres password: ')

    results = load_intermediate_results()
    results_total = len(results)

    with psycopg.connect(dbname=PG_DB_NAME,
                         user=args.db_username,
                         password=db_password,
                         host=PG_DB_HOST,
                         port=PG_DB_PORT) as conn:
        print('Fetching vastaajatunnukset...', end=' ')
        with conn.cursor() as cur:
            cur.execute(select_vastaajatunnukset_query, (args.start_date,
                                                         args.end_date))
            tunnus_hoks_id_pairs = cur.fetchall()
            tunnukset_total = len(tunnus_hoks_id_pairs)
        print('Done.')

        try:
            print('Fetching tutkinnon osa tiedot...')
            with conn.cursor() as cur:
                for tunnus, hoks_id in tunnus_hoks_id_pairs:
                    if tunnus not in results:
                        cur.execute(select_tutkinnon_osa_tiedot, (hoks_id,))
                        row = cur.fetchone()[0]
                        results[tunnus] = row
                        results_total += 1

                        if results_total % 100 == 0:
                            print(f'{results_total} / {tunnukset_total}')

        except KeyboardInterrupt:
            print('Interrupted.', end=' ')
            save_intermediate_results(results)
        except Exception as e:
            print(f'Error during fetching tutkinnon osa tiedot: {e}')
            save_intermediate_results(results)

        print('Done.')
        save_intermediate_results(results)

        with open(FINAL_RESULTS_FILENAME, 'w') as file:
            print('Saving final results...', end=' ')
            json.dump(results, file, indent=4)
            print('Done.')
