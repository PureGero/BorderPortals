package me.puregero.borderportals;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PortalRendererListener implements Listener {

    private static final int[] EAST = new int[] { 1, 0 };
    private static final int[] WEST = new int[] { -1, 0 };
    private static final int[] NORTH = new int[] { 0, -1 };
    private static final int[] SOUTH = new int[] { 0, 1 };

    private final BorderPortals borderPortals;

    public PortalRendererListener(BorderPortals borderPortals) {
        this.borderPortals = borderPortals;
    }

    private HashMap<Player, Set<Block>> renderedPortalBlocks = new HashMap<>();
    private Set<Block> currentRenderedBlocks = new HashSet<>();

    @EventHandler
    public void renderPortalListener(PlayerMoveEvent event) {
        // If they've moved a considerable distance
        if (!event.getTo().getBlock().equals(event.getFrom().getBlock())) {
            currentRenderedBlocks.clear();

            String myServer = borderPortals.getServerAt(event.getTo());
            for (int[] dir : new int[][]{ EAST, WEST, NORTH, SOUTH }) {
                int x = dir[0] * 5;
                int z = dir[1] * 5;
                String server = borderPortals.getServerAt(event.getTo().clone().add(x, 0, z));
                if (!stringEquals(server, myServer)) {
                    renderPortal(event.getPlayer(), event.getTo(), myServer, dir);
                }
            }

            // Cleanup any old portals
            Set<Block> renderedBlocks = renderedPortalBlocks.remove(event.getPlayer());
            if (renderedBlocks != null) {
                for (Block block : renderedBlocks) {
                    if (!currentRenderedBlocks.contains(block)) {
                        event.getPlayer().sendBlockChange(block.getLocation(), block.getBlockData());
                        block.getState().update(true, false);
                    }
                }
            }

            if (!currentRenderedBlocks.isEmpty()) {
                renderedPortalBlocks.put(event.getPlayer(), new HashSet<>(currentRenderedBlocks));
            }
        }
    }

    /**
     * Compare Strings that may be null
     */
    private boolean stringEquals(String string1, String string2) {
        return (string1 == null && string2 == null) || (string1 != null && string1.equals(string2));
    }

    private void renderPortal(Player player, Location location, String myServer, int[] dir) {
        int dx = dir[0];
        int dz = dir[1];

        for (int i = 0; i <= 5; i++) {
            String server = borderPortals.getServerAt(location.clone().add(dx * i, 0, dz * i));
            if (!stringEquals(server, myServer)) {
                Location signLoc = location.clone().add(dx * i, 1, dz * i);
                Block signBlock = signLoc.getBlock();
                if (!signBlock.getType().isOccluding()) {
                    player.sendBlockChange(signLoc, createWallSign(dir));
                    player.sendSignChange(signLoc, new String[]{ "Teleport to", "server", server });
                    currentRenderedBlocks.add(signBlock);
                }

                for (int j = -5 + i; j <= 5 - i; j++) {
                    for (int k = -5 + i + Math.abs(j); k <= 5 - i - Math.abs(j); k++) {
                        Location loc = location.clone().add(dx * i + dz * j, k + 1, dz * i + dx * j);
                        Block block = loc.getBlock();
                        if (!block.getType().isOccluding()) {
                            player.sendBlockChange(loc, createPortal(dir));
                            currentRenderedBlocks.add(block);
                        }
                    }
                }
                break;
            }
        }
    }

    private BlockData createWallSign(int[] dir) {
        Sign sign = (Sign) Material.OAK_WALL_SIGN.createBlockData();
        if (dir == EAST) sign.setRotation(BlockFace.WEST);
        if (dir == WEST) sign.setRotation(BlockFace.EAST);
        if (dir == NORTH) sign.setRotation(BlockFace.SOUTH);
        if (dir == SOUTH) sign.setRotation(BlockFace.NORTH);
        return sign;
    }

    private BlockData createPortal(int[] dir) {
        Orientable portal = (Orientable) Material.NETHER_PORTAL.createBlockData();
        if (dir == EAST || dir == WEST) portal.setAxis(Axis.Z);
        if (dir == NORTH || dir == SOUTH) portal.setAxis(Axis.X);
        return portal;
    }
}
