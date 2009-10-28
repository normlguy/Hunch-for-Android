package com.hunch;

import org.json.simple.JSONObject;

public class HunchCategory extends HunchObject
{
	private static Builder b;
	
	static class Builder extends HunchObject.Builder
	{
		private JSONObject val;
		private String buildName, buildUrlName, buildImageUrl;
		
		// instance control
		protected Builder() {}
		
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
		builder.init( json ).setName( json.get( "name" ).toString() );
		builder.setImageUrl( json.get( "imageUrl" ).toString() );
		builder.setUrlName( json.get( "urlName" ).toString() );
		
		return builder.build();
	}
}
