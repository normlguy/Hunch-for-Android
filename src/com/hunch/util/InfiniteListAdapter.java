package com.hunch.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * 
 * 
 * @author Tyler Levine
 * @since Mar 15, 2010
 *
 */
public abstract class InfiniteListAdapter<T> extends BaseAdapter
{
	
	public static final int DEFAULT_ITEMS_SHOWN_ON_LOAD = 30;
	public static final int DEFAULT_ITEMS_ADDED_ON_EXPANSION = 15;
	
	private final List<T> items;
	private final List<T> displayedItems;
	
	//private final int itemsShownOnLoad;
	private final int itemsAddedOnExpansion;
	
	
	public InfiniteListAdapter( final List<T> items )
	{
		this( items, DEFAULT_ITEMS_SHOWN_ON_LOAD, DEFAULT_ITEMS_ADDED_ON_EXPANSION );
	}
	
	public InfiniteListAdapter( final List<T> items, int itemsShownOnLoad, int itemsAddedOnExpansion )
	{
		// keep the parameters
		this.items = items;
		//this.itemsShownOnLoad = itemsShownOnLoad;
		this.itemsAddedOnExpansion = itemsAddedOnExpansion;
		
		// show some of the items at first
		// but make sure we have enough items to show.
		// Otherwise, just show as many as we can.
		if( items.size() >= itemsShownOnLoad )
		{
			displayedItems = new ArrayList<T>( itemsShownOnLoad );
			displayedItems.addAll( items.subList( 0, itemsShownOnLoad ) );
		}
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
		return items.get( position );
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId( int position )
	{
		return items.get( position ).hashCode();
	}
	
	/*
	 * When you inherit from this class, call this method in
	 * your getView() method. The list will not expand otherwise.
	 * 
	 * This method calls shouldLoadInline() to decide if more items
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
		
		displayedItems.addAll( items.subList( itemsDisplayed, itemsDisplayed + numItemsToAdd ) );
	}
	
	/*
	 * Override this method. It decides when we should load more items inline.
	 * 
	 * The default implementation returns true once the curPos is 90% or more than size.
	 * 
	 * @param curPos The index of the last item visible in the list.
	 * @param size The total size of the list.
	 * @returns Whether or not to load more items inline.
	 */
	protected boolean shouldLoadInline( int curPos, int size )
	{		
		return curPos >= Math.round( size * 0.9f );
	}
	
	protected List<T> getItems()
	{
		return items;
	}

	/*
	 * This method must be overridden by any sub class (duh, it's abstract). It generates the
	 * views for each item that will appear in the list.
	 * 
	 * It is important to call <code>super.tryLoadInline( int )</code> in this method
	 * to let the list try to expand when appropriate. You can decide when to expand by
	 * overriding shouldLoadInline( int, int )
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	public abstract View getView( int position, View convertView, ViewGroup parent );	

}
