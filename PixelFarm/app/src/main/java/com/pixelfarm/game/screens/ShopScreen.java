package com.pixelfarm.game.screens;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.pixelfarm.game.GameState;
import com.pixelfarm.game.Screen;
import com.pixelfarm.game.ScreenManager;
import com.pixelfarm.game.data.GameData;
import com.pixelfarm.game.renderer.NESPalette;
import com.pixelfarm.game.renderer.PixelRenderer;

public class ShopScreen extends Screen {
    private final PixelRenderer r=new PixelRenderer();
    private int tab=0; // 0=seeds, 1=animals
    private int scrollY=0;
    private float lastTy=0;

    public ShopScreen(GameState s,ScreenManager m){super(s,m);}
    @Override public void update(long d){}

    @Override
    public void render(Canvas c,int w,int h){
        SW=w;SH=h;
        r.fillRect(c,0,72,w,h-72-78,NESPalette.UI_BG);
        r.drawHUD(c,state,w);
        r.drawNavBar(c,w,h,1);

        // Title
        r.fillRect(c,0,72,w,44,NESPalette.UI_PANEL);
        r.drawTextCenter(c,"TIENDA",w/2f,72+30,NESPalette.WHITE,28f);

        // Tabs
        float tabW=w/2f;
        r.drawButton(c,0,116,tabW,36,tab==0?NESPalette.UI_ACCENT:NESPalette.UI_BUTTON,NESPalette.UI_BORDER,"Semillas",tab==0?NESPalette.WHITE:NESPalette.UI_TEXT_DIM,20f);
        r.drawButton(c,tabW,116,tabW,36,tab==1?NESPalette.UI_ACCENT:NESPalette.UI_BUTTON,NESPalette.UI_BORDER,"Animales",tab==1?NESPalette.WHITE:NESPalette.UI_TEXT_DIM,20f);

        int listY=154-scrollY;
        int rowH=90;
        if(tab==0){
            GameData.Season season=state.getSeason();
            for(int i=0;i<GameData.CROPS.length;i++){
                GameData.CropType ct=GameData.CROPS[i];
                float iy=listY+i*rowH;
                if(iy+rowH<154||iy>h-78) continue;
                boolean unlocked=state.reputation>=ct.unlockRep;
                boolean inSeason=ct.isAvailableIn(season)||state.buildings.contains("greenhouse");
                boolean canAfford=state.money>=ct.seedCost;
                int bg=(i%2==0)?NESPalette.UI_PANEL:NESPalette.UI_DARK;
                r.fillRect(c,0,iy,w,rowH-2,bg);
                // Sprite
                r.drawCropCell(c,ct.id,1f,8,iy+8,68);
                // Info
                r.drawText(c,ct.name,90,iy+28,unlocked?NESPalette.WHITE:0xFF666666,24f);
                r.drawText(c,"Semilla: $"+ct.seedCost,90,iy+52,canAfford?NESPalette.GOLD:0xFF884444,20f);
                r.drawText(c,"Venta: $"+ct.basePrice+" x"+ct.harvestCount,90,iy+70,NESPalette.LIGHT_GREEN,18f);
                // Season badge
                r.fillRect(c,w-130,iy+10,80,24,inSeason?0xFF004400:0xFF440000);
                r.drawTextCenter(c,inSeason?season.name.substring(0,3):"Off",w-90f,iy+26,inSeason?NESPalette.LIGHT_GREEN:NESPalette.RED,18f);
                // Growth time
                r.drawText(c,ct.growthHours+"h crecim.",w-130,iy+52,NESPalette.UI_TEXT_DIM,18f);
                // Buy btn
                if(!unlocked){
                    r.drawButton(c,w-82,iy+32,72,30,0xFF222222,0xFF333333,"Rep"+ct.unlockRep,0xFF666666,16f);
                } else {
                    r.drawButton(c,w-82,iy+32,72,30,
                            canAfford?NESPalette.UI_BUTTON:0xFF333333,NESPalette.UI_BORDER,
                            canAfford?"Comprar":"$$$",canAfford?NESPalette.WHITE:0xFF666666,17f);
                }
            }
        } else {
            for(int i=0;i<GameData.ANIMALS.length;i++){
                GameData.AnimalType at=GameData.ANIMALS[i];
                float iy=listY+i*rowH;
                if(iy+rowH<154||iy>h-78) continue;
                boolean unlocked=state.reputation>=at.unlockRep;
                boolean canAfford=state.money>=at.buyCost;
                boolean full=state.animals.size()>=state.animalCapacity;
                int bg=(i%2==0)?NESPalette.UI_PANEL:NESPalette.UI_DARK;
                r.fillRect(c,0,iy,w,rowH-2,bg);
                r.drawAnimal(c,at.id,8,iy+10,64);
                r.drawText(c,at.name,90,iy+28,unlocked?NESPalette.WHITE:0xFF666666,24f);
                r.drawText(c,"Costo: $"+at.buyCost,90,iy+52,canAfford?NESPalette.GOLD:0xFF884444,20f);
                r.drawText(c,at.produceItem+" c/"+at.produceHours+"h ($"+at.produceSellPrice+")",90,iy+70,NESPalette.LIGHT_GREEN,18f);
                r.drawText(c,"Comida/dia: $"+at.feedCostPerDay,w-160,iy+70,0xFFFF8844,17f);
                if(!unlocked) r.drawButton(c,w-82,iy+32,72,30,0xFF222222,0xFF333333,"Rep"+at.unlockRep,0xFF666666,16f);
                else if(full)  r.drawButton(c,w-82,iy+32,72,30,0xFF222222,0xFF333333,"Lleno",0xFF666666,16f);
                else r.drawButton(c,w-82,iy+32,72,30,canAfford?NESPalette.UI_BUTTON:0xFF333333,NESPalette.UI_BORDER,canAfford?"Comprar":"$$$",canAfford?NESPalette.WHITE:0xFF666666,17f);
            }
        }
        r.drawNotifications(c,state,w,h);
    }

