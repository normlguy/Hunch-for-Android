package com.hunch.ui;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.hunch.Const;
import com.hunch.api.HunchAPI;
import com.hunch.api.HunchNextQuestion;
import com.hunch.api.HunchObject;

/**
 * 
 * 
 * @author Tyler Levine
 * Dec 7, 2009
 *
 */
public class PlayTopicActivity extends Activity
{
	
	private final Handler handler = new Handler()
	{
		@Override
		public void handleMessage( Message msg )
		{
			switch( msg.what )
			{
				case Const.TOPIC_PLAY_INIT:
					nextQuestion = (HunchNextQuestion) msg.obj;
					
					break;
			}			
		}
	};
	
	private HunchAPI api;
	private HunchNextQuestion nextQuestion;

	@Override
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		
		final int topicId = getIntent().getIntExtra( "topicId", -1 );
		
		TextView view = new TextView( this );
		view.setText( "Loading topicID #" + topicId + "..." );
		
		setContentView( view );
		
		api = HunchAPI.getInstance();
		
		final HunchAPI.Callback callback = new HunchAPI.Callback()
		{
			@Override
			public void callComplete( HunchObject resp )
			{
				Message m = handler.obtainMessage( Const.TOPIC_PLAY_INIT, resp );
				handler.sendMessage( m );
			}
		};
		
		new Thread()
		{
			@Override
			public void run()
			{
				Map< String, String > params = new HashMap< String, String >();
				params.put( "topicId", String.valueOf( topicId ) );
				
				api.nextQuestion( params, callback );
			}
		}.start();
	}
	
	private void startTopic( final HunchNextQuestion firstQuestion )
	{
		
	}
}
