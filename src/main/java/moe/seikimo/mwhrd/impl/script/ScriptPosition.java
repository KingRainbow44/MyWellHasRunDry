package moe.seikimo.mwhrd.impl.script;

import moe.seikimo.mwhrd.script.ScriptObject;

/**
 * Represents a position in a world.
 * Includes a pitch/yaw component for rotation.
 *
 * @param x The x coordinate.
 * @param y The y coordinate.
 * @param z The z coordinate.
 * @param pitch The pitch.
 * @param yaw The yaw.
 * @param dimension The dimension.
 */
public record ScriptPosition(
    double x, double y, double z,
    float pitch, float yaw,
    String dimension
) implements ScriptObject {
}
