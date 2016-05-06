package main.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import main.PhoneBook;
import main.model.Contact;

import java.util.Optional;

public class ContactOverviewController {

    // left hand side
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
        contactsTable.setItems(phoneBook.getContactsData());
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
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(phoneBook.getPrimaryStage());
            alert.setTitle("Delete Contact");
            alert.setHeaderText("Contact will be deleted");
            alert.setContentText("Are you sure?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                contactsTable.getItems().remove(selectedIndex);
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

}
