#!/usr/bin/env python3
"""Generate ammo chest loot: supply_ammo_small / medium / large.

比例（数量）：小型:中型:大型 = 2:4:6（相对 1:2:3）
rolls：10 / 15 / 20
权重：常用10 / 次常用7 / 少见4 / 特种2
中型数量以旧士兵弹药箱为基准；超量按 stack_max 截断。
"""

from __future__ import annotations

import json
from pathlib import Path

# ---------------------------------------------------------------------------
# 可调常数
# ---------------------------------------------------------------------------
# 规格名 -> (rolls, 数量倍率相对「单位2」；小2 中4 大6)
SIZES = {
    "small": (10, 2),
    "medium": (15, 4),
    "large": (20, 6),
}
SCALE_UNIT = 4  # 中型对应倍率 4，中型 count = base * (4/4)

DEFAULT_STACK_MAX = 64

# (ammo_id, weight, base_min, base_max, stack_max)  — base 对应「中型」区间
AMMO_ENTRIES: list[tuple[str, int, int, int, int]] = [
    # 常用 weight 10
    ("tacz:9mm", 10, 16, 48, 64),
    ("tacz:45acp", 10, 16, 48, 64),
    ("tacz:556x45", 10, 16, 48, 64),
    ("tacz:762x39", 10, 16, 48, 64),
    ("tacz:545x39", 10, 16, 48, 64),
    # 次常用 weight 7
    ("tacz:12g", 7, 12, 36, 64),
    ("tacz:762x54", 7, 16, 48, 64),
    ("tacz:308", 7, 12, 36, 64),
    ("tacz:762x25", 7, 16, 48, 64),
    ("tacz:57x28", 7, 16, 48, 64),
    ("tacz:58x42", 7, 16, 48, 64),
    ("tacz:46x30", 7, 16, 48, 64),
    ("tacz:68x51fury", 7, 16, 48, 64),
    ("tacz:357mag", 7, 12, 36, 64),
    ("tacz:50ae", 7, 12, 36, 64),
    ("tacz:45_70", 7, 12, 36, 64),
    ("tacz:500mag", 7, 12, 36, 64),
    ("tacz:22wmr", 7, 16, 48, 64),
    # 少见 weight 4
    ("tacz:30_06", 4, 8, 24, 64),
    ("tacz:792x57", 4, 8, 24, 64),
    ("tacz:338", 4, 4, 12, 64),
    ("tacz:50bmg", 4, 4, 12, 64),
    # 特种 weight 2
    ("tacz:40mm", 2, 1, 3, 16),
    ("tacz:rpg_rocket", 2, 1, 3, 16),
]

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "src/main/resources/data/tacz_morelevel/loot_tables/chests/supply_ammo"


def scale_count(base_min: int, base_max: int, mult: int, stack_max: int) -> tuple[int, int]:
    # 中型 mult=4 → 原 base；小2 → *0.5；大6 → *1.5
    cmin = max(1, int(round(base_min * mult / SCALE_UNIT)))
    cmax = max(cmin, int(round(base_max * mult / SCALE_UNIT)))
    cmin = min(cmin, stack_max)
    cmax = min(cmax, stack_max)
    if cmax < cmin:
        cmax = cmin
    return cmin, cmax


def make_entry(
    ammo_id: str, weight: int, cmin: int, cmax: int
) -> dict:
    return {
        "type": "minecraft:item",
        "weight": weight,
        "name": "tacz:ammo",
        "functions": [
            {
                "function": "minecraft:set_nbt",
                "tag": '{AmmoId:"%s"}' % ammo_id,
            },
            {
                "function": "minecraft:set_count",
                "count": {"min": cmin, "max": cmax},
            },
        ],
    }


def build_table(rolls: int, mult: int) -> dict:
    entries = []
    for ammo_id, weight, bmin, bmax, stack_max in AMMO_ENTRIES:
        cmin, cmax = scale_count(bmin, bmax, mult, stack_max)
        entries.append(make_entry(ammo_id, weight, cmin, cmax))
    return {
        "type": "minecraft:chest",
        "pools": [
            {
                "rolls": rolls,
                "bonus_rolls": 0.0,
                "entries": entries,
            }
        ],
    }


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for old in OUT_DIR.glob("supply_ammo_*.json"):
        old.unlink()

    for name, (rolls, mult) in SIZES.items():
        filename = f"supply_ammo_{name}.json"
        table = build_table(rolls, mult)
        (OUT_DIR / filename).write_text(
            json.dumps(table, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
        )
        # 抽样打印常用 9mm 数量区间
        e0 = table["pools"][0]["entries"][0]
        cnt = e0["functions"][1]["count"]
        print(f"{filename}: rolls={rolls} 9mm=[{cnt['min']},{cnt['max']}] entries={len(e0) and len(table['pools'][0]['entries'])}")

    print(f"done -> {OUT_DIR}")


if __name__ == "__main__":
    main()
