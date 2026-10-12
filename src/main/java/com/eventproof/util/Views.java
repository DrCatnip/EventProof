package com.eventproof.util;

import com.eventproof.service.AppContext;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.io.UncheckedIOException;

public final class Views {
    private Views() { }
    public static FXMLLoader loader(String name, AppContext context) {
        var loader = new FXMLLoader(Resources.url("fxml/" + name + ".fxml"));
        loader.setControllerFactory(type -> {
            try {
                try { return type.getConstructor(AppContext.class).newInstance(context); }
                catch (NoSuchMethodException exception) { return type.getConstructor().newInstance(); }
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Cannot create controller " + type.getSimpleName(), exception);
            }
        });
        return loader;
    }
    public static <T> T load(FXMLLoader loader) {
        try { return loader.load(); }
        catch (IOException exception) { throw new UncheckedIOException("Cannot load view", exception); }
    }
}
