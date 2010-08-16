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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hunch.ImageManager;
import com.hunch.api.HunchResult;
import com.hunch.api.HunchRankedResults.ResultStub;

/**
 * 
 * 
 * @author Tyler Levine
 * @since Aug 12, 2010
 *
 */
public abstract class ResultListAdapter< T > extends BaseAdapter
{
	public static class ResultViewHolder
	{
		View wholeView;
		ProgressBar placeholder;
		ImageView image;
		TextView text;
		TextView number;
		TextView pct;
		String resultId;
		
		boolean isSet = false;
				
	}
	
	protected static class ResultModel
	{
		private final boolean mIsStub;
		
		private final String mName;
		private final String mId;
		private final String mImgUrl;
		private final String mEitherOrPct;
		
		public ResultModel( String name, String id, String imgUrl, String eitherOrPct )
		{
			this( name, id, imgUrl, eitherOrPct, false );
		}
		
		public static ResultModel createStub( String id, String eitherOrPct )
		{
			return new ResultModel( null, id, null, eitherOrPct, true );
		}
		
		public ResultModel( String name, String id, String imgUrl, String eitherOrPct, boolean isStub )
		{
			mName = name;
			mId = id;
			mImgUrl = imgUrl;
			mEitherOrPct = eitherOrPct;
			
			mIsStub = isStub;
		}
		
		public boolean isStub()
		{
			return mIsStub;
		}
		
		public String getName()
		{
			return mName;
		}
		
		public String getId()
		{
			return mId;
		}
		
		public String getImageUrl()
		{
			return mImgUrl;
		}
		
		public String getEitherOrPct()
		{
			return mEitherOrPct;
		}
		
		public boolean hasEitherOrPct()
		{
			return getEitherOrPct() != null;
		}
		
		@Override
		public String toString()
		{
			return "ResultModel[" + mName + "]";
		}
		
		@Override
		public int hashCode()
		{
			int code = 37;
			
/*			Log.d( Const.TAG, String.format( "hashCode() [EO-PCT: %s, ID: %s, " +
					"IMG-URL: %s, NAME: %s]", data.getString( KEY_EITHER_OR_PCT ),
					data.getString( KEY_ID ), data.getString( KEY_IMG_URL ),
					 data.getString( KEY_NAME ) ) );*/
			
			if( hasEitherOrPct() )
			{
				code = code * 3 + mEitherOrPct.hashCode();
			}
			
			code = code * 3 + mId.hashCode();
			
			// stub's wont have the rest of the data
			// and hence will throw NPE's
			if( isStub() ) return code;
			
			code = code * 3 + mImgUrl.hashCode();
			code = code * 3 + mName.hashCode();
			
			return code;
		}
	}
	
	protected final Context context;
	//private final ProgressDialog progress;
	//private boolean showProgress = true;
	protected final Map< T, HunchResult > resultsCache;
	protected final List< T > items;

	public ResultListAdapter( Context context, List< T > list )
	{
		this.context = context;
		items = list;
		
		 resultsCache = new HashMap< T, HunchResult >();
		//progress = new ProgressDialog( context );
	}
	
	@Override
	public int getCount()
	{
		return items.size();
	}
	
	@Override
	public long getItemId( int pos )
	{
		return pos;
	}
	
	@Override
	public T getItem( int pos )
	{
		return items.get( pos );
	}

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
	
	protected void resetResultView( ResultViewHolder view )
	{
		
		if( view.placeholder.getVisibility() != View.GONE &&
			view.image.getVisibility() == View.GONE )
		{
			// the view has not been set yet, no work to do
			return;
		}
		
		view.image.setVisibility( View.GONE );
		view.placeholder.setVisibility( View.VISIBLE );
		
		// reset text views (important!)
		view.number.setText( "" );
		view.text.setText( "Loading..." );
		view.pct.setText( "" );
		view.pct.setVisibility( View.GONE );
		
		view.isSet = false;
	}

	protected void setupResultView( final ResultViewHolder resultView, int position,
			ResultModel resultData )
	{
		
		if( resultView.isSet )
		{
			return;
		}
		
		// first download the result image.
		// this is gonna take a while because it needs
		// to hit the network in most cases.
		
		if( resultView.image.getVisibility() == View.GONE )
		{
			ImageManager.getInstance().getTopicImageWithCallback( context, resultData.getImageUrl(),
					new ImageManager.Callback()
			{

				@Override
				public void callComplete( Drawable d )
				{

					// remove the progressbar
					resultView.placeholder.setVisibility( View.GONE );

					// set the drawable
					resultView.image.setImageDrawable( d );
				
					// show the image view
					resultView.image.setVisibility( View.VISIBLE );
				}
			} );
		}

		// now set the rest of the fields - index, name and the percentage
		resultView.number.setText( String.valueOf( position + 1 ) + "." );

		resultView.text.setText( resultData.getName() );

		if ( resultData.hasEitherOrPct() )
		{
			resultView.pct.setVisibility( View.VISIBLE );
			resultView.pct.setText( resultData.getEitherOrPct() + "%" );
		}
		
		resultView.isSet = true;

	}

	@Override
	public abstract View getView( final int position, View convertView, ViewGroup parent );

	public abstract List< ResultModel > getAdapterData();
}
