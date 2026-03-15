package com.pixelfarm.game.screens;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.pixelfarm.game.GameState;
import com.pixelfarm.game.Screen;
import com.pixelfarm.game.ScreenManager;
import com.pixelfarm.game.data.GameData;
import com.pixelfarm.game.renderer.NESPalette;
import com.pixelfarm.game.renderer.PixelRenderer;

public class BuildingsScreen extends Screen {
    private final PixelRenderer r=new PixelRenderer();
    private int scrollY=0; private float lastTy=0;

    public BuildingsScreen(GameState s,ScreenManager m){super(s,m);}
    @Override public void update(long d){}

    @Override
    public void render(Canvas c,int w,int h){
        SW=w;SH=h;
        r.fillRect(c,0,72,w,h-72-78,NESPalette.UI_BG);
        r.drawHUD(c,state,w);
        r.drawNavBar(c,w,h,4);
        r.fillRect(c,0,72,w,44,NESPalette.UI_PANEL);
        r.drawTextCenter(c,"CONSTRUCCIONES",w/2f,72+30,NESPalette.WHITE,26f);

        int rowH=88, listY=118-scrollY;
        for(int i=0;i<GameData.BUILDINGS.length;i++){
            GameData.BuildingType bt=GameData.BUILDINGS[i];
            float iy=listY+i*rowH;
            if(iy+rowH<118||iy>h-78) continue;
            boolean built=state.buildings.contains(bt.id);
            boolean unlocked=state.reputation>=bt.unlockRep;
            boolean canAfford=state.money>=bt.cost;
            int bg=built?0xFF0A2A0A:(i%2==0?NESPalette.UI_PANEL:NESPalette.UI_DARK);
            r.fillRect(c,0,iy,w,rowH-3,bg);
            // Color block
            r.fillRect(c,8,iy+10,56,56,bt.color);
            if(built){ r.drawTextCenter(c,"OK",36,iy+42,NESPalette.WHITE,18f); }
            r.drawText(c,bt.name,76,iy+26,built?NESPalette.LIGHT_GREEN:NESPalette.WHITE,24f);
            // description multi-line
            String[] lines=bt.description.split("\n");
            for(int l=0;l<lines.length;l++) r.drawText(c,lines[l],76,iy+46+l*18,NESPalette.UI_TEXT_DIM,16f);
            if(!built){
                r.drawText(c,"$"+bt.cost,w-160,iy+26,canAfford?NESPalette.GOLD:NESPalette.RED,22f);
                if(!unlocked) r.drawButton(c,w-105,iy+40,95,30,0xFF222222,0xFF333333,"Rep "+bt.unlockRep,0xFF666666,15f);
                else r.drawButton(c,w-105,iy+40,95,30,canAfford?NESPalette.UI_BUTTON:0xFF333333,NESPalette.UI_BORDER,canAfford?"Construir":"$$$",canAfford?NESPalette.WHITE:0xFF666666,17f);
            } else {
                r.drawText(c,"Construido",w-130,iy+40,NESPalette.LIGHT_GREEN,18f);
            }
        }
        r.drawNotifications(c,state,w,h);
    }

    @Override
    public boolean onTouch(MotionEvent e){
        float tx=e.getX(),ty=e.getY();
        if(e.getAction()==MotionEvent.ACTION_DOWN) lastTy=ty;
        if(e.getAction()==MotionEvent.ACTION_MOVE){scrollY-=(int)(ty-lastTy);scrollY=Math.max(0,scrollY);lastTy=ty;return true;}
        if(e.getAction()!=MotionEvent.ACTION_DOWN) return true;
        navBarTap(tx,ty);
        int rowH=88,listY=118-scrollY;
        for(int i=0;i<GameData.BUILDINGS.length;i++){
            GameData.BuildingType bt=GameData.BUILDINGS[i];
            float iy=listY+i*rowH;
            if(ty>=iy&&ty<iy+rowH&&tx>SW-105){
                if(!state.buildings.contains(bt.id)&&state.reputation>=bt.unlockRep&&state.money>=bt.cost){
                    state.money-=bt.cost;
                    state.buildings.add(bt.id);
                    state.reputation+=50;
                    // Inventory capacity bonus
                    if(bt.id.equals("barn")) state.inventoryCapacity+=20;
                    if(bt.id.equals("silo")) state.inventoryCapacity+=50;
                    if(bt.id.equals("warehouse")) state.inventoryCapacity*=2;
                    if(bt.id.equals("stable")) state.animalCapacity+=3;
                    state.addNotification(bt.name+" construido!",NESPalette.GOLD);
                } else if(state.buildings.contains(bt.id)) state.addNotification("Ya construido",NESPalette.ORANGE);
                else state.addNotification("No puedes construir esto",NESPalette.RED);
                return true;
            }
        }
        return true;
    }
}
