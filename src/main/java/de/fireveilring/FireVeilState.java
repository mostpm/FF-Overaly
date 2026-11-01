package de.fireveilring;

import net.minecraft.world.phys.Vec3;

/** Merkt sich, wann der Fire Veil zuletzt gezuendet wurde. */
public final class FireVeilState {
    private static volatile long startMs = 0L;
    private static volatile long lastCastMs = 0L;
    private static volatile Vec3 castPos = Vec3.ZERO;

    private FireVeilState() {}

    /** Echter Cast mit der Wand (beachtet den 1s-Cooldown). */
    public static void onCast(Vec3 playerPos) {
        long now = System.currentTimeMillis();
        if (now - lastCastMs < (long) (FireVeilConfig.get().cooldownSeconds * 1000.0)) {
            return;
        }
        lastCastMs = now;
        start(playerPos);
    }

    /** Startet den Ring sofort (fuer /fireveil test). */
    public static void start(Vec3 playerPos) {
        startMs = System.currentTimeMillis();
        castPos = playerPos;
    }

    public static double remainingSeconds() {
        if (startMs == 0L) {
            return 0.0;
        }
        double elapsed = (System.currentTimeMillis() - startMs) / 1000.0;
        return FireVeilConfig.get().durationSeconds - elapsed;
    }

    public static Vec3 castPos() {
        return castPos;
    }
}
