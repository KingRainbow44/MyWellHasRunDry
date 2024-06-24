package moe.seikimo.mwhrd.beacon.powers;

import lombok.Getter;
import lombok.Setter;
import moe.seikimo.mwhrd.beacon.BeaconFuel;
import moe.seikimo.mwhrd.beacon.BeaconPower;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

@Getter @Setter
public final class EffectsPower extends BeaconPower {
    private static final List<RegistryEntry<StatusEffect>> DEFAULT_EFFECTS = List.of(
        StatusEffects.HASTE, StatusEffects.SPEED, StatusEffects.JUMP_BOOST,
        StatusEffects.STRENGTH, StatusEffects.RESISTANCE, StatusEffects.REGENERATION,
        StatusEffects.LUCK, StatusEffects.NIGHT_VISION, StatusEffects.ABSORPTION
    );

    private final Map<RegistryEntry<StatusEffect>, Integer> effects = new HashMap<>();
    private final List<RegistryEntry<StatusEffect>> known = new ArrayList<>(DEFAULT_EFFECTS);

    public EffectsPower(BlockPos blockPos) {
        super(blockPos);
    }

    @Override
    public BeaconFuel minimumFuel() {
        return BeaconFuel.LOW;
    }

    @Override
    public void read(World world, NbtCompound tag) {
        if (tag.contains("effects")) {
            this.effects.clear();
            var effects = tag.getList("effects", NbtElement.COMPOUND_TYPE);
            for (var element : effects) {
                var effect = (NbtCompound) element;
                var id = Identifier.tryParse(effect.getString("id"));
                var potency = effect.getInt("potency");

                var entry = Registries.STATUS_EFFECT.getEntry(id);
                entry.ifPresent(e -> this.effects.put(e, potency));
            }
        }

        if (tag.contains("known_effects")) {
            this.known.clear();
            for (var id : tag.getList("known_effects", NbtElement.STRING_TYPE)) {
                var effect = Registries.STATUS_EFFECT.getEntry(Identifier.tryParse(id.asString()));
                effect.ifPresent(this.known::add);
            }
        }
    }

    @Override
    public void write(World world, NbtCompound tag) {
        var effects = new NbtList();
        for (var entry : this.effects.entrySet()) {
            var effect = new NbtCompound();
            effect.putString("id", entry.getKey().getIdAsString());
            effect.putInt("potency", entry.getValue());

            effects.add(effect);
        }
        tag.put("effects", effects);

        var list = new NbtList();
        this.known.forEach(e -> e.getKey().ifPresent(k -> list.add(
            NbtString.of(k.getValue().toString())
        )));
        tag.put("known_effects", list);
    }

    @Override
    public void apply(World world, int level, PlayerEntity player) {
        if (level <= 0 || !this.minimumFuel().compare(
            this.handle.mwhrd$fuel())) return;

        var duration = switch (level) {
            case 1 -> 10;
            case 2 -> 16;
            case 3 -> 21;
            case 4 -> 33;
            default -> level * 8;
        };

        for (var effect : this.getEffects()) {
            player.addStatusEffect(new StatusEffectInstance(
                effect, 20 * duration,
                this.getPotency(effect) - 1
            ));
        }
    }

    /**
     * A beacon has 3 maximum potency levels.
     * This can be spread across 3 different effects, or 1 effect with 3 potency levels.
     *
     * @return True if all potency levels are used.
     */
    public boolean used() {
        var levelsUsed = 0;
        for (var effect : this.effects.values()) {
            levelsUsed += effect;
        }

        return levelsUsed >= 3;
    }

    /**
     * Adds an effect to the beacon.
     *
     * @param effect The effect to add.
     */
    public void addEffect(RegistryEntry<StatusEffect> effect) {
        if (this.used()) {
            throw new IllegalStateException("Remove an effect/decrease one's potency to add more!");
        }

        if (this.effects.containsKey(effect)) {
            this.effects.put(effect, Math.min(3, this.effects.get(effect) + 1));
        } else {
            this.effects.put(effect, 1);
        }
    }

    /**
     * Decreases the potency of an effect.
     *
     * @param effect The effect to decrease the potency for.
     */
    public void decreasePotency(RegistryEntry<StatusEffect> effect) {
        if (!this.effects.containsKey(effect)) return;

        var potency = this.effects.get(effect);
        if (potency <= 1) {
            this.effects.remove(effect);
            return;
        }

        this.effects.put(effect, potency - 1);
    }

    /**
     * Calculates the potency of an effect.
     * The more times the effect appears as a primary, secondary, or tertiary effect, the higher the potency.
     *
     * @param effect The effect to calculate the potency for.
     * @return The potency of the effect.
     */
    public int getPotency(RegistryEntry<StatusEffect> effect) {
        return this.effects.getOrDefault(effect, 0);
    }

    /**
     * @return A list of applied effects.
     */
    public List<RegistryEntry<StatusEffect>> getEffects() {
        return new ArrayList<>(this.effects.keySet());
    }
}
