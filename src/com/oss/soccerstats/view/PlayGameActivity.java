/////////////////////////////////////////////////////////////////
// PlayGameActivity.java
//
// SoccerSoft - Proprietary (Restricted)
// Solely for authorized persons having a need to know
//
// Copyright (c) 2010, 2011 SoccerSoft - All rights reserved.
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF SoccerSoft.
// The copyright notice above does not evidence any actual
// or intended publication of such source code.
/////////////////////////////////////////////////////////////////

package com.oss.soccerstats.view;

import java.util.ArrayList;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.oss.soccerstats.R;
import com.oss.soccerstats.data.SoccerStatsDataHelper;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Game;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Player;
import com.oss.soccerstats.data.SoccerStatsDataHelper.PlayerStats;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Team;
import com.oss.soccerstats.intent.SoccerStatIntent;
import com.oss.soccerstats.model.GameStats;
import com.oss.soccerstats.model.PlayerSubData;
import com.oss.soccerstats.model.TeamData;
import com.oss.soccerstats.util.Constants;
import com.oss.soccerstats.view.EditGameDialog.OnEditGameEventListener;
import com.oss.soccerstats.view.EditPlayerDialog.OnEditTeamEventListener;
import com.oss.soccerstats.view.GameEventDialog.OnGameEventListener;
import com.oss.soccerstats.view.NewGameDialog.OnInitGameEventListener;

