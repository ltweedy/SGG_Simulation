/**
 * Created with IntelliJ IDEA.
 * User: luke
 * Date: 15/08/2012
 * Time: 16:57
 * To change this template use File | Settings | File Templates.
 */
public class MyMaths {

    public static double dotProduct(double[] a, double[] b){
        if(a.length!=b.length) return 0;
        if(a.length == 0) return 0;

        double total = 0.0;
        for(int n=0; n<a.length; n++){
            total+= a[n]*b[n];
        }
        return total;
    }

    public static double sum(double[] a){
        double total = 0.0;
        for(int n=0; n<a.length; n++){
            total+= a[n];
        }
        return total;
    }
    public static double avg(double[] a){
        double total = sum(a);

        return total/a.length;
    }

    public static double vectorNorm(double[] a){
        if(a.length == 0) return 0.0;

        double total = 0.0;
        for(int n=0; n<a.length; n++){
            total+= a[n]*a[n];
        }
        return fastSquareRoot(total);
    }

    //public static void dragForce(braneNode obj) {
    //    obj.vx-=DRAG*obj.vx;
    //    obj.vy-=DRAG*obj.vy;
    //}
    public static double[] normalised(double[] dIn){

        double[] dOut = dIn;
        double dNorm = vectorNorm(dIn);

        for(int i=0; i<dOut.length; i++) dOut[i]/=dNorm;

        return dOut;
    }

    public static double fastSquareRoot(double x){
        double xhalf = 0.5d*x;
        long i = Double.doubleToLongBits(x);
        i = 0x5fe6ec85e7de30daL - (i>>1);
        x = Double.longBitsToDouble(i);
        x = x*(1.5d - xhalf*x*x);
        return 1.0d/x;
    }

    public static double[] push(double[] a,double value){
        double[] b = new double[a.length];
        for(int i=0; i<a.length-1; i++){
            b[i] = a[i+1];
        }
        b[a.length-1] = value;
        return b;
    }

    public static double squaredClosestApproach(double[] point, double[] start, double[] end){

        double out = 0.0;
        double[] nHat = new double[start.length];
        double[] PS   = new double[start.length];
        for(int i=0; i<nHat.length; i++){
            PS[i]   = start[i] - point[i];
            nHat[i] = end[i]   - start[i];
        }
        double nNorm = vectorNorm(nHat);

        for(int i=0; i<nHat.length; i++){
            nHat[i] /= nNorm;
        }
        double PSdnHat = dotProduct(PS,nHat);

        for(int i=0; i<nHat.length; i++){
            out += (PS[i] - (PSdnHat*nHat[i]))*(PS[i] - (PSdnHat*nHat[i]));
        }

        return out;
    }

    public static double max(double[] list){
        if(list.length == 0) return 0;
        double out = list[0];
        for(int i=1; i<list.length; i++){
            if(list[i]>out) out = list[i];
        }
        return out;
    }
    public static double bounded(double min, double max, double val){

        double r = max - min;

        return ((((val-min)%r)+r)%r)+min;
    }
}