package student_player;

import java.util.Iterator;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoBoardState.Piece;
import pentago_twist.PentagoCoord;

public class MyTools {
    public static double getSomething() {
        return Math.random();
    }
    
// check all win cons and count how many consecutive pieces we have
    public static int simpleEvaluate (PentagoBoardState boardState, Piece piece) {
		int sum = 0;
//    	check rows
    	for (int y = 0; y < PentagoBoardState.BOARD_SIZE; y++) {
    		for (int x = 0; x < 2; x++) {
    			int count = 0;
    			Piece thing = Piece.EMPTY;
    			for (int i = 0; i < 5; i++) {
        			PentagoCoord startCoord = new PentagoCoord(x+i, y);
//    				get what piece is at the location
    				Piece p = boardState.getPieceAt(startCoord);
    				if (p == Piece.EMPTY) {
    					continue;
    				}
    				if (thing == Piece.EMPTY) {
						thing = p;
						count++;
					}
					else if (p != thing) {
						count = 0;
						break;
					}
					else {
						count++;
					}
				}
//    			scoring algo
    			int sign = 0;
    			if (thing == piece) {
    				sign = 1;
    			}
    			else if (thing != Piece.EMPTY) {
    				sign = -1;
    			}
    			sum += sign*simpleEvalParams(count);
    		}
		}

//    	check column
    	for (int x = 0; x < PentagoBoardState.BOARD_SIZE; x++) {
    		for (int y = 0; y < 2; y++) {
    			int count = 0;
    			Piece thing = Piece.EMPTY;
    			for (int i = 0; i < 5; i++) {
        			PentagoCoord startCoord = new PentagoCoord(x, y+i);
//    				get what piece is at the location
    				Piece p = boardState.getPieceAt(startCoord);
    				if (p == Piece.EMPTY) {
    					continue;
    				}
    				if (thing == Piece.EMPTY) {
						thing = p;
						count++;
					}
					else if (p != thing) {
						count = 0;
						break;
					}
					else {
						count++;
					}
				}
//    			scoring algo
    			int sign = 0;
    			if (thing == piece) {
    				sign = 1;
    			}
    			else if (thing != Piece.EMPTY) {
    				sign = -1;
    			}
    			sum += sign*simpleEvalParams(count);
    		}
		}
    	
//    	check right diag
    	for (int x = 0; x < 2; x++) {
    		for (int y = 0; y < 2; y++) {
    			int count = 0;
    			Piece thing = Piece.EMPTY;
    			for (int i = 0; i < 5; i++) {
        			PentagoCoord startCoord = new PentagoCoord(x+i, y+i);
//    				get what piece is at the location
    				Piece p = boardState.getPieceAt(startCoord);
    				if (p == Piece.EMPTY) {
    					continue;
    				}
    				if (thing == Piece.EMPTY) {
						thing = p;
						count++;
					}
					else if (p != thing) {
						count = 0;
						break;
					}
					else{
						count++;
					}
				}
//    			scoring algo
    			int sign = 0;
    			if (thing == piece) {
    				sign = 1;
    			}
    			else if (thing != Piece.EMPTY) {
    				sign = -1;
    			}
    			sum += sign*simpleEvalParams(count);
    		}
		}
    	
//    	check left diag
    	for (int x = 0; x < 2; x++) {
    		for (int y = PentagoBoardState.BOARD_SIZE-2; y < PentagoBoardState.BOARD_SIZE; y++) {
    			int count = 0;
    			Piece thing = Piece.EMPTY;
    			for (int i = 0; i < 5; i++) {
        			PentagoCoord startCoord = new PentagoCoord(x+i, y-i);
//    				get what piece is at the location
    				Piece p = boardState.getPieceAt(startCoord);
    				if (p == Piece.EMPTY) {
    					continue;
    				}
    				if (thing == Piece.EMPTY) {
						thing = p;
						count++;
					}
					else if (p != thing) {
						count = 0;
						break;
					}
					else {
						count++;
					}
				}
//    			scoring algo
    			int sign = 0;
    			if (thing == piece) {
    				sign = 1;
    			}
    			else if (thing != Piece.EMPTY) {
    				sign = -1;
    			}
    			sum += sign*simpleEvalParams(count);
    		}
		}
    	return sum;
    }
    
    
    
    private static int simpleEvalParams(int count) {
    	switch (count) {
    	case 0:
			return 0;
		case 1:
			return 1;
		case 2:
			return 5;
		case 3:
			return 50;
		case 4:
			return 1000;
		case 5:
			return 100000;
		default:
			return 0;
		}

    }
}