package de.fireveilring;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Einstellungen, gespeichert in .minecraft/config/fireveilring.json */
public final class FireVeilConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static FireVeilConfig instance = new FireVeilConfig();

    // --- im Menue (/fireveil) einstellbar ---
    public boolean enabled = true;
    /** Dicke der Linie in Bloecken. */
    public double thickness = 0.10;
    public int red = 208;
    public int green = 24;
    public int blue = 24;

    // --- nur in der Datei einstellbar ---
    public double radius = 5.0;
    public double opacity = 0.85;
    public double durationSeconds = 5.0;
    public double cooldownSeconds = 1.0;
    public double heightOffset = 0.05;
    public boolean followPlayer = true;
    public int segments = 128;

    public static FireVeilConfig get() {
        return instance;
    }

    public int rgb() {
        return (clamp(red) << 16) | (clamp(green) << 8) | clamp(blue);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("fireveilring.json");
    }

    public static void load() {
        FireVeilConfig loaded = null;
        try {
            if (Files.exists(path())) {
                loaded = GSON.fromJson(Files.readString(path()), FireVeilConfig.class);
            }
        } catch (Exception e) {
            System.err.println("[FireVeilRing] Config nicht lesbar, nutze Standardwerte: " + e);
        }
        instance = loaded != null ? loaded : new FireVeilConfig();
        instance.sanitize();
        save();
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(instance));
        } catch (Exception e) {
            System.err.println("[FireVeilRing] Config nicht speicherbar: " + e);
        }
    }

    private void sanitize() {
        thickness = Math.max(0.02, Math.min(thickness, 0.5));
        red = clamp(red);
        green = clamp(green);
        blue = clamp(blue);
        radius = Math.max(0.5, Math.min(radius, 64.0));
        opacity = Math.max(0.05, Math.min(opacity, 1.0));
        durationSeconds = Math.max(0.1, durationSeconds);
        cooldownSeconds = Math.max(0.0, cooldownSeconds);
        segments = Math.max(16, Math.min(segments, 720));
    }
}
