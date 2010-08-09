package com.hunch.ui;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.hunch.ImageManager;
import com.hunch.R;
import com.hunch.api.HunchAPI;
import com.hunch.api.HunchCategory;
import com.hunch.util.InfiniteListAdapter;

/**
 * 
 * 
 * @author Tyler Levine
 * @since Aug 6, 2010
 *
 */
public class SelectCategoryActivity extends ListActivity
{
	private class CategoryListAdapter extends InfiniteListAdapter< HunchCategory >
	{
		//private final Context context;
		private final LayoutInflater inflater;
		
		private static final int ITEMS_SHOWN_ON_LOAD = 20;
		private static final int ITEMS_ADDED_ON_EXPANSION = 5;
		
		public CategoryListAdapter( Context context, List< HunchCategory > items )
		{
			//super( items );
			super( items, ITEMS_SHOWN_ON_LOAD, ITEMS_ADDED_ON_EXPANSION );
			//this.context = context;
			inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		}
		
		@Override
		public boolean shouldLoadInline( int curPos, int size )
		{
			return ( curPos > size - 3 );
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
			ImageManager.getInstance().getCategoryImage( SelectCategoryActivity.this, imageView,
					cat.getImageUrl() );
			
			addListeners( cat, view );
		}
		
		private void addListeners( final HunchCategory cat, View view )
		{
			view.setOnClickListener( new OnClickListener()
			{
				
				@Override
				public void onClick( View v )
				{
					Intent topicIntent = new Intent( SelectCategoryActivity.this,
							SelectTopicActivity.class );
					topicIntent.putExtra( "catURLName", cat.getUrlName() );
					SelectCategoryActivity.this.startActivity( topicIntent );
				}
			} );
		}
	}

	@Override
	public void onCreate( Bundle b )
	{
		super.onCreate( b );

		setContentView( R.layout.home_tab1_category_list );

		//api = HunchAPI.getInstance();
	
		final ImageView divider = new ImageView( this );
		divider.setBackgroundColor( R.drawable.categoryListDivider );
		//final LinearLayout layout = new LinearLayout( this );
		//layout.addView( divider );
		getListView().addHeaderView( divider );
		
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		startCategoryList();
	}
	
	protected void startCategoryList()
	{
		HunchAPI.getInstance().listCategories( new HunchCategory.ListCallback()
		{
			
			@Override
			public void callComplete( List< HunchCategory > h )
			{
				CategoryListAdapter adapter = new CategoryListAdapter( SelectCategoryActivity.this, h );
				
				SelectCategoryActivity.this.setListAdapter( adapter );
				
			}
		} );
		
		// empty the list to show the loading animation
		getListView().setAdapter( null );
	}
}
