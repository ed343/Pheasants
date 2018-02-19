package Multilateration;

import Jama.Matrix;
import static java.lang.Math.pow;

public class MultilaterationTEST {
    // should this be an ArrayList, so that'd it be flexible in size?
    // Populate this with the coordinates of the
    // basestations (radio-pis)
    static double[][] beaconsCoordinates;
    static double[] rssiDistances;

    
    public static void main(String[] args) {
        primer();
        getTagCoordinates(beaconsCoordinates, rssiDistances);
    }

    static void primer(){
        beaconsCoordinates=new double[][]{ {0, 2, 1}, {0, 10, 1}, 
                                           {15, 10, 0.5}, {14, 0, 2}};
        
        //rssiDistances= new double[]{7.63,8.6168,9.48,8.61}; //7, 5, 1.5
        //rssiDistances= new double[]{25.69,24.41,15.33,17.46}; //20, 10, 15
        //rssiDistances= new double[]{27.20,31.55,25.204,17.4642};//20, -10, 15
        rssiDistances= new double[]{28.28,32.49,25.79,20.61};//20, -10, -15
    }
    
    /*
     * Method calculates distance in meters based on rssi values.
     * FORMULAE:    RSSI = measuredPower - 10 * n * lg(d)
     *              d = 10 ^ ((measuredPower – RSSI) / (10 * n))
     *
     * --measuredPower is the RSSI measured at 1m from a known AP. 
     *   For example: -84 dB. Also known as the 1 Meter RSSI.
     * --n is the propagation constant or path-loss exponent. 
     *   For example: 2.7 to 4.3 (Free space has n = 2 for reference).
     * !!! measuredP and n must be determined empirically!!!
     * --RSSI is the measured RSSI
     * --d is the distance in meters
     */
        double getTagDistance(int rssi, int measuredPower) { // how do I get measuredP?
                                                                                                  
        measuredPower = -59;  // different for each basestation I think
                            // need to find out how to hardcode it
	 //n = 2 (in free space)
        return Math.pow(10d, ((double) measuredPower - rssi) / (10 * 2));
    }
        
    /*
     * The rssiDistances are computed using the getTagDistance() method 
     */
    static int getTagCoordinates( double[][] beaconsCoordinates, double[] rssiDistances){
        
        // write the equation of the spheres for each radius=rssiDistance
        // represent in matrix form as Ax=b
        // A is the matrix |1  -2x1  2y1  2z1|
        //                 |1  -2x2  2y2  2z2|
        //                 |.................|
        //                 |1  -2xm  2ym  2zm|
        // A has all known values
        // b is the column vector |d1^2-x1^2-y1^2-z1^2|
        //                        |d2^2-x2^2-y2^2-z2^2|
        //                        |...................|
        //                        |dm^2-xm^2-ym^2-zm^2|
        // b has all known values
        // it remains to find out x, which is a a column vector | x^2+y^2+z^2|
        //                                                      |     x      |
        //                                                      |     y      |
        //                                                      |     z      |
        // if A is invertible and has full rank
        //      -- if square, check that determinant is 0
        //      -- if not, call inverse()
        //      -- if A is of shape mxn and m>n, its rank should be m
        // otherwise, we can't get a single solution from Ax=b
        // solution will depend on whether we want 2D or 3D coord of the tag
        // say 2D:
        //      -- if have only 3 radios, A is square and we only have a sol if
        // rank of A is 3
        //      -- if have more than 3, A is rectangular and is not invertible
        // SOLUTION:
        // 1) do the SVD of A. get U, V and S (the diagonal matrix of sing vals)
        //   --the SVD always exists, so we're not going to get an error
        // 2) get U transpose
        // 3) get the pseudoinverse of A by calling inverse on A
        // Optional: get S pseudo by Apseudo=V*Spseudo*Utransp
        // 5) solution is x= Apseudo*b
        
        
        // DECISION TO USE 3D COORDINATES.
        
        
        // Alternatively to everything above, just use the solve method from 
        // the Matrix class and solve Ax=b
        
        int matrixSize=beaconsCoordinates.length;
        double[][] matrixArray=new double[matrixSize][4];
       
        // need to find a pretty way to do this
        matrixArray[0][0]=1;
        matrixArray[1][0]=1;
        matrixArray[2][0]=1;
        matrixArray[3][0]=1;
        
        matrixArray[0][1]=-2*beaconsCoordinates[0][0];
        matrixArray[1][1]=-2*beaconsCoordinates[1][0];
        matrixArray[2][1]=-2*beaconsCoordinates[2][0];
        matrixArray[3][1]=-2*beaconsCoordinates[3][0];

        matrixArray[0][2]=2*beaconsCoordinates[0][1];
        matrixArray[1][2]=2*beaconsCoordinates[1][1];
        matrixArray[2][2]=2*beaconsCoordinates[2][1];
        matrixArray[3][2]=2*beaconsCoordinates[3][1];
        
        matrixArray[0][3]=2*beaconsCoordinates[0][2];
        matrixArray[1][3]=2*beaconsCoordinates[1][2];
        matrixArray[2][3]=2*beaconsCoordinates[2][2];
        matrixArray[3][3]=2*beaconsCoordinates[3][2];
        System.out.println("A is: ");
        System.out.println(matrixArray[0][0] + " " + matrixArray[0][1] + " " + matrixArray[0][2] + " " + matrixArray[0][3]);
        System.out.println(matrixArray[1][0] + " " + matrixArray[1][1] + " " + matrixArray[1][2] + " " + matrixArray[0][3]);
        System.out.println(matrixArray[2][0] + " " + matrixArray[2][1] + " " + matrixArray[2][2] + " " + matrixArray[0][3]);
        System.out.println(matrixArray[3][0] + " " + matrixArray[3][1] + " " + matrixArray[3][2] + " " + matrixArray[0][3]);
        // convert to matrix from 2D array
        Matrix mat= new Matrix(matrixArray); // A
        
        double[] B=new double[matrixSize];
        B[0]=pow(rssiDistances[0],2)-pow(beaconsCoordinates[0][0],2)-
                pow(beaconsCoordinates[0][1],2)-pow(beaconsCoordinates[0][2],2);
        B[1]=pow(rssiDistances[1],2)-pow(beaconsCoordinates[1][0],2)-
                pow(beaconsCoordinates[1][1],2)-pow(beaconsCoordinates[1][2],2);
        B[2]=pow(rssiDistances[2],2)-pow(beaconsCoordinates[2][0],2)-
                pow(beaconsCoordinates[2][1],2)-pow(beaconsCoordinates[2][2],2);
        B[3]=pow(rssiDistances[3],2)-pow(beaconsCoordinates[3][0],2)-
                pow(beaconsCoordinates[3][1],2)-pow(beaconsCoordinates[3][2],2);
        
        System.out.println("B is:");
        System.out.println(B[0]);
        System.out.println(B[1]);
        System.out.println(B[2]);
        System.out.println(B[3]);
        // convert to matrix from 1D array
        Matrix b= new Matrix(B,matrixSize); // b
        Matrix sol= mat.solve(b);           // solve Ax=b
        System.out.println("Solution is:");
        System.out.println("x is: " + sol.get(1,0));
        System.out.println("y is: " + sol.get(2,0));
        System.out.println("z is: " + sol.get(3,0));
        
       return 1;
    }
}