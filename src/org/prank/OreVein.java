package org.prank;

import java.util.*;

public class OreVein {
    public static final List<OreVein> ores = new ArrayList<>();
    public static int totalWeight = 0;

    static {
        //IIA v 1.10.9
        add("naquadah", 10, 60, 10, "theend", "endasteroid");
        add("lignite", 50, 130, 100, "overworld");
        add("coal", 50, 80, 80, "overworld");
        add("magnetite", 50, 120, 160, "overworld", "nether");
        add("gold", 60, 80, 160, "overworld", "moon");
        add("iron", 10, 40, 120, "overworld", "nether");
        add("cassiterite", 40, 120, 100, "overworld");
        add("tetrahedrite", 80, 120, 80, "overworld", "nether");
        add("netherquartz", 40, 80, 80, "nether");
        add("sulfur", 5, 20, 100, "nether");
        add("copper", 10, 30, 80, "overworld", "nether");
        add("bauxite", 50, 90, 80, "overworld", "moon");
        add("salts", 50, 60, 50, "overworld");
        add("redstone", 10, 40, 60, "overworld", "nether", "moon", "mars");
        add("soapstone", 10, 40, 40, "overworld");
        add("nickel", 10, 40, 40, "overworld", "nether", "theend", "endasteroid");
        add("platinum", 40, 50, 5, "overworld", "theend", "endasteroid", "mars");
        add("pitchblende", 10, 40, 40, "overworld");
        add("uranium", 20, 30, 20, "overworld", "moon");
        add("monazite", 20, 40, 30, "overworld", "mars");
        add("molybdenum", 20, 40, 5, "overworld", "theend", "endasteroid");
        add("tungstate", 20, 50, 10, "overworld", "theend", "endasteroid", "moon");
        add("sapphire", 10, 40, 60, "overworld");
        add("manganese", 20, 30, 20, "overworld", "theend", "endasteroid");
        add("quartz", 40, 80, 60, "overworld", "moon");
        add("diamond", 5, 20, 40, "overworld");
        add("olivine", 10, 40, 60, "overworld", "theend", "endasteroid");
        add("apatite", 40, 60, 60, "overworld");
        add("galena", 30, 60, 40, "overworld");
        add("lapis", 20, 50, 40, "overworld", "theend", "endasteroid", "moon");
        add("beryllium", 5, 30, 30, "overworld", "theend", "endasteroid");
        add("oilsand", 50, 80, 80, "overworld");
        add("00", 20, 50, 20, "moon");
        add("01", 50, 120, 50, "mars");
        add("02", 5, 40, 20, "mars");
        add("03", 10, 20, 10, "mars");
        add("04", 20, 200, 30, "asteroid");
    }

    public static void add(String name, int min, int max, int weight, String... dims) {
        add(new OreVein(name, min, max, weight, dims));
    }

    public static void add(OreVein vein) {
        totalWeight += vein.weight;
        ores.add(vein);
    }

    public static void clear() {
        totalWeight = 0;
        ores.clear();
    }

    public final String name;
    public int max;
    public int min;
    public Set<String> dims = new HashSet<>();
    public int weight;

    public OreVein(String name) {
        this.name = Character.isDigit(name.charAt(0)) ? "custom." + name : name;
    }

    public OreVein(String name, int min, int max, int weight, String[] dims) {
        this(name);
        this.max = max;
        this.min = min;
        this.weight = weight;
        Collections.addAll(this.dims, Arrays.stream(dims).map(String::toLowerCase).toArray(String[]::new));
    }

    public int executeWorldgen(Random random, String dim, int chX, int chZ) {
        if (!dims.contains(dim)) return -1;
        return min + random.nextInt(max - min - 5);
    }
}
