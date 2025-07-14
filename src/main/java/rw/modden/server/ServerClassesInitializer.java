package rw.modden.server;

public class ServerClassesInitializer {
    public static void initialize() {
        ServerNetworking.initialize();
        ServerTickHandler.initialize();
    }
}
