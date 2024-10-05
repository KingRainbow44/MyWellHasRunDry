package moe.seikimo.mwhrd.custom;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface CustomTags {
    TagKey<Item> ENLIGHTENED_ARMOR = TagKey.of(RegistryKeys.ITEM, Identifier.of("mwhrd", "enlightened_armor"));
}
