package moe.seikimo.mwhrd.custom.blocks;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.game.lightrealm.TheRealmOfLight;
import moe.seikimo.mwhrd.interfaces.ITimeTraveler;
import moe.seikimo.mwhrd.utils.Players;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public final class LightPortalBlock extends AbstractCustomPortal {
    public LightPortalBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ContextPredicate getFrameValidator() {
        return (state, world, pos) -> state.isOf(Blocks.GLOWSTONE);
    }

    @Override
    protected void transportEntity(Entity entity, BlockPos pos) {
        // Non-players can't travel into the realm of light.
        if (!(entity instanceof PlayerEntity player)) return;
        if (!(player instanceof ITimeTraveler traveler)) return;

        if (player.canUsePortals(false)) {
            // Check if the player is already in the Realm of Light.
            if (Players.inWorld(MyWellHasRunDry.getRealmOfLight(), player)) {
                // Run the portal's teleportation logic.
                player.tryUsePortal(this, pos);
                return;
            }

            // Set the player's queued portal.
            traveler.mwhrd$setQueuedPortal(new Pair<>(this, pos));

            // Queue the player to be teleported.
            TheRealmOfLight.getWorld().queuePlayer(player);
            player.sendMessage(Text.translatable("text.mwhrd.dimension.rol.queued")
                .formatted(Formatting.YELLOW), true);
        }
    }

    @Override
    public TeleportTarget createTeleportTarget(ServerWorld world, Entity entity, BlockPos pos) {
        var dimension = MyWellHasRunDry.getRealmOfLight().asWorld();
        var targetPos = new Vec3d(0, -50, 0);

        // Check if the world is currently the Realm of Light.
        if (Utils.compare(world, MyWellHasRunDry.getRealmOfLight())) {
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
