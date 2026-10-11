# -*- coding: utf-8 -*-
"""
从 TaCZ 1.1.8 默认包 jar 读取新枪基础 data/index、allow_attachments 基准列表，
生成本模组四档枪械数据、index、四档 allow_attachments 标签（与原版同槽位）、
合并语言条目（仅写入 tacz_morelevel 的 lang，避免与 assets/tacz/lang 重复键导致显示不一致）。
可选：将基准枪 data/recipes 同步到 run/tacz/tacz_default_gun（见下方开关）。

依赖：本机 Gradle 已解析的 deobf TaCZ jar（ForgeGradle 缓存）。
"""
from __future__ import annotations

import copy
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
GUNS_OUT = ROOT / "src/main/resources/data/tacz/data/guns"
INDEX_OUT = ROOT / "src/main/resources/data/tacz/index/guns"
ALLOW_ATT_OUT = ROOT / "src/main/resources/data/tacz/tacz_tags/attachments/allow_attachments"
RUN_RECIPES = ROOT / "run/tacz/tacz_default_gun/data/tacz/recipes/gun"
RUN_DATA_GUNS = ROOT / "run/tacz/tacz_default_gun/data/tacz/data/guns"

# True：从 TaCZ jar 同步全部「有 *_improved_data」的基础枪到 run/tacz/...（给 gen_tier_gun_recipes.py 读目录用）
# False：不写 run；跑 gen_tier_gun_recipes 前需已有 run 下基准配方，或把该脚本改为直接读 jar
SYNC_RUN_TACZ_DEFAULT_PACK = False
LANG_MOD_ZH = ROOT / "src/main/resources/assets/tacz_morelevel/lang/zh_cn.json"
LANG_MOD_EN = ROOT / "src/main/resources/assets/tacz_morelevel/lang/en_us.json"

# TaCZ 1.1.8 新增/需纳入 NPC 池与四档数据的枪械（与 TaczOrdinaryGunCatalog 一致）
NEW_GUN_IDS = [
    "fn_evolys",
    "fn_fal",
    "hk_mk23",
    "kar98",
    "lonetrail",
    "m1014",
    "m9a4",
    "qbz_191",
    "rhino357",
    "spas_12",
    "taurus500",
    "taurus943",
]

PREFIX = "assets/tacz/custom/tacz_default_gun/data/tacz"
ASSET_PREFIX = "assets/tacz/custom/tacz_default_gun/assets/tacz/lang"

TIER_NAMES = ("improved", "refined", "perfect", "excellent")
DAMAGE_MULT = (1.5, 2.0, 2.5, 3.0)
COMFORT_MULT = (0.85, 0.7, 0.55, 0.4)
ARMOR_K = (0.5, 2.0 / 3.0, 0.75, 0.8)

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
        base.glob("**/timeless-and-classics-zero-1.1.8*.jar"),
        key=lambda p: p.stat().st_mtime,
        reverse=True,
    )
    for p in cands:
        if p.is_file() and p.name.endswith(".jar") and "mapped" in p.name:
            return p
    raise SystemExit("No timeless-and-classics-zero 1.1.8 mapped jar under forge_gradle deobf cache.")


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


def load_json_from_jar(z: zipfile.ZipFile, inner: str) -> dict:
    raw = z.read(inner).decode("utf-8")
    return json.loads(strip_tacz_comments(raw))


def load_allow_attachments_list_from_jar(z: zipfile.ZipFile, gun_id: str) -> list:
    """
    TaCZ 1.1.8：每把枪对应 tacz_tags/attachments/allow_attachments/<gun_id>.json（JSON 数组）。
    四档枪 id 为 <base>_<tier>，需为每个 tier 各写一份，内容与基准枪相同。
    """
    inner = f"{PREFIX}/tacz_tags/attachments/allow_attachments/{gun_id}.json"
    if inner not in z.namelist():
        if gun_id == "taurus943":
            inner = f"{PREFIX}/tacz_tags/attachments/allow_attachments/rhino357.json"
    if inner not in z.namelist():
        raise SystemExit(f"allow_attachments not in jar for {gun_id!r} (tried {inner})")
    raw = z.read(inner).decode("utf-8")
    data = json.loads(strip_tacz_comments(raw))
    if not isinstance(data, list):
        raise SystemExit(f"allow_attachments must be a JSON array: {inner}")
    return data


def write_tier_allow_attachments(allow_list: list, gun_id: str) -> None:
    ALLOW_ATT_OUT.mkdir(parents=True, exist_ok=True)
    for tier in TIER_NAMES:
        path = ALLOW_ATT_OUT / f"{gun_id}_{tier}.json"
        path.write_text(json.dumps(allow_list, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def roundf(x: float, nd: int = 4) -> float:
    return float(round(x, nd))


def apply_tier(gun: dict, tier_idx: int) -> None:
    dm = DAMAGE_MULT[tier_idx]
    cm = COMFORT_MULT[tier_idx]
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
                inc[key] = roundf(float(inc[key]) * cm)


def tier_index_from_base(base_index: dict, gun_id: str, tier: str, tier_ord: int, base_sort: int) -> dict:
    out = copy.deepcopy(base_index)
    out["name"] = f"tacz.gun.{gun_id}_{tier}.name"
    out["data"] = f"tacz:{gun_id}_{tier}_data"
    out["tooltip"] = f"tacz.gun.{gun_id}_{tier}.desc"
    out["sort"] = int(base_sort) + tier_ord + 1
    return out


def merge_lang_file(path: Path, new_entries: dict[str, str]) -> None:
    data = json.loads(path.read_text(encoding="utf-8"))
    for k, v in new_entries.items():
        data[k] = v
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def _write_clean_json_from_jar(z: zipfile.ZipFile, inner: str, dest: Path) -> bool:
    if inner not in z.namelist():
        return False
    raw = z.read(inner).decode("utf-8")
    clean = strip_tacz_comments(raw)
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

        for gun_id in NEW_GUN_IDS:
            data_path = f"{PREFIX}/data/guns/{gun_id}_data.json"
            idx_path = f"{PREFIX}/index/guns/{gun_id}.json"

            allow_att = load_allow_attachments_list_from_jar(z, gun_id)
            write_tier_allow_attachments(allow_att, gun_id)

            base_gun = load_json_from_jar(z, data_path)
            base_index = load_json_from_jar(z, idx_path)
            base_sort = int(base_index.get("sort", 100))

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

    merge_lang_file(LANG_MOD_ZH, lang_mod_zh)
    merge_lang_file(LANG_MOD_EN, lang_mod_en)
    print(
        "Wrote tier data, index, lang for",
        len(NEW_GUN_IDS),
        "guns.",
        "(run/tacz synced)" if SYNC_RUN_TACZ_DEFAULT_PACK else "(run/tacz sync skipped)",
    )


if __name__ == "__main__":
    main()
