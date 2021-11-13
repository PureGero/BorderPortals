package me.puregero.serversplitter;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ServerSplitter extends JavaPlugin {

    private List<Region> regions = new ArrayList<>();

    @Override
    public void onEnable() {
        regions.add(new Region("server", -100, -100, 100, 100));

        getServer().getPluginManager().registerEvents(new PortalRendererListener(this), this);

        Metrics metrics = new Metrics(this, 13309);
        metrics.addCustomChart(new SimplePie("server_count", () -> Long.toString(regions.stream().map(region -> region.getServer()).distinct().count())));
    }

    public String getServerAt(Block block) {
        return getServerAt(block.getX(), block.getZ());
    }

    public String getServerAt(Location location) {
        return getServerAt(location.getBlockX(), location.getBlockZ());
    }

    public String getServerAt(int x, int z) {
        for (Region region : regions) {
            if (region.inBounds(x, z)) {
                return region.getServer();
            }
        }
        return null;
    }



}
