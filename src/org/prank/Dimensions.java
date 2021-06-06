package org.prank;

import java.util.HashMap;
import java.util.Map;

public class Dimensions {
    public static final Map<String, Integer> knownDimensions = new HashMap<>();

    static {
        knownDimensions.put("overworld", 0);
        knownDimensions.put("nether", -1);
        knownDimensions.put("theend", 1);
        knownDimensions.put("endasteroid", 1);
        knownDimensions.put("moon", -28);
        knownDimensions.put("mars", -29);
        knownDimensions.put("asteroid", -30);
        knownDimensions.put("barnarda_c", -1022);
        knownDimensions.put("barnarda_e", -1023);
        knownDimensions.put("barnarda_f", -1026);
        knownDimensions.put("callisto", -1024);
        knownDimensions.put("deimos", -1012);
        knownDimensions.put("enceladus", -1016);
        knownDimensions.put("europa", -1014);
        knownDimensions.put("ganymede", -1015);
        knownDimensions.put("haumea", -1025);
        knownDimensions.put("io", -1013);
        knownDimensions.put("kuiper", -1009);
        knownDimensions.put("makemake", -1010);
        knownDimensions.put("mercury", -1005);
        knownDimensions.put("miranda", -1029);
        knownDimensions.put("oberon", -1018);
        knownDimensions.put("phobos", -1011);
        knownDimensions.put("pluto", -1008);
        knownDimensions.put("proteus", -1019);
        knownDimensions.put("t_ceti_e", -1028);
        knownDimensions.put("titan", -1017);
        knownDimensions.put("triton", -1020);
        knownDimensions.put("vegab", -1027);
        knownDimensions.put("venus", -1006);
    }
}
