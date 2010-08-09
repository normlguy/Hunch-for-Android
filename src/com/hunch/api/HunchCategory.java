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

package com.hunch.api;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class HunchCategory extends HunchObject
{
	private static Builder b;
	
	public interface Callback
	{
		public void callComplete( HunchCategory h );
	}
	
	public interface ListCallback
	{
		public void callComplete( List< HunchCategory > h );
	}
	
	static class Builder extends HunchObject.Builder
	{
		private JSONObject val;
		private String buildName, buildUrlName, buildImageUrl;
		
		// instance control
		private Builder() {}
		
		@Override
		Builder init( JSONObject j )
		{
			val = j;
			return this;
		}
		
		Builder setName( String name )
		{
			buildName = name;
			return this;
		}
		
		Builder setUrlName( String urlName )
		{
			buildUrlName = urlName;
			return this;
		}
		
		Builder setImageUrl( String imageUrl )
		{
			buildImageUrl = imageUrl;
			return this;
		}
		
		@Override
		void reset()
		{
			val = null;
			buildName = null;
			buildUrlName = null;
			buildImageUrl = null;
		}
		
		@Override
		HunchCategory build()
		{
			if( val == null )
				throw new IllegalStateException( "Not all required fields set before building HunchCategory!" );
			else if( buildName == null )
				throw new IllegalStateException( "Not all required fields set before building HunchCategory!" );
			else if( buildUrlName == null )
				throw new IllegalStateException( "Not all required fields set before building HunchCategory!" );
			else if( buildImageUrl == null )
				throw new IllegalStateException( "Not all required fields set before building HunchCategory!" );
			
			HunchCategory ret = new HunchCategory( val, buildName, buildUrlName, buildImageUrl );
			reset();
			
			return ret;
		}
	}
	

	private final String _name, _urlName, _imageUrl;
	private final JSONObject json;
	
	protected HunchCategory( JSONObject jsonObj, String name, String urlName, String imageUrl )
	{
		json = jsonObj;
		_name = name;
		_urlName = urlName;
		_imageUrl = imageUrl;
	}
	
	public static Builder getBuilder()
	{
		if( b == null ) b = new Builder();
		
		return b;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String getUrlName()
	{
		return _urlName;
	}
	
	public String getImageUrl()
	{
		return _imageUrl;
	}
	
	@Override
	public JSONObject getJSON()
	{
		return json;
	}
	
	@Override
	public String toString()
	{
		return getName();
	}
	
	static HunchCategory buildFromJSON( JSONObject json )
	{
		HunchCategory.Builder builder = getBuilder();
		
		// build the HunchCategory object
		try
		{
			builder.init( json )
			.setName( json.getString( "name" ) )
			.setImageUrl( json.getString( "imageUrl" ) )
			.setUrlName( json.getString( "urlName" ) );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "could not build HunchCategory!", e );
		}
		
		return builder.build();
	}
}
