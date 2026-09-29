package de.hsrm.mi.swt.view.dialog;

import java.util.List;

import de.hsrm.mi.swt.model.elemente.Kriterium;
import de.hsrm.mi.swt.model.elemente.Richtung;
import de.hsrm.mi.swt.model.elemente.Weichenregel;
import de.hsrm.mi.swt.model.gepaeck.Gepaecktyp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Dialog zur Konfiguration einer Weiche.
 */
public class WeichenDialog extends Dialog<WeichenDialog.Ergebnis> {

    private final ComboBox<Richtung> eingangsrichtung = new ComboBox<>();
    private final ComboBox<Richtung> standardrichtung = new ComboBox<>();
    private final ComboBox<Kriterium> kriterium = new ComboBox<>();
    private final ComboBox<String> zielwert = new ComboBox<>();
    private final TextField gewichtswert = new TextField();
    private final ComboBox<Gepaecktyp> gepaeckartwert = new ComboBox<>();
    private final StackPane werteingabe = new StackPane();
    private final ComboBox<Richtung> ausgangsrichtung = new ComboBox<>();
    private final ObservableList<Weichenregel> regeln = FXCollections.observableArrayList();
    private final ListView<Weichenregel> regelliste = new ListView<>(regeln);
    private final Label fehlermeldung = new Label();
    private final List<String> zielorte;

    /**
     * Erstellt einen Dialog fuer eine einfache Weichenregel.
     */
    public WeichenDialog() {
        this(List.of());
    }

    /**
     * Erstellt einen Dialog fuer eine einfache Weichenregel.
     *
     * @param zielorte verfuegbare Zielorte aus dem Anlagenplan
     */
    public WeichenDialog(List<String> zielorte) {
        this.zielorte = zielorte;
        setTitle("Weiche konfigurieren");
        setHeaderText("Routingregel für die Weiche festlegen");

        ButtonType speichernButton = new ButtonType("Speichern", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(speichernButton, ButtonType.CANCEL);
        getDialogPane().setContent(erstelleInhalt());

        Button speichern = (Button) getDialogPane().lookupButton(speichernButton);
        speichern.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (regeln.isEmpty()) {
                fehlermeldung.setText("Bitte mindestens eine Regel hinzufügen.");
                event.consume();
            }
        });

