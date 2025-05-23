package rw.modden.character;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import rw.modden.Axorunelostworlds;

import java.util.UUID;

public class PlayerData extends PersistentState {
    private static final String NBT_KEY = Axorunelostworlds.MOD_ID + ":player_data";
    private Character character;
    private int starLevel;
    private ModInventory inventory;
    private final UUID playerUuid;

    public PlayerData(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.character = null;
        this.starLevel = 0;
        this.inventory = new ModInventory();
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
        return data;
    }

    public void setCharacter(Character character) {
        this.character = character;
        if (character != null) {
            this.starLevel = character.getStarLevel();
            this.inventory.addWeapon(character.getStartingWeapon());
        }
        markDirty();
    }

    public void applyToPlayer(ServerPlayerEntity player) {
        if (character != null) {
            character.applyToPlayer(player);
            System.out.println("PlayerData: Applied character to player: " + player.getGameProfile().getName());
        }
    }

    public ModInventory getInventory() {
        return inventory;
    }

    public int getStarLevel() {
        return starLevel;
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
        if (character != null) {
            dataNbt.putString("CharacterType", character.getType().name());
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
            if (dataNbt.contains("CharacterType")) {
                String type = dataNbt.getString("CharacterType");
                Character character = CharacterInitializer.getCharacter(
                        type.equals("ASSASSIN") ? "Kllima777" : "unknown"
                );
                if (character != null) {
                    data.character = character;
                }
            }
            data.inventory = ModInventory.fromNbt(dataNbt.getCompound("Inventory"));
        }
        return data;
    }
}