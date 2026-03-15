package com.pixelfarm.game.systems;

import android.content.Context;
import android.content.SharedPreferences;

import com.pixelfarm.game.GameState;
import com.pixelfarm.game.data.GameData;
import com.pixelfarm.game.data.Models;

import org.json.JSONArray;
import org.json.JSONObject;

public class SaveSystem {
    private static final String PREFS   = "pixel_farm_save";
    private static final String KEY     = "game_state";
    private final SharedPreferences prefs;

    public SaveSystem(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void save(GameState state) {
        try {
            JSONObject j = new JSONObject();
            j.put("money",        state.money);
            j.put("debt",         state.debt);
            j.put("reputation",   state.reputation);
            j.put("totalHarvests",state.totalHarvests);
            j.put("totalEarned",  state.totalEarned);
            j.put("gameTimeMs",   state.getGameTimeMs());
            j.put("unlockedPlots",state.unlockedPlots);
            j.put("invCapacity",  state.inventoryCapacity);
            j.put("animalCap",    state.animalCapacity);
            j.put("workerCap",    state.workerCapacity);
            j.put("contractsCompleted", state.contractsCompleted);
            j.put("contractsFailed",    state.contractsFailed);

            // Plots
            JSONArray plots = new JSONArray();
            for (Models.Plot p : state.plots) {
                JSONObject pj = new JSONObject();
                pj.put("state",      p.state.name());
                pj.put("cropId",     p.cropId != null ? p.cropId : "");
                pj.put("growthPct",  p.growthPct);
                pj.put("plantedAt",  p.plantedAt);
                pj.put("watered",    p.watered);
                pj.put("waterCount", p.waterCount);
                pj.put("damaged",    p.damaged);
                plots.put(pj);
            }
            j.put("plots", plots);

            // Inventory
            JSONObject inv = new JSONObject();
            for (java.util.Map.Entry<String,Integer> e : state.inventory.entrySet())
                inv.put(e.getKey(), e.getValue());
            j.put("inventory", inv);

            // Buildings
            JSONArray blds = new JSONArray();
            for (String b : state.buildings) blds.put(b);
            j.put("buildings", blds);

            // Animals
            JSONArray animals = new JSONArray();
            for (Models.Animal a : state.animals) {
                JSONObject aj = new JSONObject();
                aj.put("typeId", a.typeId); aj.put("name", a.name);
                aj.put("happiness", a.happiness); aj.put("hunger", a.hunger);
                aj.put("lastProduce", a.lastProduceGameTime);
                aj.put("readyToCollect", a.readyToCollect);
                animals.put(aj);
            }
            j.put("animals", animals);

            // Workers
            JSONArray workers = new JSONArray();
            for (Models.Worker w : state.workers) {
                JSONObject wj = new JSONObject();
                wj.put("typeId", w.typeId); wj.put("name", w.name);
                wj.put("efficiency", w.efficiency); wj.put("active", w.active);
                wj.put("daysWorked", w.daysWorked);
                workers.put(wj);
            }
            j.put("workers", workers);

            // Loans
            JSONArray loans = new JSONArray();
            for (Models.ActiveLoan l : state.loans) {
                JSONObject lj = new JSONObject();
                lj.put("typeId", l.typeId); lj.put("amountRemaining", l.amountRemaining);
                lj.put("weeksRemaining", l.weeksRemaining);
                lj.put("weeklyInterest", l.weeklyInterest);
                lj.put("weeklyPayment", l.weeklyPayment);
                loans.put(lj);
            }
            j.put("loans", loans);

            prefs.edit().putString(KEY, j.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GameState load() {
        GameState state = new GameState();
        String json = prefs.getString(KEY, null);
        if (json == null) return state; // New game
        try {
            JSONObject j = new JSONObject(json);
            state.money         = j.optLong("money", 200);
            state.debt          = j.optLong("debt", 2000);
            state.reputation    = j.optInt("reputation", 0);
            state.totalHarvests = j.optInt("totalHarvests", 0);
            state.totalEarned   = j.optLong("totalEarned", 0);
            state.setGameTimeMs(j.optLong("gameTimeMs", 0));
            state.unlockedPlots = j.optInt("unlockedPlots", 12);
            state.inventoryCapacity = j.optInt("invCapacity", 50);
            state.animalCapacity    = j.optInt("animalCap", 3);
            state.workerCapacity    = j.optInt("workerCap", 2);
            state.contractsCompleted= j.optInt("contractsCompleted", 0);
            state.contractsFailed   = j.optInt("contractsFailed", 0);

            // Plots
            JSONArray plots = j.optJSONArray("plots");
            if (plots != null) {
                for (int i = 0; i < Math.min(plots.length(), state.plots.length); i++) {
                    JSONObject pj = plots.getJSONObject(i);
                    Models.Plot p = state.plots[i];
                    try { p.state = Models.Plot.State.valueOf(pj.optString("state","EMPTY")); }
                    catch (Exception ignored) {}
                    String cid = pj.optString("cropId","");
                    p.cropId     = cid.isEmpty() ? null : cid;
                    p.growthPct  = (float) pj.optDouble("growthPct", 0);
                    p.plantedAt  = pj.optLong("plantedAt", 0);
                    p.watered    = pj.optBoolean("watered", false);
                    p.waterCount = pj.optInt("waterCount", 0);
                    p.damaged    = pj.optBoolean("damaged", false);
                }
            }

            // Inventory
            JSONObject inv = j.optJSONObject("inventory");
            if (inv != null) {
                java.util.Iterator<String> keys = inv.keys();
                while (keys.hasNext()) {
                    String k = keys.next(); state.inventory.put(k, inv.optInt(k));
                }
            }

            // Buildings
            JSONArray blds = j.optJSONArray("buildings");
            if (blds != null) for (int i = 0; i < blds.length(); i++) state.buildings.add(blds.getString(i));

            // Animals
            JSONArray animals = j.optJSONArray("animals");
            if (animals != null) {
                for (int i = 0; i < animals.length(); i++) {
                    JSONObject aj = animals.getJSONObject(i);
                    Models.Animal a = new Models.Animal(aj.optString("typeId"), aj.optString("name"));
                    a.happiness = (float) aj.optDouble("happiness", 1.0);
                    a.hunger    = (float) aj.optDouble("hunger", 0.0);
                    a.lastProduceGameTime = aj.optLong("lastProduce", 0);
                    a.readyToCollect = aj.optBoolean("readyToCollect", false);
                    state.animals.add(a);
                }
            }

            // Workers
            JSONArray workers = j.optJSONArray("workers");
            if (workers != null) {
                for (int i = 0; i < workers.length(); i++) {
                    JSONObject wj = workers.getJSONObject(i);
                    Models.Worker w = new Models.Worker(wj.optString("typeId"), wj.optString("name"));
                    w.efficiency = (float) wj.optDouble("efficiency", 0.8);
                    w.active     = wj.optBoolean("active", true);
                    w.daysWorked = wj.optInt("daysWorked", 0);
                    state.workers.add(w);
                }
            }

            // Loans
            JSONArray loans = j.optJSONArray("loans");
            if (loans != null) {
                for (int i = 0; i < loans.length(); i++) {
                    JSONObject lj = loans.getJSONObject(i);
                    state.loans.add(new Models.ActiveLoan(
                            lj.optString("typeId"),
                            lj.optLong("amountRemaining"),
                            lj.optInt("weeksRemaining"),
                            (float) lj.optDouble("weeklyInterest"),
                            lj.optInt("weeklyPayment")));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new GameState(); // corrupt save -> new game
        }
        return state;
    }

    public void deleteSave() {
        prefs.edit().remove(KEY).apply();
    }
}
