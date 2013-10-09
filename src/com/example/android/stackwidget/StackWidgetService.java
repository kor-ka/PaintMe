/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.stackwidget;

import android.appwidget.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final int mCount = 3;
    private List<String> mWidgetItems = new ArrayList<String>();
    private Context mContext;
    private int mAppWidgetId;
	Bitmap bmp;


    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
      //  for (int i = 0; i < mCount; i++) {
		mWidgetItems.add("https://dl.dropboxusercontent.com/s/b503c92w7d7uuxb/72.png?token_hash=AAH--mSSyV1AiNGYJKFX5eoTPKuQtyLcrP7TclbYs7UVLA&dl=1");
		mWidgetItems.add("https://dl.dropboxusercontent.com/s/22dxa79gac3jds8/550587.png?token_hash=AAFO0LcwahGd9dAzPHKckMU5dH3W1buoVv4LrCaHfATkCQ&dl=1");
		mWidgetItems.add("https://dl.dropboxusercontent.com/s/iowxdnbor2v28j2/Simple_tux.png?token_hash=AAFNAciWARCOqZBhcJuBveVt0tA0vLjRS1euLUPh8nKgCQ&dl=1");
    //    }

        // We sleep for 3 seconds here to show how the empty view appears in the interim.
        // The empty view is set in the StackWidgetProvider and should be a sibling of the
        // collection view.
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mWidgetItems.clear();
    }

    public int getCount() {
        return mCount;
    }

    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        // We construct a remote views item based on our widget item xml file, and set the
        // text based on the position.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
        //rv.setTextViewText(R.id.widget_item, mWidgetItems.get(position).text);		
		
		

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putInt(StackWidgetProvider.EXTRA_ITEM, position);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

        // You can do heaving lifting in here, synchronously. For example, if you need to
        // process an image, fetch something from the network, etc., it is ok to do it here,
        // synchronously. A loading view will show up in lieu of the actual contents in the
        // interim.
		if(isOnline()){
		
			try {
				Thread.sleep(500);
				bmp = getBitmapFromURL(mWidgetItems.get(position));	

				String path = Environment.getExternalStorageDirectory().toString();
				OutputStream fOut = null;
				File exportDir = new File(Environment.getExternalStorageDirectory(), "PaintMe");

	        		if (!exportDir.exists()) { exportDir.mkdirs(); }

				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		    		String date = sdf.format(new Date(System.currentTimeMillis()));

	        		File file = new File(exportDir, position+".png");
				fOut = new FileOutputStream(file);

				getImageBitmap(mWidgetItems.get(position)).compress(Bitmap.CompressFormat.PNG, 100, fOut);
				fOut.flush();
				fOut.close();

				MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
				
				System.out.println("Loading view " + position);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		rv.setImageViewBitmap(R.id.widget_item, bmp);

        // Return the remote views object.
        return rv;
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
		
		
    }
	
	public static Bitmap getBitmapFromURL(String src) {  
        try {

            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap mybitmap = BitmapFactory.decodeStream(input);

            return mybitmap;

        } catch (Exception ex) {

            return null;
        }
	}
	public boolean isOnline() {
		ConnectivityManager cm =(ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}
}
