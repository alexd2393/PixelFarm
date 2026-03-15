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

public class FarmScreen extends Screen {
    private final PixelRenderer r = new PixelRenderer();
    private int cellSize=0, gridLeft=0, gridTop=0;
    private int selectedPlot=-1;
    private int selectedCropIdx=0;
    private boolean showCropSelector=false;
    private float anim=0f;

    public FarmScreen(GameState s, ScreenManager m){super(s,m);}
    @Override public void update(long d){anim+=d*0.001f;}

    @Override
    public void render(Canvas c, int w, int h){
        SW=w; SH=h;
        int navH=78, hudH=72;
        cellSize=(w-12)/GameState.GRID_COLS;
        gridLeft=6;
        gridTop=hudH+8;

        r.fillRect(c,0,hudH,w,h-hudH-navH,NESPalette.DARK_GREEN);
        for(int row=0;row<=GameState.GRID_ROWS;row++)
            r.fillRect(c,0,gridTop+row*cellSize-1,w,2,NESPalette.GREEN);

        for(int i=0;i<GameState.GRID_COLS*GameState.GRID_ROWS;i++){
            int col=i%GameState.GRID_COLS, row=i/GameState.GRID_COLS;
            float px=gridLeft+col*cellSize, py=gridTop+row*cellSize;
            if(i>=state.unlockedPlots){
                r.fillRect(c,px+1,py+1,cellSize-2,cellSize-2,0xFF111111);
                r.drawTextCenter(c,"?",px+cellSize/2f,py+cellSize/2f,0xFF444444,cellSize*0.4f);
            } else {
                Models.Plot plot=state.plots[i];
                r.drawCropCell(c,plot.cropId,plot.growthPct,px+1,py+1,cellSize-2);
                if(plot.watered&&plot.isGrowing())
                    r.fillRect(c,px+cellSize-9,py+2,6,6,NESPalette.LIGHT_BLUE);
                if(i==selectedPlot){
                    r.fillRect(c,px,py,cellSize,2,NESPalette.WHITE);
                    r.fillRect(c,px,py+cellSize-2,cellSize,2,NESPalette.WHITE);
                    r.fillRect(c,px,py,2,cellSize,NESPalette.WHITE);
                    r.fillRect(c,px+cellSize-2,py,2,cellSize,NESPalette.WHITE);
                }
            }
            r.fillRect(c,px,py,cellSize,1,0xFF2A2A2A);
            r.fillRect(c,px,py,1,cellSize,0xFF2A2A2A);
        }

        // Unlock button
        if(state.unlockedPlots<GameState.GRID_COLS*GameState.GRID_ROWS){
            float btnY=gridTop+GameState.GRID_ROWS*cellSize+6;
            r.drawButton(c,w/2f-100,btnY,200,38,NESPalette.UI_BUTTON,NESPalette.UI_BORDER,
                    "+Parcela $"+state.getPlotUnlockCost(),NESPalette.YELLOW,19f);
        }

        if(selectedPlot>=0&&selectedPlot<state.unlockedPlots) drawPanel(c,w,h);

        r.drawHUD(c,state,w);
        r.drawNavBar(c,w,h,0);
        r.drawNotifications(c,state,w,h);
    }

