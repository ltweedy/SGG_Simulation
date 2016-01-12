import com.sun.org.apache.xalan.internal.xsltc.compiler.*;
import sun.jvm.hotspot.code.ObjectValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: luke
 * Date: 18/02/2014
 * Time: 09:50
 * To change this template use File | Settings | File Templates.
 */
public class cellPlotter extends JPanel {

    static int border = 4;  //pixel border
    JFrame frame;
    int    upCalls;
    cellPlotter THIS = this;
    List<cell> cells;
    ChemicalEnvironment environment;
    MigrationSimulation ms;

    private Point clickPoint;
    private Point dragPoint;
    private Rectangle selectionBounds;
    private Point     ovalSelection;
    private int ovalWidth = 0;
    private int Ovalheight = 0;
    private boolean selectionIsOval = false;
    private boolean shiftDown = false;



    public cellPlotter(List<cell> cells, ChemicalEnvironment environment, MigrationSimulation ms, JFrame frame){
        this.ms = ms;
        this.environment = environment;
        this.frame = frame;
        this.cells = cells;
        this.setFocusable(true);

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_SHIFT) shiftDown = true;

            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftDown = false;

                }
            }
        });

        MouseAdapter handler = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e){
                selectionBounds = null;
                ovalSelection = convertPointToSimulationSpace(e.getPoint());
                getParent().repaint();
            }

            @Override
            public void mousePressed(MouseEvent e){
                selectionIsOval = shiftDown;
                clickPoint = convertPointToSimulationSpace(e.getPoint());
                selectionBounds = null;
                ovalSelection = null;
            }

            @Override
            public void mouseReleased(MouseEvent e){
                clickPoint = null;
                if(selectionBounds!=null); //selectAllInBounds(selectionBounds);
                //selectionBounds = null;
                ovalSelection = null;
                getParent().repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point dragPoint = convertPointToSimulationSpace(e.getPoint());
                int x = Math.min(clickPoint.x, dragPoint.x);
                int y = Math.min(clickPoint.y, dragPoint.y);
                int width = Math.max(clickPoint.x - dragPoint.x, dragPoint.x - clickPoint.x);
                int height = Math.max(clickPoint.y - dragPoint.y, dragPoint.y - clickPoint.y);
                selectionBounds = new Rectangle(x, y, width, height);
                getParent().getParent().repaint();
            }
        };

        this.addMouseListener(handler);
        this.addMouseMotionListener(handler);

    }

    // This method is called whenever the contents needs to be painted
    public synchronized void paintComponent(Graphics g) {

        int width = this.frame.getContentPane().getWidth();
        int height = this.frame.getContentPane().getHeight();

        double hScale = (width-2.0*border)/MelaMigration.dimensions[2];
        double vScale = (height-2.0*border)/MelaMigration.dimensions[3];

        cell c;

        Graphics2D g2d = (Graphics2D)g;

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2F));

        for(int i=1; i<=24; i++){
            g2d.drawLine((int) ((1000.0*i/ms.xMax)*hScale*ms.xMax), 0, (int) ((1000.0*i/ms.xMax)*hScale*ms.xMax), 200);
        }

        g2d.setStroke(new BasicStroke(3F));
        for(int i = 0; i<environment.profile.length; i++){
            for(int j = 0; j<environment.profile[i].length; j++){
                g2d.setColor(new Color((int) Math.max(0, Math.min(250,(50*environment.profile[i][j][0]))),0,(int) Math.max(0, Math.min(250,(50*environment.profile[i][j][1])))));
                g2d.fillRect(border+(int) (hScale*ChemicalEnvironment.grain*i)-1, border+(int) (vScale*ChemicalEnvironment.grain*j)-1, (int) (hScale*ChemicalEnvironment.grain)+2,  (int) (vScale*ChemicalEnvironment.grain)+2);
            }
        }

        g2d.setStroke(new BasicStroke(3F));
        for(int i = cells.size()-1; i>=0; i--){
            c = cells.get(i);
            if(c.original) g2d.setColor(new Color(60,120,200));
            else g2d.setColor(new Color(50,200,150));
            g2d.fillOval(border + (int) (hScale*(c.x()-(-1+cell.width/2))), border + (int) (vScale*(c.y()-(-1+cell.width/2))), (int) Math.max(3.0, hScale*cell.width-2), (int) Math.max(3.0, vScale*cell.width-2));
        }

        g2d.setColor(Color.GREEN);
        g2d.setStroke(new BasicStroke(1.0F));
        if(selectionBounds!=null){

            Point p1 = convertPointToDisplaySpace(new Point(selectionBounds.x,selectionBounds.y));
            Point p2 = convertPointToDisplaySpace(new Point(selectionBounds.width,selectionBounds.height));


            if(selectionIsOval) g2d.drawOval(p1.x,p1.y,p2.x,p2.y);
            else g2d.drawRect(p1.x, p1.y, p2.x, p2.y);
        }

        g2d.dispose();
    }

    public void setEnvironment(double c){

        boolean ps = ms.paused;

        if(!ps) ms.paused = true;

        for(int i=0; i<environment.profile.length; i++){
            for(int j=0; j<environment.profile[i].length; j++){
                if(i*environment.grain >= selectionBounds.x && i*environment.grain <= selectionBounds.x+selectionBounds.width
                        && j*environment.grain >= selectionBounds.y && j*environment.grain <= selectionBounds.y+selectionBounds.height){


                    if(!selectionIsOval) environment.oldprofile[i][j][0] = c;
                    else{
                        Ellipse2D e2d = new Ellipse2D.Double(selectionBounds.x,selectionBounds.y,selectionBounds.width,selectionBounds.height);
                        if(e2d.contains(i*environment.grain,j*environment.grain)){

                            environment.oldprofile[i][j][0] = c;

                        }
                    }
                    // environment.profile[i][j] = c;
                }
            }
        }


        if(!ps) ms.paused = false;
    }

    Point convertPointToSimulationSpace(Point p){
        int width = this.frame.getContentPane().getWidth();
        int height = this.frame.getContentPane().getHeight();

        double hScale = (width-2.0*border)/MelaMigration.dimensions[2];
        double vScale = (height-2.0*border)/MelaMigration.dimensions[3];

        Point p2 = p;

        p2.x = (int) ((p.x-border)/hScale);
        p2.y = (int) ((p.y-border)/vScale);

        return p2;
    }

    Point convertPointToDisplaySpace(Point p){
        int width = this.frame.getContentPane().getWidth();
        int height = this.frame.getContentPane().getHeight();

        double hScale = (width-2.0*border)/MelaMigration.dimensions[2];
        double vScale = (height-2.0*border)/MelaMigration.dimensions[3];

        Point p2 = p;

        p2.x = (int) ((p.x*hScale)+border);
        p2.y = (int) ((p.y*vScale)+border);

        return p2;

    }
}
