package com.oss.soccerstats.view;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.oss.soccerstats.R;

public class GameClockTab extends Activity
{
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.help_web_view);

      WebView helpText = (WebView)findViewById(R.id.help_webView);
      
      helpText.getSettings().setJavaScriptEnabled(false);
      helpText.loadUrl("file:///android_asset/helpPlayGame.html");
      
//TODO: REMOVE?
//      CharSequence helpContent = getText(R.string.gamePlayText);
//      helpText.setText(helpContent);
//      helpText.setEnabled(false);
//      LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
//      helpText.setLayoutParams(layoutParams);
   }
   

}
