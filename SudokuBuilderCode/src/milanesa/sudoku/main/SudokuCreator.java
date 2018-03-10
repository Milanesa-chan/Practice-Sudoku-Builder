package milanesa.sudoku.main;

import milanesa.sudoku.generics.Cell;
import milanesa.sudoku.generics.CellGroup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class SudokuCreator implements KeyListener, MouseMotionListener, MouseListener, ActionListener, ItemListener{
    private Cell[][] fullGrid;
    private CellGroup[] cellRows, cellCols;
    private CellGroup[][] cellQuads;
    private boolean gridCreationFinished;
    public static final int gCellSize = 50, gQuadLineThickness = 5;
    private JPanel panel;
    private final char[] permittedInputs = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

    SudokuCreator(JPanel panel){
        this.panel = panel;
        createGrid();
        gridCreationFinished = true;
        createRows();
        createCols();
        createQuads();
    }

    private void updateGrid(){
        resetRepeatedCells();
        checkRepeatedCells();

        panel.repaint();
    }

    private void changeSelectedContents(int cont){
        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                if(c.isSelected){
                    c.changeContent(cont);
                }
            }
        }
    }

    private void createQuads(){
        cellQuads = new CellGroup[3][3];

        for(int x=0; x<3; x++){
            for(int y=0; y<3; y++){
                ArrayList<Cell> cells = new ArrayList<Cell>();
                for(int cellX=0; cellX<9; cellX++){
                    for(int cellY=0; cellY<9; cellY++){
                        if(cellX/3 == x && cellY/3 == y){
                            cells.add(fullGrid[cellX][cellY]);
                        }
                    }
                }
                Cell[] cellsToAdd = new Cell[9];
                for(Cell c : cells){
                    cellsToAdd[cells.indexOf(c)] = c;
                }
                cellQuads[x][y] = new CellGroup(cellsToAdd);
            }
        }
    }

    private void createCols(){
        cellCols = new CellGroup[9];
        for(int x=0; x<9; x++){
            Cell[] cellsToAdd = new Cell[9];
            for(int y=0; y<9; y++){
                cellsToAdd[y] = fullGrid[y][x];
            }
            cellCols[x] = new CellGroup(cellsToAdd);
        }
    }

    private void createRows() {
        cellRows = new CellGroup[9];
        for(int y=0; y<9; y++){
            cellRows[y] = new CellGroup(fullGrid[y]);
        }
    }

    private void createGrid(){
        fullGrid = new Cell[9][9];

        for(int y=0;y<9;y++){
            for(int x=0;x<9;x++){
                int quadX = x / 3;
                int quadY = y / 3;
                int xInQuad = x % 3;
                int yInQuad = y % 3;

                int cellX = gQuadLineThickness+(quadX*(3*gCellSize + gQuadLineThickness + 2) + xInQuad*(gCellSize+1));
                int cellY = gQuadLineThickness+(quadY*(3*gCellSize + gQuadLineThickness + 2) + yInQuad*(gCellSize+1));
                fullGrid[y][x] = new Cell(0, cellX, cellY);
            }
        }
    }

    private void generateValidGrid(){
        new Thread(() -> {
            try {
                ArrayList<Cell> cellsArray = new ArrayList<>();
                for (int currentCell = 0; currentCell < 81; currentCell++) {
                    Cell currentCellObj = fullGrid[currentCell / 9][currentCell % 9];
                    boolean repetitionSolved = false;
                    for (int n = currentCellObj.getContent()+1; n < 10; n++) {
                        currentCellObj.changeContent(n);
                        updateGrid();
                        if (!currentCellObj.isRepeated) {
                            repetitionSolved = true;
                            break;
                        }
                    }
                    if (!repetitionSolved)
                        currentCell = 0;
                    Thread.sleep(100);
                }
            }catch(Exception e){e.printStackTrace();}
        }).start();
    }

    //Randomization/automation methods:
    public void randomizeCells(){
        Random r = new Random();
        unlockAllCells();

        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                c.randomizeContent(r);
            }
        }

        updateGrid();
    }

    public void randomizeRepeatedCells(){
        Random r = new Random();
        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                if(c.isRepeated){
                    c.randomizeContent(r);
                }else{
                    c.lock(true);
                }
            }
        }

        updateGrid();
    }

    private void unlockAllCells(){
        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                c.lock(false);
            }
        }
    }

    //Cell input checking and selection methods:
    private void checkIfHovered(Point cursor){
        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                if(c.gRect.contains(cursor)){
                    c.beingHovered(true);
                }else{
                    c.beingHovered(false);
                }
            }
        }
    }

    private void checkIfSelected(Point cursor){
        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                if(c.gRect.contains(cursor)){
                    c.beingSelected(true);
                }
            }
        }
    }

    private void checkAndSwitch(Point cursor){
        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                if(c.gRect.contains(cursor)){
                    c.beingSelected(!c.isSelected);
                }
            }
        }
    }

    private void deselectAll(){
        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                c.beingSelected(false);
            }
        }
    }

    //Main graphics method:
    public void paintCells(Graphics g){
        if(gridCreationFinished) {
            for (Cell[] cc : fullGrid) {
                for (Cell c : cc) {
                    c.paint(g);
                }
            }
        }
    }

    //Repetition checking:
    private void resetRepeatedCells(){
        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                c.repeated(false);
            }
        }
    }

    private void checkRepeatedCells(){
        for(CellGroup cg : cellRows){
            cg.updateRepeatedCells();
        }
        for(CellGroup cg : cellCols){
            cg.updateRepeatedCells();
        }
        for(CellGroup[] ccg : cellQuads){
            for(CellGroup cg : ccg){
                cg.updateRepeatedCells();
            }
        }
    }

    private boolean isGridValid(){
        updateGrid();

        boolean valid = true;

        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                if(c.isRepeated){
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

    //Hardware input checking:
    public void keyTyped(KeyEvent e) {

    }

    private boolean randomizing;

    public void keyPressed(KeyEvent e) {
        System.out.println(e.getKeyChar());

        if(isInputPermitted(e.getKeyChar())){
            changeSelectedContents((int)e.getKeyChar() - 48);
            updateGrid();
        }
        if(e.getKeyChar() == 't'){
            randomizing = !randomizing;

            new Thread(() -> {
                while(randomizing) {
                    try{
                        Thread.sleep(10);
                    }catch(Exception ex){}
                    randomizeRepeatedCells();
                }
            }).start();
        }
    }

    private boolean isInputPermitted(char c){
        boolean toReturn = false;
        for(char a : permittedInputs){
            if(a == c) toReturn = true;
        }
        return toReturn;
    }

    //I/O Methods.

    public void keyReleased(KeyEvent e) {

    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if(e.getButton()==1) {
            deselectAll();
            checkAndSwitch(e.getPoint());
        }else if(e.getButton()==3){
            deselectAll();
        }
        panel.repaint();
    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e) {

    }

    public void mouseMoved(MouseEvent e) {
        checkIfHovered(e.getPoint());
        panel.repaint();
    }

    private void generateValidGridByRandomization(){
        randomizeCells();
        while(!isGridValid()){
            randomizeCells();
            updateGrid();
            try{Thread.sleep(50);}catch (Exception e){e.printStackTrace();}
        }
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("randomize_all")){
            randomizeCells();
        }else if(e.getActionCommand().equals("generate")){
            generateValidGrid();
        }else if(e.getActionCommand().equals("randomize_generate")){
            new Thread(this::generateValidGridByRandomization).start();
        }
        System.out.println(e.getActionCommand());
    }

    public void itemStateChanged(ItemEvent e) {

    }
}
