"""Draw four inventory-sized icons from the dark cowboy model's materials."""

from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[2]
DESTINATION = Path(r'E:\mcsucai\mc\codex\dark_cowboy') / 'current'
PREVIEW = DESTINATION / 'item_icons_preview.png'

INK = '#26211d'
HAT_DARK = '#49362a'
HAT = '#644b36'
HAT_LIGHT = '#846549'
JACKET_DARK = '#3e3933'
JACKET = '#554e45'
JACKET_LIGHT = '#716458'
SHIRT = '#6f6b60'
DENIM_DARK = '#3b3a37'
DENIM = '#54514c'
DENIM_LIGHT = '#70695f'
BOOT_DARK = '#3b2b20'
BOOT = '#634730'
BOOT_LIGHT = '#806043'
LEATHER = '#785638'
DIRT = '#735a43'
RUST = '#8c6846'
IRON_DARK = '#676258'
IRON = '#a7a08e'
BRASS = '#bd9153'
SCARF_DARK = '#7d3330'
SCARF = '#a3463c'
SHELL = '#993e35'


def icon():
    image = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    return image, ImageDraw.Draw(image)


def hat_icon():
    image, d = icon()
    d.polygon([(2, 9), (3, 8), (4, 7), (11, 7), (12, 8), (14, 7), (15, 9),
               (13, 11), (11, 12), (3, 12), (1, 10)], fill=INK)
    d.rectangle((4, 3, 11, 9), fill=INK)
    d.polygon([(4, 4), (5, 2), (7, 3), (9, 3), (11, 2), (11, 8), (4, 8)], fill=HAT)
    d.line((5, 3, 6, 3), fill=HAT_LIGHT)
    d.line((10, 3, 11, 3), fill=HAT_LIGHT)
    d.rectangle((4, 7, 11, 8), fill=HAT_DARK)
    d.line((5, 7, 10, 7), fill=RUST)
    d.polygon([(2, 9), (4, 9), (5, 10), (11, 10), (13, 9), (14, 8), (14, 9),
               (12, 11), (4, 11), (2, 10)], fill=HAT)
    d.line((3, 9, 4, 10), fill=HAT_LIGHT)
    d.line((5, 10, 11, 10), fill=HAT_LIGHT)
    d.point((12, 10), fill=DIRT)
    d.rectangle((5, 12, 7, 12), fill=IRON_DARK)
    d.rectangle((9, 12, 11, 12), fill=IRON_DARK)
    d.point((5, 12), fill=IRON)
    return image


def chest_icon():
    image, d = icon()
    d.polygon([(3, 2), (5, 1), (6, 2), (10, 2), (11, 1), (13, 2),
               (15, 5), (14, 11), (12, 12), (12, 15), (4, 15),
               (4, 12), (1, 11), (0, 5)], fill=INK)
    d.polygon([(3, 3), (5, 2), (6, 3), (5, 10), (4, 11), (2, 10), (1, 6)], fill=JACKET)
    d.polygon([(11, 2), (13, 3), (14, 6), (13, 10), (12, 11), (11, 10), (10, 3)], fill=JACKET)
    d.rectangle((5, 3, 10, 14), fill=JACKET)
    d.polygon([(6, 3), (8, 5), (10, 3), (9, 14), (7, 14)], fill=SHIRT)
    d.line((8, 6, 8, 13), fill=JACKET_DARK)
    d.point((8, 8), fill=INK)
    d.point((8, 11), fill=INK)
    d.polygon([(5, 2), (7, 4), (6, 7), (5, 5)], fill=JACKET_LIGHT)
    d.polygon([(11, 2), (9, 4), (10, 7), (11, 5)], fill=JACKET_LIGHT)
    d.polygon([(7, 2), (8, 3), (9, 2), (9, 4), (8, 6), (7, 4)], fill=SCARF)
    d.point((8, 3), fill=SCARF_DARK)
    d.line((2, 10, 4, 10), fill=LEATHER)
    d.line((12, 10, 14, 10), fill=LEATHER)
    d.line((5, 14, 6, 14), fill=DIRT)
    d.line((10, 14, 11, 14), fill=DIRT)
    d.point((3, 6), fill=JACKET_LIGHT)
    d.point((12, 8), fill=DIRT)
    d.line((11, 4, 5, 13), fill=INK, width=2)
    d.line((11, 3, 5, 12), fill=LEATHER)
    for x, y in [(10, 4), (9, 5), (8, 7), (7, 9), (6, 10)]:
        d.line((x, y, x + 1, y + 1), fill=SHELL)
        d.point((x, y + 1), fill=BRASS)
    d.line((6, 13, 11, 13), fill=JACKET_DARK)
    return image


