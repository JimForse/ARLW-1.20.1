package rw.modden.world.dimension;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import rw.modden.Axorunelostworlds;

public class ModDimensions {
    public static String modId = Axorunelostworlds.MOD_ID;
    public static final DefaultParticleType PRIMORDIAL_MATTER = FabricParticleTypes.simple();

    public static final RegistryKey<World> CREATE_WORLD = RegistryKey.of(RegistryKeys.WORLD, new Identifier(modId, "create"));
    public static final RegistryKey<DimensionType> CREATE_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, new Identifier(modId, "create_type"));

    public static void register() {
        Axorunelostworlds.LOGGER.info("ModDimensions.register started!");

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Axorunelostworlds.LOGGER.info("Registering dimension: axorunelostworlds:create");
            Axorunelostworlds.LOGGER.info("Dimension key: " + CREATE_WORLD.getValue());
            Axorunelostworlds.LOGGER.info("Dimension type key: " + CREATE_TYPE.getValue());
        });

        // Регистрация частицы
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(Axorunelostworlds.MOD_ID, "primordialmatter"), PRIMORDIAL_MATTER);
        Axorunelostworlds.LOGGER.info("Registered particle: " + Axorunelostworlds.MOD_ID + ":primordialmatter");
    }
}