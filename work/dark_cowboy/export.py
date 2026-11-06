import base64
import json
from pathlib import Path

from inspect import call

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(r'E:\mcsucai\mc\codex\dark_cowboy') / 'current'
WORK = ROOT / 'work/dark_cowboy'
PREVIEW = Path(r'E:\mcsucai\mc\codex\dark_cowboy') / 'development/previews'
PREVIEW.mkdir(parents=True, exist_ok=True)
render = (WORK/'render.js').read_text(encoding='utf-8')

status = call('get_status')
assert status['project']['name'] == 'dark_cowboy'
call('execute_script',{'code':'for(const c of Cube.all) if(!c.export) c.visibility=true; Canvas.updateAll(); return true;'})
checks = call('check_model')
sides = call('check_sides')
print(json.dumps(checks))
print(json.dumps(sides))
assert checks['issue_count'] == 0
assert sides['issue_count'] == 0

pose = '''
const pose = {bipedHead:[0,30,0],bipedRightArm:[-32,0,-4],
    bipedLeftArm:[32,0,4],bipedRightLeg:[26,0,0],bipedLeftLeg:[-26,0,0]};
for(const g of Group.all) if(pose[g.name]) g.rotation=pose[g.name];
Canvas.updateAll(); return true;
'''
try:
    call('execute_script',{'code':pose})
    data = call('execute_script',{'code':render,'params':{'views':[[65,32,-100],[100,20,0],[-65,32,100]]}})
    (PREVIEW/'pose_check.png').write_bytes(base64.b64decode(data.split(',',1)[1]))
finally:
    call('execute_script',{'code':'for(const g of Group.all) g.rotation=[0,0,0]; Canvas.updateAll(); return true;'})

metadata = '''
Project.save_path=params.project;
Project.export_path=params.geometry;
Project.geckolib_model_type='Armor';
Project.geckolib_modid='tacz_morelevel';
Texture.all[0].path=params.texture;
Texture.all[0].saved=true;
for(const g of Group.all) g.isOpen=false;
Canvas.updateAll();
return {project:Codecs.project.compile(),geometry:Codecs.bedrock.compile()};
'''
compiled = call('execute_script',{'code':metadata,'params':{
    'project':str(OUT/'dark_cowboy.bbmodel'),'geometry':str(OUT/'dark_cowboy.geo.json'),
    'texture':str(OUT/'dark_cowboy.png')}})
for field,filename in [('project','dark_cowboy.bbmodel'),('geometry','dark_cowboy.geo.json')]:
    data=compiled[field]
    if isinstance(data,str): data=json.loads(data)
    (OUT/filename).write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')

model=json.loads((OUT/'dark_cowboy.bbmodel').read_text(encoding='utf-8'))
geo=json.loads((OUT/'dark_cowboy.geo.json').read_text(encoding='utf-8'))['minecraft:geometry'][0]
assert model['meta']['model_format']=='geckolib_model'
assert model['geckolib_model_type']=='Armor'
assert len([c for b in geo['bones'] for c in b.get('cubes',[])])==109
bones={b['name']:b for b in geo['bones']}
assert len(bones)==14
for part in ['Head','Body','RightArm','LeftArm','RightLeg','LeftLeg','RightBoot','LeftBoot']:
    assert 'armor'+part in bones
assert bones['bipedRightArm']['pivot']==[-5,22,0]
assert bones['bipedLeftArm']['pivot']==[5,22,0]
assert all(not b.get('cubes') for n,b in bones.items() if n.startswith('biped'))
for bone in bones.values():
    assert not bone.get('rotation') or bone['rotation']==[0,0,0]
    for cube in bone.get('cubes',[]):
        assert all(size>0 for size in cube['size'])
        for face in cube['uv'].values():
            assert all(0<=x<=256 for x in face['uv'])
            assert all(0<=x+size<=256 for x,size in zip(face['uv'],face['uv_size']))

call('execute_script',{'code':'for(const c of Cube.all) if(!c.export) c.visibility=true; Canvas.updateAll(); return true;'})
rendered=call('execute_script',{'code':render,'params':{}})
(OUT/'preview.png').write_bytes(base64.b64decode(rendered.split(',',1)[1]))
call('execute_script',{'code':'Project.saved=true; return Project.save_path;'})
call('set_camera_angle',{'position':[65,38,-100],'target':[0,17.5,0]})
print('Saved GeckoLib Armor, 109 outfit cubes, 14 bones and 256x256 texture. Preview mannequin excluded from geometry export.')
