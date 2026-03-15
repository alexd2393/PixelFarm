package com.pixelfarm.game.data;

public class Models {

    // =====================================================================
    // PLOT - a single farm tile
    // =====================================================================
    public static class Plot {
        public enum State { EMPTY, PLANTED, WATERED, GROWING, READY }

        public State state      = State.EMPTY;
        public String cropId    = null;     // which crop is planted
        public float growthPct  = 0f;       // 0.0 to 1.0
        public long plantedAt   = 0;        // game time in ms when planted
        public long readyAt     = 0;        // game time in ms when ready
        public boolean watered  = false;
        public int waterCount   = 0;        // times watered this growth cycle
        public boolean damaged  = false;    // event damage

        public void reset() {
            state = State.EMPTY; cropId = null; growthPct = 0f;
            plantedAt = 0; readyAt = 0; watered = false;
            waterCount = 0; damaged = false;
        }

        public boolean isEmpty()  { return state == State.EMPTY; }
        public boolean isReady()  { return state == State.READY; }
        public boolean isGrowing(){ return state == State.PLANTED ||
                                           state == State.WATERED ||
                                           state == State.GROWING; }
    }

    // =====================================================================
    // ANIMAL INSTANCE
    // =====================================================================
    public static class Animal {
        public String typeId;
        public String name;
        public float happiness;   // 0.0 to 1.0
        public float hunger;      // 0.0 to 1.0 (1 = hungry)
        public long lastProduceGameTime;
        public int totalProduced;
        public boolean readyToCollect;

        public Animal(String typeId, String name) {
            this.typeId              = typeId;
            this.name                = name;
            this.happiness           = 1.0f;
            this.hunger              = 0.0f;
            this.lastProduceGameTime = 0;
            this.totalProduced       = 0;
            this.readyToCollect      = false;
        }

        public float getProductionMultiplier() {
            return happiness * (hunger < 0.8f ? 1.0f : 0.5f);
        }
    }

    // =====================================================================
    // WORKER INSTANCE
    // =====================================================================
    public static class Worker {
        public String typeId;
        public String name;
        public float efficiency;  // 0.0 to 1.0
        public int daysWorked;
        public boolean active;
        public String currentTask; // what they're doing right now

        public Worker(String typeId, String name) {
            this.typeId      = typeId;
            this.name        = name;
            this.efficiency  = 0.8f;
            this.daysWorked  = 0;
            this.active      = true;
            this.currentTask = "Esperando...";
        }
    }

    // =====================================================================
    // CONTRACT
    // =====================================================================
    public static class Contract {
        public String id;
        public String itemId;
        public String itemName;
        public int quantity;
        public int currentDelivered;
        public long reward;
        public int deadlineDays;
        public boolean completed;
        public boolean failed;
        public int color;

        public Contract(String id, String itemId, String itemName,
                        int quantity, long reward, int deadlineDays, int color) {
            this.id               = id;
            this.itemId           = itemId;
            this.itemName         = itemName;
            this.quantity         = quantity;
            this.currentDelivered = 0;
            this.reward           = reward;
            this.deadlineDays     = deadlineDays;
            this.completed        = false;
            this.failed           = false;
            this.color            = color;
        }

        public float getProgress() {
            return (float) currentDelivered / quantity;
        }

        public boolean canDeliver(int available) {
            return available > 0 && !completed && !failed;
        }
    }

    // =====================================================================
    // ACTIVE LOAN
    // =====================================================================
    public static class ActiveLoan {
        public String typeId;
        public long amountRemaining;
        public int weeksRemaining;
        public float weeklyInterest;
        public int weeklyPayment;

        public ActiveLoan(String typeId, long amount, int weeks,
                          float weeklyInterest, int weeklyPayment) {
            this.typeId           = typeId;
            this.amountRemaining  = amount;
            this.weeksRemaining   = weeks;
            this.weeklyInterest   = weeklyInterest;
            this.weeklyPayment    = weeklyPayment;
        }
    }

    // =====================================================================
    // ACTIVE EVENT
    // =====================================================================
    public static class ActiveEvent {
        public String eventId;
        public String title;
        public String description;
        public boolean isPositive;
        public int daysRemaining;
        public String effectKey;
        public float effectValue;

        public ActiveEvent(GameData.EventData def, int dayNow) {
            this.eventId      = def.id;
            this.title        = def.title;
            this.description  = def.description;
            this.isPositive   = def.isPositive;
            this.daysRemaining= def.durationDays;
            this.effectKey    = def.effectKey;
            this.effectValue  = def.effectValue;
        }
    }

    // =====================================================================
    // NOTIFICATION (toast-like messages)
    // =====================================================================
    public static class Notification {
        public String message;
        public int color;
        public long createdAt;  // real system ms
        public float alpha;

        public Notification(String message, int color) {
            this.message   = message;
            this.color     = color;
            this.createdAt = System.currentTimeMillis();
            this.alpha     = 1.0f;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt > 3000;
        }

        public float getAlpha() {
            long age = System.currentTimeMillis() - createdAt;
            if (age < 2000) return 1.0f;
            return 1.0f - (float)(age - 2000) / 1000f;
        }
    }
}
