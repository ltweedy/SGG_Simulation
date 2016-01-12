import sun.nio.cs.ext.TIS_620;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: luke
 * Date: 19/02/2014
 * Time: 13:36
 * To change this template use File | Settings | File Templates.
 */
public class MigrationSimulation {

    public static double padding = 0.5;
    public static double absorption = 1;
    public static double CIbMax = 50;


    public double Ttotal = 0;
    public double xMax   = MelaMigration.dimensions[0];
    public double yMax   = MelaMigration.dimensions[1];
    public double drx    = 0.0;
    public double sigma  = 0.0;
    public BufferedWriter bw;
    public FileWriter     fw;
    public BufferedWriter bw2;
    public FileWriter     fw2;
    public BufferedWriter bw3;
    public FileWriter     fw3;
    public BufferedWriter bw4;
    public FileWriter     fw4;
    public static double kD     = 0.02;
    public static double[] DiffC  = new double[] {12000, 1200};

    public static double boundFraction = 1.0;

    public static double kM     = 0.7;
    public static double sMax   = 30.0;
    public static double gk     = 0.00;//0228018;
    public int stepCount = 0;
    public double[] dList;
    public static double u1 = 1.0;
    public static double u2 = 0.0;

    public static double kRec = 0.002;
    public static double rRec = 0.2;
    public boolean paused  = false;
    public boolean induced = false;


    ArrayList<cell> randomCells = new ArrayList<cell>();


    List<cell> cells;
        List<cell> newCells;
        QuadTree QT = new QuadTree(0, new DoubleRectangle(0.0,0.0,MelaMigration.dimensions[2],MelaMigration.dimensions[3]));

        public boolean proliferate  = true;
        public boolean dieoff       = false;
        public boolean contact      = true;
        public boolean absorber     = false;
        public double [][] births;

        Random RG = new Random();

        public ChemicalEnvironment environment;
        public cellPlotter cp;
        public ControlPanel controlPanel;

        public JFrame frame;

        public MigrationSimulation(boolean proliferate, boolean die, boolean contact,
                               boolean absorber, double min, double max, double alpha,double speed,double dt, double dx,double Diff,double nkD,double nkM, double nsMax){

            this.controlPanel = new ControlPanel(this);
            this.cells = new ArrayList<cell>();
            this.newCells = new ArrayList<cell>();

            for(int i = 0; i<MelaMigration.pop; i++){
                cells.add(new cell(new double[]{10+1000*(Math.random()), 1.0*MelaMigration.dimensions[1]*Math.random()}, this));
            }

            this.proliferate    = proliferate;
            this.dieoff         = die;
            this.absorber       = absorber;
            this.contact        = contact;

            MigrationSimulation.DiffC[0] = Diff;
            cell.speed = speed;
            MelaMigration.dt = dt;
            MelaMigration.rdt = Math.sqrt(dt);
            ChemicalEnvironment.grain = dx;
            MigrationSimulation.kD = nkD;
            MigrationSimulation.kM = nkM;
            MigrationSimulation.sMax = nsMax;

            this.environment    = new ChemicalEnvironment(min, max);

            if(alpha<0)       alpha = 0;
            else if(alpha>1) alpha  = 1.0;

            sigma = Math.sqrt(-Math.log(alpha*alpha));

            dList = new double[cells.size()];


            if(alpha<0)       alpha = 0;
            else if(alpha>1) alpha  = 1.0;

            if(MelaMigration.visualise){
                SetupFrames();
            }
            String sNum = "";

            if(MelaMigration.record){
                String output = MelaMigration.directory;
                File f = new File(output);
                boolean bDir = f.mkdirs();
                sNum = Integer.toString(f.list().length);
                if(f.list().length<10) sNum = "00"+sNum;
                else if(f.list().length<100) sNum = "0"+sNum;

                if(!bDir) System.out.println("Failed to create "+output);
                try{
                    fw  = new FileWriter(output+"HexSim"   +sNum+".txt");
                    fw2 = new FileWriter(output+"Receptors"+sNum+".txt");
                    fw3 = new FileWriter(output+"EnvironmentA"+sNum+".txt");
                    fw4 = new FileWriter(output+"EnvironmentB"+sNum+".txt");
                    bw  = new BufferedWriter(fw);
                    bw2 = new BufferedWriter(fw2);
                    bw3 = new BufferedWriter(fw3);
                    bw4 = new BufferedWriter(fw4);
                    String          sWrite = "";
                    if(proliferate) sWrite+="P";
                    if(contact)     sWrite+="C";
                    if(absorber)    sWrite+="A";
                    sWrite+=", alpha = " + Double.toString(alpha) + ",   ";
                    sWrite+=" " + Double.toString(min) + " -> " + Double.toString(max);
                    sWrite+=", D = " + Double.toString(DiffC[0]) + ", kD = " + Double.toString(kD);
                    sWrite+=", sMax = " + Double.toString(sMax) + ", kM = " + Double.toString(kM);
                    sWrite+=", yMax = " + Double.toString(yMax) + ", P = " + Double.toString(MelaMigration.pop);

                    bw.write(sWrite);
                    bw.newLine();
                    sWrite = "";
                    for(cell c : cells)  sWrite += c.dgr? "1": "0";

                    bw.write(sWrite);
                    bw.newLine();
                }

                catch(IOException e){e.printStackTrace();}
            }
        }

