package de.hsrm.mi.swt;

import de.hsrm.mi.swt.controller.MainMenuController;
import de.hsrm.mi.swt.model.AnlagenPlan;
import de.hsrm.mi.swt.view.MainView;
import de.hsrm.mi.swt.view.StartView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Einstiegspunkt der JavaFX-Anwendung SortiFX.
 */
public class SortiFXMain extends Application {

    private static final int START_BREITE = 1000;
    private static final int START_HOEHE = 700;

    /**
     * Startet die JavaFX-Laufzeit.
     *
     * @param args Kommandozeilenargumente der Anwendung
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initialisiert das Hauptfenster der Anwendung mit der Startansicht.
     * Von dort aus kann ein neuer Anlagenplan erstellt oder ein bestehender geladen werden.
     *
     * @param primaryStage von JavaFX bereitgestelltes Hauptfenster
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SortiFX");
        primaryStage.setScene(new Scene(new StackPane(), START_BREITE, START_HOEHE));
        zeigeStartseite(primaryStage);
        primaryStage.show();
    }

    /**
     * Zeigt die Startansicht mit einem frischen Controller an.
     * Wird sowohl beim App-Start als auch beim Zurueck-Pfeil im Editor aufgerufen.
     *
     * @param primaryStage Hauptfenster, dessen Szeneninhalt gewechselt wird
     */
    private void zeigeStartseite(Stage primaryStage) {
        MainMenuController controller = new MainMenuController();

        StartView startView = new StartView(
                controller.getStandardRasterBreite(),
                controller.getStandardRasterHoehe(),
                (breite, hoehe) -> {
                    controller.erstelleNeuenAnlagenPlan(breite, hoehe);
                    zeigeEditor(primaryStage, controller);
                },
                anlagenPlan -> planGeladen(primaryStage, controller, anlagenPlan));

        primaryStage.getScene().setRoot(startView);
    }

    /**
     * Uebernimmt einen geladenen Anlagenplan in den Controller und zeigt ihn im Editor an.
     * Wird sowohl von der Startansicht als auch vom Laden-Button im Editor selbst aufgerufen.
     *
     * @param primaryStage Hauptfenster, dessen Szeneninhalt gewechselt wird
     * @param controller Controller, der den geladenen Plan uebernimmt
     * @param anlagenPlan geladener Anlagenplan
     */
    private void planGeladen(Stage primaryStage, MainMenuController controller, AnlagenPlan anlagenPlan) {
        controller.setAnlagenPlan(anlagenPlan);
        zeigeEditor(primaryStage, controller);
    }

    /**
     * @param primaryStage Hauptfenster, dessen Szeneninhalt gewechselt wird
     * @param controller Controller mit dem anzuzeigenden Anlagenplan
     */
    private void zeigeEditor(Stage primaryStage, MainMenuController controller) {
        MainView mainView = new MainView(controller,
                anlagenPlan -> planGeladen(primaryStage, controller, anlagenPlan),
                () -> zeigeStartseite(primaryStage));
        primaryStage.getScene().setRoot(mainView);
    }
}