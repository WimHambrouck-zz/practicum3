package org.hambrouck.wim;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
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


    public static final String MAAK_HANDTEKENING = "Maak handtekening";
    public static final String STOP = "Stop";

    private final IntegriteitsModule integriteitsModule = new IntegriteitsModule();

    @FXML
    private Parent hoofdscherm;
    @FXML
    private TextField txt_invoer;
    @FXML
    private Button btn_kiesInvoer;
    @FXML
    private PasswordField txt_wachtwoord;
    @FXML
    private PasswordField txt_wachtwoord_herhaald;
    @FXML
    private Button btn_maakHandtekening;
    @FXML
    private Button btn_controleerHandtekening;
    @FXML
    private Label lbl_status;

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
    private void maakHandtekeningKlik(ActionEvent event)
    {
        if(btn_maakHandtekening.getText().equals(MAAK_HANDTEKENING))
        {
            if (checkFields(true)) {
                log(String.format("Bezig met genereren %s...", IntegriteitsModule.UITVOERBESTAND));
                setDisable(true);
                btn_maakHandtekening.setText(STOP);
                if(maakHandtekening())
                {
                    //maakAlert("Klaar!", "Genereren integriteitsbestand", Alert.AlertType.CONFIRMATION);
                    log(String.format("%s aangemaakt,%sBezig met wachten op wijzigingen in map...", IntegriteitsModule.UITVOERBESTAND, System.lineSeparator()));
                    //TODO filewatcher
                } else {
                    maakAlert("Fout bij genereren integriteitsbestand.", "Daar ging iets mis!", Alert.AlertType.ERROR);
                    setDisable(false);
                    btn_maakHandtekening.setText(MAAK_HANDTEKENING);
                }
            }
        } else if(btn_maakHandtekening.getText().equals(STOP)){
            setDisable(false);
            btn_maakHandtekening.setText(MAAK_HANDTEKENING);
        }
    }

    private boolean maakHandtekening() {
        try {
            integriteitsModule.maakHandtekening(new File(txt_invoer.getText()), txt_wachtwoord.getText());
            File resultFile = new File(txt_invoer.getText(), IntegriteitsModule.UITVOERBESTAND);
            return resultFile.exists();
        } catch (Exception e) {
            e.printStackTrace();
            maakAlert(String.format("Probleem: %s", e.getMessage()), "Daar ging iets mis!", Alert.AlertType.ERROR);
            log("Fout, probeer opnieuw...");
            return false;
        }
    }

    public void watchMe(File map, String wachtwoord) throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path dir = Paths.get(map.getPath());
        WatchKey key = dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        key.pollEvents();

    }

    private void log(String message)
    {
        Date timestamp = new Date();
        lbl_status.setText(String.format("%s - %s", new SimpleDateFormat("HH:mm:ss").format(timestamp), message));
    }


    private void setDisable(boolean disabled) {
        btn_controleerHandtekening.setDisable(disabled);
        txt_invoer.setDisable(disabled);
        btn_kiesInvoer.setDisable(disabled);
        txt_wachtwoord.setDisable(disabled);
        txt_wachtwoord_herhaald.setDisable(disabled);
    }

    @FXML
    private void controleerHandtekeningKlik(ActionEvent actionEvent) {
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
