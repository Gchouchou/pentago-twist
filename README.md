# pentago_twist AI

This is a project for comp 424 in Winter 2021. The goal was to design an AI to play pentago Twist.

## The Game

![](https://github.com/SaminYeasar/pentago_twist/blob/main/image/game.gif)


Pentago-Twist falls into the Moku family of games. Other popular games in this category include Tic-Tac-Toe and Connect-4, although Pentago-Twist has significantly more complexity. The biggest difference in
Pentago-Twist is that the board is divided into quadrants, which can be flipped or rotate 90 degree right during the game.

### Setup 
Pentago-twist is a two-player game played on a 6x6 board, which consists of four 3x3 quadrants. To begin, the board is empty. The first player plays as white and the other plays as black.


### Objective 
In order to win, each player tries to achieve 5 pieces in a row before their opponent does. A winning row can be achieved horizontally, vertically or diagonally. If all spaces on the board are occupied without a winner then a draw is declared. If rotating/flipping single quadrant results in a five-in-a-row for both players, the game also ends in a draw.


### Playing
Moves consist of two phases: placing and rotating/flipping. On a given player's turn, a piece is first placed in an empty slot on the board. The player then selects a quadrant, and can choose either to flip or rotate 90 degree right. A
complete move therefore consists of placing a piece, then rotating/flipping.

## The AI

My agent uses a minimax tree of depth 3 with alpha-beta pruning. I developed an internal representation and removed duplicate board states to ensure it finished searching in 2 seconds.
The evaluation function goes through every 5 consecutive squares and gives scores depending on the number of uncontested stones.
It also goes through every possible 5 consecutive squares after twisting the board once.
The parameters were obtained by generating a set of parameters and choosing one that lost the least.

---

# Navigate through the scripts

    src  
    |  --- autoplay ( Autoplays games; can be ignored) 
    | --- boardgame (Package for implementing boardgames, logging, GUI, and server TCP protocol, can be ignored for this project)
    | --- student_player (Package containing your agent)
    |      | --- StudentPlayer.java (The class you will implement your AI within)
    |      | --- MyTools.java (Placeholder for any extra code you may need)} \nonumber 
    | --- pentago_swap (The package implementing all game logic)
    |      | --- PentagoBoardPanel.java (Implements the GUI, can be ignored)
    |      | --- PentagoBoard.java (Used for server logic, can be ignored)
    |      | --- PentagoCoord.java (Simple class representing a board coordinate)
    |      | --- PentagoMove.java (A move object for Pentago. Relevant functions)
    |      | --- PentagoBoardState.java (Implements all game logic, most important. Note that PentagoBoardState (PBS) manages} logic concerning whose turn it is, rules, and the positions of all pieces)
    |      | --- PentagoPlayer.java (Abstract class that all players extend)} 
    |      | --- RandomPentagoPlayer.java (A random player, can be used as a baseline)

