import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Hollow bottom/middle floors of office_building.nbt:
 * keep outer shell + floor decks; clear interior walls/stairs/elevator; keep top (Y&gt;=31).
 */
public class StructureHollow {
    static final int TAG_END = 0, TAG_BYTE = 1, TAG_SHORT = 2, TAG_INT = 3, TAG_LONG = 4,
            TAG_FLOAT = 5, TAG_DOUBLE = 6, TAG_BYTE_ARRAY = 7, TAG_STRING = 8, TAG_LIST = 9,
            TAG_COMPOUND = 10, TAG_INT_ARRAY = 11, TAG_LONG_ARRAY = 12;

    static class NbtIn {
        final DataInputStream in;
        NbtIn(InputStream raw) throws IOException {
            in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(raw)));
        }
        Map.Entry<String, Object> readNamed() throws IOException {
            byte type = in.readByte();
            if (type == TAG_END) return null;
            String name = in.readUTF();
            return Map.entry(name, readPayload(type));
        }
        Object readPayload(byte type) throws IOException {
            return switch (type) {
                case TAG_BYTE -> in.readByte();
                case TAG_SHORT -> in.readShort();
                case TAG_INT -> in.readInt();
                case TAG_LONG -> in.readLong();
                case TAG_FLOAT -> in.readFloat();
                case TAG_DOUBLE -> in.readDouble();
                case TAG_BYTE_ARRAY -> {
                    int n = in.readInt();
                    yield in.readNBytes(n);
                }
                case TAG_STRING -> in.readUTF();
                case TAG_LIST -> {
                    byte et = in.readByte();
                    int n = in.readInt();
                    List<Object> list = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) list.add(readPayload(et));
                    yield new TypedList(et, list);
                }
                case TAG_COMPOUND -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    while (true) {
                        byte t = in.readByte();
                        if (t == TAG_END) break;
                        String name = in.readUTF();
                        map.put(name, readPayload(t));
                    }
                    yield map;
                }
                case TAG_INT_ARRAY -> {
                    int n = in.readInt();
                    int[] a = new int[n];
                    for (int i = 0; i < n; i++) a[i] = in.readInt();
                    yield a;
                }
                case TAG_LONG_ARRAY -> {
                    int n = in.readInt();
                    long[] a = new long[n];
                    for (int i = 0; i < n; i++) a[i] = in.readLong();
                    yield a;
                }
                default -> throw new IOException("bad type " + type);
            };
        }
    }

    static class TypedList {
        final byte elemType;
        final List<Object> items;
        TypedList(byte elemType, List<Object> items) {
            this.elemType = elemType;
            this.items = items;
        }
    }

    static class NbtOut {
        final DataOutputStream out;
        NbtOut(OutputStream raw) throws IOException {
            out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(raw)));
        }
        void writeNamed(String name, byte type, Object value) throws IOException {
            out.writeByte(type);
            out.writeUTF(name);
            writePayload(type, value);
        }
        void writePayload(byte type, Object value) throws IOException {
            switch (type) {
                case TAG_BYTE -> out.writeByte(((Number) value).byteValue());
                case TAG_SHORT -> out.writeShort(((Number) value).shortValue());
                case TAG_INT -> out.writeInt(((Number) value).intValue());
                case TAG_LONG -> out.writeLong(((Number) value).longValue());
                case TAG_FLOAT -> out.writeFloat(((Number) value).floatValue());
                case TAG_DOUBLE -> out.writeDouble(((Number) value).doubleValue());
                case TAG_BYTE_ARRAY -> {
                    byte[] a = (byte[]) value;
                    out.writeInt(a.length);
                    out.write(a);
                }
                case TAG_STRING -> out.writeUTF((String) value);
                case TAG_LIST -> {
                    TypedList tl = (TypedList) value;
                    out.writeByte(tl.elemType);
                    out.writeInt(tl.items.size());
                    for (Object o : tl.items) writePayload(tl.elemType, o);
                }
                case TAG_COMPOUND -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) value;
                    for (var e : map.entrySet()) {
                        byte t = typeOf(e.getValue());
                        out.writeByte(t);
                        out.writeUTF(e.getKey());
                        writePayload(t, e.getValue());
                    }
                    out.writeByte(TAG_END);
                }
                case TAG_INT_ARRAY -> {
                    int[] a = (int[]) value;
                    out.writeInt(a.length);
                    for (int v : a) out.writeInt(v);
                }
                case TAG_LONG_ARRAY -> {
                    long[] a = (long[]) value;
                    out.writeInt(a.length);
                    for (long v : a) out.writeLong(v);
                }
                default -> throw new IOException("bad type " + type);
            }
        }
        void close() throws IOException { out.close(); }
    }

    static byte typeOf(Object v) {
        if (v instanceof Byte) return TAG_BYTE;
        if (v instanceof Short) return TAG_SHORT;
        if (v instanceof Integer) return TAG_INT;
        if (v instanceof Long) return TAG_LONG;
        if (v instanceof Float) return TAG_FLOAT;
        if (v instanceof Double) return TAG_DOUBLE;
        if (v instanceof byte[]) return TAG_BYTE_ARRAY;
        if (v instanceof String) return TAG_STRING;
        if (v instanceof TypedList) return TAG_LIST;
        if (v instanceof Map) return TAG_COMPOUND;
        if (v instanceof int[]) return TAG_INT_ARRAY;
        if (v instanceof long[]) return TAG_LONG_ARRAY;
        if (v instanceof List) return TAG_LIST; // size stored as ArrayList of Integer
        throw new IllegalArgumentException("type " + v.getClass());
    }

    static int[] toInt3(Object o) {
        if (o instanceof int[] a) return a;
        if (o instanceof TypedList tl) {
            int[] a = new int[tl.items.size()];
            for (int i = 0; i < tl.items.size(); i++) a[i] = ((Number) tl.items.get(i)).intValue();
            return a;
        }
        if (o instanceof List<?> list) {
            int[] a = new int[list.size()];
            for (int i = 0; i < list.size(); i++) a[i] = ((Number) list.get(i)).intValue();
            return a;
        }
        throw new IllegalArgumentException("not int3: " + (o == null ? null : o.getClass()));
    }

    static boolean isAir(String name) {
        return name.equals("minecraft:air") || name.equals("minecraft:cave_air")
                || name.equals("minecraft:void_air");
    }

    static boolean isFloorDeck(String name) {
        return name.equals("minecraft:polished_andesite")
                || name.equals("minecraft:deepslate_tiles")
                || name.equals("minecraft:smooth_stone")
                || name.equals("minecraft:stone")
                || name.equals("minecraft:smooth_quartz")
                || name.equals("minecraft:polished_blackstone");
    }

    /** Stairs / elevator / loose interior props — always strip on hollowed floors. */
    static boolean isAlwaysStrip(String name) {
        return name.contains("stairs")
                || name.equals("minecraft:iron_bars")
                || name.equals("minecraft:iron_door")
                || name.equals("minecraft:bookshelf")
                || name.equals("minecraft:chest")
                || name.equals("minecraft:cyan_carpet")
                || name.equals("minecraft:spruce_planks")
                || name.equals("minecraft:azalea_leaves")
                || name.equals("minecraft:oak_stairs")
                || name.equals("minecraft:deepslate_tile_stairs");
    }

    /** Interior story heights (between floor decks). */
    static boolean isHollowStoryY(int y) {
        return (y >= 2 && y <= 5)
                || (y >= 7 && y <= 10)
                || (y >= 12 && y <= 15)
                || (y >= 17 && y <= 20)
                || (y >= 22 && y <= 25)
                || (y >= 27 && y <= 30);
    }

    /** Floor decks that should stay solid, but lose stairs/elevator/walls. */
    static boolean isFloorDeckY(int y) {
        return y == 6 || y == 11 || y == 16 || y == 21 || y == 26;
    }

    static boolean onOuterShell(int x, int z, int sx, int sz) {
        return x == 0 || z == 0 || x == sx - 1 || z == sz - 1;
    }

    /** Flood-fill air connected to the structure bounding-box border. */
    static boolean[][] floodExteriorAir(int[][] layer, List<String> names, int sx, int sz) {
        boolean[][] ext = new boolean[sx][sz];
        ArrayDeque<int[]> q = new ArrayDeque<>();
        for (int x = 0; x < sx; x++) {
            tryEnqueueExterior(q, ext, layer, names, x, 0, sx, sz);
            tryEnqueueExterior(q, ext, layer, names, x, sz - 1, sx, sz);
        }
        for (int z = 0; z < sz; z++) {
            tryEnqueueExterior(q, ext, layer, names, 0, z, sx, sz);
            tryEnqueueExterior(q, ext, layer, names, sx - 1, z, sx, sz);
        }
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!q.isEmpty()) {
            int[] p = q.removeFirst();
            for (int[] d : dirs) {
                int nx = p[0] + d[0], nz = p[1] + d[1];
                tryEnqueueExterior(q, ext, layer, names, nx, nz, sx, sz);
            }
        }
        return ext;
    }

    static void tryEnqueueExterior(
            ArrayDeque<int[]> q, boolean[][] ext, int[][] layer, List<String> names,
            int x, int z, int sx, int sz) {
        if (x < 0 || z < 0 || x >= sx || z >= sz || ext[x][z]) return;
        int st = layer[x][z];
        if (st >= 0 && !isAir(names.get(st))) return;
        ext[x][z] = true;
        q.add(new int[] {x, z});
    }

    /** True if within chebyshev radius of exterior air (keeps double-thick facade). */
    static boolean nearExterior(boolean[][] ext, int x, int z, int sx, int sz, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int nx = x + dx, nz = z + dz;
                if (nx < 0 || nz < 0 || nx >= sx || nz >= sz) return true;
                if (ext[nx][nz]) return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Path path = Path.of(args.length > 0 ? args[0]
                : "run/saves/tesr/generated/minecraft/structures/office_building.nbt");
        Path backup = path.resolveSibling(path.getFileName().toString() + ".bak");

        Map.Entry<String, Object> named;
        try (InputStream in = Files.newInputStream(path)) {
            named = new NbtIn(in).readNamed();
        }
        String rootName = named.getKey();
        Map<String, Object> root = (Map<String, Object>) named.getValue();

        int[] size = toInt3(root.get("size"));
        int sx = size[0], sy = size[1], sz = size[2];

        TypedList paletteList = asTypedList(root.get("palette"));
        List<String> names = new ArrayList<>();
        for (Object o : paletteList.items) {
            Map<String, Object> p = (Map<String, Object>) o;
            names.add((String) p.get("Name"));
        }
        int airState = -1;
        for (int i = 0; i < names.size(); i++) {
            if (isAir(names.get(i))) { airState = i; break; }
        }
        if (airState < 0) throw new IllegalStateException("no air in palette");

        TypedList blocksList = asTypedList(root.get("blocks"));

        // grid[x][y][z] = state index; also keep block map refs for writing
        int[][][] grid = new int[sx][sy][sz];
        for (int x = 0; x < sx; x++)
            for (int y = 0; y < sy; y++)
                Arrays.fill(grid[x][y], -1);
        @SuppressWarnings("unchecked")
        Map<String, Object>[][][] blockAt = new Map[sx][sy][sz];
        for (Object o : blocksList.items) {
            Map<String, Object> b = (Map<String, Object>) o;
            int[] pos = toInt3(b.get("pos"));
            int x = pos[0], y = pos[1], z = pos[2];
            if (x < 0 || y < 0 || z < 0 || x >= sx || y >= sy || z >= sz) continue;
            int state = ((Number) b.get("state")).intValue();
            grid[x][y][z] = state;
            blockAt[x][y][z] = b;
        }

        // Union exterior across hollow+floor Y so facade stays consistent
        boolean[][] exteriorUnion = new boolean[sx][sz];
        for (int y = 2; y <= 30; y++) {
            int[][] layer = new int[sx][sz];
            for (int x = 0; x < sx; x++)
                for (int z = 0; z < sz; z++)
                    layer[x][z] = grid[x][y][z];
            boolean[][] ext = floodExteriorAir(layer, names, sx, sz);
            for (int x = 0; x < sx; x++)
                for (int z = 0; z < sz; z++)
                    if (ext[x][z]) exteriorUnion[x][z] = true;
        }

        int shellRadius = 2; // double-thick curtain wall
        int cleared = 0, kept = 0;
        for (int y = 0; y < sy; y++) {
            for (int x = 0; x < sx; x++) {
                for (int z = 0; z < sz; z++) {
                    Map<String, Object> b = blockAt[x][y][z];
                    if (b == null) continue;
                    int state = grid[x][y][z];
                    String name = state < 0 ? "minecraft:air" : names.get(state);
                    boolean clear = false;

                    if (y >= 31 || y <= 1) {
                        // foundation / top untouched
                    } else if (isAlwaysStrip(name)) {
                        clear = true;
                    } else if (isHollowStoryY(y)) {
                        if (!isAir(name) && !nearExterior(exteriorUnion, x, z, sx, sz, shellRadius)) {
                            clear = true;
                        }
                    } else if (isFloorDeckY(y)) {
                        if (!isAir(name)
                                && !isFloorDeck(name)
                                && !nearExterior(exteriorUnion, x, z, sx, sz, shellRadius)) {
                            clear = true;
                        }
                    }

                    if (clear) {
                        b.put("state", airState);
                        b.remove("nbt");
                        grid[x][y][z] = airState;
                        cleared++;
                    } else {
                        kept++;
                    }
                }
            }
        }

        if (!Files.exists(backup)) {
            Files.copy(path, backup);
            System.out.println("backup=" + backup.toAbsolutePath());
        } else {
            System.out.println("backup exists, not overwritten: " + backup);
        }

        try (OutputStream os = Files.newOutputStream(path)) {
            NbtOut out = new NbtOut(os);
            out.writeNamed(rootName, (byte) TAG_COMPOUND, root);
            out.close();
        }

        System.out.printf("done size=%dx%dx%d cleared=%d kept_or_air=%d airState=%d shellRadius=%d%n",
                sx, sy, sz, cleared, kept, airState, shellRadius);
        System.out.println("wrote " + path.toAbsolutePath());
    }

    @SuppressWarnings("unchecked")
    static TypedList asTypedList(Object o) {
        if (o instanceof TypedList tl) return tl;
        if (o instanceof List<?> list) {
            // reconstruct from plain list (shouldn't happen for palette/blocks after our reader)
            byte et = list.isEmpty() ? TAG_COMPOUND : typeOf(list.get(0));
            return new TypedList(et, new ArrayList<>(list));
        }
        throw new IllegalArgumentException("not list");
    }
}
