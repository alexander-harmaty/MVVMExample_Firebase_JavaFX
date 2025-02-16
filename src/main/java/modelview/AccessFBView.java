package modelview;

import com.mycompany.mvvmexample.App;
import viewmodel.AccessDataViewModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.mycompany.mvvmexample.FirestoreContext;
import com.mycompany.mvvmexample.FirestoreContext;
import com.mycompany.mvvmexample.FirestoreContext;
import com.mycompany.mvvmexample.FirestoreContext;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import models.Person;

public class AccessFBView implements Initializable {

    @FXML
    private TextField nameField, majorField, ageField;
    
    @FXML
    private Button writeButton, readButton;
    
    @FXML
    private TextArea outputField;
    
    @FXML
    private TableView<Person> outputTable = new TableView();
    
    @FXML
    private TableColumn<Person, Integer> column_age;

    @FXML
    private TableColumn<Person, String> column_major;

    @FXML
    private TableColumn<Person, String> column_name;
    
    @FXML
    private TableColumn<Person, String> column_ID;
    
    private String currentID = "";
           
    private boolean key;
    
    private ObservableList<Person> listOfUsers = FXCollections.observableArrayList();
    
    private Person person;
    
    public ObservableList<Person> getListOfUsers() {
        return listOfUsers;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        AccessDataViewModel accessDataViewModel = new AccessDataViewModel();
        nameField.textProperty().bindBidirectional(accessDataViewModel.userNameProperty());
        majorField.textProperty().bindBidirectional(accessDataViewModel.userMajorProperty());
        //writeButton.disableProperty().bind(accessDataViewModel.isWritePossibleProperty().not());
        
        column_name.setCellValueFactory(new PropertyValueFactory<>("Name"));
        column_major.setCellValueFactory(new PropertyValueFactory<>("Major"));
        column_age.setCellValueFactory(new PropertyValueFactory<>("Age"));
        column_ID.setCellValueFactory(new PropertyValueFactory<>("ID"));
        
        outputTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                nameField.setText(outputTable.getSelectionModel().getSelectedItem().getName());
                majorField.setText(outputTable.getSelectionModel().getSelectedItem().getMajor()); 
                ageField.setText(Integer.toString(outputTable.getSelectionModel().getSelectedItem().getAge()));
                currentID = outputTable.getSelectionModel().getSelectedItem().getId();
            }
        });
        
    }

    @FXML
    private void handle_writeRecord(ActionEvent event) throws InterruptedException, ExecutionException {
        
        writeRecord();
        handle_readRecords(event);
        
    }

    @FXML
    private void handle_readRecords(ActionEvent event) {
        
        nameField.clear();
        majorField.clear();
        ageField.clear();
        outputField.clear();
        outputTable.getItems().clear();
        readRecords();
        
    }
    
    @FXML
    private void handle_updateRecord(ActionEvent event) throws InterruptedException, ExecutionException {
        
        updateRecord();
        handle_readRecords(event);
        
    }
    
    @FXML
    private void handle_removeRecord(ActionEvent event) throws InterruptedException, ExecutionException {
        
        removeRecord();
        handle_readRecords(event);
        
    }
    
    public void writeRecord() throws InterruptedException, ExecutionException {

        if( !"".equals(nameField.getText()) || !"".equals(majorField.getText()) || !"".equals(ageField.getText()) ) {
            DocumentReference docRef = App.fstore.collection("References").document(UUID.randomUUID().toString());
            // Add document data  with id "alovelace" using a hashmap
            Map<String, Object> data = new HashMap<>();
            data.put("Name", nameField.getText());
            data.put("Major", majorField.getText());
            data.put("Age", Integer.parseInt(ageField.getText()));
            //asynchronously write data
            ApiFuture<WriteResult> result = docRef.set(data);
            // ...
            System.out.println("Update time : " + result.get().getUpdateTime());
        }
        
    }
    
    public boolean readRecords() {
        
        key = false;

        //asynchronously retrieve all documents
        ApiFuture<QuerySnapshot> future =  App.fstore.collection("References").get();
        
        // future.get() blocks on response
        List<QueryDocumentSnapshot> documents;
        
        try 
        {
            documents = future.get().getDocuments();
            
            if(!documents.isEmpty())
            {
                System.out.println("Outing....");
                
                
                for (QueryDocumentSnapshot document : documents) 
                {
                    person  = new Person(
                                String.valueOf(document.getData().get("Name")), 
                                document.getData().get("Major").toString(),
                                Integer.parseInt(document.getData().get("Age").toString()),
                                String.valueOf(document.getId())
                            );
                    
                    listOfUsers.add(person);
                    
                    ////////////////////////////////////////////////////////////
                    
                    outputField.setText(
                            outputField.getText()+ 
                            document.getData().get("Name")+ " , Major: "+
                            document.getData().get("Major")+ " , Age: "+
                            document.getData().get("Age")+ " \n ");
                    
                    ////////////////////////////////////////////////////////////
                    
                    System.out.println(document.getId() + " => " + document.getData().get("Name"));
                    outputTable.setItems(listOfUsers);
                }
            }
            else
            {
               System.out.println("No data"); 
            }
            
            key=true;

        }
        catch (InterruptedException | ExecutionException ex) {}
        
        return key;
    }
    
    public void updateRecord() throws InterruptedException, ExecutionException {
        
        if( !"".equals(nameField.getText()) || !"".equals(majorField.getText()) || !"".equals(ageField.getText()) ) {
            DocumentReference docRef = App.fstore.collection("References").document(currentID);
            // Update 
            Map<String, Object> updates = new HashMap<>();
            updates.put("Name", nameField.getText());
            updates.put("Major", majorField.getText());
            updates.put("Age", Integer.parseInt(ageField.getText()));
            // Async update document
            ApiFuture<WriteResult> writeResult = docRef.update(updates);
            // ...
            System.out.println("Update time : " + writeResult.get().getUpdateTime());
        }
        
    }
    
    public void removeRecord() throws InterruptedException, ExecutionException {
        
        //asynchronously delete a document
        ApiFuture<WriteResult> writeResult = App.fstore.collection("References").document(currentID).delete();
        System.out.println("Update time : " + writeResult.get().getUpdateTime()); 
        
    }

}