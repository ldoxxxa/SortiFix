package de.hsrm.mi.swt.view;

import de.hsrm.mi.swt.controller.SimulationsController;
import de.hsrm.mi.swt.model.AnlagenPlan;
import de.hsrm.mi.swt.view.components.RasterPane;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Ansicht zum Starten, Pausieren und Zuruecksetzen der Simulation.
 */
public class SimulationsView extends BorderPane {

    private static final String SEKUNDAERFARBE = "#eef5f7";
    private static final String AKTIONSFARBE = "#619163";
    private static final String WARNFARBE = "#de3535";
    private static final String PAUSEFARBE = "#e0a300";
    private static final String STATUS_STANDARD = "-fx-background-color: white; -fx-text-fill: #333333;";

    private final SimulationsController controller;
    private final Label status;
    private Timeline simulation;
    private Button pauseButton;

    /**
     * Erstellt die Simulationsansicht fuer einen Anlagenplan.
     *
     * @param anlagenPlan zu simulierender Anlagenplan
     * @param zurueckZumEditor Aktion zum Zurueckkehren in den Editor
     */
    public SimulationsView(AnlagenPlan anlagenPlan, Runnable zurueckZumEditor) {
        this.controller = new SimulationsController(anlagenPlan);
        this.status = erstelleStatusleiste(anlagenPlan);

        VBox zentrumInhalt = new VBox(12, new RasterPane(anlagenPlan), erstelleAktionsleiste(zurueckZumEditor));
        zentrumInhalt.setAlignment(Pos.TOP_CENTER);

        ScrollPane zentrum = new ScrollPane(zentrumInhalt);
        zentrum.setFitToWidth(true);
        zentrum.setFitToHeight(false);
        zentrum.setPannable(true);
        zentrum.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        setCenter(zentrum);
        setBottom(status);
        setStyle("-fx-background-color: " + SEKUNDAERFARBE + ";");
    }

    private HBox erstelleAktionsleiste(Runnable zurueckZumEditor) {
        Button simulationButton = new Button("Simulation starten");
        simulationButton.setStyle("-fx-background-color: " + AKTIONSFARBE + "; -fx-text-fill: white;");
        simulationButton.setOnAction(event -> starteSimulation());

        pauseButton = new Button("Pause");
        pauseButton.setStyle("-fx-background-color: " + PAUSEFARBE + "; -fx-text-fill: white;");
        pauseButton.setOnAction(event -> pausiereOderFortsetzeSimulation());

        Button resetButton = new Button("Reset");
        resetButton.setStyle("-fx-background-color: " + WARNFARBE + "; -fx-text-fill: white;");
        resetButton.setOnAction(event -> setzeSimulationZurueck());

        Button zurueckButton = new Button("Zurück zum Editor");
        zurueckButton.setOnAction(event -> {
            stoppeTimeline();
            controller.stoppeSimulation();
            zurueckZumEditor.run();
        });

        HBox aktionsleiste = new HBox(12, simulationButton, pauseButton, resetButton, zurueckButton);
        aktionsleiste.setAlignment(Pos.CENTER);
        aktionsleiste.setPadding(new Insets(4, 0, 12, 0));
        return aktionsleiste;
    }

    private Label erstelleStatusleiste(AnlagenPlan anlagenPlan) {
        Label statusleiste = new Label("Simulation: " + anlagenPlan.getName());
        statusleiste.setPadding(new Insets(8, 16, 8, 16));
        statusleiste.setStyle(STATUS_STANDARD);
        return statusleiste;
    }

    private void starteSimulation() {
        if (!controller.kannSimulationStarten()) {
            status.setText("Simulation braucht gültigen Förderweg und mindestens ein Gepäckstück");
            status.setStyle("-fx-background-color: " + WARNFARBE + "; -fx-text-fill: white;");
            return;
        }

        stoppeTimeline();
        pauseButton.setText("Pause");

        controller.starteSimulation();
        status.setStyle(STATUS_STANDARD);
        status.setText("Simulation läuft");

        simulation = new Timeline(new KeyFrame(Duration.millis(450), event -> {
            boolean laeuftWeiter = controller.fuehreSimulationsschrittAus();
            String meldung = controller.getLetzteSimulationsMeldung();
            if (meldung != null) {
                status.setText(meldung);
                status.setStyle("-fx-background-color: " + WARNFARBE + "; -fx-text-fill: white;");
            }
            if (!laeuftWeiter && simulation != null) {
                simulation.stop();
                if (meldung == null) {
                    status.setStyle(STATUS_STANDARD);
                    status.setText("Simulation beendet");
                }
            }
        }));
        simulation.setCycleCount(Timeline.INDEFINITE);
        simulation.play();
    }

    private void pausiereOderFortsetzeSimulation() {
        if (simulation == null) {
            return;
        }
        if (simulation.getStatus() == Animation.Status.RUNNING) {
            simulation.pause();
            controller.pausiereSimulation();
            pauseButton.setText("Weiter");
            status.setStyle(STATUS_STANDARD);
            status.setText("Simulation pausiert");
        } else if (simulation.getStatus() == Animation.Status.PAUSED) {
            simulation.play();
            controller.setzeSimulationFort();
            pauseButton.setText("Pause");
            status.setStyle(STATUS_STANDARD);
            status.setText("Simulation läuft");
        }
    }

    private void setzeSimulationZurueck() {
        stoppeTimeline();
        pauseButton.setText("Pause");
        controller.reset();
        status.setStyle(STATUS_STANDARD);
        status.setText("Simulation zurückgesetzt");
    }

    private void stoppeTimeline() {
        if (simulation != null) {
            simulation.stop();
            simulation = null;
        }
    }
}