        public MigrationSimulation(){

            this.cells = new ArrayList<cell>();
            this.newCells = new ArrayList<cell>();

            for(int i = 0; i<MelaMigration.pop; i++){
                cells.add(new cell(new double[]{padding + 0.1*Math.random(), MelaMigration.dimensions[1]*Math.random()}, this));
            }
            setupEnvironment();

            if(MelaMigration.visualise){
                SetupFrames();
            }
        }

        public void setupEnvironment(){
            this.environment = new ChemicalEnvironment(0.0,0.0);
        }

        public void SetupFrames(){
            this.frame = new JFrame();
            cp = new cellPlotter(cells, environment, this, frame);
            this.frame.getContentPane().add(cp);
            this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.frame.setSize(1000, 100);
            this.frame.setLocation(210,MelaMigration.yPos);
            MelaMigration.yPos+=200;

            this.frame.setVisible(true);
        }

        public synchronized void iterateSimulation(){

            if(paused) try{Thread.currentThread().sleep(1000);}catch(Exception e){};

            //Collections.shuffle(cells);

            stepCount++;
            drx = gk*xMax*MelaMigration.dt;
            double xMax_m1 = xMax;
            xMax += drx;

            long T0 = System.currentTimeMillis();



            if(absorber) environment.diffuse();
            for(int i = cells.size()-1; i>=0; i--){

                cell c = cells.get(i);
                if(absorber) {
                    double cn = environment.GetLocationConcentration(c.x(), c.y());
                    if(induced) environment.TakeAtLocation(c.x(), c.y(), boundFraction * absorption * ((cell.width * cell.width) / (ChemicalEnvironment.grain * ChemicalEnvironment.grain)) * MelaMigration.dt * (0.25*(c.oB+c.oF+c.oU+c.oL)*sMax * cn / (cn + kM)));
                    else environment.TakeAtLocation(c.x(), c.y(), boundFraction * absorption * ((cell.width * cell.width) / (ChemicalEnvironment.grain * ChemicalEnvironment.grain)) * MelaMigration.dt * (sMax * cn / (cn + kM)));
                }
            }
            if(absorber) environment.react();

            if(contact) QT.clear();

            for(int i = cells.size()-1; i>=0; i--) {

                cell c = cells.get(i);
                c.position[0] += drx * (c.position[0] / xMax_m1);       // Add drift from domain growth (if any).
                //clear forces from last iteration
                c.clear();
                c.secreteEnzyme();
                //add Brownian and driven chemotactic forces
                double[] direction = this.getBiasedDirection(c);

                double distance = cell.speed * MelaMigration.dt/*Math.pow(MelaMigration.dt, H)*/;
                //dList[i] = distance;
                double dx = direction[0] * distance;
                double dy = direction[1] * distance;
                c.addForce(dx, dy);
                //Add proliferation
                c.updateGrowth();
                //Add to quad tree
                if (contact) QT.insert(c);
            }

            //if(Math.random()<0.01) System.out.println((MyMaths.avg(dList)/MelaMigration.dt));
            //Resolve quad tree interactions

            randomCells.addAll(cells);
            Collections.shuffle(randomCells);

            if(contact){
                for(int i = randomCells.size()-1; i>=0; i--){
                    cell c = randomCells.get(i);

                    ArrayList<cell> interactions = new ArrayList<cell>();
                    QT.retrieve(interactions, c);

                    for(int j = 0; j<interactions.size(); j++){
                        cell c2 = interactions.get(j);
                        if(!c.equals(c2)){
                            double[] dp = new double[] {c2.x()-c.x(), c2.y()-c.y()};

                            if((dp[0]*dp[0]+dp[1]*dp[1])<(cell.width*cell.width)){
                                double[] dpn = MyMaths.normalised(dp);
                                double dot = MyMaths.dotProduct(dpn, c.force);
                                 if(dot>0.0) c.addForce(-dot*dpn[0], -dot*dpn[1]);
                            }
                        }
                    }
                }
            }


            //Update positions based on final forces
            for(int i = randomCells.size()-1; i>=0; i--){
                cell c = randomCells.get(i);
                c.updatePosition();
                c.ld = Math.atan2(c.fy(),c.fx());
            }

            randomCells.clear();

            //add cells that have proliferated to the list
            cells.addAll(this.newCells);
            births = new double[this.newCells.size()][2];
            for(int i = 0; i<births.length; i++){
                cell c = newCells.get(i);
                births[i][0] = c.x();
                births[i][1] = c.y();
            }

            if(stepCount%100==0){
                try{bw2.newLine();} catch(IOException e){}
            }
            /*if(stepCount%180000==0){
                System.out.println("Puff!");
                environment.add(100);
            }*/
            newCells = new ArrayList<cell>();
        }

