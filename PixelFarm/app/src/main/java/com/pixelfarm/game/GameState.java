package com.pixelfarm.game;

import com.pixelfarm.game.data.GameData;
import com.pixelfarm.game.data.Models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameState {

    // =====================================================================
    // TIME SYSTEM
    // 1 game day = 3 real minutes = 180,000 ms
    // 1 game hour = 7,500 ms real
    // 24 hours per day
    // =====================================================================
    public static final long MS_PER_GAME_HOUR = 7500L;
    public static final long MS_PER_GAME_DAY  = MS_PER_GAME_HOUR * 24;  // 180000
    public static final int  HOURS_PER_DAY    = 24;
    public static final int  DAYS_PER_SEASON  = 28;
    public static final int  SEASONS_PER_YEAR = 4;

    private long gameTimeMs = 0L;    // accumulated game time in ms
    private long lastRealMs = 0L;    // last real System.currentTimeMillis()

    // =====================================================================
    // GAME PROGRESS
    // =====================================================================
    public long money         = 200L;
    public long debt          = 2000L;  // initial debt
    public int  reputation    = 0;
    public int  totalHarvests = 0;
    public long totalEarned   = 0L;

    // =====================================================================
    // FARM GRID  (8 columns × 5 rows = 40 plots, expandable)
    // =====================================================================
    public static final int GRID_COLS = 8;
    public static final int GRID_ROWS = 5;
    public int unlockedPlots = 12; // start with 12 plots
    public Models.Plot[] plots = new Models.Plot[GRID_COLS * GRID_ROWS];

    // =====================================================================
    // INVENTORY  item_id -> quantity
    // =====================================================================
    public Map<String, Integer> inventory = new HashMap<>();
    public int inventoryCapacity = 50;

    // =====================================================================
    // ANIMALS
    // =====================================================================
    public List<Models.Animal> animals = new ArrayList<>();
    public int animalCapacity = 3; // starts with 3 slots

    // =====================================================================
    // WORKERS
    // =====================================================================
    public List<Models.Worker> workers = new ArrayList<>();
    public int workerCapacity = 2;

    // =====================================================================
    // BUILDINGS  (set of building ids that have been built)
    // =====================================================================
    public Set<String> buildings = new HashSet<>();

    // =====================================================================
    // MARKET  (crop_id -> price multiplier)
    // =====================================================================
    public Map<String, Float> marketMult = new HashMap<>();

    // =====================================================================
    // CONTRACTS
    // =====================================================================
    public List<Models.Contract> contracts = new ArrayList<>();
    public List<Models.Contract> availableContracts = new ArrayList<>();

    // =====================================================================
    // LOANS
    // =====================================================================
    public List<Models.ActiveLoan> loans = new ArrayList<>();

    // =====================================================================
    // EVENTS
    // =====================================================================
    public Models.ActiveEvent activeEvent = null;
    public int daysSinceLastEvent = 0;

    // =====================================================================
    // NOTIFICATIONS
    // =====================================================================
    public List<Models.Notification> notifications = new ArrayList<>();

    // =====================================================================
    // STATISTICS
    // =====================================================================
    public int contractsCompleted = 0;
    public int contractsFailed    = 0;

    // =====================================================================
    // IRRIGATION / WEATHER
    // =====================================================================
    public float rainBonus = 1.0f;   // from weather event

    // =====================================================================
    // CONSTRUCTOR
    // =====================================================================
    public GameState() {
        // Initialize all plots
        for (int i = 0; i < plots.length; i++) {
            plots[i] = new Models.Plot();
        }

        // Initialize market multipliers
        for (GameData.CropType ct : GameData.CROPS) {
            marketMult.put(ct.id, 1.0f);
        }

        lastRealMs = System.currentTimeMillis();
    }

    // =====================================================================
    // TIME MANAGEMENT
    // =====================================================================

    public void update() {
        long now = System.currentTimeMillis();
        long delta = now - lastRealMs;
        if (delta > 5000) delta = 5000; // cap at 5s (app was in bg)
        lastRealMs = now;
        gameTimeMs += delta;
        updateCrops(delta);
        updateAnimals(delta);
        processWorkers(delta);
    }

    public long getGameTimeMs()  { return gameTimeMs; }
    public void setGameTimeMs(long t) { gameTimeMs = t; lastRealMs = System.currentTimeMillis(); }

    public int getDay()    { return (int)(gameTimeMs / MS_PER_GAME_DAY) + 1; }
    public int getHour()   { return (int)((gameTimeMs % MS_PER_GAME_DAY) / MS_PER_GAME_HOUR); }
    public int getMinute() { return (int)((gameTimeMs % MS_PER_GAME_HOUR) / (MS_PER_GAME_HOUR / 60)); }

    public GameData.Season getSeason() {
        int seasonIndex = ((getDay() - 1) / DAYS_PER_SEASON) % SEASONS_PER_YEAR;
        return GameData.Season.values()[seasonIndex];
    }

    public int getYear() {
        return ((getDay() - 1) / (DAYS_PER_SEASON * SEASONS_PER_YEAR)) + 1;
    }

    public int getDayOfSeason() {
        return ((getDay() - 1) % DAYS_PER_SEASON) + 1;
    }

    public int getWeek() {
        return ((getDay() - 1) / 7) + 1;
    }

    // =====================================================================
    // CROP MANAGEMENT
    // =====================================================================

    private void updateCrops(long deltaRealMs) {
        boolean hasIrrigation = buildings.contains("irrigation");
        boolean hasWell       = buildings.contains("well");
        float   wellBonus     = hasWell ? 1.20f : 1.0f;
        float   rainBonus     = getEventEffect("growth_mult", 1.0f);

        for (int i = 0; i < plots.length; i++) {
            Models.Plot plot = plots[i];
            if (!plot.isGrowing() || plot.cropId == null) continue;
            if (plot.damaged) continue;

            GameData.CropType ct = GameData.getCropById(plot.cropId);
            if (ct == null) continue;

            // Water requirement: crop needs to be watered at least once
            // If not watered, growth is 50% slower
            float waterMult  = plot.watered ? 1.0f : 0.5f;
            float seasonMult = ct.getSeasonMultiplier(getSeason());

            // Greenhouse overrides season penalty
            if (buildings.contains("greenhouse")) seasonMult = 1.0f;

            float totalMult = waterMult * seasonMult * wellBonus * rainBonus;

            // If irrigation building, auto-water all plots
            if (hasIrrigation && !plot.watered) {
                plot.watered = true;
                plot.waterCount++;
            }

            // Growth duration in real ms
            long growthMs = ct.growthHours * MS_PER_GAME_HOUR;
            float progress = (float) deltaRealMs / growthMs * totalMult;
            plot.growthPct = Math.min(1.0f, plot.growthPct + progress);

            if (plot.growthPct >= 1.0f) {
                plot.growthPct = 1.0f;
                plot.state     = Models.Plot.State.READY;
            } else if (plot.watered) {
                plot.state = Models.Plot.State.WATERED;
            } else {
                plot.state = Models.Plot.State.GROWING;
            }
        }
    }

    public boolean plantCrop(int plotIndex, String cropId) {
        if (plotIndex < 0 || plotIndex >= unlockedPlots) return false;
        Models.Plot plot = plots[plotIndex];
        if (!plot.isEmpty()) return false;

        GameData.CropType ct = GameData.getCropById(cropId);
        if (ct == null) return false;
        if (money < ct.seedCost) return false;
        if (getInventoryCount("seed_" + cropId) <= 0 &&
            getInventoryCount(cropId + "_seed") <= 0) {
            // Buy seed automatically (from internal stock)
            money -= ct.seedCost;
        }

        plot.cropId    = cropId;
        plot.state     = Models.Plot.State.PLANTED;
        plot.growthPct = 0f;
        plot.plantedAt = gameTimeMs;
        plot.watered   = false;
        plot.waterCount= 0;
        return true;
    }

    public boolean waterPlot(int plotIndex) {
        if (plotIndex < 0 || plotIndex >= unlockedPlots) return false;
        Models.Plot plot = plots[plotIndex];
        if (!plot.isGrowing()) return false;
        plot.watered = true;
        plot.waterCount++;
        if (plot.state == Models.Plot.State.PLANTED || plot.state == Models.Plot.State.GROWING)
            plot.state = Models.Plot.State.WATERED;
        return true;
    }

    public int harvestPlot(int plotIndex) {
        if (plotIndex < 0 || plotIndex >= unlockedPlots) return 0;
        Models.Plot plot = plots[plotIndex];
        if (!plot.isReady()) return 0;

        GameData.CropType ct = GameData.getCropById(plot.cropId);
        if (ct == null) return 0;

        int qty = ct.harvestCount;

        // Double harvest event?
        if (activeEvent != null && "harvest_mult".equals(activeEvent.effectKey)) {
            qty = (int)(qty * activeEvent.effectValue);
        }

        addToInventory(ct.id, qty);
        totalHarvests++;
        reputation += 5;
        plot.reset();
        addNotification("+" + qty + " " + ct.name, 0xFF00FF00);
        return qty;
    }

    // =====================================================================
    // ANIMAL MANAGEMENT
    // =====================================================================

    private void updateAnimals(long deltaRealMs) {
        boolean hasVet = hasWorkerOfType("vet");
        for (Models.Animal a : animals) {
            GameData.AnimalType at = GameData.getAnimalById(a.typeId);
            if (at == null) continue;

            // Hunger increases over time
            float hungerIncrease = (float) deltaRealMs / (MS_PER_GAME_DAY * 1.0f);
            a.hunger = Math.min(1.0f, a.hunger + hungerIncrease);

            // Happiness decreases if hungry
            if (a.hunger > 0.7f) {
                a.happiness = Math.max(0.2f, a.happiness - hungerIncrease * 0.5f);
            }

            // Vet maintains animal happiness
            if (hasVet) a.happiness = Math.min(1.0f, a.happiness + 0.0001f);

            // Production timer
            long produceMs = at.produceHours * MS_PER_GAME_HOUR;
            if (!a.readyToCollect) {
                long elapsed = gameTimeMs - a.lastProduceGameTime;
                if (elapsed >= produceMs) {
                    a.readyToCollect = true;
                }
            }
        }
    }

    public boolean feedAnimal(int index) {
        if (index < 0 || index >= animals.size()) return false;
        Models.Animal a = animals.get(index);
        a.hunger = 0f;
        a.happiness = Math.min(1.0f, a.happiness + 0.2f);
        return true;
    }

    public int collectAnimal(int index) {
        if (index < 0 || index >= animals.size()) return 0;
        Models.Animal a = animals.get(index);
        if (!a.readyToCollect) return 0;
        GameData.AnimalType at = GameData.getAnimalById(a.typeId);
        if (at == null) return 0;

        int qty = (int)(a.getProductionMultiplier() > 0.5f ? 1 : 0) + 1;
        addToInventory(at.produceItem, qty);
        a.readyToCollect = false;
        a.lastProduceGameTime = gameTimeMs;
        a.totalProduced += qty;
        addNotification("+" + qty + " " + at.produceItem, 0xFF90EE90);
        return qty;
    }

    // =====================================================================
    // WORKER AUTOMATION
    // =====================================================================

    private void processWorkers(long deltaRealMs) {
        for (Models.Worker w : workers) {
            if (!w.active) continue;
            GameData.WorkerType wt = GameData.getWorkerById(w.typeId);
            if (wt == null) continue;
            switch (wt.id) {
                case "farmer":
                    doFarmerWork(w);
                    break;
                case "harvester":
                    doHarvesterWork(w);
                    break;
                case "seller":
                    // Handled in daily tick
                    break;
                case "vet":
                    // Handled in updateAnimals
                    break;
            }
        }
    }

    private void doFarmerWork(Models.Worker w) {
        int count = 0;
        for (int i = 0; i < unlockedPlots; i++) {
            if (plots[i].isGrowing() && !plots[i].watered) {
                plots[i].watered = true;
                plots[i].waterCount++;
                count++;
                if (count >= 3) break; // water up to 3 per tick
            }
        }
        w.currentTask = count > 0 ? "Regando parcelas..." : "En espera";
    }

    private void doHarvesterWork(Models.Worker w) {
        int count = 0;
        for (int i = 0; i < unlockedPlots; i++) {
            if (plots[i].isReady()) {
                harvestPlot(i);
                count++;
                if (count >= 2) break;
            }
        }
        w.currentTask = count > 0 ? "Cosechando..." : "En espera";
    }

    public boolean hasWorkerOfType(String typeId) {
        for (Models.Worker w : workers)
            if (typeId.equals(w.typeId) && w.active) return true;
        return false;
    }

    // =====================================================================
    // INVENTORY
    // =====================================================================

    public int getInventoryCount(String itemId) {
        return inventory.getOrDefault(itemId, 0);
    }

    public int getTotalInventory() {
        int total = 0;
        for (int v : inventory.values()) total += v;
        return total;
    }

    public boolean addToInventory(String itemId, int qty) {
        if (getTotalInventory() + qty > inventoryCapacity) return false;
        inventory.put(itemId, getInventoryCount(itemId) + qty);
        return true;
    }

    public boolean removeFromInventory(String itemId, int qty) {
        int have = getInventoryCount(itemId);
        if (have < qty) return false;
        if (have == qty) inventory.remove(itemId);
        else inventory.put(itemId, have - qty);
        return true;
    }

    // =====================================================================
    // SELLING
    // =====================================================================

    public long getSellPrice(String itemId, int qty) {
        GameData.CropType ct = GameData.getCropById(itemId);
        float price = 0;
        if (ct != null) {
            price = ct.basePrice;
        } else {
            // Processed items / animal products
            for (GameData.ProcessedItem pi : GameData.PROCESSES) {
                if (pi.outputId.equals(itemId)) { price = pi.sellPrice; break; }
            }
            for (GameData.AnimalType at : GameData.ANIMALS) {
                if (at.produceItem.equals(itemId)) { price = at.produceSellPrice; break; }
            }
        }
        if (price == 0) price = 20; // fallback

        float mult = marketMult.getOrDefault(itemId, 1.0f);
        float eventMult = getEventEffect("price_mult_all", 1.0f);

        // Market local building
        if (buildings.contains("market_local")) eventMult *= 1.15f;

        return (long)(price * mult * eventMult * qty);
    }

    public long sellItem(String itemId, int qty) {
        if (!removeFromInventory(itemId, qty)) return -1;
        long earned = getSellPrice(itemId, qty);
        money += earned;
        totalEarned += earned;
        reputation += (int)(earned / 50);

        // Check contracts
        for (Models.Contract c : contracts) {
            if (!c.completed && !c.failed && c.itemId.equals(itemId)) {
                c.currentDelivered = Math.min(c.quantity,
                        c.currentDelivered + qty);
                if (c.currentDelivered >= c.quantity) {
                    c.completed = true;
                    money += c.reward;
                    reputation += 200;
                    contractsCompleted++;
                    addNotification("Contrato completado! +$" + c.reward,
                            0xFFFFD700);
                }
                break;
            }
        }
        addNotification("+$" + earned, 0xFFFFD700);
        return earned;
    }

    // =====================================================================
    // PROCESSING (mill, winery, etc.)
    // =====================================================================

    public boolean processItem(GameData.ProcessedItem pi) {
        if (!buildings.contains(pi.buildingId)) return false;
        if (!removeFromInventory(pi.inputId, pi.inputQty)) return false;
        addToInventory(pi.outputId, pi.outputQty);
        addNotification(pi.outputQty + " " + pi.outputName + " procesado(s)",
                0xFF90D4FF);
        return true;
    }

    // =====================================================================
    // DAILY TICK (call once per game day)
    // =====================================================================

    private int lastDayProcessed = 0;

    public void checkDailyTick() {
        int today = getDay();
        if (today <= lastDayProcessed) return;
        lastDayProcessed = today;

        // Pay animal feed costs
        long feedCost = 0;
        for (Models.Animal a : animals) {
            GameData.AnimalType at = GameData.getAnimalById(a.typeId);
            if (at != null) feedCost += at.feedCostPerDay;
        }
        if (feedCost > 0) {
            if (money >= feedCost) {
                money -= feedCost;
            } else {
                // Can't afford feed -> animals lose happiness
                for (Models.Animal a : animals)
                    a.happiness = Math.max(0.1f, a.happiness - 0.3f);
                addNotification("No puedes pagar la comida animal!", 0xFFFF4444);
            }
        }

        // Pay worker salaries
        for (Models.Worker w : workers) {
            GameData.WorkerType wt = GameData.getWorkerById(w.typeId);
            if (wt == null) continue;
            if (money >= wt.dailySalary) {
                money -= wt.dailySalary;
            } else {
                w.active = false;
                addNotification(w.name + " se fue por falta de pago", 0xFFFF4444);
            }
        }

        // Pay debt weekly (every 7 days)
        if (today % 7 == 0) {
            for (Models.ActiveLoan loan : loans) {
                if (loan.weeksRemaining > 0) {
                    if (money >= loan.weeklyPayment) {
                        money -= loan.weeklyPayment;
                        loan.amountRemaining -= loan.weeklyPayment;
                        loan.weeksRemaining--;
                        if (loan.amountRemaining <= 0) {
                            loan.amountRemaining = 0;
                            loan.weeksRemaining = 0;
                            addNotification("Préstamo pagado completamente!", 0xFF00FF00);
                        }
                    } else {
                        // Interest penalty
                        loan.amountRemaining = (long)(loan.amountRemaining * (1 + loan.weeklyInterest));
                        loan.weeksRemaining++;
                        addNotification("No pudiste pagar cuota! Interés aumenta", 0xFFFF4444);
                    }
                }
            }
            // Initial debt
            if (debt > 0) {
                long payment = Math.min(debt, 200L);
                if (money >= payment) {
                    money -= payment;
                    debt -= payment;
                } else {
                    debt = (long)(debt * 1.05f); // 5% penalty
                    addNotification("Deuda aumenta por interés: $" + debt, 0xFFFF4444);
                }
            }
        }

        // Seller worker auto-sells
        if (hasWorkerOfType("seller")) {
            long autoEarned = 0;
            for (GameData.CropType ct : GameData.CROPS) {
                int qty = getInventoryCount(ct.id);
                if (qty > 2) {
                    int sellQty = qty / 2;
                    removeFromInventory(ct.id, sellQty);
                    autoEarned += getSellPrice(ct.id, sellQty);
                }
            }
            if (autoEarned > 0) {
                money += autoEarned;
                totalEarned += autoEarned;
                addNotification("Vendedor auto-vendió: +$" + autoEarned, 0xFFFFD700);
            }
        }

        // Update market prices (daily fluctuation)
        updateMarketPrices();

        // Check event expiry
        if (activeEvent != null) {
            activeEvent.daysRemaining--;
            if (activeEvent.daysRemaining <= 0) {
                activeEvent = null;
            }
        }

        daysSinceLastEvent++;

        // Try to trigger a random event (weekly check)
        if (today % 7 == 0) {
            new com.pixelfarm.game.systems.EventSystem().tryTriggerEvent(this);
        }

        // Generate new contracts on week start
        if (today % 7 == 1) {
            generateContracts();
        }

        // Check failed contracts
        for (Models.Contract c : contracts) {
            if (!c.completed && !c.failed && c.deadlineDays-- <= 0) {
                c.failed = true;
                contractsFailed++;
                reputation = Math.max(0, reputation - 50);
                addNotification("Contrato fallido: " + c.itemName, 0xFFFF4444);
            }
        }

        // Remove old contracts
        contracts.removeIf(c -> (c.completed || c.failed) && c.deadlineDays < -3);
    }

    private void updateMarketPrices() {
        java.util.Random rnd = new java.util.Random();
        for (GameData.CropType ct : GameData.CROPS) {
            float current = marketMult.getOrDefault(ct.id, 1.0f);
            float change  = (rnd.nextFloat() - 0.5f) * 0.15f; // ±7.5%
            float next    = Math.max(0.5f, Math.min(2.0f, current + change));
            marketMult.put(ct.id, next);
        }
    }

    private void generateContracts() {
        availableContracts.clear();
        java.util.Random rnd = new java.util.Random();
        GameData.CropType[] avail = GameData.CROPS;
        int numContracts = 2 + rnd.nextInt(3); // 2-4 contracts
        for (int i = 0; i < numContracts; i++) {
            GameData.CropType ct = avail[rnd.nextInt(avail.length)];
            int qty     = 5 + rnd.nextInt(20);
            long reward = (long)(ct.basePrice * qty * 1.8f);
            int days    = 7 + rnd.nextInt(14);
            String id   = "contract_" + System.currentTimeMillis() + i;
            availableContracts.add(new Models.Contract(
                    id, ct.id, ct.name, qty, reward, days, ct.color));
        }
    }

    public boolean acceptContract(Models.Contract c) {
        contracts.add(c);
        availableContracts.remove(c);
        return true;
    }

    // =====================================================================
    // EVENTS
    // =====================================================================

    public float getEventEffect(String key, float defaultVal) {
        if (activeEvent == null) return defaultVal;
        if (key.equals(activeEvent.effectKey)) return activeEvent.effectValue;
        return defaultVal;
    }

    // =====================================================================
    // NOTIFICATIONS
    // =====================================================================

    public void addNotification(String msg, int color) {
        notifications.add(new Models.Notification(msg, color));
        if (notifications.size() > 8) notifications.remove(0);
    }

    public void cleanNotifications() {
        notifications.removeIf(Models.Notification::isExpired);
    }

    // =====================================================================
    // UNLOCKING PLOTS
    // =====================================================================

    public long getPlotUnlockCost() {
        return 100L + (unlockedPlots - 12) * 150L;
    }

    public boolean unlockNextPlot() {
        if (unlockedPlots >= plots.length) return false;
        long cost = getPlotUnlockCost();
        if (money < cost) return false;
        money -= cost;
        unlockedPlots++;
        addNotification("Nueva parcela desbloqueada!", 0xFF00FF00);
        return true;
    }
}
