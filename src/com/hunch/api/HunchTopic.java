package com.hunch.api;

import org.json.*;


/**
 * 
 * 
 * @author Tyler Levine Oct 25, 2009
 * 
 */
public class HunchTopic extends HunchObject
{

	private static Builder b = new Builder();

	static class Builder extends HunchObject.Builder
	{
		private JSONObject _val;
		private Integer buildId;
		private String buildDecision, buildImageUrl, buildResultType,
				buildUrlName, buildShortName, buildHunchUrl;
		private Boolean buildIsEitherOr;
		private HunchCategory buildCategory;
		private Double buildScore;

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

		Builder setId( Integer id )
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

		Builder setIsEitherOr( Boolean isEitherOr )
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

		Builder setScore( Double score )
		{
			this.buildScore = score;
			return this;
		}

		@Override
		void reset()
		{
			_val = null;
			buildId = null;
			buildDecision = null;
			buildImageUrl = null;
			buildResultType = null;
			buildUrlName = null;
			buildShortName = null;
			buildHunchUrl = null;
			buildIsEitherOr = false;
			buildCategory = null;
			buildScore = null;
		}

		@Override
		HunchTopic build()
		{
			assureBuildParams();

			return buildInternal();

		}

		HunchTopic buildForSearch()
		{
			assureSearchBuildParams();

			return buildInternal();
		}
		
		HunchTopic buildForNextQuestion()
		{
			assureNextQuestionBuildParams();
			
			return buildInternal();
		}

		private HunchTopic buildInternal()
		{
			HunchTopic ht = new HunchTopic( _val, buildId, buildDecision,
					buildImageUrl, buildResultType, buildUrlName,
					buildShortName, buildIsEitherOr, buildHunchUrl,
					buildCategory, buildScore );
			reset();

			return ht;
		}
		
		private void assureNextQuestionBuildParams()
		{
			if ( _val == null || buildId == null || buildHunchUrl == null
					|| buildDecision == null || buildImageUrl == null
					|| buildCategory == null )
			{
				throw new IllegalStateException(
						"Not all required fields set before building HunchTopic!" );
			}
		}

		private void assureBuildParams()
		{
			if ( _val == null || buildId == null
					|| buildDecision == null || buildImageUrl == null
					|| buildResultType == null || buildUrlName == null
					|| buildShortName == null )
			{
				throw new IllegalStateException(
						"Not all required fields set before building HunchTopic!" );
			}
		}

		private void assureSearchBuildParams()
		{
			if ( _val == null || buildId == null
					|| buildDecision == null || buildUrlName == null
					|| buildScore == null )
			{
				throw new IllegalStateException(
						"Not all required fields set before building HunchTopic!" );
			}
		}

	}

	private final Integer _id;
	private final String _decision, _imageUrl, _resultType, _urlName,
			_shortName;
	private final Boolean _isEitherOr;
	private final JSONObject json;
	private final String _hunchUrl; // optional
	private final HunchCategory _category;
	private final Double _score;

	private HunchTopic( JSONObject val, Integer id, String decision,
			String imageUrl, String resultType, String urlName,
			String shortName, Boolean isEitherOr, String hunchUrl,
			HunchCategory category, Double score )
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
		_score = score;
	}

	public static Builder getBuilder()
	{
		return b;
	}

	public Integer getId()
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

	public Boolean isEitherOr()
	{
		return _isEitherOr;
	}

	public HunchCategory getCategory()
	{
		return _category;
	}

	public Double getScore()
	{
		return _score;
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
		
		builder.init( topic );

		try
		{
			id = Integer.parseInt( topic.getString( "id" ) );
			eitherOr = Integer.parseInt( topic.getString( "eitherOrTopic" ) );

			
			builder.setId( id )
			.setDecision( topic.getString( "decision" ) )
			.setImageUrl( topic.getString( "imageUrl" ) )
			.setResultType( topic.getString( "resultType" ) )
			.setUrlName( topic.getString( "urlName" ) )
			.setShortName( topic.getString( "shortName" ) )
			.setIsEitherOr(	eitherOr == 1 );
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
