import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: luke
 * Date: 24/02/2014
 * Time: 11:12
 * To change this template use File | Settings | File Templates.
 */
public class SimGUIPanel {

    //Switches in the GUI
    boolean proliferate = false;
    boolean die         = false;
    boolean contact     = MelaMigration.ctc;
    boolean absorber    = MelaMigration.abs;
    boolean vis         = MelaMigration.visualise;
    boolean pinned      = MelaMigration.pinned;

    double  alpha       = MelaMigration.alpha;
    double  min         = MelaMigration.min;
    double  max         = MelaMigration.max;
    double  dt          = MelaMigration.dt;
    double  dx          = ChemicalEnvironment.grain;
    double  Diff        = MigrationSimulation.DiffC;
    double  speed       = cell.speed;
    double  sMax        = MigrationSimulation.sMax;
    double  kD          = MigrationSimulation.kD;
    double  kM          = MigrationSimulation.kM;
    double  xMax        = MelaMigration.dimensions[0];
    double  yMax        = MelaMigration.dimensions[1];

    private JPanel p;

    public SimGUIPanel(){
        p = new JPanel(new SpringLayout());
    }

    //Create and populate the panel.
    public void create(){

        JCheckBox jtv = new JCheckBox("Visualise", vis);
        jtv.setToolTipText("Display visual of whilst running simulation");
        p.add(jtv);

        JCheckBox jtp = new JCheckBox("Proliferate", proliferate);
        jtv.setToolTipText("have cells reproduce.");
        p.add(jtp);

        JCheckBox jtc = new JCheckBox("Contact", contact);
        jtv.setToolTipText("Have cells experience contact inhibition.");
        p.add(jtc);

        JCheckBox jta = new JCheckBox("Absorber", absorber);
        jtv.setToolTipText("Have cells degrade chemoattractant.");
        p.add(jta);

        p.add(new JLabel("Min. conc.", JLabel.TRAILING));
        JTextField jtMin = new JTextField(Double.toString(min));
        jtMin.setToolTipText("The concentration on the left-hand side. \n Does not actually have to be the minimum concentration value.");
        p.add(jtMin);

        p.add(new JLabel("Max. conc.", JLabel.TRAILING));
        JTextField jtMax = new JTextField(Double.toString(max));
        jtMax.setToolTipText("The concentration on the right-hand side. \nDoes not actually have to be the maximum concentration value.");
        p.add(jtMax);

        p.add(new JLabel("dt", JLabel.TRAILING));
        JTextField jtdt = new JTextField(Double.toString(dt));
        jtdt.setToolTipText("The time-step.");
        p.add(jtdt);

        p.add(new JLabel("dx", JLabel.TRAILING));
        JTextField jtdx = new JTextField(Double.toString(dx));
        jtdx.setToolTipText("The grid spacing (for diffusion).");
        p.add(jtdx);

        p.add(new JLabel("x size", JLabel.TRAILING));
        JTextField jtx = new JTextField(Double.toString(xMax));
        jtx.setToolTipText("The horizontal size of the simulation.");
        p.add(jtx);

        p.add(new JLabel("y size", JLabel.TRAILING));
        JTextField jty = new JTextField(Double.toString(yMax));
        jty.setToolTipText("The vertical size of the simulation.");
        p.add(jty);

        p.add(new JLabel("D", JLabel.TRAILING));
        JTextField jtDiff = new JTextField(Double.toString(Diff));
        jtDiff.setToolTipText("The diffusion coefficient (how quickly chemoattractant diffuses)");
        p.add(jtDiff);

        p.add(new JLabel("kD", JLabel.TRAILING));
        JTextField jtkd = new JTextField(Double.toString(kD));
        jtkd.setToolTipText("The dissociation constant. \nControls saturation point of receptors.");
        p.add(jtkd);

        p.add(new JLabel("sMax.", JLabel.TRAILING));
        JTextField jtsMax = new JTextField(Double.toString(sMax));
        jtsMax.setToolTipText("The maximum rate at which each cell breaks down chemoattractant.");
        p.add(jtsMax);

        p.add(new JLabel("kM.", JLabel.TRAILING));
        JTextField jtkM = new JTextField(Double.toString(kM));
        jtkM.setToolTipText("The Michaelis-Menten constant (the concentration for half-maximum rate).");
        p.add(jtkM);

        p.add(new JLabel("Cell speed.", JLabel.TRAILING));
        JTextField jtSp = new JTextField(Double.toString(speed));
        jtSp.setToolTipText("The instantaneous speed at which cells move.");
        p.add(jtSp);

        p.add(new JLabel("Polarisation [0,1)", JLabel.TRAILING));
        JTextField jtPol = new JTextField(Double.toString(alpha));
        jtPol.setToolTipText("The expected agreement in the direction a cell moves after 1 minute.");
        p.add(jtPol);

        p.add(new JLabel("directory", JLabel.TRAILING));
        JTextField jtdir = new JTextField(MelaMigration.directory,10);
        jtdir.setToolTipText("The output directory.");
        p.add(jtdir);

        p.add(new JLabel(""));

        JCheckBox jPin = new JCheckBox("Pinned", pinned);
        jtv.setToolTipText("Boundary conditions pinned to max/min inputs");
        p.add(jPin);



        SpringUtilities.makeCompactGrid(p,
                8, 4,        //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad
        int iR = JOptionPane.showConfirmDialog(p,p, "Simulation 1", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(iR==JOptionPane.OK_OPTION){

            proliferate = jtp.isSelected();
            vis         = jtv.isSelected();
            contact     = jtc.isSelected();
            absorber    = jta.isSelected();
            pinned      = jPin.isSelected();

            min   = Double.parseDouble(jtMin.getText());
            max   = Double.parseDouble(jtMax.getText());
            alpha = Double.parseDouble(jtPol.getText());
            speed = Double.parseDouble(jtSp.getText());
            Diff  = Double.parseDouble(jtDiff.getText());
            kD    = Double.parseDouble(jtkd.getText());
            kM    = Double.parseDouble(jtkM.getText());
            sMax  = Double.parseDouble(jtsMax.getText());
            dt    = Double.parseDouble(jtdt.getText());
            dx    = Double.parseDouble(jtdx.getText());

            xMax  = Double.parseDouble(jtx.getText());
            yMax  = Double.parseDouble(jty.getText());

            MelaMigration.directory = jtdir.getText();
            MelaMigration.pinned = pinned;

            MelaMigration.dimensions[0] = MelaMigration.dimensions[2] = xMax;
            MelaMigration.dimensions[1] = MelaMigration.dimensions[3] = yMax;
        }
        else{
            System.exit(0);
        }

    }

    public MigrationSimulation SetupSimulation(String name){

         return new MigrationSimulation(proliferate, die, contact, absorber, min, max, alpha,speed,dt,dx,Diff,kD,kM,sMax);
    }
}