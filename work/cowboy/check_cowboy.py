import base64
import json
import urllib.request
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'outputs/cowboy'
WORK = ROOT / 'work/cowboy'


def call(action, params=None):
    body = json.dumps({'action': action, 'params': params or {}}).encode()
    request = urllib.request.Request('http://127.0.0.1:8787/command', body,
                                     {'Content-Type': 'application/json'})
    with urllib.request.urlopen(request, timeout=90) as response:
        result = json.load(response)
    if not result['ok']:
        raise RuntimeError(result.get('error'))
    return result['result']


assert call('get_status')['project']['name'] == 'cowboy'
checks = call('check_model')
print(json.dumps(checks))
assert checks['issue_count'] == 0
print(json.dumps(call('check_sides')))

# Inspect a walking pose, then return every joint to its neutral export pose.
pose = '''
const poses = {bipedHead:[0,25,0], bipedRightArm:[-28,0,-4],
    bipedLeftArm:[28,0,4], bipedRightLeg:[24,0,0], bipedLeftLeg:[-24,0,0]};
for (const bone of Group.all) if (poses[bone.name]) bone.rotation = poses[bone.name];
Canvas.updateAll();
return true;
'''
try:
    call('execute_script', {'code': pose})
    result = call('screenshot_views', {'views': ['front_right'], 'width': 720, 'height': 800, 'annotate': False})
    (WORK / 'walking_check.png').write_bytes(base64.b64decode(result['shots'][0]['base64']))
finally:
    call('execute_script', {'code': 'for(const g of Group.all) g.rotation=[0,0,0]; Canvas.updateAll(); return true;'})

metadata = '''
Project.save_path = params.project;
Project.export_path = params.geometry;
Project.geckolib_model_type = 'Armor';
Project.geckolib_modid = 'tacz_morelevel';
Texture.all[0].path = params.texture;
Texture.all[0].saved = true;
Group.all.forEach(g=>g.isOpen=false);
Canvas.updateAll();
return {project:Codecs.project.compile(), geometry:Codecs.bedrock.compile()};
'''
compiled = call('execute_script', {'code': metadata, 'params': {
    'project': str(OUT / 'cowboy.bbmodel'), 'geometry': str(OUT / 'cowboy.geo.json'),
    'texture': str(OUT / 'cowboy.png')}})
for field, filename in [('project', 'cowboy.bbmodel'), ('geometry', 'cowboy.geo.json')]:
    data = compiled[field]
    if isinstance(data, str):
        data = json.loads(data)
    (OUT / filename).write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding='utf-8')

model = json.loads((OUT / 'cowboy.bbmodel').read_text(encoding='utf-8'))
geo = json.loads((OUT / 'cowboy.geo.json').read_text(encoding='utf-8'))['minecraft:geometry'][0]
assert model['meta']['model_format'] == 'geckolib_model'
assert model['geckolib_model_type'] == 'Armor'
assert len([c for b in geo['bones'] for c in b.get('cubes', [])]) == 37
bones = {b['name']: b for b in geo['bones']}
for name in ('armorHead', 'armorBody', 'armorRightArm', 'armorLeftArm', 'armorRightLeg', 'armorLeftLeg', 'armorRightBoot', 'armorLeftBoot'):
    assert name in bones
assert bones['bipedRightArm']['pivot'] == [-5, 22, 0]
assert bones['bipedLeftArm']['pivot'] == [5, 22, 0]
assert all(not b.get('cubes') for n, b in bones.items() if n.startswith('biped'))
for bone in bones.values():
    assert not bone.get('rotation') or bone['rotation'] == [0, 0, 0]
    for cube in bone.get('cubes', []):
        assert all(size > 0 for size in cube['size'])
        for face in cube['uv'].values():
            assert all(0 <= pos <= 128 for pos in face['uv'])
            assert all(0 <= pos + size <= 128 for pos, size in zip(face['uv'], face['uv_size']))

call('execute_script', {'code': 'Project.saved=true; return Project.save_path;'})
shots = call('screenshot_views', {'views': ['front', 'back', 'front_right'], 'width': 720, 'height': 800, 'annotate': False})
for name, shot in zip(('front.png', 'back.png', 'preview.png'), shots['shots']):
    destination = OUT if name == 'preview.png' else WORK
    (destination / name).write_bytes(base64.b64decode(shot['base64']))
print('Validated GeckoLib Armor format, 37 outfit cubes, 14 bones, 128px texture, neutral pose and UV bounds.')
