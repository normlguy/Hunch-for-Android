package com.hunch.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.hunch.Const;
import com.hunch.R;

/**
 * 
 * 
 * @author Tyler Levine
 * Dec 3, 2009
 *
 */
public class HunchHome extends TabActivity
{
	@Override
	public void onCreate( Bundle b )
	{
		super.onCreate( b );

		Log.i( Const.TAG, "Home Loaded..." );
		
		// set up the TabHost container element, and add each tab
		final TabHost tabs = getTabHost();
		TabHost.TabSpec tabOne, tabTwo, tabThree;
		
		tabOne = tabs.newTabSpec( "decide" );		
		tabOne.setContent( new Intent( this, SelectTopicActivity.class ) );
		tabOne.setIndicator( "Decide", 
				getResources().getDrawable( R.drawable.tab1_icon ) );
		
		tabs.addTab( tabOne );
		
		tabTwo = tabs.newTabSpec( "search" );
		tabTwo.setContent( new Intent( this, TabTwoContentActivity.class ) );
		tabTwo.setIndicator( "Search", 
				getResources().getDrawable( R.drawable.tab2_icon ) );
		
		tabs.addTab( tabTwo );
		
		tabThree = tabs.newTabSpec( "starred" );
		tabThree.setContent( new Intent( this, TabThreeContentActivity.class ) );
		//tabThree.setIndicator( "Starred" );
		
		//TextView test = new TextView( this );
		//test.setText( "Starred :D" );
		
//		View v = this.getLayoutInflater().inflate( R.layout.tab_indicator, null );
//		
//		TextView tv = (TextView) v.findViewById( R.id.title );
//		tv.setText( "Starred" );
//		
//		ImageView icon = (ImageView) v.findViewById( R.id.icon );
//		icon.setImageDrawable( Resources.getSystem().getDrawable( android.R.drawable.ic_menu_compass ) );
		
		tabThree.setIndicator( "Starred",
				getResources().getDrawable( R.drawable.tab3_icon ) );
		
		tabs.addTab( tabThree );
		
//		for( int i = 0; i < tabs.getTabWidget().getChildCount(); i++ )
//		{
//			View v = tabs.getTabWidget().getChildAt( i );
//			v.setMinimumHeight( 70 );
//		}
		
		//tabs.getTabWidget().setMinimumHeight( 75 );
		
		tabs.setCurrentTab( 0 );

	}
}