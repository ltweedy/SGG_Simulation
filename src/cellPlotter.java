import javax.swing.*;
import java.awt.*;
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


        public cellPlotter(List<cell> cells, ChemicalEnvironment environment, MigrationSimulation ms, JFrame frame){
            this.ms = ms;
            this.environment = environment;
            this.frame = frame;
            this.cells = cells;
        }

    // This method is called whenever the contents needs to be painted
    public synchronized void paint(Graphics g) {

        int width = this.frame.getContentPane().getWidth();
        int height = this.frame.getContentPane().getHeight();

        double hScale = (width-2.0*border)/MelaMigration.dimensions[2];
        double vScale = (height-2.0*border)/MelaMigration.dimensions[3];

        cell c;

        Graphics2D g2d = (Graphics2D)g;

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(4F));
        g2d.drawLine((int) (hScale*ms.xMax), 0, (int) (hScale*ms.xMax), 200);

        g2d.setStroke(new BasicStroke(3F));
        for(int i = 0; i<environment.profile.length; i++){
            for(int j = 0; j<environment.profile[i].length; j++){
                g2d.setColor(new Color((int) Math.max(0, Math.min(250,(100.0 * environment.profile[i][j]))),0,0));
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
    }
}
