/////////////////////////////////////////////////////////////////
// PlayerStatsAdapter.java
//
// SoccerSoft - Proprietary (Restricted)
// Solely for authorized persons having a need to know
// pursuant to Company Instructions.
//
// Copyright (c) 2010, 2011 SoccerSoft Inc.  All rights reserved.
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF SoccerSoft.
// The copyright notice above does not evidence any actual
// or intended publication of such source code.
/////////////////////////////////////////////////////////////////

package com.oss.soccerstats.widget;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.oss.soccerstats.R;
import com.oss.soccerstats.data.SoccerStatsDataHelper;
import com.oss.soccerstats.util.Constants;

public class PlayerStatsAdapter extends SimpleCursorAdapter
{

   private int[]   colors     = new int[] {Constants.DARK_BACKGROUND, 
                                           Constants.LIGHT_BACKGROUND};
   private boolean colorOne   = true;

   public PlayerStatsAdapter(Context context, int layout, Cursor c,
            String[] from, int[] to)
   {
      super(context, layout, c, from, to);
   }

   @Override
   public void bindView(View row, Context context, Cursor cursor) 
   {  
      super.bindView(row, context, cursor);

      int timeIndex = 
         cursor.getColumnIndex(SoccerStatsDataHelper.PlayerStats.PLAY_TIME);
      int playTime = cursor.getInt(timeIndex);
      //convert to seconds
      playTime = playTime / 1000; // milliseconds to seconds
      long minutes = playTime / 60;
      long remainder = playTime % 60;
      long seconds   = remainder;
      String strTime =  ( (minutes < 10 ? "0":"") + minutes
                           + ":"
                           + (seconds < 10 ? "0":"") + seconds );
      TextView timeColumn  = (TextView) row.findViewById(R.id.playerTimeOnField);
      timeColumn.setText(strTime);         

      // Alternating row colors
       if (colorOne)    row.setBackgroundColor(colors[0]);
       else             row.setBackgroundColor(colors[1]);
       colorOne = ! colorOne;
   }
   
}
