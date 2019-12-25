package com.akashd50.smallmultiplayergames;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ActiveGame {
    public static final int P1 = 1;
    public static final int P2 = 2;
    public static final String P1Tag = "O";
    public static final String P2Tag = "X";

    private int playerTurn;
    private List<Integer> gameBoard;
    private int gameWonBy;

    public ActiveGame(){
        playerTurn = P1;
        gameBoard = new ArrayList<>();
        for(int i=0;i<9;i++){
            gameBoard.add(-1);
        }

        gameWonBy = -1;
    }

    public void update(int row, int col, int state){
        int index = row*3 + col;
        gameBoard.set(index, state);
    }

    public int get(int row, int col){
        int index = row*3 + col;
        return gameBoard.get(index);
    }

    public int getPlayerTurn() {
        return playerTurn;
    }

    public void setTurn(int p) {
        playerTurn = p;
    }

    public void setGameBoard(List<Integer> list){
        this.gameBoard.clear();
        this.gameBoard.addAll(list);
    }

    public boolean hasGameEnded(){
        return false;
    }

    public int isGameWon(){
        if(isRowEqual(0)!=-1){
            return isRowEqual(0);
        }else if(isRowEqual(1)!=-1){
            return isRowEqual(1);
        }else if(isRowEqual(2)!=-1){
            return isRowEqual(2);
        }

        if(isColEqual(0)!=-1){
            return isColEqual(0);
        }else if(isColEqual(1)!=-1){
            return isColEqual(1);
        }else if(isColEqual(2)!=-1){
            return isColEqual(2);
        }

        if(get(0,0)!=-1 && (get(0,0)==get(1,1) && get(1,1) == get(2,2))){
            return get(0,0);
        }

        if(get(2,0)!=-1 && (get(2,0)==get(1,1) && get(1,1) == get(0,2))){
            return get(2,0);
        }

        return -1;
    }

    private int isRowEqual( int row ){
        if(get(row,0) != -1 && get(row,0) == get(row,1) && get(row,1) == get(row,2)){
            return get(row,0);
        }
        return -1;
    }

    private int isColEqual( int col){
        if(get(0,col) != -1 && get(0,col) == get(1,col) && get(1,col) == get(2,col)){
            return get(0,col);
        }
        return -1;
    }

    public void resetBoard(){
        gameBoard.clear();
        for(int i=0;i<9;i++){
            gameBoard.add(-1);
        }
    }

    public List<Integer> getGameBoard(){
        return gameBoard;
    }
}
