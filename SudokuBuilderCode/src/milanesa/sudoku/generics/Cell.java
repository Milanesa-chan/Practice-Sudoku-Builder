package milanesa.sudoku.generics;

import milanesa.sudoku.main.SudokuCreator;

import java.awt.*;
import java.util.Random;

public class Cell {
    private final Font cellFont = new Font("Arial", Font.BOLD, SudokuCreator.gCellSize-(SudokuCreator.gCellSize/10));
    private final Color gHoveredColor = new Color(200, 200, 200), gSelectedColor = new Color(120, 255, 120);
    private int content;
    private int absX, absY;
    public boolean isRepeated = false, isHovered = false, isSelected = false, locked;
    public Rectangle gRect;

    public Cell(int content, int x, int y){
        absX = x;
        absY = y;
        gRect = new Rectangle(x, y, SudokuCreator.gCellSize, SudokuCreator.gCellSize);
        this.content = content;
    }

    public void paint(Graphics g){
        if(!isHovered && !isSelected){
            g.setColor(Color.WHITE);
        }
        if(isHovered){
            g.setColor(gHoveredColor);
        }
        if(isSelected){
            g.setColor(gSelectedColor);
        }
        g.fillRect(absX, absY, SudokuCreator.gCellSize, SudokuCreator.gCellSize);
        if(isRepeated){
            g.setColor(Color.RED);
        }else {
            g.setColor(Color.BLACK);
        }
        g.setFont(cellFont);
        String numberToDisplay = String.valueOf(content);
        int stringW = g.getFontMetrics().stringWidth(numberToDisplay);
        int stringH = g.getFontMetrics().getHeight();
        int stringX = absX+(SudokuCreator.gCellSize/2)-(stringW/2);
        int stringY = absY+(SudokuCreator.gCellSize/2)+(stringW/2);
        g.drawString(numberToDisplay, stringX, stringY);
    }

    public void repeated(boolean state){
        if(!locked) {
            isRepeated = state;
        }
    }

    public void beingHovered(boolean state){
        isHovered = state;
    }

    public void beingSelected(boolean state){
        isSelected = state;
    }

    public void randomizeContent(Random r){
        this.content = 1+r.nextInt(8);
    }

    public void lock(boolean state){
        locked = state;
    }

    public boolean isLocked(){
        return locked;
    }

    public int getContent(){
        return content;
    }

    public void changeContent(int content){
        if(!locked) {
            this.content = content;
        }
    }
}
