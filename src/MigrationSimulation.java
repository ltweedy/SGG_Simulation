import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: luke
 * Date: 19/02/2014
 * Time: 13:36
 * To change this template use File | Settings | File Templates.
 */
public class MigrationSimulation {

    public static double padding = 0.5;
    public static double absorption = 0.02;
    public static double CIbMax = 0.05;

    public double Ttotal = 0;
    public double xMax   = MelaMigration.dimensions[0];
    public double yMax   = MelaMigration.dimensions[1];
    public double drx    = 0.0;
    public double alpha  = 0.0;
    public double sigma  = 0.5;
    public BufferedWriter bw;
    public FileWriter     fw;
    public BufferedWriter bw2;
    public FileWriter     fw2;
    public BufferedWriter bw3;
    public FileWriter     fw3;
    public static double kD     = 0.02;
    public static double DiffC  = 15000.0;
    public static double kM     = 0.02;
    public static double sMax   = 100.0;
    public static double gk     = 0.00;//0228018;
    public int stepCount = 0;
    public double[] dList;
    public static double u1 = 1.0;
    public static double u2 = 0.0;




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
        private cellPlotter cp;
        public JFrame frame;

        public MigrationSimulation(boolean proliferate, boolean die, boolean contact,
                               boolean absorber, double min, double max, double alpha,double speed,double dt, double dx,double Diff,double nkD,double nkM, double nsMax){

            this.cells = new ArrayList<cell>();
            this.newCells = new ArrayList<cell>();

            for(int i = 0; i<MelaMigration.pop; i++){
                cells.add(new cell(new double[]{padding+100*(Math.random()), 1.0*MelaMigration.dimensions[1]*Math.random()}, this));

            }

            this.proliferate    = proliferate;
            this.dieoff         = die;
            this.absorber       = absorber;
            this.contact        = contact;

            MigrationSimulation.DiffC = Diff;
            cell.speed = speed;
            MelaMigration.dt = dt;
            MelaMigration.rdt = Math.sqrt(dt);
            ChemicalEnvironment.grain = dx;
            MigrationSimulation.kD = nkD;
            MigrationSimulation.kM = nkM;
            MigrationSimulation.sMax = nsMax;

            this.environment    = new ChemicalEnvironment(min, max);
            this.alpha          = alpha;

            if(alpha<0)       alpha = 0;
            else if(alpha>1) alpha  = 1.0;

            /*if(alpha<1){
                double[][] cov = new double[2][];

                System.out.println("correlation per dt: "+Math.pow(alpha,MelaMigration.dt));

                cov[0] = new double[]{1,Math.pow(alpha,MelaMigration.dt)};
                cov[1] = new double[]{Math.pow(alpha,MelaMigration.dt),1};

                double[][] cM = Cholesky.cholesky(cov);

                //this.H              = (alpha+1)/2;
                //this.gaussianWidth  = 0.871/Math.pow(-1 - (1/(1-2*H)), 0.4605);

                System.out.println(cM[0][0]+" | "+cM[1][0]);
                System.out.println("----------------------------");
                System.out.println(cM[0][1]+" | "+cM[1][1]);

                u1 = cM[1][0];
                u2 = cM[1][1];

            }   */

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
                    fw3 = new FileWriter(output+"Environment"+sNum+".txt");
                    bw  = new BufferedWriter(fw);
                    bw2 = new BufferedWriter(fw2);
                    bw3 = new BufferedWriter(fw3);
                    String          sWrite = "";
                    if(proliferate) sWrite+="P";
                    if(contact)     sWrite+="C";
                    if(absorber)    sWrite+="A";
                    sWrite+=", alpha = " + Double.toString(alpha) + ",   ";
                    sWrite+=" " + Double.toString(min) + " -> " + Double.toString(max);
                    sWrite+=", D = " + Double.toString(DiffC) + ", kD = " + Double.toString(kD);
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
            this.frame.getContentPane().add(new cellPlotter(cells, environment, this, frame));
            this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.frame.setSize(1000, 400);
            this.frame.setLocation(10,MelaMigration.yPos);
            MelaMigration.yPos+=200;

            this.frame.setVisible(true);
        }

