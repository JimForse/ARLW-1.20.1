package rw.modden.character;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import rw.modden.combat.CombatState;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData {
    private final UUID playerUuid;
    private CombatState combatState = CombatState.NONE;
    private final Inventory inventory = new Inventory();
    private final List<Character> combatCharacters = new ArrayList<>();

    public PlayerData(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public static PlayerData getOrCreate(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return new PlayerData(player.getUuid());

        ServerState state = ServerState.get(server);
        PlayerData data = state.getPlayerData(player.getUuid());
        if (data == null) {
            data = new PlayerData(player.getUuid());
            state.setPlayerData(player.getUuid(), data);
        }
        if (data.combatState == CombatState.NONE) {
            data.resetPlayerAttributes(player);
            data.setSkinId(null, player);
        }
        return data;
    }

    public void applyToPlayer(ServerPlayerEntity player) {
        if (combatState != CombatState.NONE && !combatCharacters.isEmpty()) {
            System.out.println("PlayerData: Applying attributes for player: " + player.getGameProfile().getName() + ", combatState: " + combatState);
            combatCharacters.get(0).applyToPlayer(player);
        } else {
            System.out.println("PlayerData: Skipped applying attributes for player: " + player.getGameProfile().getName() + ", combatState: " + combatState);
            resetPlayerAttributes(player);
            setSkinId(null, player);
        }
    }

    public void resetPlayerAttributes(ServerPlayerEntity player) {
        EntityAttributeInstance healthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.clearModifiers();
            healthAttr.setBaseValue(20.0);
        }
        EntityAttributeInstance damageAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.clearModifiers();
            damageAttr.setBaseValue(1.0);
        }
        player.setHealth(player.getMaxHealth());
        System.out.println("PlayerData: Resetting attributes for player: " + player.getGameProfile().getName() + ", combatState: " + combatState);
    }

    public boolean isCombatMode() {
        return combatState != CombatState.NONE;
    }

    public boolean isEventCombatMode() {
        return combatState == CombatState.EVENT;
    }

    public void setCombatMode(CombatState state, ServerPlayerEntity player) {
        this.combatState = state;
        syncCombatMode(player);
        applyToPlayer(player);
    }

    public Character getActiveCharacter() {
        return combatCharacters.isEmpty() ? null : combatCharacters.get(0);
    }

    public void switchCharacter(int index, ServerPlayerEntity player) {
        if (index >= 0 && index < combatCharacters.size()) {
            Character selected = combatCharacters.remove(index);
            combatCharacters.add(0, selected);
            applyToPlayer(player);
            System.out.println("PlayerData: Switched to character at index " + index + " for player: " + player.getGameProfile().getName());
        } else {
            System.out.println("PlayerData: Invalid character index " + index + " for player: " + player.getGameProfile().getName());
        }
    }

    public boolean hasCharacter(String playerName) {
        for (Character character : inventory.characters) {
            Identifier skin = character.getSkin();
            if (skin != null && skin.getPath().contains(playerName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public List<Character> getCombatCharacters() {
        return combatCharacters;
    }

    public void markDirty() {
        // TODO: Уточнить, как помечать ServerState как dirty
    }

    public void setSkinId(Identifier skinId, ServerPlayerEntity player) {
        if (skinId == null) {
            skinId = new Identifier("minecraft", "textures/entity/player/steve.png");
        }
        System.out.println("PlayerData: Sent sync_skin packet for skin: " + skinId);
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_skin"),
                new PacketByteBuf(Unpooled.buffer()).writeIdentifier(skinId)));
    }

    private void syncCombatMode(ServerPlayerEntity player) {
        System.out.println("PlayerData: Sent sync_combat_mode packet for state: " + combatState);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(combatState.ordinal());
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_combat_mode"),
                buf));
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("CombatState", combatState.ordinal());
        NbtCompound inventoryNbt = new NbtCompound();
        nbt.put("Inventory", inventoryNbt);
        NbtCompound combatCharsNbt = new NbtCompound();
        nbt.put("CombatCharacters", combatCharsNbt);
    }

    public static PlayerData fromNbt(UUID uuid, NbtCompound nbt) {
        PlayerData data = new PlayerData(uuid);
        data.combatState = CombatState.values()[nbt.getInt("CombatState")];
        return data;
    }

    public static class Inventory {
        private final List<Character> characters = new ArrayList<>();

        public void addCharacter(Character character) {
            characters.add(character);
        }
    }
}