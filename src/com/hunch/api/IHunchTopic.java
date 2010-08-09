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

	public abstract String getId();

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