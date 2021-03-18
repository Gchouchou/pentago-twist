package student_player;

import java.util.ArrayList;

import boardgame.Move;

import pentago_twist.PentagoPlayer;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260871056");
    }

    static private final int MAXDEPTH = 1;
    private final static boolean MAX = true;
	private final static boolean MIN = false;

	/**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {
    	if (!MyTools.checkLoaded()) {
    		MyTools.loadFile();
    	}

    	PentagoBoardState.Piece piece;
        PentagoBoardState.Piece piece2;
        if ( boardState.getTurnPlayer() == PentagoBoardState.WHITE ) {
        	piece = PentagoBoardState.Piece.WHITE;
        	piece2 = PentagoBoardState.Piece.BLACK;
        }
        else {
        	piece = PentagoBoardState.Piece.BLACK;
        	piece2 = PentagoBoardState.Piece.WHITE;
        }
        ArrayList<PentagoMove> legalMoves = boardState.getAllLegalMoves();
        PentagoBoardState clone;
        int max = Integer.MIN_VALUE;
        Move bestMove = boardState.getRandomMove();
        int test;
        for (PentagoMove move : legalMoves) {
        	clone = (PentagoBoardState) boardState.clone();
        	clone.processMove(move);
        	test = alphaBeta(clone, MAXDEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, piece, MIN);
        	if (test > max) {
        		bestMove = move;
        		max = test; 
            	System.out.print(test + "\n");
        	}
        }
        // Return your move to be processed by the server.
        return bestMove;
    }
    
    int alphaBeta(PentagoBoardState position, int depth, int alpha, int beta,
    		PentagoBoardState.Piece piece, boolean isMAX) {
    	if (position.gameOver() || depth == 0) {
    		return MyTools.evaluate(position, piece);
    	}
    	ArrayList<PentagoMove> moves = position.getAllLegalMoves();
		for (PentagoMove m: moves) {
			PentagoBoardState successor = (PentagoBoardState) position.clone();
			successor.processMove(m);
			int value = alphaBeta(successor,depth-1,alpha,beta, piece ,!isMAX);
			if (isMAX) {
				if (value > beta) {
    				return beta;
    			}
    			if (value > alpha)
    			{
    				alpha = value;
    			}
			}
			else {
				if (value <= alpha)
			   {
					return alpha;
			   }
			   if (value < beta)
			   {
					beta = value;
			   }
			}
		}
		if (isMAX) {
			return alpha;
		}
		else {
			return beta;
		}
	}
}