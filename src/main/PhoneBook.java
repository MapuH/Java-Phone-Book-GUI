package main;

import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import main.model.Contact;
import main.view.ContactEditDialogController;
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
        setDataPath("resources/data/contacts.csv");
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

    private void loadContacts(ObservableList<Contact> contacts) {
        try(BufferedReader reader = new BufferedReader(new FileReader(getDataPath()))) {

            Pattern pattern = Pattern.compile("^([^,\"]{2,50}),([0-9+ ]{3,30})?,([0-9+ ]{3,30})?,([0-9+ ]{3,30})?,([0-9+ ]{3,30})?$");

            while (true) {
                String line = reader.readLine();
                if (line == null) {
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

        // set the application icon
        this.primaryStage.getIcons().add(new Image("file:resources/images/phonebook_icon.png"));

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

    /**
     * Opens a dialog to edit details for the specified contact. If the user
     * clicks OK, the changes are saved into the provided contact object and true
     * is returned.
     *
     * @param contact the person object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showContactEditDialog(Contact contact) {
        try {
            // load the fxml file and create a new stage for the popup dialog
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(PhoneBook.class.getResource("view/ContactEditDialog.fxml"));
            AnchorPane page = loader.load();

            // create the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add/Edit Contact");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // load the edit contact controller
            ContactEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setContact(contact);

            // show dialog and wait till user takes action
            dialogStage.showAndWait();

            return controller.isOkClicked();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
