package com.pixelfarm.game.screens;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.pixelfarm.game.GameState;
import com.pixelfarm.game.Screen;
import com.pixelfarm.game.ScreenManager;
import com.pixelfarm.game.renderer.NESPalette;
import com.pixelfarm.game.renderer.PixelRenderer;

public class TitleScreen extends Screen {
    private final PixelRenderer r = new PixelRenderer();
    private float anim = 0f;

    public TitleScreen(GameState s, ScreenManager m) { super(s, m); }

    @Override public void update(long delta) { anim += delta * 0.002f; }

    @Override
    public void render(Canvas c, int w, int h) {
        // Sky gradient (NES style: bands)
        r.fillRect(c, 0, 0, w, h, NESPalette.NIGHT_SKY);
        r.fillRect(c, 0, h*0.55f, w, h*0.45f, NESPalette.MID_BROWN);
        r.fillRect(c, 0, h*0.50f, w, 12, NESPalette.DARK_GREEN);
        r.fillRect(c, 0, h*0.52f, w, 8, NESPalette.GREEN);

        // Stars (static pixel pattern)
        int[] sx={40,90,150,220,300,60,180,260,340,120,280};
        int[] sy={30,50,20,60,35,80,15,70,25,45,55};
        for(int i=0;i<sx.length;i++){
            float blink = (float)Math.abs(Math.sin(anim*1.5f + i));
            r.fillRect(c, sx[i], sy[i], 4, 4, blink > 0.5f ? 0xFFFFFFFF : 0xFF888888);
        }

        // Moon
        r.fillRect(c, w-80, 30, 40, 40, NESPalette.YELLOW);
        r.fillRect(c, w-68, 30, 30, 30, NESPalette.NIGHT_SKY); // crescent

        // Trees (pixel art)
        drawTree(c, 30,  (int)(h*0.42f), 50);
        drawTree(c, w-80,(int)(h*0.42f), 50);
        drawTree(c, w/2-90, (int)(h*0.45f), 40);
        drawTree(c, w/2+50, (int)(h*0.45f), 40);

        // Farm house outline (pixel art)
        drawHouse(c, w/2-40, (int)(h*0.30f), 80);

        // Title plate
        r.fillRect(c, w/2-180, h/2-70, 360, 90, NESPalette.UI_DARK);
        r.fillRect(c, w/2-178, h/2-68, 356, 86, NESPalette.UI_BORDER);
        r.fillRect(c, w/2-176, h/2-66, 352, 82, NESPalette.UI_PANEL);

        // Title text with shadow
        r.drawTextCenter(c, "PIXEL", w/2f-2, h/2f-30+2, 0xFF000000, 52f);
        r.drawTextCenter(c, "PIXEL", w/2f,   h/2f-30,   NESPalette.GOLD, 52f);
        r.drawTextCenter(c, "FARM",  w/2f-2, h/2f+22+2, 0xFF000000, 52f);
        r.drawTextCenter(c, "FARM",  w/2f,   h/2f+22,   NESPalette.LIGHT_GREEN, 52f);

        // Blink "TOCA PARA JUGAR"
        if((int)(anim*2) % 2 == 0)
            r.drawTextCenter(c, ">> TOCA PARA EMPEZAR <<", w/2f, h*0.75f, NESPalette.WHITE, 26f);

        // Version
        r.drawTextCenter(c, "v1.0  NES EDITION", w/2f, h*0.85f, NESPalette.UI_TEXT_DIM, 20f);

        // Decorative crops bottom
        String[] crops={"wheat","tomato","grape","corn","strawberry"};
        int cw = w/crops.length;
        for(int i=0;i<crops.length;i++){
            r.drawCropCell(c, crops[i], 1.0f, i*cw+cw/2f-20, h*0.88f, 40);
        }
    }

    private void drawTree(Canvas c, int x, int y, int size){
        r.fillRect(c, x+size/2-4, y+size/2, 8, size/2, NESPalette.DARK_BROWN);
        r.fillRect(c, x, y, size, size/2+10, NESPalette.DARK_GREEN);
        r.fillRect(c, x+8, y-10, size-16, size/3, NESPalette.GREEN);
    }

    private void drawHouse(Canvas c, int x, int y, int w){
        // Roof
        int[] rx={x,x+w/2,x+w};
        r.fillRect(c, x+5, y, w-10, 25, NESPalette.DARK_RED);
        r.fillRect(c, x, y+15, w, 10, NESPalette.RED);
        // Walls
        r.fillRect(c, x, y+22, w, 40, NESPalette.TAN);
        // Door
        r.fillRect(c, x+w/2-8, y+38, 16, 24, NESPalette.DARK_BROWN);
        // Window
        r.fillRect(c, x+8, y+28, 18, 14, NESPalette.LIGHT_BLUE);
        r.fillRect(c, x+w-26, y+28, 18, 14, NESPalette.LIGHT_BLUE);
    }

    @Override
    public boolean onTouch(MotionEvent e) {
        if(e.getAction() == MotionEvent.ACTION_DOWN)
            manager.goTo(ScreenManager.ScreenId.FARM);
        return true;
    }
}
