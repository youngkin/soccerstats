package com.oss.soccerstats.view;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.oss.soccerstats.R;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Player;

public class SogDialog extends Dialog
{
   /**
    * The columns we are interested in from the player table
    */
   private static final String[] PLAYER_PROJECTION = new String[] {
           Player._ID,           // 0
           Player.NAME,          // 1
           Player.JERSEY_NUMBER  // 2
   };
   
   public static final int SOG     = 1;
   public static final int SAVE    = 2;
   public static final int AGAINST = 3;

   private Cursor mPlayerCursor;

   private Context mContext;

   public interface OnSogEventListener
   {
      public void onSogHandler(String aPlayerIdForSog, int aAction);
   }

   private OnSogEventListener mOnOkListener;

   public SogDialog(Context context, 
            OnSogEventListener aOnOkListener)
   {
      super(context);
      mContext = context;
      mOnOkListener = aOnOkListener;
      setContentView(R.layout.sog_details);
   }

   
   @Override
   public void onCreate(Bundle savedInstanceState)
   {  
      ContentResolver cr = mContext.getContentResolver();
      mPlayerCursor = cr.query(Player.CONTENT_URI, PLAYER_PROJECTION, null, null,
                             Player.DEFAULT_SORT_ORDER);

      // Used to map notes entries from the database to views
      SimpleCursorAdapter adapter = new SimpleCursorAdapter(
               this.getContext(), R.layout.stat_details_list_item, mPlayerCursor,
               new String[] { Player.JERSEY_NUMBER, Player.NAME, Player._ID }, 
               new int[] { R.id.playerJerseyNumber, R.id.playerName, R.id.playerID});

      Spinner sogBySpinner = (Spinner) findViewById(R.id.sogBy);
      sogBySpinner.setAdapter(adapter);
      
      Button ok   = (Button)findViewById(R.id.ok);
      ok.setOnClickListener( new Button.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            v = (View) v.getParent().getParent();
            ViewGroup dialog = (ViewGroup) v.getParent().getParent().getParent();
            TextView dialogView = (TextView) dialog.getChildAt(0);
            String dialogTitle = (String) dialogView.getText();
            
            int action = SOG;
            if ( dialogTitle.equals(mContext.getResources().getString(R.string.sogDetailsThem)) )
            {
               action = SAVE;
            }
            else if ( dialogTitle.equals(mContext.getResources().getString(R.string.goalAllowedBy)) )
            {
               action = AGAINST;
            }
            View v2 = (View) v.getParent().getParent();            
            Spinner sogSpinner = (Spinner)v2.findViewById(R.id.sogBy);
            TextView sogPlayerIdField = (TextView)sogSpinner.findViewById(R.id.playerID);
            String sogPlayerId = (String) sogPlayerIdField.getText();
            
            mOnOkListener.onSogHandler(sogPlayerId, action);
            
            dismiss();
         }
      });

      Button cancel   = (Button)findViewById(R.id.cancel);
      cancel.setOnClickListener( new Button.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            dismiss();
         }
      });
   }
   
}
