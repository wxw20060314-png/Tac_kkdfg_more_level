import base64
import io
import json
import math
import random
import urllib.request
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'outputs' / 'cowboy'
OUT.mkdir(parents=True, exist_ok=True)


def command(action, params=None):
    body = json.dumps({'action': action, 'params': params or {}}).encode()
    request = urllib.request.Request('http://127.0.0.1:8787/command', body,
                                     {'Content-Type': 'application/json'})
    with urllib.request.urlopen(request, timeout=90) as response:
        result = json.load(response)
    if not result['ok']:
        raise RuntimeError(result.get('error', result))
    return result.get('result')


groups = []
cubes = []


def group(name, origin, parent=None):
    groups.append({'name': name, 'origin': origin, 'parent': parent})


def cube(name, parent, start, end, material, rotation=None, pivot=None, reference=False):
    cubes.append({'name': name, 'parent': parent, 'from': start, 'to': end,
                  'material': material, 'rotation': rotation or [0, 0, 0],
                  'origin': pivot or start, 'reference': reference})


for part, pivot in [('Head', [0, 24, 0]), ('Body', [0, 24, 0]),
                    ('RightArm', [5, 22, 0]), ('LeftArm', [-5, 22, 0]),
                    ('RightLeg', [2, 12, 0]), ('LeftLeg', [-2, 12, 0])]:
    group('biped' + part, pivot)
    group('armor' + part, pivot, 'biped' + part)
    if part.endswith('Leg'):
        group('armor' + part.replace('Leg', 'Boot'), pivot, 'biped' + part)

# A shallow pinched crown and upturned brim give the hat its western silhouette.
cube('hat_brim', 'armorHead', [-5.1, 30.8, -5.5], [5.1, 31.25, 5.1], 'hat')
cube('hat_brim_right', 'armorHead', [5.0, 30.82, -5.25], [6.7, 31.27, 4.85], 'hat', [0, 0, 18], [5.0, 31.0, 0])
cube('hat_brim_left', 'armorHead', [-6.7, 30.82, -5.25], [-5.0, 31.27, 4.85], 'hat', [0, 0, -18], [-5.0, 31.0, 0])
cube('hat_crown', 'armorHead', [-4.15, 31.1, -4.1], [4.15, 34.6, 4.05], 'hat')
cube('hat_crown_ridge_right', 'armorHead', [1.35, 34.48, -3.7], [3.7, 35.12, 3.5], 'hat_light', [0, 0, -8], [1.35, 34.5, 0])
cube('hat_crown_ridge_left', 'armorHead', [-3.7, 34.48, -3.7], [-1.35, 35.12, 3.5], 'hat_light', [0, 0, 8], [-1.35, 34.5, 0])
cube('hat_band_front', 'armorHead', [-4.2, 31.35, -4.25], [4.2, 32.1, -4.02], 'dark_leather')
cube('hat_band_back', 'armorHead', [-4.2, 31.35, 3.98], [4.2, 32.1, 4.2], 'dark_leather')
cube('hat_band_right', 'armorHead', [4.06, 31.36, -4.05], [4.28, 32.09, 4.05], 'dark_leather')
cube('hat_band_left', 'armorHead', [-4.28, 31.36, -4.05], [-4.06, 32.09, 4.05], 'dark_leather')

cube('shirt_body', 'armorBody', [-4.12, 12.1, -2.12], [4.12, 24.12, 2.12], 'linen')
cube('vest_front_right', 'armorBody', [0.85, 14.1, -2.6], [4.25, 23.5, -2.08], 'leather')
cube('vest_front_left', 'armorBody', [-4.25, 14.1, -2.6], [-0.85, 23.5, -2.08], 'leather')
cube('vest_back', 'armorBody', [-4.23, 14.1, 2.04], [4.23, 23.5, 2.5], 'leather')
cube('vest_side_right', 'armorBody', [4.08, 14.2, -2.18], [4.42, 21.4, 2.2], 'leather')
cube('vest_side_left', 'armorBody', [-4.42, 14.2, -2.18], [-4.08, 21.4, 2.2], 'leather')
cube('vest_lapel_right', 'armorBody', [1.05, 20.6, -2.84], [2.15, 23.7, -2.48], 'leather_light', [0, 0, -17], [1.3, 23.5, -2.6])
cube('vest_lapel_left', 'armorBody', [-2.15, 20.6, -2.84], [-1.05, 23.7, -2.48], 'leather_light', [0, 0, 17], [-1.3, 23.5, -2.6])
cube('bandana_collar', 'armorBody', [-2.6, 23.65, -2.37], [2.6, 24.48, 2.25], 'red')
cube('bandana_point', 'armorBody', [-1.1, 21.8, -2.92], [1.1, 24, -2.61], 'red', [0, 0, 45], [0, 22.9, -2.7])
cube('bandana_knot', 'armorBody', [-0.58, 23.2, -3.16], [0.58, 24.02, -2.83], 'red_light')
cube('waist_belt', 'armorBody', [-4.27, 12.25, -2.3], [4.27, 13.25, 2.3], 'dark_leather')
cube('brass_buckle', 'armorBody', [-0.85, 12.22, -2.58], [0.85, 13.3, -2.26], 'brass')
cube('belt_pouch', 'armorBody', [3.75, 10.85, -1.55], [5.05, 13.3, 1.0], 'leather')
cube('belt_pouch_flap', 'armorBody', [3.68, 12.8, -1.72], [5.14, 13.42, 1.1], 'dark_leather')

