package moe.seikimo.mwhrd.impl.holder;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import moe.seikimo.mwhrd.utils.Maths;
import moe.seikimo.mwhrd.utils.Network;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;

/**
 * This will force all elements to stare at viewers.
 */
public final class StarePlayerHolder extends ElementHolder {
    @Override
    public void tick() {
        super.tick();

        for (var viewer : this.getWatchingPlayers()) {
            var player = viewer.getPlayer();

            // Send rotation packet to viewer.
            for (var element : this.getElements()) {
                var batch = new HashSet<Packet<? super ClientPlayPacketListener>>();

                // Compute rotation.
                var playerPos = player.getEntityPos();
                var elementPos = element.getCurrentPos();
                var rotation = Maths.lookAt(playerPos, elementPos);

                for (var entityId : element.getEntityIds()) {
                    batch.add(new EntityS2CPacket.Rotate(
                        entityId,
                        MathHelper.packDegrees(rotation.y),
                        MathHelper.packDegrees(rotation.x),
                        false
                    ));

                    var headYaw = MathHelper.packDegrees(rotation.y - 180);
                    batch.add(Network.setHeadYaw(player, entityId, headYaw));
                }

                viewer.sendPacket(new BundleS2CPacket(batch));
            }
        }
    }
}
