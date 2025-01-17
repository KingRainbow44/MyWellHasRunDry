package moe.seikimo.mwhrd.custom.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Component for guns.
 * Handles reloading.
 *
 * @param reloading Whether the gun is reloading or not.
 * @param remainingTicks The number of ticks remaining until the gun is reloaded.
 */
public record ReloadComponent(
    boolean reloading,
    int remainingTicks,
    int newCount
) {
    public static final ReloadComponent EMPTY = new ReloadComponent(false, 0, -1);

    public static final Codec<ReloadComponent> CODEC = RecordCodecBuilder.create(builder ->
        builder.group(
            Codec.BOOL.fieldOf("reloading").forGetter(ReloadComponent::reloading),
            Codec.INT.fieldOf("remainingTicks").forGetter(ReloadComponent::remainingTicks),
            Codec.INT.fieldOf("newCount").forGetter(ReloadComponent::newCount)
        ).apply(builder, ReloadComponent::new)
    );

    /**
     * Creates a new reload component.
     *
     * @param ticks The number of ticks until the gun is reloaded.
     * @param count The new ammo of the gun.
     * @return A new reload component.
     */
    public static ReloadComponent of(int ticks, int count) {
        return new ReloadComponent(true, ticks, count);
    }

    /**
     * Updates and re-validates the component.
     *
     * @return A new reload component, minus one.
     */
    public ReloadComponent minus() {
        return new ReloadComponent(
            this.remainingTicks > 1,
            this.remainingTicks - 1,
            this.newCount
        );
    }
}
