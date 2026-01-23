package hr.algebra.battleship.model.game;

import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private Board board;
    private int score;
    private boolean isAi;

    public Player(String name, Board board) {
        this.name = name;
        this.board = board;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Board getBoard() {
        return board;
    }
    public void setBoard(Board board) {
        this.board = board;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public boolean isAi() {
        return isAi;
    }
    public void setAi(boolean ai) {
        isAi = ai;
    }

}
