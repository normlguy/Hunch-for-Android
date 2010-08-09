package com.hunch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class ImageManager
{
	private final static Map< URL, SoftReference<Bitmap> > drawableMap = 
		new HashMap< URL, SoftReference<Bitmap> >();
	private static ExecutorService executor;
	
	private static ImageManager instance = null;
	
	// no instantiation
	private ImageManager() {}
	
	public static ImageManager getInstance()
	{
		if ( instance == null ) instance = new ImageManager();
		
		startExecutor();
		
		return instance;
	}
	
	private static void startExecutor()
	{
		// if we already have an executor, theres no need for another one.
		if( executor != null ) return;
		
		if( Const.IMG_FETCH_THREADS > 1 )
			executor = Executors.newFixedThreadPool( Const.IMG_FETCH_THREADS );
		else if ( Const.IMG_FETCH_THREADS == 1 )
			executor = Executors.newSingleThreadExecutor();
		else
			executor = Executors.newCachedThreadPool();
	}

	
	protected void cacheBitmap( Bitmap bitmap, URL url, Context context, CachePolicy... policies )
	{
		if( policies == null || policies.length == 0 )
		{
			Log.w( Const.TAG, "tried to cache image with no cache policy?!?" );
			return;
		}
		
		List< CachePolicy > completedAdds = new LinkedList< CachePolicy >();
		for( CachePolicy policy : policies )
		{
			// if we have already added to this cache, don't add again
			// in case someone accidently passes the same cache policy
			// more than once (which should never happen)
			if( completedAdds.contains( policy ) )
			{
				continue;
			}
			
			switch( policy )
			{
				case MEMORY:
					addToMemoryCache( bitmap, url );
					
					break;
					
				case INTERNAL:
					addToInternalCache( bitmap, url, context );
					
					break;
					
				case CACHE:
					addToApplicationCache( bitmap, url, context );
					
					break;
			}
			
			completedAdds.add( policy );
		}
		
	}
	
	protected void addToMemoryCache( Bitmap d, URL url )
	{
		//Log.v( Const.TAG, "adding bitmap to memory cache (" + url +  ")" );
		// add by soft reference to the level one cache (in memory)
		drawableMap.put( url, new SoftReference<Bitmap>( d ) );
	}
	
	protected void addToInternalCache( Bitmap d, URL url, Context context )
	{
		final String fileName = fileNameFromURL( url );
		File hunchImagesDir = context.getDir( Const.INTERNAL_IMG_DIR, Context.MODE_PRIVATE );
		
		// directory is guaranteed to exist by getDir()
		File imageFile = new File( hunchImagesDir, fileName );
		
		/*if( !imageFile.canWrite() )
		{
			Log.w( Const.TAG, "can't write to internal image cache! (" + imageFile.toString() + ")" );
			return;
		}*/
		
		FileOutputStream fOut = null;
		try
		{
			fOut = new FileOutputStream( imageFile );
			
		} catch ( FileNotFoundException e )
		{
			Log.w( Const.TAG, "can't find or write to file in internal cache! (" +
					imageFile.getAbsolutePath() +")" );
			return;
		}
		
		boolean success = d.compress( Bitmap.CompressFormat.PNG, 0, fOut );
		if( !success )
		{
			Log.w( Const.TAG, "image compress for internal cache failed! (" +
					imageFile.getAbsolutePath() +")" );
		}
		
		try
		{
			fOut.flush();
			fOut.close();
		} catch ( IOException e )
		{
			Log.w( Const.TAG, "couldn't flush or close output stream for internal cache! (" 
					+ imageFile.getAbsolutePath() + ")" );
		}
		
	}
	
	protected void addToApplicationCache( Bitmap d, URL url, Context context )
	{
		final String fileName = fileNameFromURL( url );
		File cachedImagesDir = new File( context.getCacheDir(), Const.CACHE_IMG_DIR );
		
		if( !cachedImagesDir.exists() )
		{
			cachedImagesDir.mkdir();
		}
		
		File imageFile = new File( cachedImagesDir, fileName );
		
		/*if( !imageFile.canWrite() )
		{
			Log.w( Const.TAG, "can't write to app image cache! (" + imageFile.toString() + ")" );
			return;
		}*/
		
		FileOutputStream fOut = null;
		try
		{
			fOut = new FileOutputStream( imageFile );
			
		} catch ( FileNotFoundException e )
		{
			Log.w( Const.TAG, "can't find or write to file in app cache! (" +
					imageFile.getAbsolutePath() +")" );
			e.printStackTrace();
			return;
		}
		
		boolean success = d.compress( Bitmap.CompressFormat.PNG, 0, fOut );
		if( !success )
		{
			Log.w( Const.TAG, "image compress for app cache failed! (" +
					imageFile.getAbsolutePath() +")" );
		}
		
		try
		{
			fOut.flush();
			fOut.close();
		} catch ( IOException e )
		{
			Log.w( Const.TAG, "couldn't flush or close output stream for app cache! (" 
					+ imageFile.getAbsolutePath() + ")" );
		}
	}
	
	protected String fileNameFromURL( URL url )
	{
		String urlString = url.toString();
		String retString = urlString.substring( urlString.lastIndexOf( '/' ) + 1, urlString.length() );
		return retString;
	}
	
	protected FileInputStream getImageFileStreamFromCache( final Context context, final URL url )
	{
		final String fileName = fileNameFromURL( url );
		
		File hunchCacheDir = new File( context.getCacheDir(), Const.CACHE_IMG_DIR );
		File[] imageFileList = hunchCacheDir.listFiles( new FilenameFilter()
		{
			
			@Override
			public boolean accept( File dir, String aFilename )
			{
				return aFilename.equals( fileName );
			}
		} );
		
		if( imageFileList == null || imageFileList.length == 0 )
		{
			return null; 
		}
			
		if( imageFileList.length > 1 )
		{
			Log.i( Const.TAG, "found " + imageFileList.length + " images for url in app cache! " +
					"(" + fileName + ")" );
		}
		
		FileInputStream iStream = null;
		try
		{
			iStream = new FileInputStream( imageFileList[0] );
		} catch ( FileNotFoundException e )
		{
			return null;
		}
		
		Log.v( Const.TAG, "found image in app cache (" + url + ")" );
		
		return iStream;
	}
	
	protected FileInputStream getImageFileStreamFromInternal( final Context context, final URL url )
	{
		final String fileName = fileNameFromURL( url );
		File internalImagesDir = context.getDir( Const.INTERNAL_IMG_DIR,  Context.MODE_PRIVATE );
		
		// the directory is guaranteed to exist by getDir()
		
		File[] imageFileList = internalImagesDir.listFiles( new FilenameFilter()
		{
			
			@Override
			public boolean accept( File dir, String aFilename )
			{
				return fileName.equals( aFilename );
			}
		} );
		
		if( imageFileList == null || imageFileList.length == 0 )
		{
			return null; 
		}
		
		if( imageFileList.length > 1 )
		{
			Log.i( Const.TAG, "found " + imageFileList.length + " images for url in internal cache! " +
					"(" + fileName + ")" );
		}
		
		FileInputStream iStream = null;
		try
		{
			iStream = new FileInputStream( imageFileList[0] );
		} catch ( FileNotFoundException e )
		{
			return null;
		}
		
		Log.v( Const.TAG, "found image in internal cache (" + url + ")" );
		
		return iStream;
	}
	
	protected Drawable getDrawableFromMemoryCache( final Context context, final URL url )
	{
		SoftReference< Bitmap > drawableRef = drawableMap.get( url );
		if( drawableRef != null )
		{
			// the key exists
			Bitmap image = drawableRef.get();
			if( image != null )
			{
				Log.v( Const.TAG, "found image in memory cache (" + fileNameFromURL( url ) + ")" );
				
				// the image hasn't been gc'd yet.
				return new BitmapDrawable( context.getResources(), image );
			}
			else
			{
				// the drawable has been gc'd, remove the key
				drawableMap.remove( url );
			}
		}
		
		return null;
	}
	
	protected Drawable getCachedCategoryImage( final Context context, final URL url )
	{
		// is the drawable in the level one cache (in memory)
		Drawable drawable = getDrawableFromMemoryCache( context, url );
		if( drawable != null )
		{
			// we got lucky, it was in memory
			return drawable;
		}
		
		// otherwise check the internal file cache
		FileInputStream imageFileStream = getImageFileStreamFromInternal( context, url );
		
		// if the stream doesn't exist, we don't have the image cached
		if( imageFileStream == null )
		{
			return null;
		}
		
		Bitmap bitmap = BitmapFactory.decodeStream( imageFileStream );
		
		// add the bitmap to the in memory cache
		addToMemoryCache( bitmap, url );
		
		return new BitmapDrawable( context.getResources(), bitmap );
	}
	
	protected Drawable getCachedTopicImage( final Context context, final URL url )
	{
		// is the drawable in the level one cache (in memory)
		Drawable drawable = getDrawableFromMemoryCache( context, url );
		if( drawable != null )
		{
			// we got lucky, it was in memory
			return drawable;
		}
		
		// otherwise check the internal file cache
		FileInputStream imageFileStream = getImageFileStreamFromCache( context, url );
		
		// if the stream doesn't exist, we don't have the image cached
		if( imageFileStream == null )
		{
			return null;
		}
		
		Bitmap bitmap = BitmapFactory.decodeStream( imageFileStream );
		
		return new BitmapDrawable( context.getResources(), bitmap );
	}
	
	protected Runnable getDownloadTask( final URL url, final Callback callback, final Context context,
			final CachePolicy... level )
	{
		// hander to deal with the image once UI thread has it
		final Handler imgHandler = new Handler()
		{
			@Override
			public void handleMessage( Message msg )
			{
				Drawable d = (Drawable) msg.obj;
				
				
				
				callback.callComplete( d );
			}
		};
		
		// the job to grab the image off the network and cache it
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Log.d( Const.TAG, String.format( "[%d] fetching image from URI (%s)", 
							Thread.currentThread().getId(), url.toString() ) );
					InputStream iStream = ImageManager.this.getInputStream( url );
					final Bitmap image = BitmapFactory.decodeStream( iStream );
					BitmapDrawable drawable = new BitmapDrawable( context.getResources(), image );
					
					final Message msg = imgHandler.obtainMessage();
					
					msg.obj = drawable;
										
					// send off the message with the drawable
					imgHandler.sendMessage( msg );
					
					// then cache the stream
					cacheBitmap( image, url, context, level );
				} catch( FileNotFoundException e )
				{
					Log.e( Const.TAG, "couldn't get image off network (404 error)" );
				}
				catch( IOException e )
				{
					Log.e( Const.TAG, "couldn't get image off network" );
				}
			}
		};
		
		return task;
	}
	
	protected InputStream getInputStream( URL url ) throws IOException
	{
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet request = new HttpGet( url.toString() );
		HttpResponse response = httpClient.execute( request );
		return response.getEntity().getContent();
	}
	
	/**
	 * Get the image for a Hunch category. These images are cached into internal storage
	 * after the first time they are downloaded. If the image is not present in the cache,
	 * it will be downloaded in a background thread then cached, and a placeholder image
	 * will be displayed it its place.
	 * 
	 * @param context Application context (to get to internal storage dir)
	 * @param image The ImageView that will display the image
	 * @param imgURL The URL to the image.
	 */
	public void getCategoryImage( final Context context, final ImageView image, final String imgURL )
	{
		URL url = stringToURL( imgURL );
		Drawable cachedDrawable = getCachedCategoryImage( context, url );
		
		if( cachedDrawable != null )
		{
			// the image is in the cache
			image.setImageDrawable( cachedDrawable );
		}
		else
		{
			
			// the image is not in the cache, must download
			downloadCategoryDrawable( url, context, new Callback()
			{
				
				@Override
				public void callComplete( Drawable d )
				{
					image.setImageDrawable( d );		
				}
			} );
			
			// set it to the placeholder image for now
			image.setImageResource( Const.CAT_DEFAULT_IMG );
		}
		
	}
	
	public void getTopicImage( final Context context, final ImageView image, final String imgURL )
	{
		URL url = stringToURL( imgURL );
		Drawable cachedDrawable = getCachedTopicImage( context, url );
		
		if( cachedDrawable != null )
		{
			// the image is in the cache
			image.setImageDrawable( cachedDrawable );
		}
		else
		{
			// the image is not in the cache, must download
			downloadTopicDrawable( url, context, new Callback()
			{
				
				@Override
				public void callComplete( Drawable d )
				{
					image.setImageDrawable( d );		
				}
			} );
			
			// set it to the placeholder image for now
			image.setImageResource( Const.TOPIC_DEFAULT_IMG );
		}
	}
	
	protected URL stringToURL( String url )
	{
		URL temp = null;
		try
		{
			temp = new URL( url );
		} catch ( MalformedURLException e )
		{
			e.printStackTrace();
			// TODO: better handling of malformed urls?
			// but really this should never happen unless the
			// hunch API starts returning bad URLs. I guess I'll
			// rely on the hunch API returning valid and parseable
			// URLs for now.
			
			return null;
		}
		
		return temp;
	}
	
	protected void downloadCategoryDrawable( URL imgURL, Context context, Callback callback )
	{
		Runnable task = getDownloadTask( imgURL, callback, context, CachePolicy.MEMORY, CachePolicy.INTERNAL );
		executor.execute( task );
	}
	
	protected void downloadTopicDrawable( URL imgURL, Context context, Callback callback )
	{
		Runnable task = getDownloadTask( imgURL, callback, context, CachePolicy.MEMORY, CachePolicy.CACHE );
		executor.execute( task );
	}
	
	public void getTopicImageWithCallback( Context context, String imgURL, Callback callback )
	{
		URL url = stringToURL( imgURL );
		Drawable cachedDrawable = getCachedTopicImage( context, url );
		
		if( cachedDrawable != null )
		{
			// the image is in the cache
			callback.callComplete( cachedDrawable );
		}
		else
		{
			// the image is not in the cache, must download
			downloadTopicDrawable( url, context, callback );
		}
	}
	
	public interface Callback
	{
		public void callComplete( Drawable d );
	}
	
	// INTERNAL is the internal memory
	// CACHE is the application cache
	// MEMORY is an in-memory HashMap backed with SoftReferences
	protected enum CachePolicy
	{
		INTERNAL, CACHE, MEMORY
	}

}