def leggings_icon():
    image, d = icon()
    d.polygon([(3, 2), (12, 2), (13, 4), (12, 14), (9, 14),
               (8, 8), (7, 14), (3, 14), (2, 4)], fill=INK)
    d.rectangle((3, 3, 12, 5), fill=DENIM_DARK)
    d.line((3, 3, 12, 3), fill=LEATHER)
    d.rectangle((7, 3, 8, 4), fill=IRON)
    d.point((8, 3), fill=INK)
    d.polygon([(3, 6), (7, 6), (7, 12), (6, 13), (3, 13)], fill=DENIM)
    d.polygon([(9, 6), (12, 6), (12, 13), (10, 13), (9, 12)], fill=DENIM)
    d.line((4, 6, 4, 12), fill=DENIM_LIGHT)
    d.line((11, 6, 11, 12), fill=DENIM_LIGHT)
    d.line((6, 6, 6, 8), fill=JACKET_DARK)
    d.line((9, 6, 9, 8), fill=JACKET_DARK)
    d.line((3, 9, 5, 8), fill=DIRT)
    d.line((4, 9, 6, 9), fill=INK)
    d.point((5, 10), fill=DENIM_LIGHT)
    d.line((10, 11, 12, 10), fill=DIRT)
    d.line((10, 12, 11, 12), fill=INK)
    d.line((3, 13, 7, 13), fill=DENIM_DARK)
    d.line((9, 13, 12, 13), fill=DENIM_DARK)
    return image


def boots_icon():
    image, d = icon()
    d.polygon([(3, 3), (7, 3), (7, 11), (8, 12), (8, 14), (1, 14),
               (1, 12), (3, 11)], fill=INK)
    d.polygon([(9, 3), (13, 3), (13, 11), (15, 12), (15, 14), (8, 14),
               (8, 12), (9, 11)], fill=INK)
    d.rectangle((3, 4, 6, 11), fill=BOOT)
    d.rectangle((10, 4, 12, 11), fill=BOOT)
    d.line((3, 4, 4, 3, 5, 4, 6, 3), fill=BOOT_LIGHT)
    d.line((10, 3, 11, 4, 12, 3, 13, 4), fill=BOOT_LIGHT)
    d.line((4, 5, 4, 10), fill=BOOT_LIGHT)
    d.line((11, 5, 11, 10), fill=BOOT_LIGHT)
    d.line((4, 8, 5, 9, 6, 8), fill=RUST)
    d.line((10, 8, 11, 9, 12, 8), fill=RUST)
    d.rectangle((2, 11, 7, 13), fill=BOOT)
    d.rectangle((9, 11, 14, 13), fill=BOOT)
    d.line((2, 12, 4, 12), fill=DIRT)
    d.line((12, 12, 14, 12), fill=DIRT)
    d.line((2, 14, 8, 14), fill=BOOT_DARK)
    d.line((8, 14, 15, 14), fill=BOOT_DARK)
    d.line((3, 10, 7, 10), fill=IRON_DARK)
    d.line((9, 10, 13, 10), fill=IRON_DARK)
    for x in (7, 8):
        d.point((x, 9), fill=IRON)
        d.point((x, 11), fill=IRON_DARK)
    d.point((7, 10), fill=BRASS)
    d.point((8, 10), fill=BRASS)
    return image


icons = {
    'helmet': hat_icon(),
    'chestplate': chest_icon(),
    'leggings': leggings_icon(),
    'boots': boots_icon(),
}
for name, image in icons.items():
    path = DESTINATION / f'dark_cowboy_{name}.png'
    image.save(path)
    assert image.size == (16, 16) and image.getbbox()

sheet = Image.new('RGBA', (4 * 16 * 16, 16 * 16), '#a49d91')
for index, image in enumerate(icons.values()):
    sheet.alpha_composite(image.resize((256, 256), Image.Resampling.NEAREST), (index * 256, 0))
sheet.convert('RGB').save(PREVIEW)
