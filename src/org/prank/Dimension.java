package org.prank;

import java.util.*;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class Dimension {

    private final int id;
    private final String name;

    private BiMap<String, FluidVein> fluids = HashBiMap.create();;

    private int totalChanceOfUOFluids = 0;

    public Dimension(String name, int id) {
        this.id = id;
        this.name = name;
    }

    public Dimension(String name, int id, BiMap<String, FluidVein> fluids) {
        this.id = id;
        this.name = name;
        this.fluids = fluids;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public int getTotalChanceOfUOFluids() {
        return totalChanceOfUOFluids;
    }

    public FluidVein getRandomFluid(Random aRandom) {
        int random = aRandom.nextInt(1000);
        FluidVein f = null;
        for (BiMap.Entry<String, FluidVein> fl : fluids.entrySet()) {
            int chance = fl.getValue().chance * 1000 / getTotalChanceOfUOFluids();
            if (random <= chance) {
                return fl.getValue();
            }
            random -= chance;
            f = fl.getValue();
        }
        return f;
    }

    public void setTotalChanceOfUOFluids(int totalChanceOfUOFluids) {
        this.totalChanceOfUOFluids = totalChanceOfUOFluids;
    }

    public BiMap<String, FluidVein> getFluids() {
        return fluids;
    }

    public static List<Dimension> dimensions = new ArrayList<>();

    public static Dimension getDimensionById(int id) {
        return dimensions.stream()
                .filter(dimension -> dimension.id == id)
                .findFirst()
                .orElse(null);
    }

    public static Dimension getDimensionByName(String name) {
        return dimensions.stream()
                .filter(dimension -> dimension.name.equals(name.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    public static boolean dimensionExistById(int id) {
        return dimensions.stream()
                .anyMatch(dimension -> dimension.id == id);
    }

    public static boolean dimensionExistByName(String name) {
        return dimensions.stream()
                .anyMatch(dimension -> dimension.name.equals(name.toLowerCase()));
    }

    static {
        dimensions.add(new Dimension("overworld", 0));
        dimensions.add(new Dimension("nether", -1));
        dimensions.add(new Dimension("theend", 1));
        dimensions.add(new Dimension("endasteroid", 1));
        dimensions.add(new Dimension("moon", -28));
        dimensions.add(new Dimension("mars", -29));
        dimensions.add(new Dimension("asteroid", -30));
        dimensions.add(new Dimension("barnarda_c", -1022));
        dimensions.add(new Dimension("barnarda_e", -1023));
        dimensions.add(new Dimension("barnarda_f", -1026));
        dimensions.add(new Dimension("callisto", -1024));
        dimensions.add(new Dimension("deimos", -1012));
        dimensions.add(new Dimension("enceladus", -1016));
        dimensions.add(new Dimension("europa", -1014));
        dimensions.add(new Dimension("ganymede", -1015));
        dimensions.add(new Dimension("haumea", -1025));
        dimensions.add(new Dimension("io", -1013));
        dimensions.add(new Dimension("kuiper", -1009));
        dimensions.add(new Dimension("makemake", -1010));
        dimensions.add(new Dimension("mercury", -1005));
        dimensions.add(new Dimension("miranda", -1029));
        dimensions.add(new Dimension("oberon", -1018));
        dimensions.add(new Dimension("phobos", -1011));
        dimensions.add(new Dimension("pluto", -1008));
        dimensions.add(new Dimension("proteus", -1019));
        dimensions.add(new Dimension("t_ceti_e", -1028));
        dimensions.add(new Dimension("titan", -1017));
        dimensions.add(new Dimension("triton", -1020));
        dimensions.add(new Dimension("vegab", -1027));
        dimensions.add(new Dimension("venus", -1006));
    }
}
