package student_player;

import boardgame.Move;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoCoord;
import pentago_twist.PentagoMove;
import pentago_twist.PentagoPlayer;
import student_player.MyTools.FastBoard;

import java.io.*;
import java.util.*;

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
     * The possible ratios for each weight:
     */
    private static final int[] ratios1 = new int[]{0, 1};
    private static final int[] ratios2 = new int[]{0, 1, 2, 3};
    private static final int[] ratios3 = new int[]{2, 3, 5, 10};
    private static final int[] ratios4 = new int[]{2, 3, 5, 7, 10, 15};

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
        return alphaBetaWrapper(fastBoard, MAXDEPTHWHITE, MAXDEPTHBLACK);
    }

    //    AlphaBeta algorithm wrapper
    public PentagoMove alphaBetaWrapper(FastBoard fastBoard, int maxDepthWhite, int maxDepthBlack) {
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
            if (piece == FastBoard.WHITE) {
                test = alphaBeta(fastBoard, maxDepthWhite, alpha, beta, piece, MIN);
            } else {
                test = alphaBeta(fastBoard, maxDepthBlack, alpha, beta, piece, MIN);
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
        ArrayList<PentagoMove> moves = position.getAllLegalMoves();
//        ArrayList<PentagoMove> moves = MyTools.getLegalMoves(position, piece, isMAX);
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
//        runSimulations();
//        testWeights(FastBoard.WHITE, new int[]{0,1,2,10,100,0});
//        testWeights(FastBoard.BLACK, new int[]{0,0,3,50,250,0});
    }

    /**
     * Generates the space of parameters that we will explore
     */
    public static ArrayList<int[]> parameterSpace() {
        ArrayList<int[]> allParams = new ArrayList<>(ratios1.length * ratios2.length * ratios3.length * ratios4.length);
//        add all possible parameters to arraylist
        for (int i = 0; i < ratios1.length; i++) {
            for (int j = 0; j < ratios2.length; j++) {
                for (int k = 0; k < ratios3.length; k++) {
                    for (int l = 0; l < ratios4.length; l++) {
                        if (i == 1 && j == 2 && k == 0 && l == 4) continue;
                        allParams.add(getWeightsFromRatios(i, j, k, l));
                    }
                }
            }
        }
//        shuffle to see if there is a better result
        Collections.shuffle(allParams);
        return allParams;
    }

    /**
     * Go iteratively through all parameters to find the best parameter
     * If white wins we iterate through parameters and try to beat white with that parameter and vice versa
     * we count the number of wins before losing for each parameter set.
     */
    public static void runSimulations() {
        ArrayList<int[]> allParams = parameterSpace();
        Random rand = new Random();
        int bStrat = 0;
        int wStrat = 0;
        while (true) {
            MyTools.loadWeights(FastBoard.WHITE, allParams.get(wStrat));
            MyTools.loadWeights(FastBoard.BLACK, allParams.get(bStrat));
            int winner = simulGame(new FastBoard());
            if (winner == FastBoard.EMPTY) {
//                tied game just randomly choose a winner
                winner = rand.nextInt(2);
            }
            if (winner == FastBoard.BLACK) {
//                if Black wins
                boolean success = false;
                int wins = 0;
                while (wStrat < allParams.size() - 1) {
                    wStrat++;
                    MyTools.loadWeights(FastBoard.WHITE, allParams.get(wStrat));
                    if (simulGame(new FastBoard()) == FastBoard.WHITE) {
                        int[] update = allParams.get(wStrat);
                        System.out.println("\nWhite wins; Black won " + wins + " games.");
                        System.out.println("New weights: {" + update[0] + "," + update[1] + ","
                                + update[2] + "," + update[3] + "," + update[4] + "," + update[5] + "}");
                        success = true;
                        break;
                    } else {
                        wins++;
                        System.out.print("#");
                    }
                }
                if (!success) {
                    int[] update = allParams.get(bStrat);
                    System.out.print("\nWhite could not win against ");
                    System.out.println("weights: {" + update[0] + "," + update[1] + ","
                            + update[2] + "," + update[3] + "," + update[4] + "," + update[5] + "}");
                    break;
                }
            }
            if (winner == FastBoard.WHITE) {
//                if White wins
                boolean success = false;
                int wins = 0;
                while (bStrat < allParams.size() - 1) {
                    bStrat++;
                    MyTools.loadWeights(FastBoard.BLACK, allParams.get(bStrat));
                    if (simulGame(new FastBoard()) == FastBoard.BLACK) {
                        int[] update = allParams.get(bStrat);
                        System.out.println("\nBlack wins; White won " + wins + " games.");
                        System.out.println("New weights: {" + update[0] + "," + update[1] + ","
                                + update[2] + "," + update[3] + "," + update[4] + "," + update[5] + "}");
                        success = true;
                        break;
                    } else {
                        wins++;
                        System.out.print("#");
                    }
                }
                if (!success) {
                    int[] update = allParams.get(wStrat);
                    System.out.print("\nBlack could not win against ");
                    System.out.println("weights: {" + update[0] + "," + update[1] + ","
                            + update[2] + "," + update[3] + "," + update[4] + "," + update[5] + "}");
                    break;
                }
            }
        }
    }

    /**
     * We take a parameter for a side (piece stands for white or black) and then take in a parameter
     * setup then make it play against every single parameter in the set, counting how many games it loses
     * then return the number of games lost.
     */
    public static void testWeights(int piece, int[] params) {
        ArrayList<int[]> allParams = parameterSpace();
//        hard coded weights
        MyTools.loadWeights(piece, params);
        int counter = 0;
        for (int[] arrays :
                allParams) {
            MyTools.loadWeights(1 - piece, arrays);
            if (simulGame(new FastBoard()) == 1 - piece) {
                counter++;
                System.out.println("Beaten by {" + arrays[0] + "," + arrays[1] + ","
                        + arrays[2] + "," + arrays[3] + "," + arrays[4] + "," + arrays[5] + "}");
            }
        }
        System.out.println("Number of counters:" + counter);
    }

    public static int[] getWeightsFromRatios(int[] weights) {
        //           int[] newWeights = new int[6];
        //           newWeights[0] = 0;
        //           newWeights[1] = ratios[0];
        //           newWeights[2] = Integer.max(newWeights[1],1) * ratios[1];
        //           newWeights[3] = Integer.max(newWeights[2],10) * ratios[2];
        //           newWeights[4] = Integer.max(newWeights[3],10) * ratios[3];
        //           newWeights[5] = 0;
        return getWeightsFromRatios(weights[0], weights[1], weights[2], weights[3]);
    }

    public static int[] getWeightsFromRatios(int weight1, int weight2, int weight3, int weight4) {
        int ratio1 = ratios1[weight1];
        int ratio2 = ratios2[weight2];
        int ratio3 = ratios3[weight3];
        int ratio4 = ratios4[weight4];
        int[] newWeights = new int[6];
        newWeights[0] = 0;
        newWeights[1] = ratio1;
        newWeights[2] = Integer.max(newWeights[1], 1) * ratio2;
        newWeights[3] = Integer.max(newWeights[2], 5) * ratio3;
        newWeights[4] = Integer.max(newWeights[3], 10) * ratio4;
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
        int whiteWins = 0;
        int whiteDWins = 0;
//        run X games
        for (int i = 0; i < 200; i++) {
            boolean dwin = false;
            FastBoard fastBoard = new FastBoard();
            fastBoard.turnNumber = 2;
            PentagoCoord[] array = new PentagoCoord[4];
//            place randomly 4 stones
            for (int j = 0; j < 4; j++) {
                int x = rand.nextInt(PentagoBoardState.BOARD_SIZE);
                int y = rand.nextInt(PentagoBoardState.BOARD_SIZE);
                if (fastBoard.board[x][y] == FastBoard.EMPTY) {
                    array[j] = new PentagoCoord(x, y);
                    if (j < 2) {
                        fastBoard.board[x][y] = FastBoard.WHITE;
                    } else {
                        fastBoard.board[x][y] = FastBoard.BLACK;
                    }
                } else {
                    j--;
                }
            }
            int outcome = simulGame(fastBoard);
            for (int j = 0; j < 4; j++) {
                plays[array[j].getX()][array[j].getY()]++;
            }
            if (outcome == FastBoard.WHITE) {
                dwin = true;
                whiteWins++;
                for (int j = 0; j < 2; j++) {
                    scores[array[j].getX()][array[j].getY()] += 2;
                }
            } else if (outcome == FastBoard.BLACK) {
                for (int j = 2; j < 4; j++) {
                    scores[array[j].getX()][array[j].getY()] += 2;
                }
            } else {
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
                whiteWins++;
                if (dwin) {
                    whiteDWins++;
                }
                for (int j = 0; j < 2; j++) {
                    scores[array[j].getX()][array[j].getY()] += 2;
                }
            } else if (outcome == FastBoard.BLACK) {
                for (int j = 2; j < 4; j++) {
                    scores[array[j].getX()][array[j].getY()] += 2;
                }
            } else {
                for (int j = 0; j < 4; j++) {
                    scores[array[j].getX()][array[j].getY()] += 1;
                }
            }
            System.out.println("Iteration: " + i);
        }
        System.out.println("White won:" + whiteWins + " and won both sides:" + whiteDWins);
        try {
            FileWriter myWriter = new FileWriter("data/scores2.csv");
            for (int i = 0; i < 36; i++) {
                myWriter.write(i / 6 + "," + i % 6 + "," + scores[i / 6][i % 6] + "," + plays[i / 6][i % 6] + "\n");
            }
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * game simulation where we use chooseMove from studentplayer
     * it returns the winner as an integer 0 for white, 1 for black and 2 for draw.
     */
    static int simulGame(FastBoard board) {
        board.evaluate(FastBoard.WHITE);
        StudentPlayer player1 = new StudentPlayer();
//        StudentPlayer player2 = new StudentPlayer();
        while (!board.getGameOver()) {
            if (board.getAllLegalMoves().size() != 0) {
                board.doMove(player1.alphaBetaWrapper(board));
            } else {
                board.gameOver = true;
            }
            board.evaluate(FastBoard.WHITE);
        }
        if (board.evaluate(FastBoard.WHITE) > 0) {
            return FastBoard.WHITE;
        } else if (board.evaluate(FastBoard.WHITE) < 0) {
            return FastBoard.BLACK;
        } else {
            return FastBoard.EMPTY;
        }
    }

//region  generating depth 6 evaluations

    /**
     * We attempt to evaluate every possible board state up to symmetry and write it to a file
     */
    static void generateDepthGames(HashSet<Long> set) {
        StudentPlayer player = new StudentPlayer();
        long counter = 0;
        try {
            FileWriter fw = new FileWriter("data/EVAL6.TXT", true);
            BufferedWriter bw = new BufferedWriter(fw);
            FileWriter fw2 = new FileWriter("data/BOARDS6.TXT", true);
            BufferedWriter bw2 = new BufferedWriter(fw2);
            FastBoard board = new FastBoard();
            long dummy;
            for (int i = 0; i < 36; i++) {
                for (int j = i + 1; j < 36; j++) {
                    for (int k = j + 1; k < 36; k++) {
                        for (int l = k + 1; l < 36; l++) {
                            for (int m = l + 1; m < 36; m++) {
                                for (int n = m + 1; n < 36; n++) {
//                                turn it into an array for ease of access
                                    int[] array = new int[]{i, j, k, l, m, n};
                                    for (int integer :
                                            array) {
                                        board.board[integer / 6][integer % 6] = FastBoard.WHITE;
                                    }
                                    dummy = board.getTag();
                                    if (set.add(dummy)) {
                                        bw2.write("" + dummy + "\n");
                                        board.rotate180();
                                        dummy = board.getTag();
                                        set.add(board.getTag());
                                        bw2.write("" + dummy + "\n");
                                        evaluateBoards(array, bw, player);
                                        counter++;
                                        System.out.println(counter);
                                    }
//                                    reset Fastboard since we hate java garbage collector
                                    for (int i2 = 0; i2 < 6; i2++) {
                                        for (int j2 = 0; j2 < 6; j2++) {
                                            board.board[i2][j2] = FastBoard.EMPTY;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            bw.close();
            bw2.close();
            fw2.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void evaluateBoards(int[] selection, BufferedWriter bw, StudentPlayer player) throws IOException {
        FastBoard board = new FastBoard();
        for (int o = 0; o < 6; o++) {
            for (int p = o + 1; p < 6; p++) {
                for (int q = p + 1; q < 6; q++) {
                    for (int i :
                            selection) {
                        board.board[i / 6][i % 6] = FastBoard.WHITE;
                    }
                    board.board[selection[o] / 6][selection[o] % 6] = FastBoard.BLACK;
                    board.board[selection[p] / 6][selection[p] % 6] = FastBoard.BLACK;
                    board.board[selection[q] / 6][selection[q] % 6] = FastBoard.BLACK;
                    board.evaluated = false;
//                    int score = board.deepEvaluate(FastBoard.WHITE);
                    int score = player.alphaBeta(board, 1, Integer.MIN_VALUE, Integer.MAX_VALUE, FastBoard.WHITE, true);
                    String s = "";
                    for (int i = 0; i < 6; i++) {
                        if (i != o && i != p && i != q) {
                            s += selection[i] + ",";
                        }
                    }
                    s += selection[o] + "," + selection[p] + "," + selection[q] + "," + score + "\n";
                    bw.write(s);
//                    reset Fastboard since we hate java garbage collector
                    for (int i = 0; i < 6; i++) {
                        for (int j = 0; j < 6; j++) {
                            board.board[i][j] = FastBoard.EMPTY;
                        }
                    }
                }
            }
        }
    }
// endregion

    /**
     * build the min max tree backwards from higher depth to lower depth reducing redundancy.
     */
    static void retrograde() {
//        we first generate a Hashmap of every possible state after 5 moves to a string
//        we also make a hashmap for the scores
//        we can also make a hashset for the possible states up to symmetry to save time and space
        HashMap<Long, String> identifierMap = new HashMap<>(500000);
        HashMap<Long, Integer> scoreMap = new HashMap<>(500000);
        HashSet<Long> symmetrySet = new HashSet<>(500000);
        try {
            FileReader fr = new FileReader("data/EVAL6.TXT");
            BufferedReader br = new BufferedReader(fr);
            String line;
            int[] coordinates = new int[6];
            int score;
            FastBoard board = new FastBoard();
            board.turnPlayer = FastBoard.BLACK;
            long tag;
            int wcounter;
            int bcounter;
            int[] newCoordinates = new int[5];
            while ((line = br.readLine()) != null) {
//                parse the String
                String[] sarray = line.split(",");
                score = Integer.parseInt(sarray[6]);
                for (int i = 0; i < 6; i++) {
                    coordinates[i] = Integer.parseInt(sarray[i]);
                }
//                the first 3 coordinates are white pieces and the second 3 are the black pieces
//                place 6 pieces on the board
                board.board[coordinates[0] / 6][coordinates[0] % 6] = FastBoard.WHITE;
                board.board[coordinates[1] / 6][coordinates[1] % 6] = FastBoard.WHITE;
                board.board[coordinates[2] / 6][coordinates[2] % 6] = FastBoard.WHITE;
                board.board[coordinates[3] / 6][coordinates[3] % 6] = FastBoard.BLACK;
                board.board[coordinates[4] / 6][coordinates[4] % 6] = FastBoard.BLACK;
                board.board[coordinates[5] / 6][coordinates[5] % 6] = FastBoard.BLACK;
//                get all 8 possible transformations of the board
                for (int i = 0; i < 8; i++) {
                    board.untwistQuadrant(i % 4, i / 4);
//                    we then search for a black piece to remove
                    for (int j = 0; j < 36; j++) {
                        if (board.board[j / 6][j % 6] == FastBoard.BLACK) {
//                            we found a black piece now we remove it, check if we already had this position recorded
//                            if not create the new position, if it was already created we update the minimum
                            board.board[j / 6][j % 6] = FastBoard.EMPTY;
                            tag = board.getTag();
                            if (symmetrySet.add(tag)) {
//                                it is a new position
//                                record the positions of the pieces
                                wcounter = 0;
                                bcounter = 3;
                                for (int k = 0; k < 36; k++) {
                                    if (board.board[k / 6][k % 6] == FastBoard.WHITE) {
                                        newCoordinates[wcounter] = k;
                                        wcounter++;
                                    }
                                    else if (board.board[k / 6][k % 6] == FastBoard.BLACK) {
                                        newCoordinates[bcounter] = k;
                                        bcounter++;
                                    }
                                }
//                                we now associate the identifier string to the tag (first three are white positions)
                                identifierMap.put(tag,"" + newCoordinates[0]+","+ newCoordinates[1]+","+
                                        newCoordinates[2]+","+ newCoordinates[3]+","+ newCoordinates[4]+",");
//                                create a score
                                scoreMap.put(tag,score);
                                board.rotate180();
//                                add its rotation to the set of explored tags
                                symmetrySet.add(board.getTag());
                            }
                            else {

                            }
//                            place the board back
                            board.board[j / 6][j % 6] = FastBoard.BLACK;
                        }
                    }
//                    twist the board back
                    board.twistQuadrant(i % 4, i / 4);
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}