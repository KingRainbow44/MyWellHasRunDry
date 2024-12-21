package moe.seikimo.mwhrd.utils.items;

import net.minecraft.component.type.LoreComponent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public final class LoreBuilder {
    /**
     * The default lore format.
     * It disables italics.
     */
    public static final Style DEFAULT_FORMAT = Style.EMPTY
        .withItalic(false);

    /**
     * Creates a new lore builder instance.
     *
     * @return A lore builder with a default new-line.
     */
    public static LoreBuilder of() {
        return new LoreBuilder(true);
    }

    /**
     * Creates a new lore builder instance.
     *
     * @param newLine Whether to add a new-line to the lore.
     * @return A lore builder with the specified new-line.
     */
    public static LoreBuilder of(boolean newLine) {
        return new LoreBuilder(newLine);
    }

    private Style style = DEFAULT_FORMAT;
    private final List<Text> lore = new ArrayList<>();

    private LoreBuilder(boolean newLine) {
        if (newLine) {
            this.lore.add(Text.empty());
        }
    }

    /**
     * Sets the style of the lore.
     *
     * @param style The style to set.
     * @return The builder for chaining.
     */
    public LoreBuilder style(Style style) {
        this.style = style;
        return this;
    }

    /**
     * Adds a new line using the default formatting.
     *
     * @param text The text to add.
     * @return The builder for chaining.
     */
    public LoreBuilder add(Text text) {
        this.lore.add(text
            .copy()
            .fillStyle(this.style));
        return this;
    }

    /**
     * Adds a new line using the default formatting.
     *
     * @param text The text to add.
     * @return The builder for chaining.
     */
    public LoreBuilder raw(Text text) {
        this.lore.add(text);
        return this;
    }

    /**
     * Adds a string literal to the lore.
     *
     * @param string The string to add.
     * @param formatting The formatting to apply.
     * @return The builder for chaining.
     */
    public LoreBuilder literal(String string, Formatting... formatting) {
        return this.add(Text
            .literal(string)
            .fillStyle(this.style.withFormatting(formatting)));
    }

    /**
     * @return The finalized lore component.
     */
    public LoreComponent build() {
        return new LoreComponent(this.lore);
    }
}
