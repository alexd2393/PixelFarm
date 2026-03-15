package com.pixelfarm.game.data;

import com.pixelfarm.game.renderer.NESPalette;

public class GameData {

    // =====================================================================
    // SEASONS
    // =====================================================================
    public enum Season {
        SPRING("Primavera", NESPalette.LIGHT_GREEN),
        SUMMER("Verano",    NESPalette.YELLOW),
        AUTUMN("Otoño",     NESPalette.ORANGE),
        WINTER("Invierno",  NESPalette.LIGHT_BLUE);

        public final String name;
        public final int color;
        Season(String name, int color) { this.name = name; this.color = color; }
    }

    // =====================================================================
    // CROP TYPE
    // =====================================================================
    public static class CropType {
        public final String id;
        public final String name;
        public final int seedCost;
        public final int growthHours;       // game hours to fully grow
        public final int basePrice;         // sell price per unit
        public final Season[] seasons;      // valid seasons (null = ALL)
        public final int color;             // NES render color
        public final int unlockRep;         // reputation needed to unlock
        public final int harvestCount;      // units per harvest

        public CropType(String id, String name, int seedCost, int growthHours,
                        int basePrice, int[] seasonIds, int color,
                        int unlockRep, int harvestCount) {
            this.id           = id;
            this.name         = name;
            this.seedCost     = seedCost;
            this.growthHours  = growthHours;
            this.basePrice    = basePrice;
            this.color        = color;
            this.unlockRep    = unlockRep;
            this.harvestCount = harvestCount;
            if (seasonIds == null) {
                this.seasons = Season.values();
            } else {
                this.seasons = new Season[seasonIds.length];
                for (int i = 0; i < seasonIds.length; i++)
                    this.seasons[i] = Season.values()[seasonIds[i]];
            }
        }

        public boolean isAvailableIn(Season s) {
            for (Season allowed : seasons) if (allowed == s) return true;
            return false;
        }

        // Season multiplier: crop grows 30% slower in off-season if planted
        public float getSeasonMultiplier(Season s) {
            return isAvailableIn(s) ? 1.0f : 0.7f;
        }
    }

    // Season index constants
    public static final int SP = 0, SU = 1, AU = 2, WI = 3;

    public static final CropType[] CROPS = {
        // id, name, cost, hours, price, seasons, color, rep, harvest
        new CropType("wheat",      "Trigo",      5,   1,  15, null,              NESPalette.CROP_WHEAT,      0,    3),
        new CropType("carrot",     "Zanahoria",  10,  2,  35, new int[]{SP},     NESPalette.CROP_CARROT,     0,    4),
        new CropType("strawberry", "Fresa",      20,  3,  80, new int[]{SP},     NESPalette.CROP_STRAWBERRY, 100,  5),
        new CropType("corn",       "Maíz",       15,  2,  55, new int[]{SU},     NESPalette.CROP_CORN,       0,    4),
        new CropType("tomato",     "Tomate",     25,  4,  110,new int[]{SU},     NESPalette.CROP_TOMATO,     200,  6),
        new CropType("melon",      "Melón",      40,  6,  200,new int[]{SU},     NESPalette.CROP_MELON,      500,  4),
        new CropType("pumpkin",    "Calabaza",   30,  5,  150,new int[]{AU},     NESPalette.CROP_PUMPKIN,    300,  5),
        new CropType("grape",      "Uva",        50,  8,  300,new int[]{AU},     NESPalette.CROP_GRAPE,      800,  8),
        new CropType("apple",      "Manzana",    80,  12, 500,new int[]{AU},     NESPalette.CROP_APPLE,      1500, 6),
        new CropType("turnip",     "Nabo",       8,   1,  25, new int[]{WI},     NESPalette.CROP_TURNIP,     0,    4),
        new CropType("coffee",     "Café",       100, 10, 600,null,              NESPalette.CROP_COFFEE,     2000, 5),
        new CropType("cacao",      "Cacao",      150, 14, 900,null,              NESPalette.CROP_CACAO,      3000, 4),
        new CropType("rice",       "Arroz",      35,  5,  180,new int[]{SU},     NESPalette.CROP_RICE,       1000, 6),
        new CropType("lavender",   "Lavanda",    60,  8,  350,new int[]{SP},     NESPalette.CROP_LAVENDER,   1500, 7),
        new CropType("mushroom",   "Champiñón",  45,  6,  260,new int[]{WI},     NESPalette.CROP_MUSHROOM,   1200, 5),
    };

