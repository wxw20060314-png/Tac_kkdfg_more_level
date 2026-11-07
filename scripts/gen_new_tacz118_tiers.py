# -*- coding: utf-8 -*-
"""
从 TaCZ 1.2.0 默认包 jar 读取全部底枪 data/index、allow_attachments、枪匠台配方，
生成本模组四档枪械数据、index、allow_attachments、语言和四档配方。
后坐力（recoil / crawl_recoil_multiplier / recoil_push，含趴下 crawl_strength）原样拷贝，不乘档。
特殊后缀枪就地套卓越，不另开四档。
M107 卓越（毒蝎手上那把）配方会先写出再删除。

依赖：本机 Gradle 已解析的 TaCZ 1.2.0 jar。
"""
from __future__ import annotations

import copy
import json
import math
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
GUNS_OUT = ROOT / "src/main/resources/data/tacz/data/guns"
INDEX_OUT = ROOT / "src/main/resources/data/tacz/index/guns"
ALLOW_ATT_OUT = ROOT / "src/main/resources/data/tacz/tacz_tags/attachments/allow_attachments"
RUN_RECIPES = ROOT / "run/tacz/tacz_default_gun/data/tacz/recipes/gun"
RUN_DATA_GUNS = ROOT / "run/tacz/tacz_default_gun/data/tacz/data/guns"
RECIPE_OUT = ROOT / "src/main/resources/data/tacz/recipes/gun"

# True：从 TaCZ jar 同步全部「有 *_improved_data」的基础枪到 run/tacz/...（给 gen_tier_gun_recipes.py 读目录用）
# False：不写 run；跑 gen_tier_gun_recipes 前需已有 run 下基准配方，或把该脚本改为直接读 jar
SYNC_RUN_TACZ_DEFAULT_PACK = False
LANG_MOD_ZH = ROOT / "src/main/resources/assets/tacz_morelevel/lang/zh_cn.json"
LANG_MOD_EN = ROOT / "src/main/resources/assets/tacz_morelevel/lang/en_us.json"

# 官方包里没有 allow_attachments 的枪，沿用旧脚本的对照。其余缺文件的枪不写空列表。
ALLOW_FALLBACK = {
    "taurus943": "rhino357",
}
# 毒蝎枪套：M107 卓越属性超标，四档配方照常生成后再删这一份。
DROP_EXCELLENT_RECIPE = {"m107"}

PREFIX = "assets/tacz/custom/tacz_default_gun/data/tacz"
ASSET_PREFIX = "assets/tacz/custom/tacz_default_gun/assets/tacz/lang"

TIER_NAMES = ("improved", "refined", "perfect", "excellent")
TIER_SUFFIXES = tuple(f"_{name}" for name in TIER_NAMES)
TIER_RECIPE_ROWS = (
    # output_suffix, material_ratio, input_gun_id_suffix, voucher registry name
    ("improved", 0.1, "", "improved_gun_upgrade_voucher"),
    ("refined", 0.2, "_improved", "refined_gun_upgrade_voucher"),
    ("perfect", 0.3, "_refined", "perfect_gun_upgrade_voucher"),
    ("excellent", 0.4, "_perfect", "excellent_gun_upgrade_voucher"),
)
DAMAGE_MULT = (1.5, 2.0, 2.5, 3.0)
# 重力。卓越档下标 EXCELLENT_TIER_INDEX。
COMFORT_MULT = (0.85, 0.7, 0.55, 0.4)
# 腰射散布 stand/move/sneak/lie。不乘瞄准散布，也不动后坐力。
HIP_INACCURACY_MULT = (0.9, 0.8, 0.7, 0.6)
ARMOR_K = (0.5, 2.0 / 3.0, 0.75, 0.8)
EXCELLENT_TIER_INDEX = 3
# 不在基础五档（无后缀 + 上面四档）里的特殊枪，就地套卓越，不另开四档。
SPECIAL_SUFFIXES = (
    "_legendary",
    "_shockwave",
    "_annihilator",
    "_ceiling",
    "_blast",
    "_dragonbreath",
)

