import argparse
import datetime
import glob
import json
import logging
import os

import pandas as pd
from elasticsearch import Elasticsearch
from elasticsearch.helpers import bulk

logging.basicConfig(format='%(asctime)s %(message)s', level=logging.INFO)
logger = logging.getLogger(__name__)

INDEX_NAME = 'stock'
# Attributes
SYMBOL = 'symbol'
DATE = 'date'
OPEN = 'open'
CLOSE = 'close'
HIGH = 'high'
LOW = 'low'
VOLUME = 'volume'

ES_HOST = 'ES_HOST'
ES_PORT = 'ES_PORT'
ES_USER = 'ES_USER'
ES_PASSWORD = 'ES_PASSWORD'

KEYS = ['symbol', 'date', 'open', 'close', 'high', 'low', 'volume']

pairs = {
    'BTCUSD': 'BTC',
    'DOGEUSD': 'DOGE',
    'ADAUSD': 'ADA',
    'ETHUSD': 'ETH',
    'XRPUSD': 'XRP'
}

# set up elasticsearch mappings and create index
# time,open,close,high,low,volume
index_mappings = {
    "mappings": {
        "stock": {
            "properties": {
                "symbol": {
                    "type": "keyword"
                },
                "date": {
                    "type": "date",
                    "format": "epoch_millis"
                },
                "open": {
                    "type": "float"
                },
                "close": {
                    "type": "float"
                },
                "high": {
                    "type": "float"
                },
                "low": {
                    "type": "float"
                },
                "volume": {
                    "type": "float"
                }
            }
        }
    }
}


def filterKeys(document):
    return {key: document[key] for key in KEYS}


def timestamp2date(ts_ms):
    return datetime.datetime.fromtimestamp(ts_ms // 1000).isoformat()


def compose_doc(document):
    return {
        SYMBOL: pairs[document[SYMBOL]],
        DATE: document[DATE],
        OPEN: document[OPEN],
        CLOSE: document[CLOSE],
        HIGH: document[HIGH],
        LOW: document[LOW],
        VOLUME: document[VOLUME]
    }


def doc_generator(df, index_name):
    df_iter = df.iterrows()
    for index, document in df_iter:
        yield {
            "_index": f"{index_name}",
            "_id": f"{document[SYMBOL]}-{document[DATE] // 1000}",
            "_source": compose_doc(document)
        }


def elastic_from_env():
    es_url = 'http://{}:{}@{}:{}/'.format(
        os.environ[ES_USER],
        os.environ[ES_PASSWORD],
        os.environ.get(ES_HOST, 'localhost'),
        os.environ.get(ES_PORT, '9200')
    )
    es = Elasticsearch([es_url])
    return es


def config_from_env():
    return {
        "elastic": {
            "host": os.environ.get(ES_HOST, 'localhost'),
            "port": os.environ.get(ES_PORT, '9200'),
            "user": os.environ[ES_USER],
            "pass": os.environ[ES_PASSWORD]
        },
        "data_path": "/tmp"
    }


def elastic(conf):
    es_url = 'http://{}:{}@{}:{}/'.format(
        conf["elastic"]["user"],
        conf["elastic"]["pass"],
        conf["elastic"]["host"],
        conf["elastic"]["port"]
    )
    es = Elasticsearch([es_url])
    return es


def index_file(conf, fname, index_name):
    logger.info(f"index_file: fname={fname} inde_name={index_name}")
    df = pd.read_csv(fname)
    es = elastic(conf)
    result = index_df(es, df, index_name)
    return result


def index_df(es, df, index_name, index_create=False):
    if index_create:
        es.indices.create(index=index_name, body=index_mappings, ignore=400)
    logger.info(f"index_file: Num. records={len(df.index)}")
    result = bulk(es, doc_generator(df, index_name=index_name))
    logger.info(f"index_file: bulk result={result}")
    return result


# python indexer.py -c config.json -i stock_trading -f ./data/stock-BTCUSD-20210501000000-20210601000000.csv
# python indexer.py -c config.json -i stock_trading -l "./data/stock-BTCUSD-20210501000000-20210503000000.*"
# python indexer.py -c config.json -i stock_trading -l "./data/stock-*.csv"
# python indexer.py -c config.json -i test_trading -l "./data/test-*.csv"
if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("-c", "--config",
                       dest="config",
                       help="config file (json)",
                       action="store",
                       type=str,
                       required=False)

    group.add_argument("-e", "--env",
                       dest="env_conf",
                       action="store_true",
                       help="Get config from env vars.")

    parser.add_argument("-f", "--file",
                        help="file to index",
                        action="append",
                        type=str,
                        required=False)

    parser.add_argument("-l", "--list",
                        help="pattern of files (*.csv)",
                        action="store",
                        type=str,
                        required=False)

    parser.add_argument("-i", "--index",
                        help="index name (stock-BTCUSD-202105)",
                        action="store",
                        type=str,
                        required=True)

    args = parser.parse_args()

    # Leemos fichero de configuración
    config = {}

    if args.config:
        with open(args.config) as json_config_file:
            config = json.load(json_config_file)
    else:
        config = config_from_env()

    files = []
    if args.file:
        files.extend(args.file)

    if args.list:
        files.extend(glob.glob(args.list))

    index_name = args.index.lower()
    for f in files:
        index_file(config, fname=f, index_name=index_name)
