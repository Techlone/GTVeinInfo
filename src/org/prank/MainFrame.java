package org.prank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.*;

@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
public class MainFrame extends JFrame {
    public static Map<Coord, String> result = new HashMap<>();

    private JTextField tfSeed;
    private JTextField tfSize;
    private JTextField tfOffsetX;
    private JTextField tfOffsetZ;
    private JComboBox<String> cbDim;
    private JComboBox<String> cbOre;
    private JTextArea taOutput;

    public MainFrame() throws HeadlessException {
        super("GTVeinInfo");
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });

        setSize(660, 480);
        setResizable(false);
        setLayout(null);

        setLabels();
        setInputs();
        setButtons();
        setTextAreas();
        setComboBoxes();

        tfSeed.setText("");
        tfSize.setText("10");
        tfOffsetX.setText("0");
        tfOffsetZ.setText("0");
        cbDim.setSelectedItem("overworld");

        setVisible(true);
    }

    public void generate(long wSeed, int chX, int chZ, String dim) {
        Random fmlRandom = new Random(wSeed);
        long xSeed = fmlRandom.nextLong() >> 2 + 1L;
        long zSeed = fmlRandom.nextLong() >> 2 + 1L;
        fmlRandom.setSeed((xSeed * chX + zSeed * chZ) ^ wSeed);
        String oreName = String.valueOf(cbOre.getSelectedItem());
        Tuple t = generateGT(new XSTR(fmlRandom.nextInt()), chX, chZ, dim);
        if (t != null && t.y != -1 && (oreName.equals("all") || oreName.equals(t.s)))
            result.put(new Coord(chX, t.y, chZ), t.s);
    }

    private Tuple generateGT(Random random, int chX, int chZ, String dim) {
        if (!((chX - 1) % 3 == 0 && (chZ - 1) % 3 == 0)) return null;

        for (int i = 0; i < 256; i++) {
            int tRandomWeight = random.nextInt(OreVein.totalWeight);
            for (OreVein oreVein : OreVein.ores) {
                tRandomWeight -= oreVein.weight;
                if (tRandomWeight > 0) continue;
                int y = oreVein.executeWorldgen(random, dim, chX, chZ);
                if (y != -1) return new Tuple(y, oreVein.name);
                break;
            }
        }
        return null;
    }

    private static class Tuple {
        public int y;
        public String s;

        public Tuple(int y, String s) {
            this.y = y;
            this.s = s;
        }
    }

    private static class Coord {
        public int x;
        public int y;
        public int z;

        public Coord(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public int hashCode() {
            return (23 ^ x) ^ z;
        }
    }

    private void calculate() {
        result.clear();
        long seed = getSeed();
        int size = Integer.parseInt(tfSize.getText()) * 3;
        int offsetX = Integer.parseInt(tfOffsetX.getText()) >> 4;
        int offsetZ = Integer.parseInt(tfOffsetZ.getText()) >> 4;
        String dim = String.valueOf(cbDim.getSelectedItem()).toLowerCase();

        for (int x = -size; x < size; x++)
            for (int z = -size; z < size; z++)
                generate(seed, x + offsetX, z + offsetZ, dim);

        List<Coord> coords = new ArrayList<>(result.keySet());
        coords.sort((l, r) -> l.x != r.x ? l.x - r.x : l.z - r.z);
        StringBuilder sb = new StringBuilder();
        coords.forEach(c -> sb
                .append(c.x * 16 + 8).append(" ")
                .append(c.y).append(" ")
                .append(c.z * 16 + 8).append(" ")
                .append(result.get(c))
                .append(System.lineSeparator()));
        taOutput.setText(sb.toString());
    }

    private long getSeed() {
        String seedStr = tfSeed.getText();
        if (seedStr == null || seedStr.equals("")) {
            long newSeed = new Random().nextLong();
            tfSeed.setText(String.valueOf(newSeed));
            return newSeed;
        }
        long seed;
        try {
            seed = Long.parseLong(seedStr);
        } catch (Exception e) {
            seed = seedStr.hashCode();
            tfSeed.setText(String.valueOf(seed));
        }
        return seed;
    }

    private void exportJM() {
        if (result.isEmpty())
            calculate();
        File waypoints = new File("waypoints");
        if (!waypoints.exists()) waypoints.mkdir();
        result.forEach(this::writeJMWayPoint);
    }

    private void writeJMWayPoint(Coord coord, String name) {
        // Translate chunk coords to block coords
        int x = coord.x * 16 + 8;
        int y = coord.y;
        int z = coord.z * 16 + 8;

        String upName = name.substring(0, 1).toUpperCase() + name.substring(1);
        String wpName = upName + "_" + x + "," + y + "," + z;
        String fileName = "waypoints/" + wpName + "." + getCurrentDimID() + ".json";

        // Stretch coords if nether dimension due to JourneyMap squeezing
        if (getCurrentDimID() == -1) {
            x *= 8;
            z *= 8;
        }

        Color color = getColor(name);
        StringBuilder sb = new StringBuilder("{")
                .append("\"id\": \"").append(wpName).append("\", ")
                .append("\"name\": \"").append(upName).append("\", ")
                .append("\"icon\": \"waypoint-normal.png\", ")
                .append("\"x\": ").append(x).append(", ")
                .append("\"y\": ").append(y).append(", ")
                .append("\"z\": ").append(z).append(", ")
                .append("\"r\": ").append(color.red).append(", ")
                .append("\"g\": ").append(color.green).append(", ")
                .append("\"b\": ").append(color.blue).append(", ")
                .append("\"enable\": true, ")
                .append("\"type\": \"Normal\", ")
                .append("\"origin\": \"JourneyMap\", ")
                .append("\"dimensions\": [").append(getCurrentDimID()).append("]}");

        try (FileWriter file = new FileWriter(fileName)) {
            file.write(sb.toString());
            file.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportMW() {
        if (result.isEmpty())
            calculate();

        StringBuilder fileBuilder = new StringBuilder()
                .append("# Configuration file\n\n")
                .append("markers {\n");

        int id = 0;
        for (Coord key : result.keySet())
            fileBuilder.append(this.writeMwWayPoint(id++, key, result.get(key)));

        fileBuilder
                .append("\tI:markerCount=").append(result.size()).append("\n")
                .append("\tS:visibleGroup=all\n}\n");
//                .append("world {\n\tI:dimensionList <\n\t\t").append(getCurrentDimID()).append("\n\t>\n}");

        try (FileWriter file = new FileWriter("mapwriter.cfg", false)) {
            file.write(fileBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String writeMwWayPoint(int id, Coord coord, String name) {
        // Translate chunk coords to block coords
        int x = coord.x * 16 + 8, y = coord.y, z = coord.z * 16 + 8;
        String upName = name.substring(0, 1).toUpperCase() + name.substring(1);
        if (getCurrentDimID() == -1) {
            x *= 8;
            z *= 8;
        }

        Color color = getColor(name);
        return "\tS:marker" + id + "=" + upName + ":" +
                x + ":" + y + ":" + z + ":" + getCurrentDimID() + ":" +
                color.hex + ":" + upName + "\n";
    }

    private int getCurrentDimID() {
        return getDimID(String.valueOf(cbDim.getSelectedItem()).toLowerCase());
    }

    private int getDimID(String dim) {
        if (Dimensions.knownDimensions.containsKey(dim))
            return Dimensions.knownDimensions.get(dim);
        JOptionPane.showMessageDialog(this, "Ask somebody to add the dimension to code.", "Unknown dimension: " + dim, JOptionPane.WARNING_MESSAGE);
        Dimensions.knownDimensions.put(dim, 0);
        return 0;
    }

    private void setButtons() {
        setButton("Calculate", 10, 40, 95, 25, this::calculate);
        setButton("JM Export", 110, 40, 95, 25, this::exportJM);
        setButton("MW Export", 210, 40, 105, 25, this::exportMW);
        JButton btnRefresh = setButton("↻", 300, 10, 25, 25, this::refresh);
        btnRefresh.setMargin(new Insets(0, 0, 0, 0));
    }

    private void refresh() {
        tfSeed.setText("");
        calculate();
    }

    private JButton setButton(String name, int x, int y, int w, int h, Runnable action) {
        JButton jButton = new JButton(name);
        jButton.addActionListener(e -> action.run());
        jButton.setBounds(x, y, w, h);
        add(jButton);
        return jButton;
    }

    private void setInputs() {
        tfSeed = setTextField(100, 10, 200, 25);
        tfSize = setTextField(380, 10, 50, 25);
        tfOffsetX = setTextField(390, 40, 50, 25);
        tfOffsetZ = setTextField(470, 40, 50, 25);
    }

    private JTextField setTextField(int x, int y, int w, int h) {
        JTextField jTextField = new JTextField();
        jTextField.setBounds(x, y, w, h);
        add(jTextField);
        return jTextField;
    }

    private void setLabels() {
        setLabel("World seed:", 10, 10, 70, 25);
        setLabel("Size:", 340, 10, 50, 25);
        setLabel("Offset", 320, 40, 50, 25);
        setLabel("X:", 365, 40, 50, 25);
        setLabel("Z:", 450, 40, 50, 25);
    }

    private void setLabel(String text, int x, int y, int w, int h) {
        JLabel jLabel = new JLabel(text);
        jLabel.setBounds(x, y, w, h);
        add(jLabel);
    }

    private void setTextAreas() {
        taOutput = setTextArea(10, 70, 635, 375);
        taOutput.setFont(new Font("monospaced", Font.PLAIN, 12));
    }

    private JTextArea setTextArea(int x, int y, int w, int h) {
        JTextArea jTextArea = new JTextArea();
        jTextArea.setBounds(x, y, w, h);
        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        jScrollPane.setBounds(x, y, w, h);
        add(jScrollPane);
        return jTextArea;
    }

    private void setComboBoxes() {
        String[] dims = OreVein.ores.stream().flatMap(v -> v.dims.stream()).distinct().toArray(String[]::new);
        Arrays.sort(dims);
        cbDim = setComboBox(440, 10, 100, 25, dims);
        cbDim.addActionListener(this::refreshOres);
        cbOre = setComboBox(550, 10, 100, 25);
    }

    @SafeVarargs
    private final <T> JComboBox<T> setComboBox(int x, int y, int w, int h, T... values) {
        JComboBox<T> jComboBox = new JComboBox<>();
        for (T value : values) jComboBox.addItem(value);
        jComboBox.setBounds(x, y, w, h);
        add(jComboBox);
        return jComboBox;
    }

    private void refreshOres(ActionEvent e) {
        String oreName = String.valueOf(cbOre.getSelectedItem()).toLowerCase();
        String dim = String.valueOf(cbDim.getSelectedItem()).toLowerCase();
        List<String> filteredOres = new ArrayList<>();
        for (OreVein ore : OreVein.ores) if (ore.dims.contains(dim)) filteredOres.add(ore.name);

        String[] ores = filteredOres.toArray(new String[0]);
        Arrays.sort(ores);
        cbOre.removeAllItems();
        if (ores.length > 1) cbOre.addItem("all");
        for (String ore : ores) cbOre.addItem(ore);

        if (filteredOres.contains(oreName)) cbOre.setSelectedItem(oreName);
    }

    private Color getColor(String name) {
        return new Color(name.hashCode());
    }

    private static class Color {
        public int red;
        public int green;
        public int blue;

        public String hex;

        public Color(int hash) {
            red = (hash) & 0xff;
            green = (hash >> 8) & 0xff;
            blue = (hash >> 16) & 0xff;
            hex = Integer.toHexString(hash & 0xffffff);
        }
    }
}