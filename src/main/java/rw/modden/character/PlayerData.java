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
    private String customName;
    private CombatState combatState = CombatState.NONE;
    private final Inventory inventory = new Inventory();
    private final List<Character> combatCharacters = new ArrayList<>();
    private String modelPath;
    private String texturePath;
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

    public PlayerData(String playerName) {
        this.playerName = playerName.toLowerCase();
        this.pathType = null;
        this.resources = new HashMap<>();
        this.hasPassiveGeneration = new HashMap<>();
        this.pathAccess = new HashMap<>();
        this.isBuilding = false;
        this.modelPath = "minecraft:entity/player/wide";
        this.texturePath = "minecraft:textures/entity/player/wide.png";
        this.animationPath = "minecraft:animations/player_animation.json";
    }

// ----------------------------------------== Getters ==----------------------------------------------------------------

    public Character getActiveCharacter() {
        return combatCharacters.isEmpty() ? null : combatCharacters.get(0);
    }
    public String getModelPath() {
        return modelPath;
    }
    public String getTexturePath() {
        return texturePath;
    }
    public String getAnimationPath() {
        return animationPath;
    }
    public CombatState getCombatState() {
        return combatState;
    }
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
    public boolean hasCharacter(String characterName) {
        for (Character character : inventory.characters) {
            if (character.getClass().getSimpleName().toLowerCase().contains(characterName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    public String getCustomName() {
        return customName != null ? customName : playerName;
    }

// ----------------------------------------== Setters ==----------------------------------------------------------------

    public void setCustomName(String customName, ServerPlayerEntity player) {
        if (Objects.equals(this.customName, customName)) {
            return; // Не отправляем пакет, если ник не изменился
        }
        this.customName = customName;
        Axorunelostworlds.LOGGER.info("PlayerData: Установлен кастомный ник {} для {}", customName, playerName);
        syncCustomName(player);
    }
    public void setModel(String modelPath, String texturePath, String animationPath, ServerPlayerEntity player) {
        this.modelPath = modelPath;
        this.texturePath = texturePath;
        this.animationPath = animationPath;
        this.modelInitialized = true;
        Axorunelostworlds.LOGGER.info("PlayerData: Установка модели: {}, текстуры: {}, анимации: {} для игрока: {}", modelPath, texturePath, animationPath, playerName);
        syncModel(player);
    }
    public void setModel(String customName, ServerPlayerEntity player) {
        String modelPath = customName + "/" + customName + ".geo.json";
        String texturePath = customName + "/" + customName + ".png";
        String animationPath = customName + "/anim.json";
        setModel(modelPath, texturePath, animationPath, player);
    }
    public void setCombatMode(CombatState state, ServerPlayerEntity player) {
        this.combatState = state;
        syncCombatMode(player);
        applyToPlayer(player);
    }
    public void setDashCoolDown(int i) {
        dashCoolDown = i;
        Axorunelostworlds.LOGGER.info("PlayerData: Установлен dashCoolDown={} для {}", i, playerName);
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
            Axorunelostworlds.LOGGER.info("PlayerData: Переключён персонаж на индекс {} для {}", index, playerName);
        } else {
            Axorunelostworlds.LOGGER.warn("PlayerData: Неверный индекс персонажа {} для {}", index, playerName);
        }
    }
    public void setDashStatus(boolean b) {
        dashStatus = b;
    }

// ----------------------------------------------------------------------------------------------------------------------

    public static PlayerData getOrCreate(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            Axorunelostworlds.LOGGER.warn("PlayerData: Сервер null для {}, создаём временные данные", player.getGameProfile().getName());
            return new PlayerData(player.getGameProfile().getName());
        }

        ServerState state = ServerState.get(server);
        String playerName = player.getGameProfile().getName().toLowerCase();
        PlayerData data = state.getPlayerData(playerName);
        if (data == null) {
            data = new PlayerData(playerName);
            state.setPlayerData(playerName, data);
        }
        if (!data.modelInitialized) {
            String customName = data.getCustomName();
            data.setModel(
                    customName + "/" + customName + ".geo.json",
                    customName + "/" + customName + ".png",
                    customName + "/anim.json",
                    player
            );
            data.modelInitialized = true;
            Axorunelostworlds.LOGGER.info("PlayerData: Инициализация модели для {}: {}, текстуры: {}, анимации: {}", playerName, data.modelPath, data.texturePath, data.animationPath);
        }
        return data;
    }

    public void applyToPlayer(ServerPlayerEntity player) {
        if (combatState != CombatState.NONE && !combatCharacters.isEmpty()) {
            combatCharacters.get(0).applyToPlayer(player);
            setModel(combatCharacters.get(0).getCharacterName(), player);
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
        if (player.getServer() == null) {
            Axorunelostworlds.LOGGER.warn("PlayerData: Не удалось синхронизировать модель, сервер null для {}", playerName);
            return;
        }
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(modelPath);
        buf.writeString(texturePath);
        buf.writeString(animationPath);
        buf.writeString(playerName);
        for (ServerPlayerEntity otherPlayer : player.getServer().getPlayerManager().getPlayerList()) {
            otherPlayer.networkHandler.sendPacket(new CustomPayloadS2CPacket(
                    new Identifier(Axorunelostworlds.MOD_ID, "sync_model"), buf));
        }
    }
    public void syncDashCoolDown(ServerPlayerEntity player) {
        if (player.getServer() == null) {
            Axorunelostworlds.LOGGER.warn("PlayerData: Не удалось синхронизировать dashCoolDown, сервер null для {}", playerName);
            return;
        }
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(dashCoolDown);
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_dash_cool_down"),
                buf));
    }
    private void syncCombatMode(ServerPlayerEntity player) {
        if (player.getServer() == null) {
            Axorunelostworlds.LOGGER.warn("PlayerData: Не удалось синхронизировать combatMode, сервер null для {}", playerName);
            return;
        }
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(combatState.ordinal());
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(
                new Identifier(Axorunelostworlds.MOD_ID, "sync_combat_mode"),
                buf));
    }
    private void syncCustomName(ServerPlayerEntity player) {
        if (player.getServer() == null) {
            Axorunelostworlds.LOGGER.warn("PlayerData: Не удалось синхронизировать customName, сервер null для {}", playerName);
            return;
        }
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(customName != null ? customName : "");
        buf.writeString(playerName);
        for (ServerPlayerEntity otherPlayer : player.getServer().getPlayerManager().getPlayerList()) {
            otherPlayer.networkHandler.sendPacket(new CustomPayloadS2CPacket(
                    new Identifier(Axorunelostworlds.MOD_ID, "sync_custom_name"), buf));
        }
    }

