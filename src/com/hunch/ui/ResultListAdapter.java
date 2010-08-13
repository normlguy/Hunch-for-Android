/*
 * Copyright Aug 12, 2010 Tyler Levine
 * 
 * This file is part of Hunch-for-Android.
 *
 * Hunch-for-Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hunch-for-Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hunch-for-Android.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hunch.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hunch.ImageManager;
import com.hunch.R;
import com.hunch.api.HunchResult;
import com.hunch.api.HunchRankedResults.ResultStub;
import com.hunch.util.InfiniteListAdapter;

/**
 * 
 * 
 * @author Tyler Levine
 * @since Aug 12, 2010
 *
 */
public abstract class ResultListAdapter< T > extends InfiniteListAdapter< T >
{
	public static class ResultViewHolder
	{
		View wholeView;
		ProgressBar image;
		TextView text;
		TextView number;
		TextView pct;
		String resultId;
	}
	
	protected static class ResultModel
	{
		private final Bundle data;
		private final boolean isStub;
		
		private final String KEY_NAME = "name";
		private final String KEY_ID = "id";
		private final String KEY_IMG_URL = "imgUrl";
		private final String KEY_EITHER_OR_PCT = "eitherOrPct";
		
		public ResultModel( String name, String id, String imgUrl, String eitherOrPct )
		{
			this( name, id, imgUrl, eitherOrPct, false );
		}
		
		public ResultModel( String name, String id, String imgUrl, String eitherOrPct, boolean isStub )
		{
			data = new Bundle();
			data.putString( KEY_NAME, name );
			data.putString( KEY_ID, id );
			data.putString( KEY_IMG_URL, imgUrl );
			data.putString( KEY_EITHER_OR_PCT, eitherOrPct );
			
			this.isStub = isStub;
		}
		
		public boolean isStub()
		{
			return isStub;
		}
		
		public String getName()
		{
			return data.getString( KEY_NAME );
		}
		
		public String getId()
		{
			return data.getString( KEY_ID );
		}
		
		public String getImageUrl()
		{
			return data.getString( KEY_IMG_URL );
		}
		
		public String getEitherOrPct()
		{
			return data.getString( KEY_EITHER_OR_PCT );
		}
		
		public boolean hasEitherOrPct()
		{
			return getEitherOrPct() != null;
		}
		
		@Override
		public String toString()
		{
			return "ResultModel[" + data.getString( KEY_NAME ) + "]";
		}
		
		@Override
		public int hashCode()
		{
			int code = 37;
			code = code * 3 + data.getString( KEY_EITHER_OR_PCT ).hashCode();
			code = code * 3 + data.getString( KEY_ID ).hashCode();
			code = code * 3 + data.getString( KEY_IMG_URL).hashCode();
			code = code * 3 + data.getString( KEY_NAME ).hashCode();
			
			return code;
		}
	}
	
	protected final Context context;
	//private final ProgressDialog progress;
	//private boolean showProgress = true;
	protected final Map< T, HunchResult > resultsCache;
	
	public static final int RESULTS_SHOWN_ON_LOAD = 10;
	public static final int RESULTS_ADDED_INLINE = 5;

	public ResultListAdapter( Context context, List< T > list )
	{
		super( list, RESULTS_SHOWN_ON_LOAD, RESULTS_ADDED_INLINE );

		this.context = context;
		
		 resultsCache = new HashMap< T, HunchResult >();
		//progress = new ProgressDialog( context );
	}

	@Override
	protected boolean shouldLoadInline( int curPos, int size )
	{
		return curPos >= size - 3;
	}
	
	protected HunchResult getFromCache( T stub )
	{
		return resultsCache.get( stub );
	}
	
	protected void addToCache( T stub, HunchResult result )
	{
		resultsCache.put( stub, result );
	}
	
	protected ResultModel buildModel( HunchResult result, ResultStub stub )
	{
		return new ResultModel( result.getName(), String.valueOf( result.getId() ),
				result.getImageUrl(), stub.getEitherOrPct() );
	}

	protected void setupResultView( final ResultViewHolder resultView, int position,
			ResultModel resultData )
	{
		// first download the result image.
		// this is gonna take a while because it needs
		// to hit the network in most cases.

		final ImageView resultImg = new ImageView( context );
		final ProgressBar placeholder = resultView.image;
		if( placeholder != null )
			placeholder.setIndeterminate( true );
		
		final ViewGroup parentGroup = (ViewGroup) resultView.wholeView;

		ImageManager.getInstance().getTopicImageWithCallback( context, resultData.getImageUrl(),
				new ImageManager.Callback()
		{

			@Override
			public void callComplete( Drawable d )
			{
				// copy the layout params and index
				resultImg.setLayoutParams( placeholder.getLayoutParams() );
				int childIndex = parentGroup.indexOfChild( placeholder );
				
				//if( progress.isShowing() )
				//	progress.hide();

				// remove the progressbar
				parentGroup.removeView( placeholder );
				
				if( placeholder != null )
					placeholder.setVisibility( View.GONE );

				// add the image view
				parentGroup.addView( resultImg, childIndex );

				// set the drawable
				resultImg.setImageDrawable( d );

			}
		} );
		
		// now set the rest of the fields - index, name and the percentage
		resultView.number.setText( String.valueOf( position + 1 ) + "." );

		resultView.text.setText( resultData.getName() );
		
		// set the special background if it's the top result
		if ( position == 0 )
		{
			resultView.wholeView.setBackgroundResource( R.drawable.top_result );
		}
		else
		{
			resultView.wholeView.setBackgroundResource( R.drawable.result_btn );
		}

		TextView resultPct = resultView.pct;
		if ( resultData.hasEitherOrPct() )
		{
			resultPct.setText( resultData.getEitherOrPct() + "%" );
		} else
		{
			resultPct.setVisibility( View.GONE );
			parentGroup.removeView( resultPct );
		}

	}

	@Override
	public abstract View getView( final int position, View convertView, ViewGroup parent );

	public abstract List< ResultModel > getAdapterData();
}
