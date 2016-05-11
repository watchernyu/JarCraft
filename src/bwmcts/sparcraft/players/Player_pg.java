/**
* This file is based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.sparcraft.players;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bwmcts.sparcraft.Constants;
import bwmcts.sparcraft.EvaluationMethods;
import bwmcts.sparcraft.Game;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.Position;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitActionTypes;

public class Player_pg extends Player {
	//PORTFOLIO GREEDY SEARCH
	
	private int _id=0;
	ArrayList<Player> portfolio;
	ArrayList<Player> ourScripts;
	ArrayList<Player> enemyScripts;
	int Iteration = 3;
	int R = 3;
	int numOfUnits = 32;
	long timeLimit = 40;
	long timeElapsed = 0;
	long startTime = 0;
	boolean LIMITTIME = true;
	int ROUNDLIMIT = 10;
	
	public Player_pg(int playerID) {
		_id=playerID;
		setID(playerID);
		portfolio = new ArrayList<Player>();
		ourScripts= new ArrayList<Player>();
		enemyScripts= new ArrayList<Player>();
		
		portfolio.add(new Player_NoOverKillAttackValue(playerID));
		portfolio.add(new Player_NOKAVMicroRight(playerID));
		portfolio.add(new Player_NOKAVMicroLeft(playerID));
		portfolio.add(new Player_NOKAVMicroUp(playerID));
		portfolio.add(new Player_NOKAVMicroDown(playerID));
		
		//portfolio.add(new Player_KiteDPS(playerID));
		//portfolio.add(new Player_NOKAVForward(playerID));
		//portfolio.add(new Player_NOKAVBack(playerID));
		//portfolio.add(new Player_NOKAVForwardFar(playerID));
		//portfolio.add(new Player_NOKAVBackClose(playerID));
		//portfolio.add(new Player_NOKAVBackFar(playerID));
		
		setting1();
	}

	public void setting1(){
		R = 0;
		Iteration = 1;
		ROUNDLIMIT = 200;
	}
	
	public void setting2(){
		R = 1;
		Iteration = 2;
		ROUNDLIMIT = 75;
	}
	
	public void setting3(){
		R = 1;
		Iteration = 1;
		ROUNDLIMIT = 25;
	}
	
	public void getMoves(GameState state, HashMap<Integer,List<UnitAction>> moves, List<UnitAction>  moveVec)
	{
	    moveVec.clear();
	    
	    startTime = System.currentTimeMillis();
	    
	    for(int i=0;i<numOfUnits;i++){
	    	enemyScripts.add(portfolio.get(0));
	    }
	    
	    fill(enemyScripts,portfolio.get(0));
	    ourScripts = getSeedScripts(state, _id, enemyScripts);
	    enemyScripts = getSeedScripts(state, (_id+1)%2,ourScripts);
	    improve(state, _id, ourScripts,enemyScripts);
	    
	    for(int r=0;r<R;r++){
	    	improve(state, _id, ourScripts,enemyScripts);
	    	improve(state, (_id+1)%2,enemyScripts,ourScripts);
	    }
	    
	    
	    if(!LIMITTIME){
	    	//System.out.println("PG Time used: "+(System.currentTimeMillis()-startTime));
	    }
	    
	    makeMove(ourScripts,state,moves,moveVec);
	}
	
	public ArrayList<Player> getSeedScripts(GameState state,int playerId,ArrayList<Player> enemy_scripts){
		int bestValue = -99999;
		Player bestScript=portfolio.get(0);
		ArrayList<Player> seedScripts = new ArrayList<Player>();
		for(int i=0;i<numOfUnits;i++){
			seedScripts.add(portfolio.get(0));
		}
		
		for(int i=0;i<portfolio.size();i++){
			fill(seedScripts,portfolio.get(i));
			int value = playout(state,playerId,seedScripts,enemy_scripts);
			//need to create a helper player that
			//has a sequence of scripts and will getmoves from those scripts.
			
			//we need to do TWO VERSIONS OF THE PLAYOUT
			//ONE VERSION IS SAME TO THE ONE IN THE PAPER:
			//IN THE PLAYOUT, EACH UNIT FOLLOW ITS ASSIGNED SCRIPT
			//THE OTHER VERSION... WE WILL THINK ABOUT IT...
			//THE OTHER VERSION: EACH UNIT USE THEIR OWN SCRIPT AT FIRST STEP THEN PLAYOUT ALL WITH NOKAV
			if (value>bestValue){
				bestValue = value;
				bestScript = portfolio.get(i);
			}
			//System.out.println("BestScript: "+bestScript+" Best value: "+bestValue);
		}
		fill(seedScripts,bestScript);
		return seedScripts;
	}
	
	public void improve(GameState state,int playerId,ArrayList<Player> self_scripts,ArrayList<Player> enemy_scripts){

		int count = 0;//just for testing.
		for (int i=0;i<Iteration;i++){
			for (int u=0;u<numOfUnits;u++){
				if(LIMITTIME&&System.currentTimeMillis()-startTime>timeLimit){
					//System.out.println("time exceeded");
					return;
				}
				
				int bestValue = -99999;
				Player bestScript=portfolio.get(0);
				for(int s=0;s<portfolio.size();s++){
					count++;
					self_scripts.set(u, portfolio.get(s));
					int value = playout(state,playerId,self_scripts,enemy_scripts);
					if(value>bestValue){
						bestValue = value;
						bestScript = portfolio.get(s);
					}
				}
				self_scripts.set(u, bestScript);
			}
		}
		//System.out.println("Improved scripts: "+self_scripts);
		//System.out.println("Count: "+count);
	}
	
	public int playout(GameState state,int playerId,ArrayList<Player> our_scripts,ArrayList<Player> enemy_scripts){
		GameState sc = state.clone(); // sc for state clone
		
		Game gc = new Game(sc, new Player_pg_helper(0,our_scripts),
				new Player_pg_helper(1,enemy_scripts), ROUNDLIMIT, false); //send scripts to game...
		gc.play();
		int scoreval = gc.getState().eval(playerId, EvaluationMethods.LTD2)._val;
		return scoreval;
	}
	
	public void fill(ArrayList<Player> seedScripts,Player s){
		//seedScripts has to be initialized before using this helper
		int n = seedScripts.size();
		for(int i=0;i<n;i++){
			seedScripts.set(i, s);
		}
	}
	
	public void makeMove(ArrayList<Player> scriptsToUse,GameState state, HashMap<Integer,List<UnitAction>> moves,
			List<UnitAction> moveVec){
		for (Integer u : moves.keySet()){
			//this u is a unit index!!
			Player scriptToUse = scriptsToUse.get(u); //
			HashMap<Integer,List<UnitAction>> oneUnitMap = new HashMap<Integer,List<UnitAction>>();
			oneUnitMap.put(u, moves.get(u));
			scriptToUse.getMoves(state, oneUnitMap, moveVec);
		}
	}
	
	public String toString(){
		return "Churchill's Portfolio Greedy Search";
	}
}
