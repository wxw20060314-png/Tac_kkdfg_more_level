#!/usr/bin/env python3
"""Generate enchanted-book chest loot: supply_enchant1 / 2 / 3.

rolls：5 / 7 / 10
每抽一本，不允许叠书（count 固定 1）
等级越高：附魔等级更高、宝藏/稀有附魔权重更大。
"""

from __future__ import annotations

import json
from pathlib import Path

ROLLS = {1: 5, 2: 7, 3: 10}

# (enchant_id, level, weight_lv1, weight_lv2, weight_lv3)
# weight 0 = 该档不出现
BOOK_ENTRIES: list[tuple[str, int, int, int, int]] = [
    # —— Lv1 为主：低级常用 ——
    ("minecraft:protection", 1, 10, 4, 1),
    ("minecraft:protection", 2, 6, 8, 3),
    ("minecraft:protection", 3, 0, 4, 6),
    ("minecraft:protection", 4, 0, 1, 4),
    ("minecraft:sharpness", 1, 10, 4, 1),
    ("minecraft:sharpness", 2, 6, 8, 3),
    ("minecraft:sharpness", 3, 0, 4, 6),
    ("minecraft:sharpness", 4, 0, 1, 4),
    ("minecraft:sharpness", 5, 0, 0, 2),
    ("minecraft:efficiency", 1, 8, 3, 1),
    ("minecraft:efficiency", 2, 5, 7, 3),
    ("minecraft:efficiency", 3, 0, 4, 6),
    ("minecraft:efficiency", 4, 0, 1, 4),
    ("minecraft:efficiency", 5, 0, 0, 2),
    ("minecraft:unbreaking", 1, 10, 5, 2),
    ("minecraft:unbreaking", 2, 4, 8, 5),
    ("minecraft:unbreaking", 3, 0, 3, 7),
    ("minecraft:power", 1, 8, 3, 1),
    ("minecraft:power", 2, 5, 7, 3),
    ("minecraft:power", 3, 0, 4, 5),
    ("minecraft:power", 4, 0, 1, 4),
    ("minecraft:power", 5, 0, 0, 2),
    ("minecraft:feather_falling", 1, 6, 3, 1),
    ("minecraft:feather_falling", 2, 4, 6, 3),
    ("minecraft:feather_falling", 3, 0, 3, 5),
    ("minecraft:feather_falling", 4, 0, 1, 3),
    ("minecraft:projectile_protection", 1, 5, 3, 1),
    ("minecraft:projectile_protection", 2, 3, 5, 3),
    ("minecraft:projectile_protection", 3, 0, 3, 4),
    ("minecraft:projectile_protection", 4, 0, 1, 3),
    ("minecraft:fire_protection", 1, 5, 3, 1),
    ("minecraft:fire_protection", 2, 3, 5, 3),
    ("minecraft:fire_protection", 3, 0, 3, 4),
    ("minecraft:fire_protection", 4, 0, 1, 3),
    ("minecraft:blast_protection", 1, 4, 2, 1),
    ("minecraft:blast_protection", 2, 2, 4, 2),
    ("minecraft:blast_protection", 3, 0, 2, 4),
    ("minecraft:blast_protection", 4, 0, 1, 3),
    ("minecraft:knockback", 1, 5, 4, 2),
    ("minecraft:knockback", 2, 2, 5, 4),
    ("minecraft:fire_aspect", 1, 4, 4, 3),
    ("minecraft:fire_aspect", 2, 0, 3, 5),
    ("minecraft:looting", 1, 4, 4, 2),
    ("minecraft:looting", 2, 0, 4, 5),
    ("minecraft:looting", 3, 0, 1, 4),
    ("minecraft:fortune", 1, 4, 4, 2),
    ("minecraft:fortune", 2, 0, 4, 5),
    ("minecraft:fortune", 3, 0, 1, 4),
    ("minecraft:punch", 1, 4, 3, 2),
    ("minecraft:punch", 2, 0, 3, 4),
    ("minecraft:flame", 1, 3, 4, 4),
    ("minecraft:infinity", 1, 1, 3, 5),
    ("minecraft:silk_touch", 1, 1, 3, 5),
    ("minecraft:aqua_affinity", 1, 3, 3, 2),
    ("minecraft:respiration", 1, 4, 3, 1),
    ("minecraft:respiration", 2, 2, 4, 3),
    ("minecraft:respiration", 3, 0, 2, 4),
    ("minecraft:depth_strider", 1, 3, 3, 2),
    ("minecraft:depth_strider", 2, 0, 3, 3),
    ("minecraft:depth_strider", 3, 0, 1, 3),
    ("minecraft:thorns", 1, 2, 3, 2),
    ("minecraft:thorns", 2, 0, 2, 3),
    ("minecraft:thorns", 3, 0, 1, 3),
    ("minecraft:loyalty", 1, 2, 3, 2),
    ("minecraft:loyalty", 2, 0, 2, 3),
    ("minecraft:loyalty", 3, 0, 1, 3),
    ("minecraft:impaling", 1, 2, 2, 1),
    ("minecraft:impaling", 2, 0, 2, 2),
    ("minecraft:impaling", 3, 0, 2, 3),
    ("minecraft:impaling", 4, 0, 1, 2),
    ("minecraft:impaling", 5, 0, 0, 2),
    ("minecraft:riptide", 1, 1, 2, 2),
    ("minecraft:riptide", 2, 0, 2, 2),
    ("minecraft:riptide", 3, 0, 1, 3),
    ("minecraft:channeling", 1, 1, 2, 3),
    ("minecraft:multishot", 1, 1, 3, 4),
    ("minecraft:piercing", 1, 3, 3, 2),
    ("minecraft:piercing", 2, 0, 3, 3),
    ("minecraft:piercing", 3, 0, 2, 3),
    ("minecraft:piercing", 4, 0, 1, 3),
    ("minecraft:quick_charge", 1, 3, 3, 2),
    ("minecraft:quick_charge", 2, 0, 3, 3),
    ("minecraft:quick_charge", 3, 0, 1, 4),
    ("minecraft:sweeping", 1, 3, 3, 2),
    ("minecraft:sweeping", 2, 0, 3, 3),
    ("minecraft:sweeping", 3, 0, 1, 3),
    # —— 宝藏 / 高稀有：偏高等级 ——
    ("minecraft:mending", 1, 0, 2, 6),
    ("minecraft:frost_walker", 1, 0, 2, 3),
    ("minecraft:frost_walker", 2, 0, 1, 3),
    ("minecraft:soul_speed", 1, 0, 1, 3),
    ("minecraft:soul_speed", 2, 0, 1, 2),
    ("minecraft:soul_speed", 3, 0, 0, 2),
    ("minecraft:swift_sneak", 1, 0, 1, 3),
    ("minecraft:swift_sneak", 2, 0, 1, 2),
    ("minecraft:swift_sneak", 3, 0, 0, 2),
    ("minecraft:binding_curse", 1, 0, 1, 1),
    ("minecraft:vanishing_curse", 1, 0, 1, 1),
]

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "src/main/resources/data/tacz_morelevel/loot_tables/chests/supply_enchant"


