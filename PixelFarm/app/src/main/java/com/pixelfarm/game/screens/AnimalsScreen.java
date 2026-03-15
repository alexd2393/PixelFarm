package com.pixelfarm.game.screens;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.pixelfarm.game.GameState;
import com.pixelfarm.game.Screen;
import com.pixelfarm.game.ScreenManager;
import com.pixelfarm.game.data.GameData;
import com.pixelfarm.game.data.Models;
import com.pixelfarm.game.renderer.NESPalette;
import com.pixelfarm.game.renderer.PixelRenderer;

public class AnimalsScreen extends Screen {
    private final PixelRenderer r=new PixelRenderer();
    private int scrollY=0; private float lastTy=0;

    public AnimalsScreen(GameState s,ScreenManager m){super(s,m);}
    @Override public void update(long d){}

    @Override
    public void render(Canvas c,int w,int h){
        SW=w;SH=h;
        r.fillRect(c,0,72,w,h-72-78,NESPalette.UI_BG);
        r.drawHUD(c,state,w);
        r.drawNavBar(c,w,h,4);
        r.fillRect(c,0,72,w,44,NESPalette.UI_PANEL);
        r.drawTextCenter(c,"ANIMALES ("+state.animals.size()+"/"+state.animalCapacity+")",w/2f,72+30,NESPalette.WHITE,26f);

        int rowH=110, listY=118-scrollY;
        if(state.animals.isEmpty()){
            r.drawTextCenter(c,"Sin animales.",w/2f,h/2f-20,NESPalette.UI_TEXT_DIM,24f);
            r.drawTextCenter(c,"Comprelos en la Tienda.",w/2f,h/2f+16,NESPalette.UI_TEXT_DIM,20f);
        }
        for(int i=0;i<state.animals.size();i++){
            Models.Animal a=state.animals.get(i);
            GameData.AnimalType at=GameData.getAnimalById(a.typeId);
            if(at==null) continue;
            float iy=listY+i*rowH;
            if(iy+rowH<118||iy>h-78) continue;
            int bg=(i%2==0)?NESPalette.UI_PANEL:NESPalette.UI_DARK;
            r.fillRect(c,0,iy,w,rowH-4,bg);
            r.drawAnimal(c,a.typeId,8,iy+10,80);
            r.drawText(c,a.name,100,iy+28,NESPalette.WHITE,24f);
            r.drawText(c,at.name,100,iy+50,NESPalette.UI_TEXT_DIM,18f);
            // Happiness bar
            r.drawText(c,"Felicidad:",100,iy+68,NESPalette.UI_TEXT_DIM,16f);
            r.drawProgressBar(c,190,iy+72,140,10,a.happiness,NESPalette.UI_DARK,happinessColor(a.happiness));
            // Hunger bar
            r.drawText(c,"Hambre:",100,iy+84,NESPalette.UI_TEXT_DIM,16f);
            r.drawProgressBar(c,190,iy+88,140,10,a.hunger,NESPalette.UI_DARK,a.hunger>0.7f?NESPalette.RED:NESPalette.ORANGE);
            // Ready badge
            if(a.readyToCollect){
                r.fillRect(c,w-140,iy+8,130,26,0xFF004400);
                r.drawTextCenter(c,at.produceItem+" LISTO!",w-75f,iy+24,NESPalette.LIGHT_GREEN,18f);
            }
            // Buttons
            r.drawButton(c,w-140,iy+40,60,34,NESPalette.MID_BLUE,NESPalette.DARK_BLUE,"Alimentar",NESPalette.WHITE,14f);
            r.drawButton(c,w-74,iy+40,64,34,
                    a.readyToCollect?NESPalette.GREEN:0xFF333333,
                    a.readyToCollect?NESPalette.DARK_GREEN:0xFF222222,
                    "Recoger",a.readyToCollect?NESPalette.WHITE:0xFF666666,14f);
        }
        r.drawNotifications(c,state,w,h);
    }

    private int happinessColor(float h){
        if(h>0.7f) return NESPalette.LIGHT_GREEN;
        if(h>0.4f) return NESPalette.YELLOW;
        return NESPalette.RED;
    }

    @Override
    public boolean onTouch(MotionEvent e){
        float tx=e.getX(),ty=e.getY();
        if(e.getAction()==MotionEvent.ACTION_DOWN) lastTy=ty;
        if(e.getAction()==MotionEvent.ACTION_MOVE){scrollY-=(int)(ty-lastTy);scrollY=Math.max(0,scrollY);lastTy=ty;return true;}
        if(e.getAction()!=MotionEvent.ACTION_DOWN) return true;
        navBarTap(tx,ty);
        int rowH=110,listY=118-scrollY;
        for(int i=0;i<state.animals.size();i++){
            float iy=listY+i*rowH;
            if(ty>=iy&&ty<iy+rowH){
                if(tx>SW-140&&tx<SW-80) state.feedAnimal(i);
                else if(tx>SW-74){
                    int got=state.collectAnimal(i);
                    if(got>0){
                        GameData.AnimalType at=GameData.getAnimalById(state.animals.get(i).typeId);
                        state.addNotification("Recogido: "+got+"x "+(at!=null?at.produceItem:"item"),NESPalette.LIGHT_GREEN);
                    }
                }
                return true;
            }
        }
        return true;
    }
}
