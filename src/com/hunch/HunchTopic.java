package com.hunch;

import org.json.simple.JSONObject;

/**
 * 
 * 
 * @author Tyler Levine Oct 25, 2009
 * 
 */
public class HunchTopic extends HunchObject
{
	
	private static Builder b;

	static class Builder
	{
		private JSONObject val;
		private int buildId;
		private String buildDecision, buildImageUrl, buildResultType,
				buildUrlName, buildShortName;
		private boolean buildIsEitherOr;

		// instance control
		protected Builder()
		{
		}

		Builder init( JSONObject val )
		{
			this.val = val;
			return this;
		}

		Builder setId( int id )
		{
			this.buildId = id;
			return this;
		}

		Builder setDecision( String decision )
		{
			this.buildDecision = decision;
			return this;
		}

		Builder setImageUrl( String imageUrl )
		{
			this.buildImageUrl = imageUrl;
			return this;
		}

		Builder setResultType( String resultType )
		{
			this.buildResultType = resultType;
			return this;

		}

		Builder setUrlName( String urlName )
		{
			this.buildUrlName = urlName;
			return this;
		}

		Builder setShortName( String shortName )
		{
			this.buildShortName = shortName;
			return this;
		}

		Builder setIsEitherOr( boolean isEitherOr )
		{
			this.buildIsEitherOr = isEitherOr;
			return this;
		}

		void reset()
		{
			val = null;
			buildId = Integer.MIN_VALUE;
			buildDecision = null;
			buildImageUrl = null;
			buildResultType = null;
			buildUrlName = null;
			buildShortName = null;
			buildIsEitherOr = false;
		}

		HunchTopic build()
		{
			if ( val == null || buildId == Integer.MIN_VALUE
							 || buildDecision == null 	 || buildImageUrl == null
							 || buildResultType == null  || buildUrlName == null
							 || buildShortName == null )
			{
				throw new IllegalStateException(
						"Not all required fields set before building HunchTopic!" );
			}

			HunchTopic ht = new HunchTopic( val, buildId, buildDecision, buildImageUrl, buildResultType, buildUrlName, buildShortName, buildIsEitherOr );
			reset();

			return ht;
		}
	}
	
	private final int _id;
	private final String _decision, _imageUrl, _resultType, _urlName, _shortName;
	private final boolean _isEitherOr;
	private final JSONObject json;
	
	protected HunchTopic( JSONObject val, int id, String decision, String imageUrl, String resultType, String urlName, String shortName, boolean isEitherOr )
	{
		json = val;
		_id = id;
		_decision = decision;
		_imageUrl = imageUrl;
		_resultType = resultType;
		_urlName = urlName;
		_shortName = shortName;
		_isEitherOr = isEitherOr;
	}
	
	public static Builder getBuilder()
	{
		if( b == null ) b = new Builder();
		
		return b;
	}

	public int getId()
	{
		return _id;
	}

	public String getDecision()
	{
		return _decision;
	}

	public String getImageUrl()
	{
		return _imageUrl;
	}

	public String getResultType()
	{
		return _resultType;
	}

	public String getUrlName()
	{
		return _urlName;
	}

	public String getShortName()
	{
		return _shortName;
	}

	public boolean isEitherOr()
	{
		return _isEitherOr;
	}

	@Override
	public JSONObject getJSON()
	{
		return json;
	}
	
	@Override
	public String toString()
	{
		return getShortName();
	}
	
	static HunchTopic buildFromJSON( JSONObject topic )
	{
		
		HunchTopic.Builder builder = getBuilder();
		
		// build the HunchTopic object
		int id = Integer.MIN_VALUE;
		int eitherOr = Integer.MIN_VALUE;
		try
		{
			id = Integer.parseInt( topic.get( "id" ).toString() );
			eitherOr = Integer.parseInt( topic.get( "eitherOrTopic" ).toString() );
		} catch ( NumberFormatException e )
		{
			e.printStackTrace();
		}
		finally
		{	
			builder.init( topic ).setId( id ).setDecision( topic.get( "decision" ).toString() );
			builder.setImageUrl( topic.get( "imageUrl" ).toString() );
			builder.setResultType( topic.get( "resultType" ).toString() );
			builder.setUrlName( topic.get( "urlName" ).toString() );
			builder.setShortName( topic.get( "shortName" ).toString() );
			builder.setIsEitherOr( ( eitherOr == 1 ) ? true : false );
		}
		
		return builder.build();
	}

}