for side, sign in [('Right', 1), ('Left', -1)]:
    def mirrored(start, end):
        if sign == 1:
            return start, end
        return [-end[0], start[1], start[2]], [-start[0], end[1], end[2]]

    def paired(name, parent, start, end, material, reference=False):
        a, b = mirrored(start, end)
        cube(name + '_' + side.lower(), parent, a, b, material, reference=reference)

    arm = 'armor' + side + 'Arm'
    paired('shirt_sleeve', arm, [4.05, 14.4, -2.12], [8.12, 24.08, 2.12], 'linen')
    paired('rolled_cuff', arm, [3.94, 14.02, -2.23], [8.23, 15.5, 2.23], 'linen_light')
    leg = 'armor' + side + 'Leg'
    paired('jeans', leg, [0.06, 0.1, -2.1], [4.12, 12.12, 2.1], 'denim')
    boot = 'armor' + side + 'Boot'
    paired('boot_shaft', boot, [0.01, 0.04, -2.27], [4.27, 5.5, 2.27], 'boot')
    paired('boot_toe', boot, [0.12, 0.2, -3.0], [4.15, 2.0, -2.1], 'boot')
    paired('boot_heel', boot, [0.17, -0.25, 0.5], [4.09, 0.31, 2.2], 'sole')
    paired('preview_hand', 'biped' + side + 'Arm', [4.01, 12, -2.01], [8.01, 14.4, 2.01], 'skin', True)

# These locked, non-exporting reference cubes follow GeckoLib's armor template convention.
cube('preview_head', 'bipedHead', [-4, 24, -4], [4, 32, 4], 'skin', reference=True)

palette = {
    'hat': (135, 97, 56), 'hat_light': (150, 111, 65),
    'leather': (97, 58, 34), 'leather_light': (127, 79, 43),
    'dark_leather': (55, 34, 25), 'linen': (194, 177, 137),
    'linen_light': (212, 195, 153), 'red': (136, 46, 37),
    'red_light': (168, 63, 44), 'denim': (49, 69, 82),
    'boot': (76, 45, 29), 'sole': (37, 28, 23), 'brass': (181, 138, 60),
    'skin': (161, 126, 94),
}

# Allocate separate UV rectangles, including a one-pixel gutter around each face.
tiles = []
for index, item in enumerate(cubes):
    x, y, z = [b - a for a, b in zip(item['from'], item['to'])]
    item['faces'] = {}
    for face, (w, h) in {'north': (x, y), 'south': (x, y), 'east': (z, y),
                         'west': (z, y), 'up': (x, z), 'down': (x, z)}.items():
        tiles.append((index, face, max(1, math.ceil(w)), max(1, math.ceil(h))))

