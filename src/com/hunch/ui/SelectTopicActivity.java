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

import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.hunch.Const;
import com.hunch.ImageManager;
import com.hunch.R;
import com.hunch.api.HunchAPI;
import com.hunch.api.HunchTopic;
import com.hunch.api.IHunchTopic;
import com.hunch.util.InfiniteListAdapter;

/**
 * 
 * 
 * @author Tyler Levine Dec 6, 2009
 * 
 */
public class SelectTopicActivity extends ListActivity
{

	private class TopicListAdapter extends InfiniteListAdapter< IHunchTopic >
	{
		//private final Context context;
		private final LayoutInflater inflater;
		
		private final static int TOPICS_SHOWN_ON_LOAD = 30;
		private final static int TOPICS_ADDED_ON_EXPANSION = 7;
		
		public TopicListAdapter( Context context, List< IHunchTopic > items )
		{
			super( items, TOPICS_SHOWN_ON_LOAD, TOPICS_ADDED_ON_EXPANSION );
			
			//this.context = context;
			inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		}

		// this method decides when we should load more items
		// into the list
		@Override
		protected boolean shouldLoadInline( int curPos, int size )
		{
			boolean b = ( curPos > size - 4 );
			//Log.d( Const.TAG, String.format( "shouldLoadInline( %d, %d ) -> %b [TopicListAdapter]",
			//		curPos, size, b ) );
			// load more if we're within 10 of the bottom of the list
			return b;
		}

		@Override
		public View getView( int position, View convertView, ViewGroup parent )
		{
			final View listItem = inflater.inflate( R.layout.image_list_item, parent, false );
			
			final IHunchTopic item = getItem( position );
			
			setupView( item, listItem );

			tryLoadInline( position );

			return listItem;
		}
		
		private void setupView( IHunchTopic topic, View view )
		{
			// get handles to the UI elements we need to set
			final ImageView imageView = (ImageView) view.findViewById( R.id.itemImage );
			final TextView text = (TextView) view.findViewById( R.id.itemText );
			
			// toString will lazy-load the HunchTopic if it supports it
			text.setText( topic.toString() );
			
			// async load the topic image
			ImageManager.getInstance().getTopicImage( SelectTopicActivity.this, imageView,
					topic.getImageUrl() );
			
			addListeners( topic, view );
		}
		
		private void addListeners( final IHunchTopic topic, View view )
		{
			view.setOnClickListener( new OnClickListener()
			{

				@Override
				public void onClick( View v )
				{
					Intent topicIntent = new Intent( SelectTopicActivity.this, TopicInterviewActivity.class );
					topicIntent.putExtra( "topicId", topic.getId() );

					SelectTopicActivity.this.startActivity( topicIntent );
				}
			} );
		}

	}

	private HunchAPI api;
	private String catURLName = null;
	//private Dialog searchDialog = null;

	@Override
	public void onCreate( Bundle b )
	{
		super.onCreate( b );

		setContentView( R.layout.home_tab1_category_list );

		api = HunchAPI.getInstance();
		
		final ImageView divider = new ImageView( this );
		divider.setBackgroundColor( R.drawable.categoryListDivider );
		//final LinearLayout layout = new LinearLayout( this );
		//layout.addView( divider );
		getListView().addHeaderView( divider );

		// get the category
		catURLName = this.getIntent().getExtras().getString( "catURLName" );
		
		Log.d( Const.TAG, "onCreate()[SelectTopicActivity]" );

	}
	
	@Override
	public void onResume()
	{
		super.onResume();

		startTopicsList();
		
		Log.d( Const.TAG, "onResume()[SelectTopicActivity]" );
	}
	
	@Override
	public void onSaveInstanceState( Bundle icicle )
	{
		super.onSaveInstanceState( icicle );
		
		//icicle.putSerializable( "curState", curState );
		//icicle.putParcelable( "listData", getListView().onSaveInstanceState() );
		
		icicle.putString( "categoryURL", catURLName );
		
		//Log.d( Const.TAG, "onSaveInstanceState()[topicSelectActivity]" );
	}
	
	@Override
	public void onRestoreInstanceState( Bundle icicle )
	{
		super.onRestoreInstanceState( icicle );
		
		//curState = (State) icicle.getSerializable( "curState" );
		//getListView().onRestoreInstanceState( icicle.getParcelable( "listData" ) );
		
		catURLName = icicle.getString( "categoryURL" );
		
		//Log.d( Const.TAG, "onRestoreInstanceState()[topicSelectActivity]" );
	}
	
	private void startTopicsList()
	{
		
		api.listTopics( catURLName, Const.TOPIC_LIST_IMG_SIZE, new HunchTopic.ListCallback()
		{

			@Override
			public void callComplete( final List< IHunchTopic > resp )
			{
				
				TopicListAdapter adapter = new TopicListAdapter( SelectTopicActivity.this, resp );
				
				SelectTopicActivity.this.setListAdapter( adapter );

			}
		} );
		
		// show the loading dialog (empty listview)
		getListView().setAdapter( null );
	}
}
