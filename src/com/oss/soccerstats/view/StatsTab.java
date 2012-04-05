package com.oss.soccerstats.view;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.oss.soccerstats.R;

public class StatsTab extends Activity
{
      public void onCreate(Bundle savedInstanceState)
      {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.help);

         TextView helpText = (TextView)findViewById(R.id.helpText);
         CharSequence helpContent = getText(R.string.playerStatsText);
         helpText.setText(helpContent);
         helpText.setEnabled(false);
         LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
         helpText.setLayoutParams(layoutParams);
   }

}
