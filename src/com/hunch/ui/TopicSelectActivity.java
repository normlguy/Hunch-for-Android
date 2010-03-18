package com.hunch.ui;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.hunch.Const;
import com.hunch.ImageCacher;
import com.hunch.R;
import com.hunch.api.HunchAPI;
import com.hunch.api.HunchCategory;
import com.hunch.api.HunchTopic;
import com.hunch.api.IHunchTopic;
import com.hunch.util.InfiniteListAdapter;

/**
 * 
 * 
 * @author Tyler Levine Dec 6, 2009
 * 
 */
public class TopicSelectActivity extends ListActivity
{
	/*interface DrawableWithText
	{
		public void setDrawableAsync( ImageView v );

		public String text();
	}*/
	
	private class CategoryListAdapter extends InfiniteListAdapter< HunchCategory >
	{
		//private final Context context;
		private final LayoutInflater inflater;
		
		public CategoryListAdapter( Context context, List< HunchCategory > items )
		{
			super( items );
			
			//this.context = context;
			inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		}
		
		@Override
		public boolean shouldLoadInline( int curPos, int size )
		{
			return ( curPos > size - 7 );
		}
		
		@Override
		public View getView( int position, View convertView, ViewGroup parent )
		{
			final View listItem = inflater.inflate( R.layout.image_list_item, parent, false );
			
			final HunchCategory item = getItem( position );
			
			setupView( item, listItem );
			
			tryLoadInline( position );
			
			return listItem;
			
		}
		
		private void setupView( HunchCategory cat, View view )
		{
			// get handles to the UI elements we need to set
			final ImageView imageView = (ImageView) view.findViewById( R.id.itemImage );
			final TextView text = (TextView) view.findViewById( R.id.itemText );
			
			// toString will lazy-load the HunchCategory if it supports it (probably not)
			text.setText( cat.toString() );
			
			// async load the category image
			ImageCacher.fromURL( cat.getImageUrl(), new ImageCacher.Callback()
			{
				@Override
				public void callComplete( Drawable d )
				{
					imageView.setImageDrawable( d );
				}
			} );
			
			addListeners( cat, view );
		}
		
		private void addListeners( final HunchCategory cat, View view )
		{
			view.setOnClickListener( new OnClickListener()
			{
				
				@Override
				public void onClick( View v )
				{
					startTopicsList( cat.getUrlName() );
				}
			} );
		}
	}

	private class TopicListAdapter extends InfiniteListAdapter< IHunchTopic >
	{
		//private final Context context;
		private final LayoutInflater inflater;
		
		public TopicListAdapter( Context context, List< IHunchTopic > items )
		{
			super( items );
			
			//this.context = context;
			inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		}

		// this method decides when we should load more items
		// into the list
		@Override
		protected boolean shouldLoadInline( int curPos, int size )
		{
			// load more if we're within 10 of the bottom of the list
			return ( curPos > size - 10 );
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
			ImageCacher.fromURL( topic.getImageUrl(), new ImageCacher.Callback()
			{
				@Override
				public void callComplete( Drawable d )
				{
					imageView.setImageDrawable( d );
				}
			} );
			
			addListeners( topic, view );
		}
		
		private void addListeners( final IHunchTopic topic, View view )
		{
			view.setOnClickListener( new OnClickListener()
			{

				@Override
				public void onClick( View v )
				{
					Intent topicIntent = new Intent( TopicSelectActivity.this, PlayTopicActivity.class );
					topicIntent.putExtra( "topicId", topic.getId() );

					TopicSelectActivity.this.startActivity( topicIntent );
				}
			} );
		}

	}
/*	
	private final class EagerUrlDrawableWithText implements DrawableWithText
	{
		private final String _url, _text;
		
		public EagerUrlDrawableWithText( String text, String url )
		{
			_text = text;
			_url = url;
		}

		 (non-Javadoc)
		 * @see com.hunch.ui.ImageTextListAdapter.DrawableWithText#setDrawableAsync(android.widget.ImageView)
		 
		@Override
		public void setDrawableAsync( final ImageView v )
		{
			ImageCacher.fromURL( _url, new ImageCacher.Callback()
			{
				@Override
				public void callComplete( Drawable d )
				{
					v.setImageDrawable( d );
				}
			} );
			
		}

		 (non-Javadoc)
		 * @see com.hunch.ui.ImageTextListAdapter.DrawableWithText#text()
		 
		@Override
		public String text()
		{
			// TODO Auto-generated method stub
			return _text;
		}
	}

	// this is a mouthful
	private final class LazyTopicDrawableWithText< T extends IHunchTopic > implements
			DrawableWithText
	{
		private final T hunchobj;
		//private Drawable d;

		public LazyTopicDrawableWithText( T obj )
		{
			hunchobj = obj;
		}

		@Override
		public String text()
		{
			// toString will lazily construct the object
			// if the class supports that.
			return hunchobj.toString();
		}

		@Override
		public void setDrawableAsync( final ImageView v )
		{
			ImageCacher.fromURL( hunchobj.getImageUrl(), new ImageCacher.Callback()
			{
				@Override
				public void callComplete( Drawable d )
				{
					v.setImageDrawable( d );
				}
			} );
		}
	};*/