// ------------------------------------------== NBT ==------------------------------------------------------------------

    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("CombatState", combatState.ordinal());
        nbt.putString("ModelPath", modelPath);
        nbt.putString("TexturePath", texturePath);
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
        for (Map.Entry<PathType, Boolean> entry : hasPassiveGeneration.entrySet())
            passiveNbt.putBoolean(entry.getKey().name(), entry.getValue());
        nbt.put("HasPassiveGeneration", passiveNbt);

        NbtCompound accessNbt = new NbtCompound();
        for (Map.Entry<PathType, Long> entry : pathAccess.entrySet())
            accessNbt.putLong(entry.getKey().name(), entry.getValue());
        nbt.put("PathAccess", accessNbt);
        nbt.putBoolean("IsBuilding", isBuilding);

        if (customName != null) {
            nbt.putString("CustomName", customName);
        }
    }
    public static PlayerData fromNbt(String playerName, NbtCompound nbt) {
        PlayerData data = new PlayerData(playerName);
        data.combatState = CombatState.values()[nbt.getInt("CombatState")];
        data.modelPath = nbt.getString("ModelPath");
        data.texturePath = nbt.getString("TexturePath");
        data.animationPath = nbt.getString("AnimationPath");
        data.modelInitialized = nbt.getBoolean("ModelInitialized");

        NbtCompound resourcesNbt = nbt.getCompound("Resources");
        for (String key : resourcesNbt.getKeys())
            data.resources.put(PathType.valueOf(key), resourcesNbt.getFloat(key));

        NbtCompound hasPassiveGenerationNbt = nbt.getCompound("HasPassiveGeneration");
        for (String key : hasPassiveGenerationNbt.getKeys())
            data.hasPassiveGeneration.put(PathType.valueOf(key), hasPassiveGenerationNbt.getBoolean(key));

        NbtCompound pathAccessNbt = nbt.getCompound("PathAccess");
        for (String key : pathAccessNbt.getKeys())
            data.pathAccess.put(PathType.valueOf(key), pathAccessNbt.getLong(key));

        data.isBuilding = nbt.getBoolean("IsBuilding");

        if (nbt.contains("PathType"))
            data.pathType = PathType.valueOf(nbt.getString("PathType"));

        if (nbt.contains("ModelPath"))
            data.modelPath = nbt.getString("ModelPath");

        if (nbt.contains("CustomName"))
            data.customName = nbt.getString("CustomName");
        return data;
    }
}