    public static CropType getCropById(String id) {
        for (CropType c : CROPS) if (c.id.equals(id)) return c;
        return null;
    }

    // =====================================================================
    // ANIMAL TYPE
    // =====================================================================
    public static class AnimalType {
        public final String id;
        public final String name;
        public final int buyCost;
        public final int produceHours;   // hours between productions
        public final String produceItem;
        public final int produceSellPrice;
        public final int feedCostPerDay; // coins per day to feed
        public final int color;
        public final int unlockRep;

        public AnimalType(String id, String name, int buyCost, int produceHours,
                          String produceItem, int produceSellPrice,
                          int feedCostPerDay, int color, int unlockRep) {
            this.id              = id;
            this.name            = name;
            this.buyCost         = buyCost;
            this.produceHours    = produceHours;
            this.produceItem     = produceItem;
            this.produceSellPrice= produceSellPrice;
            this.feedCostPerDay  = feedCostPerDay;
            this.color           = color;
            this.unlockRep       = unlockRep;
        }
    }

    public static final AnimalType[] ANIMALS = {
        new AnimalType("chicken", "Gallina",  200, 2,  "egg",    30,  10, NESPalette.ANIMAL_CHICKEN, 0),
        new AnimalType("cow",     "Vaca",     800, 4,  "milk",   120, 40, NESPalette.ANIMAL_COW,     500),
        new AnimalType("sheep",   "Oveja",    600, 6,  "wool",   200, 30, NESPalette.ANIMAL_SHEEP,   500),
        new AnimalType("pig",     "Cerdo",    500, 8,  "truffle",350, 35, NESPalette.ANIMAL_PIG,     1000),
        new AnimalType("bee",     "Colmena",  300, 3,  "honey",  180, 5,  NESPalette.ANIMAL_BEE,     800),
    };

    public static AnimalType getAnimalById(String id) {
        for (AnimalType a : ANIMALS) if (a.id.equals(id)) return a;
        return null;
    }

    // =====================================================================
    // BUILDING TYPE
    // =====================================================================
    public static class BuildingType {
        public final String id;
        public final String name;
        public final String description;
        public final int cost;
        public final int unlockRep;
        public final int color;

        public BuildingType(String id, String name, String description,
                            int cost, int unlockRep, int color) {
            this.id          = id;
            this.name        = name;
            this.description = description;
            this.cost        = cost;
            this.unlockRep   = unlockRep;
            this.color       = color;
        }
    }

    public static final BuildingType[] BUILDINGS = {
        new BuildingType("barn",         "Granero",         "+20 cap inventario",   500,  0,    NESPalette.MID_BROWN),
        new BuildingType("well",         "Pozo de Agua",    "Cultivos crecen +20%", 300,  0,    NESPalette.MID_BLUE),
        new BuildingType("greenhouse",   "Invernadero",     "Cualquier cultivo\nen cualquier estación", 2000, 1000, NESPalette.LIGHT_GREEN),
        new BuildingType("mill",         "Molino",          "Procesa trigo\nen harina x3",  1500, 500,  NESPalette.TAN),
        new BuildingType("winery",       "Bodega",          "Procesa uva\nen vino x4",      3000, 2000, NESPalette.PURPLE),
        new BuildingType("bakery",       "Panadería",       "Harina -> Pan x4",     2500, 1500, NESPalette.SAND),
        new BuildingType("market_local", "Mercado Local",   "+15% precio venta",    5000, 3000, NESPalette.GOLD),
        new BuildingType("silo",         "Silo",            "+50 cap inventario\ny producción auto",  4000, 2500, NESPalette.LIGHT_GRAY),
        new BuildingType("stable",       "Establo Grande",  "+3 cap animales",      3500, 1500, NESPalette.MID_BROWN),
        new BuildingType("roaster",      "Tostador",        "Café -> Tostado x3",   4000, 3000, NESPalette.DARK_BROWN),
        new BuildingType("irrigation",   "Riego Avanzado",  "Auto-riega parcelas",  6000, 4000, NESPalette.MID_BLUE),
        new BuildingType("warehouse",    "Bodega Industrial","Duplica inventario\nmáximo",8000,5000,NESPalette.DARK_GRAY),
    };

