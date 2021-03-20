package student_player;

import java.util.ArrayList;

import boardgame.Move;

import pentago_twist.PentagoPlayer;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

/**
 * A player file submitted by a student.
 */
public class StudentPlayer extends PentagoPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260871056");
    }

    static private final int MAXDEPTHBLACK = 2;
    static private final int MAXDEPTHWHITE = 2;
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
        long startTime = System.nanoTime();

        FastBoard fastBoard = new FastBoard(boardState);
        PentagoBoardState.Piece piece;
        PentagoBoardState.Piece piece2;
        if (boardState.getTurnPlayer() == PentagoBoardState.WHITE) {
            piece = PentagoBoardState.Piece.WHITE;
            piece2 = PentagoBoardState.Piece.BLACK;
        } else {
            piece = PentagoBoardState.Piece.BLACK;
            piece2 = PentagoBoardState.Piece.WHITE;
        }

//        ArrayList<PentagoMove> legalMoves = boardState.getAllLegalMoves();
        ArrayList<PentagoMove> legalMoves = MyTools.getLegalMoves(fastBoard);
//        PentagoBoardState clone;
        Move bestMove = boardState.getRandomMove();
        int test;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        for (PentagoMove move : legalMoves) {
//            clone = (PentagoBoardState) boardState.clone();
//            clone.processMove(move);
            fastBoard.doMove(move);
            if (piece == PentagoBoardState.Piece.WHITE) {
                test = alphaBeta(fastBoard, MAXDEPTHWHITE, alpha, beta, MyTools.convertPiece(piece), MIN);
            } else {
                test = alphaBeta(fastBoard, MAXDEPTHBLACK, alpha, beta, MyTools.convertPiece(piece), MIN);
            }
            fastBoard.undoMove(move);
            if (test > alpha) {
                bestMove = move;
                alpha = test;
//            	System.out.print(test + "\n");
                if (alpha == beta) {
                    break;
                }
            }

        }
        long stopTime = System.nanoTime();
        System.out.println("Time Elapsed: " + (stopTime - startTime) / 100000000);
        // Return your move to be processed by the server.
        return bestMove;
    }

    int alphaBeta(FastBoard position, int depth, int alpha, int beta,
                  int piece, boolean isMAX) {
        position.evaluate(piece);
        if (position.getGameOver() || depth == 0) {
//            return MyTools.evaluate(position, piece);
            return position.evaluate(piece);
        }
//    	ArrayList<PentagoMove> moves = position.getAllLegalMoves();
        ArrayList<PentagoMove> moves = MyTools.getLegalMoves(position);
        for (PentagoMove m : moves) {
//            PentagoBoardState successor = (PentagoBoardState) position.clone();
//            successor.processMove(m);
            position.doMove(m);
            int value = alphaBeta(position, depth - 1, alpha, beta, piece, !isMAX);
            position.undoMove(m);
            if (isMAX) {
                if (value > beta) {
                    return beta;
                }
                if (value > alpha) {
                    alpha = value;
                }
            } else {
                if (value <= alpha) {
                    return alpha;
                }
                if (value < beta) {
                    beta = value;
                }
            }
        }
        if (isMAX) {
            return alpha;
        } else {
            return beta;
        }
    }
}