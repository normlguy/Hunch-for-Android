package com.hunch.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hunch.Const;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * This adapter provides all of the facilities needed for infinitely loading lists.
 * 
 * Make sure you call tryLoadInline( int ) from getView(), otherwise the list won't know
 * when to append more items. It is also recommended you override shouldLoadInline( int, int )
 * to decide when to append more items to the list.
 * 
 * @author Tyler Levine
 * @since Mar 15, 2010
 *
 */
public abstract class InfiniteListAdapter<T> extends BaseAdapter
{
	
	public static final int DEFAULT_ITEMS_SHOWN_ON_LOAD = 30;
	public static final int DEFAULT_ITEMS_ADDED_ON_EXPANSION = 10;
	
	private final List<T> items;
	private final List<T> displayedItems;
	
	//private final int itemsShownOnLoad;
	private final int itemsAddedOnExpansion;
	
	/**
	 * Creates a new InfiniteListAdapter with default settings.
	 * 
	 * @param items The list of items that backs this Adapter.
	 */
	public InfiniteListAdapter( final List<T> items )
	{
		this( items, DEFAULT_ITEMS_SHOWN_ON_LOAD, DEFAULT_ITEMS_ADDED_ON_EXPANSION );
	}
	
	/**
	 * Creates a new InfiniteListAdapter with specified settings.
	 * 
	 * @param items The list of items that backs this Adapter.
	 * @param itemsShownOnLoad The number of items displayed at first.
	 * @param itemsAddedOnExpansion The number of items added to the list on each expansion.
	 */
	public InfiniteListAdapter( final List<T> items, int itemsShownOnLoad, int itemsAddedOnExpansion )
	{
		// keep the parameters
		this.items = items;
		//this.itemsShownOnLoad = itemsShownOnLoad;
		this.itemsAddedOnExpansion = itemsAddedOnExpansion;
		
		// show some of the items at first
		// but make sure we have enough items to show.
		if( items.size() >= itemsShownOnLoad )
		{
			displayedItems = new ArrayList<T>( itemsShownOnLoad );
			displayedItems.addAll( items.subList( 0, itemsShownOnLoad ) );
		}
		// Otherwise, just show as many as we can.
		else
		{
			displayedItems = new ArrayList<T>( items.size() );
			displayedItems.addAll( items.subList( 0, items.size() ) );
		}
	}
	
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount()
	{
		return displayedItems.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public T getItem( int position )
	{
		return displayedItems.get( position );
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId( int position )
	{
		return position;
		//T item = displayedItems.get( position );
		//if( item != null )
		//	return displayedItems.get( position ).hashCode();
		//else
		//	return -1;
	}
	
	/**
	 * When you inherit from this class, call this method in
	 * your getView() method. The list will not expand otherwise.
	 * 
	 * This method calls shouldLoadInline( int, int ) to decide if more items
	 * should be loaded.
	 * 
	 * @param curPos the current view position in the list
	 */
	protected void tryLoadInline( int curPos )
	{
		// we're gonna need this a few times
		final int itemsDisplayed = displayedItems.size();
		
		// if we don't want to load any more items inline, bail
		if( !shouldLoadInline( curPos, itemsDisplayed ) )
			return;
		
		// find the number of items to add.
		// either add the number of items specified in the constructor,
		// or if there aren't that many, add whatever is left
		int numItemsToAdd = ( itemsAddedOnExpansion < items.size() - itemsDisplayed ) ?
							  itemsAddedOnExpansion : items.size() - itemsDisplayed;
		
		// bail if there's nothing to do (bottom of the list)
		if( numItemsToAdd == 0 )
			return;
		
		List<T> itemsToAdd = items.subList( itemsDisplayed, itemsDisplayed + numItemsToAdd );
		
		Log.d( Const.TAG, "adding " + numItemsToAdd + " items [InfiniteListAdapter]" );
		Log.d( Const.TAG, "\tfrom list (" + itemsToAdd.size() + ") " + itemsToAdd.toString() );
				
		displayedItems.addAll( itemsToAdd );
		
		notifyDataSetChanged();
	}
	
	/**
	 * This method decides when we should load more items inline.
	 * 
	 * @param curPos The index of the last item visible in the list.
	 * @param size The total size of the list.
	 * @return true to load more items, false otherwise.
	 */
	protected abstract boolean shouldLoadInline( int curPos, int size );

	
	/**
	 * Provides raw access to full items list (whether those items are currently
	 * visible or not) to subclasses. Avoid using this unless you need to
	 * access elements of the list that are not currently displayed in the list.
	 * You can use getItem( int ) for most purposes.
	 * 
	 * @return The list that backs this adapter.
	 */
	protected List<T> getItems()
	{
		return items;
	}

	/**
	 * This method must be overridden by any sub class (duh, it's abstract). It generates the
	 * views for each item that will appear in the list.
	 * 
	 * It is important to call <code>super.tryLoadInline( int )</code> in this method
	 * to let the list try to expand when appropriate. You can decide when to expand by
	 * overriding <code>shouldLoadInline( int, int )</code>
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	public abstract View getView( int position, View convertView, ViewGroup parent );	

}
