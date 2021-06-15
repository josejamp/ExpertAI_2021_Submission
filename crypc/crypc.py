import argparse
import logging
import os
import time
from datetime import datetime
from datetime import timedelta

import numpy as np
import pandas as pd
import requests

from sanitize import sanitize
import indexer

logging.basicConfig(format='%(asctime)s %(message)s', level=logging.INFO)
logger = logging.getLogger(__name__)

API_BITFINEX_URL = 'https://api-pub.bitfinex.com'
SYMBOL = 'symbol'
BIN_SIZE = '1m'
pairs = ['BTCUSD', 'DOGE:USD', 'ADAUSD', 'ETHUSD', 'XRPUSD']

DATA_PATH = './data'


def mkdelta(deltavalue):
    _units = dict(d=60 * 60 * 24, h=60 * 60, m=60, s=1)
    seconds = 0
    defaultunit = unit = _units['d']  # default to days
    value = ''
    for ch in list(str(deltavalue).strip()):
        if ch.isdigit():
            value += ch
            continue
        if ch in _units:
            unit = _units[ch]
            if value:
                seconds += unit * int(value)
                value = ''
                unit = defaultunit
            continue
        if ch in ' \t':
            # skip whitespace
            continue
        raise ValueError('Invalid time delta: %s' % deltavalue)
    if value:
        seconds = unit * int(value)
    return timedelta(seconds=seconds)


def format_ts(ts):
    return datetime.fromtimestamp(ts / 1000).strftime('%Y-%m-%d %H:%M:%S')


def candles(symbol='btcusd', interval='1m', limit=1000, start=None, end=None, sort=-1, api_url=API_BITFINEX_URL):
    ep = '{}/v2/candles/trade:{}:t{}/hist?limit={}&start={}&end={}&sort=-1' \
        .format(api_url, interval, symbol.upper(), limit, start, end, sort)
    return requests.get(ep).json()


# Create a function to fetch the data
def fetch_candles(start=1364767200000, stop=1545346740000, symbol='btcusd',
                  interval='1m',
                  tick_limit=1000,
                  step=60000000):
    data = []
    t_from = start
    i = 0
    while t_from < stop:
        i += 1
        t_end = start + (step * i)
        t_end = stop if t_end > stop else t_end
        t_from = start + step * (i - 1)
        if t_from >= stop:
            break
        res = candles(symbol=symbol, interval=interval, limit=tick_limit, start=t_from, end=t_end)
        data.extend(res)
        logger.info('Retrieving data size={} from {} to {} for {}'.format(
            len(res),
            format_ts(t_from),
            format_ts(t_end),
            symbol))
        time.sleep(1.5)
    return data


def to_ms(year: int, month: int, day: int, hour: int = 0, minute: int = 0, seconds: int = 0):
    t = datetime(year, month, day, hour, minute, seconds)
    return time.mktime(t.timetuple()) * 1000


def save_candels(df, dir_path, symbol, ts_start, ts_stop):
    symbol_san = sanitize(symbol)
    s_start = datetime.fromtimestamp(ts_start / 1000).strftime('%Y%m%d%H%M%S')
    s_stop = datetime.fromtimestamp(ts_stop / 1000).strftime('%Y%m%d%H%M%S')
    fname = 'stock-{}-{}-{}.csv'.format(symbol_san, s_start, s_stop)
    fname_path = os.path.join(dir_path, fname)
    # Add symbol column
    df[SYMBOL] = symbol_san.upper()
    df.to_csv(os.path.join(fname_path))
    return fname_path


def index_candels(es, df, index_name, symbol):
    symbol_san = sanitize(symbol)
    df[SYMBOL] = symbol_san.upper()
    return indexer.index_df(es, df, index_name=index_name, index_create=False)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--datadir",
                       dest="datadir",
                       help="output data dir",
                       action="store",
                       type=str)

    group.add_argument("--index",
                       dest="index",
                       help="elastic index name. require ENV config",
                       action="store")

    parser.add_argument("-s", "--start-date",
                        dest="start_date",
                        default=datetime.now(),
                        type=lambda d: datetime.strptime(d, '%Y%m%d%H%M%S'),
                        help="Date in the format yyyymmddhhMMss",
                        required=False)

    parser.add_argument("-e", "--end-date",
                        dest="end_date",
                        type=lambda d: datetime.strptime(d, '%Y%m%d%H%M%S'),
                        help="Date in the format yyyymmddhhMMss",
                        required=False)

    parser.add_argument("--minus-delta",
                        dest="minus_delta",
                        type=str,
                        help="minus delta",
                        required=False)

    parser.add_argument("--inc-delta",
                        dest="inc_delta",
                        type=str,
                        help="increment delta (dhms)",
                        required=False)

    parser.add_argument("--limit",
                        dest="limit",
                        type=int,
                        default=1000,
                        required=False)

    args = parser.parse_args()
    t_start = args.start_date
    t_end = t_start
    minus_delta = 0

    if args.end_date:
        t_end = args.end_date
    elif args.minus_delta:
        minus_delta = mkdelta(args.minus_delta)
        t_start = t_start - minus_delta
    elif args.inc_delta:
        inc_delta = mkdelta(args.inc_delta)
        t_end = t_start + inc_delta

    logger.info(f"start_date={t_start} end_date={t_end} minus_delta={minus_delta}")

    # Define query parameters
    # Num. max. candles requested (1000)
    limit = args.limit
    time_step = 1000 * 60 * limit

    t_start_ms = t_start.timestamp() * 1000
    t_stop_ms = t_end.timestamp() * 1000

    save_path = args.datadir

    if args.datadir and os.path.exists(save_path) is False:
        logger.info(f"creating dir={save_path}")
        os.mkdir(save_path)

    for pair in pairs:
        pair_data = fetch_candles(start=t_start_ms,
                                  stop=t_stop_ms,
                                  symbol=pair,
                                  interval=BIN_SIZE,
                                  tick_limit=limit,
                                  step=time_step)

        # Remove error messages
        ind = [np.ndim(x) != 0 for x in pair_data]
        pair_data = [i for (i, v) in zip(pair_data, ind) if v]

        # Create pandas data frame and clean data
        names = ['date', 'open', 'close', 'high', 'low', 'volume']
        df = pd.DataFrame(pair_data, columns=names)
        df.drop_duplicates(inplace=True)
        df.set_index('date', inplace=True)
        df.sort_index(inplace=True)
        fname = save_candels(df, save_path, symbol=pair, ts_start=t_start_ms, ts_stop=t_stop_ms)
        logger.info(f'Saved: {len(df.index)} rows in {fname}')

    logger.info('Done retrieving data')
