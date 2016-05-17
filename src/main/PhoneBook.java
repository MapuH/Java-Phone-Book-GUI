package main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import main.model.Contact;
import main.model.ContactJSON;
import main.model.ContactListWrapperXML;
import main.view.ContactEditDialogController;
import main.view.ContactOverviewController;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
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

    private Stage primaryStage;
    private VBox overview;
    private ObservableList<Contact> contactsData = FXCollections.observableArrayList();

    public PhoneBook() {
    }

    public ObservableList<Contact> getContactsData() {
        return contactsData;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
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
        loadContactFile(file);
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
     * Helper function for loading files.
     *
     * @param file
     */
    public void loadContactFile(File file) {
        if (file != null) {
            if (file.getName().endsWith(".xml")) {
                loadContactDataXML(file);
            } else if (file.getName().endsWith(".csv")) {
                loadContactDataCSV(file);
            } else if (file.getName().endsWith(".json")) {
                loadContactDataJSON(file);
            }
        }
    }

    /**
     * Helper function for saving files.
     *
     * @param file
     */
    public void saveContactFile(File file) {
        if (file.getName().endsWith(".xml")) {
            saveContactDataXML(file);
        } else if (file.getName().endsWith(".csv")) {
            saveContactDataCSV(file);
        } else if (file.getName().endsWith(".json")) {
            saveContactDataJSON(file);
        }

    }

    /**
     * Loads contact data from the specified XML file. The current contact data will
     * be replaced.
     *
     * @param file
     */
    private void loadContactDataXML(File file) {
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
    private void saveContactDataXML(File file) {
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

    /**
     * Loads contact data from the specified CSV file. The current contact data will
     * be replaced.
     *
     * @param file
     */
    private void loadContactDataCSV(File file) {
        try (CSVReader reader = new CSVReader(new FileReader(file), ',', '\"', 1)) {

            contactsData.clear();
            String[] contactEntry = reader.readNext();

            while (contactEntry != null) {
                String name = contactEntry[0];
                String mobile = contactEntry[1];
                String work = contactEntry[2];
                String home = contactEntry[3];
                String additional = contactEntry[4];
                contactsData.add(new Contact(name, mobile, work, home, additional));

                contactEntry = reader.readNext();
            }

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
     * Saves the current contact data to the specified CSV file.
     *
     * @param file
     */
    private void saveContactDataCSV(File file) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {

            String[] columns = {"Name", "Mobile", "Work", "Home", "Additional"};
            writer.writeNext(columns);

            for (Contact contact : contactsData) {
                String[] contactEntry = new String[5];
                contactEntry[0] = contact.getName();
                contactEntry[1] = contact.getMobile();
                contactEntry[2] = contact.getWork();
                contactEntry[3] = contact.getHome();
                contactEntry[4] = contact.getAdditional();

                writer.writeNext(contactEntry, true);
            }

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

    /**
     * Loads contact data from the specified CSV file. The current contact data will
     * be replaced.
     *
     * @param file
     */
    private void loadContactDataJSON(File file) {
        try (FileReader reader = new FileReader(file)) {

            Gson gson = new Gson();
            contactsData.clear();

            List<ContactJSON> contactEntries = gson.fromJson(reader, new TypeToken<List<ContactJSON>>(){}.getType());
            for (ContactJSON entry : contactEntries) {
                Contact contact = new Contact(entry.getName(), entry.getMobile(), entry.getWork(),
                        entry.getWork(), entry.getAdditional());
                contactsData.add(contact);
            }

            // save filepath to the registry
            setContactFilePath(file);

        } catch (Exception e) {
            // catches any exception
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load data");
            alert.setContentText("Could not load data from file:\n" + file.getPath());

            alert.showAndWait();
        }
    }


    /**
     * Saves the current contact data to the specified JSON file.
     *
     * @param file
     */
    private void saveContactDataJSON(File file) {
        try (FileWriter writer = new FileWriter(file)) {

            List<ContactJSON> contactEntries = new ArrayList<>();
            for (Contact contact : contactsData) {
                ContactJSON entry = new ContactJSON(contact.getName(), contact.getMobile(), contact.getWork(),
                        contact.getHome(), contact.getAdditional());
                contactEntries.add(entry);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(contactEntries, writer);

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
