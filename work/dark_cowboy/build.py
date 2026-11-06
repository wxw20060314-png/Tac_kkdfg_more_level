import base64
import io
import json
import math
import random
import urllib.request
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(r'E:\mcsucai\mc\codex\dark_cowboy') / 'current'
WORK = ROOT / 'work/dark_cowboy'
OUT.mkdir(parents=True, exist_ok=True)
WORK.mkdir(parents=True, exist_ok=True)


def call(action, params=None):
    request = urllib.request.Request('http://127.0.0.1:8787/command',
        json.dumps({'action': action, 'params': params or {}}).encode(),
        {'Content-Type': 'application/json'})
    with urllib.request.urlopen(request, timeout=90) as response:
        result = json.load(response)
    if not result['ok']:
        raise RuntimeError(result.get('error', result))
    return result['result']


expand = {
    'charcoal_shirt': .40,
    'jacket_front_right': .20,
    'jacket_front_left': .20,
    'jacket_back': .20,
    'jacket_side_right': .20,
    'jacket_side_left': .20,
    'scarf_wrap': .30,
    'plain_belt': .32,
}
for side in ('right', 'left'):
    expand[f'jacket_sleeve_{side}'] = .40
    expand[f'jacket_cuff_{side}'] = .36
    expand[f'dark_denim_{side}'] = .40
    expand[f'boot_shaft_{side}'] = .40

def adjusted(name, start, end):
    start, end = list(start), list(end)
    if name == 'hat_crown':
        start[0] -= .55; end[0] += .55
        start[2] -= .55; end[2] += .55
    elif name in ('hatband_front', 'glasses_bridge') or name.startswith('sunglasses_'):
        start[2] -= .55; end[2] -= .55
    elif name == 'hatband_back':
        start[2] += .55; end[2] += .55
    elif name in ('hatband_right', 'glasses_arm_right'):
        start[0] += .55; end[0] += .55
    elif name in ('hatband_left', 'glasses_arm_left'):
        start[0] -= .55; end[0] -= .55
    if name in expand:
        amount = expand[name]
        for axis in (0, 2):
            start[axis] -= amount
            end[axis] += amount
    return start, end


groups, cubes = [], []


def group(name, origin, parent=None):
    groups.append(dict(name=name, origin=origin, parent=parent))


def cube(name, parent, start, end, material, rotation=None, pivot=None, reference=False, pattern=''):
    start, end = adjusted(name, start, end)
    cubes.append({'name': name, 'parent': parent, 'from': start, 'to': end,
        'material': material, 'rotation': rotation or [0, 0, 0],
        'origin': pivot or start, 'reference': reference, 'pattern': pattern})


def centered(name, parent, center, size, material, rotation=None, pivot=None, pattern=''):
    cube(name, parent, [c-s/2 for c, s in zip(center, size)],
         [c+s/2 for c, s in zip(center, size)], material, rotation, pivot or center, pattern=pattern)


for part, pivot in [('Head', [0, 24, 0]), ('Body', [0, 24, 0]),
                    ('RightArm', [5, 22, 0]), ('LeftArm', [-5, 22, 0]),
                    ('RightLeg', [2, 12, 0]), ('LeftLeg', [-2, 12, 0])]:
    group('biped' + part, pivot)
    group('armor' + part, pivot, 'biped' + part)
    if part.endswith('Leg'):
        group('armor' + part.replace('Leg', 'Boot'), pivot, 'biped' + part)

# The hat uses broad planar pieces; its rolled brim is part of the silhouette.
cube('hat_brim', 'armorHead', [-5.0, 30.85, -5.35], [5.0, 31.3, 5.0], 'hat')
cube('hat_brim_right', 'armorHead', [4.9, 30.9, -5.1], [6.6, 31.37, 4.7], 'hat', [0, 0, 24], [4.9, 31.1, 0])
cube('hat_brim_left', 'armorHead', [-6.6, 30.9, -5.1], [-4.9, 31.37, 4.7], 'hat', [0, 0, -24], [-4.9, 31.1, 0])
cube('hat_crown', 'armorHead', [-4.18, 31.1, -4.08], [4.18, 34.65, 4.1], 'hat')
cube('hat_crown_right', 'armorHead', [1.4, 34.5, -3.65], [3.8, 35.22, 3.4], 'hat', [0, 0, -11], [1.4, 34.5, 0])
cube('hat_crown_left', 'armorHead', [-3.8, 34.5, -3.65], [-1.4, 35.22, 3.4], 'hat', [0, 0, 11], [-1.4, 34.5, 0])
cube('hatband_front', 'armorHead', [-4.22, 31.35, -4.26], [4.22, 32.02, -4.0], 'strap')
cube('hatband_back', 'armorHead', [-4.22, 31.35, 3.96], [4.22, 32.02, 4.18], 'strap')
cube('hatband_right', 'armorHead', [4.08, 31.37, -4.05], [4.3, 32, 4.05], 'strap')
cube('hatband_left', 'armorHead', [-4.3, 31.37, -4.05], [-4.08, 32, 4.05], 'strap')
for side, x in [('right', 2.1), ('left', -2.1)]:
    centered('sunglasses_' + side, 'armorHead', [x, 29.7, -4.16], [3.2, 1.35, .32], 'glass')
    centered('glasses_arm_' + side, 'armorHead', [4.08 if x > 0 else -4.08, 30.0, -2.8], [.22, .35, 2.7], 'glass')
