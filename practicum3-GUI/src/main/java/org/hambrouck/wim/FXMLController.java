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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Kies invoerbestand");

        File invoerBestand = fileChooser.showOpenDialog(hoofdscherm.getScene().getWindow());

        if(invoerBestand != null)
        {
            String invoerPad = invoerBestand.getAbsolutePath();
            String uitvoerExt = invoerPad;
            uitvoerExt = uitvoerExt.replace("\\", "/");
            uitvoerExt = invoerPad.substring(uitvoerExt.lastIndexOf("/"));
            uitvoerExt = uitvoerExt.substring(uitvoerExt.indexOf("."));


            txt_invoer.setText(invoerPad);
            txt_uitvoer.setText(String.format("%s_uitvoer%s", invoerPad.substring(0, invoerPad.indexOf(uitvoerExt)), uitvoerExt));
        }
    }

    @FXML
    private void kiesUitvoer(ActionEvent event)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Kies uitvoerbestand");

        File invoerBestand = fileChooser.showOpenDialog(hoofdscherm.getScene().getWindow());

        if(invoerBestand != null)
        {
            txt_uitvoer.setText(invoerBestand.getAbsolutePath());
        }
    }

    public void initialize(URL location, ResourceBundle resources) {

    }
}
