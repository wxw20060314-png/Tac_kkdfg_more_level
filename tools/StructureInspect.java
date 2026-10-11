import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** Minimal structure-NBT inspector (gzip CompoundTag). */
public class StructureInspect {
    static class Tag {
        final byte type;
        final Object value;
        Tag(byte type, Object value) { this.type = type; this.value = value; }
    }

    static class Reader {
        final DataInputStream in;
        Reader(InputStream raw) throws IOException {
            in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(raw)));
        }
        Tag readNamed() throws IOException {
            byte type = in.readByte();
            if (type == 0) return new Tag((byte) 0, null);
            String name = in.readUTF();
            Object v = readPayload(type);
            return new Tag(type, Map.entry(name, v));
        }
        Object readPayload(byte type) throws IOException {
            return switch (type) {
                case 1 -> in.readByte();
                case 2 -> in.readShort();
                case 3 -> in.readInt();
                case 4 -> in.readLong();
                case 5 -> in.readFloat();
                case 6 -> in.readDouble();
                case 7 -> {
                    int n = in.readInt();
                    byte[] a = in.readNBytes(n);
                    yield a;
                }
                case 8 -> in.readUTF();
                case 9 -> {
                    byte et = in.readByte();
                    int n = in.readInt();
                    List<Object> list = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) list.add(readPayload(et));
                    yield list;
                }
                case 10 -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    while (true) {
                        byte t = in.readByte();
                        if (t == 0) break;
                        String name = in.readUTF();
                        map.put(name, readPayload(t));
                    }
                    yield map;
                }
                case 11 -> {
                    int n = in.readInt();
                    int[] a = new int[n];
                    for (int i = 0; i < n; i++) a[i] = in.readInt();
                    yield a;
                }
                case 12 -> {
                    int n = in.readInt();
                    long[] a = new long[n];
                    for (int i = 0; i < n; i++) a[i] = in.readLong();
                    yield a;
                }
                default -> throw new IOException("Unknown tag " + type);
            };
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Path path = Path.of(args.length > 0 ? args[0] :
                "run/saves/tesr/generated/minecraft/structures/office_building.nbt");
        Map<String, Object> root;
        try (InputStream in = Files.newInputStream(path)) {
            Reader r = new Reader(in);
            Tag named = r.readNamed();
            @SuppressWarnings("rawtypes")
            Map.Entry e = (Map.Entry) named.value;
            root = (Map<String, Object>) e.getValue();
        }

        System.out.printf("file=%s%n", path.toAbsolutePath());
        System.out.printf("root keys=%s%n", root.keySet());
        for (var e : root.entrySet()) {
            Object v = e.getValue();
            String t = v == null ? "null" : v.getClass().getSimpleName();
            if (v instanceof List<?> l) t += "[" + l.size() + (l.isEmpty() ? "" : " of " + l.get(0).getClass().getSimpleName()) + "]";
            if (v instanceof int[] a) t = "int[" + a.length + "]";
            System.out.printf("  %s -> %s%n", e.getKey(), t);
        }
        int[] size = toInt3(root.get("size"));
        System.out.printf("size=X%d Y%d Z%d  volume=%d%n", size[0], size[1], size[2], size[0]*size[1]*size[2]);
        System.out.printf("DataVersion=%s%n", root.get("DataVersion"));

        List<Object> palette = (List<Object>) root.get("palette");
        System.out.printf("palette=%d%n", palette.size());
        List<String> names = new ArrayList<>();
        for (Object o : palette) {
            Map<String, Object> p = (Map<String, Object>) o;
            String name = (String) p.get("Name");
            names.add(name);
        }
        Map<String, Integer> paletteCount = new TreeMap<>();
        for (String n : names) paletteCount.merge(n, 1, Integer::sum);
        System.out.println("-- palette unique blocks --");
        paletteCount.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                .forEach(e -> System.out.printf("  %4d variants  %s%n", e.getValue(), e.getKey()));

        List<Object> blocks = (List<Object>) root.get("blocks");
        System.out.printf("blocks=%d%n", blocks.size());

        int[] perY = new int[size[1]];
        Map<String, Integer> blockCounts = new TreeMap<>();
        int[][][] grid = new int[size[0]][size[1]][size[2]];
        for (int x = 0; x < size[0]; x++)
            for (int y = 0; y < size[1]; y++)
                Arrays.fill(grid[x][y], -1);

        for (Object o : blocks) {
            Map<String, Object> b = (Map<String, Object>) o;
            int[] pos = toInt3(b.get("pos"));
            int state = ((Number) b.get("state")).intValue();
            int x = pos[0], y = pos[1], z = pos[2];
            if (x < 0 || y < 0 || z < 0 || x >= size[0] || y >= size[1] || z >= size[2]) continue;
            grid[x][y][z] = state;
            perY[y]++;
            String nm = names.get(state);
            blockCounts.merge(nm, 1, Integer::sum);
        }

        System.out.println("-- blocks per Y --");
        for (int y = 0; y < size[1]; y++) {
            Map<String, Integer> layer = new TreeMap<>();
            int solid = 0, airish = 0;
            for (int x = 0; x < size[0]; x++) for (int z = 0; z < size[2]; z++) {
                int s = grid[x][y][z];
                if (s < 0) { airish++; continue; }
                String nm = names.get(s);
                layer.merge(shortName(nm), 1, Integer::sum);
                if (isAir(nm)) airish++; else solid++;
            }
            System.out.printf("Y=%2d placed=%4d solid~=%4d top=%s%n",
                    y, perY[y], solid, topN(layer, 6));
        }

        System.out.println("-- total block counts (top 40) --");
        blockCounts.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                .limit(40)
                .forEach(e -> System.out.printf("  %6d  %s%n", e.getValue(), e.getKey()));

        // Heuristic floor slabs: dense horizontal layers of floor-like blocks
        System.out.println("-- floor-like density by Y (stone/wood/concrete/carpet/slab-ish) --");
        for (int y = 0; y < size[1]; y++) {
            int floorish = 0, total = size[0] * size[2];
            for (int x = 0; x < size[0]; x++) for (int z = 0; z < size[2]; z++) {
                int s = grid[x][y][z];
                if (s < 0) continue;
                if (isFloorish(names.get(s))) floorish++;
            }
            double pct = 100.0 * floorish / total;
            if (pct >= 15 || y % 5 == 0)
                System.out.printf("Y=%2d floorish=%4d / %d (%.1f%%)%n", y, floorish, total, pct);
        }

        // Interior vs shell sample: count non-air that are not on outer shell
        int shell = 0, interior = 0;
        for (int x = 0; x < size[0]; x++) for (int y = 0; y < size[1]; y++) for (int z = 0; z < size[2]; z++) {
            int s = grid[x][y][z];
            if (s < 0 || isAir(names.get(s))) continue;
            boolean edge = x == 0 || y == 0 || z == 0 || x == size[0]-1 || y == size[1]-1 || z == size[2]-1;
            if (edge) shell++; else interior++;
        }
        System.out.printf("non-air shell-edge~=%d interior~=%d (edge includes roof/floor)%n", shell, interior);

        List<Object> entities = (List<Object>) root.getOrDefault("entities", List.of());
        System.out.printf("entities=%d%n", entities.size());
    }

    static int[] toInt3(Object o) {
        if (o instanceof int[] a) {
            if (a.length < 3) throw new IllegalArgumentException("int[] too short: " + a.length);
            return a;
        }
        if (o instanceof List<?> list) {
            int[] a = new int[list.size()];
            for (int i = 0; i < list.size(); i++) a[i] = ((Number) list.get(i)).intValue();
            return a;
        }
        throw new IllegalArgumentException("Not int3: " + (o == null ? null : o.getClass()));
    }

    static String shortName(String n) {
        int i = n.indexOf(':');
        return i >= 0 ? n.substring(i + 1) : n;
    }
    static boolean isAir(String n) {
        return n.equals("minecraft:air") || n.equals("minecraft:cave_air") || n.equals("minecraft:void_air")
                || n.equals("minecraft:light") || n.contains("structure_void");
    }
    static boolean isFloorish(String n) {
        String s = shortName(n);
        return s.contains("concrete") || s.contains("planks") || s.contains("wood")
                || s.contains("carpet") || s.contains("stone") || s.contains("brick")
                || s.contains("terracotta") || s.contains("slab") || s.contains("wool")
                || s.contains("deepslate") || s.contains("andesite") || s.contains("diorite")
                || s.contains("granite") || s.contains("quartz") || s.contains("prismarine")
                || s.contains("blackstone") || s.contains("basalt") || s.contains("calcite")
                || s.contains("smooth") || s.contains("tiles") || s.contains("mud_bricks");
    }
    static String topN(Map<String,Integer> m, int n) {
        return m.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                .limit(n)
                .map(e -> e.getKey() + ":" + e.getValue())
                .reduce((a,b) -> a + ", " + b).orElse("-");
    }
}
