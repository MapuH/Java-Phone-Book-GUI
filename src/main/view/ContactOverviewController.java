package main.view;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import main.PhoneBook;
import main.model.Contact;

import java.io.File;
import java.util.Optional;

public class ContactOverviewController {

    // left hand side
    @FXML
    private TextField filterField;
    @FXML
    private TableView<Contact> contactsTable;
    @FXML
    private TableColumn<Contact, String> nameColumn;

    // right hand side
    @FXML
    private GridPane detailsGrid;
    @FXML
    private Label nameLabel;
    @FXML
    private Label mobileLabel;
    @FXML
    private Label workLabel;
    @FXML
    private Label homeLabel;
    @FXML
    private Label additionalLabel;
    @FXML
    private Label instructionLabel;

    private PhoneBook phoneBook;
    private SortedList<Contact> sortedContacts;
    private String[] validExtensions = {"*.csv", "*.json", "*.xml"};

    public ContactOverviewController() {
    }

    @FXML
    private void initialize() {
        // initialize contacts table
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        // clear contact details
        showContactDetails(null);

        // listen for selection changes
        contactsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showContactDetails(newValue));

    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param phoneBook
     */
    public void setPhoneBook(PhoneBook phoneBook) {
        this.phoneBook = phoneBook;

        // refers to handleQuit() if application is closed
        phoneBook.getPrimaryStage().setOnCloseRequest(closeEvent -> handleQuit());

        // load contacts in the table
        FilteredList<Contact> filteredContacts = new FilteredList<>(phoneBook.getContactsData(), p -> true);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredContacts.setPredicate(contact -> {
                // if filter is empty display all contacts
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // compare names with filter text
                String filter = newValue.toLowerCase();
                if (contact.getName().toLowerCase().contains(filter)) {
                    return true;
                }

                return false;
            });
        });

        this.sortedContacts = new SortedList<>(filteredContacts);
        this.sortedContacts.comparatorProperty().bind(contactsTable.comparatorProperty());

        contactsTable.setItems(sortedContacts);
    }

    private void showContactDetails(Contact contact) {
        if (contact != null) {
            detailsGrid.setVisible(true);
            nameLabel.setText(contact.getName());
            mobileLabel.setText(contact.getMobile());
            workLabel.setText(contact.getWork());
            homeLabel.setText(contact.getHome());
            additionalLabel.setText(contact.getAdditional());
            instructionLabel.setText("");
        } else {
            detailsGrid.setVisible(false);
            instructionLabel.setText("Select a name to view details");
        }
    }

    /**
     * Called when the user clicks the new button. Opens a dialog to edit
     * details for a new contact.
     */
    @FXML
    private void handleNewContact() {
        Contact tempContact = new Contact();
        boolean clickedOK = phoneBook.showContactEditDialog(tempContact);
        if (clickedOK) {
            phoneBook.getContactsData().add(tempContact);
            phoneBook.setHasChanged(true);
            showContactDetails(tempContact);
        }
    }

    /**
     * Called when the user clicks the edit button. Opens a dialog to edit
     * details for the selected contact.
     */
    @FXML
    private void handleEditContact() {
        Contact selectedContact = contactsTable.getSelectionModel().getSelectedItem();
        if (selectedContact != null) {
            boolean clickedOK = phoneBook.showContactEditDialog(selectedContact);
            if (clickedOK) {
                showContactDetails(selectedContact);
                phoneBook.setHasChanged(true);
            }

        } else {
            // if nothing is selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(phoneBook.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Contact Selected");
            alert.setContentText("Please select a contact from the table.");

            alert.showAndWait();
        }
    }


    /**
     * Called when the user clicks the delete button.
     */
    @FXML
    private void handleDeleteContact() {
        int selectedIndex = contactsTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            int sourceIndex = this.sortedContacts.getSourceIndexFor(phoneBook.getContactsData(), selectedIndex);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(phoneBook.getPrimaryStage());
            alert.setTitle("Delete Contact");
            alert.setHeaderText("Contact will be deleted");
            alert.setContentText("Delete "+ sortedContacts.get(selectedIndex).getName() +", are you sure?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                phoneBook.getContactsData().remove(sourceIndex);
                phoneBook.setHasChanged(true);
            }

        } else {
            // executes if nothing is selected when the delete button is pressed
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(phoneBook.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Contact Selected");
            alert.setContentText("Please select a contact from the table.");

            alert.showAndWait();
        }
    }

    /**
     * Creates a new empty address book.
     */
    @FXML
    private void handleNew() {
        // check if current file was saved before creating a new phone book
        if (phoneBook.getHasChanged()) {
            File file = phoneBook.getContactFilePath();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save Changes First?");
            alert.setHeaderText("Phone book has been updated");
            if (file != null) {
                alert.setContentText("Save changes to " + file.getName() + " before proceeding?");
            } else {
                alert.setContentText("Would you like to save current phone book?");
            }
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                handleSave();
            }
        }

        // continue with creating a new phone book
        phoneBook.getContactsData().clear();
        phoneBook.setContactFilePath(null);
        phoneBook.setHasChanged(false);
    }

    /**
     * Opens a FileChooser to let the user select a phone book to load.
     */
    @FXML
    private void handleOpen() {
        // check if current file was saved before opening a new phone book
        if (phoneBook.getHasChanged()) {
            File file = phoneBook.getContactFilePath();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save Changes First?");
            alert.setHeaderText("Phone book has been updated");
            if (file != null) {
                alert.setContentText("Save changes to " + file.getName() + " before proceeding?");
            } else {
                alert.setContentText("Would you like to save current phone book?");
            }
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                handleSave();
            }
        }

        // continue with opening a new file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Phone Book File");

        // set extension filter
        FileChooser.ExtensionFilter exFilter = new FileChooser.ExtensionFilter("Supported files (*.csv, *.json, *.xml)", validExtensions);
        fileChooser.getExtensionFilters().add(exFilter);

        // show open file dialog
        File file = fileChooser.showOpenDialog(phoneBook.getPrimaryStage());

        phoneBook.loadContactFile(file);
        phoneBook.setHasChanged(false);
    }

    /**
     * Saves the file to the XML file that is currently open. If there is no
     * open file, the "save as" dialog is shown.
     */
    @FXML
    private void handleSave() {
        File contactFile = phoneBook.getContactFilePath();
        if (contactFile != null) {
            phoneBook.saveContactFile(contactFile);
        } else {
            handleSaveAs();
        }
    }

    /**
     * Opens a FileChooser to let the user select a file to save to.
     */
    @FXML
    private void handleSaveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Phone Book File");

        // set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Supported files (*.csv, *.json, *.xml)", validExtensions);
        fileChooser.getExtensionFilters().add(extFilter);

        // show save file dialog
        File file = fileChooser.showSaveDialog(phoneBook.getPrimaryStage());

        if (file != null) {
            phoneBook.saveContactFile(file);
        }
    }

    /**
     * Opens an about dialog.
     */
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("PhoneBook v0.1");
        alert.setContentText("Author: Marin Marinov");

        alert.showAndWait();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleQuit() {
        // check if current file was saved before quitting
        if (phoneBook.getHasChanged()) {
            File file = phoneBook.getContactFilePath();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save Changes First?");
            alert.setHeaderText("Phone book has been updated");
            if (file != null) {
                alert.setContentText("Save changes to " + file.getName() + " before proceeding?");
            } else {
                alert.setContentText("Would you like to save current phone book?");
            }
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                handleSave();
            }
        }

        // terminate program
        System.exit(0);
    }

}