	private HunchAPI api;
	//private List< HunchCategory > catList;
	//private List< IHunchTopic > topicList;
	//private TopicListAdapter adapter;
	
	private enum State
	{
		CATEGORIES, TOPICS
	}
	
	private State curState;
	private String lastCatUrlName = null;
	//private ListAdapter loadingAdapter;

	@Override
	public void onCreate( Bundle b )
	{
		super.onCreate( b );

		setContentView( R.layout.home_tab1_category_list );

		api = HunchAPI.getInstance();
		
		Log.d( Const.TAG, "onCreate()[topicSelectActivity]" );

	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		if( curState == null || curState == State.CATEGORIES )
		{
			startCategoryList();
		}
		else if( curState == State.TOPICS )
		{
			startTopicsList( lastCatUrlName );
		}
		
		Log.d( Const.TAG, "onResume()[topicSelectActivity]" );
	}
	
	@Override
	public void onSaveInstanceState( Bundle icicle )
	{
		super.onSaveInstanceState( icicle );
		
		icicle.putSerializable( "curState", curState );
		
		if( curState == State.TOPICS )
		{
			icicle.putString( "lastCatUrlName", lastCatUrlName );
		}
		
		Log.d( Const.TAG, String.format( "onSaveInstanceState( %s )[topicSelectActivity]", curState ) );
	}
	
	@Override
	public void onRestoreInstanceState( Bundle icicle )
	{
		super.onRestoreInstanceState( icicle );
		
		curState = (State) icicle.getSerializable( "curState" );
		
		if( curState == State.TOPICS )
		{
			lastCatUrlName = icicle.getString( "lastCatUrlName" );
		}
		
		Log.d( Const.TAG, String.format( "onRestoreInstanceState( %s )[topicSelectActivity]", curState ) );
	}
	
	@Override
	public boolean onKeyDown( int code, KeyEvent event )
	{
		if( code == KeyEvent.KEYCODE_BACK )
		{
			// someone hit the back button
			// if we're in topics mode, go back to
			// category mode
			if( curState == State.TOPICS )
			{
				startCategoryList();
				
				// nobody else handle this keypress please
				return true;
			}
			
		}
		
		return false;
	}
	
	private void startCategoryList()
	{
		curState = State.CATEGORIES;
		api.listCategories( new HunchCategory.ListCallback()
		{
			
			@Override
			public void callComplete( List< HunchCategory > h )
			{
				//catList = h;
				CategoryListAdapter adapter = new CategoryListAdapter( TopicSelectActivity.this, h );

				TopicSelectActivity.this.setListAdapter( adapter );			
			}
		});
		
		// empty the list so the loading animation shows
		getListView().setAdapter( null );
	}
	
	private void startTopicsList( String urlName )
	{
		lastCatUrlName = urlName;
		curState = State.TOPICS;
		
		api.listTopics( urlName, Const.TOPIC_LIST_IMAGE_SIZE, new HunchTopic.ListCallback()
		{

			@Override
			public void callComplete( final List< IHunchTopic > resp )
			{
				
				TopicListAdapter adapter = new TopicListAdapter( TopicSelectActivity.this, resp );
				
				TopicSelectActivity.this.setListAdapter( adapter );

			}
		} );
		
		// show the loading dialog (empty listview)
		getListView().setAdapter( null );
	}
}
