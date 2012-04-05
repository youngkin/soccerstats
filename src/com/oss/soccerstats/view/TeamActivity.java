package com.oss.soccerstats.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.oss.soccerstats.R;
import com.oss.soccerstats.data.SoccerStatsDataHelper;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Game;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Player;
import com.oss.soccerstats.data.SoccerStatsDataHelper.PlayerStats;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Season;
import com.oss.soccerstats.data.SoccerStatsDataHelper.Team;
import com.oss.soccerstats.intent.SoccerStatIntent;
import com.oss.soccerstats.view.EditPlayerDialog.OnEditTeamEventListener;

public class TeamActivity extends ListActivity
                          implements EditTeamDialog.OnEditTeamEventListener
{
   /**
    * The columns we are interested in from the database
    */
   private static final String[] TEAM_PROJECTION = new String[] {
           SoccerStatsDataHelper.Team._ID,            // 0
           SoccerStatsDataHelper.Team.TEAM_NAME,      // 1
           SoccerStatsDataHelper.Season.SEASON_NAME   // 2
   };

   private   static final int DIALOG_CONFIRM_DELETE_TEAM  = 0;
   protected static final int HEADER_ROW = 0;

//   private static final int DIALOG_HELP_STATS = 0;
   private Cursor mCursor;
   private Dialog mDialog;

private String mDeleteTeamId;

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setTitle(R.string.teamTitle);
      
      mDialog = new EditTeamDialog(this, this);

      mCursor = managedQuery(Team.CONTENT_URI, TEAM_PROJECTION, 
               null, null,
               Team.DEFAULT_SORT_ORDER);

      // Used to map rows from the database to views
      SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.teams_list_item, mCursor,
              new String[] { Team.TEAM_NAME, Season.SEASON_NAME, Team._ID }, 
              new int[] { R.id.teamName, R.id.seasonName, R.id.teamId});
      
      ListView lv = getListView();
      lv.setOnItemClickListener(new OnItemClickListener() 
      {
         public void onItemClick(AdapterView<?> parent, View view,
                  int position, long aId)
         {
            if (position != HEADER_ROW)
            {
               TableLayout container  = (TableLayout)view;
               String teamName      = (String) ((TextView)((TableRow) container.getChildAt(0)).getChildAt(0)).getText();
               String seasonName  = (String) ((TextView)((TableRow) container.getChildAt(0)).getChildAt(1)).getText();
               String teamId      = (String) ((TextView)((TableRow) container.getChildAt(0)).getChildAt(2)).getText();
               
               Intent intent = new Intent(SoccerStatIntent.TEAM_DETAILS);
               intent.addCategory(SoccerStatIntent.DEFAULT);
               intent.putExtra(SoccerStatIntent.TEAM_NAME, teamName);
               intent.putExtra(SoccerStatIntent.SEASON_NAME, seasonName);
               intent.putExtra(SoccerStatIntent.TEAM_ID, Integer.parseInt(teamId));
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
               TableRow    row         = (TableRow)container.getChildAt(0);
               TextView teamName   = (TextView) row.getChildAt(0);
               TextView teamId     = (TextView) row.getChildAt(2);
               Button   addOrDelete = (Button)mDialog.findViewById(R.id.addNewOrDelete);
               EditText team       = (EditText)mDialog.findViewById(R.id.editTeamName);
               EditText id         = (EditText)mDialog.findViewById(R.id.editTeamId);
               
               team.setText(teamName.getText());
               id.setText(teamId.getText());
               addOrDelete.setText(R.string.deleteTeam);
               mDialog.setTitle(R.string.editTeam);      
               mDialog.show();
            }
            
            return true;
         }
      });
      
      LayoutInflater inflater = getLayoutInflater();
      ViewGroup headerRow = (ViewGroup)inflater.inflate(R.layout.teams_header, 
                                                        lv, false);
      lv.addHeaderView(headerRow);
      setListAdapter(adapter);
   }
      
   @Override
   public void onEditTeamHandler(String aTeamName, String aTeamId, int aOperation,
		   						 boolean aAddNewTeam)
   {
      ContentValues values = new ContentValues();

      values.put(Team.MODIFIED_DATE, System.currentTimeMillis());
      values.put(Team.TEAM_NAME, aTeamName);

      // Commit all of our changes to persistent storage. When the update
      // completes the content provider will notify the cursor of the change, 
      // which will cause the UI to be updated.
      if ( aOperation == OnEditTeamEventListener.OP_UPDATE )
      {
         getContentResolver().update(Team.CONTENT_URI, values, 
                  Team._ID + "=" + Integer.parseInt(aTeamId), 
                  null);
      }
      else if ( aOperation == OnEditTeamEventListener.OP_ADD)
      {
         values.put(Player.CREATED_DATE, System.currentTimeMillis());
         Uri newTeamIdUri  = getContentResolver().insert(Team.CONTENT_URI, values);
         String newTeamIdStr  = newTeamIdUri.getLastPathSegment();
         int newTeamId        = Integer.parseInt(newTeamIdStr);
         
         ContentValues playerValues = new ContentValues();
         playerValues.put(Player.CREATED_DATE, System.currentTimeMillis());
         playerValues.put(Player.MODIFIED_DATE, System.currentTimeMillis());
         playerValues.put(Player.SEASON_ID, Player.SEASON_ID_DEFAULT);
         playerValues.put(Player.TEAM_ID, newTeamId);
         playerValues.put(Player.NAME, Player.OTHER_PLAYER_NAME);
         playerValues.put(Player.JERSEY_NUMBER, Player.OTHER_PLAYER_JERSEY_NUMBER);
         getContentResolver().insert(Player.CONTENT_URI, playerValues);
      }
      else if ( aOperation == OnEditTeamEventListener.OP_DELETE)
      {
         mDeleteTeamId = aTeamId;
         showDialog(DIALOG_CONFIRM_DELETE_TEAM);
      }
      else
      {
         throw new Error("Invalid operation code: " + aOperation);
      }
      
      if (aAddNewTeam)   showAddTeamDialog();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu aMenu)
   {
      MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.team_menu, aMenu);
     
      //TODO: impl team stats menu item
