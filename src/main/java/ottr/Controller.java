package ottr;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Controller {

    File file = null;
    String fileContent = "";

    @FXML
    TextField srcFileTextField;

    @FXML
    public void browseFile(javafx.event.ActionEvent actionEvent) {
        try {
            Window window = ((Node) (actionEvent.getSource())).getScene().getWindow();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("St. OTTR files (*.stOTTR)", "*.stOTTR");
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(extFilter);
            file = fileChooser.showOpenDialog(window);
            if(validateOttrFormat(file)){
                srcFileTextField.clear();
                fileContent = "";
                srcFileTextField.appendText(file.getPath());
                try {
                    File myObj = new File(file.getPath());
                    Scanner myReader = new Scanner(myObj);
                    while (myReader.hasNextLine()) {
                        fileContent = fileContent + myReader.nextLine();
                    }
                    myReader.close();
                } catch (FileNotFoundException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("File not found or incorrect file!");
                alert.setContentText("Please select a correct Standard OTTR file and continue...");
                alert.showAndWait().ifPresent(rs -> {
                });
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void resetFunction(ActionEvent actionEvent) {
        srcFileTextField.clear();
    }

    @FXML
    public void visualize(ActionEvent actionEvent) {

        Diagram graphWindow = new Diagram(fileContent);
        Stage stage = new Stage();
        graphWindow.parseFile();
        graphWindow.start(stage);
    }

    public boolean validateOttrFormat(File file){
        try {
            return file.getPath().split("\\.")[1].toLowerCase().equals("stottr");
        }catch (NullPointerException ex){
            return false;
        }
    }



}


