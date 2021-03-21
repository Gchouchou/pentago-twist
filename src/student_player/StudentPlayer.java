package student_player;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import boardgame.Move;

import pentago_twist.PentagoCoord;
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

        FastBoard fastBoard = new FastBoard(boardState);
//        PentagoBoardState.Piece piece;
//        PentagoBoardState.Piece piece2;
//        if (boardState.getTurnPlayer() == PentagoBoardState.WHITE) {
//            piece = PentagoBoardState.Piece.WHITE;
//            piece2 = PentagoBoardState.Piece.BLACK;
//        } else {
//            piece = PentagoBoardState.Piece.BLACK;
//            piece2 = PentagoBoardState.Piece.WHITE;
//        }
        return alphaBetaWrapper(fastBoard);
    }

//    AlphaBeta algorithm wrapper
    public PentagoMove alphaBetaWrapper(FastBoard fastBoard) {
        int piece = fastBoard.getTurnPlayer();
        long startTime = System.nanoTime();
        ArrayList<PentagoMove> legalMoves = MyTools.getLegalMoves(fastBoard, piece, MAX);
        PentagoMove bestMove = legalMoves.get(0);
        int test;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        for (PentagoMove move : legalMoves) {
//            clone = (PentagoBoardState) boardState.clone();
//            clone.processMove(move);
            fastBoard.doMove(move);
            if (piece == fastBoard.WHITE) {
                test = alphaBeta(fastBoard, MAXDEPTHWHITE, alpha, beta, piece, MIN);
            } else {
                test = alphaBeta(fastBoard, MAXDEPTHBLACK, alpha, beta, piece, MIN);
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
//        System.out.println("Time Elapsed: " + (stopTime - startTime) / 100000000);
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
        ArrayList<PentagoMove> moves = MyTools.getLegalMoves(position, piece, isMAX);
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

    public static void main(String[] args) {
        if (!MyTools.checkLoaded()) {
            MyTools.loadFile();
        }
        Random rand = new Random();
        int[][] scores = new int[6][6];
        int[][] plays = new int[6][6];
//        run X games
        for (int i = 0; i < 100; i++) {
//            boolean dwin = false;
            FastBoard fastBoard = new FastBoard();
            fastBoard.turnNumber = 2;
            PentagoCoord[] array = new PentagoCoord[4];
//            place randomly 4 stones
            for (int j = 0; j < 4; j++) {
                int x = rand.nextInt(PentagoBoardState.BOARD_SIZE);
                int y = rand.nextInt(PentagoBoardState.BOARD_SIZE);
                if (fastBoard.board[x][y] == FastBoard.EMPTY) {
                    array[j] = new PentagoCoord(x,y);
                    if (j < 2) {
                        fastBoard.board[x][y] = FastBoard.WHITE;
                    }
                    else {
                        fastBoard.board[x][y] = FastBoard.BLACK;
                    }
                }
                else { j--; }
            }
            int outcome = simulGame(fastBoard);
            for (int j = 0; j < 4; j++) {
                plays[array[j].getX()][array[j].getY()]++;
            }
            if (outcome == FastBoard.WHITE) {
                for (int j = 0; j < 2; j++) {
                    scores[array[j].getX()][array[j].getY()] += 2;
                }
            }
            else if (outcome == FastBoard.BLACK) {
                for (int j = 2; j < 4; j++) {
                    scores[array[j].getX()][array[j].getY()] += 2;
                }
            }
            else {
                for (int j = 0; j < 4; j++) {
                    scores[array[j].getX()][array[j].getY()] += 1;
                }
            }
            fastBoard = new FastBoard();
            fastBoard.turnNumber = 2;
            for (int j = 0; j < 4; j++) {
                if (j < 2)
                    fastBoard.board[array[j].getX()][array[j].getY()] = FastBoard.BLACK;
                else
                    fastBoard.board[array[j].getX()][array[j].getY()] = FastBoard.WHITE;
            }
            outcome = simulGame(fastBoard);
            if (outcome == FastBoard.WHITE) {
                for (int j = 0; j < 2; j++) {
                    scores[array[j].getX()][array[j].getY()] += 2;
                }
            }
            else if (outcome == FastBoard.BLACK) {
                for (int j = 2; j < 4; j++) {
                    scores[array[j].getX()][array[j].getY()] += 2;
                }
            }
            else {
                for (int j = 0; j < 4; j++) {
                    scores[array[j].getX()][array[j].getY()] += 1;
                }
            }
            System.out.println("Iteration: "+i);
        }
        try {
            FileWriter myWriter = new FileWriter("data/scores4.csv");
            for (int i = 0; i < 36; i++) {
                myWriter.write(i / 6 + "," + i % 6 + "," + scores[i / 6][i%6] + "," + plays[i/6][i%6] +"\n");
            }
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

//    game simulation where we return the piece that won
    static int simulGame(FastBoard board) {
        board.evaluate(FastBoard.WHITE);
        StudentPlayer player1 = new StudentPlayer();
        StudentPlayer player2 = new StudentPlayer();
        while (!board.getGameOver()) {
            if(board.getAllLegalMoves().size()  != 0 ) {
                board.doMove(player1.alphaBetaWrapper(board));
            }
            else {
                board.gameOver = true;
            }
            board.evaluate(FastBoard.WHITE);
        }
        if (board.evaluate(FastBoard.WHITE) > 0) { return FastBoard.WHITE; }
        else if (board.evaluate(FastBoard.WHITE) < 0) { return FastBoard.BLACK;}
        else { return FastBoard.EMPTY; }
    }
}