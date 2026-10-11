import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Copy z1-z6 from jgc2 save and wire jigsaw NBT for nether_bomb_factory.
 *
 * <pre>
 * Dual spine (face +X, right +Z):
 *   z5          z6
 *    ^           ^
 *   z3          z4
 *    ^           ^
 *   z1 &lt;-----&gt; z2
 * start = z1
 * </pre>
 */
public class WireBombFactoryJigsaws {
    static final int END = 0, BYTE = 1, SHORT = 2, INT = 3, LONG = 4, FLOAT = 5, DOUBLE = 6,
            BYTE_ARRAY = 7, STRING = 8, LIST = 9, COMPOUND = 10, INT_ARRAY = 11, LONG_ARRAY = 12;

    static class TypedList {
        final byte elem;
        final List<Object> items;

        TypedList(byte elem, List<Object> items) {
            this.elem = elem;
            this.items = items;
        }
    }

    static class In {
        final DataInputStream in;

        In(InputStream raw) throws IOException {
            in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(raw)));
        }

        Map.Entry<String, Object> named() throws IOException {
            byte t = in.readByte();
            if (t == END) return null;
            return Map.entry(in.readUTF(), payload(t));
        }

        Object payload(byte t) throws IOException {
            return switch (t) {
                case BYTE -> in.readByte();
                case SHORT -> in.readShort();
                case INT -> in.readInt();
                case LONG -> in.readLong();
                case FLOAT -> in.readFloat();
                case DOUBLE -> in.readDouble();
                case BYTE_ARRAY -> {
                    int n = in.readInt();
                    yield in.readNBytes(n);
                }
                case STRING -> in.readUTF();
                case LIST -> {
                    byte et = in.readByte();
                    int n = in.readInt();
                    List<Object> l = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) l.add(payload(et));
                    yield new TypedList(et, l);
                }
                case COMPOUND -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    while (true) {
                        byte tt = in.readByte();
                        if (tt == END) break;
                        m.put(in.readUTF(), payload(tt));
                    }
                    yield m;
                }
                case INT_ARRAY -> {
                    int n = in.readInt();
                    int[] a = new int[n];
                    for (int i = 0; i < n; i++) a[i] = in.readInt();
                    yield a;
                }
                case LONG_ARRAY -> {
                    int n = in.readInt();
                    long[] a = new long[n];
                    for (int i = 0; i < n; i++) a[i] = in.readLong();
                    yield a;
                }
                default -> throw new IOException("bad " + t);
            };
        }
    }

    static class Out {
        final DataOutputStream out;

        Out(OutputStream raw) throws IOException {
            out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(raw)));
        }

        void named(String name, byte t, Object v) throws IOException {
            out.writeByte(t);
            out.writeUTF(name);
            payload(t, v);
        }

        void payload(byte t, Object v) throws IOException {
            switch (t) {
                case BYTE -> out.writeByte(((Number) v).byteValue());
                case SHORT -> out.writeShort(((Number) v).shortValue());
                case INT -> out.writeInt(((Number) v).intValue());
                case LONG -> out.writeLong(((Number) v).longValue());
                case FLOAT -> out.writeFloat(((Number) v).floatValue());
                case DOUBLE -> out.writeDouble(((Number) v).doubleValue());
                case BYTE_ARRAY -> {
                    byte[] a = (byte[]) v;
                    out.writeInt(a.length);
                    out.write(a);
                }
                case STRING -> out.writeUTF((String) v);
                case LIST -> {
                    TypedList tl = (TypedList) v;
                    out.writeByte(tl.elem);
                    out.writeInt(tl.items.size());
                    for (Object o : tl.items) payload(tl.elem, o);
                }
                case COMPOUND -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> m = (Map<String, Object>) v;
                    for (var e : m.entrySet()) {
                        byte tt = typeOf(e.getValue());
                        out.writeByte(tt);
                        out.writeUTF(e.getKey());
                        payload(tt, e.getValue());
                    }
                    out.writeByte(END);
                }
                case INT_ARRAY -> {
                    int[] a = (int[]) v;
                    out.writeInt(a.length);
                    for (int x : a) out.writeInt(x);
                }
                case LONG_ARRAY -> {
                    long[] a = (long[]) v;
                    out.writeInt(a.length);
                    for (long x : a) out.writeLong(x);
                }
                default -> throw new IOException("bad " + t);
            }
        }

        void close() throws IOException {
            out.close();
        }
    }

    static byte typeOf(Object v) {
        if (v instanceof Byte) return BYTE;
        if (v instanceof Short) return SHORT;
        if (v instanceof Integer) return INT;
        if (v instanceof Long) return LONG;
        if (v instanceof Float) return FLOAT;
        if (v instanceof Double) return DOUBLE;
        if (v instanceof byte[]) return BYTE_ARRAY;
        if (v instanceof String) return STRING;
        if (v instanceof TypedList) return LIST;
        if (v instanceof Map) return COMPOUND;
        if (v instanceof int[]) return INT_ARRAY;
        if (v instanceof long[]) return LONG_ARRAY;
        throw new IllegalArgumentException(String.valueOf(v.getClass()));
    }

    static int[] i3(Object o) {
        if (o instanceof int[] a) return a;
        if (o instanceof TypedList tl) {
            int[] a = new int[tl.items.size()];
            for (int i = 0; i < tl.items.size(); i++) a[i] = ((Number) tl.items.get(i)).intValue();
            return a;
        }
        List<?> l = (List<?>) o;
        int[] a = new int[l.size()];
        for (int i = 0; i < l.size(); i++) a[i] = ((Number) l.get(i)).intValue();
        return a;
    }

    record Wire(int x, int y, int z, String name, String pool) {}

    /** Expanding side has pool; receiving side uses minecraft:empty. */
    static final Map<String, List<Wire>> WIRES =
            Map.ofEntries(
                    Map.entry(
                            "nether_bomb_z1",
                            List.of(
                                    new Wire(
                                            30,
                                            47,
                                            47,
                                            "tacz_morelevel:bomb_12",
                                            "tacz_morelevel:nether_bomb_factory/z2"),
                                    new Wire(
                                            47,
                                            47,
                                            44,
                                            "tacz_morelevel:bomb_13",
                                            "tacz_morelevel:nether_bomb_factory/z3"))),
                    Map.entry(
                            "nether_bomb_z2",
                            List.of(
                                    new Wire(
                                            30,
                                            47,
                                            0,
                                            "tacz_morelevel:bomb_12",
                                            "minecraft:empty"),
                                    new Wire(
                                            47,
                                            47,
                                            6,
                                            "tacz_morelevel:bomb_24",
                                            "tacz_morelevel:nether_bomb_factory/z4"))),
                    Map.entry(
                            "nether_bomb_z3",
                            List.of(
                                    new Wire(
                                            0,
                                            47,
                                            44,
                                            "tacz_morelevel:bomb_13",
                                            "minecraft:empty"),
                                    new Wire(
                                            47,
                                            47,
                                            31,
                                            "tacz_morelevel:bomb_35",
                                            "tacz_morelevel:nether_bomb_factory/z5"))),
                    Map.entry(
                            "nether_bomb_z4",
                            List.of(
                                    new Wire(
                                            0,
                                            47,
                                            6,
                                            "tacz_morelevel:bomb_24",
                                            "minecraft:empty"),
                                    new Wire(
                                            47,
                                            47,
                                            13,
                                            "tacz_morelevel:bomb_46",
                                            "tacz_morelevel:nether_bomb_factory/z6"))),
                    Map.entry(
                            "nether_bomb_z5",
                            List.of(
                                    new Wire(
                                            0,
                                            47,
                                            31,
                                            "tacz_morelevel:bomb_35",
                                            "minecraft:empty"))),
                    Map.entry(
                            "nether_bomb_z6",
                            List.of(
                                    new Wire(
                                            0,
                                            47,
                                            13,
                                            "tacz_morelevel:bomb_46",
                                            "minecraft:empty"))));

    @SuppressWarnings("unchecked")
    static void wireFile(Path src, Path dst, String id) throws Exception {
        Map.Entry<String, Object> named;
        try (InputStream in = Files.newInputStream(src)) {
            named = new In(in).named();
        }
        String rootName = named.getKey();
        Map<String, Object> root = (Map<String, Object>) named.getValue();
        List<String> names = new ArrayList<>();
        TypedList palette = (TypedList) root.get("palette");
        for (Object o : palette.items) {
            names.add((String) ((Map<String, Object>) o).get("Name"));
        }
        List<Wire> wires = WIRES.get(id);
        if (wires == null) throw new IllegalArgumentException("no wires for " + id);
        int hit = 0;
        TypedList blocks = (TypedList) root.get("blocks");
        for (Object o : blocks.items) {
            Map<String, Object> b = (Map<String, Object>) o;
            int st = ((Number) b.get("state")).intValue();
            if (!names.get(st).equals("minecraft:jigsaw")) continue;
            int[] pos = i3(b.get("pos"));
            for (Wire w : wires) {
                if (pos[0] != w.x || pos[1] != w.y || pos[2] != w.z) continue;
                Map<String, Object> nbt =
                        b.containsKey("nbt")
                                ? new LinkedHashMap<>((Map<String, Object>) b.get("nbt"))
                                : new LinkedHashMap<>();
                nbt.put("id", "minecraft:jigsaw");
                nbt.put("name", w.name);
                nbt.put("target", w.name);
                nbt.put("pool", w.pool);
                nbt.put("joint", "aligned");
                nbt.put("final_state", "minecraft:air");
                b.put("nbt", nbt);
                hit++;
                System.out.printf(
                        "  %s jigsaw (%d,%d,%d) name=%s pool=%s%n",
                        id, w.x, w.y, w.z, w.name, w.pool);
            }
        }
        if (hit != wires.size()) {
            throw new IllegalStateException(id + " expected " + wires.size() + " wires, hit " + hit);
        }
        Files.createDirectories(dst.getParent());
        try (OutputStream os = Files.newOutputStream(dst)) {
            Out out = new Out(os);
            out.named(rootName, (byte) COMPOUND, root);
            out.close();
        }
    }

    public static void main(String[] args) throws Exception {
        Path srcDir = Path.of("run/saves/jgc2/generated/minecraft/structures");
        Path dstDir = Path.of("src/main/resources/data/tacz_morelevel/structures");
        for (int i = 1; i <= 6; i++) {
            String id = "nether_bomb_z" + i;
            Path src = srcDir.resolve("z" + i + ".nbt");
            Path dst = dstDir.resolve(id + ".nbt");
            System.out.println("wire " + src + " -> " + dst);
            wireFile(src, dst, id);
        }
        System.out.println("done");
    }
}
