package rw.modden.combat;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rw.modden.character.CharacterInitializer;
import rw.modden.character.PlayerData;
import rw.modden.character.Character;

public class BattleCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("battle")
                    .requires(source -> source.hasPermissionLevel(2)) // Только для операторов
                    .then(CommandManager.literal("start")
                            .executes(context -> {
                                ServerPlayerEntity player = context.getSource().getPlayer();
                                PlayerData data = PlayerData.getOrCreate(player);
                                Character character = CharacterInitializer.getCharacter(player.getGameProfile().getName().toLowerCase());
                                if (character != null && data.getActiveCharacter() == null) {
                                    data.setActiveCharacter(character);
                                    System.out.println("BattleCommand: Set active character for " + player.getGameProfile().getName() + " to " + character.getType().name());
                                }
                                data.setCombatMode(CombatState.NORMAL, player);
                                context.getSource().sendFeedback(() -> Text.literal("Battle mode enabled"), true);
                                return 1;
                            }))
                    .then(CommandManager.literal("stop")
                            .executes(context -> {
                                ServerPlayerEntity player = context.getSource().getPlayer();
                                PlayerData.getOrCreate(player).setCombatMode(CombatState.NONE, player);
                                context.getSource().sendFeedback(() -> Text.literal("Battle mode disabled"), true);
                                return 1;
                            })));
        });
    }
}