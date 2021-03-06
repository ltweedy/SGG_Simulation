import sun.nio.cs.ext.TIS_620;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Created with IntelliJ IDEA.
 * User: luke
 * Date: 18/02/2014
 * Time: 08:41
 * To change this template use File | Settings | File Templates.
 */
public class MelaMigration {

    public static double  dimensions[] = {12000, 1500, 12000, 1500}; // um
    public static boolean visualise = true;
    public static boolean record = true;
    public static boolean exdeg  = true;

    public static int     yPos = 40;
    public static double  max = 10.0;
    public static double  min = 10.0;
    public static boolean abs = true;
    public static boolean pinned = false;
    public static boolean ctc = false;

    static int pop = 1500;                    // Initial population
    static double T  = 1*7*60+10;                // Days, Hours, Minutes
    public static double dt = 0.005;         // 0.05~30.0s
    public static double alpha = 0.85;
    public static double outInt = 10000;
    public static double rdt = Math.sqrt(dt);   // Sqrt of dt for brownian motion
    public static String directory = System.getProperty("user.home")+"/Science/Beatson/Collective Migration/MelanocyteSims/Main/";
    public static boolean finished = false;

    public Thread visualiser;
    public ExecutorService threadpool;
    ArrayList<Callable<Object>> tasks;

    public MigrationSimulation RWS;

    private MelaMigration(){
        threadpool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        tasks = new ArrayList<Callable<Object>>(4);

        if(record){
             boolean bDir = new File(directory).mkdirs();
         //   if(!bDir) System.out.println("Failed to create "+directory);
        }
        if(visualise){
           makeSimGUIs();
        }
        else{
            //RWS = new MigrationSimulation(false,false,ctc,abs,min,max,alpha);
        }
        tasks.add(Executors.callable(new Thread(){
            public void run(){
                int j = 0;
                for(int i=0; i<=T/dt; i++){
                    RWS.Ttotal+=dt;
                    //System.out.println((RWS.Ttotal/(60.0)));
                    RWS.iterateSimulation();
                    if(RWS.paused) i--;
                    //if(i%1==0){

                        RWS.draw();

                     //   RWS.frame.update(RWS.frame.getGraphics());
                     //   try {
                     //       Robot robot = new Robot();
                     //       // Capture the screen shot of the area of the screen defined by the rectangle
                     //       BufferedImage bi=robot.createScreenCapture(RWS.frame.getBounds());
                     //       ImageIO.write(bi, "png", new File("/Users/luke/Science/Beatson/Collective Migration/MelanocyteSims/t_" + Integer.toString((i/10)) + ".png"));
                     //
                     //   } catch (AWTException e) {
                     //       e.printStackTrace();
                     //   } catch (IOException e) {
                     //       e.printStackTrace();
                     //   }
                   // }
                    if(i%100==1) RWS.controlPanel.t_time.setText(Double.toString(dt*i/60.0));
                    if(j*dt>=outInt){


                        j = 0;
                        String sOut = Double.toString(i*dt/60.0)+",    ";
                        String sO2  = "";
                        String sO3  = "";
                        String sO4  = "";
                        for(cell c : RWS.cells){
                            sOut+=Double.toString(c.x())+",    "+Double.toString(c.y())+",    ";
                            sO2+=Double.toString(c.oF)+",    "+Double.toString(c.oB)+",    ";
                        }
                        double[] dEnv1 = RWS.environment.meanProfile(0);
                        double[] dEnv2 = RWS.environment.meanProfile(1);
                        for(int n = 0; n<dEnv1.length; n++){
                            sO3+= Double.toString(dEnv1[n])+",";
                            sO4+= Double.toString(dEnv2[n])+",";
                        }
                        try{
                            RWS.bw.write(sOut);
                            RWS.bw.newLine();
                            RWS.bw2.write(sO2);
                            RWS.bw2.newLine();
                            RWS.bw3.write(sO3);
                            RWS.bw3.newLine();
                            RWS.bw4.write(sO4);
                            RWS.bw4.newLine();
                        }
                        catch (IOException e){}

                    }
                    j++;
                }
                if(record){
                    try{
                        RWS.bw.flush();
                        RWS.bw.close();
                        RWS.fw.close();
                    }
                    catch (IOException e){}
                }
                while(true)    try{sleep(100);} catch(Exception e){}
            }
        }));
        try {
            java.util.List<Future<Object>> list = threadpool.invokeAll(tasks);
            finished = true;
        }
        catch (Exception e){}
        System.exit(1);
    }

    public static void main(String[] args){

        if(args.length==0) new MelaMigration();
        else if(args.length%2==0){
            parseArgs(args);
            visualise = false;
            new MelaMigration();
        }
        else System.exit(-1);
    }

    private void makeSimGUIs(){

        SimGUIPanel s1 = new SimGUIPanel();

        s1.create();

        RWS = s1.SetupSimulation("/1.txt");

    }

    private static void parseArgs(String[] args){

        for(int i=0; i<args.length; i+=2){
            try{
                if(args[i].equals("p"))             pop = Integer.parseInt(args[i+1]);
                else if(args[i].equals("max"))      max = Double.parseDouble(args[i+1]);
                else if(args[i].equals("min"))      min = Double.parseDouble(args[i+1]);
                else if(args[i].equals("D"))        MigrationSimulation.DiffC[0] = Double.parseDouble(args[i+1]);
                else if(args[i].equals("kD"))       MigrationSimulation.kD    = Double.parseDouble(args[i+1]);
                else if(args[i].equals("kM"))       MigrationSimulation.kM    = Double.parseDouble(args[i+1]);
                else if(args[i].equals("sMax"))     MigrationSimulation.sMax  = Double.parseDouble(args[i+1]);
                else if(args[i].equals("a"))        abs = Boolean.parseBoolean(args[i+1]);
                else if(args[i].equals("c"))        ctc = Boolean.parseBoolean(args[i+1]);
                else if(args[i].equals("out"))      directory += args[i+1];
                else if(args[i].equals("alpha"))    alpha += Double.parseDouble(args[i+1]);
            }
            catch(NumberFormatException e){}
        }

    }
}



