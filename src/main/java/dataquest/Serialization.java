package dataquest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


//NOTE: Can eventually be put directly into Layout; left separate for readability/editability
//NOTE: Currently saves the Layout class, and then Layout loads the info back into the correct places
//Anything added to the array in Layout but not actually added to Layout will not be saved
public class Serialization {

    //Attempts to save the Layout to SaveState.ser
    //If successful, just closes. If not, prints an error
    public void saveProject(ArrayList<Object> object) {
        String savePath = "src\\main\\resources\\SaveState.ser";
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(savePath));
            out.writeObject(object);
            out.close();
            System.out.println("Successfully saved");
        }
        catch (IOException e) {
            System.out.println("ERROR: Could not save project");
            e.printStackTrace();
        }
    }

    //Checks if it can find the project
    //If so, returns the Layout. If not, returns a new Layout()
    public ArrayList<Object> openProject(){
        String filename = "src\\main\\resources\\SaveState.ser";
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
            ArrayList<Object> deserialized = (ArrayList) in.readObject();
            in.close();
            if(deserialized instanceof ArrayList){ 
                System.out.println("Workspace successfully opened");
                return deserialized;
            } else {
                System.out.println("ERROR: Info in file not an ArrayList");
                return new ArrayList<Object>();
            }
        }
        catch (IOException e) {
            //thrown when nothing saved as well 
            System.out.println("ERROR: Could not open saved workspace");
            return new ArrayList<Object>();
        }
        catch (ClassNotFoundException e) {
            System.out.println("ERROR: Could not find class to be opened");
            return new ArrayList<Object>();
        }
    }
}
