package me.puregero.borderportals;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.WallSign;
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
                int x = dir[0];
                int z = dir[1];
                if (!stringEquals(borderPortals.getServerAt(event.getTo().clone().add(x * 5 + z * 5, 0, z * 5 + x * 5)), myServer)
                        || !stringEquals(borderPortals.getServerAt(event.getTo().clone().add(x * 5 - z * 5, 0, z * 5 - x * 5)), myServer)) {
                    renderPortal(event.getPlayer(), event.getTo(), myServer, dir);
                }
            }

            // Cleanup any old portals
            Set<Block> renderedBlocks = renderedPortalBlocks.remove(event.getPlayer());
            if (renderedBlocks != null) {
                for (Block block : renderedBlocks) {
                    if (!currentRenderedBlocks.contains(block)) {
                        event.getPlayer().sendBlockChange(block.getLocation(), block.getBlockData());
                        BlockState state = block.getState();
                        if (!state.getClass().getSimpleName().equals("CraftBlockState")) {
                            block.getState().update(true, false);
                        }
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

        for (int j = -5; j <= 5; j++) { // Left/right axis
            for (int i = 0; i <= 5 - Math.abs(j); i++) { // Forward axis
                String server = borderPortals.getServerAt(location.clone().add(dx * (i + 1) + dz * j, 0, dz * (i + 1) + dx * j));
                if (!stringEquals(server, myServer)) {
                    if (j == 0) {
                        Location signLoc = location.clone().add(dx * (i - 1), 1, dz * (i - 1));
                        Block signBlock = signLoc.getBlock();
                        if (!signBlock.getType().isOccluding()) {
                            player.sendBlockChange(signLoc, createWallSign(dir));
                            player.sendSignChange(signLoc, new String[]{ "Teleport to", "server", server, "" });
                            currentRenderedBlocks.add(signBlock);
                        }
                    }

                    for (int y = -5 + i + Math.abs(j); y <= 5 - i - Math.abs(j); y++) {
                        Location loc = location.clone().add(dx * i + dz * j, y + 1, dz * i + dx * j);
                        Block block = loc.getBlock();
                        if (!block.getType().isOccluding()) {
                            player.sendBlockChange(loc, createPortal(dir));
                            currentRenderedBlocks.add(block);
                        }
                    }
                }
            }
        }
    }

    private BlockData createWallSign(int[] dir) {
        WallSign sign = (WallSign) Material.OAK_WALL_SIGN.createBlockData();
        if (dir == EAST) sign.setFacing(BlockFace.WEST);
        if (dir == WEST) sign.setFacing(BlockFace.EAST);
        if (dir == NORTH) sign.setFacing(BlockFace.SOUTH);
        if (dir == SOUTH) sign.setFacing(BlockFace.NORTH);
        return sign;
    }

    private BlockData createPortal(int[] dir) {
        Orientable portal = (Orientable) Material.NETHER_PORTAL.createBlockData();
        if (dir == EAST || dir == WEST) portal.setAxis(Axis.Z);
        if (dir == NORTH || dir == SOUTH) portal.setAxis(Axis.X);
        return portal;
    }
}
