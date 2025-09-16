package rw.modden.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class DimensionCommands {
    private static final SuggestionProvider<ServerCommandSource> DIMENSIONS_LIST = (context, builder) -> {
        context.getSource().getServer().getWorldRegistryKeys().forEach(dimension -> {
            builder.suggest(dimension.getValue().toString());
        });
        return builder.buildFuture();
    };

    private static RegistryKey<World> dimensionDetector(Identifier dimensionId) {
        return RegistryKey.of(RegistryKeys.WORLD, dimensionId);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("dimensions")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.literal("teleport")
                                        .then(CommandManager.argument("dimension", IdentifierArgumentType.identifier())
                                                .suggests(DIMENSIONS_LIST)
                                                .executes(context -> executeTeleport(context, new Vec3d(0, 64, 0))) // Без координат
                                                .then(CommandManager.argument("coordinates", Vec3ArgumentType.vec3())
                                                        .executes(context -> executeTeleportWithCoords(context))
                                                )
                                        )
                                )
                        )
        );
    }

    private static int executeTeleport(CommandContext<ServerCommandSource> context, Vec3d coords) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        Identifier dimensionId = IdentifierArgumentType.getIdentifier(context, "dimension");

        return teleportPlayer(context.getSource(), player, dimensionId, coords);
    }

    private static int executeTeleportWithCoords(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        Identifier dimensionId = IdentifierArgumentType.getIdentifier(context, "dimension");
        Vec3d coords = Vec3ArgumentType.getVec3(context, "coordinates");

        return teleportPlayer(context.getSource(), player, dimensionId, coords);
    }

    private static int teleportPlayer(ServerCommandSource source, ServerPlayerEntity player, Identifier dimensionId, Vec3d coords) {
        RegistryKey<World> dimension = dimensionDetector(dimensionId);
        ServerWorld world = source.getServer().getWorld(dimension);

        if (world != null) {
            player.teleport(world, coords.x, coords.y, coords.z, 0, 0);
            source.sendFeedback(() -> Text.literal("Игрок " + player.getName().getString() + " телепортирован в " + dimensionId), true);
            return 1;
        } else {
            source.sendError(Text.literal("Измерение не найдено!"));
            return 0;
        }
    }
}