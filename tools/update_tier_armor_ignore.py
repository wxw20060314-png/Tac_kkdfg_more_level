"""
Rewrite bullet.extra_damage.armor_ignore in *_improved/refined/perfect/excellent_data.json
using base gun penetration x from TaCZ default pack:

  tier_n = 1 - (1 - x) * (100 - 20*n) / 100   for n = 1..4

  improved -> n=1, refined -> n=2, perfect -> n=3, excellent -> n=4

Only the main path bullet.extra_damage.armor_ignore is updated (not fire_mode_adjust etc.).
"""
from __future__ import annotations

import json
import sys
from pathlib import Path


def tier_armor_ignore(x: float, n: int) -> float:
    v = 1.0 - (1.0 - x) * (100.0 - 20.0 * n) / 100.0
    return round(v, 8)


def strip_jsonc(text: str) -> str:
    """Strip // and /* */ comments outside of quoted strings (TaCZ run configs are JSONC)."""
    result: list[str] = []
    i = 0
    n = len(text)
    in_string = False
    escape = False
    while i < n:
        c = text[i]
        if escape:
            result.append(c)
            escape = False
            i += 1
            continue
        if in_string:
            if c == "\\":
                escape = True
                result.append(c)
            elif c == '"':
                in_string = False
                result.append(c)
            else:
                result.append(c)
            i += 1
            continue
        if c == '"':
            in_string = True
            result.append(c)
            i += 1
            continue
        if c == "/" and i + 1 < n:
            nxt = text[i + 1]
            if nxt == "/":
                i += 2
                while i < n and text[i] not in "\r\n":
                    i += 1
                continue
            if nxt == "*":
                i += 2
                while i + 1 < n and not (text[i] == "*" and text[i + 1] == "/"):
                    i += 1
                if i + 1 < n:
                    i += 2
                continue
        result.append(c)
        i += 1
    return "".join(result)


def load_base_x(base_json: Path) -> float:
    raw = base_json.read_text(encoding="utf-8")
    data = json.loads(strip_jsonc(raw))
    return float(data["bullet"]["extra_damage"]["armor_ignore"])


def set_main_armor_ignore(data: object, value: float) -> bool:
    if not isinstance(data, dict):
        return False
    bullet = data.get("bullet")
    if not isinstance(bullet, dict):
        return False
    extra = bullet.get("extra_damage")
    if not isinstance(extra, dict):
        return False
    if "armor_ignore" not in extra:
        return False
    extra["armor_ignore"] = value
    return True


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    base_dir = root / "run/tacz/tacz_default_gun/data/tacz/data/guns"
    guns_dir = root / "src/main/resources/data/tacz/data/guns"

    if not base_dir.is_dir():
        print(f"Missing base gun folder: {base_dir}", file=sys.stderr)
        return 1

    improved = sorted(guns_dir.glob("*_improved_data.json"))
    if not improved:
        print(f"No *_improved_data.json under {guns_dir}", file=sys.stderr)
        return 1

    updated_files = 0
    for imp_path in improved:
        stem = imp_path.name
        if not stem.endswith("_improved_data.json"):
            continue
        prefix = stem[: -len("_improved_data.json")]
        base_path = base_dir / f"{prefix}_data.json"
        if not base_path.is_file():
            print(f"skip (no base): {prefix}", file=sys.stderr)
            continue

        x = load_base_x(base_path)
        tiers = {
            "improved": tier_armor_ignore(x, 1),
            "refined": tier_armor_ignore(x, 2),
            "perfect": tier_armor_ignore(x, 3),
            "excellent": tier_armor_ignore(x, 4),
        }

        for name, val in tiers.items():
            tier_path = guns_dir / f"{prefix}_{name}_data.json"
            if not tier_path.is_file():
                print(f"missing tier file: {tier_path.name}", file=sys.stderr)
                continue
            with tier_path.open(encoding="utf-8") as f:
                data = json.load(f)
            if not set_main_armor_ignore(data, val):
                print(f"no bullet.extra_damage.armor_ignore in {tier_path.name}", file=sys.stderr)
                continue
            with tier_path.open("w", encoding="utf-8", newline="\n") as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
                f.write("\n")
            updated_files += 1
        print(f"{prefix}: x={x} -> {tiers}")

    print(f"Wrote {updated_files} tier JSON files.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
