import os
from pathlib import Path
from collections import defaultdict

root = Path(r"e:\JAVAproject\Tac_kkdfg_more_level")
out_lines = []

def count_lines(path):
    try:
        with open(path, "r", encoding="utf-8", errors="replace") as f:
            return sum(1 for _ in f)
    except Exception:
        return 0

java_root = root / "src" / "main" / "java"
java_base = java_root / "com" / "kkdfg" / "Tacz_morelevel"
java_files = list(java_root.rglob("*.java")) if java_root.exists() else []
pkg_breakdown = defaultdict(lambda: {"files": 0, "lines": 0})
java_file_lines = []

for jf in java_files:
    lines = count_lines(jf)
    java_file_lines.append((jf, lines))
    try:
        rel = jf.relative_to(java_base)
        parts = rel.parts
        subpkg = parts[0] if parts else "(root)"
    except ValueError:
        subpkg = "(outside Tacz_morelevel)"
    pkg_breakdown[subpkg]["files"] += 1
    pkg_breakdown[subpkg]["lines"] += lines

total_java_files = len(java_files)
total_java_lines = sum(l for _, l in java_file_lines)

out_lines.append("=" * 60)
out_lines.append("CODE STATISTICS - Tac_kkdfg_more_level")
out_lines.append("=" * 60)
out_lines.append("")
out_lines.append("1. Java: src/main/java/**/*.java")
out_lines.append(f"   Total files: {total_java_files}")
out_lines.append(f"   Total lines: {total_java_lines}")
out_lines.append("   Breakdown by first subpackage under com/kkdfg/Tacz_morelevel:")
for subpkg in sorted(pkg_breakdown.keys(), key=lambda x: (-pkg_breakdown[x]["lines"], x)):
    d = pkg_breakdown[subpkg]
    out_lines.append(f"     - {subpkg}: {d['files']} files, {d['lines']} lines")

res_base = root / "src" / "main" / "resources"
json_sections = {
    "data/tacz": res_base / "data" / "tacz",
    "data/tacz_morelevel": res_base / "data" / "tacz_morelevel",
    "assets/tacz_morelevel": res_base / "assets" / "tacz_morelevel",
}
out_lines.append("")
out_lines.append("2. JSON: src/main/resources")
covered = set()
for label, base in json_sections.items():
    files = list(base.rglob("*.json")) if base.exists() else []
    for f in files:
        covered.add(f.resolve())
    total_l = sum(count_lines(f) for f in files)
    out_lines.append(f"   {label}: {len(files)} files, {total_l} lines")

all_res_json = list(res_base.rglob("*.json")) if res_base.exists() else []
other = [f for f in all_res_json if f.resolve() not in covered]
if other:
    ol = sum(count_lines(f) for f in other)
    out_lines.append(f"   (other under resources): {len(other)} files, {ol} lines")

scripts_dir = root / "scripts"
out_lines.append("")
out_lines.append("3. scripts/")
if scripts_dir.exists():
    script_files = [f for f in scripts_dir.rglob("*") if f.is_file()]
    total_sl = sum(count_lines(sf) for sf in script_files)
    by_ext = defaultdict(lambda: {"files": 0, "lines": 0})
    for sf in script_files:
        ext = sf.suffix.lower() if sf.suffix else "(no ext)"
        by_ext[ext]["files"] += 1
        by_ext[ext]["lines"] += count_lines(sf)
    out_lines.append(f"   Total files: {len(script_files)}")
    out_lines.append(f"   Total lines (all files): {total_sl}")
    for ext in sorted(by_ext.keys()):
        d = by_ext[ext]
        out_lines.append(f"     {ext}: {d['files']} files, {d['lines']} lines")
else:
    out_lines.append("   (no scripts directory)")

out_lines.append("")
out_lines.append("4. Top 10 largest Java files (by line count)")
top10 = sorted(java_file_lines, key=lambda x: -x[1])[:10]
for i, (jf, lines) in enumerate(top10, 1):
    disp = jf.relative_to(root)
    out_lines.append(f"   {i:2}. {lines:5} lines  {disp}")

out_lines.append("")
out_lines.append("=" * 60)

text = "\n".join(out_lines)
out_path = root / ".code_stats.txt"
out_path.write_text(text, encoding="utf-8")
print(text)
