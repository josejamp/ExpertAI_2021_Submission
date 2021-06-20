import json
import sys
from elasticsearch import Elasticsearch
from elasticsearch.helpers import bulk
import os


def get_symbol(lemmas, crypto_dict):
    cryptos = []
    for lemma in lemmas:
        if lemma["value"].lower() in crypto_dict:
            cryptos.append(crypto_dict[lemma["value"].lower()])
    return cryptos


def process_json(json_data, index_name, crypto_dict):
    data = json_data
    data['_type'] = "_doc"
    data['_index'] = index_name
    data['symbol'] = get_symbol(data["expertai_info"]["data"]["mainLemmas"], crypto_dict)
    # Trated as numbers
    # data["scrapped_object"]["score"] = int(data["scrapped_object"]["score"])
    return data


# Leemos fichero de configuración
with open(sys.argv[1]) as json_config_file:
    config = json.load(json_config_file)


# Índice en el que se va a cargar la info
index_name = "stock_crypto_sense_ai"

#Cryptos to treat
cryptos_dict = {"bitcoin": "BTC", "btc": "BTC", "ada": "ADA", "cardano": "ADA", "dogecoin": "DOGE", "doge": "DOGE", "ethereum": "ETH", "eth": "ETH","ripple": "XPR","xpr": "XPR"}

# Recuperamos los datos a cargar en Elastic
files = os.listdir(config["data_path"])
final_data = []
for file in files:
    with open(os.path.join(config["data_path"], file)) as data_file:
        final_data.append(process_json(json.load(data_file), index_name, cryptos_dict))


#Cargamos en Elastic
es = Elasticsearch(
    ['http://'+config["elastic"]["user"]+':'+config["elastic"]["pass"]+'@'+config["elastic"]["host"]+':'+str(config["elastic"]["port"])+'/']
)
bulk(es, final_data)