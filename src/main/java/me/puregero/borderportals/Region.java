package me.puregero.borderportals;

public class Region {

    private final String server;
    private final int x1;
    private final int z1;
    private final int x2;
    private final int z2;

    public Region(String server, int x1, int z1, int x2, int z2) {
        this.server = server;
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
    }

    public String getServer() {
        return server;
    }

    public boolean inBounds(int x, int z) {
        return x1 <= x && x <= x2 && z1 <= z && z <= z2;
    }
}
