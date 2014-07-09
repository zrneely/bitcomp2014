package org.spalabs.bitcomp.bitgive;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class HomeScreenActivity extends Activity {
	
	public static final String PREFS_NAME = "org.spalabs.bitcomp.bitgive.SharedPrefs";
	public static final String PREF_USER_EXITS = "USER_EXISTS";
	public static final String PREF_MAIN_BTC_ADDR = "MAIN_BTC";
	public static final String PREF_USER_NAME = "USER_NAME";
	
	public static final boolean PRODUCTION = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        SharedPreferences sPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if(!sPrefs.getBoolean(PREF_USER_EXITS, false)){
        	// We need to get a user set up before anything will work
        	Intent intent = new Intent(this, UserInitActivity.class);
        	startActivity(intent);
        	finish(); // Comment this line to let the user cancel initialization
        	return; // Prevents null pointer exception below
        }
        else {
        	setContentView(R.layout.activity_home_screen);
        }
        
        // Add action listeners
        final HomeScreenActivity self = this;
        ((Button) findViewById(R.id.giveButton)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(self, GiveActivity.class);
				startActivity(i);
			}
		});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }
    
}
