package mei.arisuwu.deermod.api;

public class WaterDeerCropCallbackRegistry {
    private static WaterDeerCropCallback callback = null;

    public static void register(WaterDeerCropCallback cb) {
        callback = cb;
    }

    public static WaterDeerCropCallback get() {
        return callback;
    }

    public static boolean isRegistered() {
        return callback != null;
    }
}
