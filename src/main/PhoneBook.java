package main;

import main.model.Contact;
import main.view.ContactOverviewController;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PhoneBook extends Application {

    private String dataPath;
    private Stage primaryStage;
    private VBox overview;
    private ObservableList<Contact> contactsData = FXCollections.observableArrayList();

    public PhoneBook() {
        setDataPath("src/main/data/contacts.csv");
        loadContacts(contactsData);
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public ObservableList<Contact> getContactsData() {
        return contactsData;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void loadContacts(ObservableList<Contact> contacts){
        try(BufferedReader reader = new BufferedReader(new FileReader(getDataPath()))) {

            Pattern pattern = Pattern.compile("^([^,\"]{2,50}),([0-9+, ]{3,30})?,([0-9+, ]{3,30})?,([0-9+, ]{3,30})?,([0-9+, ]{3,30})?$");

            while(true){
                String line = reader.readLine();
                if(line == null){
                    break;
                }

                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String name = matcher.group(1);
                    String mobile = matcher.group(2);
                    String work = matcher.group(3);
                    String home = matcher.group(4);
                    String additional = matcher.group(5);
                    contacts.add(new Contact(name, mobile, work, home, additional));
                }
            }

        }catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Phone Book");

        initLayout();
    }

    public void initLayout() {
        try {
            // load layout from fxml file
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(PhoneBook.class.getResource("view/ContactOverview.fxml"));
            overview = loader.load();

            Scene scene = new Scene(overview);
            primaryStage.setScene(scene);
            primaryStage.show();

            // give controller access to the phone book
            ContactOverviewController controller = loader.getController();
            controller.setPhoneBook(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
