package com.hunch.api;

import com.hunch.api.HunchTopic.Variety;

/**
 * 
 * 
 * @author Tyler Levine
 * Jan 15, 2010
 *
 */
public interface IHunchTopic
{

	public abstract Integer getId();

	public abstract String getDecision();

	public abstract String getImageUrl();

	public abstract String getResultType();

	public abstract String getUrlName();

	public abstract String getShortName();

	public abstract String getHunchUrl();

	public abstract Boolean isEitherOr();

	public abstract HunchCategory getCategory();

	public abstract Double getScore();

	public abstract Variety getVariety();

	public abstract String toString();

}