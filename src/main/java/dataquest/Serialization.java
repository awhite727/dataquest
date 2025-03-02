package dataquest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


//NOTE: Can eventually be put directly into Dataset; left separate for readability/editability
//NOTE: Currently saves the Dataset class, and then Layout loads the info back into the correct places
//Anything added to the array in Layout but not actually added to Dataset will not be saved
public class Serialization {

    //Attempts to save the Dataset to SaveState.ser
    //If successful, just closes. If not, prints an error
    public void saveProject(Dataset dataset) {
        String savePath = "src\\main\\resources\\SaveState.ser";
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(savePath));
            out.writeObject(dataset);
            out.close();
            System.out.println("Successfully saved");
        }
        catch (IOException e) {
            System.out.println("ERROR: Could not save project");
            e.printStackTrace();
        }
    }

    //Checks if it can find the project
    //If so, returns the Dataset. If not, returns a new Dataset()
    public Dataset openProject(){
        String filename = "src\\main\\resources\\SaveState.ser";
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
            Dataset dataset = (Dataset)in.readObject();
            in.close();
            System.out.println("Workspace successfully opened");
            return dataset;
        }
        catch (IOException e) {
            //thrown when nothing saved as well 
            System.out.println("ERROR: Could not open saved workspace");
            return new Dataset();
        }
        catch (ClassNotFoundException e) {
            System.out.println("ERROR: Could not find class to be opened");
            return new Dataset();
        }
    }
}
