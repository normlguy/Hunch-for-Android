package com.hunch.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

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
	interface DrawableWithText
	{
		public void setDrawableAsync( ImageView v );

		public String text();
	}

	private class TopicListAdapter extends InfiniteListAdapter<DrawableWithText>
	{
		private final Context _context;
		
		public TopicListAdapter( Context context, List<DrawableWithText> items )
		{
			super( items );
			_context = context;
			
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
			final LayoutInflater inflater = (LayoutInflater) _context
					.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

			final View listItem = inflater.inflate( R.layout.image_list_item, null );
			final ImageView imageView = (ImageView) listItem.findViewById( R.id.itemImage );
			final TextView text = (TextView) listItem.findViewById( R.id.itemText );
			
			final DrawableWithText item = getItem( position );
			
			text.setText( item.text() );
			item.setDrawableAsync( imageView );

			tryLoadInline( position );

			return listItem;
		}

		
		public class SimpleDrawableWithText implements DrawableWithText
		{
			protected final String _text;
			protected final Drawable _drawable;

			public SimpleDrawableWithText( final String text, final Drawable drawable )
			{
				_text = text;
				_drawable = drawable;
			}

			@Override
			public void setDrawableAsync( ImageView v )
			{
				v.setImageDrawable( _drawable );
			}

			@Override
			public String text()
			{
				return _text;
			}
			
		}
	}
	
	private final class EagerUrlDrawableWithText implements DrawableWithText
	{
		private final String _url, _text;
		
		public EagerUrlDrawableWithText( String text, String url )
		{
			_text = text;
			_url = url;
		}

		/* (non-Javadoc)
		 * @see com.hunch.ui.ImageTextListAdapter.DrawableWithText#setDrawableAsync(android.widget.ImageView)
		 */
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

		/* (non-Javadoc)
		 * @see com.hunch.ui.ImageTextListAdapter.DrawableWithText#text()
		 */
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
	};

	private HunchAPI api;
	private List< HunchCategory > catList;
	private List< IHunchTopic > topicList;
	private TopicListAdapter adapter;
	//private ListAdapter loadingAdapter;

	@Override
	public void onCreate( Bundle b )
	{
		super.onCreate( b );

		setContentView( R.layout.home_tab1_category_list );

		//loadingAdapter = new ArrayAdapter< String >( this, R.layout.loading_list_item,
		//		R.id.loadingItemText, new String[] { "Loading..." } );

		api = HunchAPI.getInstance();

		getListView().setOnItemClickListener( new OnItemClickListener()
		{

			public void onItemClick( final AdapterView< ? > parent, final View view,
					final int position, final long id )
			{
				final HunchCategory cat = catList.get( position );

				api.listTopics( cat.getUrlName(), "32x32", new HunchTopic.ListCallback()
				{

					@Override
					public void callComplete( List< IHunchTopic > resp )
					{
						topicList = resp;

						List< DrawableWithText > topicAdapterList =	new ArrayList< DrawableWithText >();

						for ( IHunchTopic topic : topicList )
						{
							topicAdapterList.add( new LazyTopicDrawableWithText< IHunchTopic >( topic ) );
						}
						
						adapter = new TopicListAdapter( TopicSelectActivity.this, topicAdapterList );
						
						TopicSelectActivity.this.setListAdapter( adapter );

						TopicSelectActivity.this.getListView().setOnItemClickListener( new OnItemClickListener()
						{

							@Override
							public void onItemClick( final AdapterView< ? > parent, final View view,
									final int pos, final long id )
							{
								final IHunchTopic topic = topicList.get( pos );

								//Bundle b = new Bundle();
								//b.putString( "topicId", String.valueOf( topic.getId() ) );

								Intent topicIntent = new Intent( TopicSelectActivity.this, PlayTopicActivity.class );
								topicIntent.putExtra( "topicId", topic.getId() );

								TopicSelectActivity.this.startActivity( topicIntent );
							}
						} );

					}
				} );
				
				// empty the list so the loading animation shows
				getListView().setAdapter( null );

			}
		} );

		api.listCategories( new HunchCategory.ListCallback()
		{
			
			@Override
			public void callComplete( List< HunchCategory > h )
			{
				catList = h;
				
				List< DrawableWithText > catAdapterList = new ArrayList< DrawableWithText >();

				for ( HunchCategory cat : catList )
				{
					catAdapterList.add( new EagerUrlDrawableWithText( cat.toString(), cat.getImageUrl() ) );
				}
				adapter = new TopicListAdapter( TopicSelectActivity.this, catAdapterList );

				TopicSelectActivity.this.setListAdapter( adapter );			
			}
		});


	}

}
