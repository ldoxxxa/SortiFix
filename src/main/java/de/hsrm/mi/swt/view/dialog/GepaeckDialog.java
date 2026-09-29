package de.hsrm.mi.swt.view.dialog;

import java.util.List;

import de.hsrm.mi.swt.model.gepaeck.Gepaecktyp;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Dialog zur Eingabe von Gepaeckdaten.
 */
public class GepaeckDialog extends Dialog<GepaeckDialog.Ergebnis> {

    private static final String STANDARD_GEWICHT = "20";

    private final ComboBox<String> zielort = new ComboBox<>();
    private final TextField gewicht = new TextField();
    private final ComboBox<Gepaecktyp> gepaecktyp = new ComboBox<>();
    private final Label fehlermeldung = new Label();
    private final List<String> zielorte;

    /**
     * Erstellt einen Dialog fuer ein neues Gepaeckstueck.
     *
     * @param zielorte verfuegbare Zielorte aus dem Anlagenplan
     */
    public GepaeckDialog(List<String> zielorte) {
        this.zielorte = zielorte;
        setTitle("Gepäck anlegen");
        setHeaderText("Eigenschaften des Gepäckstücks festlegen");

        ButtonType speichernButton = new ButtonType("Speichern", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(speichernButton, ButtonType.CANCEL);
        getDialogPane().setContent(erstelleInhalt());

        Button speichern = (Button) getDialogPane().lookupButton(speichernButton);
        speichern.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!eingabenGueltig()) {
                event.consume();
            }
        });

        setResultConverter(button -> {
            if (button != speichernButton) {
                return null;
            }
            return new Ergebnis(
                    zielort.getValue(),
                    Double.parseDouble(gewicht.getText().trim()),
                    gepaecktyp.getValue());
        });
    }

    private GridPane erstelleInhalt() {
        zielort.getItems().addAll(zielorte.stream().distinct().toList());
        if (!zielort.getItems().isEmpty()) {
            zielort.setValue(zielort.getItems().get(0));
        }
        gewicht.setText(STANDARD_GEWICHT);
        gewicht.setPromptText("z.B. 12.5");

        gepaecktyp.getItems().addAll(Gepaecktyp.values());
        gepaecktyp.setValue(Gepaecktyp.KOFFER);

        fehlermeldung.setStyle("-fx-text-fill: #de3535;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        grid.add(new Label("Zielort"), 0, 0);
        grid.add(zielort, 1, 0);
        grid.add(new Label("Gewicht in kg"), 0, 1);
        grid.add(gewicht, 1, 1);
        grid.add(new Label("Gepäckart"), 0, 2);
        grid.add(gepaecktyp, 1, 2);
        grid.add(fehlermeldung, 0, 3, 2, 1);

        return grid;
    }

    private boolean eingabenGueltig() {
        if (zielort.getItems().isEmpty()) {
            fehlermeldung.setText("Bitte zuerst eine Zielstation anlegen.");
            return false;
        }

        if (zielort.getValue() == null || zielort.getValue().isBlank()) {
            fehlermeldung.setText("Bitte einen Zielort auswählen.");
            return false;
        }

        try {
            double gewichtWert = Double.parseDouble(gewicht.getText().trim());
            if (gewichtWert <= 0) {
                fehlermeldung.setText("Das Gewicht muss größer als 0 sein.");
                return false;
            }
        } catch (NumberFormatException | NullPointerException e) {
            fehlermeldung.setText("Bitte ein gültiges Gewicht eingeben.");
            return false;
        }

        if (gepaecktyp.getValue() == null) {
            fehlermeldung.setText("Bitte eine Gepäckart auswählen.");
            return false;
        }

        fehlermeldung.setText("");
        return true;
    }

    /**
     * Ergebnis des Gepaeckdialogs.
     *
     * @param zielort Zielort des Gepaeckstuecks
     * @param gewicht Gewicht in Kilogramm
     * @param typ Art des Gepaeckstuecks
     */
    public record Ergebnis(String zielort, double gewicht, Gepaecktyp typ) {
    }
}
