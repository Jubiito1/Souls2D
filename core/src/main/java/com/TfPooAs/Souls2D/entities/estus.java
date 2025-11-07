package com.TfPooAs.Souls2D.entities;

public class estus {
    private int charges = 3; // cargas por run
    private int healAmount = 45; // cantidad a curar por uso

    public estus() {}

    public boolean use(Player player) {
        if (charges <= 0) {
            System.out.println("[Estus] Sin cargas");
            return false;
        }
        if (player == null || player.isDead()) return false;

        charges--;
        player.heal(healAmount);
        player.startHealing();
        System.out.println("[Estus] Usado. Cargas restantes: " + charges);
        return true;
    }

    public int getCharges() { return charges; }
    public void setCharges(int charges) { this.charges = charges; }
    public int getHealAmount() { return healAmount; }
    public void setHealAmount(int healAmount) { this.healAmount = healAmount; }
}
