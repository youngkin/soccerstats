/////////////////////////////////////////////////////////////////
// LogUtil.java
//
// SoccerSoft - Proprietary (Restricted)
// Solely for authorized persons having a need to know
//
// Copyright (c) 2010, 2011 SoccerSoft - All rights reserved.
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF SoccerSoft.
// The copyright notice above does not evidence any actual
// or intended publication of such source code.
/////////////////////////////////////////////////////////////////

package com.oss.soccerstats.util;

import android.util.Log;

public class LogUtil
{

   public static void d(String tag, String msg)
   {
      if (Log.isLoggable(tag, Log.DEBUG))
      {
         Log.d(tag, msg);
      }
   }

   public static void i(String tag, String msg)
   {
      if (Log.isLoggable(tag, Log.INFO))
      {
         Log.i(tag, msg);
      }
   }

   public static void e(String tag, String msg)
   {
      if (Log.isLoggable(tag, Log.ERROR))
      {
         Log.e(tag, msg);
      }
   }

   public static void v(String tag, String msg)
   {
      if (Log.isLoggable(tag, Log.VERBOSE))
      {
         Log.v(tag, msg);
      }
   }

   public static void w(String tag, String msg)
   {
      if (Log.isLoggable(tag, Log.WARN))
      {
         Log.w(tag, msg);
      }
   }
}