def make_book_entry(enchant_id: str, level: int, weight: int) -> dict:
    # 不允许叠书：不设 set_count，默认 1
    tag = '{StoredEnchantments:[{id:"%s",lvl:%ds}]}' % (enchant_id, level)
    return {
        "type": "minecraft:item",
        "weight": weight,
        "name": "minecraft:enchanted_book",
        "functions": [
            {
                "function": "minecraft:set_nbt",
                "tag": tag,
            }
        ],
    }


def build_table(tier: int) -> dict:
    entries = []
    for enchant_id, ench_lvl, w1, w2, w3 in BOOK_ENTRIES:
        weight = {1: w1, 2: w2, 3: w3}[tier]
        if weight <= 0:
            continue
        entries.append(make_book_entry(enchant_id, ench_lvl, weight))
    return {
        "type": "minecraft:chest",
        "pools": [
            {
                "rolls": ROLLS[tier],
                "bonus_rolls": 0.0,
                "entries": entries,
            }
        ],
    }


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for old in OUT_DIR.glob("supply_enchant*.json"):
        old.unlink()

    for tier in (1, 2, 3):
        filename = f"supply_enchant{tier}.json"
        table = build_table(tier)
        (OUT_DIR / filename).write_text(
            json.dumps(table, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
        )
        n = len(table["pools"][0]["entries"])
        print(f"{filename}: rolls={ROLLS[tier]} entries={n}")

    print(f"done -> {OUT_DIR}")


if __name__ == "__main__":
    main()