    private void drawPanel(Canvas c,int w,int h){
        int pH=showCropSelector?210:190, py=h-78-pH;
        r.fillRect(c,0,py,w,pH,NESPalette.UI_DARK);
        r.fillRect(c,0,py,w,3,NESPalette.UI_ACCENT);
        r.drawButton(c,w-70,py+8,60,32,NESPalette.UI_BUTTON,NESPalette.UI_BORDER,"X",NESPalette.WHITE,22f);

        if(showCropSelector){
            r.drawText(c,"Elige cultivo:",10,py+28,NESPalette.WHITE,22f);
            int cw=85; float cropY=py+38;
            for(int i=0;i<GameData.CROPS.length;i++){
                GameData.CropType ct=GameData.CROPS[i];
                float cx=10+i*cw;
                if(cx+cw>w-10) break;
                boolean ok=state.money>=ct.seedCost&&state.reputation>=ct.unlockRep;
                int bg=(i==selectedCropIdx)?NESPalette.UI_ACCENT:NESPalette.UI_PANEL;
                r.fillRect(c,cx,cropY,cw-3,130,bg);
                r.drawCropCell(c,ct.id,1f,cx+5,cropY+4,54);
                String nm=ct.name.length()>7?ct.name.substring(0,7):ct.name;
                r.drawTextCenter(c,nm,cx+cw/2f-2,cropY+70,NESPalette.WHITE,17f);
                r.drawTextCenter(c,"$"+ct.seedCost,cx+cw/2f-2,cropY+90,ok?NESPalette.GOLD:NESPalette.RED,17f);
                boolean inS=ct.isAvailableIn(state.getSeason())||state.buildings.contains("greenhouse");
                r.drawTextCenter(c,inS?"OK":"Off",cx+cw/2f-2,cropY+108,inS?NESPalette.LIGHT_GREEN:NESPalette.ORANGE,15f);
            }
            GameData.CropType sel=GameData.CROPS[selectedCropIdx];
            boolean canP=state.money>=sel.seedCost&&state.reputation>=sel.unlockRep;
            r.drawButton(c,10,py+pH-52,w-80,42,
                    canP?NESPalette.GREEN:0xFF333333,canP?NESPalette.DARK_GREEN:0xFF222222,
                    canP?"PLANTAR "+sel.name+" ($"+sel.seedCost+")":"Fondos o rep insuficiente",
                    canP?NESPalette.WHITE:0xFF666666,21f);
            return;
        }

        Models.Plot plot=state.plots[selectedPlot];
        r.drawText(c,"Parcela #"+(selectedPlot+1),10,py+28,NESPalette.WHITE,24f);
        if(plot.isEmpty()){
            r.drawButton(c,10,py+44,w-80,52,NESPalette.UI_BUTTON,NESPalette.UI_BORDER,
                    "PLANTAR CULTIVO",NESPalette.YELLOW,26f);
        } else if(plot.isReady()){
            GameData.CropType ct=GameData.getCropById(plot.cropId);
            r.drawText(c,(ct!=null?ct.name:"?")+" - LISTA!",10,py+52,NESPalette.LIGHT_GREEN,24f);
            r.drawButton(c,10,py+64,w-80,52,NESPalette.GREEN,NESPalette.DARK_GREEN,">> COSECHAR <<",NESPalette.WHITE,28f);
        } else if(plot.isGrowing()){
            GameData.CropType ct=GameData.getCropById(plot.cropId);
            r.drawText(c,(ct!=null?ct.name:"?")+" "+((int)(plot.growthPct*100))+"%",10,py+52,NESPalette.YELLOW,22f);
            r.drawProgressBar(c,10,py+62,w-80,14,plot.growthPct,NESPalette.UI_PANEL,NESPalette.LIGHT_GREEN);
            float bw2=(w-80)/2f-6;
            r.drawButton(c,10,py+84,bw2,48,
                    plot.watered?NESPalette.UI_PANEL:NESPalette.MID_BLUE,NESPalette.DARK_BLUE,
                    plot.watered?"Regado ✓":"REGAR",NESPalette.WHITE,22f);
            r.drawButton(c,10+bw2+12,py+84,bw2,48,0xFF553300,NESPalette.DARK_BROWN,"ARRANCAR",NESPalette.RED,22f);
        }
        r.drawText(c,"Parcela #"+(selectedPlot+1),10,py+28,NESPalette.WHITE,24f);
    }

    @Override
    public boolean onTouch(MotionEvent e){
        if(e.getAction()!=MotionEvent.ACTION_DOWN) return true;
        float tx=e.getX(),ty=e.getY();
        if(SH==0||SW==0) return true;
        navBarTap(tx,ty);
        if(ty<SH-78){
            if(selectedPlot>=0){
                int pH=showCropSelector?210:190, py=SH-78-pH;
                if(ty>py){
                    handlePanelTap(tx,ty,py,pH);
                    return true;
                }
                selectedPlot=-1; showCropSelector=false; return true;
            }
            if(cellSize>0&&ty>gridTop){
                int col=(int)((tx-gridLeft)/cellSize), row=(int)((ty-gridTop)/cellSize);
                if(col>=0&&col<GameState.GRID_COLS&&row>=0&&row<GameState.GRID_ROWS){
                    int idx=row*GameState.GRID_COLS+col;
                    if(idx<state.unlockedPlots){
                        selectedPlot=(selectedPlot==idx)?-1:idx;
                        showCropSelector=false;
                    }
                } else selectedPlot=-1;
            }
            // Unlock button
            float btnY=gridTop+GameState.GRID_ROWS*cellSize+6;
            if(ty>btnY&&ty<btnY+38&&Math.abs(tx-SW/2f)<100) state.unlockNextPlot();
        }
        return true;
    }

    private void handlePanelTap(float tx,float ty,int py,int pH){
        // Close X
        if(tx>SW-70&&ty<py+50){selectedPlot=-1;showCropSelector=false;return;}
        if(showCropSelector){
            int cw=85; float cropY=py+38;
            for(int i=0;i<GameData.CROPS.length;i++){
                float cx=10+i*cw;
                if(cx+cw>SW-10) break;
                if(tx>=cx&&tx<cx+cw-3&&ty>=cropY&&ty<cropY+130){ selectedCropIdx=i; return; }
            }
            // Plant button
            if(ty>py+pH-52){
                GameData.CropType sel=GameData.CROPS[selectedCropIdx];
                if(state.money>=sel.seedCost&&state.reputation>=sel.unlockRep){
                    if(state.plantCrop(selectedPlot,sel.id)){
                        state.addNotification("Plantado: "+sel.name,NESPalette.LIGHT_GREEN);
                        selectedPlot=-1; showCropSelector=false;
                    }
                } else state.addNotification("Fondos insuficientes",NESPalette.RED);
            }
            return;
        }
        Models.Plot plot=state.plots[selectedPlot];
        if(plot.isEmpty()){ showCropSelector=true; return; }
        if(plot.isReady()){ state.harvestPlot(selectedPlot); selectedPlot=-1; return; }
        if(plot.isGrowing()){
            float bw2=(SW-80)/2f-6;
            if(tx<10+bw2) state.waterPlot(selectedPlot);
            else { state.plots[selectedPlot].reset(); state.addNotification("Cultivo arrancado",NESPalette.ORANGE); selectedPlot=-1; }
        }
    }
}
