package br.com.truckZ;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

/**
 * Created by Ronan.lima on 06/07/16.
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.content_logo_fragment);

        Thread background = new Thread(){
            @Override
            public void run() {
                try{
                    sleep(2 * 1000);

                    Intent i = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(i);
                    finish();
                } catch (Exception e){
                    Log.d(getClass().getSimpleName().toUpperCase(),"Deu ruim na exibição da splash screen. Motivo: "+e.getMessage());
                }
            }
        };

        background.start();
    }
}
