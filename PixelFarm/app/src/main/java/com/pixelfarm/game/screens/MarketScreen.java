package com.pixelfarm.game.screens;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.pixelfarm.game.GameState;
import com.pixelfarm.game.Screen;
import com.pixelfarm.game.ScreenManager;
import com.pixelfarm.game.data.GameData;
import com.pixelfarm.game.renderer.NESPalette;
import com.pixelfarm.game.renderer.PixelRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarketScreen extends Screen {
    private final PixelRenderer r=new PixelRenderer();
    private int tab=0; // 0=vender, 1=contratos
    private int scrollY=0; private float lastTy=0;

    public MarketScreen(GameState s,ScreenManager m){super(s,m);}
    @Override public void update(long d){}

    @Override
    public void render(Canvas c,int w,int h){
        SW=w;SH=h;
        r.fillRect(c,0,72,w,h-72-78,NESPalette.UI_BG);
        r.drawHUD(c,state,w);
        r.drawNavBar(c,w,h,2);
        r.fillRect(c,0,72,w,44,NESPalette.UI_PANEL);
        r.drawTextCenter(c,"MERCADO",w/2f,72+30,NESPalette.WHITE,28f);
        r.drawButton(c,0,116,w/2f,36,tab==0?NESPalette.UI_ACCENT:NESPalette.UI_BUTTON,NESPalette.UI_BORDER,"Vender",tab==0?NESPalette.WHITE:NESPalette.UI_TEXT_DIM,20f);
        r.drawButton(c,w/2f,116,w/2f,36,tab==1?NESPalette.UI_ACCENT:NESPalette.UI_BUTTON,NESPalette.UI_BORDER,"Contratos",tab==1?NESPalette.WHITE:NESPalette.UI_TEXT_DIM,20f);

        if(tab==0) renderSellTab(c,w,h);
        else renderContractsTab(c,w,h);
        r.drawNotifications(c,state,w,h);
    }

    private void renderSellTab(Canvas c,int w,int h){
        if(state.inventory.isEmpty()){
            r.drawTextCenter(c,"Tu inventario esta vacio.",w/2f,h/2f,NESPalette.UI_TEXT_DIM,24f);
            r.drawTextCenter(c,"Ve a tu granja y cosecha!",w/2f,h/2f+36,NESPalette.UI_TEXT_DIM,20f);
            return;
        }
        // Market event indicator
        float mult=state.getEventEffect("price_mult_all",1.0f);
        if(mult!=1.0f){
            int ec=mult>1?0xFF00CC00:0xFFCC0000;
            String ms="Mercado: "+(mult>1?"+":"")+((int)((mult-1)*100))+"% precios";
            r.fillRect(c,0,152,w,28,0xFF1A2A1A);
            r.drawTextCenter(c,ms,w/2f,168,ec,20f);
        }

        int rowH=82, listY=180-scrollY;
        int idx=0;
        for(Map.Entry<String,Integer> entry:state.inventory.entrySet()){
            String itemId=entry.getKey(); int qty=entry.getValue();
            if(qty<=0) continue;
            float iy=listY+idx*rowH;
            if(iy+rowH>154&&iy<h-78){
                int bg=(idx%2==0)?NESPalette.UI_PANEL:NESPalette.UI_DARK;
                r.fillRect(c,0,iy,w,rowH-2,bg);
                // Item color square
                int col=itemColor(itemId);
                r.fillRect(c,8,iy+10,52,52,col);
                r.drawTextCenter(c,itemId.substring(0,Math.min(3,itemId.length())).toUpperCase(),34,iy+40,NESPalette.WHITE,18f);
                // Info
                String nm=prettifyId(itemId);
                r.drawText(c,nm,74,iy+26,NESPalette.WHITE,22f);
                r.drawText(c,"Stock: "+qty,74,iy+50,NESPalette.UI_TEXT_DIM,18f);
                long priceAll=state.getSellPrice(itemId,qty);
                long priceOne=state.getSellPrice(itemId,1);
                r.drawText(c,"$"+priceOne+" c/u",74,iy+68,NESPalette.GOLD,18f);
                // Market mult badge
                float m2=state.marketMult.getOrDefault(itemId,1.0f);
                int mc=m2>1.1f?NESPalette.LIGHT_GREEN:(m2<0.9f?NESPalette.RED:NESPalette.UI_TEXT_DIM);
                r.drawText(c,(m2>1f?"+":"")+((int)((m2-1)*100))+"%",w-140,iy+26,mc,18f);
                // Sell 1 btn
                r.drawButton(c,w-135,iy+36,60,30,NESPalette.UI_BUTTON,NESPalette.UI_BORDER,"x1",NESPalette.WHITE,18f);
                // Sell all btn
                r.drawButton(c,w-70,iy+36,62,30,NESPalette.GREEN,NESPalette.DARK_GREEN,"Todo",NESPalette.WHITE,18f);
            }
            idx++;
        }
    }

    private void renderContractsTab(Canvas c,int w,int h){
        int y=154;
        // Active contracts
        r.drawText(c,"CONTRATOS ACTIVOS:",10,y+22,NESPalette.YELLOW,22f);
        y+=28;
        if(state.contracts.isEmpty()){
            r.drawText(c,"  Ninguno aun.",10,y+22,NESPalette.UI_TEXT_DIM,20f);
            y+=34;
        }
        for(com.pixelfarm.game.data.Models.Contract co:state.contracts){
            if(co.completed||co.failed) continue;
            r.fillRect(c,6,y,w-12,70,NESPalette.UI_PANEL);
            r.fillRect(c,6,y,4,70,co.color);
            r.drawText(c,co.quantity+"x "+co.itemName,18,y+22,NESPalette.WHITE,22f);
            r.drawText(c,"Entregado: "+co.currentDelivered+"/"+co.quantity,18,y+44,NESPalette.LIGHT_GREEN,18f);
            r.drawProgressBar(c,150,y+48,w-260,10,co.getProgress(),NESPalette.UI_DARK,NESPalette.LIGHT_GREEN);
            r.drawText(c,"Dias: "+co.deadlineDays,w-130,y+22,co.deadlineDays<3?NESPalette.RED:NESPalette.UI_TEXT_DIM,18f);
            r.drawText(c,"$"+co.reward,w-130,y+44,NESPalette.GOLD,20f);
            y+=76;
        }
        // Available contracts
        r.drawText(c,"DISPONIBLES:",10,y+22,NESPalette.YELLOW,22f);
        y+=28;
        if(state.availableContracts.isEmpty()){
            r.drawText(c,"  Proximos disponibles mañana.",10,y+22,NESPalette.UI_TEXT_DIM,20f);
        }
        for(com.pixelfarm.game.data.Models.Contract co:state.availableContracts){
            r.fillRect(c,6,y,w-12,70,NESPalette.UI_DARK);
            r.fillRect(c,6,y,4,70,co.color);
            r.drawText(c,co.quantity+"x "+co.itemName,18,y+22,NESPalette.WHITE,22f);
            r.drawText(c,"Deadline: "+co.deadlineDays+" dias",18,y+44,NESPalette.UI_TEXT_DIM,18f);
            r.drawText(c,"Premio: $"+co.reward,w-200,y+44,NESPalette.GOLD,20f);
            r.drawButton(c,w-90,y+16,80,36,NESPalette.UI_BUTTON,NESPalette.UI_BORDER,"Aceptar",NESPalette.WHITE,18f);
            y+=76;
        }
    }

    private int itemColor(String id){
        GameData.CropType ct=GameData.getCropById(id);
        if(ct!=null) return ct.color;
        for(GameData.ProcessedItem pi:GameData.PROCESSES) if(pi.outputId.equals(id)) return pi.color;
        for(GameData.AnimalType at:GameData.ANIMALS) if(at.produceItem.equals(id)) return at.color;
        return NESPalette.UI_TEXT_DIM;
    }
    private String prettifyId(String id){
        String s=id.replace("_"," ");
        if(s.length()>0) s=Character.toUpperCase(s.charAt(0))+s.substring(1);
        return s;
    }

    @Override
    public boolean onTouch(MotionEvent e){
        float tx=e.getX(),ty=e.getY();
        if(e.getAction()==MotionEvent.ACTION_DOWN) lastTy=ty;
        if(e.getAction()==MotionEvent.ACTION_MOVE){scrollY-=(int)(ty-lastTy);scrollY=Math.max(0,scrollY);lastTy=ty;return true;}
        if(e.getAction()!=MotionEvent.ACTION_DOWN) return true;
        navBarTap(tx,ty);
        if(ty>=116&&ty<152){tab=tx<SW/2f?0:1;scrollY=0;return true;}
        if(tab==0) handleSellTap(tx,ty);
        else handleContractTap(tx,ty);
        return true;
    }

    private void handleSellTap(float tx,float ty){
        int rowH=82,listY=180-scrollY,idx=0;
        for(Map.Entry<String,Integer> entry:new ArrayList<>(state.inventory.entrySet())){
            String itemId=entry.getKey(); int qty=entry.getValue();
            if(qty<=0){idx++;continue;}
            float iy=listY+idx*rowH;
            if(ty>=iy&&ty<iy+rowH){
                if(tx>SW-70) state.sellItem(itemId,qty);
                else if(tx>SW-135) state.sellItem(itemId,1);
                return;
            }
            idx++;
        }
    }
    private void handleContractTap(float tx,float ty){
        int y=154+28;
        for(com.pixelfarm.game.data.Models.Contract co:state.availableContracts){
            y+=34;
            if(ty>=y&&ty<y+70&&tx>SW-90){state.acceptContract(co);state.addNotification("Contrato aceptado!",NESPalette.GOLD);return;}
            y+=76;
        }
    }
}
