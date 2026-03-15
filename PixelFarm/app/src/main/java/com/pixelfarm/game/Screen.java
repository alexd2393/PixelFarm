package com.pixelfarm.game;

import android.graphics.Canvas;
import android.view.MotionEvent;

public abstract class Screen {
    protected GameState state;
    protected ScreenManager manager;
    protected int SW = 0, SH = 0; // screen width/height updated each render

    public Screen(GameState state, ScreenManager manager) {
        this.state = state; this.manager = manager;
    }
    public abstract void update(long deltaMs);
    public abstract void render(Canvas canvas, int w, int h);
    public void onEnter() {}
    public void onExit()  {}
    public boolean onTouch(MotionEvent e) { return false; }

    protected void navBarTap(float tx, float ty) {
        if (SH == 0 || SW == 0) return;
        if (ty < SH - 78) return;
        float bw = SW / 5f;
        int tab = (int)(tx / bw);
        switch (tab) {
            case 0: manager.goTo(ScreenManager.ScreenId.FARM);      break;
            case 1: manager.goTo(ScreenManager.ScreenId.SHOP);      break;
            case 2: manager.goTo(ScreenManager.ScreenId.MARKET);    break;
            case 3: manager.goTo(ScreenManager.ScreenId.INVENTORY); break;
            case 4: manager.goTo(ScreenManager.ScreenId.ANIMALS);   break;
        }
    }
}
