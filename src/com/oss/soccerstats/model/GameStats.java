package com.oss.soccerstats.model;

public class GameStats
{
   private int mSogUs     = 0;
   private int mGoalsUs   = 0;
   private int mSogThem   = 0;
   private int mGoalsThem = 0;
   
   private String mOurTeamName;
   private String mTheirTeamName;
   private String mGameLocation;
   
   public int shotOnGoalUs()
   {
      return mSogUs++;
   }
   
   public int undoShotOnGoalUs()
   {
      if ( mSogUs > 0)  return mSogUs--;
      
      return mSogUs;
   }
   
   public int getSogUs()
   {
      return mSogUs;
   }
   
   public int scoreUs()
   {
      return mGoalsUs++;
   }
   
   public int undoScoreUs()
   {
      if ( mGoalsUs > 0)  return mGoalsUs--;
      
      return mGoalsUs;
   }
   
   public int getScoreUs()
   {
      return mGoalsUs;
   }
   
   public int shotOnGoalThem()
   {
      return mSogThem++;
   }
   
   public int undoShotOnGoalThem()
   {
      if ( mSogThem > 0)  return mSogThem--;
      
      return mSogThem;
   }
   
   public int getSogThem()
   {
      return mSogThem;
   }

   public int scoreThem()
   {
      return mGoalsThem++;
   }
   
   public int undoScoreThem()
   {
      if ( mGoalsThem > 0)  return mGoalsThem--;
      
      return mGoalsThem;
   }
   
   public int getScoreThem()
   {
      return mGoalsThem;
   }

   public void setOurTeamName(String aOurTeamName)
   {
      mOurTeamName = aOurTeamName;
   }

   public String getOurTeamName()
   {
      return mOurTeamName;
   }

   public void setTheirTeamName(String aTheirTeamName)
   {
      mTheirTeamName = aTheirTeamName;
   }

   public String getTheirTeamName()
   {
      return mTheirTeamName;
   }

   public void setGameLocation(String aGameLocation)
   {
      mGameLocation = aGameLocation;
   }

   public String getGameLocation()
   {
      return mGameLocation;
   }
   
}
