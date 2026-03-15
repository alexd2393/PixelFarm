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

public class WorkersScreen extends Screen {
    private final PixelRenderer r=new PixelRenderer();
    private int scrollY=0; private float lastTy=0;

    public WorkersScreen(GameState s,ScreenManager m){super(s,m);}
    @Override public void update(long d){}

    @Override
    public void render(Canvas c,int w,int h){
        SW=w;SH=h;
        r.fillRect(c,0,72,w,h-72-78,NESPalette.UI_BG);
        r.drawHUD(c,state,w);
        r.drawNavBar(c,w,h,4);
        r.fillRect(c,0,72,w,44,NESPalette.UI_PANEL);
        r.drawTextCenter(c,"TRABAJADORES ("+state.workers.size()+"/"+state.workerCapacity+")",w/2f,72+30,NESPalette.WHITE,24f);

        int y=118-scrollY;
        // Current workers
        r.drawText(c,"Empleados actuales:",10,y+22,NESPalette.YELLOW,22f); y+=30;
        if(state.workers.isEmpty()){ r.drawText(c,"  Ninguno.",10,y+20,NESPalette.UI_TEXT_DIM,20f); y+=30; }
        for(int i=0;i<state.workers.size();i++){
            Models.Worker w2=state.workers.get(i);
            GameData.WorkerType wt=GameData.getWorkerById(w2.typeId);
            r.fillRect(c,6,y,w-12,66,NESPalette.UI_PANEL);
            r.fillRect(c,6,y,4,66,w2.active?NESPalette.GREEN:NESPalette.RED);
            r.drawText(c,w2.name,18,y+22,NESPalette.WHITE,22f);
            r.drawText(c,w2.currentTask,18,y+44,NESPalette.UI_TEXT_DIM,17f);
            if(wt!=null) r.drawText(c,"$"+wt.dailySalary+"/dia",w-130,y+22,NESPalette.GOLD,20f);
            r.drawButton(c,w-82,y+34,72,24,0xFF442222,NESPalette.DARK_BROWN,"Despedir",NESPalette.RED,15f);
            y+=72;
        }
        // Hire
        r.drawText(c,"Contratar:",10,y+22,NESPalette.YELLOW,22f); y+=30;
        for(int i=0;i<GameData.WORKERS.length;i++){
            GameData.WorkerType wt=GameData.WORKERS[i];
            boolean unlocked=state.reputation>=wt.unlockRep;
            boolean canAfford=state.money>=wt.hireCost;
            boolean full=state.workers.size()>=state.workerCapacity;
            r.fillRect(c,6,y,w-12,82,NESPalette.UI_DARK);
            r.fillRect(c,6,y,4,82,unlocked?NESPalette.MID_BLUE:0xFF333366);
            r.drawText(c,wt.name,18,y+22,unlocked?NESPalette.WHITE:0xFF666666,24f);
            r.drawText(c,wt.task,18,y+44,NESPalette.UI_TEXT_DIM,17f);
            r.drawText(c,"Contratar: $"+wt.hireCost,18,y+62,canAfford?NESPalette.GOLD:NESPalette.RED,17f);
            r.drawText(c,"Salario: $"+wt.dailySalary+"/dia",w/2f,y+62,NESPalette.UI_TEXT_DIM,17f);
            if(!unlocked) r.drawButton(c,w-110,y+24,100,34,0xFF222222,0xFF333333,"Rep "+wt.unlockRep,0xFF666666,15f);
            else if(full)  r.drawButton(c,w-110,y+24,100,34,0xFF222222,0xFF333333,"Lleno",0xFF666666,15f);
            else r.drawButton(c,w-110,y+24,100,34,canAfford?NESPalette.UI_BUTTON:0xFF333333,NESPalette.UI_BORDER,canAfford?"Contratar":"$$$",canAfford?NESPalette.WHITE:0xFF666666,15f);
            y+=88;
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
        int y=118-scrollY+30;
        // Fire workers
        for(int i=0;i<state.workers.size();i++){
            if(ty>=y&&ty<y+72&&tx>SW-82){ state.workers.remove(i); state.addNotification("Trabajador despedido",NESPalette.ORANGE); return true; }
            y+=72;
        }
        if(state.workers.isEmpty()) y+=30;
        y+=30;
        // Hire workers
        for(GameData.WorkerType wt:GameData.WORKERS){
            if(ty>=y&&ty<y+88&&tx>SW-110){
                if(state.reputation>=wt.unlockRep&&state.money>=wt.hireCost&&state.workers.size()<state.workerCapacity){
                    state.money-=wt.hireCost;
                    state.workers.add(new Models.Worker(wt.id,wt.name+" "+(state.workers.size()+1)));
                    state.workerCapacity=Math.max(state.workerCapacity,state.workers.size()+1);
                    state.addNotification(wt.name+" contratado!",NESPalette.GOLD);
                } else state.addNotification("No puedes contratar esto",NESPalette.RED);
                return true;
            }
            y+=88;
        }
        return true;
    }
}
