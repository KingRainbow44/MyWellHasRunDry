package moe.seikimo.mwhrd.beacon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.Formatting;

@Getter
@RequiredArgsConstructor
public enum BeaconFuel {
    UNSTABLE(0, 159, "Unstable", Formatting.RED, null),
    LOW(160, 319, "Low", Formatting.GOLD, BeaconFuel.UNSTABLE),
    MEDIUM(320, 479, "Medium", Formatting.YELLOW, BeaconFuel.LOW),
    HIGH(480, 640, "High", Formatting.GREEN, BeaconFuel.MEDIUM),
    ;

    final int lowBound, highBound;
    final String name;
    final Formatting color;
    final BeaconFuel prev;

    /**
     * Helper method which compares two beacon fuels.
     * If fuel >= this, return true.
     *
     * @param fuel the fuel to compare
     * @return true if fuel >= this
     */
    public boolean compare(BeaconFuel fuel) {
        return fuel.ordinal() >= this.ordinal();
    }

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
