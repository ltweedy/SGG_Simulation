import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: luke
 * Date: 19/02/2014
 * Time: 08:23
 * To change this template use File | Settings | File Templates.
 */
public class ChemicalEnvironment {

    public static double grain = 40.0;
    public double[][][] profile;
    public double[][][] oldprofile;
    public static double production  = 0.0;
    public static double degradation = 0.0; // Equilibrium values of 20.

    public double dMin = 0.0;
    public double dMax = 0.0;

    public ChemicalEnvironment(double dMin, double dMax){

        this.dMin = dMin;
        this.dMax = dMax;

        int iMax = ((int) (MelaMigration.dimensions[2]/grain));
        profile = new double[iMax][][];

        for(int i = 0; i<iMax; i++){

            double[][] dNew = new double[((int) (MelaMigration.dimensions[3]/grain))][2];
            for(int j = 0; j<dNew.length; j++){
                //if(i<=(MigrationSimulation.padding/grain)) dNew[j][0] = 0.0;
                //else
                dNew[j][0] = dMin+ (((double) i)/iMax)*(dMax-dMin);
                //if(i<1000/grain)        dNew[j][0] = 0;
            }
            profile[i] = dNew;
        }

        /*for(int i = 0; i<iMax; i++){

            double[][] dNew = new double[((int) (MelaMigration.dimensions[3]/grain))][2];
            for(int j = 0; j<dNew.length; j++){
                if(i<=(MigrationSimulation.padding/grain)) dNew[j][0] = 0.0;
                //else if(i==iMax-1) dNew[j] = dMax;
                else dNew[j][0] = dMin+ (((double) i)/iMax)*(dMax-dMin);
            }
            profile[i] = dNew;
        }
        */

        oldprofile = profile.clone();
    }


    private double[] ConvertCoordinatesToDouble(double cx, double cy){

        double x = cx/grain;
        double y = cy/grain;

        x = Math.max(0.000001,Math.min(x,profile.length-1)-0.000001);

        if(y<0)                   y += profile[0].length-1;
        if(y>profile[0].length-1) y -= profile[0].length-1;

        return new double[]{x,y};
    }

    private int[] ConvertCoordinates(double cx, double cy){

        int x = (int) Math.floor(cx/grain);
        int y = (int) Math.floor(cy/grain);

        x = Math.max(0,Math.min(x,profile.length-1));
        y = Math.max(0, Math.min(y, profile[x].length - 1));

        return new int[]{x,y};
    }

    public void TakeAtLocation(double dx, double dy, double amount){

        double[] dc = ConvertCoordinatesToDouble(dx,dy);

        double dx1 =  dc[0]-Math.floor(dc[0]);
        double dx2 = -dc[0]+Math.ceil(dc[0]);
        double dy1 =  dc[1]-Math.floor(dc[1]);
        double dy2 = -dc[1]+Math.ceil(dc[1]);

        double c11 = profile[(int)Math.floor(dc[0])][(int)Math.floor(dc[1])][0];
        double c12 = profile[(int)Math.floor(dc[0])][(int)Math.ceil(dc[1])][0];
        double c21 = profile[(int)Math.ceil(dc[0])][(int)Math.floor(dc[1])][0];
        double c22 = profile[(int)Math.ceil(dc[0])][(int)Math.ceil(dc[1])][0];

        double a11 = Math.min(c11, (amount*dx2*dy2));
        double a12 = Math.min(c12, (amount*dx2*dy1));
        double a21 = Math.min(c21, (amount*dx1*dy2));
        double a22 = Math.min(c22, (amount*dx1*dy1));

        //profile[(int)Math.floor(dc[0])][(int)Math.floor(dc[1])][0] -= amount;

        profile[(int)Math.floor(dc[0])][(int)Math.floor(dc[1])][0] -= a11;
        profile[(int)Math.floor(dc[0])][(int)Math.ceil(dc[1])][0]  -= a12;
        profile[(int)Math.ceil(dc[0])][(int)Math.floor(dc[1])][0]  -= a21;
        profile[(int)Math.ceil(dc[0])][(int)Math.ceil(dc[1])][0]   -= a22;

    }

    public void AddDegraderAtLocation(double dx, double dy, double amount){

        double[] dc = ConvertCoordinatesToDouble(dx,dy);

        double dx1 =  dc[0]-Math.floor(dc[0]);
        double dx2 = -dc[0]+Math.ceil(dc[0]);
        double dy1 =  dc[1]-Math.floor(dc[1]);
        double dy2 = -dc[1]+Math.ceil(dc[1]);

        double a11 = (amount*dx2*dy2);
        double a12 = (amount*dx2*dy1);
        double a21 = (amount*dx1*dy2);
        double a22 = (amount*dx1*dy1);

        profile[(int)Math.floor(dc[0])][(int)Math.floor(dc[1])][1] += a11;
        profile[(int)Math.floor(dc[0])][(int)Math.ceil(dc[1])][1]  += a12;
        profile[(int)Math.ceil(dc[0])][(int)Math.floor(dc[1])][1]  += a21;
        profile[(int)Math.ceil(dc[0])][(int)Math.ceil(dc[1])][1]   += a22;

    }



