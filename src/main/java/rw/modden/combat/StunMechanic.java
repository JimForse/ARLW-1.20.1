package rw.modden.combat;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import rw.modden.Axorunelostworlds;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import java.util.function.Predicate;

public class StunMechanic {
    public static final String STUN_DATA_KEY = "axorunelostworlds:stun_data";
    private static final String STUNT_POINT_KEY = "StuntPoint";
    private static final String STUN_TIMER_KEY = "StunTimer";
    public static final int MAX_STUNT_POINT = 100;
    private static final int START_STUNT_POINT = 0;
    private static final int STUN_DURATION = 5 * 20;
    private static final int RESET_TIMEOUT = 2 * 20;
    private static boolean isActive = false;
    public static final Identifier STUN_UPDATE_PACKET = new Identifier(Axorunelostworlds.MOD_ID, "stun_update");

    public static void initialize() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world instanceof ServerWorld && entity instanceof LivingEntity livingEntity && CombatMechanics.isBattleActive()) {
                applyStunMechanics(livingEntity);
                syncStunData(livingEntity);
            }
            return ActionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                Box box = new Box(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
                Predicate<LivingEntity> predicate = entity -> getStunNbt(entity).getInt(STUN_TIMER_KEY) > 0;
                for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, box, predicate)) {
                    if (CombatMechanics.isBattleActive()) {
                        updateStunTimer(entity);
                        syncStunData(entity);
                    }
                }
            }
        });
    }

    public static void cleanup() {
        if (MinecraftClient.getInstance().getServer() != null) {
            for (ServerWorld world : MinecraftClient.getInstance().getServer().getWorlds()) {
                Box box = new Box(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
                Predicate<LivingEntity> predicate = entity -> getStunNbt(entity).getInt(STUN_TIMER_KEY) > 0 || getStunNbt(entity).getInt(STUNT_POINT_KEY) > 0;
                for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, box, predicate)) {
                    NbtCompound stunNbt = getStunNbt(entity);
                    stunNbt.putInt(STUNT_POINT_KEY, START_STUNT_POINT);
                    stunNbt.putInt(STUN_TIMER_KEY, 0);
                    setStunNbt(entity, stunNbt);
                    syncStunData(entity);
                }
            }
        }
    }

    private static void applyStunMechanics(LivingEntity entity) {
        NbtCompound stunNbt = getStunNbt(entity);
        int currentStuntPoint = stunNbt.getInt(STUNT_POINT_KEY);
        int stunTimer = stunNbt.getInt(STUN_TIMER_KEY);

        currentStuntPoint++;
        stunTimer = RESET_TIMEOUT;
        stunNbt.putInt(STUNT_POINT_KEY, currentStuntPoint);
        stunNbt.putInt(STUN_TIMER_KEY, stunTimer);

        if (currentStuntPoint >= MAX_STUNT_POINT) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, STUN_DURATION, 7));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, STUN_DURATION, 2));
            entity.setVelocity(0, entity.getVelocity().y, 0);
            stunNbt.putInt(STUNT_POINT_KEY, START_STUNT_POINT);
            stunNbt.putInt(STUN_TIMER_KEY, STUN_DURATION);
        }

        setStunNbt(entity, stunNbt);
    }

    private static void updateStunTimer(LivingEntity entity) {
        NbtCompound stunNbt = getStunNbt(entity);
        int stunTimer = stunNbt.getInt(STUN_TIMER_KEY);
        if (stunTimer > 0) {
            stunTimer--;
            if (stunTimer <= 0) {
                stunNbt.putInt(STUNT_POINT_KEY, START_STUNT_POINT);
            }
            stunNbt.putInt(STUN_TIMER_KEY, stunTimer);
            setStunNbt(entity, stunNbt);
        }
    }

    public static int getStuntPoint(LivingEntity entity) {
        return getStunNbt(entity).getInt(STUNT_POINT_KEY);
    }

    public static void setStuntPoint(LivingEntity entity, int value) {
        NbtCompound stunNbt = getStunNbt(entity);
        stunNbt.putInt(STUNT_POINT_KEY, Math.min(value, MAX_STUNT_POINT));
        setStunNbt(entity, stunNbt);
    }

    private static NbtCompound getStunNbt(LivingEntity entity) {
        NbtCompound nbt = new NbtCompound();
        entity.writeNbt(nbt);
        NbtCompound stunNbt = nbt.getCompound(STUN_DATA_KEY);
        if (!nbt.contains(STUN_DATA_KEY)) {
            nbt.put(STUN_DATA_KEY, stunNbt);
            entity.readNbt(nbt);
        }
        return stunNbt;
    }

    private static void setStunNbt(LivingEntity entity, NbtCompound stunNbt) {
        NbtCompound nbt = new NbtCompound();
        entity.writeNbt(nbt);
        nbt.put(STUN_DATA_KEY, stunNbt);
        entity.readNbt(nbt);
    }

    private static void syncStunData(LivingEntity entity) {
        if (entity.getWorld() instanceof ServerWorld serverWorld) {
            NbtCompound stunNbt = getStunNbt(entity);
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(entity.getId());
            buf.writeNbt(stunNbt);
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                ServerPlayNetworking.send(player, STUN_UPDATE_PACKET, buf);
            }
        }
    }
}