public class PlayGameActivity extends Activity
                      implements OnGameEventListener, 
                                 OnEditTeamEventListener, 
                                 OnEditGameEventListener,
                                 OnInitGameEventListener
{
   private static final int CLOCK_TICK = 100; //milliseconds
   private static final long MILLIS = 1000L;   
   //-----------[ Game FSM data ]---------------------------------------------//
   //
   // Game FSM
   // START          -- press clock --> PLAY_FIRST
   // PLAY_FIRST     -- press clock --> PAUSE_FIRST   (Half button enabled)
   // PAUSE_FIRST    -- press clock --> PLAY_FIRST    (Half button disabled)
   // PAUSE_FIRST    -- press Half  --> BEGIN_SECOND  (Half button text change/enabled, Clock button text change)
   // BEGIN_SECOND   -- press Half  --> PAUSE_FIRST   (Half button text reset/enabled, Clock redisplays 1st time)
   // BEGIN_SECOND   -- press Clock --> PLAY_SECOND   (Half button disabled, clock displays 00:00)
   // PLAY_SECOND    -- press Clock --> PAUSE_SECOND  (Half button enabled)
   // PAUSE_SECOND   -- press Clock --> PLAY_SECOND   (Half button disabled)
   // PAUSE_SECOND   -- press Half  --> STOP_GAME     (Half button text change/enabled)
   // STOP_GAME      -- press Clock --> PAUSE_SECOND  (Half button text change/enabled)
   // STOP_GAME      -- press Half  --> FINISH        (Half button & Clock disabled)
   private enum GameState {
      START,
      PLAY_FIRST,
      PAUSE_FIRST,
      BEGIN_SECOND,
      PLAY_SECOND,
      PAUSE_SECOND,
      STOP_GAME,
      FINISH;     
   }
   
   private enum GameStateEvent {
      CLOCK_PRESSED,
      HALF_PRESSED,
      BACK_SOFT_BUTTON_PRESSED;
   }
   
   private GameState mGameState  = GameState.START;
   
   /**
    *  Needed in case user hits the "back" soft-button while the new game dialog is
    *  displayed.  In this case the user will exit from the game without saving.
    */
   private boolean   mCancelGame = true;

   //-----------[ Player and game data ]--------------------------------------//
   //

   //Model classes for stats data
   private GameStats mGameStats     = new GameStats();
   private ArrayList<TeamData>      mTeams;
   private ArrayList<PlayerSubData> mPlayers;
   private ArrayList<PlayerSubData> mPlayersFirstHalfBackup;
   private ArrayList<PlayerSubData> mOnFieldPlayers;
   private ArrayList<PlayerSubData> mOnFieldPlayersFirstHalfBackup;

   private PlayerSubDataAdapter     mSubsAdapter;

   private int mGameId     = SoccerStatsDataHelper.GAME_ID_DEFAULT;
   private int mSeasonId   = SoccerStatsDataHelper.SEASON_ID_DEFAULT;
   private int mTeamId     = SoccerStatsDataHelper.TEAM_ID_DEFAULT;

   /**
    * The columns we are interested in from the playerStats table needed for
    * stats updates.
    */
   private static final String[] PLAYERSTATS_PROJECTION = new String[] {
           PlayerStats._ID,            // 0
           PlayerStats.PLAYER_ID,      // 1
           PlayerStats.GOALS,          // 2
           PlayerStats.SOGS,           // 3
           PlayerStats.ASSISTS,        // 4
           PlayerStats.SAVES,          // 5
           PlayerStats.AGAINST         // 5
   };

   /**
    * The columns we are interested in from the playerStats table used to 
    * initialize the substitutions list.
    */
   private static final String[] PLAYER_SUBS_PROJECTION = new String[] {
           Player._ID,           // 0
           Player.JERSEY_NUMBER, // 1
           Player.NAME           // 2
   };

   /**
    * The columns we are interested in from the team table used to 
    * initialize the mTeams list.
    */
   private static final String[] TEAM_PROJECTION = new String[] {
           Team._ID,           // 0
           Team.TEAM_NAME      // 1
   };

   // Player substitution data/mgt
   private static final boolean INITIAL_IS_ON_FIELD = false;
   private static final int INITIAL_TIME_IN_STATE = 0;
   
   // Undo support
   private Command mLastCommand = null;

   //-----------[ Dialogs ]---------------------------------------------------//
   //
   private static final int DIALOG_CLOSE_PLAY_GAME = 0;
   private static final int DIALOG_HELP_PLAY_GAME  = 1;
   private static final int DIALOG_NO_PLAYERS      = 2;

   private GameEventDialog mEventDialog;
   private Dialog          mAddPlayerDialog;
   private EditGameDialog  mEditGameDialog;
   private NewGameDialog   mNewGameDialog;

   //-----------[ Data & Handlers for time mgt ]------------------------------//
   //
   private Handler mGameClockHandler   = new Handler();
   private Runnable mUpdateClockTimeTask = new Runnable() {
      public void run() {
          final long start = mStartTime;
          final long now   = SystemClock.elapsedRealtime();
          long playTimeInMillis = now - start + mAccumulatedTime;
          int seconds = (int) (playTimeInMillis / 1000);
          int minutes = seconds / 60;
          seconds     = seconds % 60;

          mGameClockButton.setText(String.format("%02d:%02d", minutes, seconds));
                  
          updatePlayerSubStats(playTimeInMillis);
          
          mGameClockHandler.postDelayed(this, CLOCK_TICK);
      }
   };

   //Timer start time; used to determine how far the game is into the half.
   private long mStartTime = 0L;
   //Tracks time accumulated across multiple stop-watch stop/start cycles
   private long mAccumulatedTime = 0L;
   //Tracks last time player time on/off field stats were updated
   private long mLastPlayerUpdateTime = 0;

   // Track current clock value when clock paused, useful for undoing game half
   // changes (e.g., mistakenly move to second half and need to move back to 
   // the first half).
   private static final String OBVIOUSLY_INVALID_GAME_CLOCK_TIME = "XX:XX";
   private String mGameClockPausedTime = OBVIOUSLY_INVALID_GAME_CLOCK_TIME;

   //-----------[ UI controls ]-----------------------------------------------//
   //
   private Button    mGameClockButton;
   private Button    mWhichHalfButton;
   private ListView  mSubsListView;
   private Button    mGoalEventButton;
   private Button    mSogEventButton;   
   
   private Drawable  mNormalButtonBackground;
   private Drawable  mDisabledButtonBackground;
   private Drawable  mStoppedButtonBackground;

   protected boolean mIsTimerPaused = true;

   private static final float NORMAL_BUTTON_TEXT_SIZE    = 22; //scaled pixels
   private static final float REDUCED_BUTTON_TEXT_SIZE   = 18; //scaled pixels

   
   
   
   
   
   
   //
   //-----------[ METHOD IMPLEMENTATION ]-------------------------------------//
   //
   
   
   
   //-----------[ Lifecycle methods ]-----------------------------------------//
   //
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      Resources   resources = getResources();
      mStoppedButtonBackground = resources.getDrawable(
                                 R.drawable.red_button_gradient_rounded_50x50);
      setTitle(R.string.gamePlayTitle);
      setContentView(R.layout.gameplay2);
      
      initTeamData();
      mNewGameDialog = new NewGameDialog(this, this, mTeams);
      mNewGameDialog.setTitle(R.string.newGameTitle);
      
      mNewGameDialog.show();
   }

   @Override
   protected Dialog onCreateDialog(int aDialogId)
   {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      if (DIALOG_CLOSE_PLAY_GAME == aDialogId)
      {
         builder.setMessage(getString(R.string.endGameMsg)).setCancelable(false)
                  .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id)
                     {
                        PlayGameActivity.this.gameStateChange(GameStateEvent.BACK_SOFT_BUTTON_PRESSED);
                        PlayGameActivity.this.finish();
                     }
                  }).setNegativeButton(getString(R.string.no),
                           new DialogInterface.OnClickListener() {
                              public void onClick(DialogInterface dialog, int id)
                              {
                                 return;
                              }
                           });
      }
      else if (DIALOG_NO_PLAYERS == aDialogId)
      {
         builder.setTitle(getString(R.string.noPlayers)).setCancelable(false)
                  .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id)
                     {
                        showAddPlayerDialog();
                     }
                  }).setNegativeButton(getString(R.string.cancel),
                           new DialogInterface.OnClickListener() {
                              public void onClick(DialogInterface dialog, int id)
                              {
                                 return;
                              }
                           })
                  .setMessage(getString(R.string.noPlayersMsg));
      }
      else
      {
         builder.setTitle(R.string.help)
            .setNeutralButton(getString(R.string.dismiss),
                           new DialogInterface.OnClickListener() {
                              public void onClick(DialogInterface dialog, int id)
                              {
                                 return;
                              }
                           })
            .setMessage(getString(R.string.gamePlayText))
            .setCancelable(true);
      }
      
      AlertDialog alert = builder.create();

      return alert;
   }

   @Override
   public boolean onCreateOptionsMenu(Menu aMenu)
   {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.game_play_menu, aMenu);
      return true;
   }

  private void initNewGame(String aThemTeamName, String aLocation)
  {
      //TODO:Accept values for SeasonID
      mGameStats.setGameLocation(aLocation);
      mGameStats.setTheirTeamName(aThemTeamName);
      String teamName = getTeamName(mTeamId);
      mGameStats.setOurTeamName(teamName);

      // Insert new game
      ContentValues values = new ContentValues();
      values.put(Game.TEAM_ID, mTeamId);
      values.put(Game.LOCATION, aLocation);
      values.put(Game.THEM_TEAM_NAME, aThemTeamName);
      values.put(Game.US_GOALS, 0);
      values.put(Game.THEM_GOALS, 0);
      values.put(Game.US_SOG, 0);
      values.put(Game.THEM_SOG, 0);            
      Uri gameUri = getContentResolver().insert(Game.CONTENT_URI, values);
      String gameId = gameUri.getLastPathSegment();
      mGameId = Integer.parseInt(gameId);

      // initialize new playerStats rows for the game, using the game ID
      for (PlayerSubData player : mPlayers)
      {
         //TODO:Accept values for SeasonID, TeamID, ThemTeamName, and Location
         initPlayerStats(player.id);
      }
   }

   private void initControls()
   {
      mSubsListView = (ListView)findViewById(R.id.subsView);
      mSubsAdapter = new PlayerSubDataAdapter(this, 
                                              R.layout.substitutions_list_item, 
                                              mPlayers);
      mSubsAdapter.sort(mSubsAdapter);
      mSubsListView.setAdapter(mSubsAdapter);

      mGoalEventButton = (Button)findViewById(R.id.score);
      mGoalEventButton.setText(Html.fromHtml(getScore()));

      mWhichHalfButton  = (Button)findViewById(R.id.whichHalf);

      mGameClockButton  = (Button)findViewById(R.id.timePlayed);
      mGameClockButton.setText(R.string.startTime);
      mNormalButtonBackground = mGameClockButton.getBackground();
      mGameClockButton.getWidth();
      mGameClockButton.getHeight();

      mSogEventButton = (Button)findViewById(R.id.SOG);
      mSogEventButton.setText(Html.fromHtml(getSog()));
      
      mGoalEventButton.setEnabled(false);
      mSogEventButton.setEnabled(false);
      mWhichHalfButton.setEnabled(false);
      
      mDisabledButtonBackground = mWhichHalfButton.getBackground();
   }

   //-----------[ UI Event Handlers ]-----------------------------------------//
   //
   @Override
   public boolean onOptionsItemSelected(MenuItem aMenuItem) 
   {
      // Handle item selection
      switch (aMenuItem.getItemId())
      {
         case R.id.undoLast:
         {
            undoLastCommand();
           return true;
         }
         case R.id.addPlayer:
         {
           showAddPlayerDialog();
           return true;
         }
         case R.id.editGame:
         {
           showEditGameDialog();
           return true;
         }
         case R.id.help:
         {
           showDialog(DIALOG_HELP_PLAY_GAME);
           return true;
         }
         default:
            return false;
      }
   }

   private void initEventListeners()
   {
      mGameClockButton.setOnClickListener(new View.OnClickListener() {

         public void onClick(View view)
         {
            gameStateChange(GameStateEvent.CLOCK_PRESSED);
         }
      });
      
      mWhichHalfButton.setOnClickListener(new View.OnClickListener() {
      
         public void onClick(View view)
         {
            gameStateChange(GameStateEvent.HALF_PRESSED);            
            mSubsListView.requestLayout();
         }
      });
            
      mSogEventButton.setOnClickListener(new View.OnClickListener() {      
         public void onClick(View view)
         {
            mEventDialog.setTitle(R.string.sogDetails); 
            mEventDialog.setIsGoalDialog(false);
            mEventDialog.show();
         }
      });
         
      mSogEventButton.setOnLongClickListener(new View.OnLongClickListener() {      
         public boolean onLongClick(View view)
         {
            if (mLastCommand != null)
            {
               switch (mLastCommand.getCommand())
               {
                  case Command.COMMAND_SOG:
                  {
                     undoSogUs();
                     break;
                  }
                  case Command.COMMAND_SAVE:
                  {
                     undoSogThem();
                     break;
                  }

                  default:
                     break;
               }
               
               mSogEventButton.setText( Html.fromHtml(getSog()) );
               mLastCommand = null;
               return true;
            }
            return false;
         }
      });
         
      mGoalEventButton.setOnClickListener(new View.OnClickListener() {

         public void onClick(View view)
         {
            mEventDialog.setTitle(R.string.goalDetails);      
            mEventDialog.setIsGoalDialog(true);   
            mEventDialog.show();
         }
      });

      mGoalEventButton.setOnLongClickListener(new View.OnLongClickListener() {      
         public boolean onLongClick(View view)
         {
            if (mLastCommand != null)
            {
               switch (mLastCommand.getCommand())
               {
                  case Command.COMMAND_GOAL:
                  {
                     undoScoreUs();
                     break;
                  }
                  case Command.COMMAND_GOAL_AGAINST:
                  {
                     undoScoreThem();
                     break;
                  }

                  default:
                     break;
               }
               
               mGoalEventButton.setText( Html.fromHtml(getScore()) );
               mLastCommand = null;
               return true;
            }
            return false;
         }
      });
   } //END - initEventListeners()
     
   private void enableStatButtons()
   {
      mGoalEventButton.setEnabled(true);
      mSogEventButton.setEnabled(true);
      mWhichHalfButton.setEnabled(false);
      //HACK to fix problem with disabled state displaying incorrectly.
      mWhichHalfButton.setBackgroundDrawable(mDisabledButtonBackground);
   }

   private void disableStatButtons()
   {
      mGoalEventButton.setEnabled(false);
      mSogEventButton.setEnabled(false);
      mWhichHalfButton.setEnabled(true);
   }  

   @Override
   protected void onStop()
   {
      super.onStop();
   }


   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event)  {
       if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
               && keyCode == KeyEvent.KEYCODE_BACK
               && event.getRepeatCount() == 0) {
           // Take care of calling this method on earlier versions of
           // the platform where it doesn't exist.
           onBackPressed();
           return true;
       }

       return super.onKeyDown(keyCode, event);
   }

   @Override
   public void onBackPressed() {
      // This will be called either automatically for you on 2.0
      // or later, or by the code above on earlier versions of the
      // platform.
      showDialog(DIALOG_CLOSE_PLAY_GAME);
   }   
   
   //-----------[ Data access ]-----------------------------------------------//
   //
   private String getTeamName(int aTeamId)
   {
      String[] teamProjection = {Team.TEAM_NAME};
      Cursor cursor = getContentResolver().query(Team.CONTENT_URI, 
               teamProjection, SoccerStatsDataHelper.TEAM_TABLE_NAME + "." 
               + Team._ID + "=" + aTeamId, null,
               Team.DEFAULT_SORT_ORDER);
      cursor.moveToFirst();
      String teamName = cursor.getString(0);
      cursor.close();
      return teamName;
   }


   //-----------[ Team management ]-------------------------------------------//
   //

   private void initTeamData()
   {
      mTeams          = new ArrayList<TeamData>();
      
      Cursor cursor = getContentResolver().query(Team.CONTENT_URI, 
               TEAM_PROJECTION, null, null,
               Team.DEFAULT_SORT_ORDER);
      cursor.moveToFirst();
      
      for (int i = 0; i < cursor.getCount(); i++ )
      {
         int idColumnIndex     = cursor.getColumnIndex(SoccerStatsDataHelper.Team._ID);
         int nameColumnIndex   = cursor.getColumnIndex(SoccerStatsDataHelper.Team.TEAM_NAME);
         int teamId            = cursor.getInt(idColumnIndex);
         String teamName       = cursor.getString(nameColumnIndex);
         TeamData team    = new TeamData(teamId, teamName);
         
         mTeams.add(team);
         cursor.moveToNext();
      }

      cursor.close();
   }
   
   public void onNewGameHandler(int    aOurTeamId, 
                                 String aOurTeamName,
                                 String aTheirTeamName,
                                 String aGameLocation)
   {
      mCancelGame = false;
      
      mTeamId = aOurTeamId;
      
      String title = getString(R.string.mainTitle) + " - " + aOurTeamName;
      if ( null != aTheirTeamName && !aTheirTeamName.equals("") )
      {
         title = title + " v. " + aTheirTeamName;
      }
      setTitle(title);
      
      initPlayerData();
      mEventDialog      = new GameEventDialog(this, this, mOnFieldPlayers);
      mEditGameDialog   = new EditGameDialog(this, this);
      mAddPlayerDialog  = new EditPlayerDialog(this, this);     
      
      initNewGame(aTheirTeamName, aGameLocation);           
      initControls();
      initEventListeners();
      
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      
      if (mPlayers.size() == 0)
      {
         showDialog(DIALOG_NO_PLAYERS);
      }      
   }
   
   public void onCancelGameHandler()
   {
      PlayGameActivity.this.gameStateChange(GameStateEvent.BACK_SOFT_BUTTON_PRESSED);
      PlayGameActivity.this.finish();
   }

   //-----------[ Game management ]-------------------------------------------//
   //

   public void onEditGameHandler(String aOurTeamName, String aTheirTeamName,
                                 String aGameLocation, String aGameId)
   {
      mGameStats.setOurTeamName(aOurTeamName);
      mGameStats.setTheirTeamName(aTheirTeamName);
      mGameStats.setGameLocation(aGameLocation);

      ContentValues teamValues = new ContentValues();
      teamValues.put(Team.TEAM_NAME, aOurTeamName);
      getContentResolver().update(Team.CONTENT_URI, teamValues,
                  Team._ID + "=" + mTeamId, null);
      
      ContentValues gameValues = new ContentValues();
      gameValues.put(Game.THEM_TEAM_NAME, aTheirTeamName);
      gameValues.put(Game.LOCATION, aGameLocation);
      getContentResolver().update(Game.CONTENT_URI, gameValues,
               Game._ID + "=" + mGameId, null);
   }
   
   private void showEditGameDialog()
   {
      EditText usTeamName = (EditText)mEditGameDialog.findViewById(R.id.editUs);
      usTeamName.setText(mGameStats.getOurTeamName());
      EditText theirTeamName = (EditText)mEditGameDialog.findViewById(R.id.editThem);
      theirTeamName.setText(mGameStats.getTheirTeamName());
      theirTeamName.requestFocus();
      EditText gameLocation = (EditText)mEditGameDialog.findViewById(R.id.gameLocation);
      gameLocation.setText(mGameStats.getGameLocation());
      mEditGameDialog.setTitle(R.string.editGameTitle);      
      mEditGameDialog.show();
   }
   
   //-----------[ Player sub management ]-------------------------------------//
   //

   // Defined in layout xml file (substitutions_list_item.xml)
   public void subClickHandler(View aView)
   {
      TableLayout row           = (TableLayout) (aView.getParent().getParent());
      int listPosition           = (Integer) row.getTag();
      PlayerSubData playerData   = mPlayers.get(listPosition);
      
      updatePlayTimeStats(playerData);
      if (playerData.onFieldBool)
      {
         playerData.onFieldBool = false;
         mOnFieldPlayers.remove(playerData);
      }
      else
      {
         playerData.onFieldBool = true;
         mOnFieldPlayers.add(playerData);
      }

      playerData.timeInStateInMillis     = 0; //seconds
      mSubsAdapter.sort(mSubsAdapter);
      mSubsAdapter.notifyDataSetChanged();      
      mSubsListView.startLayoutAnimation();
      
      mEventDialog.notifyDataSetChanged();
   }

   private void restoreFirstHalfPlayerStats()
   {
      int mPlayersSize = mPlayers.size();
      if ( mPlayersSize != mPlayersFirstHalfBackup.size() )
      {
         throw new 
            RuntimeException( "PlayGameActivity.restoreFirstHalfPlayerStats: " +
         		   "mPlayers.size() != mPlayersFirstHalfBackup.size()" );
      }
      
      mPlayers.clear();
      for (PlayerSubData playerData: mPlayersFirstHalfBackup)
      {
         mPlayers.add(playerData);
      }
      mSubsAdapter.sort(mSubsAdapter);
      mSubsAdapter.notifyDataSetChanged();
      mPlayersFirstHalfBackup.clear();
      mPlayersFirstHalfBackup = null;
      
      mOnFieldPlayers.clear();
      for (PlayerSubData onFieldPlayer : mOnFieldPlayersFirstHalfBackup)
      {
         mOnFieldPlayers.add(onFieldPlayer);
      }
      mOnFieldPlayersFirstHalfBackup.clear();
      mOnFieldPlayersFirstHalfBackup = null;
   }
   
   /**
    * Backing the first half stats up serves 2 purposes:
    *    1. A way to restore all as it was in case the user returns to the
    *       1st half instead of actually starting the 2nd half.
    *    2. Capture the time related stats so that player stats can be updated
    *       with the 1st half results if the 2nd half is actually started
    *       (@see secondHalfPlayerStatsReset()).
    */
   private void backupFirstHalfPlayerStats()
   {
      mPlayersFirstHalfBackup = null;
      mPlayersFirstHalfBackup = new ArrayList<PlayerSubData>();
      for (PlayerSubData playerData : mPlayers)
      {
         PlayerSubData backupPlayerData = 
            new PlayerSubData(playerData.id, playerData.jerseyNo, playerData.name, 
                     playerData.timeInStateInMillis, playerData.onFieldBool);
         backupPlayerData.benchTimeInMillis = playerData.benchTimeInMillis;
         backupPlayerData.fieldTimeInMillis = playerData.fieldTimeInMillis;
         mPlayersFirstHalfBackup.add(backupPlayerData);
         playerData.timeInStateInMillis = 0;
         playerData.onFieldBool = false;
      }
      mSubsAdapter.sort(mSubsAdapter);
      mSubsAdapter.notifyDataSetChanged();
      
      mOnFieldPlayersFirstHalfBackup = null;
      mOnFieldPlayersFirstHalfBackup = new ArrayList<PlayerSubData>();
      PlayerSubData otherPlayer      = null;
      for (PlayerSubData onFieldPlayers : mOnFieldPlayers)
      {
         mOnFieldPlayersFirstHalfBackup.add(onFieldPlayers);
         if (onFieldPlayers.name.equals("Other"))
         {
            otherPlayer = onFieldPlayers;
         }
      }
      mOnFieldPlayers.clear();
      mOnFieldPlayers.add(otherPlayer);
   }

   /**
    * Just before starting the clock for the 2nd half 2 things need to happen:
    *    1. Recover the time related stats from the 1st half (from the backup
    *       made when the user indicated they might start the 2nd half).
    *    2. Update each player's time related stats from the first half.  These
    *       can only be updated after the second half is started.  Otherwise,
    *       if the user returns to playing the first half (i.e., they change
    *       their mind about starting the 2nd half) the players timeInState and
    *       bench/field times will have been prematurely modified. This will
    *       result in incorrect time displays in the substitution toggle buttons 
    *       when returning to 1st half play.
    */
   private void saveFirstHalfPlayerStats()
   {
      int mPlayersSize = mPlayers.size();
      if ( mPlayersSize != mPlayersFirstHalfBackup.size() )
      {
         throw new 
            RuntimeException( "PlayGameActivity.secondHalfPlayerStatsReset: " +
                  "mPlayers.size() != mPlayersFirstHalfBackup.size()" );
      }

      for (int i = 0; i < mPlayersFirstHalfBackup.size(); i++)
      {
         // ------------------------------------------------------------------//
         // -------[ Recover time related stats from first half ]-------------//
         // ------------------------------------------------------------------//
         PlayerSubData backupPlayerData   = mPlayersFirstHalfBackup.get(i);
         //Array sort orders may differ, need to ensure that the same player 
         //from both arrays is being used.
         PlayerSubData playerData         = getMatchingPlayer(backupPlayerData);
         //onField status may have changed from 1st half.
         boolean currentOnField           = playerData.onFieldBool;
         //Need to restore these fields from the backup prior to updating the
         //time related stats.
         playerData.onFieldBool           = backupPlayerData.onFieldBool;
         playerData.timeInStateInMillis   = backupPlayerData.timeInStateInMillis;
         playerData.benchTimeInMillis     = backupPlayerData.benchTimeInMillis;
         playerData.fieldTimeInMillis     = backupPlayerData.fieldTimeInMillis;
                  
         // ------------------------------------------------------------------//
         // -------[ Now update stats for 2nd half ]--------------------------//
         // ------------------------------------------------------------------//
         updatePlayTimeStats(playerData);         
         //Now set remaining state for beginning of 2nd half
         playerData.onFieldBool           = currentOnField;
         playerData.timeInStateInMillis = 0;
      }
      
      mPlayersFirstHalfBackup.clear();
      mPlayersFirstHalfBackup = null;
      mSubsAdapter.sort(mSubsAdapter);
      mSubsAdapter.notifyDataSetChanged();
   }

   /**
    * Finds the matching mPlayers entry for the associated aBackupPlayer.
    * 
    * @param aBackupPlayerData
    * @return
    */
   private PlayerSubData getMatchingPlayer(PlayerSubData aBackupPlayer)
   {
      int            id             = aBackupPlayer.id;
      PlayerSubData  retPlayerData  = null;
      
      for (PlayerSubData playerData : mPlayers)
      {
         if ( id == playerData.id )    retPlayerData = playerData;
      }
      
      return retPlayerData;
   }
      

   private void updatePlayerSubStats(long aHalfTimerInMillis)
   {
      final int PLAYER_UPDATE_INTERVAL = 5000; // 5 seconds
      
      if ( aHalfTimerInMillis >= mLastPlayerUpdateTime + PLAYER_UPDATE_INTERVAL  )
      {
//         long start = SystemClock.elapsedRealtime();
         
         long interval = aHalfTimerInMillis - mLastPlayerUpdateTime;
         
         for (PlayerSubData playerData : mPlayers)
         {
            playerData.timeInStateInMillis = playerData.timeInStateInMillis + interval;
         }
         mLastPlayerUpdateTime = aHalfTimerInMillis;
         mSubsAdapter.sort(mSubsAdapter);
         mSubsAdapter.notifyDataSetChanged();
         
//         long end = SystemClock.elapsedRealtime();
//         long statUpdateDuration = end - start;
//         Log.i(TAG, "Interval to update player time stats: " + 
//               statUpdateDuration + "(ms)");
      }
      
   }

   private void updatePlayTimeStats(PlayerSubData playerData)
   {
      if (playerData.onFieldBool)
      {
         playerData.fieldTimeInMillis += playerData.timeInStateInMillis;
      }
      else
      {
         playerData.benchTimeInMillis += playerData.timeInStateInMillis;
      }
   }

   private void initPlayerData()
   {
      mPlayers          = new ArrayList<PlayerSubData>();
      mOnFieldPlayers   = new ArrayList<PlayerSubData>();
      
      Cursor cursor = getContentResolver().query(Player.CONTENT_URI, 
               PLAYER_SUBS_PROJECTION, SoccerStatsDataHelper.PLAYER_TABLE_NAME + "." 
               + Player.TEAM_ID + "=" + mTeamId, null,
               Player.DEFAULT_SORT_ORDER);
      cursor.moveToFirst();
      
      for (int i = 0; i < cursor.getCount(); i++ )
      {
         int idColumnIndex       = cursor.getColumnIndex(SoccerStatsDataHelper.Player._ID);
         int jerseyColumnIndex   = cursor.getColumnIndex(SoccerStatsDataHelper.Player.JERSEY_NUMBER);
         int nameColumnIndex     = cursor.getColumnIndex(SoccerStatsDataHelper.Player.NAME);
         int playerId            = cursor.getInt(idColumnIndex);
         String playerJerseyNo   = cursor.getString(jerseyColumnIndex);
         String playerName       = cursor.getString(nameColumnIndex);
         PlayerSubData player    = new PlayerSubData(playerId, playerJerseyNo, playerName, 
                                       INITIAL_TIME_IN_STATE, INITIAL_IS_ON_FIELD);
         
         //The "Other", anonymous, player is always on-field and never shown in
         //the subs list.
         if (playerName.equals("Other"))
         {
            mOnFieldPlayers.add(player);
         }
         else
         {
            mPlayers.add(player);
         }
         cursor.moveToNext();
      }

      cursor.close();
   }

   private void disableSubstitutionControls()
   {
      int numSubRows = mSubsListView.getChildCount();
      for (int i = 0; i < numSubRows; i++)
      {
         View v = mSubsListView.getChildAt(i);
         ToggleButton tb = (ToggleButton) v.findViewById(R.id.onFieldToggle);
         tb.setEnabled(false);
      }
   }
   //-----------[ Roster management ]-----------------------------------------//
   //
   /**
    * Implementation of OnEditPlayerEventListener interface.
    */
   public void onEditPlayerHandler(String aPlayerName,
            String aPlayerJerseyNumber, String aPlayerId, int aOperation,
            boolean aAddNewPlayer)
   {
      if ( aOperation == OnEditTeamEventListener.OP_ADD)
      {
         int playerId = addPlayerToProvider(aPlayerName, aPlayerJerseyNumber, aOperation);
         initPlayerStats(playerId);
      }
      else
      {
         throw new Error("Invalid operation code: " + aOperation);
      }
      
      if (aAddNewPlayer)   showAddPlayerDialog();
   }

   private int addPlayerToProvider(String aPlayerName, String aPlayerJerseyNumber,
            int aOperation) throws Error
   {
      long now = System.currentTimeMillis()/MILLIS;
      ContentValues values = new ContentValues();

      values.put(Player.MODIFIED_DATE, now);
      values.put(Player.NAME, aPlayerName);
      values.put(Player.JERSEY_NUMBER, aPlayerJerseyNumber);
      values.put(Player.TEAM_ID, mTeamId);

      // Commit all of our changes to persistent storage. When the update
      // completes the content provider will notify the cursor of the change, 
      // which will cause the UI to be updated.
      int playerIdIntValue;
      if ( aOperation == OnEditTeamEventListener.OP_ADD)
      {
         values.put(Player.CREATED_DATE, now);
         Uri uri = getContentResolver().insert(Player.CONTENT_URI, values);
         String playerId = uri.getLastPathSegment();
         playerIdIntValue = Integer.parseInt(playerId);
         addNewPlayerSubDataItem(playerIdIntValue, aPlayerJerseyNumber, aPlayerName);
      }
      else
      {
         throw new Error("Invalid operation code: " + aOperation);
      }
      return playerIdIntValue;
   }
   
   private void initPlayerStats(int aPlayerID) throws Error
   {
      long now = System.currentTimeMillis()/MILLIS;
      ContentValues values = new ContentValues();
      values.put(PlayerStats.CREATED_DATE, now);
      values.put(PlayerStats.MODIFIED_DATE, now);
      values.put(PlayerStats.SEASON_ID, mSeasonId );
      values.put(PlayerStats.TEAM_ID, mTeamId);
      values.put(PlayerStats.GAME_ID, mGameId);
      values.put(PlayerStats.GOALS, 0);
      values.put(PlayerStats.SOGS, 0);
      values.put(PlayerStats.ASSISTS, 0);
      values.put(PlayerStats.BENCH_TIME, 0);
      values.put(PlayerStats.PLAY_TIME, 0);
      values.put(PlayerStats.SAVES, 0);            
      values.put(PlayerStats.AGAINST, 0);    
      
      values.put(PlayerStats.PLAYER_ID, aPlayerID);
      getContentResolver().insert(PlayerStats.CONTENT_URI, values);   

   }
   
   private void showAddPlayerDialog()
   {
      EditText jersey = (EditText)mAddPlayerDialog.findViewById(R.id.editJerseyNumber);
      EditText name   = (EditText)mAddPlayerDialog.findViewById(R.id.editPlayerName);
      jersey.setText("");
      name.setText("");
      jersey.setHint(R.string.jerseyNumber);
      name.setHint(R.string.playerName);
      mAddPlayerDialog.setTitle(R.string.addPlayer);      
      mAddPlayerDialog.show();
   }
   
   private void addNewPlayerSubDataItem(int aId, String aJerseyNo, String aName)
   {
      PlayerSubData newPlayer = new PlayerSubData(aId, aJerseyNo, aName, 0, false);
      mPlayers.add(newPlayer);
      mSubsAdapter.sort(mSubsAdapter);
      mSubsAdapter.notifyDataSetChanged();
   }
   
   //-----------[ Score/SOG management ]--------------------------------------//
   //
   private String getSog()
   {
      String sogTitle   = getString(R.string.SOG);
      String sog        = Integer.toString(mGameStats.getSogUs()) + " - "
                        + Integer.toString(mGameStats.getSogThem());
      return styledStats(sogTitle, sog);
   }

   private String styledStats(String aTitle, String aStat)
   {
      return "<b>" + aTitle + "</b><br><center>" + aStat + "</center>";
   }

   private String getScore()
   {
      String scoreTitle = getString(R.string.score);
      String score      = Integer.toString(mGameStats.getScoreUs()) + " - "
                        + Integer.toString(mGameStats.getScoreThem());
      return styledStats(scoreTitle, score);
   }
   
   //-----------[ Player stats management ]-----------------------------------//
   //
   @Override
   public void onGameEventHandler(String aPlayerId1, String aPlayerId2, int aAction)
   {
      ContentResolver cr = getContentResolver();
      
      if (GameEventDialog.GOAL == aAction)
      {
         int goals   = updatePlayerStats(aPlayerId1, cr, PlayerStats.GOALS);
         int assists = updatePlayerStats(aPlayerId2, cr, PlayerStats.ASSISTS);
         mGameStats.scoreUs();
         mGoalEventButton.setText(Html.fromHtml(getScore()));
   
         mLastCommand = new Command( Integer.parseInt(aPlayerId1), 
                                   goals,
                                   Integer.parseInt(aPlayerId2),
                                   assists,
                                   Command.NULL_PLAYER_ID,
                                   0 /*no saves */,
                                   Command.NULL_PLAYER_ID,
                                   0 /*no sogs */,
                                   Command.NULL_PLAYER_ID,                                   
                                   0 /*no goals against */,
                                   Command.COMMAND_GOAL );
      }
      else if (GameEventDialog.SOG == aAction)
      {
         int sogs = updatePlayerStats(aPlayerId1, cr, PlayerStats.SOGS);
         mGameStats.shotOnGoalUs();
         mSogEventButton.setText(Html.fromHtml(getSog()));
 
         mLastCommand = new Command( Command.NULL_PLAYER_ID,
                                 0 /*no goals */,
                                 Command.NULL_PLAYER_ID,
                                 0 /*no assists */,
                                 Command.NULL_PLAYER_ID,
                                 0 /*no saves */,
                                 Integer.parseInt(aPlayerId1), 
                                 sogs,
                                 Command.NULL_PLAYER_ID,                                   
                                 0 /*no goals against */,
                                 Command.COMMAND_SOG);
      }
      else if (GameEventDialog.SAVE == aAction)
      {
         int saves = updatePlayerStats(aPlayerId1, cr, PlayerStats.SAVES);
         mGameStats.shotOnGoalThem();
         mSogEventButton.setText(Html.fromHtml(getSog()));
         
         mLastCommand = new Command( Command.NULL_PLAYER_ID,
                                   0 /*no goals */,
                                   Command.NULL_PLAYER_ID,
                                   0 /*no assists */,
                                   Integer.parseInt(aPlayerId1), 
                                   saves,
                                   Command.NULL_PLAYER_ID,                                   
                                   0 /*no sogs */,
                                   Command.NULL_PLAYER_ID,                                   
                                   0 /*no goals against */,
                                   Command.COMMAND_SAVE);
      }
      else
      {
         int goalsAgainst = updatePlayerStats(aPlayerId1, cr, PlayerStats.AGAINST);
         mGameStats.scoreThem();
         mGoalEventButton.setText(Html.fromHtml(getScore()));
         
         mLastCommand = new Command( Command.NULL_PLAYER_ID,
                                   0 /*no goals */,
                                   Command.NULL_PLAYER_ID,
                                   0 /*no assists */,
                                   Command.NULL_PLAYER_ID, 
                                   0 /*no saves */,
                                   Command.NULL_PLAYER_ID,                                   
                                   0 /*no sogs */,
                                   Integer.parseInt(aPlayerId1),                                                                      
                                   goalsAgainst,
                                   Command.COMMAND_GOAL_AGAINST);
      }
   }

   private int updatePlayerStats(String aPlayerStatsId,
            ContentResolver aContentResolver, String aColumnName)
   {
      int updatedColumnValue = 0;
      Cursor cursor = aContentResolver.query(PlayerStats.CONTENT_URI, 
               PLAYERSTATS_PROJECTION, 
               PlayerStats.PLAYER_ID + "=" + aPlayerStatsId
               + " AND " + SoccerStatsDataHelper.PLAYERSTATS_TABLE_NAME + "." 
                         + PlayerStats.SEASON_ID + "=" + mSeasonId
               + " AND " + SoccerStatsDataHelper.PLAYERSTATS_TABLE_NAME + "." 
                         + PlayerStats.GAME_ID + "=" + mGameId
               + " AND " + SoccerStatsDataHelper.PLAYERSTATS_TABLE_NAME + "." 
                         + PlayerStats.TEAM_ID + "=" + mTeamId, 
               null,
               PlayerStats.DEFAULT_SORT_ORDER);
      cursor.moveToFirst();
      
      long now = System.currentTimeMillis()/MILLIS;
      if ( cursor.getCount() > 1 )
      {
         throw new IllegalStateException("Expected only a single match to " +
         		"a PlayerStats query for Player <" + aPlayerStatsId + ">.");
      }
      else if ( cursor.getCount() == 1 )
      {
         int goalColumnIndex = cursor.getColumnIndex(aColumnName);
         updatedColumnValue = cursor.getInt(goalColumnIndex);
         ContentValues values = new ContentValues();
         values.put(PlayerStats.MODIFIED_DATE, now);
         values.put(aColumnName, ++updatedColumnValue);
         aContentResolver.update(PlayerStats.CONTENT_URI, values,
                  PlayerStats.PLAYER_ID + "=" + aPlayerStatsId
                  + " AND " + SoccerStatsDataHelper.PLAYERSTATS_TABLE_NAME 
                            + "." + PlayerStats.SEASON_ID + "=" + mSeasonId
                  + " AND " + SoccerStatsDataHelper.PLAYERSTATS_TABLE_NAME 
                            + "." + PlayerStats.GAME_ID + "=" + mGameId
                  + " AND " + SoccerStatsDataHelper.PLAYERSTATS_TABLE_NAME 
                            + "." + PlayerStats.TEAM_ID + "=" + mTeamId,
                  null);
      }
      else if ( cursor.getCount() == 0 )
      {
         ContentValues values = new ContentValues();
         values.put(PlayerStats.CREATED_DATE, now);
         values.put(PlayerStats.MODIFIED_DATE, now);
         values.put(PlayerStats.SEASON_ID, mSeasonId);
         values.put(PlayerStats.TEAM_ID, mTeamId);
         values.put(PlayerStats.GAME_ID, mGameId);
         values.put(PlayerStats.PLAYER_ID, aPlayerStatsId);
         
         if( aColumnName.equals(PlayerStats.GOALS) )
         {
            values.put(PlayerStats.GOALS, ++updatedColumnValue);
            values.put(PlayerStats.SOGS, 0);
            values.put(PlayerStats.ASSISTS, 0);
            values.put(PlayerStats.BENCH_TIME, 0);
            values.put(PlayerStats.PLAY_TIME, 0);
            values.put(PlayerStats.SAVES, 0);            
            values.put(PlayerStats.AGAINST, 0);            
         } else if ( aColumnName.equals(PlayerStats.SOGS) )
         {
            values.put(PlayerStats.GOALS, 0);
            values.put(PlayerStats.SOGS, ++updatedColumnValue);
            values.put(PlayerStats.ASSISTS, 0);
            values.put(PlayerStats.BENCH_TIME, 0);
            values.put(PlayerStats.PLAY_TIME, 0);
            values.put(PlayerStats.SAVES, 0);            
            values.put(PlayerStats.AGAINST, 0);            
         } else if ( aColumnName.equals(PlayerStats.ASSISTS) )
         {
            values.put(PlayerStats.GOALS, 0);
            values.put(PlayerStats.SOGS, 0);
            values.put(PlayerStats.ASSISTS, ++updatedColumnValue);
            values.put(PlayerStats.BENCH_TIME, 0);
            values.put(PlayerStats.PLAY_TIME, 0);
            values.put(PlayerStats.SAVES, 0);            
            values.put(PlayerStats.AGAINST, 0);            
         } else if ( aColumnName.equals(PlayerStats.BENCH_TIME) )
         {
            values.put(PlayerStats.GOALS, 0);
            values.put(PlayerStats.SOGS, 0);
            values.put(PlayerStats.ASSISTS, 0);
            values.put(PlayerStats.BENCH_TIME, 0);
            values.put(PlayerStats.PLAY_TIME, 0);
            values.put(PlayerStats.SAVES, 0);            
            values.put(PlayerStats.AGAINST, 0);            
         } else if ( aColumnName.equals(PlayerStats.PLAY_TIME) )
         {
            values.put(PlayerStats.GOALS, 0);
            values.put(PlayerStats.SOGS, 0);
            values.put(PlayerStats.ASSISTS, 0);
            values.put(PlayerStats.BENCH_TIME, 0);
            values.put(PlayerStats.PLAY_TIME, 0);
            values.put(PlayerStats.SAVES, 0);            
            values.put(PlayerStats.AGAINST, 0);            
         } else if ( aColumnName.equals(PlayerStats.SAVES) )
         {
            values.put(PlayerStats.GOALS, 0);
            values.put(PlayerStats.SOGS, 0);
            values.put(PlayerStats.ASSISTS, 0);
            values.put(PlayerStats.BENCH_TIME, 0);
            values.put(PlayerStats.PLAY_TIME, 0);
            values.put(PlayerStats.PLAY_TIME, 0);
            values.put(PlayerStats.SAVES, ++updatedColumnValue);
            values.put(PlayerStats.AGAINST, 0);            
         } else 
         {
            values.put(PlayerStats.GOALS, 0);
            values.put(PlayerStats.SOGS, 0);
            values.put(PlayerStats.ASSISTS, 0);
            values.put(PlayerStats.BENCH_TIME, 0);
            values.put(PlayerStats.PLAY_TIME, 0);
            values.put(PlayerStats.SAVES, 0);            
            values.put(PlayerStats.AGAINST, ++updatedColumnValue);            
         } 

            
         aContentResolver.insert(PlayerStats.CONTENT_URI, values);               
      }
      cursor.close();
      return updatedColumnValue;
   }
   
   // Saves outstanding player stats
   protected void saveStats()
   {
      ContentValues gameValues = new ContentValues();
      gameValues.put(Game.THEM_GOALS, mGameStats.getScoreThem());
      gameValues.put(Game.US_GOALS, mGameStats.getScoreUs());
      gameValues.put(Game.THEM_SOG, mGameStats.getSogThem());
      gameValues.put(Game.US_SOG, mGameStats.getSogUs());
      gameValues.put(Game.LOCATION, mGameStats.getGameLocation());
      gameValues.put(Game.THEM_TEAM_NAME, mGameStats.getTheirTeamName());
      getContentResolver().update(Game.CONTENT_URI, gameValues,
               Game._ID + "=" + mGameId, null);
      
      for (PlayerSubData playerData : mPlayers)
      {
         updatePlayTimeStats(playerData);
         long now = System.currentTimeMillis()/MILLIS;
         ContentValues values = new ContentValues();
         values.put(PlayerStats.MODIFIED_DATE, now);
         values.put(PlayerStats.BENCH_TIME, playerData.benchTimeInMillis);
         values.put(PlayerStats.PLAY_TIME, playerData.fieldTimeInMillis);
         getContentResolver().update(PlayerStats.CONTENT_URI, values,
                  PlayerStats.PLAYER_ID + "=" + playerData.id
                  + " AND " + PlayerStats.SEASON_ID + "=" + mSeasonId
                  + " AND " + PlayerStats.GAME_ID + "=" + mGameId
                  + " AND " + PlayerStats.TEAM_ID + "=" + mTeamId,
                  null);
      }
   }

   //-----------[ Undo support ]----------------------------------------------//
   //
   public void undoLastCommand()
   {
      if (mLastCommand != null)
      {
         switch (mLastCommand.getCommand())
         {
            case Command.COMMAND_GOAL:
            {
               undoScoreUs();
               break;
            }
            case Command.COMMAND_GOAL_AGAINST:
            {
               undoScoreThem();
               break;
            }
            case Command.COMMAND_SOG:
            {
               undoSogUs();
               break;
            }
            case Command.COMMAND_SAVE:
            {
               undoSogThem();
               break;
            }

            default:
               break;
         }
         
         mGoalEventButton.setText( Html.fromHtml(getScore()) );
         mSogEventButton.setText( Html.fromHtml(getSog()) );
         mLastCommand = null;
      }
   }
   
   private void undoScoreUs()
   {
      if ( mGameStats.getScoreUs() > 0 )
      {
         int scoringPlayerId        = mLastCommand.getScoringPlayerId();
         int scoringPlayerGoals     = mLastCommand.getScoringPlayerGoalsTotal();
         int assistingPlayerId      = mLastCommand.getAssistingPlayerId();
         int assistingPlayerAssists = mLastCommand.getAssistingPlayerAssistsTotal();
         
         long now = System.currentTimeMillis()/MILLIS;
         ContentValues values = new ContentValues();
         values.put(PlayerStats.MODIFIED_DATE, now);
         values.put(PlayerStats.GOALS, scoringPlayerGoals - 1);
         getContentResolver().update(PlayerStats.CONTENT_URI, values,
                  PlayerStats.PLAYER_ID + "=" + scoringPlayerId
                  + " AND " + PlayerStats.SEASON_ID + "=" + mSeasonId
                  + " AND " + PlayerStats.GAME_ID + "=" + mGameId
                  + " AND " + PlayerStats.TEAM_ID + "=" + mTeamId,
                  null);

         values = new ContentValues();
         values.put(PlayerStats.MODIFIED_DATE, now);
         values.put(PlayerStats.ASSISTS, assistingPlayerAssists - 1);
         getContentResolver().update(PlayerStats.CONTENT_URI, values,
                  PlayerStats.PLAYER_ID + "=" + assistingPlayerId
                  + " AND " + PlayerStats.SEASON_ID + "=" + mSeasonId
                  + " AND " + PlayerStats.GAME_ID + "=" + mGameId
                  + " AND " + PlayerStats.TEAM_ID + "=" + mTeamId,
                  null);
         
         mGameStats.undoScoreUs();
      }
   }

   private void undoScoreThem()
   {
      if ( mGameStats.getScoreThem() > 0 )
      {
         int keeperId               = mLastCommand.getGoalsAgainstPlayerId();
         int keeperGoalsAgainst     = mLastCommand.getGoalsAgainstPlayerTotal();
         
         long now = System.currentTimeMillis()/MILLIS;
         ContentValues values = new ContentValues();
         values.put(PlayerStats.MODIFIED_DATE, now);
         values.put(PlayerStats.AGAINST, keeperGoalsAgainst - 1);
         getContentResolver().update(PlayerStats.CONTENT_URI, values,
                  PlayerStats.PLAYER_ID + "=" + keeperId
                  + " AND " + PlayerStats.SEASON_ID + "=" + mSeasonId
                  + " AND " + PlayerStats.GAME_ID + "=" + mGameId
                  + " AND " + PlayerStats.TEAM_ID + "=" + mTeamId,
                  null);

         mGameStats.undoScoreThem();
      }
   }

   private void undoSogUs()
   {
      if ( mGameStats.getSogUs() > 0 )
      {
         int sogPlayerId   = mLastCommand.getSogPlayerId();
         int sogPlayerSogs = mLastCommand.getSogPlayerSogsTotal();
         
         if ( sogPlayerSogs == Command.NULL_PLAYER_ID  ||
              sogPlayerId   == Command.NULL_PLAYER_ID )
         {
            return; //can only undo the last command if it matches the request.
         }
         
         long now = System.currentTimeMillis()/MILLIS;
         ContentValues values = new ContentValues();
         values.put(PlayerStats.MODIFIED_DATE, now);
         values.put(PlayerStats.SOGS, sogPlayerSogs - 1);
         getContentResolver().update(PlayerStats.CONTENT_URI, values,
                  PlayerStats.PLAYER_ID + "=" + sogPlayerId
                  + " AND " + PlayerStats.SEASON_ID + "=" + mSeasonId
                  + " AND " + PlayerStats.GAME_ID + "=" + mGameId
                  + " AND " + PlayerStats.TEAM_ID + "=" + mTeamId,
                  null);

         mGameStats.undoShotOnGoalUs();
      }
   }

   //TODO: refactor with undoSogUs()
   private void undoSogThem()
   {
      if ( mGameStats.getSogThem() > 0 )
      {
         int savePlayerId     = mLastCommand.getSavingPlayerId();
         int sogPlayerSaves   = mLastCommand.getSavingPlayerSavesTotal();
         
         if ( sogPlayerSaves == Command.NULL_PLAYER_ID  ||
              savePlayerId    == Command.NULL_PLAYER_ID )
         {
            return; //can only undo the last command if it matches the request.
         }
         
         long now = System.currentTimeMillis()/MILLIS;
         ContentValues values = new ContentValues();
         values.put(PlayerStats.MODIFIED_DATE, now);
         values.put(PlayerStats.SAVES, sogPlayerSaves - 1);
         getContentResolver().update(PlayerStats.CONTENT_URI, values,
                  PlayerStats.PLAYER_ID + "=" + savePlayerId
                  + " AND " + PlayerStats.SEASON_ID + "=" + mSeasonId
                  + " AND " + PlayerStats.GAME_ID + "=" + mGameId
                  + " AND " + PlayerStats.TEAM_ID + "=" + mTeamId,
                  null);

         mGameStats.undoShotOnGoalThem();
      }
   }

   //-----------[ Game state machine ]----------------------------------------//
   //
   private void gameStateChange(GameStateEvent aEvent)
   {
      // Game FSM
      // START          -- press clock --> PLAY_FIRST
      // PLAY_FIRST     -- press clock --> PAUSE_FIRST   (Half button enabled)
      // PAUSE_FIRST    -- press clock --> PLAY_FIRST    (Half button disabled)
      // PAUSE_FIRST    -- press Half  --> BEGIN_SECOND  (Half button text change/enabled, Clock button text change)
      // BEGIN_SECOND   -- press Half  --> PAUSE_FIRST   (Half button text reset/enabled, Clock redisplays 1st time)
      // BEGIN_SECOND   -- press Clock --> PLAY_SECOND   (Half button disabled, clock displays 00:00)
      // PLAY_SECOND    -- press Clock --> PAUSE_SECOND  (Half button enabled)
      // PAUSE_SECOND   -- press Clock --> PLAY_SECOND   (Half button disabled)
      // PAUSE_SECOND   -- press Half  --> STOP_GAME     (Half button text change/enabled)
      // STOP_GAME      -- press Clock --> PAUSE_SECOND  (Half button text change/enabled)
      // STOP_GAME      -- press Half  --> FINISH        (Half button & Clock disabled)
      
      switch (mGameState)
      {
         case START:
         {
            handleGameStateEventInStart(aEvent);
            break;
         }
         case PLAY_FIRST:
         {
            handleGameStateEventInPlayFirstHalf(aEvent);
            break;
         }
         case PAUSE_FIRST:
         {
            handleGameStateEventInPauseFirstHalf(aEvent);
            break;
         }
         case BEGIN_SECOND:
         {
            handleGameStateEventInBeginSecondHalf(aEvent);
            break;
         }
         case PLAY_SECOND:
         {
            handleGameStateEventInPlaySecondHalf(aEvent);
            break;
         }
         case PAUSE_SECOND:
         {
            handleGameStateEventInPauseSecondHalf(aEvent);
            break;
         }
         case STOP_GAME:
         {
            handleGameStateEventInStop(aEvent);
            break;
         }
         case FINISH:
         {
            handleGameStateEventInFinish(aEvent);
            break;
         }
         default:
         {
            throw new IllegalStateException("PlayGame.gameStateChange(), " +
            		"illegal state encountered.");
         }
      }
   }
   
   private void handleGameStateEventInFinish(GameStateEvent aEvent)
   {
      //TODO: Needed?
   }

   private void handleGameStateEventInStop(GameStateEvent aEvent)
   {
      // STOP_GAME      -- press Clock --> PAUSE_SECOND  (Half button text change/enabled)
      // STOP_GAME      -- press Half  --> FINISH        (Half button & Clock disabled)
      if (aEvent == GameStateEvent.CLOCK_PRESSED)
      {
         mGameClockButton.setText( mGameClockPausedTime );
         restoreButtonTextSize(mGameClockButton);
         restoreButtonTextSize(mWhichHalfButton);
         mGameClockPausedTime = OBVIOUSLY_INVALID_GAME_CLOCK_TIME;
         mWhichHalfButton.setText(R.string.secondHalf);
         mWhichHalfButton.setEnabled(true);
         mWhichHalfButton.setBackgroundDrawable(mNormalButtonBackground);
         mGameState = GameState.PAUSE_SECOND;
      }
      else if (aEvent == GameStateEvent.HALF_PRESSED)
      {
         mGameClockButton.setEnabled(false);
         mWhichHalfButton.setEnabled(false);
         restoreButtonTextSize(mGameClockButton);
         restoreButtonTextSize(mWhichHalfButton);
         mGameClockButton.setBackgroundDrawable(mStoppedButtonBackground);
         mWhichHalfButton.setText(R.string.gameOverGame);
         mGameClockButton.setText(R.string.gameOverOver);
         disableSubstitutionControls();
         saveStats();
         Intent intent = new Intent(SoccerStatIntent.SHOW_PLAYER_STATS);
         intent.addCategory(SoccerStatIntent.DEFAULT);
         intent.putExtra(SoccerStatIntent.GAME_ID, mGameId);
         startActivity(intent);
         mGameState = GameState.FINISH;
      }
      else if (aEvent == GameStateEvent.BACK_SOFT_BUTTON_PRESSED)
      {
         saveStats();
         mGameState = GameState.FINISH;
      }
   }

   private void handleGameStateEventInPauseSecondHalf(GameStateEvent aEvent)
   {
      // PAUSE_SECOND   -- press Clock --> PLAY_SECOND   (Half button disabled)
      // PAUSE_SECOND   -- press Half  --> STOP_GAME     (Half button text change/enabled)
      if (aEvent == GameStateEvent.CLOCK_PRESSED)
      {
         resumeHalf();
         mGameState = GameState.PLAY_SECOND;
      }
      else if(aEvent == GameStateEvent.HALF_PRESSED)
      {
         mGameClockPausedTime = (String) mGameClockButton.getText();
         mGameClockButton.setText( getString(R.string.resumeHalf) );
         reduceButtonTextSize(mGameClockButton);
         reduceButtonTextSize(mWhichHalfButton);
         mWhichHalfButton.setText(R.string.endGame);
         mWhichHalfButton.setBackgroundDrawable(mStoppedButtonBackground);
         mGameState = GameState.STOP_GAME;
      }
      else if (aEvent == GameStateEvent.BACK_SOFT_BUTTON_PRESSED)
      {
         saveStats();
         mGameState = GameState.FINISH;
      }
   }

   private void handleGameStateEventInPlaySecondHalf(GameStateEvent aEvent)
   {
      if (aEvent == GameStateEvent.BACK_SOFT_BUTTON_PRESSED)
      {
         saveStats();
         mGameState = GameState.FINISH;
      }
      else
      {
         // PLAY_SECOND    -- press Clock --> PAUSE_SECOND  (Half button enabled)
         pauseHalf();
         mGameState = GameState.PAUSE_SECOND;
      }
   }

   private void handleGameStateEventInBeginSecondHalf(GameStateEvent aEvent)
   {
      // BEGIN_SECOND   -- press Clock --> PLAY_SECOND   (Half button disabled, clock displays 00:00)
      // BEGIN_SECOND   -- press Half  --> PAUSE_FIRST   (Half button text reset/enabled, Clock redisplays 1st time)
      if (aEvent == GameStateEvent.CLOCK_PRESSED)
      {
         saveFirstHalfPlayerStats();
         startHalf();     
         mGameState = GameState.PLAY_SECOND;
      }
      else if  (aEvent == GameStateEvent.HALF_PRESSED)
      {
         restoreFirstHalfPlayerStats();
         mGameClockButton.setText( mGameClockPausedTime );
         mGameClockPausedTime = OBVIOUSLY_INVALID_GAME_CLOCK_TIME;
         mGameClockButton.setBackgroundDrawable(mStoppedButtonBackground);
         mWhichHalfButton.setText(R.string.firstHalf);
         mGameState = GameState.PAUSE_FIRST;
      }
      else if (aEvent == GameStateEvent.BACK_SOFT_BUTTON_PRESSED)
      {
         saveStats();
         mGameState = GameState.FINISH;
      }
   }

   private void handleGameStateEventInPauseFirstHalf(GameStateEvent aEvent)
   {
      // PAUSE_FIRST    -- press clock --> PLAY_FIRST    (Half button disabled)
      // PAUSE_FIRST    -- press Half  --> BEGIN_SECOND  (Half button text change/enabled, Clock button text change)
      if (aEvent == GameStateEvent.CLOCK_PRESSED)
      {
         resumeHalf();
         mGameState = GameState.PLAY_FIRST;
      }
      else if(aEvent == GameStateEvent.HALF_PRESSED)
      {
         mGameClockPausedTime = (String) mGameClockButton.getText();
         mGameClockButton.setText( getString(R.string.startTime) );
         mGameClockButton.setBackgroundDrawable(mNormalButtonBackground);
         mWhichHalfButton.setText(R.string.secondHalf);
         mWhichHalfButton.setEnabled(true);
         backupFirstHalfPlayerStats();
         mGameState = GameState.BEGIN_SECOND;
      }
      else if (aEvent == GameStateEvent.BACK_SOFT_BUTTON_PRESSED)
      {
         saveStats();
         mGameState = GameState.FINISH;
      }
   }

   private void handleGameStateEventInPlayFirstHalf(GameStateEvent aEvent)
   {
      if (aEvent == GameStateEvent.BACK_SOFT_BUTTON_PRESSED)
      {
         saveStats();
         mGameState = GameState.FINISH;
      }
      else
      {
         // PLAY_FIRST     -- press clock --> PAUSE_FIRST   (Half button enabled)
         pauseHalf();
         mGameState = GameState.PAUSE_FIRST;
      }
   }

   private void handleGameStateEventInStart(GameStateEvent aEvent)
   {
      if (aEvent == GameStateEvent.BACK_SOFT_BUTTON_PRESSED)
      {
         if (! mCancelGame )  saveStats();
         mGameState = GameState.FINISH;
      }
      else
      {
         // START          -- press clock --> PLAY_FIRST
         startHalf();     
         mGameState = GameState.PLAY_FIRST;
      }
   }

   private void startHalf()
   {
      mStartTime = 0L;
      mAccumulatedTime = 0L;
      mGameClockButton.setText(R.string.timePlayed);
      mStartTime = SystemClock.elapsedRealtime();
      mLastPlayerUpdateTime = 0;
      mGameClockHandler.removeCallbacks(mUpdateClockTimeTask);
      mGameClockHandler.postDelayed(mUpdateClockTimeTask, CLOCK_TICK);

      enableStatButtons();
   }

   private void pauseHalf()
   {
      mGameClockHandler.removeCallbacks(mUpdateClockTimeTask);
      mGameClockButton.setBackgroundDrawable(mStoppedButtonBackground);
      String currentClockValue = (String) mGameClockButton.getText();
      String[] minutesSeconds  = currentClockValue.split(":");
      int minutes = Integer.parseInt(minutesSeconds[0]);
      int seconds = Integer.parseInt(minutesSeconds[1]);
      mAccumulatedTime = (seconds*1000) + (minutes*60*1000);
      
      disableStatButtons();
   }

   private void resumeHalf()
   {
      mGameClockHandler.removeCallbacks(mUpdateClockTimeTask);
      mStartTime = SystemClock.elapsedRealtime();
      mGameClockHandler.postDelayed(mUpdateClockTimeTask, CLOCK_TICK);
      
      enableStatButtons();
      mGameClockButton.setBackgroundDrawable(mNormalButtonBackground);
   }

   private void reduceButtonTextSize(Button aButton)
   {
      changeButtonTextSize(aButton, REDUCED_BUTTON_TEXT_SIZE);
   }
   
   private void restoreButtonTextSize(Button aButton)
   {
      changeButtonTextSize(aButton, NORMAL_BUTTON_TEXT_SIZE);
   }
   
   private void changeButtonTextSize(Button aButton, float aNewTextSize)
   {
      aButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, aNewTextSize);
   }
   

   //
   //-----------[ Helper classes ]--------------------------------------------//
   //
   
   
   //-----------[ Player subs list adapter & helper]--------------------------//
   //


   private class PlayerSubDataAdapter extends ArrayAdapter<PlayerSubData> 
                                      implements Comparator<PlayerSubData>
   {

      int[]   colors     = new int[] {Constants.DARK_BACKGROUND, 
                                      Constants.LIGHT_BACKGROUND};
      private ArrayList<PlayerSubData> mPlayers;

//      private int[] colors = new int[] {0x10808080, 0x30ffffff};

      public PlayerSubDataAdapter(Context context, int textViewResourceId, 
                                  ArrayList<PlayerSubData> aPlayers) 
      {
         super(context, textViewResourceId, aPlayers);
         this.mPlayers = aPlayers;
      }

      @Override
      public View getView(int aPosition, View aConvertView, ViewGroup aParent) {
         View v = aConvertView;
         if (v == null) 
         {
            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.substitutions_list_item, null);
         }
         PlayerSubData playerData = mPlayers.get(aPosition);
         if (playerData == null) 
         {
            throw new Error("Unexpected null player entry encountered");
         }
         else
         {
            TextView       jersey      = (TextView) v.findViewById(R.id.playerJerseyNumber);
            TextView       name        = (TextView) v.findViewById(R.id.playerName);
            TextView       timeInState = (TextView) v.findViewById(R.id.timeInState);
            ToggleButton   onFieldBtn  = (ToggleButton) v.findViewById(R.id.onFieldToggle);
            if (null == jersey || null == name || null == timeInState || null == onFieldBtn) 
            {
               throw new Error("Unexpected null view encountered");
            }
            
            jersey.setText(playerData.jerseyNo);
            name.setText(playerData.name);
            timeInState.setText( convertToMmSs(playerData.timeInStateInMillis) );
            onFieldBtn.setChecked(playerData.onFieldBool);            
         }
         
         // Alternating row colors
         int colorPos = aPosition % colors.length;
         v.setBackgroundColor(colors[colorPos]);
         
         // Store relative list position in item for use by onClick() method
         v.setTag(aPosition);

         return v;
      }

      private String convertToMmSs(long timeInState)
      {
         //convert to seconds
         timeInState = timeInState / 1000; // milliseconds to seconds
         long minutes = timeInState / 60;
         long remainder = timeInState % 60;
         long seconds   = remainder;
         return ( (minutes < 10 ? "0":"") + minutes
                + ":"
                + (seconds < 10 ? "0":"") + seconds );
      }

      @Override
      public int compare(PlayerSubData player1, PlayerSubData player2)
      {
         // Return values; flipped from normal because we want descending order
         int LESS_THAN     = 1;
         int EQUAL         = 0;
         int GREATER_THAN  = -1;
         
         // Sort onField == true ahead of false,
         // then sort in descending order on timeInState
         if ( player1.onFieldBool == true && player2.onFieldBool == true)
         {
            if (player1.timeInStateInMillis > player2.timeInStateInMillis)  return GREATER_THAN;
            if (player1.timeInStateInMillis < player2.timeInStateInMillis)  return LESS_THAN;
            protectAgainstNPE(player1, player2);
            return player1.jerseyNo.compareTo(player2.jerseyNo);
         }
         else if (player1.onFieldBool == true && player2.onFieldBool == false)
         {
            return GREATER_THAN;
         }
         else if (player1.onFieldBool == false && player2.onFieldBool == true)
         {
            return LESS_THAN;
         }
         else if (player1.onFieldBool == false && player2.onFieldBool == false)
         {
            if (player1.timeInStateInMillis > player2.timeInStateInMillis)  return GREATER_THAN;
            if (player1.timeInStateInMillis < player2.timeInStateInMillis)  return LESS_THAN;
            protectAgainstNPE(player1, player2);
            return player1.jerseyNo.compareTo(player2.jerseyNo);
         }
         return EQUAL;
      }

      // protects against edge conditions where, in some cases, player jersey
      // numbers could be null.
      private void protectAgainstNPE(PlayerSubData aPlayer1,
                                     PlayerSubData aPlayer2)
      {
         if (aPlayer1.jerseyNo == null)    aPlayer1.jerseyNo = "";
         if (aPlayer2.jerseyNo == null)    aPlayer2.jerseyNo = "";
      }
   }

   //-----------[ UNDO helper classes ]----------------------------------//
   //
   private class Command
   {
      public static final int NULL_PLAYER_ID       = -1;
      public static final int COMMAND_SOG          = 0;
      public static final int COMMAND_SAVE         = 1;
      public static final int COMMAND_GOAL         = 2;
      public static final int COMMAND_GOAL_AGAINST = 3;
      
      private int mCommand;
      private int mScoringPlayerId;
      private int mScoringPlayerGoalsTotal;
      private int mAssistingPlayerId;
      private int mAssistingPlayerAssistsTotal;
      private int mSavingPlayerId;
      private int mSavingPlayerSavesTotal;
      private int mSogPlayerId;
      private int mSogPlayerSogsTotal;
      private int mGoalsAgainstPlayerId;
      private int mGoalsAgainstPlayerTotal;
      
      @SuppressWarnings("unused")
      private Command() {};
      
      public Command(int aScoringPlayerId, int aScoringPlayerGoalsTotal,
                         int aAssistingPlayerId, int aAssistingPlayerAssistsTotal,
                         int aSavingPlayerId, int aSavingPlayerSavesTotal,
                         int aSogPlayerId, int aSogPlayerSogsTotal,
                         int aGoalsAgainstPlayerId, int aGoalsAgainstPlayerTotal,
                         int aCommand)
      {
         setCommand(aCommand);
         setScoringPlayerId(aScoringPlayerId);
         setScoringPlayerGoalsTotal(aScoringPlayerGoalsTotal);
         setAssistingPlayerId(aAssistingPlayerId);
         setAssistingPlayerAssistsTotal(aAssistingPlayerAssistsTotal);
         setSavingPlayerId(aSavingPlayerId);
         setSavingPlayerSavesTotal(aSavingPlayerSavesTotal);
         setSogPlayerId(aSogPlayerId);
         setSogPlayerSogsTotal(aSogPlayerSogsTotal);
         setGoalsAgainstPlayerId(aGoalsAgainstPlayerId);
         setGoalsAgainstPlayerTotal(aGoalsAgainstPlayerTotal);
      }
      
      public int getScoringPlayerId()
      {
         return mScoringPlayerId;
      }
      
      public void setScoringPlayerId(int aScoringPlayerId)
      {
         this.mScoringPlayerId = aScoringPlayerId;
      }
      
      public void setScoringPlayerGoalsTotal(int aScoringPlayerGoalsTotal)
      {
         this.mScoringPlayerGoalsTotal = aScoringPlayerGoalsTotal;
      }
      
      public int getScoringPlayerGoalsTotal()
      {
         return mScoringPlayerGoalsTotal;
      }
      
      public void setAssistingPlayerId(int aAssistingPlayerId)
      {
         this.mAssistingPlayerId = aAssistingPlayerId;
      }
      
      public int getAssistingPlayerId()
      {
         return mAssistingPlayerId;
      }
      
      public void setAssistingPlayerAssistsTotal(
               int aAssistingPlayerAssistsTotal)
      {
         this.mAssistingPlayerAssistsTotal = aAssistingPlayerAssistsTotal;
      }
      
      public int getAssistingPlayerAssistsTotal()
      {
         return mAssistingPlayerAssistsTotal;
      }
      public void setSavingPlayerId(int aSavingPlayerId)
      {
         this.mSavingPlayerId = aSavingPlayerId;
      }
      
      public int getSavingPlayerId()
      {
         return mSavingPlayerId;
      }
      
      public void setSavingPlayerSavesTotal(int aSavingPlayerSavesTotal)
      {
         this.mSavingPlayerSavesTotal = aSavingPlayerSavesTotal;
      }
      
      public int getSavingPlayerSavesTotal()
      {
         return mSavingPlayerSavesTotal;
      }

      public void setSogPlayerId(int aSogPlayerId)
      {
         this.mSogPlayerId = aSogPlayerId;
      }

      public int getSogPlayerId()
      {
         return mSogPlayerId;
      }

      public void setSogPlayerSogsTotal(int aSogPlayerSogsTotal)
      {
         this.mSogPlayerSogsTotal = aSogPlayerSogsTotal;
      }

      public int getSogPlayerSogsTotal()
      {
         return mSogPlayerSogsTotal;
      }

      public void setGoalsAgainstPlayerId(int aGoalsAgainstPlayerId)
      {
         this.mGoalsAgainstPlayerId = aGoalsAgainstPlayerId;
      }

      public int getGoalsAgainstPlayerId()
      {
         return mGoalsAgainstPlayerId;
      }

      public void setGoalsAgainstPlayerTotal(int aGoalsAgainstPlayerTotal)
      {
         this.mGoalsAgainstPlayerTotal = aGoalsAgainstPlayerTotal;
      }

      public int getGoalsAgainstPlayerTotal()
      {
         return mGoalsAgainstPlayerTotal;
      }

      public void setCommand(int mCommand)
      {
         this.mCommand = mCommand;
      }

      public int getCommand()
      {
         return mCommand;
      }
   }
   
   
}