/////////////////////////////////////////////////////////////////
// PlayerSubData.java
//
// SoccerSoft - Proprietary (Restricted)
// Solely for authorized persons having a need to know
//
// Copyright (c) 2010, 2011, 2012 SoccerSoft - All rights reserved.
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF SoccerSoft.
// The copyright notice above does not evidence any actual
// or intended publication of such source code.
/////////////////////////////////////////////////////////////////

package com.oss.soccerstats.model;

public class PlayerSubData
{
   public int     id;
   public String  jerseyNo;
   public String  name;
   public long    timeInStateInMillis;
   public boolean onFieldBool;
   public long    benchTimeInMillis;
   public long    fieldTimeInMillis;
   
   public PlayerSubData(int aId, String aJerseyNo, String aName, long aTimeInState, boolean aOnFieldBool)
   {
      id = aId;
      jerseyNo = aJerseyNo;
      name = aName;
      timeInStateInMillis = aTimeInState;
      onFieldBool = aOnFieldBool;
      benchTimeInMillis = 0;
      fieldTimeInMillis = 0;
   }
}
