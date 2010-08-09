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

import static com.hunch.Const.MENU_RESTART_TOPIC;
import static com.hunch.Const.QUESTION_IMG_SIZE;
import static com.hunch.Const.RESPONSE_IMG_SIZE;
import static com.hunch.Const.TAG;
import static com.hunch.Const.TOPIC_IMG_SIZE;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hunch.ImageManager;
import com.hunch.R;
import com.hunch.api.HunchAPI;
import com.hunch.api.HunchNextQuestion;
import com.hunch.api.HunchQuestion;
import com.hunch.api.HunchResponse;
import com.hunch.api.HunchTopic;
import com.hunch.api.IHunchTopic;
import com.hunch.util.InfiniteListAdapter;

/**
 * 
 * 
 * @author Tyler Levine Dec 7, 2009
 * 
 */
public class TopicInterviewActivity extends Activity
{
	/**
	 * 
	 * 
	 * @author Tyler Levine
	 * @since Mar 15, 2010
	 * 
	 */
	private class ResponseListAdapter extends InfiniteListAdapter< HunchResponse >
	{
		private final Context context;
		//private String curSkipQAState;
		
		public static final int RESPONSES_SHOWN_ON_LOAD = 10;
		public static final int RESPONSES_ADDED_INLINE = 5;

		/**
		 * @param items
		 */
		public ResponseListAdapter( Context context, List< HunchResponse > items )
		{
			super( items, RESPONSES_SHOWN_ON_LOAD, RESPONSES_ADDED_INLINE );

			this.context = context;
		}

		@Override
		public View getView( int position, View convertView, ViewGroup parent )
		{
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

			//Log.d( TAG, "creating View for result #" + position );

			// inflate the layout responsible for responses
			View item = inflater.inflate( R.layout.play_topic_response_item, null );

			Button respBtn = (Button) item.findViewById( R.id.responseButton );

			final HunchResponse resp = getItem( position );

			respBtn.setOnClickListener( new OnClickListener()
			{

				@Override
				public void onClick( View v )
				{
					handleResponse( resp );
				}
			} );

			String text = resp.getText();
			respBtn.setText( text );
			
			// try to load more items inline
			super.tryLoadInline( position );

			return item;
		}

		@Override
		protected boolean shouldLoadInline( int curPos, int size )
		{
			// load more if we're within 2 items of the end
			return curPos >= size - 2;
		}

	}

	

	private HunchAPI api;

	private String curQAState;
	
	private String curTopicId;
	private String curTopicTitle;
	private String curTopicImgUrl;
	
	//private boolean showResultsLoadingDialog;
	
	//private Integer latestResultDetailsId;

	/*
	 * Does this field need to be here? Or can we handle everything with the
	 * local param to startTopic?
	 */
	// private HunchNextQuestion nextQuestion;

