/*
 * Copyright 2009, 2010 Tyler Levine
 * 
 * This file is part of Hunch for Android.
 *
 * Hunch for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hunch for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hunch for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hunch.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hunch.Const;
import com.hunch.ImageManager;
import com.hunch.R;
import com.hunch.api.HunchAPI;
import com.hunch.api.HunchResult;

/**
 * 
 * 
 * @author Tyler Levine
 * @since Apr 19, 2010
 *
 */
public class ResultDetailsActivity extends Activity
{
	private ProgressDialog progress;
	
	@Override
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		
		// get the info for the result
		int resultId = getIntent().getExtras().getInt( "resultId" );
		
		startProgressDialog();
		
		createLayouts();
		
		HunchAPI.getInstance().getResult( String.valueOf( resultId ), Const.RESULT_DETAILS_IMG_SIZE,
				new HunchResult.Callback()
		{
					
			@Override
			public void callComplete( HunchResult h )
			{
				setupDetails( h );
				
				dismissProgressDialog();
			}
		} );
		
		setupListeners();
		
	}
	
	private void startProgressDialog()
	{
		// let the user know we're loading
		if( progress == null )
		{
			progress = new ProgressDialog( this );
			progress.setIndeterminate( true );
			progress.setTitle( "Loading..." );
			progress.setMessage( "Fetching result details" );
			progress.show();
		}
		else
		{
			progress.show();
		}
	}
	
	private void dismissProgressDialog()
	{
		if( progress.isShowing() )
		{
			progress.dismiss();
			progress = null;
		}
	}
	
	private void createLayouts()
	{
		final View rootLayout = getLayoutInflater().inflate( R.layout.play_topic, null );
		
		// inflate a new view for the result and attach the sub-view
		FrameLayout contentFrame = (FrameLayout) rootLayout.findViewById( R.id.topicContentContainer );
		contentFrame = (FrameLayout) getLayoutInflater().inflate( R.layout.result_details, contentFrame );
		
		setContentView( rootLayout );
	}
	
	private void setupListeners()
	{
		// set up the soft back button
		Button back = (Button) findViewById( R.id.lastQuestion );
		back.setOnClickListener( new View.OnClickListener()
		{
			
			@Override
			public void onClick( View v )
			{
				ResultDetailsActivity.this.finish();
			}
		} );
	}
	
	private void setupDetails( final HunchResult result )
	{
		
		final View contentLayout = findViewById( R.id.topicContentContainer );
		final LinearLayout infoLayout = (LinearLayout) contentLayout.findViewById( R.id.infoLayout );
		final ImageView detailsImage = (ImageView) infoLayout.findViewById( R.id.infoImage );
		
		// first start the image download which requires another API call
		ImageManager.getInstance().getTopicImage( this, detailsImage, result.getImageUrl() );
		
		// remove the topic image - we don't need it
		View topicImage = findViewById( R.id.topicImage );
		topicImage.setVisibility( View.GONE );
		
		// set the title
		TextView resultTitle = (TextView) findViewById( R.id.topicTitle );
		resultTitle.setGravity( Gravity.CENTER_HORIZONTAL );
		resultTitle.setText( result.getName() );
		
		// set the details text
		TextView resultText = (TextView) infoLayout.findViewById( R.id.infoText );
		resultText.setText( result.getDescription() );
		
		if( !result.hasAffiliateLinks() ) return;
		
		// we have affiliate links to set up
		for( final HunchResult.AffiliateLink link : result.getAffiliateLinks() )
		{
			// first inflate a view
			View shopButton = getLayoutInflater().inflate( R.layout.shop_button, null );
			TextView vendorName = (TextView) shopButton.findViewById( R.id.vendorName );
			TextView itemPrice = (TextView) shopButton.findViewById( R.id.itemPrice );
		
			vendorName.setText( link.getText() );
			itemPrice.setText( String.format( "$%<.2f", link.getPrice().floatValue() ) );
			
			shopButton.setOnClickListener( new View.OnClickListener()
			{
				
				@Override
				public void onClick( View v )
				{
					Intent webIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( link.getUrl() ) );
					startActivity( webIntent );
				}
			} );
			
			infoLayout.addView( shopButton );
		}
		
	}

}