    public static BuildingType getBuildingById(String id) {
        for (BuildingType b : BUILDINGS) if (b.id.equals(id)) return b;
        return null;
    }

    // =====================================================================
    // PROCESSED ITEMS
    // =====================================================================
    public static class ProcessedItem {
        public final String inputId;
        public final int inputQty;
        public final String buildingId;
        public final String outputId;
        public final String outputName;
        public final int outputQty;
        public final int sellPrice;
        public final int color;

        public ProcessedItem(String inputId, int inputQty, String buildingId,
                             String outputId, String outputName, int outputQty,
                             int sellPrice, int color) {
            this.inputId    = inputId;
            this.inputQty   = inputQty;
            this.buildingId = buildingId;
            this.outputId   = outputId;
            this.outputName = outputName;
            this.outputQty  = outputQty;
            this.sellPrice  = sellPrice;
            this.color      = color;
        }
    }

    public static final ProcessedItem[] PROCESSES = {
        new ProcessedItem("wheat", 3, "mill",       "flour",      "Harina",        2, 45,   NESPalette.SAND),
        new ProcessedItem("flour", 2, "bakery",     "bread",      "Pan",           3, 150,  NESPalette.LIGHT_BROWN),
        new ProcessedItem("grape", 4, "winery",     "wine",       "Vino",          2, 800,  NESPalette.PURPLE),
        new ProcessedItem("coffee",2, "roaster",    "roasted_coffee","Café Tostado",2, 1500, NESPalette.DARK_BROWN),
        new ProcessedItem("milk",  3, "bakery",     "cheese",     "Queso",         2, 400,  NESPalette.YELLOW),
        new ProcessedItem("cacao", 3, "bakery",     "chocolate",  "Chocolate",     3, 900,  NESPalette.DARK_BROWN),
    };

    // =====================================================================
    // WORKER TYPE
    // =====================================================================
    public static class WorkerType {
        public final String id;
        public final String name;
        public final String task;
        public final int dailySalary;
        public final int hireCost;
        public final int unlockRep;

        public WorkerType(String id, String name, String task,
                          int dailySalary, int hireCost, int unlockRep) {
            this.id          = id;
            this.name        = name;
            this.task        = task;
            this.dailySalary = dailySalary;
            this.hireCost    = hireCost;
            this.unlockRep   = unlockRep;
        }
    }

    public static final WorkerType[] WORKERS = {
        new WorkerType("farmer",    "Peón",         "Riega cultivos auto",       50,  200,  0),
        new WorkerType("harvester", "Cosechador",   "Cosecha automáticamente",   80,  350,  300),
        new WorkerType("seller",    "Vendedor",     "Vende al mejor precio",     100, 500,  800),
        new WorkerType("vet",       "Veterinario",  "Cuida animales auto",       120, 600,  1000),
        new WorkerType("trader",    "Comerciante",  "Gestiona contratos",        200, 1000, 2000),
    };

    // =====================================================================
    // RANDOM EVENTS
    // =====================================================================
    public static class EventData {
        public final String id;
        public final String title;
        public final String description;
        public final boolean isPositive;
        public final int durationDays;
        // Effect keys: "price_mult_all", "growth_mult", "theft_pct", "bonus_money"
        public final String effectKey;
        public final float effectValue;

