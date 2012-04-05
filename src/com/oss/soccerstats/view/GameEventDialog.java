package com.oss.soccerstats.view;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.oss.soccerstats.R;
import com.oss.soccerstats.model.PlayerSubData;

public class GameEventDialog extends Dialog
{
   /** 
    * The actions reported by the onGameEventHandler() callback.
    */
   public static final int GOAL     = 0;
   public static final int SOG      = 1;
   public static final int SAVE     = 2;
   public static final int GOAL_AGAINST  = 3;

   private boolean mIsGoalDialog;
   
   private RadioGroup   mRadioGroup;
   private RadioButton  mUsSelected;
   private RadioButton  mThemSelected;
   private TextView     mTextForSpinner1;
   private TextView     mTextForSpinner2;
   private Spinner      mSpinner1;
   private Spinner      mSpinner2;
   private Button       mOkButton;
   private Button       mCancelButton;

   /**
    * A callback interfaced used by this class to pass the results of the
    * dialog to interested listeners.
    * 
    */
   public interface OnGameEventListener
   {
      public void onGameEventHandler(String aPlayerId1, String aPlayerId2, int aAction);
   }

   private OnGameEventListener mOnGameEventListener;

   private ArrayList<PlayerSubData> mOnFieldPlayers;

   private GameEventAdapter mGameEventAdapter;

   public GameEventDialog(Context context, 
                          OnGameEventListener aOnGameEventListener, 
                          ArrayList<PlayerSubData> aPlayersList)
   {
      super(context);
      mOnGameEventListener = aOnGameEventListener;
      mOnFieldPlayers = aPlayersList;
      setContentView(R.layout.event_details);
   }

