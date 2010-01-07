package com.hunch.util;

/**
 * My implementation of API Level 5's Pair class.
 * 
 * @author Tyler Levine
 * Jan 6, 2010
 *
 */
public class Pair< F, S >
{
	public final F first;
	public final S second;
	
	public Pair( F first, S second )
	{
		this.first = first;
		this.second = second;
	}
	
	public static < A, B > Pair< A, B > create( A a, B b )
	{
		return new Pair< A, B >( a, b );
	}
	
	@Override
	public boolean equals( Object o )
	{
		if( !(o instanceof Pair< ?, ? >) ) return false;
		
		if( first == null )
			throw new NullPointerException( "first field of pair is null!" );
		
		if( second == null )
			throw new NullPointerException( "second field of pair is null!" );
			
		return  first.equals( ((Pair< ?, ? >) o).first ) 
			&& second.equals( ((Pair< ?, ? >) o).second );
	}
	
	@Override
	public int hashCode()
	{
		if( first == null )
			throw new NullPointerException( "first field of pair is null!" );
		
		if( second == null )
			throw new NullPointerException( "second field of pair is null!" );
		
		int result = 17;
		result = 31 * result + first.hashCode();
		result = 31 * result + second.hashCode();
		
		return result;
	}
}