        setResultConverter(button -> {
            if (button != speichernButton) {
                return null;
            }

            return new Ergebnis(eingangsrichtung.getValue(), standardrichtung.getValue(), List.copyOf(regeln));
        });
    }

    private VBox erstelleInhalt() {
        eingangsrichtung.getItems().addAll(Richtung.values());
        eingangsrichtung.setValue(Richtung.WEST);

        standardrichtung.getItems().addAll(Richtung.values());
        standardrichtung.setValue(Richtung.SUED);

        kriterium.getItems().addAll(Kriterium.values());
        kriterium.setValue(Kriterium.ZIELORT);
        kriterium.valueProperty().addListener((obs, alt, neu) -> aktualisiereWerteingabe());

        ausgangsrichtung.getItems().addAll(Richtung.values());
        ausgangsrichtung.setValue(Richtung.OST);

        zielwert.getItems().addAll(zielorte.stream().distinct().toList());
        if (!zielwert.getItems().isEmpty()) {
            zielwert.setValue(zielwert.getItems().get(0));
        }

        gewichtswert.setPromptText("z.B. 20");
        gewichtswert.setTextFormatter(new TextFormatter<>(aenderung ->
                aenderung.getControlNewText().matches("\\d*(\\.\\d*)?") ? aenderung : null));

        gepaeckartwert.getItems().addAll(Gepaecktyp.values());
        gepaeckartwert.setValue(Gepaecktyp.KOFFER);

        Button hinzufuegen = new Button("Regel hinzufügen");
        hinzufuegen.setOnAction(event -> fuegeRegelHinzu());

        Button entfernen = new Button("Ausgewählte Regel entfernen");
        entfernen.setOnAction(event -> entferneAusgewaehlteRegel());

        regelliste.setPrefHeight(140);
        regelliste.setCellFactory(liste -> new ListCell<>() {
            @Override
            protected void updateItem(Weichenregel regel, boolean leer) {
                super.updateItem(regel, leer);
                setText(leer || regel == null ? null : beschreibeRegel(regel));
            }
        });

        fehlermeldung.setStyle("-fx-text-fill: #de3535;");
        aktualisiereWerteingabe();

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        grid.add(new Label("Eingangsrichtung"), 0, 0);
        grid.add(eingangsrichtung, 1, 0);
        grid.add(new Label("Standardrichtung"), 0, 1);
        grid.add(standardrichtung, 1, 1);
        grid.add(new Label("Kriterium"), 0, 2);
        grid.add(kriterium, 1, 2);
        grid.add(new Label("Vergleichswert"), 0, 3);
        grid.add(werteingabe, 1, 3);
        grid.add(new Label("Ausgangsrichtung"), 0, 4);
        grid.add(ausgangsrichtung, 1, 4);

        HBox aktionen = new HBox(8, hinzufuegen, entfernen);
        VBox inhalt = new VBox(12,
                grid,
                aktionen,
                new Label("Hinzugefügte Regeln"),
                regelliste,
                fehlermeldung);
        inhalt.setPadding(new Insets(16));
        return inhalt;
    }

    private void aktualisiereWerteingabe() {
        werteingabe.getChildren().clear();
        switch (kriterium.getValue()) {
            case ZIELORT -> werteingabe.getChildren().add(zielwert);
            case GEWICHT -> werteingabe.getChildren().add(gewichtswert);
            case GEPÄCKART -> werteingabe.getChildren().add(gepaeckartwert);
        }
    }

    private String ermittleVergleichswert() {
        return switch (kriterium.getValue()) {
            case ZIELORT -> zielwert.getValue();
            case GEWICHT -> gewichtswert.getText();
            case GEPÄCKART -> gepaeckartwert.getValue().name();
        };
    }

    private void fuegeRegelHinzu() {
        if (!istAktuelleRegelGueltig()) {
            return;
        }

        regeln.add(new Weichenregel(
                kriterium.getValue(),
                ermittleVergleichswert(),
                ausgangsrichtung.getValue(),
                regeln.size() + 1));
        fehlermeldung.setText("");
    }

    private void entferneAusgewaehlteRegel() {
        Weichenregel regel = regelliste.getSelectionModel().getSelectedItem();
        if (regel == null) {
            fehlermeldung.setText("Bitte zuerst eine Regel auswählen.");
            return;
        }
        regeln.remove(regel);
        aktualisierePrioritaeten();
        fehlermeldung.setText("");
    }

    private boolean istAktuelleRegelGueltig() {
        if (ausgangsrichtung.getValue() == null) {
            fehlermeldung.setText("Bitte eine Ausgangsrichtung auswählen.");
            return false;
        }

        switch (kriterium.getValue()) {
            case ZIELORT -> {
                if (zielwert.getValue() == null || zielwert.getValue().isBlank()) {
                    fehlermeldung.setText("Bitte zuerst eine Zielstation anlegen oder auswählen.");
                    return false;
                }
            }
            case GEWICHT -> {
                try {
                    double gewicht = Double.parseDouble(gewichtswert.getText());
                    if (gewicht <= 0) {
                        fehlermeldung.setText("Das Gewicht muss größer als 0 sein.");
                        return false;
                    }
                } catch (NumberFormatException | NullPointerException e) {
                    fehlermeldung.setText("Bitte ein gültiges Gewicht eingeben.");
                    return false;
                }
            }
            case GEPÄCKART -> {
                if (gepaeckartwert.getValue() == null) {
                    fehlermeldung.setText("Bitte eine Gepäckart auswählen.");
                    return false;
                }
            }
        }
        return true;
    }

    private void aktualisierePrioritaeten() {
        for (int i = 0; i < regeln.size(); i++) {
            regeln.get(i).setPrioritaet(i + 1);
        }
    }

    private String beschreibeRegel(Weichenregel regel) {
        return regel.getPrioritaet() + ". "
                + regel.getKriterium() + " = " + regel.getWert()
                + " -> " + regel.getAusgangsrichtung();
    }

    /**
     * Ergebnis des Weichendialogs.
     *
     * @param eingangsrichtung Richtung, aus der Gepaeck in die Weiche eintritt
     * @param standardrichtung Standardrichtung, wenn keine Regel passt
     * @param regeln konfigurierte Weichenregeln
     */
    public record Ergebnis(Richtung eingangsrichtung, Richtung standardrichtung, List<Weichenregel> regeln) {
    }
}
