package main;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import main.model.Contact;
import main.model.ContactListWrapperXML;
import main.view.ContactEditDialogController;
import main.view.ContactOverviewController;
import java.io.*;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class PhoneBook extends Application {

    private String dataPath;
    private Stage primaryStage;
    private VBox overview;
    private ObservableList<Contact> contactsData = FXCollections.observableArrayList();

    public PhoneBook() {
        // setDataPath("resources/data/contacts.csv");
        // loadContacts(contactsData);
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
        this.primaryStage.setTitle("PhoneBook");

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

        // try to load the last opened contacts file
        File file = getContactFilePath();
        if (file != null) {
            loadContactDataXML(file);
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

    /**
     * Returns the file that was last opened.
     * The preference is read from the OS specific registry. If no such
     * preference can be found, null is returned.
     *
     * @return
     */
    public File getContactFilePath() {
        Preferences prefs = Preferences.userNodeForPackage(PhoneBook.class);
        String filePath = prefs.get("filePath", null);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
    }

    /**
     * Sets the file path of the currently loaded file. The path is persisted in
     * the OS specific registry.
     *
     * @param file the file or null to remove the path
     */
    public void setContactFilePath(File file) {
        Preferences prefs = Preferences.userNodeForPackage(PhoneBook.class);
        if (file != null) {
            prefs.put("filePath", file.getPath());

            // update the title
            primaryStage.setTitle("PhoneBook - " + file.getName());
        } else {
            prefs.remove("filePath");

            // update the title
            primaryStage.setTitle("PhoneBook");
        }
    }

    /**
     * Loads contact data from the specified XML file. The current contact data will
     * be replaced.
     *
     * @param file
     */
    public void loadContactDataXML(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(ContactListWrapperXML.class);
            Unmarshaller um = context.createUnmarshaller();

            // reading xml from the file and unmarshalling
            ContactListWrapperXML wrapperXML = (ContactListWrapperXML) um.unmarshal(file);

            contactsData.clear();
            contactsData.addAll(wrapperXML.getContacts());

            // save filepath to the registry
            setContactFilePath(file);

        } catch (Exception e) {
            // catches any exception
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load data");
            alert.setContentText("Could not load data from file:\n" + file.getPath());

            alert.showAndWait();
        }
    }

    /**
     * Saves the current contact data to the specified XML file.
     *
     * @param file
     */
    public void saveContactDataXML(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(ContactListWrapperXML.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // wrapping the contacts data
            ContactListWrapperXML wrapperXML = new ContactListWrapperXML();
            wrapperXML.setContacts(contactsData);

            // marshalling and saving xml to the file
            m.marshal(wrapperXML, file);

            // save the filepath to the registry
            setContactFilePath(file);

        } catch (Exception e) {
            // catches any exception
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save data");
            alert.setContentText("Could not save data to file:\n" + file.getPath());

            alert.showAndWait();
        }
    }
}
