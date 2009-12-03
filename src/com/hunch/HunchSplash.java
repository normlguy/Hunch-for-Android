package com.hunch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HunchSplash extends Activity
{
	public static final String LOG_TAG = "HUNCH";

	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.splash );
		
		final Button b = (Button) findViewById( R.id.splashButton );
		b.setOnClickListener( new OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				startActivity( new Intent( getApplication(), HunchHome.class ) );
				HunchSplash.this.finish();
			}
		} );
	}
}