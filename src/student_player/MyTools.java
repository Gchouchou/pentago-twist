package student_player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoCoord;
import pentago_twist.PentagoMove;

public class MyTools {

    //   region <Eval File Loading>
    private static final String SIMPLETXT = "SIMPLE.txt";
    private static final Integer FILELENGTH = 32;
    private static int[][] evalWeights;
    private static final String LINEARCSV = "Linear.csv";

    private static boolean loaded = false;

    public static ArrayList<int[][]> template;
    public static int[][] linearWeights;
    //  method to read string and create the set of 5 coordinates.
    private static int[][] stringToComb(String s) {
        int[][] a = new int[5][2];
        char[] parser = s.toCharArray();
        a[0][0] = parser[0] - '0';
        a[0][1] = parser[1] - '0';
        a[1][0] = parser[2] - '0';
        a[1][1] = parser[3] - '0';
        a[2][0] = parser[4] - '0';
        a[2][1] = parser[5] - '0';
        a[3][0] = parser[6] - '0';
        a[3][1] = parser[7] - '0';
        a[4][0] = parser[8] - '0';
        a[4][1] = parser[9] - '0';
        return a;
    }


    public static boolean checkLoaded() {
        return loaded;
    }

    public static void loadFile() {
        template = new ArrayList<>(FILELENGTH);
        loaded = true;
        try {
            FileReader fr = new FileReader("data/" + SIMPLETXT);
            BufferedReader br = new BufferedReader(fr);
            String str;
            while ((str = br.readLine()) != null) {
                template.add(stringToComb(str));
            }
            br.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        evalWeights = new int[2][];
        evalWeights[FastBoard.WHITE] = new int[]{0,0,0,47,997,0};
        evalWeights[FastBoard.BLACK] = new int[]{0,1,10,47,997,0};

        linearWeights = new int[3][];
        try {
            FileReader fr = new FileReader("data/" + LINEARCSV);
            BufferedReader br = new BufferedReader(fr);
            String str;
            for (int i = 0; i < 3; i++) {
                str = br.readLine();
                linearWeights[i] = strToArray(str);
            }
            br.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int[] strToArray(String string) {
        String[] arr = string.split(",");
        assert (arr.length == 6);
        int[] array = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            array[i] = Integer.parseInt(arr[i]);
        }
        return array;
    }

//    change evaluation function weights for a particular color
    public static void loadWeights(int piece, int[] arr) {
        evalWeights[piece] = arr;
    }
// endregion

//    evaluation function based on how many rocks we have in a 5 block win
    public static int evalParams(int count, int piece) {
        return evalWeights[piece][count];
    }

    //	region <Legal move Filtering>
//	get legal moves up to symmetry
    public static ArrayList<PentagoMove> getLegalMoves(FastBoard boardState, int piece, boolean isMax) {
//        if (boardState.getTurnNumber() < 2) {
//        }
//        return boardState.getAllLegalMoves();
        ArrayList<PentagoMove> list =  getLegalMovesSymmetry(boardState);
//        test every transform and see which one improves our score
        if (isMax) piece = 1 - piece;
        int[][] array = new int[4][2];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                boardState.twistQuadrant(i,j);
                array[i][j] = boardState.evaluate(piece);
                boardState.untwistQuadrant(i,j);
            }
        }
//        we then sort moves by their transforms to hopefully get better moves first
        Collections.sort(list, new moveComparator(array));
//        Collections.shuffle(list);
        return list;
    }

    public static class moveComparator implements Comparator<PentagoMove> {
        int[][] saves;
        public moveComparator(int[][] array) {
            saves = array;
        }
        public int compare(PentagoMove m1, PentagoMove m2) {
            return ((Integer) saves[m1.getASwap()][m1.getBSwap()]).compareTo(
                    saves[m2.getASwap()][m2.getBSwap()]);
        }
    }

    public static ArrayList<PentagoMove> getLegalMovesSymmetry(FastBoard boardState) {
        ArrayList<PentagoMove> moves = boardState.getAllLegalMoves();
        ArrayList<PentagoMove> nonDupeMoves = new ArrayList<>(moves.size());
        HashSet<Long> positions = new HashSet<>(600);
        for (PentagoMove m : moves) {
//            PentagoBoardState successor = (PentagoBoardState) boardState.clone();
//            successor.processMove(m);
//            int[][] mat = boardConvert(successor);
            boardState.doMove(m);
            if (positions.add(boardState.getTag())) {
//				new position
                nonDupeMoves.add(m);
//                no point is searching for symmetries in position that are usually not symmetric
                if (boardState.getTurnNumber() < 3) {
    //				rotate 180
                    boardState.rotate180();
                    positions.add(boardState.getTag());
                    boardState.rotate180();
                }
            }
            boardState.undoMove(m);
        }
        return nonDupeMoves;
    }

