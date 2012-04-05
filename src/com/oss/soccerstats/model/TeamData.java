/////////////////////////////////////////////////////////////////
// TeamData.java
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

public class TeamData
{
   public int     mId;
   public String  mTeamName;
   
   public TeamData(int aId, String aTeamName)
   {
      mId = aId;
      mTeamName = aTeamName;
   }
}
