import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** Inspect structure size + jigsaw/structure_block positions. */
public class StructureJigsawProbe {
    static class R {
        DataInputStream in;
        R(InputStream raw) throws IOException {
            in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(raw)));
        }
        Object payload(byte t) throws IOException {
            return switch (t) {
                case 1 -> in.readByte();
                case 2 -> in.readShort();
                case 3 -> in.readInt();
                case 4 -> in.readLong();
                case 5 -> in.readFloat();
                case 6 -> in.readDouble();
                case 7 -> { int n = in.readInt(); yield in.readNBytes(n); }
                case 8 -> in.readUTF();
                case 9 -> {
                    byte et = in.readByte();
                    int n = in.readInt();
                    List<Object> l = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) l.add(payload(et));
                    yield l;
                }
                case 10 -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    while (true) {
                        byte tt = in.readByte();
                        if (tt == 0) break;
                        m.put(in.readUTF(), payload(tt));
                    }
                    yield m;
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
                default -> throw new IOException("t=" + t);
            };
        }
        Map.Entry<String, Object> named() throws IOException {
            byte t = in.readByte();
            if (t == 0) return null;
            return Map.entry(in.readUTF(), payload(t));
        }
    }

    static int[] i3(Object o) {
        if (o instanceof int[] a) return a;
        List<?> l = (List<?>) o;
        int[] a = new int[l.size()];
        for (int i = 0; i < l.size(); i++) a[i] = ((Number) l.get(i)).intValue();
        return a;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            Path path = Path.of(arg);
            var named = new R(Files.newInputStream(path)).named();
            Map<String, Object> root = (Map<String, Object>) named.getValue();
            int[] size = i3(root.get("size"));
            List<Object> palette = (List<Object>) root.get("palette");
            List<String> names = new ArrayList<>();
            for (Object o : palette) {
                Map<String, Object> p = (Map<String, Object>) o;
                String name = (String) p.get("Name");
                names.add(name);
                if (name.contains("jigsaw") || name.contains("structure_block")) {
                    System.out.printf("  palette[%d]=%s props=%s%n", names.size() - 1, name, p.get("Properties"));
                }
            }
            System.out.printf("=== %s ===%n", path.getFileName());
            System.out.printf("size=X%d Y%d Z%d vol=%d blocks=%d%n",
                    size[0], size[1], size[2], size[0] * size[1] * size[2],
                    ((List<?>) root.get("blocks")).size());

            int jigsawCount = 0;
            for (Object o : (List<Object>) root.get("blocks")) {
                Map<String, Object> b = (Map<String, Object>) o;
                int st = ((Number) b.get("state")).intValue();
                String nm = names.get(st);
                if (!nm.contains("jigsaw") && !nm.equals("minecraft:structure_block")) continue;
                int[] pos = i3(b.get("pos"));
                jigsawCount++;
                Map<String, Object> nbt = b.containsKey("nbt") ? (Map<String, Object>) b.get("nbt") : Map.of();
                System.out.printf(
                        "  %s @ (%d,%d,%d) facing/props palette=%s%n    nbt={name=%s target=%s pool=%s joint=%s final_state=%s id=%s}%n",
                        nm,
                        pos[0], pos[1], pos[2],
                        paletteProps(palette.get(st)),
                        nbt.get("name"),
                        nbt.get("target"),
                        nbt.get("pool"),
                        nbt.get("joint"),
                        nbt.get("final_state"),
                        nbt.get("id"));
            }
            System.out.printf("jigsawOrStructureBlocks=%d%n%n", jigsawCount);
        }
    }

    @SuppressWarnings("unchecked")
    static String paletteProps(Object o) {
        Map<String, Object> p = (Map<String, Object>) o;
        Object props = p.get("Properties");
        return props == null ? "-" : props.toString();
    }
}
