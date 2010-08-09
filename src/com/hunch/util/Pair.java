/*
 * Copyright 2009, 2010 Tyler Levine
 * Portions copyright 2009 Google, Inc.
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
