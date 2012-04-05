package com.oss.soccerstats.view;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.oss.soccerstats.R;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Player;

public class ScoreGoalDialog extends Dialog
{
   /**
    * The columns we are interested in from the player table
    */
   private static final String[] PLAYER_PROJECTION = new String[] {
           Player._ID,           // 0
           Player.NAME,          // 1
           Player.JERSEY_NUMBER  // 2
   };

   private Cursor mPlayerCursor;

   private Context mContext;

   public interface OnGoalScoredEventListener
   {
      public void onGoalScoredHandler(String aPlayerIdForGoal,
                                      String aPlayerIdForAssist);
   }

   private OnGoalScoredEventListener mOnOkListener;

   public ScoreGoalDialog(Context context, 
            OnGoalScoredEventListener aOnOkListener)
   {
      super(context);
      mContext = context;
      mOnOkListener = aOnOkListener;
      setContentView(R.layout.goal_details);
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

      Spinner scoredBySpinner = (Spinner) findViewById(R.id.scoredBy);
      scoredBySpinner.setAdapter(adapter);
      Spinner assistedBySpinner = (Spinner) findViewById(R.id.assistedBy);
      assistedBySpinner.setAdapter(adapter);
      
      Button ok   = (Button)findViewById(R.id.ok);
      ok.setOnClickListener( new Button.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            View v2 = (View) v.getParent().getParent();            
            Spinner scoreSpinner = (Spinner)v2.findViewById(R.id.scoredBy);
            TextView scorePlayerIdField = (TextView)scoreSpinner.findViewById(R.id.playerID);
            String scorePlayerId = (String) scorePlayerIdField.getText();
            Spinner assistSpinner = (Spinner)v2.findViewById(R.id.assistedBy);
            TextView assistPlayerIdField = (TextView)assistSpinner.findViewById(R.id.playerID);
            String assistPlayerId = (String) assistPlayerIdField.getText();
            
            mOnOkListener.onGoalScoredHandler(scorePlayerId, assistPlayerId);
            
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
