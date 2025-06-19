package rw.modden.combat.path;

import rw.modden.character.PlayerData;

public abstract class Path {
    protected boolean hasPassiveGeneration;
    protected final PathType pathType;
    protected float resource;
    protected float maxResource;
    protected int graceOfWays;

    public Path(PlayerData playerData, PathType pathType) {
        this.pathType = pathType;
    }

    public static void initialize() {}
    public static void addResource(float amount) {} // Увеличить шкалу
    public static void consumeResource(float amount) {} // Уменьшить шкалу
    public static void updatePassiveResource() {} // Проверить мир и добавить ресурс
    public static void upgradePath() {} // Прокачка пути
    public static void checkWorldAccess(PathType targetPath) {} // Проверка доступа к другому миру
    public static void syncData() {} // Синхронизация шкалы и доступа с клиентом
}
