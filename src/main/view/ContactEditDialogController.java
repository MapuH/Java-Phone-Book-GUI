package main.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import main.model.Contact;

public class ContactEditDialogController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField mobileField;
    @FXML
    private TextField workField;
    @FXML
    private TextField homeField;
    @FXML
    private TextField additionalField;

    private Stage dialogStage;
    private Contact contact;
    private boolean clickedOK = false;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;

        // listener for pressing ESCAPE/ENTER
        dialogStage.getScene().setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                handleCancel();
            } else if (keyEvent.getCode() == KeyCode.ENTER) {
                handleOK();
            }
        });

    }

    /**
     * Sets the contact to be edited in the dialog.
     *
     * @param contact
     */
    public void setContact(Contact contact) {
        this.contact = contact;

        nameField.setText(contact.getName());
        mobileField.setText(contact.getMobile());
        workField.setText(contact.getWork());
        homeField.setText(contact.getHome());
        additionalField.setText(contact.getAdditional());
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     *
     * @return
     */
    public boolean isOkClicked() {
        return clickedOK;
    }

    /**
     * Called if the user clicks OK.
     */
    @FXML
    private void handleOK() {
        if (isInputValid()) {
            contact.setName(nameField.getText());
            contact.setMobile(mobileField.getText());
            contact.setWork(workField.getText());
            contact.setHome(homeField.getText());
            contact.setAdditional(additionalField.getText());

            clickedOK = true;
            dialogStage.close();
        }
    }

    /**
     * Called if the user clicks Cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Input validation
     *
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().length() == 0) {
            errorMessage += "Name field is mandatory!\n\n";
        }

        if (nameField.getText() != null && nameField.getText().length() > 0 && !nameField.getText().matches("[^,\"]{2,50}")) {
            errorMessage += "Name cannot contain commas or quotes\n" +
                    "and has to be between 2 and 50 characters long!\n\n";
        }

        if ((mobileField.getText() != null && mobileField.getText().length() > 0 && !mobileField.getText().matches("[0-9+, ]{3,30}")) ||
                (workField.getText() != null && workField.getText().length() > 0 && !workField.getText().matches("[0-9+, ]{3,30}")) ||
                (homeField.getText() != null && homeField.getText().length() > 0 && !homeField.getText().matches("[0-9+, ]{3,30}")) ||
                (additionalField.getText() != null && additionalField.getText().length() > 0 && !additionalField.getText().matches("[0-9+, ]{3,30}"))) {
            errorMessage += "Phone numbers can contain only digits, spaces and '+'!\n" +
                    "Min length: 3. Max length: 30.\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Input");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);

            alert.getDialogPane().setPrefHeight(180.0);
            alert.showAndWait();

            return false;
        }

    }

}
