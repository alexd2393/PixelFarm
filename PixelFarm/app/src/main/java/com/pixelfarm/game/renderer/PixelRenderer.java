package com.pixelfarm.game.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.pixelfarm.game.GameState;
import com.pixelfarm.game.data.GameData;
import com.pixelfarm.game.data.Models;

public class PixelRenderer {
    private final Paint paint = new Paint();
    private final RectF  rect  = new RectF();
    private static final int T = 0;

    public PixelRenderer() { paint.setAntiAlias(false); }

    public void fillRect(Canvas c, float x, float y, float w, float h, int color) {
        paint.setColor(color); rect.set(x,y,x+w,y+h); c.drawRect(rect,paint);
    }
    public void drawBorder(Canvas c,float x,float y,float w,float h,int fill,int border,float b){
        fillRect(c,x,y,w,h,border); fillRect(c,x+b,y+b,w-b*2,h-b*2,fill);
    }
    public void drawText(Canvas c,String text,float x,float y,int color,float size){
        paint.setColor(color); paint.setTextSize(size); paint.setTypeface(Typeface.MONOSPACE);
        c.drawText(text,x,y,paint);
    }
    public void drawTextCenter(Canvas c,String t,float cx,float cy,int color,float sz){
        paint.setColor(color); paint.setTextSize(sz); paint.setTypeface(Typeface.MONOSPACE);
        float tw=paint.measureText(t); c.drawText(t,cx-tw/2f,cy+sz/3f,paint);
    }
    public float measureText(String t,float sz){ paint.setTextSize(sz); return paint.measureText(t); }
    public void drawButton(Canvas c,float x,float y,float w,float h,int bg,int border,String lbl,int tc,float ts){
        fillRect(c,x+3,y+3,w,h,0x55000000); fillRect(c,x,y,w,h,border);
        fillRect(c,x+2,y+2,w-4,h-4,bg); drawTextCenter(c,lbl,x+w/2f,y+h/2f,tc,ts);
    }
    public void drawProgressBar(Canvas c,float x,float y,float w,float h,float progress,int bg,int fill){
        fillRect(c,x,y,w,h,bg); if(progress>0) fillRect(c,x,y,w*Math.min(1f,progress),h,fill);
    }
    public void drawSprite(Canvas c,int[][] sprite,float x,float y,int ps){
        for(int row=0;row<sprite.length;row++) for(int col=0;col<sprite[row].length;col++){
            int color=sprite[row][col]; if(color==0) continue;
            paint.setColor(color); rect.set(x+col*ps,y+row*ps,x+col*ps+ps,y+row*ps+ps); c.drawRect(rect,paint);
        }
    }
    public void drawCropCell(Canvas c,String cropId,float progress,float x,float y,int cellSize){
        int ps=Math.max(1,cellSize/16);
        fillRect(c,x,y,cellSize,cellSize,NESPalette.MID_BROWN);
        paint.setColor(NESPalette.DARK_BROWN);
        for(int i=2;i<14;i+=4) for(int j=2;j<14;j+=4){
            rect.set(x+i*ps,y+j*ps,x+i*ps+ps,y+j*ps+ps); c.drawRect(rect,paint);
        }
        if(progress<=0f) return;
        int cc=cropColor(cropId);
        if(progress<0.35f){
            fillRect(c,x+7*ps,y+11*ps,2*ps,3*ps,NESPalette.LIGHT_GREEN);
            fillRect(c,x+5*ps,y+12*ps,2*ps,2*ps,NESPalette.LIGHT_GREEN);
            fillRect(c,x+9*ps,y+12*ps,2*ps,2*ps,NESPalette.LIGHT_GREEN);
        } else if(progress<0.70f){
            fillRect(c,x+7*ps,y+5*ps,2*ps,9*ps,NESPalette.GREEN);
            fillRect(c,x+4*ps,y+8*ps,3*ps,3*ps,NESPalette.LIGHT_GREEN);
            fillRect(c,x+9*ps,y+9*ps,3*ps,3*ps,NESPalette.LIGHT_GREEN);
            fillRect(c,x+6*ps,y+3*ps,4*ps,4*ps,cc);
        } else {
            fillRect(c,x+7*ps,y+4*ps,2*ps,10*ps,NESPalette.GREEN);
            fillRect(c,x+3*ps,y+7*ps,4*ps,3*ps,NESPalette.LIGHT_GREEN);
            fillRect(c,x+9*ps,y+8*ps,4*ps,3*ps,NESPalette.LIGHT_GREEN);
            fillRect(c,x+4*ps,y+1*ps,8*ps,6*ps,cc);
            fillRect(c,x+4*ps,y+1*ps,3*ps,2*ps,blend(cc,0xFFFFFFFF,0.5f));
        }
        if(progress>=1.0f){ paint.setColor(0x44FFD700); rect.set(x,y,x+cellSize,y+cellSize); c.drawRect(rect,paint); }
    }
    public void drawAnimal(Canvas c,String id,float x,float y,int size){
        int ps=Math.max(1,size/8);
        int W,B,O,R,P,D,Y,G;
        switch(id){
            case "chicken":
                W=NESPalette.ANIMAL_CHICKEN; O=NESPalette.ORANGE; R=NESPalette.RED;
                drawSprite(c,new int[][]{{T,T,T,R,T,T,T,T},{T,T,W,W,W,T,T,T},{T,W,W,W,W,W,T,T},{T,W,W,W,W,W,T,T},{T,T,W,W,W,T,T,T},{T,T,O,T,O,T,T,T},{T,O,O,T,O,O,T,T},{T,T,T,T,T,T,T,T}},x,y,ps); break;
            case "cow":
                W=NESPalette.ANIMAL_COW; B=NESPalette.MID_BROWN;
                drawSprite(c,new int[][]{{T,B,T,T,T,T,B,T},{T,B,W,W,W,W,B,T},{T,W,W,W,W,W,W,T},{T,W,B,W,W,B,W,T},{T,W,W,W,W,W,W,T},{T,B,W,W,W,W,B,T},{T,B,T,T,T,T,B,T},{T,T,T,T,T,T,T,T}},x,y,ps); break;
            case "sheep":
                W=NESPalette.ANIMAL_SHEEP; G=NESPalette.DARK_GRAY;
                drawSprite(c,new int[][]{{T,T,W,W,W,W,T,T},{T,W,W,W,W,W,W,T},{W,W,W,W,W,W,W,W},{W,W,G,W,W,G,W,W},{T,W,W,W,W,W,W,T},{T,G,W,W,W,W,G,T},{T,G,G,T,T,G,G,T},{T,T,T,T,T,T,T,T}},x,y,ps); break;
            case "pig":
                P=NESPalette.ANIMAL_PIG; D=0xFFCC7090;
                drawSprite(c,new int[][]{{T,T,P,P,P,P,T,T},{T,P,P,P,P,P,P,T},{P,P,D,P,P,D,P,P},{P,P,P,P,P,P,P,P},{T,P,D,D,D,D,P,T},{T,P,P,P,P,P,P,T},{T,D,T,T,T,T,D,T},{T,D,T,T,T,T,D,T}},x,y,ps); break;
            default: // bee or unknown
                Y=NESPalette.YELLOW; B=NESPalette.BLACK; int WW=0xAAE8F0FF;
                drawSprite(c,new int[][]{{T,T,WW,T,T,WW,T,T},{T,WW,WW,T,T,WW,WW,T},{T,T,Y,Y,Y,Y,T,T},{T,T,B,Y,Y,B,T,T},{T,T,Y,Y,Y,Y,T,T},{T,T,B,Y,Y,B,T,T},{T,T,Y,T,T,Y,T,T},{T,T,T,T,T,T,T,T}},x,y,ps);
        }
    }
    public void drawHUD(Canvas c,GameState state,int w){
        fillRect(c,0,0,w,72,NESPalette.UI_DARK);
        fillRect(c,0,70,w,2,NESPalette.UI_BORDER);
        drawText(c,"$"+state.money,12,48,NESPalette.GOLD,36f);
        GameData.Season season=state.getSeason();
        String t=season.name+" D"+state.getDayOfSeason()+" A"+state.getYear()+" "+String.format("%02d:00",state.getHour());
        float tw=measureText(t,21f);
        drawText(c,t,w/2f-tw/2f,46,season.color,21f);
        GameData.RepLevel rl=GameData.getRepLevel(state.reputation);
        float rtw=measureText(rl.title,21f);
        drawText(c,rl.title,w-rtw-10,46,rl.color,21f);
        if(state.debt>0) drawText(c,"DEUDA:$"+state.debt,12,68,0xFFFF5555,17f);
        if(state.activeEvent!=null){
            int ec=state.activeEvent.isPositive?0xFF00CC00:0xFFCC0000;
            String et=(state.activeEvent.isPositive?"+ ":"! ")+state.activeEvent.title;
            if(et.length()>28) et=et.substring(0,28);
            drawText(c,et,w-measureText(et,17f)-10,68,ec,17f);
        }
    }
    public void drawNavBar(Canvas c,int w,int h,int activeIdx){
        String[] labels={"Granja","Tienda","Mercado","Invent.","Mas"};
        int barH=78;
        fillRect(c,0,h-barH,w,barH,NESPalette.UI_DARK);
        fillRect(c,0,h-barH,w,2,NESPalette.UI_BORDER);
        float bw=w/(float)labels.length;
        for(int i=0;i<labels.length;i++){
            int bg=(i==activeIdx)?NESPalette.UI_ACCENT:NESPalette.UI_BUTTON;
            int tc=(i==activeIdx)?NESPalette.WHITE:NESPalette.UI_TEXT_DIM;
            drawButton(c,i*bw+2,h-barH+4,bw-4,barH-8,bg,NESPalette.UI_BORDER,labels[i],tc,19f);
        }
    }
    public void drawNotifications(Canvas c,GameState state,int w,int h){
        float y=h-105;
        for(int i=state.notifications.size()-1;i>=0;i--){
            Models.Notification n=state.notifications.get(i);
            float alpha=n.getAlpha(); if(alpha<=0) continue;
            paint.setAlpha((int)(alpha*220)); paint.setTextSize(30f); paint.setTypeface(Typeface.MONOSPACE);
            float tw=paint.measureText(n.message);
            paint.setColor(0x88000000); c.drawText(n.message,w/2f-tw/2f+2,y+2,paint);
            paint.setColor(n.color); c.drawText(n.message,w/2f-tw/2f,y,paint);
            paint.setAlpha(255); y-=38;
        }
    }
    private int cropColor(String id){
        if(id==null) return NESPalette.LIGHT_GREEN;
        switch(id){
            case "wheat": return NESPalette.CROP_WHEAT; case "carrot": return NESPalette.CROP_CARROT;
            case "strawberry": return NESPalette.CROP_STRAWBERRY; case "corn": return NESPalette.CROP_CORN;
            case "tomato": return NESPalette.CROP_TOMATO; case "melon": return NESPalette.CROP_MELON;
            case "pumpkin": return NESPalette.CROP_PUMPKIN; case "grape": return NESPalette.CROP_GRAPE;
            case "apple": return NESPalette.CROP_APPLE; case "turnip": return NESPalette.CROP_TURNIP;
            case "coffee": return NESPalette.CROP_COFFEE; case "cacao": return NESPalette.CROP_CACAO;
            case "rice": return NESPalette.CROP_RICE; case "lavender": return NESPalette.CROP_LAVENDER;
            case "mushroom": return NESPalette.CROP_MUSHROOM; default: return NESPalette.LIGHT_GREEN;
        }
    }
    public int blend(int c1,int c2,float t){
        int r=(int)(((c1>>16)&0xFF)*(1-t)+((c2>>16)&0xFF)*t);
        int g=(int)(((c1>>8)&0xFF)*(1-t)+((c2>>8)&0xFF)*t);
        int b=(int)(((c1)&0xFF)*(1-t)+((c2)&0xFF)*t);
        return 0xFF000000|(r<<16)|(g<<8)|b;
    }
}
