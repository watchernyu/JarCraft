package genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import bwmcts.sparcraft.Game;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;

public class Population {
	public boolean showEvolutionProcess = false;
	public double mutateRate = 0.2;
	public int numMutateEachSuperior = 2;
	public int numOfBest = 3;
	public int generation;
	public int bestscore;
	//public ArrayList<ArrayList<ArrayList<Integer>>> DNAs;
	public ArrayList<Beast> beasts;

	public int futureSteps;
	public int numOfMutations;
	public int numOfScripts;
	public int numOfBeasts;
	public int numOfUnits;

	private int evalGameRoundLimit = 20;
	private Random ran;
	private GameState state; //THIS WILL NOT BE UPDATED SO EACH TIME IN GETMOVES() 
	private ArrayList<Player> scripts;
	private Player player;
	
	private Player playerForEval1;
	private Player playerForEval2;

	//NEED TO CREATE A NEW POPULATION
	public Population(Player player,GameState state,int futureSteps, int numOfScripts,int numOfUnits,ArrayList<Player> scripts) {
		this.state = state;
		this.generation = 0;
		this.futureSteps = futureSteps;
		this.numOfUnits = numOfUnits;
		this.numOfScripts = numOfScripts;
		this.ran = new Random();
		this.scripts = scripts;
		this.player = player;
		this.beasts = new ArrayList<Beast>();
		
		playerForEval1 = new Player_NoOverKillAttackValue(0);
		playerForEval2 = new Player_NoOverKillAttackValue(1);
	}
	
	public void reinitialize(GameState s){
		this.state = s;
		beasts.clear();
		initialize();
		
		/*
		//long st = System.currentTimeMillis();//##
		for(int p=0;p<numOfBest;p++){//refresh the score..
			beasts.get(p).score = evalDna(state,beasts.get(p).getDna());
		}
		
		for(int p=0;p<1;p++){
			ArrayList<ArrayList<Integer>> DNA = new ArrayList<ArrayList<Integer>>();
			for(int k=0;k<futureSteps;k++){
				DNA.add(new ArrayList<Integer>());
				for(int i=0;i<numOfUnits;i++){
					DNA.get(k).add(0); //for now, all start with pure NOKAV scripts.
				}
			}
			int score = evalDna(state,DNA);
			beasts.add(new Beast(DNA,score));
		}
		select();
		//System.out.println("time used: "+(System.currentTimeMillis()-st));
		 * 
		 */
	}

	public void initialize(){
		//long st = System.currentTimeMillis();//##
		int basicScore = 0;
		for(int p=0;p<1;p++){
			ArrayList<ArrayList<Integer>> DNA = new ArrayList<ArrayList<Integer>>();
			for(int k=0;k<futureSteps;k++){
				DNA.add(new ArrayList<Integer>());
				for(int i=0;i<numOfUnits;i++){
					DNA.get(k).add(0); //for now, all start with pure NOKAV scripts.
				}
			}
			int score = evalDna(state,DNA);
			basicScore=score;
			beasts.add(new Beast(DNA,score));
		}
		
		for(int p=0;p<numOfBest-1;p++){
			ArrayList<ArrayList<Integer>> DNA = new ArrayList<ArrayList<Integer>>();
			for(int k=0;k<futureSteps;k++){
				DNA.add(new ArrayList<Integer>());
				for(int i=0;i<numOfUnits;i++){
					DNA.get(k).add(0); //for now, all start with pure NOKAV scripts.
				}
			}
			beasts.add(new Beast(DNA,basicScore));
		}
		select();
		//System.out.println("time used: "+(System.currentTimeMillis()-st));
	}
	
	public void evolve(int rounds){//continue evolving for a certain rounds
		for(int i=0;i<rounds;i++){
			mutateAll();
			select();
		}
	}
	
	public ArrayList<ArrayList<Integer>> bestDna(){
		return beasts.get(0).getDna();
	}
	
	public void printHighScore(){
		System.out.println("Current high scores in population: ");
		for (int i=0;i<4;i++){
			System.out.print(beasts.get(i).score+" ");
		}
		System.out.println();
	}
	
	public void select(){//basically discard all the beasts that are less valuable..
		//long st = System.currentTimeMillis();
		Collections.sort(beasts);
		generation ++;

		ArrayList<Beast> tempbeasts = new ArrayList<Beast>();
		for (int i=0;i<numOfBest;i++){
			tempbeasts.add(beasts.get(i));
		}
		beasts.clear();
		beasts = tempbeasts;
		if(showEvolutionProcess){
			System.out.println("Generation: "+generation);//only for testing
			showBeasts();//only for testing
		}
		//System.out.println("Select time used: "+(System.currentTimeMillis()-st));
	}
	
	public void showBeasts(){//will display the all the beasts in population, along with their scores.
		int n = beasts.size();
		for(int i=0;i<n;i++){
			beasts.get(i).printDnaAndScore();
		}
	}
	
	public void mutateAll(){
		//long st = System.currentTimeMillis();
		for(int index =0;index<numOfBest;index++){//the best 5 dnas are used to mutate
			ArrayList<ArrayList<Integer>> currentDna = beasts.get(index).getDna();
			for(int m=0;m<numMutateEachSuperior;m++){//each superior DNA will be mutated 4 times
				ArrayList<ArrayList<Integer>> newDna = mutateDna(currentDna);
				int score = evalDna(state,newDna);
				beasts.add(new Beast(newDna,score));
			}
		}
		//System.out.println("time used: "+(System.currentTimeMillis()-st));
	}
	
	public ArrayList<ArrayList<Integer>> mutateDna(ArrayList<ArrayList<Integer>> DNA) {
		//will return a mutated dna. DON'T FORGET TO DEEPCOPY!!
		ArrayList<ArrayList<Integer>> newDNA = new ArrayList<ArrayList<Integer>>();
		// helper function to mutate a DNA, according to some rate.
		for(int i=0;i<DNA.size();i++){
			ArrayList<Integer> dnapiece = DNA.get(i);
			ArrayList<Integer> newdnapiece = new ArrayList<Integer>();
			newDNA.add(newdnapiece);
			for(int j=0;j<DNA.get(0).size();j++){
				Double mut = ran.nextDouble();
				if (mut < mutateRate) { //add a mutant
					int newGene = ran.nextInt(scripts.size());
					newdnapiece.add(newGene);
				}else{ //else add the old gene
					newdnapiece.add(dnapiece.get(j));
				}
			}
		}
		return newDNA;
	}
	
	public int evalDna(GameState currentState,ArrayList<ArrayList<Integer>> DNA){
		GameState sc = currentState.clone(); // sc for state clone
		Game gc = new Game(sc, playerForEval1,
				playerForEval2, evalGameRoundLimit, false, scripts); //send scripts to game...
		//long st = System.currentTimeMillis();
		int ss = gc.dnaEvalGroup(DNA,1);
		//System.out.println("time used: "+(System.currentTimeMillis()-st));
		return ss; //HARDCODE EVALUATION METHOD TO 1
	}
}