        public synchronized void draw(){
          //  frame.update(frame.getGraphics());
          //  frame.validate();
            frame.repaint();
        }

        public double[] getBiasedDirection(cell c){   // This determines the direction of motion-

            //Determine CI based directional biases.
            double   cp1x = environment.GetLocationConcentration(c.x() + 0.5*cell.width, c.y());
            double   cm1x = environment.GetLocationConcentration(c.x() - 0.5*cell.width, c.y());
            double   cp1y = environment.GetLocationConcentration(c.x(), c.y() + 0.5*cell.width);
            double   cm1y = environment.GetLocationConcentration(c.x(), c.y() - 0.5*cell.width);
            double   c0   = environment.GetLocationConcentration(c.x(), c.y());

            //System.out.println("Grad -> "+ (cp1x-cm1x)+":"+(cp1y-cm1y));

            c.oF = 0.5*(cp1x+c0)/(0.5*(cp1x+c0)+kD);
            c.oB = 0.5*(cm1x+c0)/(0.5*(cm1x+c0)+kD);

            c.oU = 0.5*(cp1y+c0)/(0.5*(cp1y+c0)+kD);
            c.oL = 0.5*(cm1y+c0)/(0.5*(cm1y+c0)+kD);

            // COMMENT HERE TO REMOVE RECEPTOR KINETICS FROM CHEMOTAXIS

            double sx = (0.5*(cp1x+c0)/(0.5*(cp1x+c0)+kD) - 0.5*(cm1x+c0)/(0.5*(cm1x+c0)+kD));
            double sy = (0.5*(cp1y+c0)/(0.5*(cp1y+c0)+kD) - 0.5*(cm1y+c0)/(0.5*(cm1y+c0)+kD));
            /*double sT = cp1x/(cp1x+kD)+cm1x/(cm1x+kD)+cp1y/(cp1y+kD)+cm1y/(cm1y+kD)+c0/(c0+kD);

            if(sT<0.005){
                sx = 0;
                sy = 0;
            }
            else{
                double rt = Math.sqrt(sT);
                sx /=rt;
                sy /=rt;
            }*/


            // UNCOMMENT HERE TO REMOVE RECEPTOR KINETICS FROM CHEMOTAXIS

            //double sx = (cp1x-cm1x);
            //double sy = (cp1y-cm1y);

            //System.out.println(cp1x+":"+cm1x);
            //System.out.println("s(x,y) -> "+ sx+".x + "+sy+".y");

            // Random direction -> bias~1 / s.d.
            // Bias induced by persistence.
            double th;

            if(MelaMigration.alpha<0.000001
            || c.ld == 10)  th = -Math.PI+Math.random()*2.0*Math.PI;
            else if(MelaMigration.alpha >= 1) th = c.ld;

            else            th = MyMaths.bounded(-Math.PI, Math.PI, c.ld+(MelaMigration.rdt*sigma)*RG.nextGaussian());



            double xDir = (Math.cos(th)+c.CIb*sx*MelaMigration.dt);
            double yDir = (Math.sin(th)+c.CIb*sy*MelaMigration.dt);



            /*double xDir;
            double yDir;
            //System.out.println("theta = "+th);
            if(c.ld == 10){
                th = -Math.PI+RG.nextDouble()*2.0*Math.PI;
                xDir = (Math.cos(th)+c.CIb*sx);
                yDir = (Math.sin(th)+c.CIb*sy);
            }
            else{
                double rD = (-Math.PI+RG.nextDouble()*2.0*Math.PI);
                xDir = u1*Math.cos(c.ld) + u2*Math.cos(rD)+c.CIb*sx;
                yDir = u1*Math.sin(c.ld) + u2*Math.sin(rD)+c.CIb*sy;
            }         */


            //c.CIb+=(kRec*(CIbMax-c.CIb)/CIbMax - rRec*(c.oB+c.oF+c.oU+c.oL)/4)*MelaMigration.dt;
            c.CIb = Math.max(c.CIb,0);

            Ttotal++;
            return MyMaths.normalised(new double[]{xDir,yDir});

        };
}
