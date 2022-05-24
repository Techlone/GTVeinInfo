package org.prank;

public class FluidVein {

    public final String name;
    public final int max;
    public final int min;
    public final int chance;
    public final String dimensionId;
    public final int amountPerOperation;

    public FluidVein(String name, int max, int min, String dimensionId, int amountPerOperation, int chance) {
        this.name = name;
        this.max = max;
        this.min = min;
        this.dimensionId = dimensionId;
        this.amountPerOperation = amountPerOperation;
        this.chance = chance;
    }
}
