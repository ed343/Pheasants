package Multilateration;

import Jama.Matrix;
import static java.lang.Math.pow;
import java.util.ArrayList;

/**
 * Class that applies the maths behind multilateration.
 */
public class MLATEquation {
    int matrixSize=0;
    ArrayList<Double[]> radiosCoordinates= new ArrayList<>();
    ArrayList<Double> tagDistances=new ArrayList<>();
    
    /**
     * Constructor.
     * 
     * @param matSize       : Matrix size.
     * @param radCoordinates: Coordinates of radios.
     * @param tagDistances  : Distance estimations. 
     */
    public MLATEquation(int matSize, ArrayList<Double[]> radCoordinates,
                        ArrayList<Double> tagDistances){
        this.matrixSize=matSize;
        for (Double[] d : radCoordinates){
            this.radiosCoordinates.add(d.clone());
        }
        this.tagDistances=tagDistances;
    }
    
    /**
     * Ensure correctness of maths.
     * 
     * @return : Boolean informing us of whether fix was successful.
     */
    boolean fix(){
        // if by any chance one radio does not pick up one tag at one time
        // we don't take that radio into consideration 
        // when doing multilateration
        int count=0;
        for (int i=0; i<tagDistances.size(); i++){
           // System.out.println("distance: " + tagDistances.get(i));
            if (tagDistances.get(i)==-999.999){
                this.radiosCoordinates.remove(i-count);
                int aux=matrixSize-1;
                this.matrixSize=aux;
                // to preserve indices even if we remove from radiosCoordinates
                count++; 
            }
        }
       // System.out.println("No of radios which have picked it up is "
       //                 + radiosCoordinates.size());
        if (radiosCoordinates.size()<=3){
       //     System.out.println("Not enough radios for multilateration");
            return false;
        }
       // System.out.println("Matrix size is " + matrixSize);
        return true;
    }
    /* Method returns the computed A matrix for the equation AX=B
     */
    /**
     * Method returns the computed A matrix for the equation AX=B.
     * 
     * @return A matrix.
     */
    Matrix getA(){
        //the 4 is hardcoded because A will always have 4 columns
        //if we multilaterate for 3D coordinates
        double[][] matrixArray=new double[matrixSize][4]; 
        // need to find a pretty way to do this
        for (int i=0; i<matrixSize; i++){
            matrixArray[i][0]=1;
            matrixArray[i][1]=-2*radiosCoordinates.get(i)[0];
            matrixArray[i][2]=2*radiosCoordinates.get(i)[1];
            matrixArray[i][3]=2*radiosCoordinates.get(i)[2];
            
        }
        // convert to matrix from 2D array
        Matrix mat= new Matrix(matrixArray); // this is A
         
        return mat;
    }

    /**
     * Method returns the computed B matrix for the equation AX=B.
     * 
     * @return B matrix.
     */
    Matrix getB(){
        double[] B=new double[matrixSize];
        for (int i=0; i<matrixSize; i++){
            B[i]=pow(tagDistances.get(i),2)
                    -pow(radiosCoordinates.get(i)[0],2)
                    -pow(radiosCoordinates.get(i)[1],2)
                    -pow(radiosCoordinates.get(i)[2],2);
        }     
        // convert to matrix from 1D array
        Matrix b= new Matrix(B,matrixSize); // this is b
        return b;
    }

    /**
     * Method returns the solution of the equation AX=B.
     * 
     * @return X matrix.
     */
    Matrix getSolution(){
        
        // if this gives you problems, then create an MLATEq object in MLAT
        // and call the getA and getB there
        
        Matrix A=this.getA();
        Matrix B=this.getB();
        Matrix sol=A.solve(B);
        return sol;
    }
}

