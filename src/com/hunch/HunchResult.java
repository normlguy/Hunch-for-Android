package com.hunch;

import org.json.*;

/**
 * 
 * 
 * @author Tyler Levine
 * Nov 5, 2009
 *
 */
public class HunchResult extends HunchObject
{

	private static Builder b;
	
	static class Builder extends HunchObject.Builder
	{
		
		private JSONObject val;
		private int __id, __topicId;
		private String __type, __name, __urlName, __desc, __imageUrl, __readMoreUrl, __hunchUrl;
		
		private Builder() {}
		
		@Override
		Builder init( JSONObject jsonVal )
		{
			val = jsonVal;
			return this;
		}
		
		Builder setId( int id )
		{
			__id = id;
			return this;
		}
		
		Builder setTopicId( int topicId )
		{
			__topicId = topicId;
			return this;
		}
		
		Builder setType( String type )
		{
			__type = type;
			return this;
		}
		
		Builder setImageUrl( String imageUrl )
		{
			__imageUrl = imageUrl;
			return this;
		}
		
		Builder setDescription( String desc )
		{
			__desc = desc;
			return this;
		}
		
		Builder setName( String name )
		{
			__name = name;
			return this;
		}
		
		Builder setUrlName( String urlName )
		{
			__urlName = urlName;
			return this;
		}
		
		Builder setReadMoreUrl( String readMoreUrl )
		{
			__readMoreUrl = readMoreUrl;
			return this;
		}
		
		Builder setHunchUrl( String hunchUrl )
		{
			__hunchUrl = hunchUrl;
			return this;
		}

		@Override
		void reset()
		{
			val = null;
			__name = null;
			__imageUrl = null;
			__urlName = null;
			__desc = null;
			__readMoreUrl = null;
			__hunchUrl = null;
			__id = __topicId = Integer.MIN_VALUE;
		}
		
		@Override
		HunchResult build()
		{
			if( val == null || __name == null || __imageUrl == null || __urlName == null
					|| __desc == null || __readMoreUrl == null || __hunchUrl == null
					|| __id == Integer.MIN_VALUE || __topicId == Integer.MIN_VALUE )
			{
				throw new IllegalStateException( "Not all required fields set before building HunchQuestion!" );
			}
			
			HunchResult ret = new HunchResult( val, __id, __topicId, __type, __name, __urlName,
					__desc, __imageUrl, __readMoreUrl, __hunchUrl );
			reset();
			
			return ret;
		}
	}
	
	private final JSONObject json;
	private int _id, _topicId;
	private String _type, _name, _urlName, _desc, _imageUrl, _readMoreUrl, _hunchUrl;
	
	
	private HunchResult( JSONObject jsonObj, int id, int topicId, String type, String name,
			String urlName, String desc, String imageUrl, String readMoreUrl, String hunchUrl )
	{
		json = jsonObj;
		_id = id;
		_topicId = topicId;
		_type = type;
		_name = name;
		_urlName = urlName;
		_desc = desc;
		_imageUrl = imageUrl;
		_readMoreUrl = readMoreUrl;
		_hunchUrl = hunchUrl;
		
	}
	
	public static Builder getBuilder()
	{
		if( b == null ) b = new Builder();
		
		return b;
	}
	
	public String getImageUrl()
	{
		return _imageUrl;
	}
	
	public String getType()
	{
		return _type;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String getUrlName()
	{
		return _urlName;
	}
	
	public String getDescription()
	{
		return _desc;
	}
	
	public String getReadMoreUrl()
	{
		return _readMoreUrl;
	}
	
	public String getHunchUrl()
	{
		return _hunchUrl;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getTopicId()
	{
		return _topicId;
	}
	
	static HunchResult buildFromJSON( JSONObject json )
	{
		HunchResult.Builder builder = getBuilder();
		
		// build the HunchQuestion object
		int id = Integer.MIN_VALUE, topicId = Integer.MIN_VALUE;
		try
		{
			id = Integer.parseInt( json.getString( "id" ) );
			topicId = Integer.parseInt( json.getString( "topicId" ) );
			
			builder.init( json )
			.setId( id )
			.setTopicId( topicId )
			.setImageUrl( json.getString( "imageUrl") )
			.setType( json.get( "type" ).toString() )
			.setDescription( json.get( "description" ).toString() )
			.setName( json.get( "name" ).toString() )
			.setUrlName( json.get( "urlName" ).toString() )
			.setReadMoreUrl( json.get( "readMoreUrl" ).toString() )
			.setHunchUrl( json.get( "hunchUrl" ).toString() );
			
		} catch ( NumberFormatException e )
		{
			throw new RuntimeException( "Couldn't build HunchResult!", e );
		} catch( JSONException e )
		{
			throw new RuntimeException( "Couldn't build HunchResult!", e );
		}
		
		return builder.build();
	}

	@Override
	public JSONObject getJSON()
	{
		return json;
	}

}
