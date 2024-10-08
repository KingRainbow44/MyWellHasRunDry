package moe.seikimo.mwhrd;

import moe.seikimo.mwhrd.game.lightrealm.LightTowerGenerator;
import net.minecraft.util.math.Vec3i;

public final class LightTowerGeneratorTest {
    private static final Vec3i[] POSITIONS = {
        new Vec3i(0, -56, 0),
        new Vec3i(5, -47, 0),
        new Vec3i(0, -38, 5),
        new Vec3i(-5, -29, 0),
        new Vec3i(0, -20, -5),
        new Vec3i(0, -11, 0),
    };

    /**
     * Runs the test.
     */
    public static void main(String[] args) {
        var generator = new LightTowerGenerator();
        generator.generate(6);

        var nodes = generator.getNodes();
        for (var i = 0; i < nodes.size(); i++) {
            var node = nodes.get(i);
            var expected = POSITIONS[i];
            if (!node.equals(expected)) {
                throw new AssertionError("Node " + i + " is not at the expected position.");
            }
        }

        System.out.println("All nodes are at the expected positions.");
    }
}
