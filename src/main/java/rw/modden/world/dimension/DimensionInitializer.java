package rw.modden.world.dimension;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.registry.RegistryKeys;
import rw.modden.Axorunelostworlds;

public class DimensionInitializer {
    public static void initialize() {
        try {
            Axorunelostworlds.LOGGER.info("DimensionInitializer started!");
            ModDimensions.register();
            Axorunelostworlds.LOGGER.info("ModDimensions.register completed!");
            ServerLifecycleEvents.SERVER_STARTING.register(server -> {
                Axorunelostworlds.LOGGER.info("Server starting!");
                Axorunelostworlds.LOGGER.info("Registered dimensions: " + server.getRegistryManager().get(RegistryKeys.DIMENSION).getIds());
                Axorunelostworlds.LOGGER.info("Registered dimension types: " + server.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE).getIds());
                Axorunelostworlds.LOGGER.info("Registered biomes: " + server.getRegistryManager().get(RegistryKeys.BIOME).getIds());
            });
            ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                Axorunelostworlds.LOGGER.info("Server started!");
                Axorunelostworlds.LOGGER.info("Registered dimensions (STARTED): " + server.getRegistryManager().get(RegistryKeys.DIMENSION).getIds());
                Axorunelostworlds.LOGGER.info("Registered dimension types (STARTED): " + server.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE).getIds());
                Axorunelostworlds.LOGGER.info("Registered biomes (STARTED): " + server.getRegistryManager().get(RegistryKeys.BIOME).getIds());
            });
        } catch (Exception e) {
            Axorunelostworlds.LOGGER.info("Error in DimensionInitializer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
