package com.hunch;

import org.json.*;

public class HunchCategory extends HunchObject
{
	private static Builder b;
	
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
			if( val == null || buildName == null || buildUrlName == null || buildImageUrl == null )
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
