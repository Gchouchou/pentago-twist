package student_player;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoCoord;
import pentago_twist.PentagoMove;

import java.util.ArrayList;

//	faster board implementation and allows reversing moves
public class FastBoard {
    int[][] board;
    boolean evaluated;
    int score;
    boolean gameOver;
    int turnPlayer;
    int turnNumber;

    public static final int WHITE = 0;
    public static final int BLACK = 1;
    public static final int EMPTY = 3;

    //    copy a boardState
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
            score += sign * MyTools.evalParams(count);
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
// #endregion

}

