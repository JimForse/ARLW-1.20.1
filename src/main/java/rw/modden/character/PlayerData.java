package rw.modden.character;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import rw.modden.Axorunelostworlds;
import rw.modden.combat.CombatState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData extends PersistentState {
    private static final String NBT_KEY = Axorunelostworlds.MOD_ID + ":player_data";
    private Character activeCharacter;
    private List<Character> combatCharacters = new ArrayList<>(3);
    private int starLevel;
    private ModInventory inventory;
    private final UUID playerUuid;
    private Identifier skinId;
    private CombatState combatState = CombatState.NONE;

    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f2345678901a");

    public PlayerData(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.activeCharacter = null;
        this.starLevel = 0;
        this.inventory = new ModInventory();
        this.combatState = CombatState.NONE;
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
        if (combatState != CombatState.NONE && activeCharacter != null) {
            System.out.println("PlayerData: Applying attributes for player: " + player.getGameProfile().getName() + ", combatState: " + combatState);
            activeCharacter.applyToPlayer(player);
            setSkinId(new Identifier(Axorunelostworlds.MOD_ID, "textures/skins/" + player.getGameProfile().getName().toLowerCase()), player);
        } else {
            System.out.println("PlayerData: Skipped applying attributes for player: " + player.getGameProfile().getName() + ", combatState: " + combatState);
            resetPlayerAttributes(player);
            setSkinId(null, player);
        }
    }

    public boolean hasCharacter(String playerName) {
        for (Character character : inventory.getCharacters()) {
            if (CharacterInitializer.getCharacter(playerName) == character) {
                return true;
            }
        }
        return false;
    }

    public void setCharacter(Character character, ServerPlayerEntity player) {
        inventory.addCharacter(character);
        if (!combatCharacters.contains(character) && combatCharacters.size() < 3) {
            combatCharacters.add(character);
        }
        if (activeCharacter == null) {
            activeCharacter = character;
        }
        this.starLevel = character.getStarLevel();
        if (combatState != CombatState.NONE && activeCharacter != null) {
            applyToPlayer(player);
        }
        markDirty();
    }

    public void setActiveCharacter(Character character) {
        if (this.activeCharacter != character) {
            this.activeCharacter = character;
            markDirty();
            System.out.println("PlayerData: Set active character to: " + (character != null ? character.getType().name() : "null"));
        }
    }

    public void switchCharacter(int index, ServerPlayerEntity player) {
        if (combatState == CombatState.NORMAL && index >= 0 && index < combatCharacters.size()) {
            activeCharacter = combatCharacters.get(index);
            applyToPlayer(player);
            syncSkin(player);
        }
    }

    public void setCombatMode(CombatState state, ServerPlayerEntity player) {
        this.combatState = state;
        if (state != CombatState.NONE && activeCharacter != null) {
            System.out.println("PlayerData: Applying attributes for player: " + player.getGameProfile().getName() + ", combatState: " + state);
            applyToPlayer(player);
        } else {
            System.out.println("PlayerData: Resetting attributes for player: " + player.getGameProfile().getName() + ", combatState: " + state);
            resetPlayerAttributes(player);
            setSkinId(null, player);
        }
        syncCombatMode(player);
        markDirty();
    }

    public boolean isCombatMode() {
        return combatState != CombatState.NONE;
    }

    public boolean isEventCombatMode() {
        return combatState == CombatState.EVENT;
    }

    public List<Character> getCombatCharacters() {
        return combatCharacters;
    }

    public Character getActiveCharacter() {
        return activeCharacter;
    }

    private void resetPlayerAttributes(ServerPlayerEntity player) {
        EntityAttributeInstance healthAttr = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_UUID);
            player.setHealth(20.0f);
        }
        EntityAttributeInstance damageAttr = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.removeModifier(DAMAGE_MODIFIER_UUID);
        }
    }

    private void syncSkin(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(skinId != null ? skinId : new Identifier("minecraft", "textures/entity/player/steve.png"));
        ServerPlayNetworking.send(player, new Identifier(Axorunelostworlds.MOD_ID, "sync_skin"), buf);
        System.out.println("PlayerData: Sent sync_skin packet for skin: " + (skinId != null ? skinId : "default"));
    }

    private void syncCombatMode(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(combatState.ordinal());
        ServerPlayNetworking.send(player, new Identifier(Axorunelostworlds.MOD_ID, "sync_combat_mode"), buf);
        System.out.println("PlayerData: Sent sync_combat_mode packet for state: " + combatState);
    }

    public ModInventory getInventory() {
        return inventory;
    }

    public int getStarLevel() {
        return starLevel;
    }

    public void setSkinId(Identifier skinId, ServerPlayerEntity player) {
        this.skinId = skinId;
        syncSkin(player);
        markDirty();
    }

    public void setStarLevel(int starLevel) {
        this.starLevel = clamp(starLevel, 0, 5);
        markDirty();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound dataNbt = new NbtCompound();
        dataNbt.putInt("StarLevel", starLevel);
        dataNbt.putInt("CombatState", combatState.ordinal());
        if (activeCharacter != null) {
            dataNbt.putString("CharacterType", activeCharacter.getType().name());
        }
        dataNbt.put("Inventory", inventory.writeNbt(new NbtCompound()));
        nbt.put(NBT_KEY + "_" + playerUuid.toString(), dataNbt);
        return nbt;
    }

    public static PlayerData fromNbt(UUID playerUuid, NbtCompound nbt) {
        PlayerData data = new PlayerData(playerUuid);
        String key = NBT_KEY + "_" + playerUuid.toString();
        if (nbt.contains(key)) {
            NbtCompound dataNbt = nbt.getCompound(key);
            data.starLevel = dataNbt.getInt("StarLevel");
            data.combatState = CombatState.values()[dataNbt.getInt("CombatState")];
            if (dataNbt.contains("CharacterType")) {
                String type = dataNbt.getString("CharacterType");
                Character character = CharacterInitializer.getCharacter(
                        type.equals(Character.CharacterType.SUPPORT.name()) ? "kllima777" : "unknown"
                );
                if (character != null) {
                    data.inventory.addCharacter(character);
                    if (!data.combatCharacters.contains(character) && data.combatCharacters.size() < 3) {
                        data.combatCharacters.add(character);
                    }
                    if (data.activeCharacter == null) {
                        data.activeCharacter = character;
                    }
                    data.starLevel = character.getStarLevel();
                    data.markDirty();
                }
            }
            data.inventory = ModInventory.fromNbt(dataNbt.getCompound("Inventory"));
        }
        return data;
    }
}