package com.hunch.ui;

import static com.hunch.Const.RESULT_IMG_SIZE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hunch.ImageManager;
import com.hunch.R;
import com.hunch.api.HunchAPI;
import com.hunch.api.HunchRankedResults;
import com.hunch.api.HunchResult;
import com.hunch.api.HunchRankedResults.ResultStub;
import com.hunch.util.InfiniteListAdapter;

/**
 * 
 * 
 * @author Tyler Levine
 * @since Aug 9, 2010
 *
 */
public class ShowResultsActivity extends Activity
{
	private class ResultListAdapter extends InfiniteListAdapter< ResultStub >
	{
		private final Context context;
		//private final ProgressDialog progress;
		//private boolean showProgress = true;
		private final Map< ResultStub, HunchResult > resultsCache = new HashMap< ResultStub, HunchResult >();
		
		public static final int RESULTS_SHOWN_ON_LOAD = 10;
		public static final int RESULTS_ADDED_INLINE = 5;

		public ResultListAdapter( Context context, List< ResultStub > list )
		{
			super( list, RESULTS_SHOWN_ON_LOAD, RESULTS_ADDED_INLINE );

			this.context = context;
			//progress = new ProgressDialog( context );
		}
		
		public HunchResult getFromCache( ResultStub stub )
		{
			if( !resultsCache.containsKey( stub ) ) return null;
			
			return resultsCache.get( stub );
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

			ImageManager.getInstance().getTopicImageWithCallback( ShowResultsActivity.this,
					result.getImageUrl(), new ImageManager.Callback()
			{

				@Override
				public void callComplete( Drawable d )
				{
					// copy the layout params and index
					resultImg.setLayoutParams( placeholder.getLayoutParams() );
					int childIndex = parentGroup.indexOfChild( placeholder );
					
					//if( progress.isShowing() )
					//	progress.hide();

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
			
			//if( progress.isShowing() )
			//	progress.dismiss();
			
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
				HunchAPI.getInstance().getResult( stub.getId(), RESULT_IMG_SIZE,
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
			
			//if( showProgress )
			//{
			//	progress.setIndeterminate( true );
			//	progress.setTitle( "Results" );
			//	progress.setMessage( "Fetching your results..." );
			//	progress.show();
				
			//	showProgress = false;
			//}
			
			return resultView;
		}

	}

	private void saveResults( Bundle bundle )
	{
		TextView topicTitle = (TextView) findViewById( R.id.topicTitle );
		bundle.putString( "topicTitle", topicTitle.getText().toString() );
		
		ListView resultsLayout = (ListView) findViewById( R.id.responseLayout );
		
		ResultListAdapter adapter = (ResultListAdapter) resultsLayout.getAdapter();
		
		int numResults = adapter.getCount();
		
		ArrayList< Parcelable > results = new ArrayList< Parcelable >( numResults );
		
		for( int i = 0; i < numResults; i++ )
		{
			ResultStub resultStub = adapter.getItem( i );
			HunchResult result = adapter.getFromCache( resultStub );
			
			if( result == null ) continue;
			
			Bundle resultBundle = new Bundle();
			resultBundle.putInt( "order", i );
			resultBundle.putString( "text", result.getName() );
			resultBundle.putInt( "id", result.getId() );

			results.add( resultBundle );
		}
		
		bundle.putParcelableArrayList( "results", results );
		
	}
	
	private void showResults( final HunchRankedResults results )
	{
		//curState = State.RESULTS;
		
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

		resultItemsLayout.setAdapter( new ResultListAdapter( this,
				results.getAllResults() ) );
		
		//if( showResultsLoadingDialog )
		//	showResultsLoadingDialog = false;

		final View responseLayout = this.findViewById( R.id.questionAndResponsesLayout );

		final TextView titleText = (TextView) responseLayout.findViewById( R.id.questionText );
		titleText.setText( R.string.resultsTitleText );
	}
	
	private void resultDetails( HunchResult result )
	{
		//latestResultDetailsId = result.getId();
		Intent resultDetailsIntent = new Intent( this, ResultDetailsActivity.class );
		resultDetailsIntent.putExtra( "resultId", result.getId() );
		
		startActivity( resultDetailsIntent );
		
		
		//Log.d( TAG, "show result with ID#" + result.getId() );
	}
}