	@Override
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );

		

		api = HunchAPI.getInstance();
		//showResultsLoadingDialog = true;

		//if ( icicle != null && icicle.containsKey( "qaState" ) )
		//	curQAState = icicle.getString( "qaState" );
		
		curTopicId = getIntent().getStringExtra( "topicId" );
		Log.d( TAG, String.format( "creating: (tid: %s)", curTopicId ) );

	}

	@Override
	public void onResume()
	{
		super.onResume();
		
		//String topicId = getIntent().getStringExtra( "topicId" );
		
		assert curTopicId != null;
		
		startTopic( curTopicId );
		
		Log.d( TAG, String.format( "resuming: (tid: %s, qaS: %s)", curTopicId, curQAState ) );
	}

	@Override
	public void onRestoreInstanceState( Bundle icicle )
	{
		super.onRestoreInstanceState( icicle );

		//if ( topicId == null )
		//	topicId = icicle.getString( "topicId" );

		curQAState = icicle.getString( "qaState" );
		curTopicId = icicle.getString( "topicId" );
		curTopicTitle = icicle.getString( "topicTitle" );
		curTopicImgUrl = icicle.getString( "curTopicImgUrl" );
		//curState = (State) icicle.getSerializable( "curState" );
		//showResultsLoadingDialog = icicle.getBoolean( "showLoadingDialog" );
		//latestResultDetailsId = icicle.getInt( "latestResultDetailsId" );

		//Log.d( TAG, String.format( "restoring state %s: (tid: %s, qaS: %s)",
		//		curState, topicId, curQAState ) );//, latestResultDetailsId ) );
	}

	@Override
	public void onSaveInstanceState( Bundle out )
	{
		super.onSaveInstanceState( out );

		//Log.d( TAG, String.format( "saving state %s: (tid: %s, qaS: %s)",
		//		curState, topicId, curQAState ) );

		//out.putSerializable( "state", curState );
		out.putString( "qaState", curQAState );
		out.putString( "topicId", curTopicId );
		out.putString( "topicTitle", curTopicTitle );
		out.putString( "topicImgUrl", curTopicImgUrl );
		//out.putBoolean( "showLoadingDialog", showResultsLoadingDialog );
		
		/*if( curState == State.RESULTS )
		{
			Bundle results = new Bundle();
			saveResults( results );
			out.putBundle( "savedResults", results );
		}*/
		
		//if( latestResultDetailsId != null )
		//	out.putInt( "latestResultDetailsId", latestResultDetailsId );
		
		//out.putSerializable( "curState", curState );
	}
	
	
	
	private void startTopic( final String topicId )
	{		
		// first create the layouts
		createTopicLayouts();
		
		// then get the first question
		api.nextQuestion( topicId, curQAState, null, QUESTION_IMG_SIZE, RESPONSE_IMG_SIZE, TOPIC_IMG_SIZE,
				new HunchNextQuestion.Callback()
		{
			@Override
			public void callComplete( final HunchNextQuestion h )
			{	
				if ( h.isResult() )
				{
					showResults( h.getRankedResultResponses() );
				}
				else
				{
					populateTopic( h );
					setupQuestion( h );
				}
				
				setupBackButton( h );
			}
		} );
	}

	private void createTopicLayouts()
	{
		// first inflate the layout to get the main viewgroup
		final LayoutInflater inflater = getLayoutInflater();

		final View mainLayout = inflater.inflate( R.layout.play_topic, null );
		FrameLayout topicContentContainer = (FrameLayout) 
						mainLayout.findViewById( R.id.topicContentContainer );
		final View topicContent = inflater.inflate( R.layout.topic_content_layout, topicContentContainer );

		// disable focusing on the actual list item views (instead contain the
		// focusing to the pseudo-button views within the list views)
		final ListView responses = (ListView) topicContent.findViewById( R.id.responseLayout );
		responses.setItemsCanFocus( false );
		
		// temporarily hide the topic title textview
		// otherwise it just sits there displaying "false"
		// until the API call returns and the topic is loaded
		TextView topicTitle = (TextView) mainLayout.findViewById( R.id.topicTitle );
		topicTitle.setText( "" );
		
		// display everything
		setContentView( mainLayout );		
		
	}
	
	/**
	 * This method sets up the textviews and images and such for the topic.
	 * 
	 * It also populates the question textview and responses.
	 * 
	 * @param question The question to display.
	 */
	private void populateTopic( HunchNextQuestion question )
	{
		if( question.isResult() )
		{
			showResults( question.getRankedResultResponses() );			
			return;
		}
		
		// otherwise we can just get the topic info out of the HunchNextQuestion
		IHunchTopic topic = question.getTopic();

		// set the topic image
		final ImageView topicImg = (ImageView) findViewById( R.id.topicImage );

		ImageManager.getInstance().getTopicImage( this, topicImg, topic.getImageUrl() );
		
		// set the topic title
		final TextView topicTitle = (TextView) findViewById( R.id.topicTitle );
		String strTopicTitle = topic.getDecision();
		topicTitle.setText( strTopicTitle );
	}

	private void handleResponse( final HunchResponse resp )
	{
		curQAState = resp.getQAState();

		api.nextQuestion( curTopicId, resp.getQAState(), null, QUESTION_IMG_SIZE,
				RESPONSE_IMG_SIZE, TOPIC_IMG_SIZE, new HunchNextQuestion.Callback()
		{
			@Override
			public void callComplete( final HunchNextQuestion response )
			{
				if ( response.isResult() )
				{
					showResults( response.getRankedResultResponses() );
				}
				else
				{
					setupQuestion( response );
					setupBackButton( response );
				}

			}
		} );
	}

	/**
	 * Sets up the question text and response list adapter.
	 * 
	 * @param nextQuestion
	 */
	private void setupQuestion( final HunchNextQuestion nextQuestion )
	{
		//curState = State.INTERVIEW;
		
		final HunchQuestion question = nextQuestion.getNextQuestion();

		final View topicContent = findViewById( R.id.questionAndResponsesLayout );

		// FrameLayout curContentContainer = (FrameLayout) findViewById(
		// R.id.topicContentContainer );
		// curContentContainer.removeAllViews();

		// final View topicContent = getLayoutInflater().inflate(
		// R.layout.topic_content_layout, null );

		// what is the question?
		final TextView questionText = (TextView) topicContent.findViewById( R.id.questionText );
		questionText.setText( question.getText() );

		// add the responses to the list
		final ListView responses = (ListView) topicContent.findViewById( R.id.responseLayout );
		responses.setAdapter( new ResponseListAdapter( this, question.getResponses() ) );


	}
	
	private void setupBackButton( final HunchNextQuestion h )
	{
		final Button button = (Button) findViewById( R.id.lastQuestion );
		final String prevQAState = h.getPrevQAState();
		// final State state = curState;
		
		// if the user moves through the menus very quickly
		// we can reach the results page while a question is
		// still being retrieved.
		if( button == null )
			return;
		
		button.setOnClickListener( new View.OnClickListener()
		{
			
			@Override
			public void onClick( View v )
			{
				if( prevQAState == null )
				{
					// this is the first question, let's go back to the topic list by finishing the activity
					TopicInterviewActivity.this.finish();
				}
				else
				{
					api.nextQuestion( curTopicId, prevQAState, null, QUESTION_IMG_SIZE,
							RESPONSE_IMG_SIZE, TOPIC_IMG_SIZE, new HunchNextQuestion.Callback()
					{

						@Override
						public void callComplete( HunchNextQuestion hnq )
						{
							//populateTopic( h );
							/*if( state == State.RESULTS )
							{
								// if we are showing results right now
								// we need to recreate the layouts to
								// display the topic again
								createTopicLayouts();
							}*/
						
							setupQuestion( hnq );
							setupBackButton( hnq );
						}
					
					} );
				}
			}
		} );
	}
	
	@Override
	public boolean onCreateOptionsMenu( final Menu menu )
	{
		super.onCreateOptionsMenu( menu );
		
		// create the menu
		// we don't care about group or order right now.
		menu.add( Menu.NONE, MENU_RESTART_TOPIC, Menu.NONE, "Restart Topic" )
			.setIcon( R.drawable.restart_topic );
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		if( item.getItemId() == MENU_RESTART_TOPIC )
		{
			curQAState = "";
			
			startTopic( curTopicId );
		}
		
		return super.onOptionsItemSelected( item );
	}
	
	protected void showResults( String rankedResultResponses )
	{
		Intent resultDetailsIntent = new Intent( this, ShowResultsActivity.class );
		resultDetailsIntent.putExtra( "rankedResultResponses", rankedResultResponses );
		resultDetailsIntent.putExtra( "topicId", curTopicId );
		resultDetailsIntent.putExtra( "topicTitle", curTopicTitle );
		resultDetailsIntent.putExtra( "topicImgUrl", curTopicImgUrl );
		
		startActivity( resultDetailsIntent );
	}
}
