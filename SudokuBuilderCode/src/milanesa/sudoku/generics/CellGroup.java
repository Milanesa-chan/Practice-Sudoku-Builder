package milanesa.sudoku.generics;

import milanesa.sudoku.main.SudokuCreator;

import java.util.ArrayList;
import java.util.Random;

public class CellGroup {
    public Cell[] groupCells;

    public CellGroup(Cell[] cells){
        groupCells = cells;
    }

    public void updateRepeatedCells(){
        for(Cell Ca : groupCells){
            for(Cell Cb: groupCells){
                if(!Ca.equals(Cb) && Ca.getContent() == Cb.getContent() && Ca.getContent() != 0){
                    Ca.repeated(true);
                    Cb.repeated(true);
                }
            }
        }
    }

    public Cell[] getCellGrid(){
        return groupCells;
    }

    public void randomizeValidly(Random r){
        ArrayList<Integer> usedNumbers = new ArrayList<>();

        for(Cell c : groupCells){
            int nextNumber = 1+r.nextInt(9);
            while(usedNumbers.contains(nextNumber)){
                nextNumber = 1+r.nextInt(9);
            }
            c.changeContent(nextNumber);
            usedNumbers.add(nextNumber);
        }
    }

}
