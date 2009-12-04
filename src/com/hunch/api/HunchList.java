package com.hunch.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.json.JSONObject;


/**
 * 
 * 
 * @author Tyler Levine
 * Oct 25, 2009
 *
 */
public class HunchList< T extends HunchObject > extends HunchObject implements List<T>
{
	
	private final List< T > list;
	//private final JSONArray json;
	
	public HunchList()
	{
		 list = new ArrayList< T >();
	}

	@Override
	public boolean add( T object )
	{
		return list.add( object );
	}


	@Override
	public void add( int location, T object )
	{
		list.add( location, object );
		
	}

	@Override
	public boolean addAll( Collection< ? extends T > arg0 )
	{
		return list.addAll( arg0 );
	}

	@Override
	public boolean addAll( int arg0, Collection< ? extends T > arg1 )
	{
		return list.addAll( arg0, arg1 );
	}

	@Override
	public void clear()
	{
		list.clear();		
	}

	@Override
	public boolean contains( Object object )
	{
		return list.contains( object );
	}

	@Override
	public boolean containsAll( Collection< ? > arg0 )
	{
		return list.containsAll( arg0 );
	}

	@Override
	public T get( int location )
	{
		return list.get( location );
	}

	@Override
	public int indexOf( Object object )
	{
		return list.indexOf( object );
	}


	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Override
	public Iterator< T > iterator()
	{
		return list.iterator();
	}

	@Override
	public int lastIndexOf( Object object )
	{
		return list.lastIndexOf( object );
	}

	@Override
	public ListIterator< T > listIterator()
	{
		return list.listIterator();
	}

	@Override
	public ListIterator< T > listIterator( int location )
	{
		return list.listIterator( location );
	}

	@Override
	public T remove( int location )
	{
		return list.remove( location );
	}

	@Override
	public boolean remove( Object object )
	{
		return list.remove( object );
	}


	@Override
	public boolean removeAll( Collection< ? > arg0 )
	{
		return list.removeAll( arg0 );
	}

	@Override
	public boolean retainAll( Collection< ? > arg0 )
	{
		return list.retainAll( arg0 );
	}

	@Override
	public T set( int location, T object )
	{
		return list.set( location, object );
	}

	@Override
	public int size()
	{
		return list.size();
	}

	@Override
	public List< T > subList( int start, int end )
	{
		return list.subList( start, end );
	}

	@Override
	public Object[] toArray()
	{
		return list.toArray();
	}

	@Override
	public < type > type[] toArray( type[] array )
	{
		return list.toArray( array );
	}

	@Override
	public JSONObject getJSON()
	{
		throw new UnsupportedOperationException( "getJSON() not supported on HunchList objects!" );
	}
	
	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder( "=HunchList=\n{\n" );
		
		for( T val : this )
		{
			b.append( val.toString() );
			b.append( "\n" );
		}
		
		b.append( "}" );
		
		return b.toString();
	}

}
