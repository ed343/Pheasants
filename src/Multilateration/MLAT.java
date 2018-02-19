package Multilateration;

import Jama.Matrix;

public class MLAT {
    static int no_of_radios;
    static int radioIndex=0; //get from user maybe? 
                            //find a way to pass the radio I am computing for
    //maybe I can get this from Primer, since it's the class that has the data
    
    public static void main(String args[]){    
        // sketch out the flow of the program:
        // 0. create a program instance:
        PrimerClass primer= new PrimerClass();
        // 1. set no. of radios in field
        primer.setNumberOfRadios(5); // change to user input
        // 2. insert all radio coordinates
        for (int i=0; i<no_of_radios; i++){
            //user input
            //repeated call to primer.setRadioCoordinates()
        }
        // 3. insert all radio 1 Meter RSSI
        for (int i=0; i<no_of_radios; i++){
            //unser input
            //repeated call to primer.setRadioMeasuredPower()
        }
        // 4. get all rssi values
        //parse the file - i know which radio it is by the file
        //for each "tracking detection" line
        //get the tag number
        //get the time of the detection
        //get the rssi
        //if the line is a bust, so no detection or it says -infinity, 
        //put rssi down as 0
        for (int i=0; i<no_of_radios; i++){
            //get parsed values
            //ordered list of tagID and corresponding list of time and rssi
            //repeated call to primer.setTimeRssiValues()
        }
        // 5. find out all distances using RSSI
        for (int i=0; i<no_of_radios; i++){
            //repeated call to getTagDistance() from RssiEq
            //getTagDistance(rssiValues[i], measuredPower[i])
            //save it in attribute primer.tagDistances[i]
        }
        // 6. additional denoising steps like rssiequationrefined
        

        // call to primer.rssiValues[radio_no]
        // loop over primer.rssiValues[0].size()
        // for each tag, do the next lines of code
   
        int matrixSize=0; // equal to how many tags picked up my current tag
        
 
        MLATEquation eq=new MLATEquation(radioIndex, matrixSize, 
                                        primer.getRadiosCoordinates(), 
                                        primer.getTagDistances());
        
        Matrix sol= eq.getSolution(); //these are our coordinates
                                      //the matrix is 4x1, and entries 1,2,3
                                      //give us the x, y, z coord
    }
}