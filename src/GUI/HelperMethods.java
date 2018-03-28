/**
 * This is a class containing methods that might be useful throughout the application.
 */

package GUI;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class HelperMethods {

    /**
     * Method used to convert an ArrayList of Long to ArrayList of Strings.
     * @param intList
     * @return
     */
    public static ArrayList<String> convertList(ArrayList<Long> intList) {

        ArrayList<String> stringArray = new ArrayList<String>(intList.size());

        for (Long myInt : intList)
        {
            stringArray.add(myInt.toString());
        }

        return stringArray;
    }

    /**
     * Method used to deduplicate an ArrayList of Strings.
     * @param list
     * @return
     */
    public static ArrayList deduplicate(ArrayList list) {
        ArrayList al = new ArrayList<>(list);
        // add elements to al, including duplicates
        Set hs = new HashSet<>(al);
        al.clear();
        al.addAll(hs);

        return al;
    }

}
