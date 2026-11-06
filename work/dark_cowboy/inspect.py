import base64
import json
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
WORK = ROOT / 'work/dark_cowboy'
PREVIEW = Path(r'E:\mcsucai\mc\codex\dark_cowboy') / 'development/previews'
PREVIEW.mkdir(parents=True, exist_ok=True)


def call(action, params=None):
    request = urllib.request.Request('http://127.0.0.1:8787/command',
        json.dumps({'action':action,'params':params or {}}).encode(),
        {'Content-Type':'application/json'})
    with urllib.request.urlopen(request,timeout=90) as response:
        result=json.load(response)
    if not result['ok']:
        raise RuntimeError(result.get('error',result))
    return result['result']


if __name__ == '__main__':
    print(json.dumps(call('check_model')))
    data=call('execute_script',{'code':(WORK/'render.js').read_text(encoding='utf-8'),'params':{}})
    (PREVIEW/'pass2.png').write_bytes(base64.b64decode(data.split(',',1)[1]))
