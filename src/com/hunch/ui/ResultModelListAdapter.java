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

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hunch.Const;
import com.hunch.R;
import com.hunch.api.HunchAPI;
import com.hunch.api.HunchResult;
import com.hunch.api.HunchRankedResults.ResultStub;

/**
 * 
 * 
 * @author Tyler Levine
 * @since Aug 12, 2010
 *
 */
public class ResultModelListAdapter extends ResultListAdapter< ResultListAdapter.ResultModel >
{

	/**
	 * @param context
	 * @param list
	 */
	public ResultModelListAdapter( Context context, List< ResultModel > list )
	{
		super( context, list );
	}
	
	protected ResultModel buildModel( HunchResult result, ResultModel stubModel )
	{
		return new ResultModel( result.getName(), String.valueOf( result.getId() ),
				result.getImageUrl(), stubModel.getEitherOrPct() );
	}
	
	@Override
	public View getView( final int position, View convertView, ViewGroup parent )
	{
		Log.d( Const.TAG, "ResultModelListAdapter.getView() pos: " + position +
				" convertView: " + convertView + " parent: " + parent );
		
		// get the basic result info
		final ResultModel stub = getItem( position );
		
		// try it without convert view for now.. there won't
		// be too many items in the results list typically
		// convertView = null;
		
		// view holder pattern courtesy Romain Guy
		ResultViewHolder tempHolder;
		if( convertView == null )
		{
			// first find the inflater
			final LayoutInflater inflater = (LayoutInflater)
					context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			
			convertView = inflater.inflate( R.layout.result_list_item, null );
			
			tempHolder = new ResultViewHolder();
			tempHolder.placeholder = (ProgressBar) convertView.findViewById( R.id.placeholder );
			tempHolder.image = (ImageView) convertView.findViewById( R.id.result_icon );
			tempHolder.text = (TextView) convertView.findViewById( R.id.result_name );
			tempHolder.number = (TextView) convertView.findViewById( R.id.result_number );
			tempHolder.pct = (TextView) convertView.findViewById( R.id.result_pct );
			tempHolder.resultId = stub.getId();
			tempHolder.wholeView = convertView;
			
			convertView.setTag( tempHolder );
		}
		else
		{
			tempHolder = (ResultViewHolder) convertView.getTag();
			resetResultView( tempHolder );
		}
		
		final ResultViewHolder holder = tempHolder;
		
		HunchResult result = getFromCache( stub );
		
		if( result == null || // is it not in the cache?
			stub.isStub() ) // is it a stub?
		{
			// get the full result off the network
			HunchAPI.getInstance().getResult( stub.getId(), Const.RESULT_IMG_SIZE,
					new HunchResult.Callback()
			{

				@Override
				public void callComplete( HunchResult h )
				{					
					setupResultView( holder, position, buildModel( h, stub ) );
					addToCache( stub, h );
				}
			} );
		}
		// we have a full model in the cache
		else
		{
			super.setupResultView( holder, position, buildModel( result, stub ) );
		}
		
		// finally try to add more items inline
		// super.tryLoadInline( position );
		
		return convertView;
	}

	/* (non-Javadoc)
	 * @see com.hunch.ui.ResultListAdapter#getAdapterData()
	 */
	@Override
	public List< ResultModel > getAdapterData()
	{
		return items;
	}

}
