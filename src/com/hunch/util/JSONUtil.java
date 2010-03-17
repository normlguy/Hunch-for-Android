package com.hunch.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * 
 * @author Tyler Levine
 * Feb 1, 2010
 *
 */
public final class JSONUtil
{
	public static String printFormat( JSONObject o )
	{
		String ret;
		try
		{
			ret = o.toString( 4 );
		} catch ( JSONException e )
		{
			throw new RuntimeException( e );
		}
		
		return ret;
	}
}
