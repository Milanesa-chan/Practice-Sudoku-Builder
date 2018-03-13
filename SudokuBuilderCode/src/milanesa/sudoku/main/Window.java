package milanesa.sudoku.main;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {
    private JPanel panel;
    private SudokuCreator sudokuCreator;
    private JMenuBar menuBar;
    public static void main(String[] args){new Window();}
    public JMenuItem menuItemGenerate, menuItemGenerateCont;
    public static Window currentWindowObj;

    private Window(){
        currentWindowObj = this;
        createWindow();
        panel.repaint();
    }

    private void createWindow() {

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setLayout(new BorderLayout());

        panel = new JPanel(){
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                paintPanel(g);
            }
        };

        //setLookAndFeel();

        sudokuCreator = new SudokuCreator(panel);

        generateMenuBar();
        add(menuBar, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);

        int windowSize = 4* SudokuCreator.gQuadLineThickness +9* SudokuCreator.gCellSize +6;
        panel.setSize(windowSize, windowSize);

        System.out.println(menuBar.getBounds().getHeight());

        SwingUtilities.invokeLater(() -> {
            this.getContentPane().setPreferredSize(new Dimension(windowSize, windowSize+23));
            this.pack();
            this.setResizable(false);

            panel.addMouseListener(sudokuCreator);
            panel.addMouseMotionListener(sudokuCreator);
            this.addKeyListener(sudokuCreator);
        });
    }

    private void setLookAndFeel(){
        SwingUtilities.invokeLater(() -> {
            try{
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }catch(Exception e){}
        });
    }

    private void generateMenuBar(){
        menuBar = new JMenuBar();

        JMenu actionTab = new JMenu("Action");
        menuBar.add(actionTab);
        actionTab.addActionListener(sudokuCreator);

        JMenuItem randomizeItem = new JMenuItem("Randomize Grid");
        randomizeItem.setActionCommand("randomize_all");
        randomizeItem.addActionListener(sudokuCreator);
        actionTab.add(randomizeItem);

        menuItemGenerate = new JMenuItem("Generate Valid Grid");
        menuItemGenerate.setActionCommand("generate");
        menuItemGenerate.addActionListener(sudokuCreator);
        actionTab.add(menuItemGenerate);

        menuItemGenerateCont = new JMenuItem("Generate Valid Grids (Continuous)");
        menuItemGenerateCont.setActionCommand("generate_continuous");
        menuItemGenerateCont.addActionListener(sudokuCreator);
        actionTab.add(menuItemGenerateCont);
    }

    private void paintPanel(Graphics g){
        paintLines(g);
        sudokuCreator.paintCells(g);
    }

    private void paintLines(Graphics g){
        g.setColor(Color.BLACK);
        int thick = SudokuCreator.gQuadLineThickness;
        int cell = SudokuCreator.gCellSize;

        g.fillRect(0, 0, panel.getWidth(), thick);
        g.fillRect(0, 0, thick, panel.getHeight());

        for(int q=0; q<3; q++){
            int x = thick+(3*cell+2+thick)*q+cell;
            g.drawLine(x, 0, x, panel.getHeight());
            x += cell+1;
            g.drawLine(x, 0, x, panel.getHeight());
            x += cell+1;
            g.fillRect(x, 0, thick, panel.getHeight());
        }

        for(int q=0; q<3; q++){
            int y = thick+(3*cell+2+thick)*q+cell;
            g.drawLine(0, y, panel.getWidth(), y);
            y += cell+1;
            g.drawLine(0, y, panel.getWidth(), y);
            y += cell+1;
            g.fillRect(0, y, panel.getWidth(), thick);
        }
    }


}
