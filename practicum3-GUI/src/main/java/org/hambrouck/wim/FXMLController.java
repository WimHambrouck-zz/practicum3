package org.hambrouck.wim;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;


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
    private void kiesInvoer(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        File invoerBestand = directoryChooser.showDialog(hoofdscherm.getScene().getWindow());

        if (invoerBestand != null) {
            txt_invoer.setText(invoerBestand.getAbsolutePath());
        }
    }

    @FXML
    private void maakHandtekeningKlik(ActionEvent event) throws IOException {
        if (btn_maakHandtekening.getText().equals(MAAK_HANDTEKENING)) {
            if (checkFields(true)) {
                log(String.format("Bezig met genereren %s...", IntegriteitsModule.UITVOERBESTAND));
                setDisable(true);
                btn_maakHandtekening.setText(STOP);
                if (maakHandtekening()) {
                    //maakAlert("Klaar!", "Genereren integriteitsbestand", Alert.AlertType.CONFIRMATION);
                    log(String.format("%s aangemaakt,%sBezig met wachten op wijzigingen in map...", IntegriteitsModule.UITVOERBESTAND, System.lineSeparator()));
                    //TODO filewatcher
                    watchMe(new File(txt_invoer.getText()));
                } else {
                    maakAlert("Fout bij genereren integriteitsbestand.", "Daar ging iets mis!", Alert.AlertType.ERROR);
                    setDisable(false);
                    btn_maakHandtekening.setText(MAAK_HANDTEKENING);
                }
            }
        } else if (btn_maakHandtekening.getText().equals(STOP)) {
            timer.stop();
            setDisable(false);
            btn_maakHandtekening.setText(MAAK_HANDTEKENING);
            log("Gestopt. Voer veldjes in en kies actie...");
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

    private static Timer timer;
    private WatchKey watchKey;

    public void watchMe(File map) throws IOException {
        final WatchService watchService = FileSystems.getDefault().newWatchService();
        Path dir = Paths.get(map.getPath());
        watchKey = dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);


        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                List<WatchEvent<?>> events = watchKey.pollEvents();

                for (WatchEvent event : events) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    } else {
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        //checken of het niet de integrity.xml is die een ENTRY_MODIFY triggered
                        if (!filename.toString().equals(IntegriteitsModule.UITVOERBESTAND)) {
                            //als het geen OVERFLOW is, is er iets aangepast, dus handtekening hermaken
                            if (maakHandtekening()) {
                                //maakAlert("Klaar!", "Genereren integriteitsbestand", Alert.AlertType.CONFIRMATION);
                                Platform.runLater(new Runnable() {
                                    public void run() {
                                        log(String.format("Wijziging gedetecteerd, hermaken %s,%sBezig met wachten op wijzigingen in map...", IntegriteitsModule.UITVOERBESTAND, System.lineSeparator()));
                                    }
                                });
                            } else {
                                Platform.runLater(new Runnable() {
                                    public void run() {
                                        maakAlert("Fout bij genereren integriteitsbestand.", "Daar ging iets mis!", Alert.AlertType.ERROR);
                                        setDisable(false);
                                        btn_maakHandtekening.setText(MAAK_HANDTEKENING);
                                    }
                                });
                            }
                            break;
                            // uit de for lus stappen. Zelfs als er meerdere aanpassingen in de eventqueue staan,
                            // moet maakHandtekening() maar één keer worden aangeroepen, gezien die toch de volledige
                            // map afloopt en derhalve dus alle mogelijke aanpassingen in rekening brengt
                        }
                    }

                }

                if (!watchKey.reset()) {
                    timer.stop();
                }
            }
        });

        timer.start();
    }

    private void unWatchMe(File map) {

    }

    public static void stopTimer() {
        if (timer != null)
            timer.stop();
    }

    private void log(String message) {
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
        if (checkFields(true)) {
            //TODO controleer handtekening
        }
    }


    private void maakAlert(String message, String title, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.showAndWait();
    }

    private boolean checkFields(boolean wachtwoordControle) {
        if (txt_invoer.getText().isEmpty()) {
            maakAlert("Gelieve een invoerbestand op te geven.", "Probleem", Alert.AlertType.ERROR);
            txt_invoer.requestFocus();
            return false;
        } else if (txt_wachtwoord.getText().isEmpty()) {
            maakAlert("Gelieve een wachtwoord op te geven.", "Probleem", Alert.AlertType.ERROR);
            txt_wachtwoord.requestFocus();
            return false;
        } else if (wachtwoordControle && txt_wachtwoord_herhaald.getText().isEmpty()) {
            maakAlert("Gelieve je wachtwoord opnieuw op te geven.", "Probleem", Alert.AlertType.ERROR);
            txt_wachtwoord_herhaald.requestFocus();
            return false;
        } else if (wachtwoordControle && (!txt_wachtwoord.getText().equals(txt_wachtwoord_herhaald.getText()))) {
            maakAlert("Wachtwoorden komen niet overeen!", "Probleem", Alert.AlertType.ERROR);
            txt_wachtwoord.requestFocus();
            return false;
        }
        return true;
    }

    public void initialize(URL location, ResourceBundle resources) {

    }


}
