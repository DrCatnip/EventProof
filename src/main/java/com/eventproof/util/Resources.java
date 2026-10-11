package com.eventproof.util;

import java.net.URL;
import java.util.Objects;

public final class Resources {
    private Resources() { }
    public static URL url(String path) {
        return Objects.requireNonNull(Resources.class.getResource("/com/eventproof/" + path),
                "Missing EventProof resource: " + path);
    }
}
