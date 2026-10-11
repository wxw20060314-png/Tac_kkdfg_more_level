# -*- coding: utf-8 -*-
from __future__ import annotations

import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
GUNS_DIR = ROOT / "src/main/resources/data/tacz/data/guns"
PREFIX = "assets/tacz/custom/tacz_default_gun/data/tacz"
TIERS = ("improved", "refined", "perfect", "excellent")

# mod gun id -> jar base gun data stem
SYNC = {
    "aa12": "aa12",
    "m870": "m870",
    "aug": "aug",
    "taurus943": "rhino357",
}
SPECIALS = {
    "aa12_shockwave": "aa12",
    "taurus943_legendary": "rhino357",
}


def find_tacz_jar() -> Path:
    base = (
        Path.home()
        / ".gradle/caches/forge_gradle/deobf_dependencies/maven/modrinth/timeless-and-classics-zero"
    )
    cands = sorted(
        base.glob("**/timeless-and-classics-zero-1.1.8*.jar"),
        key=lambda p: p.stat().st_mtime,
        reverse=True,
    )
    for p in cands:
        if p.is_file() and "mapped" in p.name:
            return p
    raise SystemExit("No TaCZ 1.1.8 mapped jar found")


def strip_json_comments(text: str) -> str:
    out: list[str] = []
    i, n = 0, len(text)
    in_str = False
    while i < n:
        c = text[i]
        if in_str:
            out.append(c)
            if c == "\\" and i + 1 < n:
                out.append(text[i + 1])
                i += 2
                continue
            if c == '"':
                in_str = False
            i += 1
            continue
        if c == '"':
            in_str = True
            out.append(c)
            i += 1
            continue
        if c == "/" and i + 1 < n and text[i + 1] == "/":
            i += 2
            while i < n and text[i] not in "\r\n":
                i += 1
            continue
        if c == "/" and i + 1 < n and text[i + 1] == "*":
            i += 2
            while i + 1 < n and not (text[i] == "*" and text[i + 1] == "/"):
                i += 1
            i += 2
            continue
        out.append(c)
        i += 1
    return "".join(out)


def load_types(z: zipfile.ZipFile, base_id: str) -> list:
    path = f"{PREFIX}/data/guns/{base_id}_data.json"
    raw = z.read(path).decode("utf-8")
    data = json.loads(strip_json_comments(raw))
    types = data.get("allow_attachment_types")
    if not isinstance(types, list):
        raise SystemExit(f"no allow_attachment_types in jar {path}")
    return types


def patch_file(path: Path, types: list) -> bool:
    text = path.read_text(encoding="utf-8")
    data = json.loads(text)
    old = data.get("allow_attachment_types")
    if old == types:
        return False
    data["allow_attachment_types"] = types
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    return True


def main() -> None:
    jar = find_tacz_jar()
    print("jar:", jar)
    changed = 0
    with zipfile.ZipFile(jar) as z:
        base_types = {k: load_types(z, v) for k, v in {**SYNC, **{v: v for v in SYNC.values()}}.items()}
        # clearer: map jar id -> types
        jar_types = {jid: load_types(z, jid) for jid in set(SYNC.values())}
        for gun, jar_id in SYNC.items():
            types = jar_types[jar_id]
            print(gun, "<-", jar_id, types)
            for tier in TIERS:
                path = GUNS_DIR / f"{gun}_{tier}_data.json"
                if not path.is_file():
                    print("  missing", path.name)
                    continue
                if patch_file(path, types):
                    print("  updated", path.name)
                    changed += 1
                else:
                    print("  ok", path.name)
        for special, jar_id in SPECIALS.items():
            types = jar_types[jar_id]
            path = GUNS_DIR / f"{special}_data.json"
            print(special, "<-", jar_id, types)
            if not path.is_file():
                print("  missing", path.name)
                continue
            if patch_file(path, types):
                print("  updated", path.name)
                changed += 1
            else:
                print("  ok", path.name)
    print("changed", changed)


if __name__ == "__main__":
    main()
