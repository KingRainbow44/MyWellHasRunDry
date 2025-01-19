package moe.seikimo.mwhrd.utils;

import net.minecraft.text.Text;

public interface Time {
    /**
     * Converts ticks to a {@link Text} object.
     *
     * @param ticks The number of ticks to convert.
     * @return The {@link Text} object representing the number of ticks.
     */
    static Text toString(long ticks) {
        var minutes = ticks / 1200;
        var seconds = (ticks % 1200) / 20;

        var text = Text.empty();

        if (minutes > 0) {
            text = text.append("%sm".formatted(minutes));
        }

        if (seconds > 0) {
            if (minutes > 0) text = text.append(" ");
            text = text.append("%ss".formatted(seconds));
        }

        return text;
    }
}
