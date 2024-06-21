package moe.seikimo.mwhrd.beacon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.Formatting;

@Getter
@RequiredArgsConstructor
public enum BeaconFuel {
    UNSTABLE(0, 159, "Unstable", Formatting.RED),
    LOW(160, 319, "Low", Formatting.GOLD),
    MEDIUM(320, 479, "Medium", Formatting.YELLOW),
    HIGH(480, 640, "High", Formatting.GREEN),
    ;

    final int lowBound, highBound;
    final String name;
    final Formatting color;

    /**
     * Get the fuel level of the beacon.
     *
     * @param fuel the fuel level
     * @return the fuel level of the beacon
     */
    public static BeaconFuel getFuel(int fuel) {
        for (var value : values()) {
            if (fuel >= value.lowBound && fuel <= value.highBound) {
                return value;
            }
        }
        return UNSTABLE;
    }
}
