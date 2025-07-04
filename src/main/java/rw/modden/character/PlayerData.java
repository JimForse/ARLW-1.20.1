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
import rw.modden.combat.path.PathType;
import rw.modden.server.ServerState;

import java.util.*;

public class PlayerData {

/*
     В этом классе хранятся данные игрока
     Постарайся ничего не сломать...
*/

// ----------------------------------------== Private ==----------------------------------------------------------------
    private final UUID playerUuid;
    private CombatState combatState = CombatState.NONE;
    private final Inventory inventory = new Inventory();
    private final List<Character> combatCharacters = new ArrayList<>();
    private String modelPath;
    private boolean dashStatus;
    private boolean modelChanged = false;
    private boolean modelInitialized = false;
    private int dashCoolDown = 60;
// -----------------------------------------== Public ==----------------------------------------------------------------
    public PathType pathType;
    public Map<PathType, Float> resources;
    public Map<PathType, Long> pathAccess;
    public Map<PathType, Boolean> hasPassiveGeneration;
    public boolean immunity;
    public boolean isBuilding;
// ---------------------------------------------------------------------------------------------------------------------

    public PlayerData(UUID playerUuid) {
        this.playerUuid = playerUuid;
        pathType = null;
        resources = new HashMap<>();
        hasPassiveGeneration = new HashMap<>();
        pathAccess = new HashMap<>();
        isBuilding = false;
    }

// ----------------------------------------== Getters ==----------------------------------------------------------------

