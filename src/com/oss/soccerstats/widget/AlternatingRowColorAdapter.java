package com.oss.soccerstats.widget;

import com.oss.soccerstats.util.Constants;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;

public class AlternatingRowColorAdapter extends SimpleCursorAdapter
{
   private int[]   colors     = new int[] {Constants.DARK_BACKGROUND, 
                                           Constants.LIGHT_BACKGROUND};
   private boolean colorOne   = true;

   public AlternatingRowColorAdapter(Context context, int layout, Cursor c,
            String[] from, int[] to)
   {
      super(context, layout, c, from, to);
   }

   @Override
   public void bindView(View row, Context context, Cursor cursor) 
   {  
      super.bindView(row, context, cursor);

      // Alternating row colors
       if (colorOne)    row.setBackgroundColor(colors[0]);
       else             row.setBackgroundColor(colors[1]);
       colorOne = ! colorOne;
   }
}
