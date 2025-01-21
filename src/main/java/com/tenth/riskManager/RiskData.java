package com.tenth.riskManager;

public class RiskData {
    private int riskLevel;
    private boolean isFlagged;

    public RiskData(int riskLevel, boolean isFlagged) {
        this.riskLevel = riskLevel;
        this.isFlagged = isFlagged;
    }

    public int getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(int riskLevel) {
        this.riskLevel = riskLevel;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setFlagged(boolean flagged) {
        isFlagged = flagged;
    }
}
