#!/usr/bin/env python3
"""Generate resource supply chest loot tables supply{a}{b}.json (a<=b).

=== 算法（后人改常数即可）===
1. 首次归属：buy_1→buy_5 扫描，同物品只进「最下级首次」档，记下 price1（货币进位 8）。
2. 原权重：w0 = (N // price1) * GRADIENT[level]，count=1。
3. 合并表后：对物品池所有 w 求 GCD 整除，保证可出现权重 1。
4. 安全化简（仅可堆叠且 w > SAFE_MAX）：
     while w > SAFE_MAX and count*2 <= max_stack:
         count *= 2; w //= 2
     目标是把过大权重压进相对安全带 [SAFE_MIN, SAFE_MAX]（8~32）。
     w < SAFE_MIN 或 w <= SAFE_MAX：不处理（贵重/中等保持原相对稀有度）。
5. 亚安全：不可堆叠、或堆叠已满仍过大：
     w = clamp(w, SUB_SAFE_MIN, SUB_SAFE_MAX)  # 1~64
6. 两个 pool：货币 rolls=MONEY_ROLLS；物品 rolls=ITEM_ROLLS。

堆叠上限见 STACK_MAX（未列出默认 1=不可用堆叠化简）。
"""

from __future__ import annotations

import json
import math
from pathlib import Path

# ---------------------------------------------------------------------------
# 可调常数（文件前暴露）
# ---------------------------------------------------------------------------
N = 256
GRADIENT = {1: 16, 2: 8, 3: 4, 4: 2, 5: 1}

# 相对安全区间：仅当 w > SAFE_MAX 才尝试堆叠×2 / 权÷2 往回压
SAFE_MIN = 8
SAFE_MAX = 32

# 亚安全区间：压不住或不可堆叠时夹紧
SUB_SAFE_MIN = 1
SUB_SAFE_MAX = 64

ITEM_ROLLS = 15
MONEY_ROLLS = 1

CURRENCY = {
    1: ("tacz_morelevel:black_universal_currency_digit_1", 10, 20),
    2: ("tacz_morelevel:black_universal_currency_digit_2", 8, 16),
    3: ("tacz_morelevel:black_universal_currency_digit_3", 7, 14),
    4: ("tacz_morelevel:black_universal_currency_digit_4", 6, 12),
    5: ("tacz_morelevel:black_universal_currency_digit_4", 18, 36),
}

DIGIT_MULT = {
    "tacz_morelevel:black_universal_currency_digit_1": 1,
    "tacz_morelevel:black_universal_currency_digit_2": 8,
    "tacz_morelevel:black_universal_currency_digit_3": 64,
    "tacz_morelevel:black_universal_currency_digit_4": 512,
}

# 可堆叠合并表：item_id -> max_stack；未列出视为 1（不可堆叠化简）
STACK_MAX: dict[str, int] = {
    "minecraft:iron_ingot": 64,
    "minecraft:copper_ingot": 64,
    "minecraft:gold_ingot": 64,
    "minecraft:gunpowder": 64,
    "minecraft:emerald": 64,
    "minecraft:diamond": 64,
    "minecraft:netherite_ingot": 64,
    "minecraft:blaze_rod": 64,
    "minecraft:redstone": 64,
    "minecraft:lapis_lazuli": 64,
    "minecraft:dragon_egg": 64,
    "minecraft:nether_star": 64,
    "tacz_morelevel:obsidian_item": 64,
    "tacz_morelevel:molotov": 64,
    "tacz_morelevel:landmine": 64,
    "tacz_morelevel:frozen_landmine": 64,
    "tacz_morelevel:grenade": 64,
    "tacz_morelevel:detection_grenade": 64,
    "tacz_morelevel:improved_gun_upgrade_voucher": 64,
    "tacz_morelevel:refined_gun_upgrade_voucher": 64,
    "tacz_morelevel:perfect_gun_upgrade_voucher": 64,
    "tacz_morelevel:excellent_gun_upgrade_voucher": 64,
    "tacz_morelevel:seventh_steel_ingot": 64,
    "tacz_morelevel:sixth_steel_ingot": 64,
    "tacz_morelevel:super_potion_bottle": 64,
    "tacz_morelevel:carbon_fiber_armor_plate_junior": 16,
    "tacz_morelevel:carbon_fiber_armor_plate_medium": 16,
    "tacz_morelevel:carbon_fiber_armor_plate_advanced": 16,
    "tacz_morelevel:ballistic_steel_armor_plate_junior": 16,
    "tacz_morelevel:ballistic_steel_armor_plate_medium": 16,
    "tacz_morelevel:ballistic_steel_armor_plate_advanced": 16,
    "tacz_morelevel:blast_steel_armor_plate_junior": 16,
    "tacz_morelevel:blast_steel_armor_plate_medium": 16,
    "tacz_morelevel:blast_steel_armor_plate_advanced": 16,
    "tacz_morelevel:flash_resist_goggles_junior": 16,
    "tacz_morelevel:flash_resist_goggles_medium": 16,
    "tacz_morelevel:flash_resist_goggles_advanced": 16,
    "tacz_morelevel:black_universal_currency_digit_1": 64,
    "tacz_morelevel:black_universal_currency_digit_2": 64,
    "tacz_morelevel:black_universal_currency_digit_3": 64,
    "tacz_morelevel:black_universal_currency_digit_4": 64,
}

