package com.pixelfarm.game;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.pixelfarm.game.screens.AnimalsScreen;
import com.pixelfarm.game.screens.BankScreen;
import com.pixelfarm.game.screens.BuildingsScreen;
import com.pixelfarm.game.screens.FarmScreen;
import com.pixelfarm.game.screens.InventoryScreen;
import com.pixelfarm.game.screens.MarketScreen;
import com.pixelfarm.game.screens.ShopScreen;
import com.pixelfarm.game.screens.TitleScreen;
import com.pixelfarm.game.screens.WorkersScreen;

public class ScreenManager {

    public enum ScreenId {
        TITLE, FARM, MARKET, SHOP, INVENTORY,
        ANIMALS, BUILDINGS, BANK, WORKERS
    }

    private Screen currentScreen;
    private Screen nextScreen;
    private GameState state;

    // Pre-create all screens to avoid GC
    private Screen title, farm, market, shop, inventory,
                   animals, buildings, bank, workers;

    public ScreenManager(GameState state) {
        this.state = state;
        title      = new TitleScreen(state, this);
        farm       = new FarmScreen(state, this);
        market     = new MarketScreen(state, this);
        shop       = new ShopScreen(state, this);
        inventory  = new InventoryScreen(state, this);
        animals    = new AnimalsScreen(state, this);
        buildings  = new BuildingsScreen(state, this);
        bank       = new BankScreen(state, this);
        workers    = new WorkersScreen(state, this);

        currentScreen = title;
        currentScreen.onEnter();
    }

    public void goTo(ScreenId id) {
        Screen s = getScreen(id);
        if (s == null || s == currentScreen) return;
        currentScreen.onExit();
        currentScreen = s;
        currentScreen.onEnter();
    }

    private Screen getScreen(ScreenId id) {
        switch (id) {
            case TITLE:     return title;
            case FARM:      return farm;
            case MARKET:    return market;
            case SHOP:      return shop;
            case INVENTORY: return inventory;
            case ANIMALS:   return animals;
            case BUILDINGS: return buildings;
            case BANK:      return bank;
            case WORKERS:   return workers;
        }
        return null;
    }

    public void update(long deltaMs) {
        if (currentScreen != null)
            currentScreen.update(deltaMs);
    }

    public void render(Canvas canvas, int w, int h) {
        if (currentScreen != null)
            currentScreen.render(canvas, w, h);
    }

    public boolean onTouch(MotionEvent e) {
        return currentScreen != null && currentScreen.onTouch(e);
    }

    public Screen getCurrent() { return currentScreen; }
}
