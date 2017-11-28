package org.prank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {
    public static Map<Coord, String> result = new HashMap<>();

    private JTextField tfSeed;
    private JTextField tfSize;
    private JTextField tfOffsetX;
    private JTextField tfOffsetZ;
    private JComboBox<String> cbDim;
    private JComboBox<String> cbOre;
    private JTextArea taOutput;
    ;

    public MainFrame() throws HeadlessException {
        super("GTVeinInfo");
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });

        setSize(640, 480);
        setResizable(false);
        setLayout(null);

        setLabels();
        setInputs();
        setButtons();
        setTextAreas();
        setComboBoxes();

        setVisible(true);

        tfSeed.setText("123");
        tfSize.setText("10");
        tfOffsetX.setText("0");
        tfOffsetZ.setText("0");
    }

    public void generate(long wSeed, int chX, int chZ, String dim) {
        Random fmlRandom = new Random(wSeed);
        long xSeed = fmlRandom.nextLong() >> 2 + 1L;
        long zSeed = fmlRandom.nextLong() >> 2 + 1L;
        //8487375766640924151
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
        long seed = Long.parseLong(tfSeed.getText());
        int size = Integer.parseInt(tfSize.getText()) * 3;
        int offsetX = Integer.parseInt(tfOffsetX.getText()) >> 4;
        int offsetZ = Integer.parseInt(tfOffsetZ.getText()) >> 4;
        String dim = String.valueOf(cbDim.getSelectedItem());

        for (int x = -size; x < size; x++)
            for (int z = -size; z < size; z++)
                generate(seed, x + offsetX, z + offsetZ, dim);

        List<Coord> coords = result.keySet().stream().collect(Collectors.toList());
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

    private void export() {
        if (result.isEmpty())
            calculate();
        File waypoints = new File("waypoints");
        if (!waypoints.exists()) {
            waypoints.mkdir();
        }
        result.forEach(this::writeWayPoint);
    }

    private void writeWayPoint(Coord coord, String name) {
        String upName = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
        String wpName = upName + "_" + coord.x + "," + coord.y + "," + coord.z;
        String fileName = "waypoints/" + wpName + "." + getCurrentDimID() + ".json";
        
        // Translate chunk coords to block coords
        int x = coord.x * 16 + 8;
        int y = coord.y;
        int z = coord.z * 16 + 8;

        // Stretch coords if nether dimension due to JourneyMap squeezing
        if (getCurrentDimID() == -1)
        {
            x *= 8;
            z *= 8;
        }

        int hash = name.hashCode();
        StringBuilder sb = new StringBuilder("{")
                .append("\"id\": \"").append(wpName).append("\", ")
                .append("\"name\": \"").append(upName).append("\", ")
                .append("\"icon\": \"waypoint-normal.png\", ")
                .append("\"x\": ").append(x).append(", ")
                .append("\"y\": ").append(y).append(", ")
                .append("\"z\": ").append(z).append(", ")
                .append("\"r\": ").append((hash) & 0xff).append(", ")
                .append("\"g\": ").append((hash >> 8) & 0xff).append(", ")
                .append("\"b\": ").append((hash >> 16) & 0xff).append(", ")
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

    private int getCurrentDimID() {
        String dim = String.valueOf(cbDim.getSelectedItem()).toLowerCase();
        switch (dim) {
            case "overworld":
                return 0;
            case "nether":
                return -1;
            case "theend":
            case "endasteroid":
                return 1;
            case "moon":
                return -28;
            case "mars":
                return -29;
            case "asteroid":
                return -30;
        }
        return 0;
    }

    private void setButtons() {
        setButton("Calculate", 10, 40, 95, 25, this::calculate);
        setButton("JM Export", 110, 40, 95, 25, this::export);
    }

    private void setButton(String name, int x, int y, int w, int h, Runnable action) {
        JButton jButton = new JButton(name);
        jButton.addActionListener(e -> action.run());
        jButton.setBounds(x, y, w, h);
        add(jButton);
    }

    private void setInputs() {
        tfSeed = setTextField(100, 10, 200, 25);
        tfSize = setTextField(380, 10, 50, 25);
        tfOffsetX = setTextField(300, 40, 50, 25);
        tfOffsetZ = setTextField(380, 40, 50, 25);
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
        setLabel("Offset", 230, 40, 50, 25);
        setLabel("X:", 280, 40, 50, 25);
        setLabel("Z:", 360, 40, 50, 25);
    }

    private JLabel setLabel(String text, int x, int y, int w, int h) {
        JLabel jLabel = new JLabel(text);
        jLabel.setBounds(x, y, w, h);
        add(jLabel);
        return jLabel;
    }

    private void setTextAreas() {
        taOutput = setTextArea(10, 70, 615, 375);
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
        cbDim.setSelectedItem("overworld");
        List<String> veins = new ArrayList<>(OreVein.ores.size() + 1);
        for (OreVein ore : OreVein.ores)
            if (!veins.contains(ore.name))
                veins.add(Character.isDigit(ore.name.charAt(0)) ? "custom." + ore.name : ore.name);
        Collections.sort(veins);
        veins.add(0, "all");
        cbOre = setComboBox(440, 40, 100, 25, veins.toArray(new String[0]));
    }

    @SafeVarargs
    private final <T> JComboBox<T> setComboBox(int x, int y, int w, int h, T... values) {
        JComboBox<T> jComboBox = new JComboBox<>();
        for (T value : values) jComboBox.addItem(value);
        jComboBox.setBounds(x, y, w, h);
        add(jComboBox);
        return jComboBox;
    }

    private JCheckBox setCheckBox(String name, int x, int y, int w, int h) {
        JCheckBox jCheckBox = new JCheckBox(name);
        jCheckBox.setBounds(x, y, w, h);
        add(jCheckBox);
        return jCheckBox;
    }
}
