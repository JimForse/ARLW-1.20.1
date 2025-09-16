package rw.modden.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import rw.modden.Axorunelostworlds;

public class PrimordialMatterParticle extends SpriteBillboardParticle {
    protected PrimordialMatterParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, SpriteProvider spriteProvider) {
        super(world, x, y, z, vx, vy, vz);
        Axorunelostworlds.LOGGER.info("Attempting to create PrimordialMatterParticle at x=" + x + ", y=" + y + ", z=" + z);
        if (spriteProvider == null) {
            Axorunelostworlds.LOGGER.error("SpriteProvider is null for PrimordialMatterParticle - texture not loaded");
            this.markDead(); // Убиваем частицу, чтобы не крашило
            return;
        }
        Axorunelostworlds.LOGGER.info("SpriteProvider found: " + spriteProvider);
        this.setSprite(spriteProvider); // Устанавливаем текстуру
        this.scale = 0.2F + world.random.nextFloat() * 0.1F; // Размер 0.2-0.3 блоков
        this.maxAge = 20 + world.random.nextInt(20); // Время жизни 1-2 секунды
        this.red = 0.0F; // Бирюзовый цвет (RGB: 0, 0.8, 0.8)
        this.green = 0.8F;
        this.blue = 0.8F;
        this.alpha = 0.8F; // Прозрачность 80%
        this.velocityX = vx; // Скорость (медленная)
        this.velocityY = vy + 0.01; // Медленно вверх
        this.velocityZ = vz;
        this.collidesWithWorld = false; // Не сталкивается с блоками
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_LIT; // Светящаяся частица
    }

    @Override
    public int getBrightness(float tint) {
        return 240; // Максимальная яркость (emissive, светится в темноте)
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
            Axorunelostworlds.LOGGER.info("Particle factory created with spriteProvider: " + spriteProvider);
        }

        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            Axorunelostworlds.LOGGER.info("Creating PrimordialMatterParticle at x=" + x + ", y=" + y + ", z=" + z);
            return new PrimordialMatterParticle(world, x, y, z, vx, vy, vz, spriteProvider);
        }
    }
}