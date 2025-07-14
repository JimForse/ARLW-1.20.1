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
    private String playerName = "";
    private CombatState combatState = CombatState.NONE;
    private final Inventory inventory = new Inventory();
    private final List<Character> combatCharacters = new ArrayList<>();
    private String modelPath;
    private String animationPath;
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
        this.playerName = playerName.toLowerCase();
        this.pathType = null;
        this.resources = new HashMap<>();
        this.hasPassiveGeneration = new HashMap<>();
        this.pathAccess = new HashMap<>();
        this.isBuilding = false;
        this.modelPath = "minecraft:entity/player/wide";
        this.animationPath = "animations/player_animation.json";
    }

// ----------------------------------------== Getters ==----------------------------------------------------------------

    public Character getActiveCharacter() {
        return combatCharacters.isEmpty() ? null : combatCharacters.get(0);
    }
    public String getModelPath() {
        return modelPath;
    }
    public String getAnimationPath() {
        return animationPath;
    }
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

    public void setModel(String modelPath, String animationPath, ServerPlayerEntity player) {
        this.modelPath = modelPath;
        this.animationPath = animationPath;
        this.modelInitialized = true;
        Axorunelostworlds.LOGGER.info("PlayerData: Установка модели: {}, анимации: {} для игрока: {}", modelPath, animationPath, player.getGameProfile().getName());
        syncModel(player);
    }
    public void setModel(String modelPath, ServerPlayerEntity player) {
        String animationPath = "models/" + player.getGameProfile().getName().toLowerCase() + "/animation.json";
        setModel(modelPath, animationPath, player);
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
        combatCharacters.clear();
        combatCharacters.add(character);
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
        if (!data.modelInitialized) {
            String playerName = player.getGameProfile().getName().toLowerCase();
            // Устанавливаем модель только если персонаж существует, иначе стандартная
            Character character = CharacterInitializer.getCharacter(playerName);
            data.setModel(
                    character != null ? character.getModelPath() : "minecraft:entity/player/wide",
                    character != null ? character.getAnimationPath() : "animations/player_animation.json",
                    player
            );
            data.modelInitialized = true;
            Axorunelostworlds.LOGGER.info("PlayerData: Инициализация модели для {}: {}", playerName, data.modelPath);
        }
        return data;
    }

    public void applyToPlayer(ServerPlayerEntity player) {
        if (combatState != CombatState.NONE && !combatCharacters.isEmpty()) {
            combatCharacters.get(0).applyToPlayer(player);
            setModel(combatCharacters.get(0).getModelPath(), combatCharacters.get(0).getAnimationPath(), player);
        } else {
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
    }

    private void sendPlayerJoinPacket(ServerPlayerEntity player) {
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

    private void syncModel(ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(modelPath);
        buf.writeString(animationPath);
        buf.writeString(playerName);
        for (ServerPlayerEntity otherPlayer : player.getServer().getPlayerManager().getPlayerList()) {
            otherPlayer.networkHandler.sendPacket(new CustomPayloadS2CPacket(
                    new Identifier(Axorunelostworlds.MOD_ID, "sync_model"), buf));
        }
    }
    public void syncDashCoolDown(ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(dashCoolDown);
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_dash_cool_down"),
                buf));
    }
    private void syncCombatMode(ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(combatState.ordinal());
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_combat_mode"),
                buf));
    }

// ------------------------------------------== NBT ==------------------------------------------------------------------

    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("CombatState", combatState.ordinal());
        nbt.putString("ModelPath", modelPath);
        nbt.putString("AnimationPath", animationPath);
        nbt.putBoolean("ModelInitialized", modelInitialized);
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
        data.modelPath = nbt.getString("ModelPath");
        data.animationPath = nbt.getString("AnimationPath");
        data.modelInitialized = nbt.getBoolean("ModelInitialized");

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