# 黑市有售但不进补给箱战利品
LOOT_EXCLUDE: set[str] = {
    "tacz_morelevel:bandage",
    "tacz_morelevel:first_aid_kit",
    "tacz_morelevel:medical_box",
}

# 黑市笔误 → 真实注册 ID（生成前强制纠正，避免整表解析失败）
ITEM_ID_ALIASES: dict[str, str] = {
    "tacz_morelevel:forth_bulletproof_chestplate": "tacz_morelevel:fourth_bulletproof_chestplate",
}

ROOT = Path(__file__).resolve().parents[1]
BUY_DIR = ROOT / "src/main/resources/data/tacz_morelevel/black_market"
OUT_DIR = ROOT / "src/main/resources/data/tacz_morelevel/loot_tables/chests/supply"


def canonical_item_id(item_id: str) -> str:
    return ITEM_ID_ALIASES.get(item_id, item_id)


def price1(input_obj: dict) -> int:
    item = input_obj.get("item", "")
    count = int(input_obj.get("count", 1))
    mult = DIGIT_MULT.get(item)
    if mult is None:
        return max(1, count)
    return max(1, count * mult)


def is_resource_output(out: dict) -> str | None:
    if "gun" in out or "ammo" in out:
        return None
    item_id = out.get("item")
    if not item_id:
        return None
    item_id = canonical_item_id(item_id)
    if ":ammo" in item_id or item_id.endswith("_ammo") or "/ammo" in item_id:
        return None
    if item_id in LOOT_EXCLUDE:
        return None
    return item_id


def load_first_appearance_units() -> dict[int, dict[str, int]]:
    units: dict[int, dict[str, int]] = {lvl: {} for lvl in range(1, 6)}
    seen: set[str] = set()
    for level in range(1, 6):
        data = json.loads((BUY_DIR / f"buy_{level}.json").read_text(encoding="utf-8"))
        level_cheapest: dict[str, int] = {}
        for trade in data.get("trades", []):
            item_id = is_resource_output(trade.get("output") or {})
            if item_id is None or item_id in seen:
                continue
            p = price1(trade.get("input") or {})
            if item_id not in level_cheapest or p < level_cheapest[item_id]:
                level_cheapest[item_id] = p
        for item_id, p in level_cheapest.items():
            units[level][item_id] = p
            seen.add(item_id)
    return units


def max_stack(item_id: str) -> int:
    return int(STACK_MAX.get(item_id, 1))


def gcd_many(nums: list[int]) -> int:
    g = 0
    for n in nums:
        g = math.gcd(g, abs(n))
    return g if g > 0 else 1