TIER_SUFFIX_ZH = {
    "improved": "改进",
    "refined": "精致",
    "perfect": "完美",
    "excellent": "卓越",
}
TIER_SUFFIX_EN = {
    "improved": "Improved",
    "refined": "Refined",
    "perfect": "Perfect",
    "excellent": "Excellent",
}
TIER_COLOR_TACZ = {
    "improved": "§a",
    "refined": "§9",
    "perfect": "§5",
    "excellent": "§c",
}


def find_tacz_jar() -> Path:
    base = Path.home() / ".gradle/caches/forge_gradle/deobf_dependencies/maven/modrinth/timeless-and-classics-zero"
    if not base.is_dir():
        raise SystemExit(f"Gradle deobf cache not found: {base}")
    cands = sorted(
        base.glob("**/timeless-and-classics-zero-1.2.0*.jar"),
        key=lambda p: p.stat().st_mtime,
        reverse=True,
    )
    for p in cands:
        if p.is_file() and p.name.endswith(".jar") and "mapped" in p.name:
            return p
    raise SystemExit("No timeless-and-classics-zero 1.2.0 mapped jar under forge_gradle deobf cache.")


def strip_tacz_comments(text: str) -> str:
    """去掉 // 与 /* */ 注释（字符串外），便于 json.loads。"""
    out: list[str] = []
    i, n = 0, len(text)
    in_str = False
    esc = False
    while i < n:
        c = text[i]
        if in_str:
            out.append(c)
            if esc:
                esc = False
            elif c == "\\":
                esc = True
            elif c == '"':
                in_str = False
            i += 1
            continue
        if c == '"':
            in_str = True
            out.append(c)
            i += 1
            continue
        if c == "/" and i + 1 < n and text[i + 1] == "/":
            while i < n and text[i] != "\n":
                i += 1
            continue
        if c == "/" and i + 1 < n and text[i + 1] == "*":
            i += 2
            while i + 1 < n and not (text[i] == "*" and text[i + 1] == "/"):
                i += 1
            i = min(i + 2, n)
            continue
        out.append(c)
        i += 1
    return "".join(out)


def read_jar_text(z: zipfile.ZipFile, inner: str) -> str:
    return z.read(inner).decode("utf-8-sig")


def load_json_from_jar(z: zipfile.ZipFile, inner: str) -> dict:
    return json.loads(strip_tacz_comments(read_jar_text(z, inner)))


def allow_attachments_inner(gun_id: str) -> str:
    return f"{PREFIX}/tacz_tags/attachments/allow_attachments/{gun_id}.json"


def load_allow_attachments_list_from_jar(z: zipfile.ZipFile, gun_id: str) -> list | None:
    """
    每把枪对应 tacz_tags/attachments/allow_attachments/<gun_id>.json（JSON 数组）。
    四档枪 id 为 <base>_<tier>，有基准列表时各写一份，内容与基准枪相同。
    官方包没有该文件、也没有 ALLOW_FALLBACK 时返回 None，不写空列表。
    """
    names = set(z.namelist())
    inner = allow_attachments_inner(gun_id)
    if inner not in names and gun_id in ALLOW_FALLBACK:
        inner = allow_attachments_inner(ALLOW_FALLBACK[gun_id])
    if inner not in names:
        return None
    data = json.loads(strip_tacz_comments(read_jar_text(z, inner)))
    if not isinstance(data, list):
        raise SystemExit(f"allow_attachments must be a JSON array: {inner}")
    return data


def list_base_gun_ids(z: zipfile.ZipFile) -> list[str]:
    """默认包里的底枪。已是四档后缀的 id 不当底枪。"""
    prefix = f"{PREFIX}/data/guns/"
    ids: list[str] = []
    for name in z.namelist():
        if not name.startswith(prefix) or not name.endswith("_data.json"):
            continue
        gun_id = name[len(prefix) : -len("_data.json")]
        if "/" in gun_id or gun_id.lower().endswith(TIER_SUFFIXES):
            continue
        ids.append(gun_id)
    return sorted(ids)


