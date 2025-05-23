package rw.modden.character;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerState extends PersistentState {
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public static ServerState get(MinecraftServer server) {
        // TODO: Для 13 кастомных миров заменить getOverworld на основной мир
        return server.getOverworld()
                .getPersistentStateManager()
                .getOrCreate(ServerState::fromNbt, ServerState::new, "axorunelostworlds_server_state");
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public void setPlayerData(UUID uuid, PlayerData data) {
        playerDataMap.put(uuid, data);
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            nbt = entry.getValue().writeNbt(nbt);
        }
        return nbt;
    }

    public static ServerState fromNbt(NbtCompound nbt) {
        ServerState state = new ServerState();
        for (String key : nbt.getKeys()) {
            if (key.startsWith("axorunelostworlds:player_data_")) {
                UUID uuid = UUID.fromString(key.substring("axorunelostworlds:player_data_".length()));
                state.playerDataMap.put(uuid, PlayerData.fromNbt(uuid, nbt));
            }
        }
        return state;
    }
}