    //	rotating a board
    public static void rotate180(int[][] mat) {
//        take all squares above half way
        for (int x = 0; x < PentagoBoardState.BOARD_SIZE / 2; x++) {
            for (int y = 0; y < PentagoBoardState.BOARD_SIZE; y++) {
                int temp = mat[x][y];
                mat[x][y] = mat[PentagoBoardState.BOARD_SIZE - x - 1][PentagoBoardState.BOARD_SIZE - y - 1];
                mat[PentagoBoardState.BOARD_SIZE - x - 1][PentagoBoardState.BOARD_SIZE - y - 1] = temp;
            }
        }
    }

//	Getting Unique Tag of Matrix
    public static long boardTag(int[][] mat) {
        long index = 0;
        for (int x = 0; x < PentagoBoardState.BOARD_SIZE; x++) {
            for (int y = 0; y < PentagoBoardState.BOARD_SIZE; y++) {
                index += mat[x][y];
//			    mod 3 so we have to go up by 3 and we can barely fit inside the space
                index *= 3;
            }
        }
        return index;
    }

    //	Converting Board to Matrix
    private static int[][] boardConvert(PentagoBoardState boardState) {
        int[][] mat = new int[PentagoBoardState.BOARD_SIZE][PentagoBoardState.BOARD_SIZE];
        for (int x = 0; x < PentagoBoardState.BOARD_SIZE; x++) {
            for (int y = 0; y < PentagoBoardState.BOARD_SIZE; y++) {
                mat[x][y] = convertPiece(boardState.getPieceAt(x, y));
            }
        }
        return mat;
    }

    //	converting pieces to integers
    public static int convertPiece(PentagoBoardState.Piece p) {
        switch (p) {
            case BLACK:
                return FastBoard.BLACK;
            case WHITE:
                return FastBoard.WHITE;
            default:
                return FastBoard.EMPTY;
        }
    }
//	endregion

    //	faster board implementation and allows reversing moves
    public static class FastBoard {
        public int[][] board;
        boolean evaluated;
        int score;
        boolean gameOver;
        public int turnPlayer;
        public int turnNumber;

        public static final int WHITE = 0;
        public static final int BLACK = 1;
        public static final int EMPTY = 3;

        //    copy a PentagoBoardState
        public FastBoard(PentagoBoardState boardState) {
            board = new int[PentagoBoardState.BOARD_SIZE][PentagoBoardState.BOARD_SIZE];
            for (int x = 0; x < PentagoBoardState.BOARD_SIZE; x++) {
                for (int y = 0; y < PentagoBoardState.BOARD_SIZE; y++) {
                    board[x][y] = MyTools.convertPiece(boardState.getPieceAt(x, y));
                }
            }
            evaluated = false;
            gameOver = false;
            turnPlayer = boardState.getTurnPlayer();
            turnNumber = boardState.getTurnNumber();
        }

        //    create a new board
        public FastBoard() {
            board = new int[PentagoBoardState.BOARD_SIZE][PentagoBoardState.BOARD_SIZE];
            for (int x = 0; x < PentagoBoardState.BOARD_SIZE; x++) {
                for (int y = 0; y < PentagoBoardState.BOARD_SIZE; y++) {
                    board[x][y] = EMPTY;
                }
            }
            evaluated = false;
            gameOver = false;
            turnPlayer = 0;
            turnNumber = 0;
        }

        public boolean getGameOver() {
            return gameOver;
        }

        public int getTurnNumber() {
            return turnNumber;
        }

