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

import static com.hunch.Const.RESULT_IMG_SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hunch.Const;
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
		//private final Map< ResultStub, HunchResult > resultsCache = new HashMap< ResultStub, HunchResult >();
		
		public static final int RESULTS_SHOWN_ON_LOAD = 10;
		public static final int RESULTS_ADDED_INLINE = 5;

		public ResultListAdapter( Context context, List< ResultStub > list )
		{
			super( list, RESULTS_SHOWN_ON_LOAD, RESULTS_ADDED_INLINE );

			this.context = context;
			//progress = new ProgressDialog( context );
		}
		
		public ResultModel getFromModel( int position )
		{
			if( model == null ) return null;
			
			return model.getResultModel( position );
		}
		
		public void addToModel( int position, String name, String id, String imgUrl, String pct )
		{
			if( model == null ) return;
			
			if( model.getResultModel( position ) != null )
			{
				// there is already a result here! don't overwrite
				return;
			}
			
			ResultModel resultModel = new ResultModel( name, id, imgUrl, pct );
			
			Log.d( Const.TAG, "adding to model at position " + position + ", total size: " 
					+ model.size() );
			
			model.addResultModel( position, resultModel );
		}

		@Override
		protected boolean shouldLoadInline( int curPos, int size )
		{
			return curPos >= size - 3;
		}
		
		private void setupResultView( final View resultView, final String resultName,
				final String resultImgUrl, final String resultId, int index, String pct,
				boolean addToModel )
		{
			if( addToModel )
			{
				addToModel( index, resultName, resultId, resultImgUrl, pct );
			}
			
			setupResultView( resultView, resultName, resultImgUrl, resultId, index, pct );
		}

		private void setupResultView( final View resultView, final String resultName,
				final String resultImgUrl, final String resultId, int index, String pct )
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
					resultImgUrl, new ImageManager.Callback()
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
					resultImg.setImageDrawable( d );

				}
			} );

			
			
			// now set the rest of the fields - index, name and the percentage
			TextView resultNumber = (TextView) resultView.findViewById( R.id.resultNumber );
			resultNumber.setText( String.valueOf( index + 1 ) + "." );

			TextView resultNameView = (TextView) resultView.findViewById( R.id.resultName );
			resultNameView.setText( resultName );

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
					resultDetails( resultId );
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
			
			
			ResultModel result = getFromModel( position );
			if( result != null )
			{
				
				if( stub.hasEitherOrPct() )
				{
					setupResultView( resultView, result.getName(), result.getImageUrl(),
							result.getId(), position, stub.getEitherOrPct() );
				}
				else
				{
					setupResultView( resultView, result.getName(), result.getImageUrl(),
							result.getId(), position, null );
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
						if( stub.hasEitherOrPct() )
						{
							setupResultView( resultView, h.getName(), h.getImageUrl(), 
									String.valueOf( h.getId() ), position, stub.getEitherOrPct(), true );
						}
						else
						{
							setupResultView( resultView, h.getName(), h.getImageUrl(),
									String.valueOf( h.getId() ), position, null, true );
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

	private static class ShowResultsModel
	{
		private ResultModel[] results;
		private final Bundle data;
		
		private final String KEY_TOPIC_ID = "topicId";
		private final String KEY_TOPIC_NAME = "topicName";
		private final String KEY_TOPIC_IMG_URL = "topicImgUrl";
		
		private static final int GROWTH_MARGIN = 10;
		
		public ShowResultsModel( String topicId, String topicName, String topicImgUrl,
				int initialCapacity )
		{
			data = new Bundle();
			data.putString( KEY_TOPIC_ID, topicId );
			data.putString( KEY_TOPIC_NAME, topicName );
			data.putString( KEY_TOPIC_IMG_URL, topicImgUrl );
			
			results = new ResultModel[ initialCapacity ];
		}
		
		public ShowResultsModel( String topicId, String topicName, String topicImgUrl )
		{
			this( topicId, topicName, topicImgUrl, GROWTH_MARGIN );
		}
		
		public void addResultModel( int position, ResultModel model )
		{
			try
			{
				results[ position ] = model;
			} catch ( ArrayIndexOutOfBoundsException e )
			{
				setCapacity( position + GROWTH_MARGIN );
				addResultModel( position, model );
			}
		}
		
		public ResultModel getResultModel( int position )
		{
			ResultModel ret = null;
			try
			{
				ret = results[ position ];
			} catch ( IndexOutOfBoundsException e )
			{
				return null;
			}
			
			return ret;
		}
		
		public List< ResultModel > getModelList()
		{
			return Arrays.asList( results );
		}
		
		public void setCapacity( int size )
		{
			ResultModel[] temp = results;
			results = new ResultModel[ size ];
			
			// if the original array was empty there's nothing to copy
			if( temp == null ) return;
			
			// otherwise copy the data over
			System.arraycopy( temp, 0, results, 0, size );
		}
		
		public int size()
		{
			return results.length;
		}
		
		public String getTopicId()
		{
			return data.getString( KEY_TOPIC_ID );
		}
		
		public String getTopicTitle()
		{
			return data.getString( KEY_TOPIC_NAME );
		}
		
		public String getTopicImageUrl()
		{
			return data.getString( KEY_TOPIC_IMG_URL );
		}
		
	}
	
	private static class ResultModel extends ResultStub
	{
		private final Bundle data;
		
		private String KEY_NAME = "name";
		private String KEY_ID = "id";
		private String KEY_IMG_URL = "imgUrl";
		private String KEY_EITHER_OR_PCT = "eitherOrPct";
		
		public ResultModel( String name, String id, String imgUrl, String eitherOrPct )
		{
			super( id, eitherOrPct );
			
			data = new Bundle();
			data.putString( KEY_NAME, name );
			data.putString( KEY_ID, id );
			data.putString( KEY_IMG_URL, imgUrl );
			data.putString( KEY_EITHER_OR_PCT, eitherOrPct );
		}
		
		public String getName()
		{
			return data.getString( KEY_NAME );
		}
		
		public String getId()
		{
			return data.getString( KEY_ID );
		}
		
		public String getImageUrl()
		{
			return data.getString( KEY_IMG_URL );
		}
		
		public String getEitherOrPct()
		{
			return data.getString( KEY_EITHER_OR_PCT );
		}
		
		public boolean hasEitherOrPct()
		{
			return getEitherOrPct() != null;
		}
	}
	
	private ShowResultsModel model;
	
	private ProgressDialog progress;
	
	@Override
	public Object onRetainNonConfigurationInstance()
	{
		return model;
	}
	
	@Override
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );		
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		Intent resultDetailsIntent = getIntent();
		String rankedResultResponses = resultDetailsIntent.getStringExtra( "rankedResultResponses" );
		
		ShowResultsModel lastModel = (ShowResultsModel) getLastNonConfigurationInstance();
		String topicId, topicTitle, topicImgUrl;
		if( lastModel != null )
		{
			// the values are in the model
			topicId = lastModel.getTopicId();
			topicTitle = lastModel.getTopicTitle();
			topicImgUrl = lastModel.getTopicImageUrl();
			
			startResultsFromLastModel( lastModel );
		}
		else
		{
			// this is the first run... get the values from the intent
			topicId = resultDetailsIntent.getStringExtra( "topicId" );
			topicTitle = resultDetailsIntent.getStringExtra( "topicTitle" );
			topicImgUrl = resultDetailsIntent.getStringExtra( "topicImgUrl" );
			
			startResults( rankedResultResponses, topicId, topicTitle, topicImgUrl );
		}
		Log.d( Const.TAG, String.format( "resuming ShowResultsActivity (%s, id: %s)",
				topicTitle, topicId ) );
		
		
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
	}
	
	protected void setupTopicHeader( String topicTitle, String topicImgUrl )
	{
		TextView topicTitleView = (TextView) findViewById( R.id.topicTitle );
		topicTitleView.setText( topicTitle );
		
		ImageView topicImgView = (ImageView) findViewById( R.id.topicImage );
		
		ImageManager.getInstance().getTopicImage( this, topicImgView, topicImgUrl );
	}
	
	protected View createView()
	{
		// first inflate the layout to get the main viewgroup
		final LayoutInflater inflater = getLayoutInflater();

		final View mainLayout = inflater.inflate( R.layout.show_results, null );
		FrameLayout resultsContentContainer = (FrameLayout) 
						mainLayout.findViewById( R.id.resultsContentContainer );
		final View resultsContent = inflater.inflate( R.layout.topic_content_layout,
				resultsContentContainer );

		// disable focusing on the actual list item views (instead contain the
		// focusing to the pseudo-button views within the list views)
		final ListView resultsList = (ListView) resultsContent.findViewById( R.id.responseLayout );
		resultsList.setItemsCanFocus( false );
		
		// temporarily hide the topic title textview
		// otherwise it just sits there displaying "false"
		// until the API call returns and the topic is loaded
		TextView resultTitle = (TextView) mainLayout.findViewById( R.id.topicTitle );
		resultTitle.setText( "" );
		
		final TextView resultText = (TextView) resultsContent.findViewById( R.id.questionText );
		resultText.setText( "" );
		
		final View responseLayout = resultsContent.findViewById( R.id.questionAndResponsesLayout );

		final TextView titleText = (TextView) responseLayout.findViewById( R.id.questionText );
		titleText.setText( R.string.resultsTitleText );	
		
		return mainLayout;
	}
	
	private void setupModel( String topicTitle, String topicId, String topicImgUrl )
	{
		model = new ShowResultsModel( topicId, topicTitle, topicImgUrl );
	}
	
	@SuppressWarnings( "unchecked" )
	private void startResultsFromLastModel( final ShowResultsModel aModel )
	{
		setContentView( createView() );
		
		final ListView resultItemsLayout = (ListView) findViewById( R.id.responseLayout );
		
		setupTopicHeader( aModel.getTopicTitle(), aModel.getTopicImageUrl() );
		
		// I do love generics but boy do they cause some ugly casts on occasion... 
		List< ? > modelListForAdapter = (List<?>) aModel.getModelList();
		resultItemsLayout.setAdapter( new ResultListAdapter( this, 
				(List< ResultStub >) modelListForAdapter ) );
	}
	
	private void startResults( final String results, final String topicId,
			final String topicTitle, final String topicImgUrl )
	{
		setContentView( createView() );
		
		// unfortunately because of the way the API and the app interact,
		// a double API call is needed before any results are shown, and 
		// an additional API call for each result... ugly
		startProgressDialog();
		
		HunchAPI.getInstance().rankedResults( topicId, results, null, new HunchRankedResults.Callback()
		{
			
			@Override
			public void callComplete( HunchRankedResults h )
			{
				int curSize = model.size();
				model.setCapacity( h.getAllResults().size() );
				Log.d( Const.TAG, "ensuring model list capacity (old size: " + curSize
						+ ", new ensured size: " + h.getAllResults().size() + ", actual size: " +
								model.size() + ")" );
				
				final ListView resultItemsLayout = (ListView) findViewById( R.id.responseLayout );
				
				resultItemsLayout.setAdapter( new ResultListAdapter( ShowResultsActivity.this,
						h.getAllResults() ) );
				
				dismissProgressDialog();
			}
		} );
		
		setupTopicHeader( topicTitle, topicImgUrl );
		setupModel( topicTitle, topicId, topicImgUrl );		
		
	}
	
	private void startProgressDialog()
	{
		// let the user know we're loading
		if( progress == null )
		{
			progress = new ProgressDialog( this );
			progress.setIndeterminate( true );
			progress.setTitle( "Wait just a second..." );
			progress.setMessage( "Getting your results" );
			progress.show();
		}
	}
	
	private void dismissProgressDialog()
	{
		if( progress.isShowing() )
		{
			progress.dismiss();
		}
	}
	
	private void resultDetails( String resultId )
	{
		//latestResultDetailsId = result.getId();
		Intent resultDetailsIntent = new Intent( this, ResultDetailsActivity.class );
		resultDetailsIntent.putExtra( "resultId", resultId );
		
		startActivity( resultDetailsIntent );
		
		
		//Log.d( TAG, "show result with ID#" + result.getId() );
	}
}
