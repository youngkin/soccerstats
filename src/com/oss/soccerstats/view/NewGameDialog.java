/////////////////////////////////////////////////////////////////
// NewGameDialog.java
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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.oss.soccerstats.R;
import com.oss.soccerstats.model.TeamData;

public class NewGameDialog extends Dialog
{
   public interface OnInitGameEventListener
   {
      public void onNewGameHandler(int    aOurTeamId, 
    		  						         String aOurTeamName,
                                    String aTheirTeamName,
                                    String aGameLocation);

      public void onCancelGameHandler();
   }

   private OnInitGameEventListener mEventListener;
   private Spinner mOurTeamSpinner;
   private TeamRowAdapter mTeamSpinnerAdapter;
   private ArrayList<TeamData> mTeamsList;

   public NewGameDialog(Context context, 
                        OnInitGameEventListener aOnOkListener, 
                        ArrayList<TeamData> aTeamsList)
   {
      super(context);
      setContentView(R.layout.init_game_details);
      mTeamsList = aTeamsList;
      mEventListener = aOnOkListener;
   }
   
   @Override
   public void onCreate(Bundle savedInstanceState)
   {      
      mTeamSpinnerAdapter = new TeamRowAdapter(this.getContext(), 
            R.layout.new_game_teams_list_item, R.id.teamId, mTeamsList);
      mTeamSpinnerAdapter.setDropDownViewResource(R.layout.new_game_teams_list_item);

      mOurTeamSpinner = (Spinner)findViewById(R.id.chooseOurTteam);
      mOurTeamSpinner.setAdapter(mTeamSpinnerAdapter);
      
      mOurTeamSpinner.setFocusable(true);
      mOurTeamSpinner.setFocusableInTouchMode(true);
      mOurTeamSpinner.requestFocus();
      mOurTeamSpinner.requestFocusFromTouch();

      Button ok   = (Button)findViewById(R.id.ok);
      ok.setOnClickListener( new Button.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            v = (View) v.getParent().getParent();
            TextView teamIdField = (TextView)mOurTeamSpinner.findViewById(R.id.teamId);
            String teamIdValue = (String) teamIdField.getText();
            TextView teamNameField = (TextView)mOurTeamSpinner.findViewById(R.id.teamName);
            String teamNameValue = (String) teamNameField.getText();
            EditText themNameField = (EditText)v.findViewById(R.id.editThem);
            String themNameValue  = themNameField.getText().toString();
            EditText locationField = (EditText)v.findViewById(R.id.gameLocation);
            String locationValue  = locationField.getText().toString();
            mEventListener.onNewGameHandler(Integer.parseInt(teamIdValue), teamNameValue, themNameValue,
            locationValue);
            dismiss();
         }
      });     
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
      mEventListener.onCancelGameHandler();
   }   
   
   private class TeamRowAdapter extends ArrayAdapter<TeamData>
   {
      private ArrayList<TeamData> mTeams;

      public TeamRowAdapter(Context context, int layoutResourceId, 
                              int textViewResourceId, 
                              ArrayList<TeamData> aTeams) 
      {
         super(context, layoutResourceId, textViewResourceId, aTeams);
         this.mTeams = aTeams;
      }

      @Override
      public View getDropDownView(int position, View convertView,
               ViewGroup parent)
      {
         return getView(position, convertView, parent);
      }

      @Override
      public View getView(int aPosition, View aConvertView, ViewGroup aParent) 
      {
         View row = aConvertView;
         if (row == null) 
         {
            LayoutInflater inflater=getLayoutInflater();
            row=inflater.inflate(R.layout.new_game_teams_list_item, aParent, false);
         }
         
         TeamData teamData = mTeams.get(aPosition);
         if (teamData == null) 
         {
            throw new Error("Unexpected null team entry encountered");
         }
         else
         {
            TextView teamId   = (TextView) row.findViewById(R.id.teamId);
            TextView name     = (TextView) row.findViewById(R.id.teamName);
            TextView season   = (TextView) row.findViewById(R.id.seasonName);
            if (null == name || null == teamId || null == season) 
            {
               throw new Error("Unexpected null view encountered");
            }
            
            name.setText(teamData.mTeamName);
            teamId.setText(Integer.toString(teamData.mId));
            season.setText(""); //Season is unused, set to empty string
         } 
         
         return row;
      }
   }

}
