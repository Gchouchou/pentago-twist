package student_player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import boardgame.Board;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoBoardState.Piece;
import pentago_twist.PentagoMove;

public class MyTools {

    //   region <Eval File Loading>
    private static final String FILENAME = "SIMPLE.txt";
    private static final Integer FILELENGTH = 32;

    private static boolean loaded = false;

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

    public static ArrayList<int[][]> template;

    public static boolean checkLoaded() {
        return loaded;
    }

    public static void loadFile() {
        template = new ArrayList<>(FILELENGTH);
        loaded = true;
        try {
            FileReader fr = new FileReader("data/" + FILENAME);
            //Now how to read the file and parse it
            //create file reader and buffered reader
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
    }
// endregion

//    region <Evaluation Function>
//    evaluation function based on how many rocks we have in a 5 block win
    public static int evalParams(int count) {
        switch (count) {
            case 1:
                return 1;
            case 2:
                return 5;
            case 3:
                return 47;
            case 4:
                return 997;
            case 5:
//			not always needed
                return 100000;
            default:
                return 0;
        }

    }
    // check all win cons and count how many consecutive pieces we have
    public static int evaluate(PentagoBoardState boardState, Piece piece) {
//    	check if the game is over
        if (boardState.gameOver()) {
            int win = boardState.getWinner();
            Piece winner;
            if (win == Board.DRAW) {
                return 0;
            } else if (win == PentagoBoardState.WHITE) {
                winner = Piece.WHITE;
            } else {
                winner = Piece.BLACK;
            }
            if (winner == piece) {
                return Integer.MAX_VALUE;
            } else {
                return Integer.MIN_VALUE;
            }
        }

        int sum = 0;

        for (int[][] wins : template) {
            int count = 0;
            Piece thing = Piece.EMPTY;
            for (int i = 0; i < 5; i++) {
//				get what piece is at the location
                Piece p = boardState.getPieceAt(wins[i][0], wins[i][1]);
                if (p == Piece.EMPTY) {
                    continue;
                }
                if (thing == Piece.EMPTY) {
                    thing = p;
                    count++;
                } else if (p != thing) {
                    count = 0;
                    break;
                } else {
                    count++;
                }
            }
//			scoring algo
            int sign = 0;
            if (thing == piece) {
                sign = 1;
            } else if (thing != Piece.EMPTY) {
                sign = -1;
            }
            sum += sign * evalParams(count);
        }

        return sum;
    }
// endregion

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

    //	flipping a board
//	private static void flipMatrix(int mat[][]) {
//		for (int x = 0; x < PentagoBoardState.BOARD_SIZE / 2; x++) {
//			for (int y = 0; y < PentagoBoardState.BOARD_SIZE; y++) {
//				int temp = mat[x][y];
//				mat[x][y] = mat[x+PentagoBoardState.BOARD_SIZE / 2][y];
//				mat[x+PentagoBoardState.BOARD_SIZE / 2][y] = temp;
//			}
//		}
//	}
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

}