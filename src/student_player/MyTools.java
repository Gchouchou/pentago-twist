package student_player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

import boardgame.Board;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoBoardState.Piece;
import pentago_twist.PentagoCoord;
import pentago_twist.PentagoMove;

public class MyTools {
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    public static final int EMPTY = 3;

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

    private static ArrayList<int[][]> template;

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
    private static int evalParams(int count) {
        switch (count) {
            case 1:
                return 1;
            case 2:
                return 5;
            case 3:
                return 50;
            case 4:
                return 1000;
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
    public static ArrayList<PentagoMove> getLegalMoves(FastBoard boardState) {
//        if (boardState.getTurnNumber() < 2) {
//            return getLegalMovesSymmetry(boardState);
//        }
        return boardState.getAllLegalMoves();
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
//				rotate 180
                boardState.rotate180();
                positions.add(boardState.getTag());
                boardState.rotate180();
            }
            boardState.undoMove(m);
        }
        return nonDupeMoves;
    }

    //	rotating a board
    private static void rotate180(int mat[][]) {
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
    private static long boardTag(int mat[][]) {
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
                return BLACK;
            case WHITE:
                return WHITE;
            default:
                return EMPTY;
        }
    }
//	endregion

    //	faster board implementation and allows reversing moves
    public static class FastBoard {
        int[][] board;
        boolean evaluated;
        int score;
        boolean gameOver;
        int turnPlayer;
        int turnNumber;

        public FastBoard(PentagoBoardState boardState) {
            board = new int[PentagoBoardState.BOARD_SIZE][PentagoBoardState.BOARD_SIZE];
            for (int x = 0; x < PentagoBoardState.BOARD_SIZE; x++) {
                for (int y = 0; y < PentagoBoardState.BOARD_SIZE; y++) {
                    board[x][y] = convertPiece(boardState.getPieceAt(x, y));
                }
            }
            evaluated = false;
            gameOver = false;
            turnPlayer = boardState.getTurnPlayer();
            turnNumber = boardState.getTurnNumber();
        }

        public boolean getGameOver() {
            return gameOver;
        }

        public int getTurnNumber() {
            return turnNumber;
        }

        public ArrayList<PentagoMove> getAllLegalMoves() {
            ArrayList<PentagoMove> moves = new ArrayList<>();
            for (int x = 0; x < PentagoBoardState.BOARD_SIZE; x++) {
                for (int y = 0; y < PentagoBoardState.BOARD_SIZE; y++) {
                    if (board[x][y] != EMPTY) {continue;}
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

            for (int[][] wins : template) {
                int count = 0;
                int thing = EMPTY;
                for (int i = 0; i < 5; i++) {
//				get what piece is at the location
                    int p = board[wins[i][0]][wins[i][1]];
                    if (p == EMPTY) {
                        continue;
                    }
                    if (thing == EMPTY) {
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
                score += sign * evalParams(count);
            }

            if (win) {
                score = Integer.MAX_VALUE;
                gameOver = true;
            }
            if (otherWin) {
                score = Integer.MIN_VALUE;
                gameOver = true;
            }
            return score;
        }

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
            turnPlayer =  1 - turnPlayer ;
        }

        public void undoMove(PentagoMove move) {
            unTwistQuadrant(move.getASwap(), move.getBSwap());
            PentagoCoord coord = move.getMoveCoord();
            board[coord.getX()][coord.getY()] = EMPTY;
            if (turnPlayer == 0) {
                turnNumber--;
            }
            turnPlayer = 1 - turnPlayer ;
            evaluated = false;
            gameOver = false;
        }

        //		perform rotation
        public void unTwistQuadrant(int quadrant, int twistType) {
            switch (twistType) {
                case 0:
                    rotateQuadrantLeft(quadrant);
                    break;
                default:
                    flipQuadrant(quadrant);
                    break;
            }
            evaluated = false;
        }

        //		perform rotation
        public void twistQuadrant(int quadrant, int twistType) {
            switch (twistType) {
                case 0:
                    rotateQuadrantRight(quadrant);
                    break;
                default:
                    flipQuadrant(quadrant);
                    break;
            }
            evaluated = false;
        }

        private void rotateQuadrantLeft(int quadrant) {
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
                int temp = board[x][y + i];
                board[x][y + i] = board[x + 2][y + i];
                board[x + 2][y + i] = temp;
            }
        }

        private long getTag() {
            return boardTag(board);
        }

        private void rotate180() {
            MyTools.rotate180(board);
        }
    }

}