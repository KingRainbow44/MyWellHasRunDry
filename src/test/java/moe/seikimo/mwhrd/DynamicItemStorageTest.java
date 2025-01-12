package moe.seikimo.mwhrd;

import moe.seikimo.mwhrd.utils.items.DynamicItemStorage;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class DynamicItemStorageTest {
    private static DynamicItemStorage storage;

    @BeforeAll
    public static void setup() {
        // Prepare Minecraft environment.
        SharedConstants.createGameVersion();
        Bootstrap.initialize();

        // Configure the storage.
        storage = new DynamicItemStorage(1, 2);
        storage.insert(new ItemStack(Items.EMERALD, 63));
        storage.insert(new ItemStack(Items.IRON_INGOT, 16));
        storage.insert(new ItemStack(Items.GOLD_INGOT, 6));
        storage.insert(new ItemStack(Items.DIAMOND, 3));

        System.out.println("Initial storage:");
        System.out.println(storage.toString());
        System.out.println();
    }

    @Test
    @Order(1)
    public void testInitialInsert() {
        Assertions.assertEquals(2, storage.size(), "The storage should have 2 pages.");
        Assertions.assertEquals(4, storage.count(), "The storage should have 4 stacks.");
        Assertions.assertEquals(63, storage.count(Items.EMERALD), "The storage should have 63 emeralds.");
    }

    @Test
    @Order(2)
    public void testSimpleInsertions() {
        storage.insert(new ItemStack(Items.GOLD_INGOT, 6));
        storage.insert(new ItemStack(Items.NETHERITE_INGOT, 1));

        Assertions.assertEquals(3, storage.size(), "The storage should have 3 pages.");
        Assertions.assertEquals(5, storage.count(), "The storage should have 5 stacks.");
        Assertions.assertEquals(12, storage.count(Items.GOLD_INGOT), "The storage should have 12 gold ingots.");

        System.out.println("Simple insertions:");
        System.out.println(storage.toString());
        System.out.println();
    }

    @Test
    @Order(3)
    public void testComplexInsertions() {
        storage.insert(new ItemStack(Items.GOLD_INGOT, 53));
        storage.insert(new ItemStack(Items.EMERALD, 63));

        Assertions.assertEquals(4, storage.size(), "The storage should have 4 pages.");
        Assertions.assertEquals(7, storage.count(), "The storage should have 7 stacks.");
        Assertions.assertEquals(126, storage.count(Items.EMERALD), "The storage should have 126 emeralds.");
        Assertions.assertEquals(65, storage.count(Items.GOLD_INGOT), "The storage should have 65 gold ingots.");

        System.out.println("Complex insertions:");
        System.out.println(storage.toString());
        System.out.println();
    }
}
