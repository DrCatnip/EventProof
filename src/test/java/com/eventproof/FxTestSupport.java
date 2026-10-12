package com.eventproof;

import javafx.application.Platform;
import java.util.concurrent.*;

public final class FxTestSupport {
    private FxTestSupport() { }
    public static void start() throws Exception {
        var ready = new CountDownLatch(1);
        try { Platform.startup(() -> { Platform.setImplicitExit(false); ready.countDown(); }); }
        catch (IllegalStateException initialized) { Platform.runLater(ready::countDown); }
        if (!ready.await(15, TimeUnit.SECONDS)) throw new AssertionError("JavaFX did not start");
    }
    public static <T> T run(java.util.concurrent.Callable<T> operation) throws Exception {
        var task = new FutureTask<>(operation);
        Platform.runLater(task);
        return task.get(20, TimeUnit.SECONDS);
    }
}
