package moe.seikimo.mwhrd.custom;

/**
 * General, static class for initializing registry changes.
 */
public interface Custom {
    /**
     * Registers all custom content.
     */
    static void register() {
        CustomStats.register();
        CustomBlocks.register();
        CustomComponents.register();
        CustomItems.register(); // Depends on components, blocks being registered.
        CustomEntities.register();
        CustomProviders.register();
    }
}
