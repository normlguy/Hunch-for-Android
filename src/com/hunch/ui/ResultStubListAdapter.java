/*
 * Copyright Aug 12, 2010 Tyler Levine
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
package com.hunch.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class ResultStubListAdapter extends ResultListAdapter< ResultStub >
{
	public ResultStubListAdapter( Context context, List< ResultStub > list )
	{
		super( context, list );
	}
	
	@Override
	public View getView( final int position, View convertView, ViewGroup parent )
	{
		// get the basic result info
		final ResultStub stub = getItem( position );
		
		// view holder pattern courtesy Romain Guy
		ResultViewHolder tempHolder;
		if( convertView == null )
		{
			// first find the inflater
			final LayoutInflater inflater = (LayoutInflater)
					context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			
			convertView = inflater.inflate( R.layout.result_list_item, null );
			
			tempHolder = new ResultViewHolder();
			tempHolder.image = (ProgressBar) convertView.findViewById( R.id.resultImage );
			tempHolder.text = (TextView) convertView.findViewById( R.id.resultName );
			tempHolder.number = (TextView) convertView.findViewById( R.id.resultNumber );
			tempHolder.pct = (TextView) convertView.findViewById( R.id.resultPct );
			tempHolder.resultId = stub.getId();
			tempHolder.wholeView = convertView;
			
			convertView.setTag( tempHolder );
		}
		else
		{
			tempHolder = (ResultViewHolder) convertView.getTag();
		}
		
		final ResultViewHolder holder = tempHolder;
		
		HunchResult result = getFromCache( stub );
		
		if( result != null )
		{
			super.setupResultView( holder, position, buildModel( result, stub ) );
		}
		// we don't have this result yet, go get it.
		else
		{
			// and use it to get the full result off the network
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
		
		// set the special background if it's the top result
		if ( position == 0 )
			convertView.setBackgroundResource( R.drawable.top_result );
		
		// finally try to add more items inline
		super.tryLoadInline( position );
		
		return convertView;
	}

	/* (non-Javadoc)
	 * @see com.hunch.ui.ResultListAdapter#getAdapterData()
	 */
	@Override
	public List< ResultListAdapter.ResultModel > getAdapterData()
	{
		
		List< ResultListAdapter.ResultModel > ret =
			new LinkedList< ResultListAdapter.ResultModel >();
		
		// add from cache
		for( Map.Entry< ResultStub, HunchResult > entry : resultsCache.entrySet() )
		{
			ret.add( buildModel( entry.getValue(), entry.getKey() ) );
		}
		
		// add the rest of the stubs that haven't been downloaded yet
		for( ResultStub stub : super.getItems() )
		{
			// but not the ones who are already downloaded, we already got those
			if( resultsCache.containsKey( stub ) ) continue;
			
			// otherwise convert to ResultModel and return
			ret.add( new ResultModel( null, stub.getId(), null, stub.getEitherOrPct(), true ) );
		}
		
		return ret;
	}
}