    @Override
    public boolean onTouch(MotionEvent e){
        float tx=e.getX(),ty=e.getY();
        if(e.getAction()==MotionEvent.ACTION_DOWN) lastTy=ty;
        if(e.getAction()==MotionEvent.ACTION_MOVE){ scrollY-=(int)(ty-lastTy); scrollY=Math.max(0,scrollY); lastTy=ty; return true; }
        if(e.getAction()!=MotionEvent.ACTION_DOWN) return true;
        navBarTap(tx,ty);
        // Tabs
        if(ty>=116&&ty<152){ tab=tx<SW/2f?0:1; scrollY=0; return true; }
        // List items
        int rowH=90; int listY=154-scrollY;
        if(tab==0){
            for(int i=0;i<GameData.CROPS.length;i++){
                float iy=listY+i*rowH;
                if(ty>=iy&&ty<iy+rowH&&tx>SW-82){
                    GameData.CropType ct=GameData.CROPS[i];
                    if(state.reputation>=ct.unlockRep&&state.money>=ct.seedCost){
                        state.money-=ct.seedCost;
                        state.addToInventory("seed_"+ct.id,3);
                        state.addNotification("Comprado: 3x semillas "+ct.name,NESPalette.GOLD);
                    } else state.addNotification("No puedes comprar esto",NESPalette.RED);
                    return true;
                }
            }
        } else {
            for(int i=0;i<GameData.ANIMALS.length;i++){
                float iy=listY+i*rowH;
                if(ty>=iy&&ty<iy+rowH&&tx>SW-82){
                    GameData.AnimalType at=GameData.ANIMALS[i];
                    if(state.reputation>=at.unlockRep&&state.money>=at.buyCost&&state.animals.size()<state.animalCapacity){
                        state.money-=at.buyCost;
                        state.animals.add(new com.pixelfarm.game.data.Models.Animal(at.id,at.name+" "+(state.animals.size()+1)));
                        state.addNotification("Comprado: "+at.name,NESPalette.GOLD);
                    } else state.addNotification("No puedes comprar esto",NESPalette.RED);
                    return true;
                }
            }
        }
        return true;
    }
}
