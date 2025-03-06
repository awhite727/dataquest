package dataquest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;


//Anything added to the array in Layout but not actually added to Layout will not be saved
public class Serialization implements Serializable{
    ArrayList<Object> savedWorkspace = new ArrayList<>();
    
    //Attempts to save the all the information to SaveState.ser
    //TODO: Include any other information needed to be saved
    //If need to save something non-static, need to change pass saveProject an ArrayList<Object> of all the classes
    //And make those classes implement Serializable 
    public void saveProject() {
        String savePath = "src\\main\\resources\\SaveState.ser";
        try {
            //Add any more elements here
            savedWorkspace.add(new ArrayList<Field>(Dataset.dataArray));
            
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(savePath));
            out.writeObject(savedWorkspace);
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
