/**
* This file is based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.sparcraft.players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import bwmcts.sparcraft.Constants;
import bwmcts.sparcraft.EvaluationMethods;
import bwmcts.sparcraft.Game;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.Position;
import bwmcts.sparcraft.StateEvalScore;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitActionTypes;

import java.util.Random;

public class Player_Watcher extends Player {

	private int _id = 0;
	private int enemy;
	Random ran;

	public Player_Watcher(int playerID) {
		_id = playerID;
		setID(playerID);
		enemy = GameState.getEnemy(_id);
		ran = new Random();
	}

	public void getMoves(GameState state, HashMap<Integer, List<UnitAction>> moves, List<UnitAction> moveVec) {
		moveVec.clear();
		boolean foundUnitAction = false;
		int actionMoveIndex = 0;
		int furthestMoveIndex = 0;
		int closestMoveIndex = 0;
		Unit ourUnit, closestUnit = null;
		List<UnitAction> actions;
		int dist = 0, bestMoveIndex = 0;
		UnitAction move;

		for (Integer u : moves.keySet()) {//currently only testing for one unit....
			foundUnitAction = false;
			actionMoveIndex = 0;
			

			ourUnit = (state.getUnit(_id, u));

			bestMoveIndex = 0;
			float maxScore = 0;
			
			actions = moves.get(u);// a list of unit actions of this unit
			int minDiff = 999999999;
			
			int genesMutated[] = new int[5];
			Arrays.fill(genesMutated,-1);
			int geneIndex = 0;
			
			//System.out.println("action size: "+actions.size());
			
			
			//////////////////////EVOLUTION ALG///////////////////////////
			int gene = 0;
			for (int attempt = 0;attempt<3;attempt++){
				gene = ran.nextInt(actions.size());
				genesMutated[geneIndex] = gene;
				geneIndex++;
				
				move = actions.get(gene);
				if(move.type()!=UnitActionTypes.MOVE){continue;} //currently just don't care about attack
				
				//here for this move we will create a new state
				//to implement state-based alg.
				GameState sc = state.clone(); //sc for state clone
				Game gc = new Game(sc, new Player_NoOverKillAttackValue(this.enemy),
						new Player_NoOverKillAttackValue(this.enemy), 30, false);
				ArrayList<UnitAction> newMoveVec = new ArrayList<UnitAction>();
				newMoveVec.clear();
				
				newMoveVec.add(actions.get(gene));
				gc.forwardWithMoves(newMoveVec);
				newMoveVec.clear();
				GameState newState = gc.getState();
				
				Unit s_Unit = newState.getUnit(ID(), u);
				Unit s_closestUnit=s_Unit.canHeal() ? newState.getClosestOurUnit(ID(), u) : newState.getClosestEnemyUnit(_id,u);
				Position s_ourDest			=new Position(s_Unit.pos().getX(),s_Unit.pos().getY());
				int s_dist					=s_closestUnit.getDistanceSqToPosition(s_ourDest, newState.getTime());
				int difference = s_dist - 50000;
				if(difference<0){difference = - difference;}
				if(difference<minDiff){
					minDiff = difference;
					bestMoveIndex = gene;
				}
			}
			//////////////////////EVOLUTION ALG///////////////////////////		

			
			/*
			//THE FOLLOWING IS THE OLD CODE!!!!!!!!!!!!!!!
			for (int m = 0; m < actions.size(); ++m) {
				move = actions.get(m);
				if(move.type()!=UnitActionTypes.MOVE){continue;} //currently just don't care about attack
				
				//here for this move we will create a new state
				//to implement state-based alg.
				GameState sc = state.clone(); //sc for state clone
				Game gc = new Game(sc, new Player_NoOverKillAttackValue(this.enemy),
						new Player_NoOverKillAttackValue(this.enemy), 30, false);
				ArrayList<UnitAction> newMoveVec = new ArrayList<UnitAction>();
				newMoveVec.clear();
				newMoveVec.add(actions.get(m));
				gc.forwardWithMoves(newMoveVec);
				newMoveVec.clear();
				GameState newState = gc.getState();
				
				Unit s_Unit = newState.getUnit(ID(), u);
				Unit s_closestUnit=s_Unit.canHeal() ? newState.getClosestOurUnit(ID(), u) : newState.getClosestEnemyUnit(_id,u);
				Position s_ourDest			=new Position(s_Unit.pos().getX(),s_Unit.pos().getY());
				int s_dist					=s_closestUnit.getDistanceSqToPosition(s_ourDest, newState.getTime());
				int difference = s_dist - 50000;
				if(difference<0){difference = - difference;}
				if(difference<minDiff){
					minDiff = difference;
					bestMoveIndex = m;
				}
				
				//System.out.println("distance: "+s_dist);
				
				StateEvalScore gameScore = newState.eval(Players.Player_One.ordinal(), EvaluationMethods.LTD2);
				//System.out.println("move: "+m+" score: "+gameScore._val);
				////////////////////////////////
				//////////////////FIX THIS/////////////////
				///////////////////////////////////
			}
			*/

			// the move we will be returning
			//bestMoveIndex = 0;

			// if we have an attack move we will use that one
			if (foundUnitAction) // if can attack now
			{
				
				bestMoveIndex = actionMoveIndex;
			}
			
			
			/*
			// if I can put some state-based stuff here and then pick a new
			// bestMoveIndex...
			int moveLimit = 30; // so this is the limit of a game's length
			GameState stateClone = state.clone();
			Game gameClone = new Game(stateClone, new Player_NoOverKillAttackValue(this.enemy),
					new Player_NoOverKillAttackValue(this.enemy), moveLimit, false);
			gameClone.play(); // so this will simply play til the end...
			GameState finalState = gameClone.getState();
			// you can now evaluate the state however you wish. let's use an
			// LTD2 evaluation from the point of view of player one
			StateEvalScore gameScore = finalState.eval(Players.Player_One.ordinal(), EvaluationMethods.LTD2);
			//System.out.println(gameScore._val);

			// not necessary to use play() but we can use the following:
			// state.makeMoves(moveVec); //let's not worry about its details for
			// now
			// state.finishedMoving();
			// moveVec.clear();			
			*/
			//LTD2()
			System.out.print("Mutated Genes: ");
			for(int i=0;i<genesMutated.length;i++){
				if(genesMutated[i]!=-1){System.out.print(genesMutated[i]+" ");}
			}
			System.out.println(" Best gene found, gene index: "+ bestMoveIndex);
			
			//the following line will give the next moves(the actual next moves) to the real game
			moveVec.add(actions.get(bestMoveIndex));
		}
	}
	public String toString() {
		return "Watcher's first state-based alg";
	}
}
