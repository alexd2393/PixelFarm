package com.pixelfarm.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.pixelfarm.game.renderer.NESPalette;
import com.pixelfarm.game.systems.SaveSystem;

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private GameLoop     gameLoop;
    private ScreenManager screenManager;
    private GameState    state;
    private SaveSystem   saveSystem;

    private final Paint debugPaint = new Paint();

    private int surfaceWidth  = 0;
    private int surfaceHeight = 0;

    // Daily tick tracker
    private int lastTickDay = 0;

    public GameSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);

        state         = new SaveSystem(context).load();
        saveSystem    = new SaveSystem(context);
        screenManager = new ScreenManager(state);

        debugPaint.setColor(Color.WHITE);
        debugPaint.setTextSize(28f);
        debugPaint.setAntiAlias(true);
    }

    // =====================================================================
    // SURFACE LIFECYCLE
    // =====================================================================

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceWidth  = getWidth();
        surfaceHeight = getHeight();
        gameLoop = new GameLoop(this);
        gameLoop.startLoop();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        surfaceWidth  = w;
        surfaceHeight = h;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        saveSystem.save(state);
        if (gameLoop != null) gameLoop.stopLoop();
    }

    // =====================================================================
    // GAME LOOP METHODS (called from GameLoop thread)
    // =====================================================================

    public void updateGame(long deltaMs) {
        state.update();

        // Daily tick
        int today = state.getDay();
        if (today != lastTickDay) {
            lastTickDay = today;
            state.checkDailyTick();
        }

        state.cleanNotifications();
        screenManager.update(deltaMs);
    }

    public void drawFrame() {
        Canvas canvas = null;
        try {
            canvas = getHolder().lockCanvas();
            if (canvas != null) {
                synchronized (getHolder()) {
                    canvas.drawColor(NESPalette.UI_BG);
                    screenManager.render(canvas, surfaceWidth, surfaceHeight);
                    // Uncomment next line for FPS debug:
                    // canvas.drawText("FPS:" + (int)gameLoop.currentFps, 10, 30, debugPaint);
                }
            }
        } finally {
            if (canvas != null) {
                try { getHolder().unlockCanvasAndPost(canvas); }
                catch (Exception ignored) {}
            }
        }
    }

    // =====================================================================
    // PAUSE / RESUME
    // =====================================================================

    public void pause() {
        saveSystem.save(state);
        if (gameLoop != null) gameLoop.stopLoop();
    }

    public void resume() {
        if (gameLoop == null || !gameLoop.isAlive()) {
            gameLoop = new GameLoop(this);
            gameLoop.startLoop();
        }
    }

    // =====================================================================
    // TOUCH INPUT
    // =====================================================================

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        screenManager.onTouch(event);
        return true;
    }
}
