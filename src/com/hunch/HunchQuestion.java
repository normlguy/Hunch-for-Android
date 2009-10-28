package com.hunch;

import org.json.simple.JSONObject;

public class HunchQuestion extends HunchObject
{
	
	private static Builder b;
	
	static class Builder extends HunchObject.Builder
	{
		
		private JSONObject val;
		private int __id, __topicId;
		private String __text, __imageUrl;
		private int[] __responseIds;
		
		protected Builder() {}
		
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
		
		Builder setText( String text )
		{
			__text = text;
			return this;
		}
		
		Builder setImageUrl( String imageUrl )
		{
			__imageUrl = imageUrl;
			return this;
		}
		
		Builder setResponseIds( int[] ids )
		{
			this.__responseIds = ids;
			return this;
		}

		@Override
		void reset()
		{
			val = null;
			__text = null;
			__imageUrl = null;
			__responseIds = null;
			__id = __topicId = Integer.MIN_VALUE;
		}
		
		@Override
		HunchQuestion build()
		{
			if( val == null || __text == null || __imageUrl == null || __responseIds == null
					|| __id == Integer.MIN_VALUE || __topicId == Integer.MIN_VALUE )
			{
				throw new IllegalStateException( "Not all required fields set before building HunchQuestion!" );
			}
			
			HunchQuestion ret = new HunchQuestion( val, __text, __imageUrl, __id, __topicId, __responseIds );
			reset();
			
			return ret;
		}
	}
	
	private final JSONObject json;
	private final String _text, _imageUrl;
	private final int _id, _topicId;
	private final int[] _responseIds;
	
	protected HunchQuestion( JSONObject jsonObj, String text, String imageUrl,
			int id, int topicId, int[] responseIds )
	{
		json = jsonObj;
		_text = text;
		_imageUrl = imageUrl;
		_id = id;
		_topicId = topicId;
		_responseIds = responseIds;
	}
	
	public static Builder getBuilder()
	{
		if( b == null ) b = new Builder();
		
		return b;
	}
	
	public String getText()
	{
		return _text;
	}
	
	public String getImageUrl()
	{
		return _imageUrl;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getTopicId()
	{
		return _topicId;
	}
	
	public int[] getResponseIds()
	{
		return _responseIds;
	}

	@Override
	public JSONObject getJSON()
	{
		return json;
	}

}
