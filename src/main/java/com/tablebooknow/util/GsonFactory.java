package com.tablebooknow.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Factory class for creating properly configured Gson instances
 * that can handle Java time classes without reflection issues.
 */
public class GsonFactory {

    /**
     * Creates a Gson instance configured with adapters for Java time classes.
     * @return A configured Gson instance
     */
    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    /**
     * Creates a Gson instance with pretty printing and adapters for Java time classes.
     * @return A configured Gson instance with pretty printing
     */
    public static Gson createPrettyGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
    }
}