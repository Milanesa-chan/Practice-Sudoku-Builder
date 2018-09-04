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
    private boolean gridCreationFinished, optionPaintWhileGenerating = true, optionRandomSeed = true;
    public static final int gCellSize = 50, gQuadLineThickness = 5;
    private JPanel panel;
    private final char[] permittedInputs = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
    private Thread currentGenerationThread;
    private static SudokuCreator currentSudokuCreatorObj;
    private JMenuItem menuItemGenerate;
    private final int maxTries = 1000000;
    private long currentSeed = 12345;
    private Window windowObj;

    SudokuCreator(JPanel panel){
        windowObj = Window.currentWindowObj;
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
    private boolean generating;

    private void generateValidGrid(long seed){
        generating = true;
        currentGenerationThread =
                new Thread(() -> {
                    try {
                        long seedForThread = seed;
                        int totalTries = 0;
                        clearGrid();
                        while(!isGridValid()) {
                            clearGrid();
                            updateGrid();

                            if(optionPaintWhileGenerating) panel.repaint();

                            Random rand = new Random();
                            if (seedForThread != 0){
                                rand.setSeed(seedForThread);

                                windowObj.setLabelMessage("Generating grid with seed: "+seedForThread);
                            }else{
                                windowObj.setLabelMessage("Generating grid with random seed.");
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
                            if(isGridRepeated() && seed != 0){
                                windowObj.setLabelMessage("Seed cannot generate a valid grid.");
                                break;
                            }
                            if(!generating){
                                break;
                            }

                        }
                        System.out.println("Total Tries: "+totalTries);
                        menuItemGenerate.setText("Generate Valid Grid");
                        if(!isGridRepeated()) {
                            windowObj.setLabelMessage("Finished generating grid.");
                        }
                        generating = false;
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
    void paintCells(Graphics g){
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
        //Sends the event to the different subgroups of menus.
        actionTabActions(e);
        optionsTabActions(e);
        System.out.println(e.getActionCommand());
    }

    //Tests actions from the actions tab.
    private void actionTabActions(ActionEvent e){
        menuItemGenerate = windowObj.menuItemGenerate;

        switch (e.getActionCommand()) {
            case "generate":
                if (generating) {
                    generating = false;
                    menuItemGenerate.setText("Generate Valid Grid");
                    windowObj.setLabelMessage("Stopped generation.");
                } else {
                    if(optionRandomSeed){
                        generateValidGrid(0);
                    }else {
                        generateValidGrid(currentSeed);
                    }
                    menuItemGenerate.setText("Stop Generating");
                }
                break;
        }
    }
    //Tests actions from the options tab.
    private void optionsTabActions(ActionEvent e){
        JCheckBox checkbox;
        switch(e.getActionCommand()){
            case "option_paint_generating":
                checkbox = (JCheckBox) e.getSource();
                optionPaintWhileGenerating = checkbox.isSelected();
                break;
            case "seed_field":
                JTextField field = (JTextField) e.getSource();
                try {
                    currentSeed = Long.parseLong(field.getText());
                    windowObj.setLabelMessage("Seed set to: "+currentSeed);
                }catch(NumberFormatException ex){
                    windowObj.setLabelMessage("Incorrect seed format or length. Must be a number.");
                }
                field.setText("");
                break;
            case "random_seed":
                checkbox = (JCheckBox) e.getSource();
                optionRandomSeed = checkbox.isSelected();
        }
    }

    public void itemStateChanged(ItemEvent e) {}
}
