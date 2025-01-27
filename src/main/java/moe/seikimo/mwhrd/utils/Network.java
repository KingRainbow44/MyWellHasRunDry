package moe.seikimo.mwhrd.utils;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.util.math.MathHelper;

public interface Network {
    /**
     * Creates a {@link EntitySetHeadYawS2CPacket} packet instance.
     *
     * @param base The entity to use to construct the packet.
     * @param entityId The entity ID.
     * @param headYaw The head yaw. Should be packed with {@link MathHelper#packDegrees(float)}
     * @return The packet instance.
     */
    static EntitySetHeadYawS2CPacket setHeadYaw(Entity base, int entityId, byte headYaw) {
        var packet = new EntitySetHeadYawS2CPacket(base, headYaw);
        packet.entityId = entityId;

        return packet;
    }
}
