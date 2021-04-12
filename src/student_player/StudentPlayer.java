package student_player;

import boardgame.Move;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoCoord;
import pentago_twist.PentagoMove;
import pentago_twist.PentagoPlayer;
import student_player.MyTools.FastBoard;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

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

    static private final int MAXDEPTHBLACK = 3;
    static private final int MAXDEPTHWHITE = 3;
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

        FastBoard fastBoard = new FastBoard(boardState);
        long startTime = System.nanoTime();
        PentagoMove move = alphaBetaWrapper(fastBoard);
        long stopTime = System.nanoTime();
//        System.out.println("" + ( stopTime -startTime ) / 10000000);
        if (stopTime- startTime <20000000) {
//            go deeper when we finish it in less 0.02 seconds
            return alphaBetaWrapper(fastBoard,MAXDEPTHWHITE+1,MAXDEPTHBLACK+1);
        }
        return move;
    }

    public PentagoMove alphaBetaWrapper(FastBoard fastBoard) {
        return alphaBetaWrapper(fastBoard, MAXDEPTHWHITE, MAXDEPTHBLACK);
    }

    /**
     * Modified minimax to return the chosen move (only the first layer)
     */
    public PentagoMove alphaBetaWrapper(FastBoard fastBoard, int maxDepthWhite, int maxDepthBlack) {
        int piece = fastBoard.getTurnPlayer();
        ArrayList<PentagoMove> legalMoves = MyTools.getLegalMoves(fastBoard, piece);
        PentagoMove bestMove = legalMoves.get(0);
        int test;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        for (PentagoMove move : legalMoves) {
            fastBoard.doMove(move);
            if (piece == FastBoard.WHITE) {
                test = alphaBeta(fastBoard, maxDepthWhite-1, alpha, beta, piece, MIN);
            } else {
                test = alphaBeta(fastBoard, maxDepthBlack-1, alpha, beta, piece, MIN);
            }
            fastBoard.undoMove(move);
            if (test > alpha) {
                bestMove = move;
                alpha = test;
//                System.out.print(test + "\n");
                if (alpha == beta) {
                    break;
                }
            }

        }
//        System.out.println("Time Elapsed: " + (stopTime - startTime) / 100000000);
        return bestMove;
    }

    /**
     * Standard fixed depth Alpha beta pruning minimax algorithm returning the score
     */
    int alphaBeta(FastBoard position, int depth, int alpha, int beta,
                  int piece, boolean isMAX) {
        position.evaluate(piece);
        if (position.getGameOver() || depth == 0) {
            return position.deepEvaluate(piece);
        }
        ArrayList<PentagoMove> moves = MyTools.getLegalMoves(position, piece);
        for (PentagoMove m : moves) {
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