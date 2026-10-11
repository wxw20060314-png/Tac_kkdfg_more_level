# -*- coding: utf-8 -*-
"""
将本模组四档枪的 allow_attachments 与 TaCZ 1.1.8 默认包原样对齐。
只更新 *_improved/refined/perfect/excellent.json；特殊枪（ceiling/blast 等）不动。
"""
from __future__ import annotations

import json
import re
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ALLOW_OUT = ROOT / "src/main/resources/data/tacz/tacz_tags/attachments/allow_attachments"
PREFIX = "assets/tacz/custom/tacz_default_gun/data/tacz"
TIERS = ("improved", "refined", "perfect", "excellent")
TIER_RE = re.compile(r"^(.+)_(" + "|".join(TIERS) + r")\.json$")

# jar 缺省时的回退（与 gen_new_tacz118_tiers.py 一致）
FALLBACK_GUN = {"taurus943": "rhino357"}


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
        if p.is_file() and "mapped" in p.name:
            return p
    raise SystemExit("No timeless-and-classics-zero 1.1.8 mapped jar under forge_gradle deobf cache.")


def strip_tacz_comments(text: str) -> str:
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


def load_allow(z: zipfile.ZipFile, gun_id: str) -> list:
    candidates = [gun_id]
    if gun_id in FALLBACK_GUN:
        candidates.append(FALLBACK_GUN[gun_id])
    for gid in candidates:
        inner = f"{PREFIX}/tacz_tags/attachments/allow_attachments/{gid}.json"
        if inner not in z.namelist():
            continue
        data = json.loads(strip_tacz_comments(z.read(inner).decode("utf-8")))
        if not isinstance(data, list) or not data:
            raise SystemExit(f"empty/invalid allow_attachments in jar: {inner}")
        return data
    raise SystemExit(f"allow_attachments missing in jar for {gun_id!r}")


def main() -> None:
    jar = find_tacz_jar()
    bases: set[str] = set()
    for path in ALLOW_OUT.glob("*.json"):
        m = TIER_RE.match(path.name)
        if m:
            bases.add(m.group(1))

    if not bases:
        raise SystemExit(f"No tier allow_attachments under {ALLOW_OUT}")

    updated = 0
    with zipfile.ZipFile(jar) as z:
        for gun_id in sorted(bases):
            allow_list = load_allow(z, gun_id)
            text = json.dumps(allow_list, ensure_ascii=False, indent=2) + "\n"
            for tier in TIERS:
                path = ALLOW_OUT / f"{gun_id}_{tier}.json"
                if not path.is_file():
                    continue
                old = path.read_text(encoding="utf-8")
                if old != text:
                    path.write_text(text, encoding="utf-8")
                    updated += 1
            print(f"OK {gun_id}: {len(allow_list)} tags")

    print(f"Done. Updated {updated} files from {jar.name}")


if __name__ == "__main__":
    main()
