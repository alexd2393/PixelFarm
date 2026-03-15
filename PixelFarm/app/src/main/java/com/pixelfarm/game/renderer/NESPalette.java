package com.pixelfarm.game.renderer;

public class NESPalette {
    // === BASIC ===
    public static final int BLACK        = 0xFF000000;
    public static final int WHITE        = 0xFFFCFCFC;
    public static final int DARK_GRAY    = 0xFF747474;
    public static final int LIGHT_GRAY   = 0xFFBCBCBC;

    // === BROWNS / EARTH ===
    public static final int DARK_BROWN   = 0xFF503000;
    public static final int BROWN        = 0xFF7C5000;
    public static final int MID_BROWN    = 0xFF8B4513;
    public static final int LIGHT_BROWN  = 0xFFAC7C00;
    public static final int TAN          = 0xFFD4A574;
    public static final int SAND         = 0xFFE8C97A;

    // === GREENS / PLANTS ===
    public static final int DARK_GREEN   = 0xFF004400;
    public static final int GREEN        = 0xFF007800;
    public static final int MID_GREEN    = 0xFF00A800;
    public static final int LIGHT_GREEN  = 0xFF50C840;
    public static final int YELLOW_GREEN = 0xFF7C9400;

    // === BLUES / SKY / WATER ===
    public static final int NIGHT_SKY    = 0xFF0D0D2B;
    public static final int DARK_BLUE    = 0xFF000088;
    public static final int BLUE         = 0xFF0000E8;
    public static final int MID_BLUE     = 0xFF2038EC;
    public static final int LIGHT_BLUE   = 0xFF6888FC;
    public static final int SKY_BLUE     = 0xFF87CEEB;
    public static final int DAWN_SKY     = 0xFFFF8C42;
    public static final int DUSK_SKY     = 0xFFCC5533;

    // === REDS ===
    public static final int DARK_RED     = 0xFF8B0000;
    public static final int RED          = 0xFFD80000;
    public static final int BRIGHT_RED   = 0xFFFC3000;

    // === YELLOWS / ORANGES ===
    public static final int DARK_YELLOW  = 0xFFAC7C00;
    public static final int YELLOW       = 0xFFFFD700;
    public static final int ORANGE       = 0xFFFC7400;
    public static final int GOLD         = 0xFFFFAA00;

    // === PURPLES / PINKS ===
    public static final int DARK_PURPLE  = 0xFF440064;
    public static final int PURPLE       = 0xFF940084;
    public static final int LIGHT_PURPLE = 0xFFCC78CC;
    public static final int PINK         = 0xFFF878F8;
    public static final int DARK_PINK    = 0xFFCC7090;

    // === UI PANEL COLORS ===
    public static final int UI_BG        = 0xFF1A1A2E;
    public static final int UI_PANEL     = 0xFF16213E;
    public static final int UI_DARK      = 0xFF0F0F23;
    public static final int UI_BORDER    = 0xFF0F3460;
    public static final int UI_ACCENT    = 0xFFE94560;
    public static final int UI_BUTTON    = 0xFF2A4A8A;
    public static final int UI_BUTTON_H  = 0xFF3A6ACE;
    public static final int UI_TEXT      = 0xFFF0F0F0;
    public static final int UI_TEXT_DIM  = 0xFF909090;

    // === CROP COLORS (primary render color per crop) ===
    public static final int CROP_WHEAT       = YELLOW;
    public static final int CROP_CARROT      = ORANGE;
    public static final int CROP_STRAWBERRY  = BRIGHT_RED;
    public static final int CROP_CORN        = GOLD;
    public static final int CROP_TOMATO      = RED;
    public static final int CROP_MELON       = LIGHT_GREEN;
    public static final int CROP_PUMPKIN     = ORANGE;
    public static final int CROP_GRAPE       = PURPLE;
    public static final int CROP_APPLE       = RED;
    public static final int CROP_TURNIP      = LIGHT_PURPLE;
    public static final int CROP_COFFEE      = DARK_BROWN;
    public static final int CROP_CACAO       = BROWN;
    public static final int CROP_RICE        = LIGHT_GRAY;
    public static final int CROP_LAVENDER    = LIGHT_PURPLE;
    public static final int CROP_MUSHROOM    = TAN;

    // === ANIMAL COLORS ===
    public static final int ANIMAL_CHICKEN   = 0xFFF8F8D8;
    public static final int ANIMAL_COW       = 0xFFF0E8C8;
    public static final int ANIMAL_SHEEP     = 0xFFF4F4F4;
    public static final int ANIMAL_PIG       = 0xFFF4A8C8;
    public static final int ANIMAL_BEE       = YELLOW;
}
