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
    private boolean gridCreationFinished, optionPaintWhileGenerating = true;
    public static final int gCellSize = 50, gQuadLineThickness = 5;
    private JPanel panel;
    private final char[] permittedInputs = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
    private Thread currentGenerationThread;
    private static SudokuCreator currentSudokuCreatorObj;
    private JMenuItem menuItemGenerate, menuItemGenerateCont;
    private final int maxTries = 1000000;

    SudokuCreator(JPanel panel){
        this.panel = panel;
        createGrid();
        gridCreationFinished = true;
        createRows();
        createCols();
        createQuads();
        currentSudokuCreatorObj = this;
    }

    private void updateGrid(){
        resetRepeatedCells();
        checkRepeatedCells();
    }

    private void clearGrid(){
        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                c.changeContent(0);
            }
        }
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

    //Randomization/automation methods:
    private void randomizeCells(){
        Random r = new Random();

        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                c.randomizeContent(r);
            }
        }

        updateGrid();
        panel.repaint();
    }

    private boolean generatingCont;

    private void generateContinuously(){
        generatingCont = true;

        new Thread(() -> {
            try {
                while (generatingCont) {
                    generateValidGrid(0);
                    while (generating) {
                        Thread.sleep(100);
                    }
                    Thread.sleep(5000);
                }
            }catch(Exception e){ e.printStackTrace();}
        }).start();
    }

    private boolean generating;

    private void generateValidGrid(long seed){
        generating = true;
        currentGenerationThread =
                new Thread(() -> {
                    try {
                        int totalTries = 0;
                        clearGrid();
                        while(!isGridValid()) {
                            totalTries = 0;
                            clearGrid();
                            updateGrid();
                            if(optionPaintWhileGenerating) panel.repaint();

                            Random rand = new Random();
                            if (seed != 0){
                                rand.setSeed(seed);
                                System.out.println("Generating grid with seed: "+seed);
                            }

                            for (CellGroup g : cellRows) {
                                g.randomizeValidly(rand);
                                int tries = 0;
                                while (isGridRepeated() && generating && tries < maxTries) {
                                    g.randomizeValidly(rand);
                                    updateGrid();
                                    if(optionPaintWhileGenerating) panel.repaint();
                                    tries++;
                                }
                                if(!generating){
                                    break;
                                }
                                if(isGridRepeated()) break;
                                totalTries += tries;
                            }
                            if(!generating){
                                break;
                            }

                        }
                        System.out.println("Total Tries: "+totalTries);
                        if(!generatingCont) {
                            Window.currentWindowObj.menuItemGenerate.setText("Generate Valid Grid");
                        }
                        generating = false;
                        Window.currentWindowObj.menuItemGenerateCont.setEnabled(true);
                        panel.repaint();
                    }catch(Exception e){e.printStackTrace();}
                });
        currentGenerationThread.start();
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

    //Grid repetition and validation checks:
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

    private boolean doesGridContainZeroes(){
        boolean contains = false;
        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                if(c.getContent()==0){
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }

    private boolean isGridValid(){
        updateGrid();

        boolean valid = true;

        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                if(c.isRepeated || c.getContent()==0){
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

    private boolean isGridRepeated(){
        updateGrid();

        boolean repeated = false;

        for(Cell[] cc : fullGrid){
            for(Cell c : cc){
                if(c.isRepeated){
                    repeated = true;
                    break;
                }
            }
        }
        return repeated;
    }

    //I/O Methods.
    public void keyTyped(KeyEvent e) {

    }

    public void keyPressed(KeyEvent e) {
        System.out.println(e.getKeyChar());

        if(isInputPermitted(e.getKeyChar())){
            changeSelectedContents((int)e.getKeyChar() - 48);
            updateGrid();
            panel.repaint();
        }
    }

    private boolean isInputPermitted(char c){
        boolean toReturn = false;
        for(char a : permittedInputs){
            if(a == c) toReturn = true;
        }
        return toReturn;
    }

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
        if(!generating) {
            panel.repaint();
        }
    }

    public void actionPerformed(ActionEvent e) {
        menuItemGenerate = Window.currentWindowObj.menuItemGenerate;
        menuItemGenerateCont = Window.currentWindowObj.menuItemGenerateCont;

        switch (e.getActionCommand()) {
            case "randomize_all":
                randomizeCells();
                break;
            case "generate":
                if (generating) {
                    menuItemGenerateCont.setEnabled(true);
                    generating = false;
                    menuItemGenerate.setText("Generate Valid Grid");
                } else {
                    menuItemGenerateCont.setEnabled(false);
                    generateValidGrid(0);
                    menuItemGenerate.setText("Stop Generating");
                }
                break;
            case "generate_continuous":
                if (generatingCont) {
                    menuItemGenerate.setEnabled(true);
                    generatingCont = false;
                    generating = false;
                    menuItemGenerateCont.setText("Generate Valid Grids (Continuous)");
                } else {
                    menuItemGenerate.setEnabled(false);
                    menuItemGenerateCont.setText("Stop Generating (Continuous)");
                    generateContinuously();
                }
                break;
            case "seed_generate":
                JTextField seedTextField = (JTextField) e.getSource();
                try {
                    generateValidGrid(Long.valueOf(seedTextField.getText()));
                } catch (NumberFormatException ex) {
                    System.out.println("Seed must be an 8 byte number");
                }
                break;
            case "option_paint_generating":
                JCheckBox checkbox = (JCheckBox) e.getSource();
                optionPaintWhileGenerating = checkbox.isSelected();
                break;
        }
        System.out.println(e.getActionCommand());
    }

    public void itemStateChanged(ItemEvent e) {

    }
}
