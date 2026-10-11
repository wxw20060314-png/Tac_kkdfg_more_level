import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class ShellProbe {
    static class R {
        DataInputStream in;
        R(InputStream raw) throws IOException {
            in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(raw)));
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
                    yield in.readNBytes(n);
                }
                case 8 -> in.readUTF();
                case 9 -> {
                    byte et = in.readByte();
                    int n = in.readInt();
                    List<Object> l = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) l.add(readPayload(et));
                    yield l;
                }
                case 10 -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    while (true) {
                        byte t = in.readByte();
                        if (t == 0) break;
                        String name = in.readUTF();
                        m.put(name, readPayload(t));
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
                default -> throw new IOException("t=" + type);
            };
        }
        Map.Entry<String, Object> readNamed() throws IOException {
            byte t = in.readByte();
            if (t == 0) return null;
            String n = in.readUTF();
            return Map.entry(n, readPayload(t));
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
    public static void main(String[] a) throws Exception {
        Path bak = Path.of(
                "d:/JAVAproject/Tac_kkdfg_more_level/run/saves/tesr/generated/minecraft/structures/office_building.nbt.bak");
        var named = new R(Files.newInputStream(bak)).readNamed();
        Map<String, Object> root = (Map<String, Object>) named.getValue();
        int[] size = i3(root.get("size"));
        List<Object> palette = (List<Object>) root.get("palette");
        List<String> names = new ArrayList<>();
        for (Object o : palette) names.add((String) ((Map<String, Object>) o).get("Name"));
        int minX = 999, maxX = -1, minZ = 999, maxZ = -1;
        Map<Integer, Integer> edgeX = new TreeMap<>();
        Map<Integer, Integer> edgeZ = new TreeMap<>();
        int y2shell = 0, y2total = 0;
        for (Object o : (List<Object>) root.get("blocks")) {
            Map<String, Object> b = (Map<String, Object>) o;
            int[] p = i3(b.get("pos"));
            int st = ((Number) b.get("state")).intValue();
            String nm = names.get(st);
            if (p[1] != 2) continue;
            if (nm.equals("minecraft:air")) continue;
            y2total++;
            minX = Math.min(minX, p[0]);
            maxX = Math.max(maxX, p[0]);
            minZ = Math.min(minZ, p[2]);
            maxZ = Math.max(maxZ, p[2]);
            if (p[0] == 0 || p[0] == size[0] - 1 || p[2] == 0 || p[2] == size[2] - 1) y2shell++;
            edgeX.merge(p[0], 1, Integer::sum);
            edgeZ.merge(p[2], 1, Integer::sum);
        }
        System.out.printf(
                "size=%s y2 solid=%d onExactEdge=%d bbox x[%d,%d] z[%d,%d]%n",
                Arrays.toString(size), y2total, y2shell, minX, maxX, minZ, maxZ);
        System.out.println("top X counts:");
        edgeX.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> System.out.println("  x=" + e.getKey() + " " + e.getValue()));
        System.out.println("top Z counts:");
        edgeZ.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> System.out.println("  z=" + e.getKey() + " " + e.getValue()));
    }
}
