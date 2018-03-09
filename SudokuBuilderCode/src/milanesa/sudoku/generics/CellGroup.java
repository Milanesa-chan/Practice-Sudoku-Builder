package milanesa.sudoku.generics;

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
}
