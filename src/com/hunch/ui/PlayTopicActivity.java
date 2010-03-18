package com.hunch.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import static com.hunch.Const.*;
import com.hunch.ImageCacher;
import com.hunch.R;
import com.hunch.api.HunchAPI;
import com.hunch.api.HunchNextQuestion;
import com.hunch.api.HunchQuestion;
import com.hunch.api.HunchRankedResults;
import com.hunch.api.HunchResponse;
import com.hunch.api.HunchResult;
import com.hunch.api.HunchTopic;
import com.hunch.api.IHunchTopic;
import com.hunch.api.HunchRankedResults.ResultStub;
import com.hunch.util.InfiniteListAdapter;

/**
 * 
 * 
 * @author Tyler Levine Dec 7, 2009
 * 
 */
public class PlayTopicActivity extends Activity
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

	private class ResultListAdapter extends InfiniteListAdapter< ResultStub >
	{
		private final Context context;
		private final ProgressDialog progress;
		private boolean showProgress = true;
		private final Map< ResultStub, HunchResult > resultsCache = new HashMap< ResultStub, HunchResult >();
		
		public static final int RESULTS_SHOWN_ON_LOAD = 10;
		public static final int RESULTS_ADDED_INLINE = 5;

		public ResultListAdapter( Context context, List< ResultStub > list )
		{
			super( list, RESULTS_SHOWN_ON_LOAD, RESULTS_ADDED_INLINE );

			this.context = context;
			progress = new ProgressDialog( context );
		}

		@Override
		protected boolean shouldLoadInline( int curPos, int size )
		{
			return curPos >= size - 3;
		}

		private void setupResultView( final HunchResult result, View resultView, int index,
				String pct )
		{
			// first download the result image.
			// this is gonna take a while because it needs
			// to hit the network in most cases.

			final ImageView resultImg = new ImageView( context );
			final ProgressBar placeholder = (ProgressBar) resultView.findViewById( R.id.resultImage );
			if( placeholder != null )
				placeholder.setIndeterminate( true );
			
			final ViewGroup parentGroup = (ViewGroup) resultView;

			ImageCacher.fromURL( result.getImageUrl(), new ImageCacher.Callback()
			{

				@Override
				public void callComplete( Drawable d )
				{
					// copy the layout params and index
					resultImg.setLayoutParams( placeholder.getLayoutParams() );
					int childIndex = parentGroup.indexOfChild( placeholder );
					
					if( progress.isShowing() )
						progress.hide();

					// remove the progressbar
					parentGroup.removeView( placeholder );
					
					if( placeholder != null )
						placeholder.setVisibility( View.GONE );

					// add the image view
					parentGroup.addView( resultImg, childIndex );

					// set the drawable
					resultImg.setBackgroundDrawable( d );

				}
			} );

			// now set the rest of the fields - index, name and the percentage
			TextView resultNumber = (TextView) resultView.findViewById( R.id.resultNumber );
			resultNumber.setText( String.valueOf( index + 1 ) + "." );

			TextView resultName = (TextView) resultView.findViewById( R.id.resultName );
			resultName.setText( result.getName() );

			TextView resultPct = (TextView) resultView.findViewById( R.id.resultPct );
			if ( pct != null )
			{
				resultPct.setText( pct + "%" );
			} else
			{
				resultPct.setVisibility( View.GONE );
				parentGroup.removeView( resultPct );
			}
			
			if( progress.isShowing() )
				progress.dismiss();
			
			// add the click handlers for the result
			resultView.setOnClickListener( new View.OnClickListener()
			{
				
				@Override
				public void onClick( View v )
				{
					resultDetails( result );
				}
			} );

		}

		@Override
		public View getView( final int position, View convertView, ViewGroup parent )
		{
			// first find the inflater
			final LayoutInflater inflater = (LayoutInflater)
					context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			
			final View resultView = inflater.inflate( R.layout.result_list_item, null );
			
			// get the basic result info
			final ResultStub stub = getItem( position );
			
			/*
			 * BUGFIX:
			 * 
			 *  getView() is called every time the view is supposed to enter the screen.
			 *  
			 *  This was not the behavior I expected at first, so getting the result off the network
			 *  every time was not smart.
			 *  
			 *  I'm going to try to cache the results locally and use that before trying to
			 *  get the result off the wire.
			 */

			if( resultsCache.containsKey( stub ) )
			{
				HunchResult result = resultsCache.get( stub );
				
				if( stub.hasEitherOrPct() )
				{
					setupResultView( result, resultView, position, stub.getEitherOrPct() );
				}
				else
				{
					setupResultView( result, resultView, position, null );
				}
			}
			// we don't have this result yet, go get it.
			else
			{
				// and use it to get the full result off the network
				HunchAPI.getInstance().getResult( stub.getId(), RESULT_IMAGE_SIZE,
						new HunchResult.Callback()
				{

					@Override
					public void callComplete( HunchResult h )
					{
						// add it to the cache set
						resultsCache.put( stub, h );
					
						if( stub.hasEitherOrPct() )
						{
							setupResultView( h, resultView, position, stub.getEitherOrPct() );
						}
						else
						{
							setupResultView( h, resultView, position, null );
						}
					}
				} );
			}
			
			// set the special background if it's the top result
			if ( position == 0 )
				resultView.setBackgroundResource( R.drawable.top_result );
			
			// finally try to add more items inline
			super.tryLoadInline( position );
			
			if( showProgress )
			{
				progress.setIndeterminate( true );
				progress.setTitle( "Results" );
				progress.setMessage( "Fetching your results..." );
				progress.show();
				
				showProgress = false;
			}
			
			return resultView;
		}

	}

	private HunchAPI api;

	private String topicId;
	private String curQAState;
	
	private enum State
	{
		INTERVIEW, RESULTS, RESULTS_DETAIL
	};
	
	private State curState;
	
	private Integer latestResultDetailsId;

	/*
	 * Does this field need to be here? Or can we handle everything with the
	 * local param to startTopic?
	 */
	// private HunchNextQuestion nextQuestion;

	@Override
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );

		topicId = String.valueOf( getIntent().getIntExtra( "topicId", -1 ) );

		api = HunchAPI.getInstance();

		//if ( icicle != null && icicle.containsKey( "qaState" ) )
		//	curQAState = icicle.getString( "qaState" );

		Log.d( TAG, String.format( "creating: (tid: %s)", topicId ) );

	}

	@Override
	public void onResume()
	{
		super.onResume();

		assert topicId != null;

		/*api.nextQuestion( topicId, curQAState, null, "32x32", "32x32", "64x64",
			new HunchNextQuestion.Callback()
			{
				@Override
				public void callComplete( HunchNextQuestion resp )
				{
					startTopic( resp );
					
					setupQuestion( resp );
				}
			} );*/
		if( curState == State.RESULTS_DETAIL )
		{
			
		}
		else
		{
			startTopic();
		}
		
		Log.d( TAG, String.format( "resuming: (tid: %s, qaS: %s)", topicId, curQAState ) );
	}

	@Override
	public void onRestoreInstanceState( Bundle icicle )
	{
		super.onRestoreInstanceState( icicle );

		if ( topicId == null )
			topicId = icicle.getString( "topicId" );

		curQAState = icicle.getString( "qaState" );
		curState = (State) icicle.getSerializable( "curState" );
		latestResultDetailsId = icicle.getInt( "latestResultDetailsId" );

		Log.d( TAG, String.format( "restoring state %s: (tid: %s, qaS: %s, detailsID: %s)",
				curState, topicId, curQAState, latestResultDetailsId ) );
	}

	@Override
	public void onSaveInstanceState( Bundle out )
	{
		super.onSaveInstanceState( out );

		Log.d( TAG, String.format( "saving state %s: (tid: %s, qaS: %s)",
				curState, topicId, curQAState ) );

		out.putString( "qaState", curQAState );
		out.putString( "topicId", topicId );
		
		if( latestResultDetailsId != null )
			out.putInt( "latestResultDetailsId", latestResultDetailsId );
		
		out.putSerializable( "curState", curState );
	}
	
	private void startTopic()
	{		
		// first create the layouts
		createTopicLayouts();
		
		// then get the first question
		api.nextQuestion( topicId, curQAState, null, QUESTION_IMAGE_SIZE, RESPONSE_IMAGE_SIZE, 
				TOPIC_IMAGE_SIZE, new HunchNextQuestion.Callback()
		{
			@Override
			public void callComplete( final HunchNextQuestion h )
			{	
				if ( h.isResult() )
				{
					api.rankedResults( topicId, h.getRankedResultResponses(), null,
						new HunchRankedResults.Callback()
						{

							@Override
							public void callComplete( HunchRankedResults results )
							{
								populateTopic( h );
								showResults( results );
							}
						} );
				}
				else
				{
					populateTopic( h );
					setupQuestion( h );
				}
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
			/* we were resumed while displaying results
			 * and our HunchNextQuestion has only rankedResultsResponses
			 * so we can't use it to populate the topic info
			 * in this case, we're forced to do an extra API call to get the info
			 */
			
			api.getTopic( topicId, null, TOPIC_IMAGE_SIZE, new HunchTopic.Callback()
			{
				
				@Override
				public void callComplete( IHunchTopic topic )
				{
					// set the topic image
					final ImageView topicImg = (ImageView) findViewById( R.id.topicImage );

					ImageCacher.fromURL( topic.getImageUrl(), new ImageCacher.Callback()
					{

						@Override
						public void callComplete( Drawable d )
						{
							topicImg.setImageDrawable( d );
						}

					} );

					// set the topic title
					final TextView topicTitle = (TextView) findViewById( R.id.topicTitle );
					String strTopicTitle = topic.getDecision();
					topicTitle.setText( strTopicTitle );
					
				}
			} );
			
			return;
		}
		
		// otherwise we can just get the topic info out of the HunchNextQuestion
		IHunchTopic topic = question.getTopic();

		// set the topic image
		final ImageView topicImg = (ImageView) findViewById( R.id.topicImage );

		ImageCacher.fromURL( topic.getImageUrl(), new ImageCacher.Callback()
		{

			@Override
			public void callComplete( Drawable d )
			{
				topicImg.setImageDrawable( d );
			}

		} );

		// set the topic title
		final TextView topicTitle = (TextView) findViewById( R.id.topicTitle );
		String strTopicTitle = topic.getDecision();
		topicTitle.setText( strTopicTitle );
	}

	private void handleResponse( final HunchResponse resp )
	{
		curQAState = resp.getQAState();

		api.nextQuestion( topicId, resp.getQAState(), null, QUESTION_IMAGE_SIZE, RESPONSE_IMAGE_SIZE,
				TOPIC_IMAGE_SIZE, new HunchNextQuestion.Callback()
		{
			@Override
			public void callComplete( final HunchNextQuestion response )
			{
				if ( response.isResult() )
				{
					api.rankedResults( topicId, response.getRankedResultResponses(), null,
						new HunchRankedResults.Callback()
						{

							@Override
							public void callComplete( HunchRankedResults h )
							{
								showResults( h );
							}
						} );
				}
				else
				{
					setupQuestion( response );
				}

			}
		} );
	}

	private void showResults( final HunchRankedResults results )
	{
		curState = State.RESULTS;
		
		// first remove "skip this question" and "back" buttons
		// since we don't have a use for them in the results screen
		ViewGroup topicLayout = (ViewGroup) findViewById( R.id.playTopicLayout );
		View buttonLayout = topicLayout.findViewById( R.id.playTopicBottomButtonLayout );
		topicLayout.removeView( buttonLayout );

		// now get handles to the result content layouts
		// final List< ResultStub > resultsList = results.getAllResults();
		// final LayoutInflater inflater = getLayoutInflater();
		// final RelativeLayout contentLayout = (RelativeLayout) findViewById(
		// R.id.questionAndResponsesLayout );
		final ListView resultItemsLayout = (ListView) findViewById( R.id.responseLayout );

		resultItemsLayout.setAdapter( new ResultListAdapter( this, results.getAllResults() ) );

		final View responseLayout = this.findViewById( R.id.questionAndResponsesLayout );

		final TextView titleText = (TextView) responseLayout.findViewById( R.id.questionText );
		titleText.setText( R.string.resultsTitleText );
	}

	private void setupQuestion( final HunchNextQuestion nextQuestion )
	{
		curState = State.INTERVIEW;
		
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
	
	private void resultDetails( HunchResult result )
	{
		latestResultDetailsId = result.getId();
		curState = State.RESULTS_DETAIL;
		
		Log.d( TAG, "show result with ID#" + result.getId() );
	}
}
