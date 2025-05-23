package rw.modden.combat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

public class StuntMechanicClient implements ClientModInitializer{
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(StunMechanic.STUN_UPDATE_PACKET, (client, handler, buf, responseSender) -> {
            int entityId = buf.readInt();
            NbtCompound stunNbt = buf.readNbt();
            client.execute(() -> {
                if (client.world != null) {
                    var entity = client.world.getEntityById(entityId);
                    if (entity instanceof LivingEntity livingEntity && stunNbt != null) {
                        NbtCompound nbt = new NbtCompound();
                        livingEntity.writeNbt(nbt);
                        nbt.put(StunMechanic.STUN_DATA_KEY, stunNbt);
                        livingEntity.readNbt(nbt);
                    }
                }
            });
        });
    }
}
