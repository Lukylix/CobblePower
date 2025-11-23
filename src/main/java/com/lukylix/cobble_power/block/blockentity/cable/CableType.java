package com.lukylix.cobble_power.block.blockentity.cable;

public class CableType {

    public static final CableType IRON = new CableType("iron", 1000, Tier.BASIC);
    private final String name;
    private final long transferRate;

    public CableType(String name, long transferRate, Tier tier) {
        this.name = name;
        this.transferRate = transferRate;
    }

    public long getTransferRate() {
        return transferRate;
    }


    @Override
    public String toString() {
        return name.toUpperCase();
    }

    // Simple tier enum
    public enum Tier {
        BASIC,
        ADVANCED,
        INDUSTRIAL
    }
}