    public double GetLocationConcentration(double dx, double dy) {

        double[] dc = ConvertCoordinatesToDouble(dx, dy);

        double dx1 =  dc[0]-Math.floor(dc[0]);
        double dx2 = -dc[0]+Math.ceil(dc[0]);
        double dy1 =  dc[1]-Math.floor(dc[1]);
        double dy2 = -dc[1]+Math.ceil(dc[1]);

        double c11 = profile[(int)Math.floor(dc[0])][(int)Math.floor(dc[1])][1];
        double c12 = profile[(int)Math.floor(dc[0])][(int)Math.ceil(dc[1])][1];
        double c21 = profile[(int)Math.ceil(dc[0])][(int)Math.floor(dc[1])][1];
        double c22 = profile[(int)Math.ceil(dc[0])][(int)Math.ceil(dc[1])][1];

        //System.out.format("c11 -> %f, c12 -> %f, c21 -> %f,c22 -> %f%n",  c11,c12,c21,c22);
        //System.out.format("dx1 -> %f, dx2 -> %f, dy1 -> %f,dy2 -> %f%n",  dx1,dx2,dy1,dy2);

        return (c11*dx2*dy2 + c12*dx2*dy1 + c21*dx1*dy2 + c22*dx1*dy1);

        //int[] ic = ConvertCoordinates(dx,dy);
        //return profile[ic[0]][ic[1]][0];
    }

    public double[] GetGradientAtLocation(double dx, double dy){
        int[] ic = ConvertCoordinates(dx,dy);
        double xDiff = profile[Math.min(ic[0]+1, profile.length-1)][ic[1]][0] - profile[(Math.max(ic[0]-1, 0))][ic[1]][0];
        double yDiff = profile[ic[0]][Math.min(ic[1]+1, profile[ic[0]].length-1)][0] - profile[ic[0]][Math.max(ic[1]-1, 0)][0];

        return new double[]{xDiff/(2*grain),yDiff/(2*grain)};
    }

    public void diffuse(){
        //Explicit method

        double dx = grain;
        double dt = MelaMigration.dt;

        int iB = MelaMigration.pinned ? 1 : 0;
        oldprofile = profile.clone();

        for(int i = iB; i<profile.length-iB; i++){
            for(int j = 0; j<profile[i].length; j++){
                for(int k=0; k<profile[i][j].length; k++){

                    int yLength = oldprofile[i].length-1;
                    double ip1,im1,jp1,jm1;
                    ip1 = im1 = oldprofile[i][j][k];  //Neumann boundaries in x.

                    jp1 = oldprofile[i][0][k];
                    jm1 = oldprofile[i][yLength][k]; //Periodic boundaries in y.

                    if(i > 0) im1 = oldprofile[i-1][j][k];
                    if(i < oldprofile.length-1) ip1 = oldprofile[i+1][j][k];
                    if(j > 0) jm1 = oldprofile[i][j-1][k];
                    if(j < yLength) jp1 = oldprofile[i][j+1][k];

                    profile[i][j][k] += (MigrationSimulation.DiffC[k]*dt/(dx*dx))*(ip1+im1+jp1+jm1-4*oldprofile[i][j][k]);
                }
            }
        }
        if(MelaMigration.pinned){
            for(int j = 0; j<profile[0].length; j++){
                profile[0][j][0] = dMin;
                profile[profile.length-1][j][0] = dMax;
            }
        }
    }

    public void react(){
        for(int i=0; i<profile.length; i++){
            for(int j=0; j<profile[i].length; j++){
                double ca = profile[i][j][0];
                double cd = profile[i][j][1];

                profile[i][j][0] += (ChemicalEnvironment.production-ChemicalEnvironment.degradation*profile[i][j][0])*MelaMigration.dt;
                double amount = Math.min(profile[i][j][0], MelaMigration.dt*0.05*cd*ca*MigrationSimulation.sMax/(ca+MigrationSimulation.kM));
                profile[i][j][0] -=amount;
            }
        }

    }

    public double[] meanProfile(int iProfile){
        double[] dOut = new double[profile.length];
        for(int i = 0; i<profile.length; i++){
            double dMean = 0;
            for(int j = 0; j<profile[i].length; j++){
                dMean+=profile[i][j][iProfile];
            }
            dMean/=profile[i].length;
            dOut[i] = dMean;
        }
        return dOut;
    }
}
