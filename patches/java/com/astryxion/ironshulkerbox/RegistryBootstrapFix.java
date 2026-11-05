package com.astryxion.ironshulkerbox;

import net.fabricmc.api.ModInitializer;

/**
 * Server-side bootstrapping fix for Iron Shulker Boxes 26.3-1.0.0.
 *
 * The mod registers its six MenuTypes inside the static initialiser of
 * com.astryxion.ironshulkerbox.common.registraton.IronShulkerBoxesMenuTypes.
 * That class is referenced only by the (client-only) screen registration and by
 * IronShulkerBoxMenu's factory methods. On a dedicated server nothing touches it
 * during mod loading, so the class is first initialised when a player right-clicks
 * a shulker box -- long after BuiltInRegistries has been frozen -- and registration
 * fails with "Registry is already frozen".
 *
 * In single player the client entrypoint initialises the class early, before
 * registries are frozen, which is why the bug only shows up on servers.
 *
 * This entrypoint forces that class to initialise while mod loading is still in
 * progress.
 */
public final class RegistryBootstrapFix implements ModInitializer {
    private static final String[] EARLY_CLASSES = {
            "com.astryxion.ironshulkerbox.common.registraton.IronShulkerBoxesMenuTypes"
    };

    @Override
    public void onInitialize() {
        ClassLoader loader = RegistryBootstrapFix.class.getClassLoader();
        for (String name : EARLY_CLASSES) {
            try {
                Class.forName(name, true, loader);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Iron Shulker Boxes: missing class " + name, e);
            }
        }
    }
}
