package com.eventproof.util;

import com.eventproof.model.*;
import javafx.util.StringConverter;

public final class DisplayNames {
    private DisplayNames() { }
    public static String label(Enum<?> value) {
        if (value == null) return "";
        String lower = value.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
    public static <T> StringConverter<T> converter(java.util.function.Function<T, String> name) {
        return new StringConverter<>() {
            @Override public String toString(T value) { return value == null ? "" : name.apply(value); }
            @Override public T fromString(String text) { throw new UnsupportedOperationException(); }
        };
    }
    public static String event(Event event) { return event.getEventName() + " (#" + event.getEventId() + ")"; }
    public static String venue(Venue venue) { return venue.getVenueName() + " (" + venue.getCapacity() + " capacity)"; }
}
