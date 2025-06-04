package rw.modden.character;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerState extends PersistentState {
    private static final String KEY = "axorunelostworlds_state";
    private final Map<UUID, PlayerData> playerData = new HashMap<>();

    public static ServerState get(MinecraftServer server) {
        // TODO: сделать для 13 кастомных миров
        return server.getWorld(World.OVERWORLD).getServer().getWorld(World.OVERWORLD)
                .getPersistentStateManager().getOrCreate(ServerState::fromNbt, ServerState::new, KEY);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerData.get(uuid);
    }

    public void setPlayerData(UUID uuid, PlayerData data) {
        playerData.put(uuid, data);
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();
        for (Map.Entry<UUID, PlayerData> entry : playerData.entrySet()) {
            NbtCompound playerNbt = new NbtCompound();
            entry.getValue().writeNbt(playerNbt);
            playersNbt.put(entry.getKey().toString(), playerNbt);
        }
        nbt.put("Players", playersNbt);
        return nbt;
    }

    public static ServerState fromNbt(NbtCompound nbt) {
        ServerState state = new ServerState();
        NbtCompound playersNbt = nbt.getCompound("Players");
        for (String key : playersNbt.getKeys()) {
            UUID uuid = UUID.fromString(key);
            state.playerData.put(uuid, PlayerData.fromNbt(uuid, playersNbt.getCompound(key)));
        }
        return state;
    }
}