   @Override
   public void onCreate(Bundle savedInstanceState)
   {  
      mGameEventAdapter = new GameEventAdapter(this.getContext(), 
               R.layout.stat_details_list_item, R.id.playerIDStatDetails, mOnFieldPlayers);
      mGameEventAdapter.setDropDownViewResource(R.layout.stat_details_list_item);

      mSpinner1 = (Spinner) findViewById(R.id.spinner1);
      mSpinner1.setAdapter(mGameEventAdapter);
      mSpinner2 = (Spinner) findViewById(R.id.spinner2);
      mSpinner2.setAdapter(mGameEventAdapter);
      
      mTextForSpinner1 = (TextView) findViewById(R.id.spinner1Text);
      mTextForSpinner1 = (TextView) findViewById(R.id.spinner1Text);
      mTextForSpinner2 = (TextView) findViewById(R.id.spinner2Text);
      mTextForSpinner2 = (TextView) findViewById(R.id.spinner2Text);
      
      mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
      mUsSelected = (RadioButton) findViewById(R.id.chooseUs);
      mUsSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
         
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
         {
            resetView();
            
            if (! mUsSelected.isChecked())   return;
            
            if (mIsGoalDialog)
            {
               mOkButton.setEnabled(true);
               
               mTextForSpinner1.setText(R.string.scoredBy);
               mTextForSpinner1.setVisibility(View.VISIBLE);
               mTextForSpinner2.setText(R.string.assistedBy);
               mTextForSpinner2.setVisibility(View.VISIBLE);
               
               mSpinner1.setVisibility(View.VISIBLE);
               mSpinner2.setVisibility(View.VISIBLE);
            }
            else
            {
               mOkButton.setEnabled(true);
               
               mTextForSpinner1.setText(R.string.sogBy);
               mTextForSpinner1.setVisibility(View.VISIBLE);
               
               mSpinner1.setVisibility(View.VISIBLE);
            }
         }
      });
      
      mThemSelected = (RadioButton) findViewById(R.id.chooseThem);
      mThemSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
         
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
         {
            resetView();
            
            if (! mThemSelected.isChecked())   return;
            
            if (mIsGoalDialog)
            {
               mOkButton.setEnabled(true);
               
               mTextForSpinner1.setText(R.string.goalAllowedBy);
               mTextForSpinner1.setVisibility(View.VISIBLE);
               
               mSpinner1.setVisibility(View.VISIBLE);
            }
            else
            {
               mOkButton.setEnabled(true);
               
               mTextForSpinner1.setText(R.string.sogSavedBy);
               mTextForSpinner1.setVisibility(View.VISIBLE);
               
               mSpinner1.setVisibility(View.VISIBLE);
            }
         }
      });
      
      mOkButton   = (Button)findViewById(R.id.ok);
      mOkButton.setOnClickListener( new Button.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            String spinner1PlayerId  = null;           
            String spinner2PlayerId  = null;           
            int action = SOG;
            
            if ( isUsDialog() )
            {
               TextView spinner1PlayerIdField = (TextView)mSpinner1.findViewById(R.id.playerIDStatDetails);
               spinner1PlayerId = (String) spinner1PlayerIdField.getText();
               TextView spinner2PlayerIdField = (TextView)mSpinner2.findViewById(R.id.playerIDStatDetails);
               // might be "View.visibility=gone"
               if (null != spinner2PlayerIdField)
               {
                  spinner2PlayerId = (String) spinner2PlayerIdField.getText();
               }
               if (mUsSelected.isChecked())
               {
                  action = GOAL;
               }
               else
               {
                  action = GOAL_AGAINST;
               }
            }
            else 
            {
               TextView spinner1PlayerIdField = (TextView)mSpinner1.findViewById(R.id.playerIDStatDetails);
               spinner1PlayerId = (String) spinner1PlayerIdField.getText();
               if (mUsSelected.isChecked())
               {
                  action = SOG;
               }
               else
               {
                  action = SAVE;
               }
            }
            
            mOnGameEventListener.onGameEventHandler(spinner1PlayerId, spinner2PlayerId, action);
            
            dismiss();
            resetView();
         }
      });

      mCancelButton   = (Button)findViewById(R.id.cancel);
      mCancelButton.setOnClickListener( new Button.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            dismiss();
            resetView();
         }
      });
   }

   public boolean isUsDialog()
   {
      return mIsGoalDialog;
   }

   public void setIsGoalDialog(boolean aIsUsDialog)
   {
      this.mIsGoalDialog = aIsUsDialog;
   }
   
   private void resetView()
   {
      mRadioGroup.clearCheck();
      mTextForSpinner1.setVisibility(View.GONE);
      mTextForSpinner2.setVisibility(View.GONE);
      mSpinner1.setVisibility(View.GONE);
      mSpinner2.setVisibility(View.GONE);
      mOkButton.setEnabled(false);
   }

   private class GameEventAdapter extends ArrayAdapter<PlayerSubData>
   {
      private ArrayList<PlayerSubData> mPlayers;

      public GameEventAdapter(Context context, int layoutResourceId, 
                              int textViewResourceId, 
                              ArrayList<PlayerSubData> aPlayers) 
      {
         super(context, layoutResourceId, textViewResourceId, aPlayers);
         this.mPlayers = aPlayers;
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
            row=inflater.inflate(R.layout.stat_details_list_item, aParent, false);
         }
         
         PlayerSubData playerData = mPlayers.get(aPosition);
         if (playerData == null) 
         {
            throw new Error("Unexpected null player entry encountered");
         }
         else
         {
            TextView jersey   = (TextView) row.findViewById(R.id.playerJerseyNumberStatDetails);
            TextView name     = (TextView) row.findViewById(R.id.playerNameStatDetails);
            TextView id       = (TextView) row.findViewById(R.id.playerIDStatDetails);
            if (null == jersey || null == name || null == id) 
            {
               throw new Error("Unexpected null view encountered");
            }
            
            jersey.setText(playerData.jerseyNo);
            name.setText(playerData.name);
            id.setText(Integer.toString(playerData.id));
         } 
         
         return row;
      }
   }

   public void notifyDataSetChanged()
   {
      if (null != mGameEventAdapter)
      {
         mGameEventAdapter.notifyDataSetChanged();
      }
   }

}
