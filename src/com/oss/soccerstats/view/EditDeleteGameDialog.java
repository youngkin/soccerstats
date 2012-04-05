package com.oss.soccerstats.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.oss.soccerstats.R;

public class EditDeleteGameDialog extends Dialog
{
   public interface OnEditGameEventListener
   {
      public static final int OP_CHANGE = 0;
      public static final int OP_DELETE = 1;

      public void onEditGameHandler(String aOurTeamName, String aTheirTeamName,
               String aGameLocation, String aGameId,
               int aOperation);
   }

   private OnEditGameEventListener mOnOkListener;
   private Context mContext;

   public EditDeleteGameDialog(Context context, OnEditGameEventListener aOnOkListener)
   {
      super(context);
      mContext = context;
      setContentView(R.layout.edit_game_summary);
      mOnOkListener = aOnOkListener;
   }

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      Button ok = (Button) findViewById(R.id.ok);
      ok.setOnClickListener(new Button.OnClickListener() {
         @Override
         public void onClick(View v)
         {
            v = (View) v.getParent().getParent();
            TextView usNameField = (TextView) v.findViewById(R.id.editUs);
            String usName = usNameField.getText().toString();
            EditText themNameField = (EditText) v.findViewById(R.id.editThem);
            String themName = themNameField.getText().toString();
            TextView gameDateField = (TextView) v.findViewById(R.id.gameDate);
            String gameDateValue = gameDateField.getText().toString();
            TextView gameIdField = (TextView) v.findViewById(R.id.gameID);
            String gameIdValue = gameIdField.getText().toString();
            mOnOkListener.onEditGameHandler(usName, themName, gameDateValue,
                     gameIdValue, OnEditGameEventListener.OP_CHANGE);
            dismiss();
         }
      });

      Button delete = (Button) findViewById(R.id.delete);
      delete.setOnClickListener(new Button.OnClickListener() {
         @Override
         public void onClick(View v)
         {
            v = (View) v.getParent().getParent();
            TextView gameIdField = (TextView) v.findViewById(R.id.gameID);
            String gameIdValue = gameIdField.getText().toString();
            mOnOkListener.onEditGameHandler(null, null, null,
                     gameIdValue, OnEditGameEventListener.OP_DELETE);
            dismiss();
         }
      });

      Button cancel = (Button) findViewById(R.id.cancel);
      cancel.setOnClickListener(new Button.OnClickListener() {
         @Override
         public void onClick(View v)
         {
            dismiss();
         }
      });

      //I'm not sure this is needed. I got this behavior by explicitly adding
      //and android:imeOptions="actionDone" to the themTeamName EditText field
      //in the layout file. Leaving in anyway, hmmmm....
      EditText themTeamName = (EditText)findViewById(R.id.editThem);
      themTeamName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
         public boolean onEditorAction(TextView aThemTeamName, int actionId, KeyEvent event) {
             if (actionId == EditorInfo.IME_ACTION_DONE) {
                 // hide virtual keyboard
                 InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(aThemTeamName.getWindowToken(), 0);
                 return true;
             }
             return false;
         }
     });
   }

}
