package com.hunch.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * 
 * 
 * @author Tyler Levine
 * Dec 6, 2009
 *
 */
public class TabTwoContentActivity extends Activity
{
	
	@Override
	public void onCreate( Bundle b )
	{
		super.onCreate( b );
		TextView text = new TextView( this );
		text.setText( "This is tab 2 (from Intent!)" );
		
		setContentView( text );
	}

}
