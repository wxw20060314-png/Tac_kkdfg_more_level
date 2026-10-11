#!/usr/bin/env python3
"""Generate gun chest loot tables supply_gun{a}{b}.json (a<=b).

=== 算法 ===
1. 从 black_market/buy_*.json 收集全部 \"gun\" ID。
2. 五档（按 GunId 后缀）：
   1 普通（无后缀） / 2 改进 _improved / 3 精致 _refined /
   4 完美 _perfect / 5 卓越 _excellent
3. 同档权重相等 = GRADIENT[level]；合并表跨档乘梯度后 GCD，再亚安全 clamp 到 [1, SUB_SAFE_MAX]。
4. 不做堆叠安全区间（枪不可堆叠）。
5. 单 pool，rolls=ITEM_ROLLS；条目为 tacz:modern_kinetic_gun + set_nbt GunId。
"""

from __future__ import annotations

import json
import math
import re
from pathlib import Path

# ---------------------------------------------------------------------------
# 可调常数
# ---------------------------------------------------------------------------
GRADIENT = {1: 16, 2: 8, 3: 4, 4: 2, 5: 1}
SUB_SAFE_MIN = 1
SUB_SAFE_MAX = 64
ITEM_ROLLS = 15

GUN_ITEM = "tacz:modern_kinetic_gun"

# 后缀 → 单位级（先匹配长的）
TIER_SUFFIXES: list[tuple[str, int]] = [
    ("_excellent", 5),
    ("_perfect", 4),
    ("_refined", 3),
    ("_improved", 2),
]

ROOT = Path(__file__).resolve().parents[1]
BUY_DIR = ROOT / "src/main/resources/data/tacz_morelevel/black_market"
OUT_DIR = ROOT / "src/main/resources/data/tacz_morelevel/loot_tables/chests/supply_gun"


def tier_of(gun_id: str) -> int:
    for suffix, tier in TIER_SUFFIXES:
        if gun_id.endswith(suffix):
            return tier
    return 1  # 普通


def collect_guns_by_tier() -> dict[int, list[str]]:
    guns: set[str] = set()
    for path in sorted(BUY_DIR.glob("buy_*.json")):
        data = json.loads(path.read_text(encoding="utf-8"))
        for trade in data.get("trades", []):
            g = (trade.get("output") or {}).get("gun")
            if g:
                guns.add(g)

    by_tier: dict[int, list[str]] = {i: [] for i in range(1, 6)}
    for g in sorted(guns):
        by_tier[tier_of(g)].append(g)
    return by_tier


def gcd_many(nums: list[int]) -> int:
    g = 0
    for n in nums:
        g = math.gcd(g, abs(n))
    return g if g > 0 else 1


def clamp_subsafe(w: int) -> int:
    return max(SUB_SAFE_MIN, min(SUB_SAFE_MAX, w))


def make_gun_entry(gun_id: str, weight: int) -> dict:
    # set_nbt 字符串里引号需转义
    tag = '{GunId:"%s"}' % gun_id
    return {
        "type": "minecraft:item",
        "weight": weight,
        "name": GUN_ITEM,
        "functions": [
            {
                "function": "minecraft:set_nbt",
                "tag": tag,
            }
        ],
    }


def build_table(a: int, b: int, by_tier: dict[int, list[str]]) -> dict:
    raw: list[tuple[str, int]] = []
    for level in range(a, b + 1):
        w0 = GRADIENT[level]
        for gun_id in by_tier[level]:
            raw.append((gun_id, w0))

    if not raw:
        return {
            "type": "minecraft:chest",
            "pools": [{"rolls": ITEM_ROLLS, "bonus_rolls": 0.0, "entries": []}],
        }

    g = gcd_many([w for _, w in raw])
    entries = [
        make_gun_entry(gun_id, clamp_subsafe(max(1, w0 // g))) for gun_id, w0 in raw
    ]

    return {
        "type": "minecraft:chest",
        "pools": [
            {
                "rolls": ITEM_ROLLS,
                "bonus_rolls": 0.0,
                "entries": entries,
            }
        ],
    }


def main() -> None:
    by_tier = collect_guns_by_tier()
    for lvl, guns in by_tier.items():
        print(f"tier {lvl}: {len(guns)} guns")

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for old in OUT_DIR.glob("supply_gun*.json"):
        old.unlink()

    for a in range(1, 6):
        for b in range(a, 6):
            name = f"supply_gun{a}{b}.json"
            table = build_table(a, b, by_tier)
            (OUT_DIR / name).write_text(
                json.dumps(table, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
            )
            ws = [e["weight"] for e in table["pools"][0]["entries"]]
            print(
                f"  {name}: entries={len(ws)} "
                f"w=[{min(ws) if ws else 0},{max(ws) if ws else 0}]"
            )

    print(f"done -> {OUT_DIR}")


if __name__ == "__main__":
    main()
