package com.pixelfarm.game;

public class GameLoop extends Thread {

    private static final long TARGET_FPS     = 60;
    private static final long MS_PER_FRAME   = 1000 / TARGET_FPS;
    private static final long MAX_DELTA_MS   = 50; // cap frame delta

    private final GameSurfaceView view;
    private volatile boolean running = false;

    // Diagnostics
    public float currentFps = 60f;
    private long frameCount  = 0;
    private long fpsTimer    = 0;

    public GameLoop(GameSurfaceView view) {
        this.view = view;
        setName("GameLoop");
        setDaemon(true);
    }

    public void startLoop() {
        running = true;
        start();
    }

    public void stopLoop() {
        running = false;
        try { join(1000); } catch (InterruptedException ignored) {}
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        fpsTimer = lastTime;

        while (running) {
            long now   = System.currentTimeMillis();
            long delta = Math.min(now - lastTime, MAX_DELTA_MS);
            lastTime   = now;

            // Update game logic
            view.updateGame(delta);

            // Draw frame
            view.drawFrame();

            // FPS counter
            frameCount++;
            if (now - fpsTimer >= 1000) {
                currentFps = frameCount * 1000f / (now - fpsTimer);
                frameCount = 0;
                fpsTimer   = now;
            }

            // Sleep to target FPS
            long elapsed = System.currentTimeMillis() - now;
            long sleep   = MS_PER_FRAME - elapsed;
            if (sleep > 0) {
                try { Thread.sleep(sleep); }
                catch (InterruptedException ignored) {}
            }
        }
    }
}