def write_tier_allow_attachments(allow_list: list, gun_id: str) -> None:
    ALLOW_ATT_OUT.mkdir(parents=True, exist_ok=True)
    for tier in TIER_NAMES:
        path = ALLOW_ATT_OUT / f"{gun_id}_{tier}.json"
        path.write_text(json.dumps(allow_list, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def roundf(x: float, nd: int = 4) -> float:
    return float(round(x, nd))


def is_special_variant(gun_id: str) -> bool:
    lower = gun_id.lower()
    return any(lower.endswith(suffix) for suffix in SPECIAL_SUFFIXES)


def apply_tier(gun: dict, tier_idx: int) -> None:
    dm = DAMAGE_MULT[tier_idx]
    cm = COMFORT_MULT[tier_idx]
    hip = HIP_INACCURACY_MULT[tier_idx]
    ak = ARMOR_K[tier_idx]
    head_mul = 1.0 + 0.1 * (tier_idx + 1)

    bullet = gun.get("bullet")
    if not isinstance(bullet, dict):
        return

    if isinstance(bullet.get("damage"), (int, float)):
        bullet["damage"] = roundf(float(bullet["damage"]) * dm)

    ed = bullet.get("extra_damage")
    if isinstance(ed, dict):
        v = float(ed.get("armor_ignore", 0.0))
        ed["armor_ignore"] = roundf(min(0.92, (1.0 - v) * ak + v))
        hs = float(ed.get("head_shot_multiplier", 1.5))
        ed["head_shot_multiplier"] = roundf(hs * head_mul)
        adj = ed.get("damage_adjust")
        if isinstance(adj, list):
            for row in adj:
                if isinstance(row, dict) and isinstance(row.get("damage"), (int, float)):
                    row["damage"] = roundf(float(row["damage"]) * dm)

    if isinstance(bullet.get("gravity"), (int, float)):
        bullet["gravity"] = roundf(float(bullet["gravity"]) * cm)

    inc = gun.get("inaccuracy")
    if isinstance(inc, dict):
        for key in ("stand", "move", "sneak", "lie"):
            if key in inc and isinstance(inc[key], (int, float)):
                inc[key] = roundf(float(inc[key]) * hip)


def tier_index_from_base(base_index: dict, gun_id: str, tier: str, tier_ord: int, base_sort: int) -> dict:
    out = copy.deepcopy(base_index)
    out["name"] = f"tacz.gun.{gun_id}_{tier}.name"
    out["data"] = f"tacz:{gun_id}_{tier}_data"
    out["tooltip"] = f"tacz.gun.{gun_id}_{tier}.desc"
    out["sort"] = int(base_sort) + tier_ord + 1
    return out


def scale_count(count: int, ratio: float) -> int:
    return int(math.floor(count * ratio))


def scale_materials(materials: list, ratio: float) -> list:
    out = []
    for material in materials:
        count = scale_count(int(material.get("count", 1)), ratio)
        if count <= 0:
            continue
        out.append({"item": material["item"], "count": count})
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


def write_tier_recipes(z: zipfile.ZipFile, gun_ids: list[str]) -> tuple[int, list[str], list[str]]:
    """按底枪配方的 10/20/30/40% 写四档。DROP_EXCELLENT_RECIPE 里的卓越配方写完即删。"""
    RECIPE_OUT.mkdir(parents=True, exist_ok=True)
    names = set(z.namelist())
    written = 0
    skipped: list[str] = []
    dropped: list[str] = []
    for gun_id in gun_ids:
        if is_special_variant(gun_id):
            continue
        inner = f"{PREFIX}/recipes/gun/{gun_id}.json"
        if inner not in names:
            skipped.append(f"{gun_id}: no vanilla recipe")
            continue
        base_recipe = load_json_from_jar(z, inner)
        materials_template = base_recipe.get("materials") or []
        for out_suffix, ratio, base_suffix, voucher_name in TIER_RECIPE_ROWS:
            materials = scale_materials(materials_template, ratio)
            materials.append(gun_ingredient(f"tacz:{gun_id}{base_suffix}"))
            materials.append(voucher_ingredient(voucher_name))
            recipe = {
                "type": "tacz:gun_smith_table_crafting",
                "materials": materials,
                "result": {"type": "gun", "id": f"tacz:{gun_id}_{out_suffix}"},
            }
            out_file = RECIPE_OUT / f"{gun_id}_{out_suffix}.json"
            out_file.write_text(json.dumps(recipe, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
            written += 1
            if out_suffix == "excellent" and gun_id in DROP_EXCELLENT_RECIPE:
                out_file.unlink()
                written -= 1
                dropped.append(out_file.name)
    return written, skipped, dropped


def merge_lang_file(path: Path, new_entries: dict[str, str]) -> None:
    data = json.loads(path.read_text(encoding="utf-8"))
    for k, v in new_entries.items():
        data[k] = v
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def _write_clean_json_from_jar(z: zipfile.ZipFile, inner: str, dest: Path) -> bool:
    if inner not in z.namelist():
        return False
    clean = strip_tacz_comments(read_jar_text(z, inner))
    json.loads(clean)
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(clean.strip() + "\n", encoding="utf-8")
    return True


def sync_run_tacz_default_gun_from_jar(z: zipfile.ZipFile) -> None:
    """按 src 下 *_improved_data.json 推断基础枪名，从 jar 解压到 run/tacz/tacz_default_gun。"""
    RUN_DATA_GUNS.mkdir(parents=True, exist_ok=True)
    RUN_RECIPES.mkdir(parents=True, exist_ok=True)
    bases = sorted({p.name.removesuffix("_improved_data.json") for p in GUNS_OUT.glob("*_improved_data.json")})
    n_data = n_rec = 0
    for base in bases:
        if _write_clean_json_from_jar(z, f"{PREFIX}/data/guns/{base}_data.json", RUN_DATA_GUNS / f"{base}_data.json"):
            n_data += 1
        else:
            print("WARN: no base gun data in jar:", base)
        recipe_inner = f"{PREFIX}/recipes/gun/{base}.json"
        if recipe_inner in z.namelist():
            if _write_clean_json_from_jar(z, recipe_inner, RUN_RECIPES / f"{base}.json"):
                n_rec += 1
        elif base == "taurus943":
            rh = load_json_from_jar(z, f"{PREFIX}/recipes/gun/rhino357.json")
            rh["result"] = {"type": "gun", "id": "tacz:taurus943"}
            (RUN_RECIPES / "taurus943.json").write_text(
                json.dumps(rh, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
            )
            n_rec += 1
        else:
            print("WARN: no base recipe in jar:", base)
    print(f"Synced run/tacz/tacz_default_gun: {n_data} *_data.json, {n_rec} base recipes")


def main() -> None:
    jar = find_tacz_jar()
    print("Using jar:", jar)

    GUNS_OUT.mkdir(parents=True, exist_ok=True)
    INDEX_OUT.mkdir(parents=True, exist_ok=True)

    lang_mod_zh: dict[str, str] = {}
    lang_mod_en: dict[str, str] = {}

    with zipfile.ZipFile(jar, "r") as z:
        if SYNC_RUN_TACZ_DEFAULT_PACK:
            sync_run_tacz_default_gun_from_jar(z)
        else:
            print("Skip run/tacz sync (SYNC_RUN_TACZ_DEFAULT_PACK=False).")

        jar_lang_zh = load_json_from_jar(z, f"{ASSET_PREFIX}/zh_cn.json")
        jar_lang_en = load_json_from_jar(z, f"{ASSET_PREFIX}/en_us.json")
        gun_ids = list_base_gun_ids(z)
        print("Base guns in jar:", len(gun_ids))

        missing_allow: list[str] = []
        for gun_id in gun_ids:
            data_path = f"{PREFIX}/data/guns/{gun_id}_data.json"
            idx_path = f"{PREFIX}/index/guns/{gun_id}.json"

            allow_att = load_allow_attachments_list_from_jar(z, gun_id)
            if allow_att is None:
                missing_allow.append(gun_id)

            base_gun = load_json_from_jar(z, data_path)
            base_index = load_json_from_jar(z, idx_path)
            base_sort = int(base_index.get("sort", 100))

            if is_special_variant(gun_id):
                g = copy.deepcopy(base_gun)
                apply_tier(g, EXCELLENT_TIER_INDEX)
                out_gun = GUNS_OUT / f"{gun_id}_data.json"
                out_gun.write_text(json.dumps(g, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
                out_idx = INDEX_OUT / f"{gun_id}.json"
                out_idx.write_text(
                    json.dumps(base_index, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
                )
                if allow_att is not None:
                    ALLOW_ATT_OUT.mkdir(parents=True, exist_ok=True)
                    allow_path = ALLOW_ATT_OUT / f"{gun_id}.json"
                    allow_path.write_text(
                        json.dumps(allow_att, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
                    )
                continue

            if allow_att is not None:
                write_tier_allow_attachments(allow_att, gun_id)

            base_name_zh = jar_lang_zh.get(f"tacz.gun.{gun_id}.name", gun_id)
            base_name_en = jar_lang_en.get(f"tacz.gun.{gun_id}.name", gun_id)
            base_desc_zh = jar_lang_zh.get(f"tacz.gun.{gun_id}.desc", "")
            base_desc_en = jar_lang_en.get(f"tacz.gun.{gun_id}.desc", "")

            for ti, tier in enumerate(TIER_NAMES):
                g = copy.deepcopy(base_gun)
                apply_tier(g, ti)
                out_gun = GUNS_OUT / f"{gun_id}_{tier}_data.json"
                out_gun.write_text(json.dumps(g, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

                idx = tier_index_from_base(base_index, gun_id, tier, ti, base_sort)
                out_idx = INDEX_OUT / f"{gun_id}_{tier}.json"
                out_idx.write_text(json.dumps(idx, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

                suf_zh = TIER_SUFFIX_ZH[tier]
                suf_en = TIER_SUFFIX_EN[tier]
                col = TIER_COLOR_TACZ[tier]

                lang_mod_zh[f"tacz.gun.{gun_id}_{tier}.name"] = f"{col}§o{base_name_zh}{suf_zh}§r"
                lang_mod_zh[f"tacz.gun.{gun_id}_{tier}.desc"] = base_desc_zh
                lang_mod_en[f"tacz.gun.{gun_id}_{tier}.name"] = f"{col}§o{base_name_en} {suf_en}§r"
                lang_mod_en[f"tacz.gun.{gun_id}_{tier}.desc"] = base_desc_en

        recipe_written, recipe_skipped, recipe_dropped = write_tier_recipes(z, gun_ids)

    merge_lang_file(LANG_MOD_ZH, lang_mod_zh)
    merge_lang_file(LANG_MOD_EN, lang_mod_en)
    print(
        "Wrote tier data, index, lang for",
        len(gun_ids),
        "guns.",
        "(run/tacz synced)" if SYNC_RUN_TACZ_DEFAULT_PACK else "(run/tacz sync skipped)",
    )
    if missing_allow:
        print("No allow_attachments (left unset):", ", ".join(missing_allow))
    print(f"Wrote {recipe_written} recipes to {RECIPE_OUT}")
    if recipe_dropped:
        print("Deleted excellent recipes:", ", ".join(recipe_dropped))
    if recipe_skipped:
        print("Skipped recipes:")
        for line in recipe_skipped:
            print(" ", line)


if __name__ == "__main__":
    main()
