#!/usr/bin/env python3
"""Generate food chest loot: supply_food1 / 2 / 3.

rolls：10 / 15 / 20
数量：1级个位数、2级十几、3级二十几（稀有物单独更少）
等级越高，稀有食物权重越高、常见食物权重相对下降。
"""

from __future__ import annotations

import json
from pathlib import Path

# level -> rolls
ROLLS = {1: 10, 2: 15, 3: 20}

# 常见/次常见：按等级给数量区间
COUNT_COMMON = {
    1: (3, 8),
    2: (10, 16),
    3: (20, 28),
}
COUNT_UNCOMMON = {
    1: (2, 5),
    2: (8, 14),
    3: (16, 24),
}
# 稀有：始终少量
COUNT_RARE = {
    1: (1, 2),
    2: (1, 3),
    3: (2, 4),
}
COUNT_EPIC = {
    1: (1, 1),
    2: (1, 1),
    3: (1, 2),
}

# (item_id, rarity, weight_by_level[1], [2], [3])
# rarity: common / uncommon / rare / epic → 选数量表
FOOD_ENTRIES: list[tuple[str, str, int, int, int]] = [
    # 常见
    ("minecraft:bread", "common", 14, 10, 6),
    ("minecraft:baked_potato", "common", 12, 9, 5),
    ("minecraft:cooked_beef", "common", 12, 9, 6),
    ("minecraft:cooked_porkchop", "common", 12, 9, 6),
    ("minecraft:cooked_chicken", "common", 12, 9, 5),
    ("minecraft:cooked_mutton", "common", 10, 8, 5),
    ("minecraft:cooked_cod", "common", 10, 7, 4),
    ("minecraft:cooked_salmon", "common", 10, 7, 4),
    ("minecraft:apple", "common", 12, 8, 4),
    ("minecraft:carrot", "common", 10, 7, 4),
    ("minecraft:cookie", "common", 8, 6, 3),
    ("minecraft:melon_slice", "common", 8, 6, 3),
    ("minecraft:sweet_berries", "common", 8, 5, 3),
    # 次常见
    ("minecraft:golden_carrot", "uncommon", 3, 8, 10),
    ("minecraft:pumpkin_pie", "uncommon", 4, 7, 8),
    ("minecraft:mushroom_stew", "uncommon", 3, 6, 7),
    ("minecraft:rabbit_stew", "uncommon", 2, 5, 7),
    ("minecraft:honey_bottle", "uncommon", 3, 6, 8),
    ("minecraft:glow_berries", "uncommon", 4, 6, 5),
    ("minecraft:dried_kelp", "uncommon", 5, 5, 3),
    # 稀有
    ("minecraft:golden_apple", "rare", 1, 3, 6),
    ("minecraft:cake", "rare", 1, 2, 4),
    ("minecraft:chorus_fruit", "rare", 1, 3, 5),
    # 史诗
    ("minecraft:enchanted_golden_apple", "epic", 0, 1, 3),
]

COUNT_BY_RARITY = {
    "common": COUNT_COMMON,
    "uncommon": COUNT_UNCOMMON,
    "rare": COUNT_RARE,
    "epic": COUNT_EPIC,
}

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "src/main/resources/data/tacz_morelevel/loot_tables/chests/supply_food"


def make_entry(item_id: str, weight: int, cmin: int, cmax: int) -> dict:
    return {
        "type": "minecraft:item",
        "weight": weight,
        "name": item_id,
        "functions": [
            {
                "function": "minecraft:set_count",
                "count": {"min": cmin, "max": cmax},
            }
        ],
    }


def build_table(level: int) -> dict:
    entries = []
    for item_id, rarity, w1, w2, w3 in FOOD_ENTRIES:
        weight = {1: w1, 2: w2, 3: w3}[level]
        if weight <= 0:
            continue
        cmin, cmax = COUNT_BY_RARITY[rarity][level]
        # 炖菜/蛋糕不可堆或堆叠小：数量压到 1~少数
        if item_id in (
            "minecraft:mushroom_stew",
            "minecraft:rabbit_stew",
            "minecraft:cake",
            "minecraft:honey_bottle",
        ):
            if level == 1:
                cmin, cmax = 1, 2
            elif level == 2:
                cmin, cmax = 2, 4
            else:
                cmin, cmax = 3, 6
        entries.append(make_entry(item_id, weight, cmin, cmax))
    return {
        "type": "minecraft:chest",
        "pools": [
            {
                "rolls": ROLLS[level],
                "bonus_rolls": 0.0,
                "entries": entries,
            }
        ],
    }


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for old in OUT_DIR.glob("supply_food*.json"):
        old.unlink()

    for level in (1, 2, 3):
        filename = f"supply_food{level}.json"
        table = build_table(level)
        (OUT_DIR / filename).write_text(
            json.dumps(table, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
        )
        n = len(table["pools"][0]["entries"])
        print(f"{filename}: rolls={ROLLS[level]} entries={n}")

    print(f"done -> {OUT_DIR}")


if __name__ == "__main__":
    main()
