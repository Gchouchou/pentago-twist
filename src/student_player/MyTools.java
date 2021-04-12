package student_player;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoCoord;
import pentago_twist.PentagoMove;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class MyTools {

    //   region <Eval File Loading>
    private static final String SIMPLETXT = "SIMPLE.txt";
    private static final Integer FILELENGTH = 32;
    private static final String COMPLEXTXT = "COMPLEX.txt";
    private static final Integer COMPLEXLENGTH = 96;

    private static int[][] evalWeights;
    private static boolean loaded = false;

    public static ArrayList<int[][]> template;
    public static ArrayList<int[][]> template2;


    public static boolean checkLoaded() {
        return loaded;
    }

    /**
     * Loads evaluation weights
     * loads the set of five in a row coordinates
     * also loads the set of indirect five in a row coordinates
     */
    public static void loadFile() {
        template = new ArrayList<>(FILELENGTH);
        template2 = new ArrayList<>(COMPLEXLENGTH);
        loaded = true;
        LoadStrings(SIMPLETXT, template);
        LoadStrings(COMPLEXTXT, template2);
        evalWeights = new int[2][];
        evalWeights[FastBoard.WHITE] = new int[]{0,1,2,10,100,0};
        evalWeights[FastBoard.BLACK] = evalWeights[FastBoard.WHITE];

    }

    /**
     * Takes file name an target array list and reads every string of set of five coordinates
     * and puts it into the array list
     */
    private static void LoadStrings(String fileName, ArrayList<int[][]> targetList) {
        try {
            FileReader fr = new FileReader("data/" + fileName);
            BufferedReader br = new BufferedReader(fr);
            String str;
            while ((str = br.readLine()) != null) {
                targetList.add(stringToComb(str));
            }
            br.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  read a string of length 10
     *  in the form (x1y1x2y2...)
     *  and create an array of 5 coordinates.
     */
    private static int[][] stringToComb(String s) {
        int[][] a = new int[5][2];
        char[] parser = s.toCharArray();
        for (int i = 0; i < 10; i++) {
            a[i / 2][i % 2] = parser[i] - '0';
        }
        return a;
    }
// endregion

    //    evaluation function based on how many rocks we have in a 5 block win
    public static int evalParams(int count, int piece) {
        return evalWeights[piece][count];
    }

    //	region <Legal move Filtering>

    /**
     * Get all legal moves from ths board position
     * evaluated by white or black (piece) (the one starting the alpha beta pruning)
     */
    public static ArrayList<PentagoMove> getLegalMoves(FastBoard boardState, int piece) {
//        Get all moves/score pair up to symmetry
        ArrayList<MoveScorePair> list = getLegalMovesSymmetry(boardState, piece);
//        we then sort moves by their depth 1 evaluations for better performance
        Collections.sort(list);
        if (boardState.getTurnPlayer() == piece) {
            Collections.reverse(list);
        }

//        Take only the moves out
        ArrayList<PentagoMove> list1 = new ArrayList<>(list.size());
        for (MoveScorePair pair :
                list) {
            list1.add(pair.move);
        }
        return list1;
    }

    /**
     * Class that contains a move and its attached score
     */
    private static class MoveScorePair implements Comparable<MoveScorePair> {
        public PentagoMove move;
        public Integer score;

        public MoveScorePair(PentagoMove move, Integer score) {
            this.move = move;
            this.score = score;
        }

        /**
         * We compare moves by their scores
         */
        @Override
        public int compareTo(MoveScorePair o) {
            return this.score.compareTo(o.score);
        }
    }

    /**
     * Get all moves up to symmetry
     */
    public static ArrayList<MoveScorePair> getLegalMovesSymmetry(FastBoard boardState, int piece) {
//        get all possible moves
        ArrayList<PentagoMove> moves = boardState.getAllLegalMoves();
        ArrayList<MoveScorePair> nonDupeMoves = new ArrayList<>(moves.size());
        HashSet<Long> positions = new HashSet<>(moves.size());
        for (PentagoMove m : moves) {
            boardState.doMove(m);
            if (positions.add(boardState.getTag())) {
//				move leads to a unique position
                nonDupeMoves.add(new MoveScorePair(m, boardState.evaluate(piece)));
//                only search for symmetries in the first few moves
                if (boardState.getTurnNumber() < 3) {
                    //rotate 180
                    boardState.rotate180();
                    positions.add(boardState.getTag());
                    boardState.rotate180();
                }
            }
            boardState.undoMove(m);
        }
        return nonDupeMoves;
    }

    /**
     * Convert PentagoBoardState.Piece into an integer
     */
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

    /**
     * Custom internal representation of a PentagoBoardState with less overhead
     * and better performance
     */
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

        //    Get all legal moves
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

        /**
         * Evaluate board state and checks if anybody is winning the game
         * sets score the the local variable score
         * piece is used when white and black have different evaluation functions
         */
        public int evaluate(int piece) {
            if (evaluated) {
                return score;
            }
            evaluated = true;

            score = 0;
            boolean win = false;
            boolean otherWin = false;
            boolean premptwin = false;

            for (int[][] wins : MyTools.template) {
                int count = 0;
                int color = EMPTY;
                for (int i = 0; i < 5; i++) {
                    int p = board[wins[i][0]][wins[i][1]];
//                skip when we see nothing
                    if (p == EMPTY) {
                        continue;
                    }
//                update if it is the first piece we saw
                    if (color == EMPTY) {
                        color = p;
                        count++;
                    } else if (p != color) {
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
                if (color == piece) {
                    sign = 1;
                    if (count == 5) {
                        win = true;
                    }
                } else if (color == 1-piece) {
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
                if (count == 4 && this.turnPlayer == color) {
//                    it is also his turn to play compensate for that
                    premptwin = true;
                }
//                update score according to the count
                score += sign * MyTools.evalParams(count, piece);
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
            if (premptwin) {
                if (turnPlayer == piece) {
                    score = Integer.MAX_VALUE;
                } else {
                    score = Integer.MIN_VALUE;
                }
                gameOver = true;
                return score;
            }
//            if the game is over and drawn but is not obvious
            if (gameOver || turnNumber >= 18) {
                score = 0;
                gameOver = true;
                return 0;
            }
            return score;
        }

        /**
         * a more expensive evaluation function that hopefully distinguishes more moves
         * done for both sides
         */
        public int deepEvaluate(int piece) {
            evaluate(piece);
//            return score;
            if (gameOver) {
                return score;
            }
            int sum = 0;
            boolean premptwin = false;

            for (int[][] wins : MyTools.template2) {
                int count = 0;
                int color = EMPTY;
                for (int i = 0; i < 5; i++) {
//				get what piece is at the location
                    int p = board[wins[i][0]][wins[i][1]];
//                skip when we see nothing
                    if (p == EMPTY) {
                        continue;
                    }
//                update if it is the first piece we saw
                    if (color == EMPTY) {
                        color = p;
                        count++;
                    } else if (p != color) {
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
                if (color == piece) {
                    sign = 1;
                } else if (color == 1 - piece) {
                    sign = -1;
                }
                if ((count == 4 || count == 5) && color == turnPlayer) {
//                    he can win in one move
                    premptwin = true;
                    break;
                } else if (count == 5 && color == 1 - turnPlayer) {
//                  we are actually not winning (since he could block so we downgrade to count = 4
                    count = 4;
                }
                sum += sign * MyTools.evalParams(count, piece);
            }

            if (premptwin) {
                if (turnPlayer == piece) {
                    score = Integer.MAX_VALUE;
                } else {
                    score = Integer.MIN_VALUE;
                }
                gameOver = true;
                return score;
            }
            score += sum;
            return score;
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
            long index = 0;
            for (int x = 0; x < PentagoBoardState.BOARD_SIZE; x++) {
                for (int y = 0; y < PentagoBoardState.BOARD_SIZE; y++) {
                    index += board[x][y];
//			    mod 3 so we have to go up by 3 and we can barely fit inside the space
                    index *= 3;
                }
            }
            return index;
        }

        public void rotate180() {
            for (int x = 0; x < PentagoBoardState.BOARD_SIZE / 2; x++) {
                for (int y = 0; y < PentagoBoardState.BOARD_SIZE; y++) {
                    int temp = board[x][y];
                    board[x][y] = board[PentagoBoardState.BOARD_SIZE - x - 1][PentagoBoardState.BOARD_SIZE - y - 1];
                    board[PentagoBoardState.BOARD_SIZE - x - 1][PentagoBoardState.BOARD_SIZE - y - 1] = temp;
                }
            }
        }

        public int getTurnPlayer() {
            return turnPlayer;
        }
// #endregion

    }
}