texture = Image.new('RGBA', (128, 128), (0, 0, 0, 0))
draw = ImageDraw.Draw(texture)
rng = random.Random(42)
px = py = row_height = 0
for index, face, width, height in sorted(tiles, key=lambda t: (-t[3], -t[2])):
    if px + width + 2 > 128:
        px, py, row_height = 0, py + row_height, 0
    assert py + height + 2 <= 128, 'Texture atlas is full'
    x0, y0 = px + 1, py + 1
    px += width + 2
    row_height = max(row_height, height + 2)
    item = cubes[index]
    item['faces'][face] = [x0, y0, x0 + width, y0 + height]
    base = palette[item['material']]

    def color(delta):
        return tuple(max(0, min(255, value + delta)) for value in base) + (255,)

    face_shade = {'up': 9, 'down': -15, 'north': 3, 'south': -5, 'east': -3, 'west': -7}[face]
    for y in range(height):
        for x in range(width):
            shade = face_shade + round(5 * (1 - y / max(height - 1, 1))) + rng.choice([-2, 0, 0, 2])
            if width >= 3 and height >= 3 and (x == 0 or x == width - 1 or y == height - 1):
                shade -= 10
            texture.putpixel((x0 + x, y0 + y), color(shade))

    name = item['name']
    if name.startswith('vest_front') and face == 'north':
        draw.line((x0, y0 + height - 4, x0 + width - 1, y0 + height - 4), fill=color(-24))
        for y in range(1, height - 1, 2):
            draw.point((x0 + width - 1, y0 + y), fill=color(27))
    if name == 'vest_back' and face == 'south':
        draw.line((x0 + 1, y0 + 2, x0 + width - 2, y0 + 2), fill=color(18))
        draw.line((x0 + width // 2, y0 + 3, x0 + width // 2, y0 + height - 2), fill=color(-12))
    if name == 'shirt_body' and face == 'north':
        cx = x0 + width // 2
        draw.line((cx, y0, cx, y0 + height - 1), fill=color(-19))
        for y in [3, 6, 9]:
            draw.point((cx, y0 + y), fill=(81, 62, 40, 255))
    if name.startswith('jeans'):
        if face in ('east', 'west'):
            draw.line((x0 + width // 2, y0, x0 + width // 2, y0 + height - 1), fill=color(17))
        if face == 'south':
            draw.rectangle((x0 + 1, y0 + 2, x0 + width - 2, y0 + 4), outline=color(22))
        if face == 'north':
            draw.line((x0 + 1, y0 + 6, x0 + width - 2, y0 + 6), fill=color(10))
    if name.startswith('boot_shaft') and face in ('north', 'south'):
        draw.line([(x0, y0 + 1), (x0 + width // 2, y0 + 3), (x0 + width - 1, y0 + 1)], fill=color(39))
    if item['material'] == 'red' and width >= 3 and height >= 3:
        draw.line((x0, y0 + height - 2, x0 + width - 1, y0 + height - 2), fill=color(35))
    if name == 'brass_buckle' and face == 'north':
        draw.point((x0, y0), fill=(236, 193, 103, 255))
    if name == 'preview_head' and face == 'north':
        draw.rectangle((x0, y0, x0 + 7, y0 + 1), fill=(56, 39, 29, 255))
        draw.point((x0 + 1, y0 + 3), fill=(50, 43, 35, 255))
        draw.point((x0 + 6, y0 + 3), fill=(50, 43, 35, 255))
        draw.line((x0 + 2, y0 + 6, x0 + 5, y0 + 6), fill=(91, 64, 44, 255))
    # Extend edge texels into the gutter to prevent sampling bleed.
    for y in range(height):
        texture.putpixel((x0 - 1, y0 + y), texture.getpixel((x0, y0 + y)))
        texture.putpixel((x0 + width, y0 + y), texture.getpixel((x0 + width - 1, y0 + y)))
    for x in range(-1, width + 1):
        texture.putpixel((x0 + x, y0 - 1), texture.getpixel((x0 + x, y0)))
        texture.putpixel((x0 + x, y0 + height), texture.getpixel((x0 + x, y0 + height - 1)))

texture.save(OUT / 'cowboy.png')
buffer = io.BytesIO()
texture.save(buffer, format='PNG')
texture_url = 'data:image/png;base64,' + base64.b64encode(buffer.getvalue()).decode()

status = command('get_status')
if status.get('has_project'):
    raise RuntimeError('A project is already open. Inspect it before rebuilding.')
command('new_project', {'format': 'geckolib_model', 'name': 'cowboy',
                         'geometry_name': 'cowboy', 'texture_width': 128, 'texture_height': 128})
script = '''
Project.geckolib_model_type = 'Armor';
Project.geckolib_modid = 'tacz_morelevel';
Project.box_uv = false;
Undo.initEdit({elements: [], textures: [], outliner: true});
const groups = {};
for (const spec of params.groups) {
    const group = new Group({name: spec.name, origin: spec.origin});
    if (spec.parent) group.addTo(groups[spec.parent]);
    group.init();
    groups[spec.name] = group;
}
const texture = new Texture({name: 'cowboy.png'}).fromDataURL(params.texture).add(false);
for (const spec of params.cubes) {
    const cube = new Cube({name: spec.name, from: spec.from, to: spec.to,
        origin: spec.origin, rotation: spec.rotation, box_uv: false, autouv: 0,
        export: !spec.reference, locked: spec.reference});
    cube.addTo(groups[spec.parent]).init();
    for (const [face, uv] of Object.entries(spec.faces)) {
        cube.faces[face].uv = uv;
        cube.faces[face].texture = texture.uuid;
    }
}
Undo.finishEdit('Build lightweight western cowboy outfit');
Canvas.updateAll();
return {cubes: Cube.all.length, outfit: Cube.all.filter(c=>c.export).length,
        format: Format.id, model_type: Project.geckolib_model_type};
'''
print(command('execute_script', {'code': script, 'params': {'groups': groups, 'cubes': cubes, 'texture': texture_url}}))
(ROOT / 'work/cowboy/design.json').write_text(json.dumps({'groups': groups, 'cubes': cubes}, indent=2), encoding='utf-8')
print('Atlas occupied height', py + row_height)
