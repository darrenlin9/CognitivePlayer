package org.ggp.base.player.gamer.statemachine;

import java.util.List;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

public final class CourseraCognitivePlayer extends StateMachineGamer
{
	/**
	 * Employs a simple sample "Monte Carlo" algorithm to come up with the best playable move,
	 * and then spends the last few seconds searching for a potential terminating move.
	 * This program is mostly a modification of a few basic algorithms provided by the Stanford class.
	 */
	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
	    StateMachine theMachine = getStateMachine();
		long start = System.currentTimeMillis();
		
		// Allow five seconds to do a search for a potential game-ending move before time runs out.
		long finishBy = timeout - 5000;
		
		List<Move> moves = theMachine.getLegalMoves(getCurrentState(), getRole());
		Move selection = moves.get(0);
		if (moves.size() > 1)
		{		
    		int[] totalPoints = new int[moves.size()];
    		int[] totalAttempts = new int[moves.size()];
    		
    		// Perform depth charges for each potential move, and keep track
    		// of the total score and total attempts accumulated for each move.
    		for (int i = 0; true; i = (i+1) % moves.size()) 
    		{
    		    if (System.currentTimeMillis() > finishBy)
    		        break;
    		    
    		    int theScore = performDepthChargeFromMove(theMachine.getInitialState(), moves.get(i));
    		    totalPoints[i] += theScore;
    		    totalAttempts[i]++;
    		}
    
    		// Calculate the expected score for each move.
    		double[] expectedPoints = new double[moves.size()];
    		for (int i = 0; i < moves.size(); i++) 
    		{
    		    expectedPoints[i] = (double)totalPoints[i] / totalAttempts[i];
    		}

    		// Find the move with the best expected score.
    		int bestMove = 0;
    		double bestMoveScore = expectedPoints[0];
    		for (int i = 1; i < moves.size(); i++) 
    		{
    		    if (expectedPoints[i] > bestMoveScore) 
    		    {
    		        bestMoveScore = expectedPoints[i];
    		        bestMove = i;
    		    }
    		}
    		
    		// Store the current best move.
    		selection = moves.get(bestMove);
		}
		
		// Searches for a state where the opponent could win the game and "blocks" it.
		for(Move moveUnderConsideration : moves) 
		{
		    MachineState nextState = theMachine.getNextState(getCurrentState(), theMachine.getRandomJointMove(getCurrentState(), getRole(), moveUnderConsideration));

		    if(theMachine.isTerminal(nextState)) 
		    {
		        if(theMachine.getGoal(nextState, getRole()) == 0) 
		        {
		            continue;
		        } 
		        
		        // If the opponent can win next turn with a move, take that move.
		        else if(theMachine.getGoal(nextState, getRole()) == 100)
		        {
	                selection = moveUnderConsideration;
	                return selection;
		        } 
		    }
		}
		long stop = System.currentTimeMillis();
		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}
	private int[] depth = new int[1];
	int performDepthChargeFromMove(MachineState theState, Move myMove) 
	{	    
		StateMachine theMachine = getStateMachine();
		try 
		{
	            MachineState finalState = theMachine.performDepthCharge(theMachine.getRandomNextState(theState, getRole(), myMove), depth);
	            return theMachine.getGoal(finalState, getRole());
	        } catch (Exception e) 
	        {
	            e.printStackTrace();
	            return 0;
	        }
	}
	

	// This is the default State Machine
	public StateMachine getInitialStateMachine() 
	{
		return new CachedStateMachine(new ProverStateMachine());
	}	
	public DetailPanel getDetailPanel()
	{
		return new SimpleDetailPanel();
	}	

	@Override
	public void stateMachineStop()
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void stateMachineAbort() 
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void preview(Game g, long timeout) throws GamePreviewException 
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getName()
	{
		return getClass().getSimpleName();
	}


	@Override
	public Move stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException 
	{
		// TODO Auto-generated method stub
		return null;
	}
}
