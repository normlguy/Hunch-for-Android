package com.hunch;

import org.json.*;

/**
 * 
 * 
 * @author Tyler Levine Oct 25, 2009
 * 
 */
public class HunchTopic extends HunchObject
{
	
	private static Builder b;

	static class Builder extends HunchObject.Builder
	{
		private JSONObject _val;
		private int buildId;
		private String buildDecision, 
					   buildImageUrl,
					   buildResultType,
					   buildUrlName,
					   buildShortName,
					   buildHunchUrl;
		private boolean buildIsEitherOr;
		private HunchCategory buildCategory;

		// instance control
		private Builder()
		{
		}

		@Override
		Builder init( JSONObject val )
		{
			this._val = val;
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
		
		Builder setHunchUrl( String hunchUrl )
		{
			this.buildHunchUrl = hunchUrl;
			return this;
		}
		
		Builder setCategory( HunchCategory category )
		{
			this.buildCategory = category;
			return this;
		}

		@Override
		void reset()
		{
			_val = null;
			buildId = Integer.MIN_VALUE;
			buildDecision = null;
			buildImageUrl = null;
			buildResultType = null;
			buildUrlName = null;
			buildShortName = null;
			buildHunchUrl = null;
			buildIsEitherOr = false;
			buildCategory = null;
		}

		@Override
		HunchTopic build()
		{
			if ( _val == null ||
				 buildId == Integer.MIN_VALUE ||
				 buildDecision == null ||
				 buildImageUrl == null ||
				 buildResultType == null ||
				 buildUrlName == null ||
				 buildShortName == null )
			{
				throw new IllegalStateException(
						"Not all required fields set before building HunchTopic!" );
			}

			HunchTopic ht = new HunchTopic( _val,
											buildId,
											buildDecision,
											buildImageUrl,
											buildResultType,
											buildUrlName,
											buildShortName,
											buildIsEitherOr,
											buildHunchUrl,
											buildCategory );
			reset();

			return ht;
		}
	}
	
	private final int _id;
	private final String _decision, _imageUrl, _resultType, _urlName, _shortName;
	private final boolean _isEitherOr;
	private final JSONObject json;
	private String _hunchUrl; // optional
	private HunchCategory _category;
	
	private HunchTopic( JSONObject val,
						int id,
						String decision,
						String imageUrl,
						String resultType,
						String urlName,
						String shortName,
						boolean isEitherOr,
						String hunchUrl,
						HunchCategory category )
	{
		json = val;
		_id = id;
		_decision = decision;
		_imageUrl = imageUrl;
		_resultType = resultType;
		_urlName = urlName;
		_shortName = shortName;
		_isEitherOr = isEitherOr;
		_hunchUrl = hunchUrl;
		_category = category;
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
	
	public String getHunchUrl()
	{
		return _hunchUrl;
	}

	public boolean isEitherOr()
	{
		return _isEitherOr;
	}
	
	public HunchCategory getCategory()
	{
		return _category;
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
			id = Integer.parseInt( topic.getString( "id" ) );
			eitherOr = Integer.parseInt( topic.getString( "eitherOrTopic" ) );
			
			builder.init( topic )
			.setId( id )
			.setDecision( topic.getString( "decision" ) )
			.setImageUrl( topic.getString( "imageUrl" ) )
			.setResultType( topic.getString( "resultType" ) )
			.setUrlName( topic.getString( "urlName" ) )
			.setShortName( topic.getString( "shortName" ) )
			.setIsEitherOr( eitherOr == 1 );
		} catch ( NumberFormatException e )
		{
			throw new RuntimeException( "could not build HunchTopic!", e );
		} catch ( JSONException e )
		{
			throw new RuntimeException( "could not build HunchTopic", e );
		}
		
		return builder.build();
	}

}
