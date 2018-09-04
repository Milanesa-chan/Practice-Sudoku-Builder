package milanesa.sudoku.main;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class Window extends JFrame {
    private JPanel panel;
    private SudokuCreator sudokuCreator;
    private JMenuBar menuBar;
    public static void main(String[] args){new Window();}
    public JMenuItem menuItemGenerate, menuItemGenerateBlankSpaces;
    public JTextField menuSeedField;
    public static Window currentWindowObj;
    public JLabel bottomLabel;

    private Window(){
        currentWindowObj = this;
        createWindow();
        panel.repaint();
    }

    public String getCurrentSeed(){
        System.out.println("Current seed return: "+menuSeedField.getText());
        return menuSeedField.getText();
    }

    private void createWindow() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        this.setResizable(false);

        panel = new JPanel(){
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                paintPanel(g);
            }
        };
        sudokuCreator = new SudokuCreator(panel);

        generateMenuBar();
        bottomLabel = new JLabel("Welcome to Sudoku Helper.");

        add(menuBar, BorderLayout.NORTH);
        add(panel, BorderLayout.SOUTH);
        add(bottomLabel, BorderLayout.SOUTH);

        int windowSize = 4* SudokuCreator.gQuadLineThickness + 9*SudokuCreator.gCellSize + 6;

        int menuBarHeight = menuBar.getPreferredSize().height;
        menuBar.setPreferredSize(new Dimension(windowSize, menuBarHeight));
        int bottomLabelHeight = bottomLabel.getPreferredSize().height;
        bottomLabel.setPreferredSize(new Dimension(windowSize, bottomLabelHeight));
        int windowHeight = windowSize+menuBarHeight+bottomLabelHeight;
        panel.setPreferredSize(new Dimension(windowSize, windowSize));

        menuBar.setBounds(0, 0, menuBar.getPreferredSize().width, menuBar.getPreferredSize().height);
        panel.setBounds(0, menuBarHeight, panel.getPreferredSize().width, panel.getPreferredSize().height+1);

        SwingUtilities.invokeLater(() -> {
            this.getContentPane().setPreferredSize(new Dimension(windowSize, windowHeight));
            this.pack();
            this.setVisible(true);

            panel.addMouseListener(sudokuCreator);
            panel.addMouseMotionListener(sudokuCreator);
            this.addKeyListener(sudokuCreator);
        });


    }

    private void print(Object toPrint) {
        System.out.println(toPrint);
    }

    private void generateMenuBar(){
        menuBar = new JMenuBar();

        JMenu actionTab = new JMenu("Action"); //Action tab.
        menuBar.add(actionTab);
        actionTab.addActionListener(sudokuCreator);

        menuItemGenerate = new JMenuItem("Generate Valid Grid"); //Generate valid grid item.
        menuItemGenerate.setActionCommand("generate");
        menuItemGenerate.addActionListener(sudokuCreator);
        actionTab.add(menuItemGenerate);

        menuItemGenerateBlankSpaces = new JMenuItem("Generate Blank Spaces");
        menuItemGenerateBlankSpaces.setActionCommand("generate_blanks");
        menuItemGenerateBlankSpaces.addActionListener(sudokuCreator);
        actionTab.add(menuItemGenerateBlankSpaces);

        JMenu optionsTab = new JMenu("Options"); //Options tab.
        menuBar.add(optionsTab);

        JCheckBox optionPaintGenerating = new JCheckBox("Paint While Generating"); //Option paint generating.
        optionPaintGenerating.setActionCommand("option_paint_generating");
        optionPaintGenerating.addActionListener(sudokuCreator);
        optionPaintGenerating.setSelected(true);
        optionsTab.add(optionPaintGenerating);

        JCheckBox optionRandomSeed = new JCheckBox("Randomize Seed");
        optionRandomSeed.setActionCommand("random_seed");
        optionRandomSeed.addActionListener(sudokuCreator);
        optionRandomSeed.setSelected(true);
        optionsTab.add(optionRandomSeed);

        JMenu seedTab = new JMenu("Seed:");
        optionsTab.add(seedTab);

        menuSeedField = new JTextField(20); //Seed text field.
        menuSeedField.setActionCommand("seed_field");
        menuSeedField.addActionListener(sudokuCreator);
        seedTab.add(menuSeedField);
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

    public void setLabelMessage(String message){
        bottomLabel.setText(message);
    }
}