//      inflater.inflate(R.menu.stats_menu, aMenu);
     
     return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem aMenuItem) 
   {
      // Handle item selection
      switch (aMenuItem.getItemId())
      {
	      case R.id.addTeam:
	      {
	        showAddTeamDialog();
	        return true;
	      }
//	      case R.id.help:
//	      {
//	        showDialog(DIALOG_HELP_STATS);
//	        return true;
//	      }
         default:
            return false;
      }
   }

   @Override
   protected Dialog onCreateDialog(int aDialogId)
   {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      
      builder.setMessage(getString(R.string.confirmDeleteTeam)).setCancelable(false)
               .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id)
                  {
                     TeamActivity.this.completeTeamDelete();
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

   private void completeTeamDelete()
   {
      Uri teamUri = ContentUris.withAppendedId(Team.CONTENT_URI, 
                                                 Integer.parseInt(mDeleteTeamId) );
      getContentResolver().delete(teamUri, null, null);

      getContentResolver().delete(PlayerStats.CONTENT_URI, 
              PlayerStats.TEAM_ID + "=" + Integer.parseInt(mDeleteTeamId), 
              null);
     
      getContentResolver().delete(Player.CONTENT_URI, 
              Player.TEAM_ID + "=" + Integer.parseInt(mDeleteTeamId), 
              null);
     
      getContentResolver().delete(Game.CONTENT_URI, 
              Game.TEAM_ID + "=" + Integer.parseInt(mDeleteTeamId), 
              null);
     
      mDeleteTeamId = null;
   }
   
   private void showAddTeamDialog()
   {
       Button   addOrDelete = (Button)mDialog.findViewById(R.id.addNewOrDelete);
       EditText team       = (EditText)mDialog.findViewById(R.id.editTeamName);
       EditText id         = (EditText)mDialog.findViewById(R.id.editTeamId);
       
       team.setText("");
       id.setText("");
       addOrDelete.setText(R.string.addTeam);
       mDialog.setTitle(R.string.addTeam);      
       mDialog.show();
   }
 
}
