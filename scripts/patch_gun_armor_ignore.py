"""One-off: set bullet.extra_damage.armor_ignore on tiered gun data from base x and multipliers."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main/resources/data/tacz/data/guns"
DEFAULT = ROOT / "run/tacz/tacz_default_gun/data/tacz/data/guns"

MULT = {"improved": 0.8, "refined": 0.6, "perfect": 0.4, "excellent": 0.2}
PAT = re.compile(r"^(.+)_(improved|refined|perfect|excellent)_data\.json$")
# TaCZ 默认包里的 gun data 常带尾逗号等，不能用 strict json；取首个 armor_ignore 数值。
_ARMOR_IGNORE_RE = re.compile(r'"armor_ignore"\s*:\s*([0-9.]+)')


def armor_ignore_for(x: float, m: float) -> float:
    return round(1.0 - (1.0 - x) * m, 6)


def load_x(basename: str) -> float:
    for d in (DEFAULT, SRC):
        p = d / f"{basename}_data.json"
        if not p.is_file():
            continue
        text = p.read_text(encoding="utf-8")
        m = _ARMOR_IGNORE_RE.search(text)
        if not m:
            raise ValueError(f"no armor_ignore in {p}")
        return float(m.group(1))
    raise FileNotFoundError(basename)


def main() -> None:
    missing_base: list[str] = []
    updated = 0
    for path in sorted(SRC.glob("*_data.json")):
        m = PAT.match(path.name)
        if not m:
            continue
        base, tier = m.group(1), m.group(2)
        try:
            x = load_x(base)
        except FileNotFoundError:
            missing_base.append(base)
            continue
        new_ai = armor_ignore_for(x, MULT[tier])
        with open(path, encoding="utf-8") as f:
            j = json.load(f)
        j["bullet"]["extra_damage"]["armor_ignore"] = new_ai
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            json.dump(j, f, indent=2)
            f.write("\n")
        updated += 1
    print("updated", updated, "files")
    if missing_base:
        print("MISSING BASE:", sorted(set(missing_base)))


if __name__ == "__main__":
    main()
