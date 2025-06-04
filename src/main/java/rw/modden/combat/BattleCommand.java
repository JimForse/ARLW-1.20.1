package rw.modden.combat;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BattleCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("battle")
                    .requires(source -> source.hasPermissionLevel(2)) // Только для операторов
                    .then(CommandManager.literal("start")
                            .executes(context -> {
                                ServerPlayerEntity player = context.getSource().getPlayer();
                                CombatMechanics.setBattleStatus(true, player);
                                context.getSource().sendFeedback(() -> Text.literal("Battle mode enabled"), true);
                                return 1;
                            }))
                    .then(CommandManager.literal("stop")
                            .executes(context -> {
                                ServerPlayerEntity player = context.getSource().getPlayer();
                                CombatMechanics.setBattleStatus(false, player);
                                context.getSource().sendFeedback(() -> Text.literal("Battle mode disabled"), true);
                                return 1;
                            })));
        });
    }
}