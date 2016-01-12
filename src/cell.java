/**
 * Created with IntelliJ IDEA.
 * User: luke
 * Date: 18/02/2014
 * Time: 08:46
 * To change this template use File | Settings | File Templates.
 */
import java.awt.*;
import java.util.Random;

public class cell {
    public double[] position;
    public double[] force;
    public MigrationSimulation sim;
    public static double speed = 20.0;  // um/min
    public static double rspeed = Math.sqrt(speed);  // um/min
    public static double kBIRTH = 0.00031;
    public static double kDEATH = 0.00002;
    public static Random RG = new Random();
    public boolean original;
    public DoubleRectangle box;
    public static double width = 10.0;     //Cells 20 um across
    public double ld  = 10.0;
    public double CIb = MigrationSimulation.CIbMax;
    public double oF = 0;
    public double oB = 0;
    public double oU = 0;
    public double oL = 0;
    public boolean dgr = true;




    //public double[] velocity;

    public double x(){ return position[0];}
    public double y(){ return position[1];}
    public double fx(){ return force[0];}
    public double fy(){ return force[1];}



    public cell(double[] position, MigrationSimulation sim){

        this.position = position;
        this.force = new double[]{0.0,0.0};
        this.box = new DoubleRectangle(position[0]-width/2.0,
                                       position[1]-width/2,
                                       width,
                                       width   //width and height the same.
                                );

        this.sim = sim;
        this.original = true;
    }

    public void clear(){
        force[0] = 0.0;
        force[1] = 0.0;

    }

    public void updatePosition(){

        this.position[0]+=force[0];
        this.position[1]+=force[1];
        this.box.x = (position[0]-width/2);
        this.box.y = (position[1]-width/2);

        if(this.position[1]<MigrationSimulation.padding||this.position[1]> sim.yMax-MigrationSimulation.padding) {
            this.position[1] = MyMaths.bounded(MigrationSimulation.padding, sim.yMax - MigrationSimulation.padding, this.position[1]);
        }
        //if(this.position[1]<MigrationSimulation.padding) this.position[1] = 2.0*MigrationSimulation.padding-this.position[1];
        //else if(this.position[1]> sim.yMax-MigrationSimulation.padding) this.position[1] = 2.0* (sim.yMax-MigrationSimulation.padding) - this.position[1];


        if(this.position[0]<MigrationSimulation.padding) this.position[0] = 2.0*MigrationSimulation.padding-this.position[0];
        else if(this.position[0]> sim.xMax-MigrationSimulation.padding){
            this.position[0] = 2.0* (sim.xMax-MigrationSimulation.padding) - this.position[0];
            //System.out.println((sim.Ttotal/60));
        }

    }

    public void addForce(double dx, double dy){
        force[0]+=dx;
        force[1]+=dy;
    }

    public void updateGrowth(){

        if(sim.dieoff)      this.cull(0.0,0.0,4.0);
        if(sim.proliferate) this.split(0.0,0.0,MelaMigration.dimensions[0]);

    }


    public synchronized void cull(double a, double b, double c){

        double t1 = a*x()*x();
        double t2 = b*x();
        double t3 = c;

        if((kDEATH*MelaMigration.dt>Math.random())&&((t1+t2+t3)<Math.random()*MelaMigration.dimensions[0])){
            sim.cells.remove(this);
        }
    }

    public void secreteEnzyme(){
        sim.environment.AddDegraderAtLocation(x(),y(), 0.01*(1.0-MigrationSimulation.boundFraction)*MelaMigration.dt);
    }

    public synchronized void split(double a, double b, double c){

        if((kBIRTH*MelaMigration.dt>Math.random())/*&&((t1+t2+t3)>Math.random()*MelaMigration.dimensions[0])*/){
            cell clone = new cell(new double[] {position[0] + width*RG.nextGaussian(), position[1] + width*RG.nextGaussian()},this.sim);
            clone.position[1] = MyMaths.bounded(0.0, MelaMigration.dimensions[1]-0.0, clone.position[1]);
            if(clone.position[0]<0.1) clone.position[0] = 0.2-clone.position[0];
            else if(clone.position[0]> MelaMigration.dimensions[0]-0.1) clone.position[0] = 2.0* (MelaMigration.dimensions[0]-0.1) - clone.position[0];


            sim.newCells.add(clone);
            clone.original = false;
        }
    }
}
