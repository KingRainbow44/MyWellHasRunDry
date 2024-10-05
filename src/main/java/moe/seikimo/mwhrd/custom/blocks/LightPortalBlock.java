package moe.seikimo.mwhrd.custom.blocks;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.custom.CustomWorlds;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public final class LightPortalBlock extends AbstractCustomPortal {
    @Override
    protected ContextPredicate getFrameValidator() {
        return (state, world, pos) -> state.isOf(Blocks.GLOWSTONE);
    }

    @Override
    public TeleportTarget createTeleportTarget(ServerWorld world, Entity entity, BlockPos pos) {
        var dimension = MyWellHasRunDry.getRealmOfLight();
        var targetPos = Vec3d.ZERO;

        // Check if the world is currently the Realm of Light.
        if (world.getRegistryKey() == CustomWorlds.REALM_OF_LIGHT) {
            // We should redirect them to the Overworld.
            dimension = world.getServer().getWorld(ServerWorld.OVERWORLD);
            if (dimension == null) throw new IllegalStateException("Overworld is not loaded.");

            // Teleport the entity to the overworld's spawn point.
            targetPos = dimension.getSpawnPos().toCenterPos();

            // If the entity is a player, teleport them to their spawn point.
            if (entity instanceof ServerPlayerEntity player) {
                return player.getRespawnTarget(false, TeleportTarget.NO_OP);
            }
        }

        // These portals always work by sending the player to the same point in the other dimension.
        return new TeleportTarget(
            dimension,
            targetPos, Vec3d.ZERO,
            entity.getYaw(), entity.getPitch(),
            TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET
                .then(e -> e.addPortalChunkTicketAt(pos))
        );
    }
}