    public Character getActiveCharacter() {
        return combatCharacters.isEmpty() ? null : combatCharacters.get(0);
    }
    public String getModelPath() { return modelPath; }
    public CombatState getCombatState() { return combatState; }
    public Inventory getInventory() {
        return inventory;
    }
    public List<Character> getCombatCharacters() {
        return combatCharacters;
    }
    public String getCharacterName(ServerPlayerEntity player) {
        Character activeCharacter = getActiveCharacter();
        return activeCharacter != null ? activeCharacter.getCharacterName() : "None";
    }
    public boolean isCombatMode() {
        return combatState != CombatState.NONE;
    }
    public int getDashCoolDown() {
        return dashCoolDown;
    }
    public boolean isEventCombatMode() {
        return combatState == CombatState.EVENT;
    }
    public boolean hasCharacter(String playerName) {
        for (Character character: inventory.characters) {
            if (character.getClass().getSimpleName().toLowerCase().contains(playerName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

// ----------------------------------------== Setters ==----------------------------------------------------------------

    public void setModel(String modelPath, ServerPlayerEntity player) {
        if (!Objects.equals(this.modelPath, modelPath)) {
            this.modelPath = modelPath;
            modelChanged = true;
            System.out.println("setModel called from: " + Thread.currentThread().getStackTrace()[2]);
            System.out.println("PlayerData: Elementary modelPath \"" + this.modelPath + "\" changes to: \"" + modelPath + "\"");
            System.out.println("PlayerData: modelPath set to: " + modelPath);
            System.out.println("PlayerData: Setting model: " + modelPath + " for player: " + player.getGameProfile().getName());
        }
        if (modelChanged) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeString(modelPath != null ? modelPath : "");
            player.networkHandler.sendPacket(new CustomPayloadS2CPacket(
                    new Identifier(Axorunelostworlds.MOD_ID, "sync_model"),
                    buf));
            System.out.println("PlayerData: Sent sync_model packet for model: " + modelPath);
            modelChanged = false;
        }
    }
    public void setCombatMode(CombatState state, ServerPlayerEntity player) {
        this.combatState = state;
        syncCombatMode(player);
        applyToPlayer(player);
    }
    public void setDashCoolDown(int i) {
        dashCoolDown = i;
    }
    public void setActiveCharacter(Character character, ServerPlayerEntity player) {
        combatCharacters.clear(); // Очищаем список, чтобы новый персонаж стал активным
        combatCharacters.add(character);
        System.out.println("PlayerData: Set active character for player: " + player.getGameProfile().getName());
        applyToPlayer(player);
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
    public void setDashStatus(boolean b) {
        dashStatus = b;
    }

// ----------------------------------------------------------------------------------------------------------------------

    public static PlayerData getOrCreate(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return new PlayerData(player.getUuid());

        ServerState state = ServerState.get(server);
        PlayerData data = state.getPlayerData(player.getUuid());
        if (data == null) {
            data = new PlayerData(player.getUuid());
            state.setPlayerData(player.getUuid(), data);
        }
        if (data.combatState == CombatState.NONE && !data.modelInitialized) {
            data.resetPlayerAttributes(player);
            String skinName = player.getGameProfile().getName().toLowerCase();
            data.scheduleSkinUpdate(player, "axorunelostworlds:models/" + skinName);
            System.out.println("PlayerData: Initialized model for " + player.getName().getString() + ": axorunelostworlds:models/" + skinName);
            data.sendPlayerJoinPacket(player);
            data.modelInitialized = true;
        }
        return data;
    }

    public void applyToPlayer(ServerPlayerEntity player) {
        if (combatState != CombatState.NONE && !combatCharacters.isEmpty()) {
            System.out.println("PlayerData: Applying attributes for player: " + player.getGameProfile().getName() + ", combatState: " + combatState);
            combatCharacters.get(0).applyToPlayer(player);
            setModel(combatCharacters.get(0).getModelPath(), player);
        } else {
            System.out.println("PlayerData: Skipped applying attributes for player: " + player.getGameProfile().getName() + ", combatState: " + combatState);
            resetPlayerAttributes(player);
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

    private void scheduleSkinUpdate(ServerPlayerEntity player, String modelPath) {
        player.getServer().execute(() -> {
            if (player.isDisconnected()) {
                System.out.println("PlayerData: Player " + player.getName().getString() + " disconnected, skipping skin update");
                return;
            }
            // Задержка для уверенности, что игрок полностью инициализирован
            player.getServer().execute(() -> setModel(modelPath, player));
        });
    }

    private void sendPlayerJoinPacket(ServerPlayerEntity player) {
        System.out.println("PlayerData: Sent player_join packet for player: " + player.getName().getString());
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(
                new Identifier(Axorunelostworlds.MOD_ID, "player_join"),
                buf));
    }

    public void markDirty() {
        // TODO: Уточнить, как помечать ServerState как dirty
    }

    public static class Inventory {
        private final List<Character> characters = new ArrayList<>();
        public void addCharacter(Character character) {
            characters.add(character);
        }
    }

// ------------------------------------------== Synchronize ==----------------------------------------------------------

    public void syncDashCoolDown(ServerPlayerEntity player) {
        System.out.println("PlayerData: Sent sync_dash_cool_down packet for state: " + dashCoolDown);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(dashCoolDown);
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_dash_cool_down"),
                buf));
    }
    private void syncCombatMode(ServerPlayerEntity player) {
        System.out.println("PlayerData: Sent sync_combat_mode packet for state: " + combatState);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(combatState.ordinal());
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_combat_mode"),
                buf));
    }

// ------------------------------------------== NBT ==------------------------------------------------------------------

    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("CombatState", combatState.ordinal());
        if (modelPath != null)
            nbt.putString("ModelPath", modelPath);
        NbtCompound inventoryNbt = new NbtCompound();
        nbt.put("Inventory", inventoryNbt);
        NbtCompound combatCharsNbt = new NbtCompound();
        nbt.put("CombatCharacters", combatCharsNbt);
        if (pathType != null)
            nbt.putString("PathType", pathType.name());

        NbtCompound resourcesNbt = new NbtCompound();
        for (Map.Entry<PathType, Float> entry : resources.entrySet())
            resourcesNbt.putFloat(entry.getKey().name(), entry.getValue());
        nbt.put("Resources", resourcesNbt);

        NbtCompound passiveNbt = new NbtCompound();
        for(Map.Entry<PathType, Boolean> entry: hasPassiveGeneration.entrySet())
            passiveNbt.putBoolean(entry.getKey().name(), entry.getValue());
        nbt.put("HasPassiveGeneration", passiveNbt);

        NbtCompound accessNbt = new NbtCompound();
        for (Map.Entry<PathType, Long> entry: pathAccess.entrySet())
            accessNbt.putLong(entry.getKey().name(), entry.getValue());
        nbt.put("PathAccess", accessNbt);
        nbt.putBoolean("IsBuilding", isBuilding);
    }
    public static PlayerData fromNbt(UUID uuid, NbtCompound nbt) {
        PlayerData data = new PlayerData(uuid);
        data.combatState = CombatState.values()[nbt.getInt("CombatState")];
        if (nbt.contains("ModelPath"))
            data.modelPath = nbt.getString("ModelPath");

        NbtCompound resourcesNbt = nbt.getCompound("Resources");
        for (String key: resourcesNbt.getKeys())
            data.resources.put(PathType.valueOf(key), resourcesNbt.getFloat(key));

        NbtCompound hasPassiveGenerationNbt = nbt.getCompound("HasPassiveGeneration");
        for (String key: hasPassiveGenerationNbt.getKeys())
            data.hasPassiveGeneration.put(PathType.valueOf(key), hasPassiveGenerationNbt.getBoolean(key));

        NbtCompound pathAccessNbt = nbt.getCompound("PathAccess");
        for (String key: pathAccessNbt.getKeys())
            data.pathAccess.put(PathType.valueOf(key), pathAccessNbt.getLong(key));

        data.isBuilding = nbt.getBoolean("IsBuilding");

        if (nbt.contains("PathType"))
            data.pathType = PathType.valueOf(nbt.getString("PathType"));

        if (nbt.contains("ModelPath"))
            data.modelPath = nbt.getString("ModelPath");

        return data;
    }
}