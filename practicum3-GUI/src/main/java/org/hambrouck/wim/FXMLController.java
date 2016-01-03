package org.hambrouck.wim;

import java.io.File;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import javax.naming.AuthenticationException;
import javax.swing.*;

public class FXMLController implements Initializable {


    @FXML
    private Parent hoofdscherm;
    @FXML
    private TextField txt_invoer;
    @FXML
    private TextField txt_uitvoer;
    @FXML
    private PasswordField txt_wachtwoord;
    @FXML
    private PasswordField txt_wachtwoord_herhaald;
    @FXML
    private Button btn_kiesInvoer;
    @FXML
    private Button btn_kiesUitvoer;


    @FXML
    private void kiesInvoer(ActionEvent event)
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        File invoerBestand = directoryChooser.showDialog(hoofdscherm.getScene().getWindow());

        if(invoerBestand != null)
        {
            txt_invoer.setText(invoerBestand.getAbsolutePath());
        }
    }

    @FXML
    private void maakHandtekening(ActionEvent event)
    {
        if(checkFields(true))
        {

        }
    }


    private void maakAlert(String message, String title, Alert.AlertType alertType)
    {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.showAndWait();
    }

    private boolean checkFields(boolean wachtwoordControle)
    {
        if(txt_invoer.getText().isEmpty())
        {
            maakAlert("Gelieve een invoerbestand op te geven.", "Probleem", Alert.AlertType.ERROR);
            txt_invoer.requestFocus();
            return false;
        } else if(txt_wachtwoord.getText().isEmpty())
        {
            maakAlert("Gelieve een wachtwoord op te geven.", "Probleem", Alert.AlertType.ERROR);
            txt_wachtwoord.requestFocus();
            return false;
        } else if(wachtwoordControle && txt_wachtwoord_herhaald.getText().isEmpty())
        {
            maakAlert("Gelieve je wachtwoord opnieuw op te geven.", "Probleem", Alert.AlertType.ERROR);
            txt_wachtwoord_herhaald.requestFocus();
            return false;
        } else if(wachtwoordControle && (!txt_wachtwoord.getText().equals(txt_wachtwoord_herhaald.getText())))
        {
            maakAlert("Wachtwoorden komen niet overeen!", "Probleem", Alert.AlertType.ERROR);
            txt_wachtwoord.requestFocus();
            return false;
        }
        return true;
    }

    public void initialize(URL location, ResourceBundle resources) {

    }
}
