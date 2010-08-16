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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hunch.ImageManager;
import com.hunch.R;
import com.hunch.api.HunchAPI;
import com.hunch.api.HunchRankedResults;
import com.hunch.api.HunchRankedResults.ResultStub;
import com.hunch.ui.ResultListAdapter.ResultModel;
import com.hunch.ui.ResultListAdapter.ResultViewHolder;

/**
 * 
 * 
 * @author Tyler Levine
 * @since Aug 9, 2010
 *
 */
public class ShowResultsActivity extends Activity
{
	private static class ShowResultsModel
	{
		private List< ResultModel > results;
				
		private final String topicId;
		private final String topicTitle;
		private final String topicImgUrl;
		
		//private static final int GROWTH_MARGIN = 10;
		
		public ShowResultsModel( List< ResultModel > modelList, TopicInfo topic )
		{
			topicId = topic.id;
			topicTitle = topic.title;;
			topicImgUrl = topic.imgUrl;
			
			results = modelList;
		}
		
		public List< ResultModel > getModelList()
		{
			return results;
		}
		
		public String getTopicId()
		{
			return topicId;
		}
		
		public String getTopicTitle()
		{
			return topicTitle;
		}
		
		public String getTopicImageUrl()
		{
			return topicImgUrl;
		}
		
	}
	
	protected static class TopicInfo
	{
		String title;
		String imgUrl;
		String id;
	}
	
	// private ShowResultsModel model;
	private ProgressDialog progress;
	private ResultListAdapter<?> adapter;
	private TopicInfo curTopicInfo;
	
	@Override
	public Object onRetainNonConfigurationInstance()
	{
		List< ResultModel > models = adapter.getAdapterData();
		ShowResultsModel model = new ShowResultsModel( models, curTopicInfo );
		return model;
	}
	
	@Override
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		
		setContentView( createView() );
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
		
		setupTopicInfo( topicTitle, topicId, topicImgUrl );
		
	}
	
	protected void setupTopicInfo( String title, String id, String imgUrl )
	{
		curTopicInfo = new TopicInfo();
		curTopicInfo.title = title;
		curTopicInfo.id = id;
		curTopicInfo.imgUrl = imgUrl;
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
	}
	
	protected void setupTopicHeader( String topicTitle, String topicImgUrl )
	{
		TextView topicTitleView = (TextView) findViewById( R.id.topic_name );
		topicTitleView.setText( topicTitle );
		
		ImageView topicImgView = (ImageView) findViewById( R.id.topic_icon );
		
		ImageManager.getInstance().getTopicImage( this, topicImgView, topicImgUrl );
	}
	
	protected View createView()
	{
		// first inflate the layout to get the main viewgroup
		final LayoutInflater inflater = getLayoutInflater();

		final View mainLayout = inflater.inflate( R.layout.results, null );
		//FrameLayout resultsContentContainer = (FrameLayout) 
		//				mainLayout.findViewById( R.id.results_list );
		final View resultsContent = mainLayout.findViewById( R.id.results_list_layout );

		// disable focusing on the actual list item views (instead contain the
		// focusing to the pseudo-button views within the list views)
		final ListView resultsList = (ListView) resultsContent.findViewById( R.id.response_list );
		
		
		//resultsList.setItemsCanFocus( false );
		setupListeners( resultsList );
		
		// temporarily hide the topic title textview
		// otherwise it just sits there displaying "false"
		// until the API call returns and the topic is loaded
		TextView resultTitle = (TextView) mainLayout.findViewById( R.id.topic_name );
		resultTitle.setText( "" );

		return mainLayout;
	}
	
	private void startResultsFromLastModel( final ShowResultsModel aModel )
	{		
		final ListView resultItemsLayout = (ListView) findViewById( R.id.response_list );
		
		setupTopicHeader( aModel.getTopicTitle(), aModel.getTopicImageUrl() );
		
		// I do love generics but boy do they cause some ugly casts on occasion...
		List< ResultModel > modelListForAdapter = aModel.getModelList();
		adapter = new ResultModelListAdapter( this, modelListForAdapter );
		resultItemsLayout.setAdapter( adapter );
	}
	
	private void startResults( final String results, final String topicId,
			final String topicTitle, final String topicImgUrl )
	{		
		// unfortunately because of the way the API and the app interact,
		// a double API call is needed before any results are shown, and 
		// an additional API call for each result... ugly
		startProgressDialog();
		
		HunchAPI.getInstance().rankedResults( topicId, results, null, new HunchRankedResults.Callback()
		{
			
			@Override
			public void callComplete( HunchRankedResults h )
			{				
				final ListView resultItemsLayout = (ListView) findViewById( R.id.response_list );
				
				adapter = new ResultModelListAdapter( ShowResultsActivity.this,
						convertStubs( h.getAllResults() ) );
				resultItemsLayout.setAdapter( adapter );
				
				dismissProgressDialog();
			}
		} );
		
		setupTopicHeader( topicTitle, topicImgUrl );		
	}
	
	private List< ResultModel > convertStubs( List< ResultStub > stubs )
	{
		List< ResultModel > models = new ArrayList< ResultModel >( stubs.size() );
		
		for( ResultStub stub : stubs )
		{
			models.add( ResultModel.createStub( stub.getId(), stub.getEitherOrPct() ) );
		}
		
		return models;
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
	
	private void setupListeners( final ListView view )
	{
		view.setOnItemClickListener( new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick( AdapterView< ? > parent, View aView, int position, long id )
			{
				ResultViewHolder viewHolder = (ResultViewHolder) aView.getTag();
				resultDetails( viewHolder.resultId );
			}
			
		} );
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