centered('glasses_bridge', 'armorHead', [0, 30, -4.28], [1.3, .32, .25], 'glass')

cube('charcoal_shirt', 'armorBody', [-4.1, 12.1, -2.1], [4.1, 24.05, 2.1], 'shirt', pattern='shirt')
cube('jacket_front_right', 'armorBody', [.85, 14.35, -2.58], [4.2, 23.65, -2.06], 'jacket', pattern='jacket_front')
cube('jacket_front_left', 'armorBody', [-4.2, 14.35, -2.58], [-.85, 23.65, -2.06], 'jacket', pattern='jacket_front')
cube('jacket_back', 'armorBody', [-4.2, 14.35, 2.03], [4.2, 23.65, 2.48], 'jacket', pattern='jacket_back')
cube('jacket_side_right', 'armorBody', [4.07, 14.45, -2.17], [4.37, 21.5, 2.17], 'jacket')
cube('jacket_side_left', 'armorBody', [-4.37, 14.45, -2.17], [-4.07, 21.5, 2.17], 'jacket')
for sign, side in [(1, 'right'), (-1, 'left')]:
    centered('upper_lapel_' + side, 'armorBody', [sign * 2.05, 22.7, -2.75], [1.3, 2.25, .35], 'lapel', [0, 0, sign * 20])
    centered('lower_lapel_' + side, 'armorBody', [sign * 1.5, 20.65, -2.77], [1.15, 2.05, .33], 'lapel', [0, 0, sign * -31])
    for i, (height, width) in enumerate([(.7, 1.05), (1.12, 1.1), (.5, 1.15)]):
        x = sign * (1.4 + i * 1.05)
        centered('ragged_front_' + side + str(i), 'armorBody', [x, 14.48-height/2, -2.4], [width, height, .48], 'jacket', pattern='hem')
for i, height in enumerate([.6, 1.0, .55, .85]):
    centered('ragged_back_' + str(i), 'armorBody', [-3.15+i*2.1, 14.48-height/2, 2.25], [2.02, height, .4], 'jacket', pattern='hem')

# The scarf has a wrap, raised knot, two overlapping tails and two folds.
cube('scarf_wrap', 'armorBody', [-3.1, 23.2, -2.38], [3.1, 24.0, 2.29], 'scarf')
centered('scarf_front_fold', 'armorBody', [0, 23.55, -2.53], [5.2, .42, .35], 'scarf_light', [0, 0, -4])
centered('scarf_back_fold', 'armorBody', [0, 23.55, 2.37], [5.2, .4, .28], 'scarf')
centered('scarf_tail', 'armorBody', [0, 22.25, -2.86], [2.25, 2.25, .36], 'scarf', [0, 0, 45])
centered('scarf_overlap', 'armorBody', [.18, 22.43, -3.08], [1.65, 1.65, .22], 'scarf_light', [0, 0, 32])
centered('scarf_knot', 'armorBody', [0, 23.55, -3.11], [.95, .86, .55], 'scarf_light')
centered('scarf_crease_left', 'armorBody', [-.54, 22.8, -3.22], [.25, 1.35, .18], 'scarf', [0, 0, -21])
centered('scarf_crease_right', 'armorBody', [.59, 22.65, -3.23], [.23, 1.25, .17], 'scarf_light', [0, 0, 24])
cube('plain_belt', 'armorBody', [-4.23, 12.22, -2.28], [4.23, 13.12, 2.28], 'belt')
centered('belt_buckle', 'armorBody', [0, 12.67, -2.46], [1.55, 1.12, .35], 'iron', pattern='buckle')