def apply_safe_and_subsafe(item_id: str, w: int) -> tuple[int, int]:
    """返回 (weight, count)。"""
    count = 1
    stack = max_stack(item_id)

    # 贵重 (<SAFE_MIN) / 中等 (<=SAFE_MAX)：不做安全堆叠化简
    if w <= SAFE_MAX:
        # 仍过亚安全上限时（理论上少见）夹一下
        if w > SUB_SAFE_MAX:
            w = SUB_SAFE_MAX
        if w < SUB_SAFE_MIN:
            w = SUB_SAFE_MIN
        return w, count

    # w > SAFE_MAX：尝试堆叠化简压回安全带
    if stack > 1:
        while w > SAFE_MAX and count * 2 <= stack and w % 2 == 0:
            count *= 2
            w //= 2
        # 若不能整除 2 但仍过大，能叠则尽量再压（允许奇数除法取整）
        while w > SAFE_MAX and count * 2 <= stack:
            count *= 2
            w = max(SUB_SAFE_MIN, w // 2)

    # 堆叠已满仍过大，或本就不可堆叠：亚安全夹紧
    if w > SUB_SAFE_MAX:
        w = SUB_SAFE_MAX
    if w < SUB_SAFE_MIN:
        w = SUB_SAFE_MIN
    return w, count


def make_item_entry(item_id: str, weight: int, count: int) -> dict:
    entry: dict = {
        "type": "minecraft:item",
        "weight": weight,
        "name": item_id,
    }
    if count != 1:
        entry["functions"] = [
            {
                "function": "minecraft:set_count",
                "count": count,
            }
        ]
    return entry


def make_currency_entry(item_id: str, weight: int, cmin: int, cmax: int) -> dict:
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


def build_table(a: int, b: int, units: dict[int, dict[str, int]]) -> dict:
    # (level, item_id, price) raw weights before gcd
    raw_items: list[tuple[str, int]] = []
    for level in range(a, b + 1):
        g = GRADIENT[level]
        for item_id, p in sorted(units[level].items()):
            w0 = max(1, (N // p) * g)
            raw_items.append((item_id, w0))

    money_raw: list[tuple[str, int, int, int]] = []
    for level in range(a, b + 1):
        g = GRADIENT[level]
        cur_id, cmin, cmax = CURRENCY[level]
        mid = max(1, (cmin + cmax) // 2)
        w0 = max(1, (N // mid) * g)
        money_raw.append((cur_id, w0, cmin, cmax))

    # 表级 GCD，让权重能为 1
    item_gcd = gcd_many([w for _, w in raw_items]) if raw_items else 1
    money_gcd = gcd_many([w for _, w, _, _ in money_raw]) if money_raw else 1

    item_entries: list[dict] = []
    for item_id, w0 in raw_items:
        w = max(1, w0 // item_gcd)
        w, count = apply_safe_and_subsafe(item_id, w)
        item_entries.append(make_item_entry(item_id, w, count))

    money_entries: list[dict] = []
    for cur_id, w0, cmin, cmax in money_raw:
        w = max(1, w0 // money_gcd)
        # 货币：按可堆叠走同一套安全/亚安全（数量区间仍用 CURRENCY 的 min/max）
        w, _ = apply_safe_and_subsafe(cur_id, w)
        money_entries.append(make_currency_entry(cur_id, w, cmin, cmax))

    return {
        "type": "minecraft:chest",
        "pools": [
            {
                "rolls": MONEY_ROLLS,
                "bonus_rolls": 0.0,
                "entries": money_entries,
            },
            {
                "rolls": ITEM_ROLLS,
                "bonus_rolls": 0.0,
                "entries": item_entries,
            },
        ],
    }


def main() -> None:
    units = load_first_appearance_units()
    for lvl, items in units.items():
        print(f"unit {lvl} (first-seen): {len(items)} items")

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for old in OUT_DIR.glob("supply*.json"):
        old.unlink()

    for a in range(1, 6):
        for b in range(a, 6):
            name = f"supply{a}{b}.json"
            table = build_table(a, b, units)
            (OUT_DIR / name).write_text(
                json.dumps(table, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
            )
            items = table["pools"][1]["entries"]
            ws = [e["weight"] for e in items]
            stacked = sum(1 for e in items if "functions" in e)
            print(
                f"  {name}: items={len(items)} stacked_entries={stacked} "
                f"w=[{min(ws) if ws else 0},{max(ws) if ws else 0}] "
                f"has_w1={1 in ws}"
            )

    print(f"done -> {OUT_DIR}")


if __name__ == "__main__":
    main()
