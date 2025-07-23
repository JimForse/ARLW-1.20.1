package rw.modden.server;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import rw.modden.character.PlayerData;

import java.util.HashMap;
import java.util.Map;

public class ServerState extends PersistentState {
    private static final String KEY = "axorunelostworlds_state";
    private static final Map<String, ServerState> INSTANCES = new HashMap<>();
    private final Map<String, PlayerData> playerData = new HashMap<>();

    public static ServerState get(MinecraftServer server) {
        return INSTANCES.computeIfAbsent(server.getSaveProperties().getLevelName(), k -> new ServerState());
    }

    public PlayerData getPlayerData(String playerName) {
        return playerData.get(playerName.toLowerCase());
    }

    public void setPlayerData(String playerName, PlayerData data) {
        playerData.put(playerName.toLowerCase(), data);
        markDirty();
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();
        for (Map.Entry<String, PlayerData> entry : playerData.entrySet()) {
            NbtCompound playerNbt = new NbtCompound();
            entry.getValue().writeNbt(playerNbt);
            playersNbt.put(entry.getKey(), playerNbt);
        }
        nbt.put("Players", playersNbt);
        return playersNbt;
    }

    public void readNbt(NbtCompound nbt) {
        NbtCompound playersNbt = nbt.getCompound("Players");
        for (String playerName : playersNbt.getKeys()) {
            playerData.put(playerName.toLowerCase(), PlayerData.fromNbt(playerName, playersNbt.getCompound(playerName)));
        }
    }
}