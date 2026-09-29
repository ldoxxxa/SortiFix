package de.hsrm.mi.swt.view.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Dialog zur Eingabe des Zielorts einer Zielstation.
 */
public class ZielDialog extends Dialog<ZielDialog.Ergebnis> {

    private final TextField zielort = new TextField();
    private final Label fehlermeldung = new Label();

    /**
     * Erstellt einen Dialog fuer eine neue Zielstation.
     */
    public ZielDialog() {
        setTitle("Ziel anlegen");
        setHeaderText("Zielort der Zielstation festlegen");

        ButtonType speichernButton = new ButtonType("Speichern", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(speichernButton, ButtonType.CANCEL);
        getDialogPane().setContent(erstelleInhalt());

        Button speichern = (Button) getDialogPane().lookupButton(speichernButton);
        speichern.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!eingabeGueltig()) {
                event.consume();
            }
        });

        setResultConverter(button -> {
            if (button != speichernButton) {
                return null;
            }
            return new Ergebnis(zielort.getText().trim());
        });
    }

    private GridPane erstelleInhalt() {
        zielort.setPromptText("z.B. Terminal A");
        fehlermeldung.setStyle("-fx-text-fill: #de3535;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.add(new Label("Zielort"), 0, 0);
        grid.add(zielort, 1, 0);
        grid.add(fehlermeldung, 0, 1, 2, 1);
        return grid;
    }

    private boolean eingabeGueltig() {
        if (zielort.getText() == null || zielort.getText().isBlank()) {
            fehlermeldung.setText("Bitte einen Zielort eingeben.");
            return false;
        }
        fehlermeldung.setText("");
        return true;
    }

    /**
     * Ergebnis des Zieldialogs.
     *
     * @param zielort Name der Zielstation
     */
    public record Ergebnis(String zielort) {
    }
}