        public EventData(String id, String title, String description,
                         boolean isPositive, int durationDays,
                         String effectKey, float effectValue) {
            this.id          = id;
            this.title       = title;
            this.description = description;
            this.isPositive  = isPositive;
            this.durationDays= durationDays;
            this.effectKey   = effectKey;
            this.effectValue = effectValue;
        }
    }

    public static final EventData[] EVENTS = {
        new EventData("rain",       "¡Lluvia Abundante!",    "Cultivos crecen +30%",         true,  3, "growth_mult",     1.30f),
        new EventData("festival",   "Festival del Pueblo",   "Precios +25% esta semana",     true,  7, "price_mult_all",  1.25f),
        new EventData("subsidy",    "Subsidio Gubernamental","Recibes $500 bonus",           true,  1, "bonus_money",     500f),
        new EventData("tourists",   "Temporada Turística",   "Ventas +40% esta semana",      true,  7, "price_mult_all",  1.40f),
        new EventData("richsoil",   "Suelo Fértil",          "Cosechas dobles esta semana",  true,  7, "harvest_mult",    2.0f),
        new EventData("harvest_prize","Premio a la Cosecha", "Tu granja gana un premio.\n$1000 bonus!", true, 1, "bonus_money", 1000f),
        new EventData("plague",     "¡Plaga de Insectos!",   "Cultivos crecen -50%",         false, 5, "growth_mult",     0.50f),
        new EventData("drought",    "Sequía Prolongada",     "Cultivos necesitan +2 riegos", false, 5, "water_need",      2.0f),
        new EventData("flood",      "Inundación",            "2 parcelas aleatorias dañadas",false, 1, "flood",           2.0f),
        new EventData("theft",      "¡Robo en el Granero!",  "Pierdes 20% del inventario",   false, 1, "theft_pct",       0.20f),
        new EventData("fired",      "Trabajador Renuncia",   "Un empleado se va",             false, 1, "fire_worker",     1.0f),
        new EventData("price_crash","Caída del Mercado",     "Precios -30% esta semana",     false, 7, "price_mult_all",  0.70f),
    };

    public static WorkerType getWorkerById(String id) {
        for (WorkerType w : WORKERS) if (w.id.equals(id)) return w;
        return null;
    }

    // =====================================================================
    // LOAN TYPES
    // =====================================================================
    public static class LoanType {
        public final String id;
        public final String name;
        public final int amount;
        public final float weeklyInterest;
        public final int weeklyPayment;

        public LoanType(String id, String name, int amount,
                        float weeklyInterest, int weeklyPayment) {
            this.id             = id;
            this.name           = name;
            this.amount         = amount;
            this.weeklyInterest = weeklyInterest;
            this.weeklyPayment  = weeklyPayment;
        }
    }

    public static final LoanType[] LOANS = {
        new LoanType("small",    "Préstamo Pequeño",    5000,  0.05f, 300),
        new LoanType("medium",   "Préstamo Mediano",    15000, 0.07f, 1200),
        new LoanType("large",    "Préstamo Grande",     50000, 0.10f, 5000),
    };

    // =====================================================================
    // REPUTATION LEVELS
    // =====================================================================
    public static class RepLevel {
        public final int minRep;
        public final String title;
        public final int color;
        public RepLevel(int minRep, String title, int color) {
            this.minRep = minRep; this.title = title; this.color = color;
        }
    }

    public static final RepLevel[] REP_LEVELS = {
        new RepLevel(0,     "Novato",         NESPalette.LIGHT_GRAY),
        new RepLevel(1000,  "Granjero",       NESPalette.LIGHT_GREEN),
        new RepLevel(5000,  "Experto",        NESPalette.YELLOW),
        new RepLevel(15000, "Terrateniente",  NESPalette.ORANGE),
        new RepLevel(40000, "Magnate",        NESPalette.GOLD),
    };

    public static RepLevel getRepLevel(int rep) {
        RepLevel current = REP_LEVELS[0];
        for (RepLevel rl : REP_LEVELS) if (rep >= rl.minRep) current = rl;
        return current;
    }
}
