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

import static com.hunch.Const.MENU_BACK_TO_LIST;
import static com.hunch.Const.MENU_RESTART_TOPIC;
import static com.hunch.Const.MENU_SKIP_TO_RESULTS;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hunch.Const;
import com.hunch.ImageManager;
import com.hunch.R;
import com.hunch.api.HunchAPI;
import com.hunch.api.HunchNextQuestion;
import com.hunch.api.HunchQuestion;
import com.hunch.api.HunchResponse;
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

	protected HunchAPI api;

	protected String curQAState;
	protected String curRankedResultsResponses;
	
	protected IHunchTopic curTopic = null;
	

	@Override
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );		

		api = HunchAPI.getInstance();

		String topicId = getIntent().getStringExtra( "topicId" );
		Log.d( TAG, String.format( "creating: (tid: %s)", topicId ) );

	}

	@Override
	public void onResume()
	{
		super.onResume();
		
		String topicId = getIntent().getStringExtra( "topicId" );
		
		startTopic( topicId );
		
		Log.d( TAG, String.format( "resuming: (tid: %s, qaS: %s)", topicId, curQAState ) );
	}	
	
	private void startTopic( final String topicId )
	{		
		// first set up the content view
		setContentView(	createView() );
		
		// then get the first question
		api.nextQuestion( topicId, curQAState, null, QUESTION_IMG_SIZE, RESPONSE_IMG_SIZE,
				TOPIC_IMG_SIZE,	new HunchNextQuestion.Callback()
		{
			@Override
			public void callComplete( final HunchNextQuestion h )
			{
				if( h.getTopic() != null )
				{
					curTopic = h.getTopic();
				}
				
				if ( h.isResult() )
				{
					showResults( h.getRankedResultResponses(), curTopic );
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

	private View createView()
	{
		// first inflate the layout to get the main viewgroup
		final LayoutInflater inflater = getLayoutInflater();

		final View mainLayout = inflater.inflate( R.layout.interview, null );
		
		final RelativeLayout topicContent = (RelativeLayout)
			mainLayout.findViewById( R.id.interview_content );
		
		// temporarily hide the topic title textview
		// otherwise it just sits there displaying "false"
		// until the API call returns and the topic is loaded
		//TextView topicTitle = (TextView) mainLayout.findViewById( R.id.topic_name );
		//topicTitle.setText( "" );
		
		final TextView questionText = (TextView) topicContent.findViewById( R.id.question_text );
		questionText.setText( "" );
		
		return mainLayout;
		
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
		// otherwise we can just get the topic info out of the HunchNextQuestion
		IHunchTopic topic = question.getTopic();
		
		if( question.isResult() )
		{
			showResults( question.getRankedResultResponses(), topic );			
			return;
		}
		
		// set the topic image
		final ImageView topicImg = (ImageView) findViewById( R.id.topic_icon );

		ImageManager.getInstance().getTopicImage( this, topicImg, topic.getImageUrl() );
		
		// set the topic title
		final TextView topicTitle = (TextView) findViewById( R.id.topic_name );
		topicTitle.setText( topic.getDecision() );
	}

	private void handleResponse( final HunchResponse resp )
	{
		final String oldQAState = curQAState;
		curQAState = resp.getQAState();

		api.nextQuestion( curTopic.getId(), curQAState, null, QUESTION_IMG_SIZE,
				RESPONSE_IMG_SIZE, TOPIC_IMG_SIZE, new HunchNextQuestion.Callback()
		{
			@Override
			public void callComplete( final HunchNextQuestion response )
			{
				if ( response.isResult() )
				{
					// don't update the qaState because if we do, and the user
					// hits the back button they will be bounced to the results
					// screen again instead of the last question, which is probably
					// what they want.
					curQAState = oldQAState;
					
					showResults( response.getRankedResultResponses(), curTopic );
				}
				else
				{
					setupQuestion( response );
					setupBackButton( response );
				}
				
				// update the variable with the super long name
				curRankedResultsResponses = response.getRankedResultResponses();

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

		final View topicContent = findViewById( R.id.interview_content );

		// FrameLayout curContentContainer = (FrameLayout) findViewById(
		// R.id.topicContentContainer );
		// curContentContainer.removeAllViews();

		// final View topicContent = getLayoutInflater().inflate(
		// R.layout.topic_content_layout, null );

		// what is the question?
		final TextView questionText = (TextView) topicContent.findViewById( R.id.question_text );
		questionText.setText( question.getText() );

		// add the responses to the list
		final ListView responses = (ListView) topicContent.findViewById( R.id.response_list );
		responses.setAdapter( new ResponseListAdapter( this, question.getResponses() ) );


	}
	
	private void setupBackButton( final HunchNextQuestion h )
	{
		final Button button = (Button) findViewById( R.id.back_button );
		final String prevQAState = h.getPrevQAState();
		// final State state = curState;
		
		// Log.d( Const.TAG, "setting up back button [" + button + "]" );
		
		
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
				// Log.d( Const.TAG, "back button clicked [" + button + "]" );
				if( prevQAState == null )
				{
					// this is the first question, let's go back to the topic list by finishing the activity
					TopicInterviewActivity.this.finish();
				}
				else
				{
					api.nextQuestion( curTopic.getId(), prevQAState, null, QUESTION_IMG_SIZE,
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
		menu.add( Menu.NONE, MENU_SKIP_TO_RESULTS, Menu.NONE, "Skip to Results" )
			.setIcon( R.drawable.ic_menu_forward );
		menu.add( Menu.NONE, MENU_BACK_TO_LIST, Menu.NONE, "Back to List" )
			.setIcon( R.drawable.ic_menu_agenda );
		
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		if( item.getItemId() == MENU_RESTART_TOPIC )
		{
			curQAState = "";
			
			startTopic( curTopic.getId() );
		}
		else if( item.getItemId() == MENU_SKIP_TO_RESULTS )
		{
			if( curQAState == null ||
				curQAState == "" )
			{
				Toast.makeText( this, "You must answer at least one question" +
						" before getting any results!", Toast.LENGTH_SHORT ).show();
				return super.onOptionsItemSelected( item );
			}
			
			showResults( curRankedResultsResponses, curTopic );
		}
		else if( item.getItemId() == MENU_BACK_TO_LIST )
		{
			// finish the activity... ShowResultsDetails and any activites it launches
			// must be finish()ed by now to avoid the possibility of going "back" to
			// result details when we really want to go back to TopicSelectActivity
			this.finish();
		}
		
		return super.onOptionsItemSelected( item );
	}
	
	protected void showResults( String rankedResultResponses, IHunchTopic topic )
	{
		Intent resultDetailsIntent = new Intent( this, ShowResultsActivity.class );
		resultDetailsIntent.putExtra( "rankedResultResponses", rankedResultResponses );
		resultDetailsIntent.putExtra( "topicId", topic.getId() );
		resultDetailsIntent.putExtra( "topicTitle", topic.getDecision() );
		resultDetailsIntent.putExtra( "topicImgUrl", topic.getImageUrl() );
		
		startActivity( resultDetailsIntent );
	}
}
