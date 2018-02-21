package Multilateration;

import Jama.Matrix;
import java.util.ArrayList;

public class MLAT {
    static int no_of_radios;
    static int radioIndex=0; //get from user maybe? 
    

    
    
    
    public static void main(String args[]){    
        //ArrayList to store the Data extracted from all Log files.
        ArrayList<LogData> dataArr = new ArrayList<LogData>();
        // sketch out the flow of the program:
        // 0. create a program instance:
        PrimerClass primer= new PrimerClass();
        // 1. set no. of radios in field
        primer.setNumberOfRadios(5); // change to user input
        //
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
        // 3.5. Extract data from all relevant log files.
        for (int i=0;i<no_of_radios;i++){
            //Ask user to input the full path of a log file.
            String fPath = "/Path/Name";
            //Create a new instance of LogData with path.
            LogData log = new LogData(fPath);
            //Add LogData object to ArrayList.
            dataArr.add(log);
        }
        // 4. get all rssi values
        for (int i=0; i<no_of_radios; i++){
            //Retrieve LogData object.
            LogData log = dataArr.get(i);
            //Get times for this base station.
            ArrayList<Long> tData = log.getTimes();
            //Get IDs for this basestation.
            ArrayList<Long> idData = log.getIDs();
            //Get RSSI values for this basestation.
            ArrayList<Double> rssiData = log.getRSSIs();
            primer.setTimeRssiValues(tData,idData,rssiData);
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
