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

public class BankScreen extends Screen {
    private final PixelRenderer r=new PixelRenderer();

    public BankScreen(GameState s,ScreenManager m){super(s,m);}
    @Override public void update(long d){}

    @Override
    public void render(Canvas c,int w,int h){
        SW=w;SH=h;
        r.fillRect(c,0,72,w,h-72-78,NESPalette.UI_BG);
        r.drawHUD(c,state,w);
        r.drawNavBar(c,w,h,4);
        r.fillRect(c,0,72,w,44,NESPalette.UI_PANEL);
        r.drawTextCenter(c,"BANCO",w/2f,72+30,NESPalette.WHITE,28f);

        int y=124;
        // Initial debt
        if(state.debt>0){
            r.fillRect(c,6,y,w-12,70,0xFF2A0000);
            r.fillRect(c,6,y,4,70,NESPalette.RED);
            r.drawText(c,"Deuda inicial: $"+state.debt,18,y+24,NESPalette.RED,22f);
            r.drawText(c,"Cuota semanal: $200 (5% interes)",18,y+46,NESPalette.UI_TEXT_DIM,18f);
            r.drawButton(c,w-130,y+16,120,36,state.money>=200?NESPalette.UI_BUTTON:0xFF333333,NESPalette.UI_BORDER,"Pagar $200",state.money>=200?NESPalette.WHITE:0xFF666666,17f);
            y+=78;
        } else {
            r.fillRect(c,6,y,w-12,44,0xFF002200);
            r.drawTextCenter(c,"Deuda inicial PAGADA! ✓",w/2f,y+28,NESPalette.LIGHT_GREEN,22f);
            y+=52;
        }

        // Active loans
        r.drawText(c,"Prestamos activos:",10,y+22,NESPalette.YELLOW,22f); y+=28;
        if(state.loans.isEmpty()){
            r.drawText(c,"  Ninguno.",10,y+20,NESPalette.UI_TEXT_DIM,20f); y+=32;
        }
        for(Models.ActiveLoan loan:state.loans){
            if(loan.amountRemaining<=0) continue;
            r.fillRect(c,6,y,w-12,66,NESPalette.UI_PANEL);
            r.drawText(c,"Restante: $"+loan.amountRemaining,18,y+22,NESPalette.RED,22f);
            r.drawText(c,"Cuota: $"+loan.weeklyPayment+"/semana  ("+loan.weeksRemaining+" semanas)",18,y+44,NESPalette.UI_TEXT_DIM,17f);
            r.drawButton(c,w-130,y+14,120,36,state.money>=loan.weeklyPayment?NESPalette.UI_BUTTON:0xFF333333,NESPalette.UI_BORDER,"Pagar cuota",state.money>=loan.weeklyPayment?NESPalette.WHITE:0xFF666666,16f);
            y+=74;
        }

        // Available loans
        r.drawText(c,"Solicitar prestamo:",10,y+22,NESPalette.YELLOW,22f); y+=28;
        for(GameData.LoanType lt:GameData.LOANS){
            r.fillRect(c,6,y,w-12,66,NESPalette.UI_DARK);
            r.drawText(c,lt.name+": $"+lt.amount,18,y+22,NESPalette.GOLD,22f);
            r.drawText(c,"$"+lt.weeklyPayment+"/semana  "+(int)(lt.weeklyInterest*100)+"% interes",18,y+44,NESPalette.UI_TEXT_DIM,17f);
            r.drawButton(c,w-130,y+14,120,36,NESPalette.UI_BUTTON,NESPalette.UI_BORDER,"Solicitar",NESPalette.WHITE,17f);
            y+=74;
        }
        r.drawNotifications(c,state,w,h);
    }

    @Override
    public boolean onTouch(MotionEvent e){
        if(e.getAction()!=MotionEvent.ACTION_DOWN) return true;
        float tx=e.getX(),ty=e.getY();
        navBarTap(tx,ty);
        int y=124;
        // Debt payment
        if(state.debt>0&&ty>=y&&ty<y+78&&tx>SW-130){
            if(state.money>=200){ state.money-=200; state.debt=Math.max(0,state.debt-200); state.addNotification("Deuda reducida a $"+state.debt,NESPalette.GOLD); }
            else state.addNotification("Sin fondos",NESPalette.RED);
            return true;
        }
        y+=state.debt>0?78:52;
        y+=28;
        // Loan payments
        if(!state.loans.isEmpty()){
            for(Models.ActiveLoan loan:state.loans){
                if(ty>=y&&ty<y+74&&tx>SW-130){
                    if(state.money>=loan.weeklyPayment){ state.money-=loan.weeklyPayment; loan.amountRemaining-=loan.weeklyPayment; loan.weeksRemaining--; state.addNotification("Cuota pagada!",NESPalette.GOLD); }
                    else state.addNotification("Sin fondos",NESPalette.RED);
                    return true;
                }
                y+=74;
            }
        } else y+=32;
        y+=28;
        // New loan
        for(GameData.LoanType lt:GameData.LOANS){
            if(ty>=y&&ty<y+74&&tx>SW-130){
                int weeks=(int)(lt.amount/lt.weeklyPayment)+4;
                state.loans.add(new Models.ActiveLoan(lt.id,lt.amount,weeks,lt.weeklyInterest,lt.weeklyPayment));
                state.money+=lt.amount;
                state.addNotification("Prestamo de $"+lt.amount+" aprobado!",NESPalette.GOLD);
                return true;
            }
            y+=74;
        }
        return true;
    }
}
