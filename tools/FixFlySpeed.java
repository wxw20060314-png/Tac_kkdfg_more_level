import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Read/write Minecraft gzip NBT; fix Player abilities flySpeed to 0.05. */
public class FixFlySpeed {
    static final int END = 0, BYTE = 1, SHORT = 2, INT = 3, LONG = 4, FLOAT = 5, DOUBLE = 6,
            BYTE_ARRAY = 7, STRING = 8, LIST = 9, COMPOUND = 10, INT_ARRAY = 11, LONG_ARRAY = 12;

    static class TypedList {
        final byte elem;
        final List<Object> items;
        TypedList(byte elem, List<Object> items) { this.elem = elem; this.items = items; }
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
                case BYTE_ARRAY -> { int n = in.readInt(); yield in.readNBytes(n); }
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
        void close() throws IOException { out.close(); }
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

    @SuppressWarnings("unchecked")
    static int fixAbilities(Map<String, Object> abilities, String where) {
        int n = 0;
        if (abilities.containsKey("flySpeed")) {
            Object old = abilities.get("flySpeed");
            float f = ((Number) old).floatValue();
            System.out.printf("  %s flySpeed %s -> 0.05%n", where, old);
            if (old instanceof Double) abilities.put("flySpeed", 0.05d);
            else abilities.put("flySpeed", 0.05f);
            if (f != 0.05f) n++;
        }
        return n;
    }

    @SuppressWarnings("unchecked")
    static int fixPlayer(Map<String, Object> player, String where) {
        int n = 0;
        Object ab = player.get("abilities");
        if (ab instanceof Map<?, ?> m) {
            n += fixAbilities((Map<String, Object>) m, where + "/abilities");
        }
        return n;
    }

    @SuppressWarnings("unchecked")
    static int fixRoot(Map<String, Object> root, String label) {
        int n = 0;
        // player .dat root is the player compound
        if (root.containsKey("abilities")) {
            n += fixPlayer(root, label);
        }
        // level.dat: Data.Player.abilities
        Object data = root.get("Data");
        if (data instanceof Map<?, ?> dm) {
            Map<String, Object> dataMap = (Map<String, Object>) dm;
            Object player = dataMap.get("Player");
            if (player instanceof Map<?, ?> pm) {
                n += fixPlayer((Map<String, Object>) pm, label + "/Data/Player");
            }
        }
        return n;
    }

    public static void main(String[] args) throws Exception {
        float target = 0.05f;
        List<Path> files = new ArrayList<>();
        for (String a : args) files.add(Path.of(a));
        if (files.isEmpty()) {
            System.err.println("usage: FixFlySpeed <level.dat|player.dat>...");
            return;
        }
        for (Path path : files) {
            if (!Files.exists(path)) {
                System.out.println("missing " + path);
                continue;
            }
            Map.Entry<String, Object> named;
            try (InputStream in = Files.newInputStream(path)) {
                named = new In(in).named();
            }
            String rootName = named.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> root = (Map<String, Object>) named.getValue();
            System.out.println("file=" + path.toAbsolutePath());
            int changed = fixRoot(root, rootName.isEmpty() ? "(root)" : rootName);
            if (changed == 0) {
                System.out.println("  no flySpeed change needed (or missing)");
                // still show current if present
                continue;
            }
            Path bak = path.resolveSibling(path.getFileName().toString() + ".flybak");
            if (!Files.exists(bak)) {
                Files.copy(path, bak);
                System.out.println("  backup=" + bak.getFileName());
            }
            try (OutputStream os = Files.newOutputStream(path)) {
                Out out = new Out(os);
                out.named(rootName, (byte) COMPOUND, root);
                out.close();
            }
            System.out.println("  wrote ok, fieldsFixed=" + changed + " target=" + target);
        }
    }
}
