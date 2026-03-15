package com.pixelfarm.game.systems;

import com.pixelfarm.game.GameState;
import com.pixelfarm.game.data.GameData;
import com.pixelfarm.game.data.Models;
import com.pixelfarm.game.renderer.NESPalette;

import java.util.Random;

public class EventSystem {
    private final Random rnd = new Random();

    public void tryTriggerEvent(GameState state) {
        // Only trigger if no active event and enough days have passed
        if (state.activeEvent != null) return;
        if (state.daysSinceLastEvent < 5) return;

        // 25% chance each week
        if (rnd.nextFloat() > 0.25f) return;

        GameData.EventData def = GameData.EVENTS[rnd.nextInt(GameData.EVENTS.length)];
        state.activeEvent = new Models.ActiveEvent(def, state.getDay());
        state.daysSinceLastEvent = 0;

        // Apply instant effects
        if ("bonus_money".equals(def.effectKey)) {
            state.money += (long) def.effectValue;
            state.addNotification(def.title + " +$" + (int)def.effectValue,
                    def.isPositive ? NESPalette.GOLD : NESPalette.RED);
        } else if ("theft_pct".equals(def.effectKey)) {
            // Remove % of each inventory item
            for (String key : new java.util.ArrayList<>(state.inventory.keySet())) {
                int qty = state.getInventoryCount(key);
                int remove = (int)(qty * def.effectValue);
                if (remove > 0) state.removeFromInventory(key, remove);
            }
            state.addNotification(def.title + " Perdiste items!", NESPalette.RED);
        } else if ("flood".equals(def.effectKey)) {
            // Damage 2 random plots
            java.util.List<Integer> active = new java.util.ArrayList<>();
            for (int i = 0; i < state.unlockedPlots; i++)
                if (state.plots[i].isGrowing()) active.add(i);
            for (int i = 0; i < Math.min(2, active.size()); i++) {
                state.plots[active.get(rnd.nextInt(active.size()))].damaged = true;
            }
            state.addNotification(def.title, NESPalette.RED);
        } else if ("fire_worker".equals(def.effectKey)) {
            if (!state.workers.isEmpty()) {
                state.workers.remove(rnd.nextInt(state.workers.size()));
                state.addNotification(def.title, NESPalette.RED);
            }
        } else {
            int c = def.isPositive ? NESPalette.LIGHT_GREEN : NESPalette.RED;
            state.addNotification(def.title, c);
        }
    }
}
