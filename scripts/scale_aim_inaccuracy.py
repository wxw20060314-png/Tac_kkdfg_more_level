"""Scale only inaccuracy.aim by gun quality tier. Do not touch other fields."""
from __future__ import annotations

import re
from pathlib import Path

GUNS = Path(__file__).resolve().parents[1] / "src/main/resources/data/tacz/data/guns"

TIER_MULT = {
    "improved": 0.85,
    "refined": 0.7,
    "perfect": 0.55,
    "excellent": 0.4,
}

SPECIAL_KINDS = ("legendary", "blast", "annihilator", "ceiling")


def format_num(x: float) -> str:
    return f"{x:.6f}".rstrip("0").rstrip(".")


def extract_inaccuracy_aim(text: str) -> float | None:
    m = re.search(r'"inaccuracy"\s*:\s*\{([^{}]*)\}', text, re.S)
    if not m:
        return None
    am = re.search(r'"aim"\s*:\s*(-?\d+(?:\.\d+)?)', m.group(1))
    return float(am.group(1)) if am else None


def set_inaccuracy_aim(text: str, new_val: float) -> str:
    def repl(m: re.Match[str]) -> str:
        block = m.group(1)
        new_block, n = re.subn(
            r'("aim"\s*:\s*)(-?\d+(?:\.\d+)?)',
            lambda mm: mm.group(1) + format_num(new_val),
            block,
            count=1,
        )
        if n != 1:
            raise ValueError("aim replace failed in inaccuracy")
        return '"inaccuracy": {' + new_block + "}"

    new_text, n = re.subn(
        r'"inaccuracy"\s*:\s*\{([^{}]*)\}',
        repl,
        text,
        count=1,
        flags=re.S,
    )
    if n != 1:
        raise ValueError("inaccuracy block not found")
    return new_text


def movement_speed_aim(text: str) -> str | None:
    m = re.search(r'"movement_speed"\s*:\s*\{([^{}]*)\}', text, re.S)
    if not m:
        return None
    am = re.search(r'"aim"\s*:\s*(-?\d+(?:\.\d+)?)', m.group(1))
    return am.group(1) if am else None


def aim_time(text: str) -> str | None:
    m = re.search(r'"aim_time"\s*:\s*(-?\d+(?:\.\d+)?)', text)
    return m.group(1) if m else None


def main() -> None:
    tier_files: dict[str, dict[str, Path]] = {}
    special_files: list[tuple[Path, str, str]] = []

    for path in sorted(GUNS.glob("*_data.json")):
        stem = path.name[: -len("_data.json")]
        matched = False
        for tier in TIER_MULT:
            suf = "_" + tier
            if stem.endswith(suf):
                base = stem[: -len(suf)]
                tier_files.setdefault(base, {})[tier] = path
                matched = True
                break
        if matched:
            continue
        for kind in SPECIAL_KINDS:
            suf = "_" + kind
            if stem.endswith(suf):
                base = stem[: -len(suf)]
                special_files.append((path, base, kind))
                matched = True
                break
        if not matched:
            print("SKIP non-tier:", path.name)

    ref_aim: dict[str, float] = {}
    for base, td in tier_files.items():
        aims: dict[str, float] = {}
        for tier, path in td.items():
            aim = extract_inaccuracy_aim(path.read_text(encoding="utf-8"))
            if aim is None:
                print("NO AIM", path.name)
                continue
            aims[tier] = aim
        if not aims:
            continue
        vals = set(aims.values())
        if len(vals) == 1:
            ref = next(iter(vals))
        else:
            ref = aims.get("improved") or aims.get("refined") or max(aims.values())
            print("WARN varied aims", base, aims, "ref", ref)
        ref_aim[base] = ref

    changed = 0
    errors: list[str] = []

    for base, td in sorted(tier_files.items()):
        if base not in ref_aim:
            continue
        ref = ref_aim[base]
        for tier, path in td.items():
            new_aim = ref * TIER_MULT[tier]
            text = path.read_text(encoding="utf-8")
            old = extract_inaccuracy_aim(text)
            if old is None:
                errors.append(path.name)
                continue
            if abs(old - new_aim) < 1e-9:
                continue
            try:
                new_text = set_inaccuracy_aim(text, new_aim)
            except Exception as e:
                errors.append(f"{path.name}: {e}")
                continue
            if movement_speed_aim(text) != movement_speed_aim(new_text):
                errors.append(f"{path.name}: movement_speed.aim changed!")
                continue
            if aim_time(text) != aim_time(new_text):
                errors.append(f"{path.name}: aim_time changed!")
                continue
            path.write_text(new_text, encoding="utf-8", newline="\n")
            changed += 1
            print(f"{path.name}: {old} -> {format_num(new_aim)}")

    for path, base, _kind in sorted(special_files):
        exc = tier_files.get(base, {}).get("excellent")
        if exc:
            new_aim = extract_inaccuracy_aim(exc.read_text(encoding="utf-8"))
            if new_aim is None:
                errors.append(f"{path.name}: excellent has no aim")
                continue
        elif base in ref_aim:
            new_aim = ref_aim[base] * 0.4
        else:
            errors.append(f"{path.name}: no excellent/ref for {base}")
            continue

        text = path.read_text(encoding="utf-8")
        old = extract_inaccuracy_aim(text)
        if old is None:
            errors.append(path.name)
            continue
        if abs(old - new_aim) < 1e-9:
            print(f"{path.name}: already {format_num(new_aim)}")
            continue
        try:
            new_text = set_inaccuracy_aim(text, new_aim)
        except Exception as e:
            errors.append(f"{path.name}: {e}")
            continue
        if movement_speed_aim(text) != movement_speed_aim(new_text):
            errors.append(f"{path.name}: movement_speed.aim changed!")
            continue
        if aim_time(text) != aim_time(new_text):
            errors.append(f"{path.name}: aim_time changed!")
            continue
        path.write_text(new_text, encoding="utf-8", newline="\n")
        changed += 1
        print(f"{path.name}: {old} -> {format_num(new_aim)} (align excellent)")

    print("---")
    print("changed", changed)
    print("families", len(ref_aim))
    print("specials", len(special_files))
    print("errors", errors)


if __name__ == "__main__":
    main()
