package de.hsrm.mi.swt.view;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.hsrm.mi.swt.controller.SpeicherController;
import de.hsrm.mi.swt.model.AnlagenPlan;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Startansicht der Anwendung.
 * Leitet per Startbutton in den Editor weiter.
 */
public class StartView extends VBox {

    private static final String PRIMAERFARBE = "#3c7280";
    private static final String AKTIONSFARBE = "#619163";
    private static final String FEHLERFARBE = "#de3535";
    private static final int MIN_RASTER_GROESSE = 5;
    private static final int MAX_RASTER_GROESSE = 40;

    private final SpeicherController speicherController = new SpeicherController();

    /**
     * Erstellt die Startansicht.
     *
     * @param standardBreite vorausgewaehlte Rasterbreite
     * @param standardHoehe vorausgewaehlte Rasterhoehe
     * @param editorOeffnen Aktion zum Oeffnen der Editoransicht
     * @param planLaden Aktion zum Oeffnen eines geladenen Anlagenplans
     */
    public StartView(int standardBreite, int standardHoehe, BiConsumer<Integer, Integer> editorOeffnen,
            Consumer<AnlagenPlan> planLaden) {
        Label titel = new Label("SortiFX");
        titel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: " + PRIMAERFARBE + ";");

        Label untertitel = new Label("Gepäckförderanlage planen und simulieren");
        untertitel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333;");

        Spinner<Integer> breiteSpinner = erstelleGroessenSpinner(standardBreite);
        Spinner<Integer> hoeheSpinner = erstelleGroessenSpinner(standardHoehe);
        Label breiteFehler = erstelleFehlerLabel();
        Label hoeheFehler = erstelleFehlerLabel();

        VBox groessenAuswahl = new VBox(8,
                erstelleBeschriftetesFeld("Rasterbreite", breiteSpinner, breiteFehler),
                erstelleBeschriftetesFeld("Rasterhöhe", hoeheSpinner, hoeheFehler));
        groessenAuswahl.setAlignment(Pos.CENTER);
        groessenAuswahl.setMaxWidth(220);

        Button startButton = new Button("Editor starten");
        startButton.setDefaultButton(true);
        startButton.setMinWidth(180);
        startButton.setPadding(new Insets(10, 18, 10, 18));
        startButton.setStyle("-fx-background-color: " + AKTIONSFARBE
                + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        startButton.setOnAction(event -> {
            Integer breite = validiereGroesse(breiteSpinner, breiteFehler);
            Integer hoehe = validiereGroesse(hoeheSpinner, hoeheFehler);
            if (breite != null && hoehe != null) {
                editorOeffnen.accept(breite, hoehe);
            }
        });

        Button ladenButton = new Button("Anlagenplan laden");
        ladenButton.setMinWidth(180);
        ladenButton.setPadding(new Insets(10, 18, 10, 18));
        ladenButton.setOnAction(event -> ladeAnlagenPlan(planLaden));

        setAlignment(Pos.CENTER);
        setSpacing(18);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: #eef5f7;");
        getChildren().addAll(titel, untertitel, groessenAuswahl, startButton, ladenButton);
    }

    private Spinner<Integer> erstelleGroessenSpinner(int startwert) {
        Spinner<Integer> spinner = new Spinner<>(MIN_RASTER_GROESSE, MAX_RASTER_GROESSE, startwert);
        spinner.setEditable(true);
        spinner.setMaxWidth(120);
        spinner.getEditor().setTextFormatter(new TextFormatter<>(aenderung -> {
            String text = aenderung.getControlNewText();
            if (text.matches("\\d*")) {
                return aenderung;
            }
            return null;
        }));
        return spinner;
    }

    private VBox erstelleBeschriftetesFeld(String text, Spinner<Integer> spinner, Label fehlerLabel) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label hinweis = new Label("Erlaubt: mindestens 5, maximal 40");
        hinweis.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");

        VBox feld = new VBox(4, label, hinweis, spinner, fehlerLabel);
        feld.setAlignment(Pos.CENTER);
        return feld;
    }

    private Label erstelleFehlerLabel() {
        Label label = new Label("Eingabe fehlerhaft");
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: " + FEHLERFARBE + ";");
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    private Integer validiereGroesse(Spinner<Integer> spinner, Label fehlerLabel) {
        try {
            int wert = Integer.parseInt(spinner.getEditor().getText());
            boolean gueltig = wert >= MIN_RASTER_GROESSE && wert <= MAX_RASTER_GROESSE;
            markiereValidierung(spinner, fehlerLabel, gueltig);
            return gueltig ? wert : null;
        } catch (NumberFormatException exception) {
            markiereValidierung(spinner, fehlerLabel, false);
            return null;
        }
    }

    private void markiereValidierung(Spinner<Integer> spinner, Label fehlerLabel, boolean gueltig) {
        if (gueltig) {
            spinner.getEditor().setStyle("");
            fehlerLabel.setVisible(false);
            fehlerLabel.setManaged(false);
            return;
        }

        spinner.getEditor().setStyle("-fx-border-color: " + FEHLERFARBE
                + "; -fx-border-width: 2px; -fx-border-radius: 3px;");
        fehlerLabel.setVisible(true);
        fehlerLabel.setManaged(true);
    }

    private void ladeAnlagenPlan(Consumer<AnlagenPlan> planLaden) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Anlagenplan laden");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SortiFX JSON", "*.json"));

        Window window = getScene() == null ? null : getScene().getWindow();
        java.io.File datei = fileChooser.showOpenDialog(window);
        if (datei == null) {
            return;
        }

        try {
            planLaden.accept(speicherController.lade(datei.toPath()));
        } catch (Exception exception) {
            zeigeFehler("Anlagenplan konnte nicht geladen werden.");
        }
    }

    private void zeigeFehler(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}