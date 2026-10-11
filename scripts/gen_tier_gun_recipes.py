# -*- coding: utf-8 -*-
"""生成改进/精致/完美/卓越枪械枪匠台配方（依赖 run/tacz 下默认包原版配方为基准 x）。"""
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE_GUN_RECIPES = ROOT / "run/tacz/tacz_default_gun/data/tacz/recipes/gun"
GUNS_DATA = ROOT / "src/main/resources/data/tacz/data/guns"
OUT_DIR = ROOT / "src/main/resources/data/tacz/recipes/gun"


def scale_count(c: int, ratio: float) -> int:
    return int(math.floor(c * ratio))


def scale_materials(materials: list, ratio: float) -> list:
    out = []
    for m in materials:
        n = scale_count(m.get("count", 1), ratio)
        if n <= 0:
            continue
        out.append({"item": m["item"], "count": n})
    return out


def gun_ingredient(gun_id: str) -> dict:
    return {
        "item": {
            "type": "forge:partial_nbt",
            "item": "tacz:modern_kinetic_gun",
            "nbt": {"GunId": gun_id},
        },
        "count": 1,
    }


def voucher_ingredient(voucher_path: str) -> dict:
    return {
        "item": {"item": f"tacz_morelevel:{voucher_path}"},
        "count": 1,
    }


def main() -> None:
    if not BASE_GUN_RECIPES.is_dir():
        raise SystemExit(f"Missing default gun recipes: {BASE_GUN_RECIPES}")

    improved_data_files = list(GUNS_DATA.glob("*_improved_data.json"))
    if not improved_data_files:
        raise SystemExit(f"No *_improved_data.json in {GUNS_DATA}")

    OUT_DIR.mkdir(parents=True, exist_ok=True)

    tier_rows = [
        # output_suffix, material_ratio, input_gun_id_suffix, voucher registry name (without modid)
        ("improved", 0.1, "", "improved_gun_upgrade_voucher"),
        ("refined", 0.2, "_improved", "refined_gun_upgrade_voucher"),
        ("perfect", 0.3, "_refined", "perfect_gun_upgrade_voucher"),
        ("excellent", 0.4, "_perfect", "excellent_gun_upgrade_voucher"),
    ]

    written = 0
    skipped = []

    for imp_path in sorted(improved_data_files):
        base = imp_path.name.removesuffix("_improved_data.json")
        base_recipe_file = BASE_GUN_RECIPES / f"{base}.json"
        if not base_recipe_file.is_file():
            skipped.append(f"{base}: no vanilla recipe {base_recipe_file.name}")
            continue

        with base_recipe_file.open(encoding="utf-8") as f:
            base_recipe = json.load(f)

        materials_template = base_recipe.get("materials") or []

        for out_suffix, ratio, base_suffix, voucher_name in tier_rows:
            materials = scale_materials(materials_template, ratio)
            input_gun_id = f"tacz:{base}{base_suffix}"
            output_gun_id = f"tacz:{base}_{out_suffix}"

            materials.append(gun_ingredient(input_gun_id))
            materials.append(voucher_ingredient(voucher_name))

            recipe = {
                "type": "tacz:gun_smith_table_crafting",
                "materials": materials,
                "result": {"type": "gun", "id": output_gun_id},
            }

            out_file = OUT_DIR / f"{base}_{out_suffix}.json"
            with out_file.open("w", encoding="utf-8", newline="\n") as f:
                json.dump(recipe, f, ensure_ascii=False, indent=2)
                f.write("\n")
            written += 1

    print(f"Wrote {written} recipes to {OUT_DIR}")
    if skipped:
        print("Skipped:")
        for s in skipped:
            print(" ", s)


if __name__ == "__main__":
    main()
