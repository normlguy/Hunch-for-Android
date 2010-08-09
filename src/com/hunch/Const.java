package com.hunch;

import android.graphics.Color;

public final class Const
{
	// debug tag
	public static final String	TAG						= "HUNCH";

	// update constants
	public static final int		HOME_CATEGORY_LIST_INIT	= 0;
	public static final int		HOME_TOPIC_LIST_INIT	= 1;
	public static final int		TOPIC_PLAY_INIT			= 2;
	public static final int		TOPIC_PLAY_RESPONSE		= 3;
	public static final int		TOPIC_PLAY_TRANSITION	= 4;
	public static final int		TOPIC_PLAY_END			= 5;
	
	// sizes for the images we pull from the Hunch API
	public static final String TOPIC_IMG_SIZE = "64x64";
	public static final String TOPIC_LIST_IMG_SIZE = "32x32";
	public static final String RESULT_IMG_SIZE = "48x48";
	public static final String RESULT_DETAILS_IMG_SIZE = "128x128";
	public static final String CATEGORY_IMG_SIZE = "32x32";
	
	// not sure that any of these images are being displayed, but I'll define them anyway
	public static final String QUESTION_IMG_SIZE = "48x48";
	public static final String RESPONSE_IMG_SIZE = "48x48";
	
	// size of the image download buffer
	public static final int IMG_DOWNLOAD_BUFFER_SIZE = 2048;
	
	// where to cache images that are downloaded
	public static final String INTERNAL_IMG_DIR = "hunch_images";
	public static final String CACHE_IMG_DIR = "hunch_image_cache";
	
	// default image to be shown for category images
	// while real images are downloading or otherwise unavailable.
	public static final int CAT_DEFAULT_IMG = R.drawable.default_image_32;
	public static final int TOPIC_DEFAULT_IMG = R.drawable.default_image_32;
	
	// color constants
	public static final int		HOME_TAB_FOCUSED_COLOR	= Color.argb( 198, 57, 25, 125 );
	
	// thread number constants (0 for cached thread pool)
	public static final int IMG_FETCH_THREADS = 3;
	public static final int API_CALL_THREADS = 3;
	
	// topic play menu item identifiers
	public static final int MENU_RESTART_TOPIC = 0;
	
	// select topic menu item identifiers
	public static final int MENU_SEARCH = 0;
	public static final int MENU_SETTINGS = 1;
}
