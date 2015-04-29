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

    public static double grain = 20.0;
    public double[][] profile;
    public double[][] oldprofile;
    public Complex[][] fProfile;
    public Complex[][] kernel;
    public static double production  = 0.0;
    public static double degradation = 0.00; // Equilibrium values of 20.
    Fourier2D f2d;

    public ChemicalEnvironment(double dMin, double dMax){

        int iMax = ((int) (MelaMigration.dimensions[2]/grain))-1;
        profile = new double[iMax][];

        for(int i = 0; i<iMax; i++){

            double[] dNew = new double[((int) (MelaMigration.dimensions[3]/grain))];
            for(int j = 0; j<dNew.length; j++){
                if(i<=(MigrationSimulation.padding/grain)) dNew[j] = 0.0;
                else dNew[j] = dMin+ (((double) i)/iMax)*(dMax-dMin);
            }
            profile[i] = dNew;
        }

        for(int i = 0; i<iMax; i++){

        double[] dNew = new double[((int) (MelaMigration.dimensions[3]/grain))];
        for(int j = 0; j<dNew.length; j++){
            if(i<=(MigrationSimulation.padding/grain)) dNew[j] = 0.0;
            //else if(i==iMax-1) dNew[j] = dMax;
            else dNew[j] = dMin+ (((double) i)/iMax)*(dMax-dMin);
        }
        profile[i] = dNew;
    }

        oldprofile = profile.clone();

        if(MelaMigration.convolutionDiffusion){
            f2d = new Fourier2D(profile.length,profile[0].length);
            fProfile = new Complex[profile.length][profile[0].length];
            kernel   = createKernel(profile.length,profile[0].length,Math.sqrt(MelaMigration.dt*MigrationSimulation.DiffC/grain));

            for(int i=0; i<fProfile.length; i++){
                for(int j=0;j<fProfile[i].length; j++){
                    fProfile[i][j] = new Complex(0,0);
                }
            }
        }
    }

    private int[] ConvertCoordinates(double cx, double cy){

        int x = (int) Math.floor(cx/grain);
        int y = (int) Math.floor(cy/grain);

        x = Math.max(0,Math.min(x,profile.length-1));
        y = Math.max(0,Math.min(y,profile[x].length-1));

        return new int[]{x,y};
    }

    public void TakeAtLocation(double dx, double dy, double amount){
        int[] ic = ConvertCoordinates(dx,dy);

        profile[ic[0]][ic[1]] -=amount;
    }

    public void add(double cn){
        for(int i=0; i<profile.length; i++){
            for(int j=0; j<profile[i].length; j++){
                profile[i][j]+=cn;
            }
        }
    }

    public double GetLocationConcentration(double dx, double dy){
        int[] ic = ConvertCoordinates(dx,dy);
        return profile[ic[0]][ic[1]];
    }

    public double[] GetGradientAtLocation(double dx, double dy){
        int[] ic = ConvertCoordinates(dx,dy);
        double xDiff = profile[Math.min(ic[0]+1, profile.length-1)][ic[1]] - profile[(Math.max(ic[0]-1, 0))][ic[1]];
        double yDiff = profile[ic[0]][Math.min(ic[1]+1, profile[ic[0]].length-1)] - profile[ic[0]][Math.max(ic[1]-1, 0)];

        return new double[]{xDiff/(2*grain),yDiff/(2*grain)};
    }


    public double GetConcentrationAtLocation(double dx, double dy){
        int[]  ic = ConvertCoordinates(dx,dy);

        double c0 = profile[ic[0]][ic[1]];

        /*c0       += profile.get(Math.min(ic[0]+1, profile.size()-1))[ic[1]];
        c0       += profile.get(Math.max(ic[0]-1, 0))[ic[1]];
        c0       += profile.get(ic[0])[Math.min(ic[1]+1, profile.get(ic[0]).length-1)]
        c0       += profile.get(ic[0])[Math.max(ic[1]-1, 0)];        */

        return c0/5;
    }

    public void diffuse(double D, double dt,double dx){
        int iB = MelaMigration.pinned ? 1 : 0;
        if(MelaMigration.convolutionDiffusion){
            for(int i=0;i<profile.length;i++){
                for(int j=0; j<profile[0].length; j++){
                    fProfile[i][j].real = profile[i][j];
                    fProfile[i][j].imag = 0;
                }
            }
            fProfile = f2d.fft(fProfile);
            for(int i=0;i<profile.length;i++){
                for(int j=0; j<profile[0].length; j++){
                    fProfile[i][j].mult(kernel[i][j]);
                }
            }
            fProfile = f2d.ifft(fProfile);



            for(int i=0;i<profile.length;i++){
                for(int j=0; j<profile[0].length; j++){
                    profile[i][j] = profile.length*profile[0].length*fProfile[i][j].real;
                }
            }

            for(int i=0;i<profile.length;i++){
                for(int j=0; j<profile[0].length; j++){
                    fProfile[i][j].real = profile[i][j];
                    fProfile[i][j].imag = 0;
                }
            }
            fProfile = f2d.fft(fProfile);
            for(int i=0;i<profile.length;i++){
                for(int j=0; j<profile[0].length; j++){
                    fProfile[i][j].mult(kernel[i][j]);
                }
            }
            fProfile = f2d.ifft(fProfile);

            for(int i=0;i<profile.length;i++){
                for(int j=0; j<profile[0].length; j++){
                    profile[i][j] = profile.length*profile[0].length*fProfile[i][j].real;
                }
            }


            //System.out.println("Fourier");
        }
        /*if(MelaMigration.CNM){
            //Crank-Nicolson,
            for(int i = 0; i<profile.length; i++){
                for(int j = 0; j<profile[i].length; j++){
                    int yLength = oldprofile[i].length-1;
                    double ip1,im1,jp1,jm1;
                    ip1 = im1 = oldprofile[i][j];
                    jp1 =       oldprofile[i][0];
                    jm1 = oldprofile[i][yLength];


                    if(i > 0) im1 = oldprofile[i-1][j];
                    if(i < oldprofile.length-1) ip1 = oldprofile[i+1][j];
                    if(j > 0) jm1 = oldprofile[i][j-1];
                    if(j < yLength) jp1 = oldprofile[i][j+1];

                    double r = (D*dt/(dx*dx));


                    profile[i][j] += (D*dt/dx)*(ip1+im1+jp1+jm1-4*oldprofile[i][j]);
                }
            }
        }*/
        else{
            for(int i = iB; i<profile.length-iB; i++){
                for(int j = 0; j<profile[i].length; j++){
                    int yLength = oldprofile[i].length-1;
                    double ip1,im1,jp1,jm1;
                    ip1 = im1 = oldprofile[i][j];
                    jp1 =       oldprofile[i][0];
                    jm1 = oldprofile[i][yLength];


                    if(i > 0) im1 = oldprofile[i-1]   [j];
                    if(i < oldprofile.length-1) ip1 = oldprofile[i+1][j];
                    if(j > 0) jm1 = oldprofile[i][j-1];
                    if(j < yLength) jp1 = oldprofile[i][j+1];

                    profile[i][j] += (D*dt/(dx*dx))*(ip1+im1+jp1+jm1-4*oldprofile[i][j])+(ChemicalEnvironment.production-ChemicalEnvironment.degradation*profile[i][j])*dt;
                }
            }
        }
        oldprofile = profile.clone();
    }

    public double[] meanProfile(){
        double[] dOut = new double[profile.length];
        for(int i = 0; i<profile.length; i++){
            double dMean = 0;
            for(int j = 0; j<profile[i].length; j++){
                dMean+=profile[i][j];
            }
            dMean/=profile[i].length;
            dOut[i] = dMean;
        }
        return dOut;
    }

    private Complex[][] createKernel(int dx, int dy, double dsig){

        int ixm = (int) Math.ceil(dx/2);
        int iym = (int) Math.ceil(dy/2);

        Complex[][] kernel = new Complex[dx][dy];

        double total = 0;

        for(int i=0; i<dx; i++){
            for(int j=0; j<dy; j++){
                double dVal = Math.exp(-((i-ixm)*(i-ixm))/(2*dsig*dsig))
                            * Math.exp(-((j-iym)*(j-iym))/(2*dsig*dsig));
                kernel[i][j] = new Complex();
                kernel[i][j].real = dVal;
                kernel[i][j].imag = 0;
                total+=dVal;
            }
        }
        BufferedImage bi = new BufferedImage(dx,dy,1);

        for(int i=0; i<dx; i++){
            for(int j=0; j<dy; j++){
                double r = (150.0*(Math.sqrt(Math.sqrt(kernel[i][j].real*kernel[i][j].real+kernel[i][j].imag*kernel[i][j].imag))));
                bi.setRGB(i,j,((int)r<<16)|(0<<8)|0);
                kernel[i][j].real/=total;
            }
        }
        kernel = f2d.fft(kernel);

        ImageIcon icon = new ImageIcon(bi);
        JOptionPane.showMessageDialog(null,icon);

        return kernel;
    }
}