        public synchronized void iterateSimulation(){


            stepCount++;
            drx = gk*xMax*MelaMigration.dt;
            double xMax_m1 = xMax;
            xMax += drx;

            if(absorber) environment.diffuse(DiffC, MelaMigration.dt, ChemicalEnvironment.grain);
            if(contact) QT.clear();

            for(int i = cells.size()-1; i>=0; i--){
                cell c = cells.get(i);
                c.position[0] += drx*(c.position[0]/xMax_m1);
                //clear forces from last iteration
                c.clear();
                //add Brownian and driven chemotactic forces
                double[] direction = this.getBiasedDirection(c);
                double distance = Math.abs(RG.nextGaussian())*cell.speed*MelaMigration.dt/*Math.pow(MelaMigration.dt, H)*/;
                //dList[i] = distance;
                double dx = direction[0]*distance;
                double dy = direction[1]*distance;
                c.addForce(dx,dy);
                //Add proliferation
                c.updateGrowth();
                //Add to quad tree
                if(contact) QT.insert(c);
            }
            //if(Math.random()<0.01) System.out.println((MyMaths.avg(dList)/MelaMigration.dt));
            //Resolve quad tree interactions
            if(contact){
                for(int i = cells.size()-1; i>=0; i--){
                    cell c = cells.get(i);

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
            for(int i = cells.size()-1; i>=0; i--){
                cell c = cells.get(i);
                c.updatePosition();
                c.ld = Math.atan2(c.fy(),c.fx());
            }

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
            frame.update(frame.getGraphics());
            frame.validate();
            frame.repaint();
        }

        public double[] getBiasedDirection(cell c){   // This determines the direction of motion-

            //Determine CI based directional biases.
            double   cp1x = environment.GetConcentrationAtLocation(c.x()+ChemicalEnvironment.grain,c.y());
            double   cm1x = environment.GetConcentrationAtLocation(c.x()-ChemicalEnvironment.grain,c.y());
            double   cp1y = environment.GetConcentrationAtLocation(c.x(),c.y()+ChemicalEnvironment.grain);
            double   cm1y = environment.GetConcentrationAtLocation(c.x(),c.y()-ChemicalEnvironment.grain);

            //System.out.println("Grad -> "+ (cp1x-cm1x)+":"+(cp1y-cm1y));

            c.oF = cp1x/(cp1x+kD);
            c.oB = cm1x/(cm1x+kD);

            double sx = cp1x/(cp1x+kD) - cm1x/(cm1x+kD);
            double sy = cp1y/(cp1y+kD) - cm1y/(cm1y+kD);
            //double sx = cp1x-cm1x;
            //double sy = cp1y-cm1y;
            //System.out.println("s(x,y) -> "+ sx+".x + "+sy+".y");

            // Random direction -> bias~1 / s.d.
            // Bias induced by persistence.
            double th;


            if(alpha<0.000001
            || c.ld == 10)  th = -Math.PI+Math.random()*2.0*Math.PI;
            else if(alpha >= 1) th = c.ld;
            else            th = MyMaths.bounded(-Math.PI, Math.PI, c.ld+(MelaMigration.rdt*sigma)*RG.nextGaussian());

            double xDir = (Math.cos(th)+c.CIb*sx);
            double yDir = (Math.sin(th)+c.CIb*sy);


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
            if(absorber){
                double cn = environment.GetLocationConcentration(c.x(),c.y());
                if(c.dgr) environment.TakeAtLocation(c.x(),c.y(), absorption*((cell.width*cell.width)/(ChemicalEnvironment.grain*ChemicalEnvironment.grain))*MelaMigration.dt*(sMax*cn/(cn+kM)));
            }


            //c.CIb+=(0.003*(CIbMax-c.CIb)-0.003*(cp1x+cm1x+cp1y+cm1y)/4)*MelaMigration.dt;
            c.CIb = Math.max(c.CIb,0);

            Ttotal++;
            return MyMaths.normalised(new double[]{xDir,yDir});

        };
}