# Both loaded sides are attached to the torso, leaving the shoulders free to move.
for surface, z in [('front', -2.97), ('back', 2.83)]:
    centered('bandolier_' + surface, 'armorBody', [-.05, 18.25, z], [1.15, 14.45, .28], 'strap', [0, 0, -33], pattern='stitch')
    for i in range(8):
        t = .16 + i * .095
        x, y = 3.88 - 7.86*t, 24.3 - 12.12*t
        depth = -3.38 if surface == 'front' else 3.26
        pivot = [x, y, depth]
        centered('shell_' + surface + '_' + str(i), 'armorBody', pivot, [.59, 1.45, .6], 'shell', [0, 0, 57], pivot)
        center = [x, y-.67, depth]
        centered('shell_brass_' + surface + '_' + str(i), 'armorBody', center, [.65, .33, .66], 'brass', [0, 0, 57], pivot)
centered('bandolier_shoulder', 'armorBody', [3.7, 24.15, -.05], [1.13, .34, 5.8], 'strap', [0, 0, -17], pattern='stitch')
centered('bandolier_hip', 'armorBody', [-4.3, 12.55, -.05], [.33, 1.04, 5.8], 'strap', [0, 0, -12], pattern='stitch')

for side, sign in [('Right', 1), ('Left', -1)]:
    arm, leg, boot = 'armor'+side+'Arm', 'armor'+side+'Leg', 'armor'+side+'Boot'

    def paired(name, parent, start, end, material, pattern='', reference=False):
        if sign == -1:
            start, end = [-end[0], start[1], start[2]], [-start[0], end[1], end[2]]
        cube(name+'_'+side.lower(), parent, start, end, material, reference=reference, pattern=pattern)

    paired('jacket_sleeve', arm, [4.03, 13.95, -2.13], [8.13, 24.06, 2.13], 'jacket', 'sleeve_tear' if side == 'Right' else 'sleeve')
    paired('jacket_cuff', arm, [3.95, 13.75, -2.23], [8.23, 14.85, 2.23], 'jacket', 'cuff')
    paired('dark_denim', leg, [.05, .12, -2.1], [4.1, 12.12, 2.1], 'denim', 'jeans_'+side.lower())
    paired('boot_shaft', boot, [.01, .04, -2.25], [4.26, 5.7, 2.26], 'boot', 'boot')
    paired('boot_toe', boot, [.12, .18, -3.0], [4.14, 1.95, -2.09], 'boot', 'toe')
    paired('boot_heel', boot, [.17, -.23, .6], [3.98, .31, 2.19], 'sole')
    paired('spur_band_outer', boot, [4.2, 1.03, -.35], [4.47, 1.53, 2.4], 'iron')
    paired('spur_band_inner', boot, [.15, 1.03, -.35], [.4, 1.53, 2.4], 'iron')
    paired('spur_band_back', boot, [-.04, 1.05, 2.19], [4.35, 1.51, 2.48], 'iron')
    paired('spur_shank', boot, [1.94, 1.15, 2.38], [2.34, 1.48, 3.43], 'iron')
    center = [sign*2.14, 1.33, 3.67]
    for j, angle in enumerate([0, 60, 120]):
        centered('spur_rowel_'+side.lower()+'_'+str(j), boot, center, [.24, 1.35, .35], 'iron', [angle, 0, 0])
    centered('spur_hub_'+side.lower(), boot, center, [.44, .5, .5], 'brass_dark')
    paired('preview_hand', 'biped'+side+'Arm', [4.01, 12, -2.01], [8.01, 14.0, 2.01], 'skin', reference=True)
cube('preview_head', 'bipedHead', [-4, 24, -4], [4, 32, 4], 'skin', reference=True, pattern='head')
for item in cubes:
    if item['name'] == 'spur_band_inner_right':
        item['name'] = 'spur_instep_a'
    elif item['name'] == 'spur_band_inner_left':
        item['name'] = 'spur_instep_b'

palette = {'hat': (83,65,49), 'jacket': (77,69,61), 'lapel': (91,77,64),
    'shirt': (103,99,88), 'strap': (107,76,51), 'belt': (72,55,42),
    'scarf': (134,46,41), 'scarf_light': (155,60,49), 'denim': (77,74,70),
    'boot': (88,63,44), 'sole': (53,46,38), 'iron': (143,134,115),
    'brass': (172,130,67), 'brass_dark': (132,100,55), 'shell': (141,49,41),
    'glass': (22,25,26), 'skin': (181,139,103)}

