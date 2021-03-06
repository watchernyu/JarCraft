/**
* This file is based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.sparcraft.players;

import java.util.HashMap;
import java.util.List;

import bwmcts.sparcraft.Constants;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Position;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitActionTypes;

public class Player_RetreatFar extends Player {
	//will try to get help from the farthest friendly unit
	
	private int _id=0;
	
	public Player_RetreatFar(int playerID) {
		_id=playerID;
		setID(playerID);
	}

	public void getMoves(GameState  state, HashMap<Integer,List<UnitAction>> moves, List<UnitAction>  moveVec)
	{
	    moveVec.clear();
		for (Integer u : moves.keySet())
		{
			boolean foundUnitAction				=false;
			int actionMoveIndex				=0;
			int closestMoveIndex				=0;
			int actionDistance	=Integer.MAX_VALUE;
			int closestMoveDist	=Integer.MAX_VALUE;
			double actionHighestDPS=-9999;

			Unit ourUnit = state.getUnit(ID(), u);
			
			Unit farthestUnit			=state.getFarthestOurUnit(ID(), u);
			//System.out.println(moves.get(0));///for testing
			/////////////////////////////////////////////////
			
			for (int m=0; m<moves.get(u).size(); m++)
			{
				UnitAction move	=moves.get(u).get(m);
					

				if (move.type() == UnitActionTypes.MOVE)
				{
					Position ourDest			=new Position(ourUnit.pos().getX() + Constants.Move_Dir[move.index()][0],	 ourUnit.pos().getY() + Constants.Move_Dir[move.index()][1]);
					int dist					=farthestUnit.getDistanceSqToPosition(ourDest, state.getTime());

					if (dist < closestMoveDist)
					{
						closestMoveDist = dist;
						closestMoveIndex = m;
					}
				}
			}

			int bestMoveIndex=closestMoveIndex;

			moveVec.add(moves.get(u).get(bestMoveIndex));
		}
	}
	
	public String toString(){
		return "AttackClosest";
	}
}
