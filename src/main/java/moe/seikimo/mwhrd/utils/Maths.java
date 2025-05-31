package moe.seikimo.mwhrd.utils;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public interface Maths {
    /**
     * Constant for converting radians to degrees.
     */
    float RAD_TO_DEG = 57.2957763671875f;

    /**
     * Computes the pitch and yaw angles to look at a target.
     *
     * @param src The source position.
     * @param target The target position.
     * @return The pitch and yaw angles.
     */
    static Vec2f lookAt(Vec3d src, Vec3d target) {
        var d = target.x - src.x;
        var e = target.y - src.y;
        var f = target.z - src.z;
        var g = Math.sqrt(d * d + f * f);

        var pitch = (float) MathHelper.wrapDegrees(MathHelper.atan2(e, g) * RAD_TO_DEG);
        var yaw = (float) MathHelper.wrapDegrees(MathHelper.atan2(f, d) * RAD_TO_DEG - 90.0f);

        return new Vec2f(pitch, yaw);
    }
}