# Shared face tiles keep repeated cartridges and mirrored cloth within one atlas.
tiles = {}
for item in cubes:
    x, y, z = [b-a for a, b in zip(item['from'], item['to'])]
    item['face_keys'] = {}
    for face, (w, h) in {'north': (x,y), 'south': (x,y), 'east': (z,y),
                         'west': (z,y), 'up': (x,z), 'down': (x,z)}.items():
        density = 4 if item['material'] in ('shell', 'brass') else 2
        width, height = max(1, math.ceil(w*density)), max(1, math.ceil(h*density))
        key = (item['material'], item['pattern'], face, width, height)
        tiles.setdefault(key, {'width': width, 'height': height})
        item['face_keys'][face] = key

size = 128
while True:
    px = py = row_height = 0
    for key, tile in sorted(tiles.items(), key=lambda kv: (-kv[1]['height'], -kv[1]['width'])):
        w, h = tile['width'], tile['height']
        if px+w+2 > size:
            px, py, row_height = 0, py+row_height, 0
        tile['x'], tile['y'] = px+1, py+1
        px += w+2
        row_height = max(row_height, h+2)
    if py+row_height <= size:
        break
    size *= 2
    assert size <= 256

texture = Image.new('RGBA', (size,size), (0,0,0,0))
draw = ImageDraw.Draw(texture)
rng = random.Random(71)
for (material, pattern, face, width, height), tile in tiles.items():
    x0, y0 = tile['x'], tile['y']
    base = palette[material]

    def shade(delta, tint=None):
        color = tint or base
        return tuple(max(0,min(255,v+delta)) for v in color)+(255,)

    face_light = {'up': 10, 'down': -10, 'north': 7, 'south': 1, 'east': 1, 'west': -2}[face]
    dirty = material in ('hat','jacket','lapel','denim','boot','strap')
    for y in range(height):
        for x in range(width):
            grain = rng.choice([-3,-1,0,0,1,3])
            block = ((x//3*17+y//3*13)%11-5)*.65
            delta = face_light + grain + block + 3*(1-y/max(height-1,1))
            if width>3 and height>3 and (x in (0,width-1) or y==height-1):
                delta -= 8
            if dirty and ((x*11+y*7)%29 < 3) and (y>height*.7 or x<2 or x>width-3):
                color = shade(4,(73,55,38))
            else:
                color = shade(round(delta))
            texture.putpixel((x0+x,y0+y),color)
    if dirty and width>=5 and height>=5:
        # Sparse connected scuffs, broader along lower hems and boot toes.
        for i in range(max(1,width*height//95)):
            xx, yy = rng.randrange(width-2), rng.randrange(1,height-1)
            length = min(rng.randint(2,4),width-xx)
            draw.line((x0+xx,y0+yy,x0+xx+length-1,y0+yy), fill=(74,59,43,255))
    if pattern in ('stitch','hem','cuff','jacket_front','jacket_back') and width>3 and height>3:
        for x in range(1,width-1,3):
            draw.point((x0+x,y0+height-2), fill=(96,76,53,255))
    if pattern=='shirt' and face=='north':
        cx=x0+width//2
        draw.line((cx,y0,cx,y0+height-1), fill=shade(-18))
        for y in range(5,height-2,5):
            draw.rectangle((cx-1,y0+y,cx,y0+y+1),fill=(30,29,26,255))
    if pattern=='jacket_front' and face=='north':
        draw.line((x0+1,y0+height-6,x0+width-2,y0+height-6), fill=(22,20,18,255))
    if pattern=='jacket_back' and face=='south':
        draw.line((x0+1,y0+3,x0+width-2,y0+3),fill=shade(15))
    tear = (pattern=='sleeve_tear' and face in ('north','east')) or (pattern.startswith('jeans') and face=='north')
    if tear and width>=5 and height>=9:
        yy = 5 if pattern=='sleeve_tear' else (height//3 if pattern.endswith('right') else height//2)
        for x in range(1,width-1):
            y = yy - (x-1)//3
            draw.point((x0+x,y0+y-1),fill=(85,68,49,255))
            draw.point((x0+x,y0+y),fill=(20,18,17,255))
            draw.point((x0+x,y0+y+1),fill=(26,23,21,255))
            if x%3==0: draw.point((x0+x,y0+y+2),fill=(73,57,40,255))
    if pattern in ('boot','toe') and face not in ('up','down'):
        for y in range(max(0,height-3),height):
            for x in range(width):
                if rng.random()<.65:
                    texture.putpixel((x0+x,y0+y), (67+rng.randrange(13),51+rng.randrange(8),35+rng.randrange(7),255))
    if material=='shell':
        if width>=2 and height>=3:
            draw.line((x0,y0+1,x0,y0+height-2),fill=shade(21))
        if face not in ('up','down'):
            draw.rectangle((x0,y0+height//2,x0+width-1,y0+height//2+1),fill=(59,43,30,255))
    if material=='glass':
        draw.line((x0,y0,x0+width-1,y0),fill=(33,36,36,255))
    if pattern=='buckle' and face=='north' and width>2 and height>2:
        draw.rectangle((x0+1,y0+1,x0+width-2,y0+height-2),fill=(27,24,21,255))
    if pattern=='head':
        if face=='north':
            draw.rectangle((x0,y0,x0+width-1,y0+2),fill=(43,30,22,255))
            draw.line((x0+6,y0+12,x0+9,y0+12),fill=(111,76,52,255))
        elif face in ('south','up'):
            draw.rectangle((x0,y0,x0+width-1,y0+height-1),fill=(43,30,22,255))
        elif face in ('east','west'):
            draw.rectangle((x0,y0,x0+width-1,y0+4),fill=(43,30,22,255))
    for y in range(height):
        texture.putpixel((x0-1,y0+y),texture.getpixel((x0,y0+y)))
        texture.putpixel((x0+width,y0+y),texture.getpixel((x0+width-1,y0+y)))
    for x in range(-1,width+1):
        texture.putpixel((x0+x,y0-1),texture.getpixel((x0+x,y0)))
        texture.putpixel((x0+x,y0+height),texture.getpixel((x0+x,y0+height-1)))

for item in cubes:
    item['faces'] = {}
    for face,key in item.pop('face_keys').items():
        t=tiles[key]
        item['faces'][face]=[t['x'],t['y'],t['x']+t['width'],t['y']+t['height']]

texture.save(OUT/'dark_cowboy.png')
buffer=io.BytesIO()
texture.save(buffer,format='PNG')
texture_url='data:image/png;base64,'+base64.b64encode(buffer.getvalue()).decode()
status=call('get_status')
if status.get('has_project') and status['project']['name'] != 'dark_cowboy':
    raise RuntimeError('Inspect the open project before creating a new one.')
if not status.get('has_project'):
    call('new_project', {'format':'geckolib_model','name':'dark_cowboy','geometry_name':'dark_cowboy',
                      'texture_width':size,'texture_height':size})
script='''
Project.geckolib_model_type='Armor';
Project.geckolib_modid='tacz_morelevel';
Project.box_uv=false;
Undo.initEdit({elements:Cube.all.slice(),textures:Texture.all.slice(),outliner:true});
const groups={};
for (const s of params.groups) {
  let g=Group.all.find(g=>g.name===s.name);
  if(!g) {
    g=new Group({name:s.name,origin:s.origin});
    if(s.parent) g.addTo(groups[s.parent]);
    g.init();
  }
  groups[s.name]=g;
}
let texture=Texture.all.find(t=>t.name==='dark_cowboy.png');
if(texture) texture.fromDataURL(params.texture);
else texture=new Texture({name:'dark_cowboy.png'}).fromDataURL(params.texture).add(false);
for(const s of params.cubes) {
  let c=Cube.all.find(c=>c.name===s.name);
  const data={name:s.name,from:s.from,to:s.to,origin:s.origin,rotation:s.rotation,
    box_uv:false,autouv:0,export:!s.reference,locked:s.reference};
  if(c) c.extend(data);
  else { c=new Cube(data); c.addTo(groups[s.parent]).init(); }
  for(const [face,uv] of Object.entries(s.faces)) {
    c.faces[face].uv=uv; c.faces[face].texture=texture.uuid;
  }
}
Undo.finishEdit('Build dark western outfit with loaded bandolier and spurs');
Canvas.updateAll();
return {outfit:Cube.all.filter(c=>c.export).length,reference:Cube.all.filter(c=>!c.export).length,format:Format.id};
'''
print(call('execute_script',{'code':script,'params':{'groups':groups,'cubes':cubes,'texture':texture_url}}))
(WORK/'design.json').write_text(json.dumps({'groups':groups,'cubes':cubes,'texture_size':size},indent=2),encoding='utf-8')
print('Texture size',size,'occupied height',py+row_height)
