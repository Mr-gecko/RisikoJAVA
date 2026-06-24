package model;

import java.io.Serializable;

import model.enums.AttackResult;

public class AttackReport implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final AttackResult result;
    private final int attackerLosses;
    private final int defenderLosses;
    private final int attackerRemaining;
    private final int defenderRemaining;
    
    public AttackReport(AttackResult result, int attackerLosses, int defenderLosses, int attackerRemaining, int defenderRemaining) {
        this.result = result;
        this.attackerLosses = attackerLosses;
        this.defenderLosses = defenderLosses;
        this.attackerRemaining = attackerRemaining;
        this.defenderRemaining = defenderRemaining;
    }
    
    public AttackResult getResult() { return result; }
    public int getAttackerLosses() { return attackerLosses; }
    public int getDefenderLosses() { return defenderLosses; }
    public int getAttackerRemaining() { return attackerRemaining; }
    public int getDefenderRemaining() { return defenderRemaining; }
    
    public boolean isCloseFight() {
        int total = attackerLosses + defenderLosses;
        if (total == 0) return false;
        double ratio = (double) Math.abs(attackerLosses - defenderLosses) / total;
        return ratio < 0.2; // less than 20% difference = close fight
    }
}