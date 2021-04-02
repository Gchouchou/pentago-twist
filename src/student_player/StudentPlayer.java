package student_player;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import boardgame.Move;

import pentago_twist.PentagoCoord;
import pentago_twist.PentagoPlayer;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

import student_player.MyTools.FastBoard;

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
     *   The possible ratios for each weight:
     */
    private static final int[] ratios1 = new int[]{0,1};
    private static final int[] ratios2 = new int[]{0,1,2,3};
    private static final int[] ratios3 = new int[]{2,3,5,10};
    private static final int[] ratios4 = new int[]{2,3,5,7,10};

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
//        if (stopTime- startTime <10000000) {
////            go deeper when we finish it in less 0.01 seconds
//            return alphaBetaWrapper(fastBoard,MAXDEPTHWHITE+1,MAXDEPTHBLACK+1);
//        }
        return move;
    }
    public PentagoMove alphaBetaWrapper(FastBoard fastBoard) {
        return alphaBetaWrapper(fastBoard,MAXDEPTHWHITE,MAXDEPTHBLACK);
    }

//    AlphaBeta algorithm wrapper
    public PentagoMove alphaBetaWrapper(FastBoard fastBoard,int maxDepthWhite, int maxDepthBlack) {
        int piece = fastBoard.getTurnPlayer();
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
                test = alphaBeta(fastBoard, maxDepthWhite, alpha, beta, piece, MIN);
            } else {
                test = alphaBeta(fastBoard, maxDepthBlack, alpha, beta, piece, MIN);
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
//        System.out.println("Time Elapsed: " + (stopTime - startTime) / 100000000);
        // Return your move to be processed by the server.
        return bestMove;
    }

    int alphaBeta(FastBoard position, int depth, int alpha, int beta,
                  int piece, boolean isMAX) {
        position.evaluate(piece);
        if (position.getGameOver() || depth == 0) {
//            return MyTools.evaluate(position, piece);
            return position.deepEvaluate(piece);
//            return position.evaluate(piece);
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
        ArrayList<int[]> allParams = new ArrayList<>(ratios1.length*ratios2.length*ratios3.length*ratios4.length);
//        add all possible parameters to arraylist
        for (int i = 0; i < ratios1.length; i++) {
            for (int j = 0; j < ratios2.length; j++) {
                for (int k = 0; k < ratios3.length; k++) {
                    for (int l = 0; l < ratios4.length; l++) {
                        allParams.add(getWeightsFromRatios(i,j,k,l));
                    }
                }
            }
        }
//        shuffle to see if there is a better result
        Collections.shuffle(allParams);
        /*
        int bStrat = 0;
        int wStrat = 0;
        while (true) {
            MyTools.loadWeights(FastBoard.WHITE,allParams.get(wStrat));
            MyTools.loadWeights(FastBoard.BLACK,allParams.get(bStrat));
            int winner = simulGame(new FastBoard());
            if (winner == FastBoard.BLACK) {
//                if Black wins
                boolean success = false;
                while (wStrat < allParams.size() - 1) {
                    wStrat++;
                    MyTools.loadWeights(FastBoard.WHITE,allParams.get(wStrat));
                    if (simulGame(new FastBoard()) == FastBoard.WHITE) {
                        int[] update = allParams.get(wStrat);
                        System.out.println("White wins!");
                        System.out.println("New weights: {"+ update[0] + "," + update[1] + ","
                        + update[2] + "," + update[3] + "," + update[4] + "," + update[5] + "}");
                        success = true;
                        break;
                    }
                }
                if (!success) {
                    int[] update = allParams.get(bStrat);
                    System.out.print("White could not win against ");
                    System.out.println("weights: {"+ update[0] + "," + update[1] + ","
                            + update[2] + "," + update[3] + "," + update[4] + "," + update[5] + "}");
                    break;
                }
            }
            if (winner == FastBoard.WHITE) {
//                if White wins
                boolean success = false;
                while (bStrat < allParams.size() - 1) {
                    bStrat++;
                    MyTools.loadWeights(FastBoard.BLACK,allParams.get(bStrat));
                    if (simulGame(new FastBoard()) == FastBoard.BLACK) {
                        int[] update = allParams.get(bStrat);
                        System.out.println("Black wins!");
                        System.out.println("New weights: {"+ update[0] + "," + update[1] + ","
                                + update[2] + "," + update[3] + "," + update[4] + "," + update[5] + "}");
                        success = true;
                        break;
                    }
                }
                if (!success) {
                    int[] update = allParams.get(wStrat);
                    System.out.print("Black could not win against ");
                    System.out.println("weights: {"+ update[0] + "," + update[1] + ","
                            + update[2] + "," + update[3] + "," + update[4] + "," + update[5] + "}");
                    break;
                }
            }
        }
        */

//        hard coded weights
        MyTools.loadWeights(FastBoard.BLACK, new int[]{0,0,3,50,250,0});
        int counter = 0;
        for (int[] arrays :
                allParams) {
            MyTools.loadWeights(FastBoard.WHITE, arrays);
            if (simulGame(new FastBoard()) == FastBoard.WHITE) {
                counter++;
                System.out.println("Beaten by {"+ arrays[0] + "," + arrays[1] + ","
                        + arrays[2] + "," + arrays[3] + "," + arrays[4] + "," + arrays[5] + "}");
            }
        }
        System.out.println("Number of counters:"+ counter);
    }


//  class that generalize a evaluation parameters

   public static int[] getWeightsFromRatios(int[] weights) {
                                                      //           int[] newWeights = new int[6];
                                                      //           newWeights[0] = 0;
                                                      //           newWeights[1] = ratios[0];
                                                      //           newWeights[2] = Integer.max(newWeights[1],1) * ratios[1];
                                                      //           newWeights[3] = Integer.max(newWeights[2],10) * ratios[2];
                                                      //           newWeights[4] = Integer.max(newWeights[3],10) * ratios[3];
                                                      //           newWeights[5] = 0;
                                                      return getWeightsFromRatios(weights[0],weights[1], weights[2], weights[3]);
                                                      }

   public static int[] getWeightsFromRatios(int weight1,int weight2,int weight3,int weight4) {
                                                                                         //           int ratio1 = weight1;
                                                                                         //           int ratio2 = weight2;
                                                                                         //           int ratio3;
                                                                                         //           switch (weight3) {
                                                                                         //               case 0:
                                                                                         //                   ratio3 = 1;
                                                                                         //                   break;
                                                                                         //               case 1:
                                                                                         //                   ratio3 = 2;
                                                                                         //                   break;
                                                                                         //               case 2:
                                                                                         //                   ratio3 = 3;
                                                                                         //                   break;
                                                                                         //               case 3:
                                                                                         //                   ratio3 = 4;
                                                                                         //                   break;
                                                                                         //               default:
                                                                                         //                   ratio3 = 10;
                                                                                         //                   break;
                                                                                         //           }
                                                                                         //           int ratio4;
                                                                                         //           switch (weight4) {
                                                                                         //               case 0:
                                                                                         //                   ratio4 = 2;
                                                                                         //                   break;
                                                                                         //               case 1:
                                                                                         //                   ratio4 = 3;
                                                                                         //                   break;
                                                                                         //               case 2:
                                                                                         //                   ratio4 = 5;
                                                                                         //                   break;
                                                                                         //               default:
                                                                                         //                   ratio4 = 10;
                                                                                         //                   break;
                                                                                         //           }
                                                                                         int ratio1 = ratios1[weight1];
                                                                                         int ratio2 = ratios2[weight2];
                                                                                         int ratio3 = ratios3[weight3];
                                                                                         int ratio4 = ratios4[weight4];
                                                                                         int[] newWeights = new int[6];
                                                                                         newWeights[0] = 0;
                                                                                         newWeights[1] = ratio1;
                                                                                         newWeights[2] = Integer.max(newWeights[1],1) * ratio2;
                                                                                         newWeights[3] = Integer.max(newWeights[2],5) * ratio3;
                                                                                         newWeights[4] = Integer.max(newWeights[3],10) * ratio4;
                                                                                         newWeights[5] = 0;
                                                                                         return newWeights;
                                                                                         }

   public static int[] copy(int[] base) {
       int[] copy = new int[base.length];
       for (int i = 0; i < base.length; i++) {
           copy[i] = base[i];
       }
       return copy;
   }

    public static void openingMoveSimul() {
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
//    3 means a draw.
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