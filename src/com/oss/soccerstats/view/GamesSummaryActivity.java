package com.oss.soccerstats.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.oss.soccerstats.R;
import com.oss.soccerstats.data.SoccerStatsDataHelper;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Game;
import com.oss.soccerstats.data.SoccerStatsDataHelper.PlayerStats;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Team;
import com.oss.soccerstats.intent.SoccerStatIntent;
import com.oss.soccerstats.util.Constants;
import com.oss.soccerstats.view.EditDeleteGameDialog.OnEditGameEventListener;

public class GamesSummaryActivity extends ListActivity
                                  implements OnEditGameEventListener
{
   /**
    * The columns we are interested in from the database
    */
   private static final String[] GAMES_PROJECTION = new String[] {
           SoccerStatsDataHelper.Game._ID,            // 0
           SoccerStatsDataHelper.Game.GAME_DATE,      // 1
           SoccerStatsDataHelper.Game.THEM_TEAM_NAME, // 2
           SoccerStatsDataHelper.Game.US_GOALS,       // 3
           SoccerStatsDataHelper.Game.THEM_GOALS,     // 4
           SoccerStatsDataHelper.Game.US_SOG,         // 5
           SoccerStatsDataHelper.Game.THEM_SOG,       // 6
           SoccerStatsDataHelper.Team.TEAM_NAME       // 7
   };
   private     static final int DIALOG_CONFIRM_DELETE_GAME  = 0;
   protected   static final int HEADER_ROW                  = 0;
   private Cursor mCursor;
   private EditDeleteGameDialog  mEditDeleteGameDialog;
   private String mDeleteThisGameId;
   private int mTeamId;

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setTitle(R.string.playerStatsTitle);
      
      Intent launchIntent = getIntent();
      mTeamId = launchIntent.getIntExtra(SoccerStatIntent.TEAM_ID, 
               SoccerStatsDataHelper.TEAM_ID_DEFAULT);
      mEditDeleteGameDialog   = new EditDeleteGameDialog(this, this);

      mCursor = managedQuery(Game.CONTENT_URI, GAMES_PROJECTION, 
               Game.TEAM_ID + "=" + mTeamId, null,
               Game.DEFAULT_SORT_ORDER);

      // Used to game rows from the database to views
      GameListAdapter adapter = new GameListAdapter(
              this, R.layout.games_list_item, mCursor,
              new String[] { Game.GAME_DATE, Game.THEM_TEAM_NAME, Game.US_GOALS, 
                             Game.US_SOG, Game._ID, Team.TEAM_NAME }, 
              new int[] { R.id.gameDate, R.id.opponent, R.id.score, 
                          R.id.shots_on_goal, R.id.gameID, R.id.teamName });
      
      ListView lv = getListView();
      LayoutInflater inflater = getLayoutInflater();
      ViewGroup headerRow = (ViewGroup)inflater.inflate(R.layout.games_header, 
                                                        lv, false);
      lv.addHeaderView(headerRow);
     
      lv.setOnItemClickListener(new OnItemClickListener() 
      {
         public void onItemClick(AdapterView<?> parent, View view,
                  int position, long aId)
         {
            if (position != HEADER_ROW)
            {
               TableLayout container  = (TableLayout)view;
               String gameId = 
                  (String) ((TextView)((TableRow) container.getChildAt(0)).
                                                            getChildAt(4)).
                                                            getText();
               
               Intent intent = new Intent(SoccerStatIntent.GAME_STATS_SUMMARY);
               intent.addCategory(SoccerStatIntent.DEFAULT);
               intent.putExtra(SoccerStatIntent.GAME_ID, Integer.parseInt(gameId));
               intent.putExtra(SoccerStatIntent.TEAM_ID, mTeamId);
               startActivity(intent);
            }
         }
      });
      
      lv.setOnItemLongClickListener(new OnItemLongClickListener() 
      {
         public boolean onItemLongClick(AdapterView<?> parent, View view,
                  int position, long aId)
         {
            if (position != HEADER_ROW)
            {
               TableLayout container  = (TableLayout)view;
               TableRow    row        = (TableRow)container.getChildAt(0);
               if ( row.getChildCount() > 1)
               {
                  TextView gameDate             = (TextView) row.getChildAt(0);
                  TextView themTeamName         = (TextView) row.getChildAt(1);
                  TextView gameId               = (TextView) row.getChildAt(4);
                  TextView ourTeamName          = (TextView) row.getChildAt(5);
                  TextView dialogOurTeamname    = (TextView)mEditDeleteGameDialog.findViewById(R.id.editUs);
                  EditText dialogThemTeamname   = (EditText)mEditDeleteGameDialog.findViewById(R.id.editThem);
                  TextView dialogGameDate       = (TextView)mEditDeleteGameDialog.findViewById(R.id.gameDate);
                  TextView dialogGameId         = (TextView)mEditDeleteGameDialog.findViewById(R.id.gameID);
                  
                  dialogOurTeamname.setText(ourTeamName.getText());
                  dialogThemTeamname.setText(themTeamName.getText());
                  dialogGameDate.setText(gameDate.getText());
                  dialogGameId.setText(gameId.getText());
                  mEditDeleteGameDialog.setTitle(R.string.editDeleteGameTitle);      
                  mEditDeleteGameDialog.show();
               }
            }
            
            return true;
         }
      });

      setListAdapter(adapter);
      
   }
      
   public void onEditGameHandler(String aOurTeamName, String aTheirTeamName,
            String aGameLocation, String aGameId, int aOperation)
   {
      if ( aOperation == OnEditGameEventListener.OP_CHANGE )
      {
         ContentValues gameValues = new ContentValues();
         gameValues.put(Game.THEM_TEAM_NAME, aTheirTeamName);
         gameValues.put(Game.MODIFIED_DATE, System.currentTimeMillis());
         getContentResolver().update(Game.CONTENT_URI, gameValues,
                  Game._ID + "=" + aGameId, null);
      }
      else if ( aOperation == OnEditGameEventListener.OP_DELETE)
      {
         mDeleteThisGameId = aGameId;
         showDialog(DIALOG_CONFIRM_DELETE_GAME);
      }
      else
      {
         throw new Error("Invalid operation code: " + aOperation);
      }
   }

   @Override
   protected Dialog onCreateDialog(int aDialogId)
   {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      
      builder.setMessage(getString(R.string.deleteGameMsg)).setCancelable(false)
               .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id)
                  {
                     GamesSummaryActivity.this.completeDeleteGame();
                  }
               }).setNegativeButton(getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id)
                           {
                              return;
                           }
                        });
      
      AlertDialog alert = builder.create();
      return alert;
   }

   private void completeDeleteGame()
   {
      Uri gameUri = ContentUris.withAppendedId(Game.CONTENT_URI, 
               Integer.parseInt(mDeleteThisGameId) );
      getContentResolver().delete(gameUri, null, null);
      
      getContentResolver().delete(PlayerStats.CONTENT_URI, 
               PlayerStats.GAME_ID + "=" + Integer.parseInt(mDeleteThisGameId), 
               null);
      
      mDeleteThisGameId = null;
   }

   private class GameListAdapter extends SimpleCursorAdapter
   {

      private int[]   colors     = new int[] {Constants.DARK_BACKGROUND, 
                                              Constants.LIGHT_BACKGROUND};
      private boolean colorOne   = true;

      public GameListAdapter(Context context, int layout, Cursor c,
                             String[] from, int[] to)
      {
         super(context, layout, c, from, to);
      }

      @Override
      public void bindView(View row, Context context, Cursor cursor) 
      {  
         super.bindView(row, context, cursor);

         //Game Date
         int dateIndex = 
            cursor.getColumnIndex(SoccerStatsDataHelper.Game.GAME_DATE);
         long gameDateTimestamp = cursor.getInt(dateIndex);
         gameDateTimestamp = gameDateTimestamp * Constants.MILLIS;
         Calendar calGameDate = Calendar.getInstance();
         calGameDate.setTimeInMillis(gameDateTimestamp);
         Date dateGameDate = calGameDate.getTime();
         SimpleDateFormat formatter = 
            new SimpleDateFormat( getString(R.string.dateFormat) );
         String strGameDate   = formatter.format(dateGameDate);         
         TextView gameDateColumn  = (TextView) row.findViewById(R.id.gameDate);
         gameDateColumn.setText(strGameDate);    
         
         //Opponent (them team name)
         int themTeamNameIndex = 
            cursor.getColumnIndex(SoccerStatsDataHelper.Game.THEM_TEAM_NAME);
         String themTeamNameValue = cursor.getString(themTeamNameIndex);
         TextView themTeamNameField = (TextView) row.findViewById(R.id.opponent);
         themTeamNameField.setText(themTeamNameValue);
         
         //Score
         formatScoreSog(row, cursor, SoccerStatsDataHelper.Game.US_GOALS,
                  SoccerStatsDataHelper.Game.THEM_GOALS, R.id.score);    
         
         //SOG
         formatScoreSog(row, cursor, SoccerStatsDataHelper.Game.US_SOG,
                  SoccerStatsDataHelper.Game.THEM_SOG, R.id.shots_on_goal);
         
         //GameID - hidden
         int gameIdIndex = 
            cursor.getColumnIndex(SoccerStatsDataHelper.Game._ID);
         int gameIdValue = cursor.getInt(gameIdIndex);
         TextView gameIdField = (TextView) row.findViewById(R.id.gameID);
         gameIdField.setText(Integer.toString(gameIdValue));

         //Team name - hidden
         int teamNameIndex = 
            cursor.getColumnIndex(SoccerStatsDataHelper.Team.TEAM_NAME);
         String teamNameValue = cursor.getString(teamNameIndex);
         TextView teamIdField = (TextView) row.findViewById(R.id.teamName);
         teamIdField.setText(teamNameValue);

         // Alternating row colors
          if (colorOne)    row.setBackgroundColor(colors[0]);
          else             row.setBackgroundColor(colors[1]);
          colorOne = ! colorOne;
      }

      private void formatScoreSog(View row, Cursor cursor, String column1,
                                  String column2, int textViewId)
      {
         int usScoreIndex = 
            cursor.getColumnIndex(column1);
         int scoreUs = cursor.getInt(usScoreIndex);
         int themScoreIndex = 
            cursor.getColumnIndex(column2);
         int themScore = cursor.getInt(themScoreIndex);
         String score      = scoreUs + " - " + themScore;
         TextView scoreColumn  = (TextView) row.findViewById(textViewId);
         scoreColumn.setText(score);
      }
      
   }

}
