package de.hsrm.mi.swt.view;

import java.util.function.Consumer;

import de.hsrm.mi.swt.controller.MainMenuController;
import de.hsrm.mi.swt.model.AnlagenPlan;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Hauptansicht der Anwendung.
 * Kombiniert den Kopfbereich mit der Editoransicht.
 */
public class MainView extends BorderPane {

    private static final String PRIMAERFARBE = "#3c7280";

    /**
     * Erstellt die Hauptansicht fuer den vom Controller bereitgestellten Anlagenplan.
     *
     * @param controller Controller der Hauptansicht
     * @param planLaden Aktion, die beim Laden eines neuen Anlagenplans aus dem Editor heraus ausgefuehrt wird
     * @param zurueckZurStartseite Aktion, die beim Klick auf den Zurueck-Pfeil ausgefuehrt wird
     */
    public MainView(MainMenuController controller, Consumer<AnlagenPlan> planLaden, Runnable zurueckZurStartseite) {
        setTop(erstelleKopfbereich(zurueckZurStartseite));
        zeigeEditor(controller, planLaden);
    }

    private void zeigeEditor(MainMenuController controller, Consumer<AnlagenPlan> planLaden) {
        setCenter(new EditorView(
                controller.getAnlagenPlan(),
                planLaden,
                () -> zeigeSimulation(controller, planLaden)));
    }

    private void zeigeSimulation(MainMenuController controller, Consumer<AnlagenPlan> planLaden) {
        setCenter(new SimulationsView(
                controller.getAnlagenPlan(),
                () -> zeigeEditor(controller, planLaden)));
    }

    private HBox erstelleKopfbereich(Runnable zurueckZurStartseite) {
        Button zurueckButton = new Button("\u2190");
        zurueckButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;"
                + " -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand;");
        zurueckButton.setOnAction(event -> zurueckZurStartseite.run());

        Region abstandshalter = new Region();
        HBox.setHgrow(abstandshalter, Priority.ALWAYS);

        Label titel = new Label("SortiFX");
        titel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        HBox kopfbereich = new HBox(zurueckButton, abstandshalter, titel);
        kopfbereich.setAlignment(Pos.CENTER_LEFT);
        kopfbereich.setPadding(new Insets(14, 18, 14, 18));
        kopfbereich.setStyle("-fx-background-color: " + PRIMAERFARBE + ";");
        return kopfbereich;
    }
}