        //    Get all legal moves no brain
        public ArrayList<PentagoMove> getAllLegalMoves() {
            ArrayList<PentagoMove> moves = new ArrayList<>();
            for (int x = 0; x < PentagoBoardState.BOARD_SIZE; x++) {
                for (int y = 0; y < PentagoBoardState.BOARD_SIZE; y++) {
                    if (board[x][y] != EMPTY) {
                        continue;
                    }
                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 2; j++) {
                            moves.add(new PentagoMove(x, y, i, j, turnPlayer));
                        }
                    }
                }
            }
            return moves;
        }

        public int evaluate(int piece) {
            if (evaluated) {
                return score;
            }
            evaluated = true;

            score = 0;
            boolean win = false;
            boolean otherWin = false;

            for (int[][] wins : MyTools.template) {
                int count = 0;
                int thing = EMPTY;
                for (int i = 0; i < 5; i++) {
//				get what piece is at the location
                    int p = board[wins[i][0]][wins[i][1]];
//                skip when we see nothing
                    if (p == EMPTY) {
                        continue;
                    }
//                update if it is the first piece we saw
                    if (thing == EMPTY) {
                        thing = p;
                        count++;
                    } else if (p != thing) {
//                    different colored piece we stop counting
                        count = 0;
                        break;
                    } else {
//                    same colored piece we continue
                        count++;
                    }
                }
//			scoring algo
//            check what color were the pieces
                int sign = 0;
                if (thing == piece) {
                    sign = 1;
                    if (count == 5) {
                        win = true;
                    }
                } else if (thing != EMPTY) {
                    sign = -1;
                    if (count == 5) {
                        otherWin = true;
                    }
                }
//				it's a draw just break
                if (win && otherWin) {
                    this.score = 0;
                    gameOver = true;
                    return this.score;
                }
                score += sign * MyTools.evalParams(count,piece);
            }

            if (win) {
                score = Integer.MAX_VALUE;
                gameOver = true;
                return score;
            }
            if (otherWin) {
                score = Integer.MIN_VALUE;
                gameOver = true;
                return score;
            }
            if (gameOver || turnNumber >= 18) {
                score = 0; gameOver = true; return 0;
            }
            return score;
        }

        //    a more expensive evaluation function that hopefully distinguishes more moves
        public int deepEvaluate(int piece) {
            evaluate(piece);
            if (gameOver|| piece == BLACK) {return score;}
            int sum = 0;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < PentagoBoardState.BOARD_SIZE; j++) {
                    int x = i;
                    int y = j;
                    if (board[x][y] == piece) {
                        sum += MyTools.linearWeights[i][j];
                    } else if (board[x][y] == 1 - piece) {
                        sum -= MyTools.linearWeights[i][j];
                    }
                    x = PentagoBoardState.BOARD_SIZE - x - 1;
                    y = PentagoBoardState.BOARD_SIZE - y - 1;
                    if (board[x][y] == piece) {
                        sum += MyTools.linearWeights[i][j];
                    } else if (board[x][y] == 1 - piece) {
                        sum -= MyTools.linearWeights[i][j];
                    }
                }
            }
            return score+sum;
        }

        //    region Board Manipulation
        public void doMove(PentagoMove move) {
            evaluated = false;
            assert (!gameOver);
            assert (board[move.getMoveCoord().getX()][move.getMoveCoord().getY()] == EMPTY);
            assert (move.getPlayerID() == turnPlayer);
            PentagoCoord coord = move.getMoveCoord();
            board[coord.getX()][coord.getY()] = turnPlayer;
            twistQuadrant(move.getASwap(), move.getBSwap());
            if (turnPlayer != 0) {
                turnNumber++;
            }
            turnPlayer = 1 - turnPlayer;
        }

        public void undoMove(PentagoMove move) {
            untwistQuadrant(move.getASwap(), move.getBSwap());
            PentagoCoord coord = move.getMoveCoord();
            board[coord.getX()][coord.getY()] = EMPTY;
            if (turnPlayer == 0) {
                turnNumber--;
            }
            turnPlayer = 1 - turnPlayer;
            evaluated = false;
            gameOver = false;
        }

        //		perform rotation
        public void twistQuadrant(int quadrant, int twistType) {
            if (twistType == 0)
                rotateQuadrantRight(quadrant);
            else
                flipQuadrant(quadrant);
            evaluated = false;
        }

        //		rotation the other way
        public void untwistQuadrant(int quadrant, int twistType) {
            if (twistType == 0) {
                rotateQuadrantLeft(quadrant);
            } else {
                flipQuadrant(quadrant);
            }
            evaluated = false;
        }

        private void rotateQuadrantLeft(int quadrant) {
//        center starting point
            int x = 3 * (quadrant / 2);
            int y = 3 * (quadrant % 2);
            int temp = board[x][y];
            board[x][y] = board[x][y + 2];
            board[x][y + 2] = board[x + 2][y + 2];
            board[x + 2][y + 2] = board[x + 2][y];
            board[x + 2][y] = temp;
            temp = board[x + 1][y];
            board[x + 1][y] = board[x][y + 1];
            board[x][y + 1] = board[x + 1][y + 2];
            board[x + 1][y + 2] = board[x + 2][y + 1];
            board[x + 2][y + 1] = temp;
        }

        private void rotateQuadrantRight(int quadrant) {
            int x = 3 * (quadrant / 2);
            int y = 3 * (quadrant % 2);
            int temp = board[x][y];
            board[x][y] = board[x + 2][y];
            board[x + 2][y] = board[x + 2][y + 2];
            board[x + 2][y + 2] = board[x][y + 2];
            board[x][y + 2] = temp;
            temp = board[x + 1][y];
            board[x + 1][y] = board[x + 2][y + 1];
            board[x + 2][y + 1] = board[x + 1][y + 2];
            board[x + 1][y + 2] = board[x][y + 1];
            board[x][y + 1] = temp;
        }

        private void flipQuadrant(int quadrant) {
            int x = 3 * (quadrant / 2);
            int y = 3 * (quadrant % 2);
            for (int i = 0; i < 3; i++) {
                int temp = board[x + i][y];
                board[x + i][y] = board[x + i][y + 2];
                board[x + i][y + 2] = temp;
            }
        }

        public long getTag() {
            return MyTools.boardTag(board);
        }

        public void rotate180() {
            MyTools.rotate180(board);
        }

        public int getTurnPlayer() {
            return turnPlayer;
        }
// #endregion

    }
}