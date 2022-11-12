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
    private void handle_addRecord(ActionEvent event) {
        
        addRecord();
        nameField.clear();
        majorField.clear();
        ageField.clear();
        handle_readRecords(event);
        
    }

    @FXML
    private void handle_readRecords(ActionEvent event) {
        
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
    
    public void addRecord() {

        DocumentReference docRef = App.fstore.collection("References").document(UUID.randomUUID().toString());
        // Add document data  with id "alovelace" using a hashmap
        Map<String, Object> data = new HashMap<>();
        data.put("Name", nameField.getText());
        data.put("Major", majorField.getText());
        data.put("Age", Integer.parseInt(ageField.getText()));
        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);
        
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
            
            if(documents.size()>0)
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
                    
                    System.out.println(document.getId() + " => " + document.getData().get("Name"));
                    
                    ////////////////////////////////////////////////////////////
                    
                    outputTable.setItems(listOfUsers);
                }
            }
            else
            {
               System.out.println("No data"); 
            }
            
            key=true;

        }
        catch (InterruptedException | ExecutionException ex) 
        {
             ex.printStackTrace();
        }
        
        return key;
    }
    
    public void updateRecord() throws InterruptedException, ExecutionException {
        // Create an initial document to update
        DocumentReference frankDocRef = App.fstore.collection("References").document("currentID");
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("name", "Frank");
        initialData.put("age", 12);

        Map<String, Object> favorites = new HashMap<>();
        favorites.put("food", "Pizza");
        favorites.put("color", "Blue");
        favorites.put("subject", "Recess");
        initialData.put("favorites", favorites);

        ApiFuture<WriteResult> initialResult = frankDocRef.set(initialData);
        // Confirm that data has been successfully saved by blocking on the operation
        initialResult.get();

        // Update age and favorite color
        Map<String, Object> updates = new HashMap<>();
        updates.put("age", 13);
        updates.put("favorites.color", "Red");

        // Async update document
        ApiFuture<WriteResult> writeResult = frankDocRef.update(updates);
        // ...
        System.out.println("Update time : " + writeResult.get().getUpdateTime());

    }
    
    public void removeRecord() throws InterruptedException, ExecutionException {
        //asynchronously delete a document
        ApiFuture<WriteResult> writeResult = App.fstore.collection("References").document(currentID).delete();
        System.out.println("Update time : " + writeResult.get().getUpdateTime()); 
    }

}