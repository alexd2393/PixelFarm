package com.pixelfarm.game.screens;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.pixelfarm.game.GameState;
import com.pixelfarm.game.Screen;
import com.pixelfarm.game.ScreenManager;
import com.pixelfarm.game.data.GameData;
import com.pixelfarm.game.renderer.NESPalette;
import com.pixelfarm.game.renderer.PixelRenderer;

import java.util.Map;

public class InventoryScreen extends Screen {
    private final PixelRenderer r=new PixelRenderer();
    private int scrollY=0; private float lastTy=0;

    public InventoryScreen(GameState s,ScreenManager m){super(s,m);}
    @Override public void update(long d){}

    @Override
    public void render(Canvas c,int w,int h){
        SW=w;SH=h;
        r.fillRect(c,0,72,w,h-72-78,NESPalette.UI_BG);
        r.drawHUD(c,state,w);
        r.drawNavBar(c,w,h,3);

        // Header
        r.fillRect(c,0,72,w,44,NESPalette.UI_PANEL);
        r.drawTextCenter(c,"INVENTARIO",w/2f,72+30,NESPalette.WHITE,28f);

        // Capacity bar
        int total=state.getTotalInventory();
        r.fillRect(c,0,116,w,32,NESPalette.UI_DARK);
        r.drawProgressBar(c,10,122,w-110,18,(float)total/state.inventoryCapacity,
                NESPalette.UI_PANEL,total>state.inventoryCapacity*0.8f?NESPalette.ORANGE:NESPalette.GREEN);
        r.drawText(c,total+"/"+state.inventoryCapacity+" slots",w-96,135,NESPalette.UI_TEXT_DIM,18f);

        // Processing shortcuts
        r.fillRect(c,0,148,w,30,0xFF0A1A0A);
        r.drawText(c,"Procesamiento:",10,166,NESPalette.YELLOW,18f);

        int py=178, rowH=70, idx=0;
        int listY=py-scrollY;

        // All inventory items
        for(Map.Entry<String,Integer> entry:state.inventory.entrySet()){
            String id=entry.getKey(); int qty=entry.getValue();
            if(qty<=0) continue;
            float iy=listY+idx*rowH;
            if(iy+rowH>py&&iy<h-78){
                int bg=(idx%2==0)?NESPalette.UI_PANEL:NESPalette.UI_DARK;
                r.fillRect(c,0,iy,w,rowH-2,bg);
                // Color block
                int col=getItemColor(id);
                r.fillRect(c,8,iy+8,48,48,col);
                String abbr=id.replace("_","").substring(0,Math.min(3,id.replace("_","").length())).toUpperCase();
                r.drawTextCenter(c,abbr,32,iy+36,NESPalette.WHITE,16f);
                // Name + qty
                r.drawText(c,prettify(id),70,iy+26,NESPalette.WHITE,22f);
                r.drawText(c,"x"+qty,70,iy+50,NESPalette.LIGHT_GREEN,20f);
                // Sell price
                r.drawText(c,"~$"+state.getSellPrice(id,1)+"/u",w/2f,iy+26,NESPalette.GOLD,20f);
                // Check if processable
                for(GameData.ProcessedItem pi:GameData.PROCESSES){
                    if(pi.inputId.equals(id)&&state.buildings.contains(pi.buildingId)){
                        r.drawButton(c,w-130,iy+12,120,40,
                                qty>=pi.inputQty?NESPalette.UI_BUTTON:0xFF333333,NESPalette.UI_BORDER,
                                qty>=pi.inputQty?"Procesar":"Necesita "+pi.inputQty,
                                qty>=pi.inputQty?NESPalette.WHITE:0xFF666666,16f);
                    }
                }
            }
            idx++;
        }

        if(state.inventory.isEmpty())
            r.drawTextCenter(c,"Inventario vacio. Cosecha tus cultivos!",w/2f,h/2f,NESPalette.UI_TEXT_DIM,22f);

        // Stats panel at very bottom above nav
        r.fillRect(c,0,h-78-50,w,50,NESPalette.UI_DARK);
        r.drawText(c,"Cosechas totales: "+state.totalHarvests,10,h-78-28,NESPalette.UI_TEXT_DIM,18f);
        r.drawText(c,"Ganado total: $"+state.totalEarned,w/2f,h-78-28,NESPalette.GOLD,18f);

        r.drawNotifications(c,state,w,h);
    }

    private int getItemColor(String id){
        GameData.CropType ct=GameData.getCropById(id);
        if(ct!=null) return ct.color;
        for(GameData.ProcessedItem pi:GameData.PROCESSES) if(pi.outputId.equals(id)) return pi.color;
        for(GameData.AnimalType at:GameData.ANIMALS) if(at.produceItem.equals(id)) return at.color;
        return NESPalette.UI_TEXT_DIM;
    }
    private String prettify(String id){
        String s=id.replace("_"," ");
        return s.length()>0?Character.toUpperCase(s.charAt(0))+s.substring(1):s;
    }

    @Override
    public boolean onTouch(MotionEvent e){
        float tx=e.getX(),ty=e.getY();
        if(e.getAction()==MotionEvent.ACTION_DOWN) lastTy=ty;
        if(e.getAction()==MotionEvent.ACTION_MOVE){scrollY-=(int)(ty-lastTy);scrollY=Math.max(0,scrollY);lastTy=ty;return true;}
        if(e.getAction()!=MotionEvent.ACTION_DOWN) return true;
        navBarTap(tx,ty);
        // Process button taps
        int py=178,rowH=70,idx=0;
        int listY=py-scrollY;
        for(Map.Entry<String,Integer> entry:new java.util.LinkedHashMap<>(state.inventory).entrySet()){
            String id=entry.getKey(); int qty=entry.getValue();
            float iy=listY+idx*rowH;
            if(ty>=iy&&ty<iy+rowH&&tx>SW-130){
                for(GameData.ProcessedItem pi:GameData.PROCESSES){
                    if(pi.inputId.equals(id)&&state.buildings.contains(pi.buildingId)&&qty>=pi.inputQty){
                        state.processItem(pi); return true;
                    }
                }
            }
            idx++;
        }
        return true;
    }
}
