package de.hsrm.mi.swt.view;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.hsrm.mi.swt.controller.EditorController;
import de.hsrm.mi.swt.controller.EditorController.Werkzeug;
import de.hsrm.mi.swt.controller.SpeicherController;
import de.hsrm.mi.swt.model.AnlagenPlan;
import de.hsrm.mi.swt.model.Position;
import de.hsrm.mi.swt.model.elemente.Foerderelement;
import de.hsrm.mi.swt.model.elemente.Gerade;
import de.hsrm.mi.swt.model.elemente.Knick;
import de.hsrm.mi.swt.model.elemente.Richtung;
import de.hsrm.mi.swt.model.elemente.Weiche;
import de.hsrm.mi.swt.model.elemente.Ziel;
import de.hsrm.mi.swt.model.logik.ValidierungsErgebnis;
import de.hsrm.mi.swt.view.components.FoerderelementNode;
import de.hsrm.mi.swt.view.components.GepaeckstueckNode;
import de.hsrm.mi.swt.view.components.RasterPane;
import de.hsrm.mi.swt.view.dialog.GepaeckDialog;
import de.hsrm.mi.swt.view.dialog.WeichenDialog;
import de.hsrm.mi.swt.view.dialog.ZielDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Editoransicht fuer den Anlagenplan.
 * Stellt Werkzeugleiste, Rasterbereich und Statusleiste dar.
 */
public class EditorView extends BorderPane {

    private static final String SEKUNDAERFARBE = "#eef5f7";
    private static final String AKTIONSFARBE = "#619163";
    private static final String AKZENTFARBE = "#6c9aa6";
    private static final String WARNFARBE = "#de3535";
    private static final String STATUS_STANDARD = "-fx-background-color: white; -fx-text-fill: #333333;";
    private static final double ICON_BREITE = 34;

    private final EditorController controller;
    private final SpeicherController speicherController;
    private final AnlagenPlan anlagenPlan;
    private final Consumer<AnlagenPlan> planLaden;
    private final Runnable simulationOeffnen;
    private final Label status;
    private final RasterPane rasterPane;

    /**
     * Erstellt die Editoransicht fuer einen Anlagenplan.
     *
     * @param anlagenPlan darzustellender Anlagenplan
     * @param planLaden Aktion, die beim Laden eines neuen Anlagenplans ausgefuehrt wird
     * @param simulationOeffnen Aktion zum Oeffnen der Simulationsansicht
     */
    public EditorView(AnlagenPlan anlagenPlan, Consumer<AnlagenPlan> planLaden, Runnable simulationOeffnen) {
        this.anlagenPlan = anlagenPlan;
        this.planLaden = planLaden;
        this.simulationOeffnen = simulationOeffnen;
        this.controller = new EditorController(anlagenPlan);
        this.speicherController = new SpeicherController();
        this.status = erstelleStatusleiste(anlagenPlan);

        this.rasterPane = new RasterPane(
                anlagenPlan,
                this::verarbeiteRasterklick,
                this::verarbeiteRasterRechtsklick);

        VBox zentrumInhalt = new VBox(12, rasterPane, erstelleAktionsleiste());
        zentrumInhalt.setAlignment(Pos.TOP_CENTER);

        ScrollPane zentrum = new ScrollPane(zentrumInhalt);
        zentrum.setFitToWidth(true);
        zentrum.setFitToHeight(false);
        zentrum.setPannable(true);
        zentrum.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        setLeft(erstelleWerkzeugleiste());
        setCenter(zentrum);
        setBottom(status);
        setStyle("-fx-background-color: " + SEKUNDAERFARBE + ";");
        setFocusTraversable(true);
        addEventFilter(KeyEvent.KEY_PRESSED, this::verarbeiteTastendruck);
        validiereBeimOeffnen();
    }

    private VBox erstelleWerkzeugleiste() {
        Label titel = new Label("Werkzeuge");
        titel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button geradeButton = erstelleWerkzeugButton("Gerade", Werkzeug.GERADE);
        Button knickButton = erstelleWerkzeugButton("Knick", Werkzeug.KNICK);
        Button weicheButton = erstelleWerkzeugButton("Weiche", Werkzeug.WEICHE);
        Button zielButton = erstelleWerkzeugButton("Ziel", Werkzeug.ZIEL);
        Button gepaeckButton = erstelleWerkzeugButton("Gepäck", Werkzeug.GEPAECK);
        Button spiegelnButton = erstelleWerkzeugButton("Spiegeln", Werkzeug.SPIEGELN);
        Button drehenButton = erstelleWerkzeugButton("Drehen", Werkzeug.DREHEN);
        Button verschiebenButton = erstelleWerkzeugButton("Verschieben", Werkzeug.VERSCHIEBEN);
        Button loeschenButton = erstelleWerkzeugButton("Element löschen", Werkzeug.LOESCHEN);
        loeschenButton.setStyle("-fx-background-color: " + WARNFARBE + "; -fx-text-fill: white;");

        Button speichernButton = new Button("Anlagenplan speichern");
        speichernButton.setMaxWidth(Double.MAX_VALUE);
        speichernButton.setAlignment(Pos.CENTER);
        speichernButton.setOnAction(event -> speichereAnlagenPlan());

        Button ladenButton = new Button("Anlagenplan laden");
        ladenButton.setMaxWidth(Double.MAX_VALUE);
        ladenButton.setAlignment(Pos.CENTER);
        ladenButton.setOnAction(event -> ladeAnlagenPlan());

        Button zuruecksetzenButton = new Button("Zurücksetzen");
        zuruecksetzenButton.setMaxWidth(Double.MAX_VALUE);
        zuruecksetzenButton.setAlignment(Pos.CENTER);
        zuruecksetzenButton.setStyle("-fx-background-color: " + WARNFARBE + "; -fx-text-fill: white;");
        zuruecksetzenButton.setOnAction(event -> setzeAnlagenPlanZurueck());

        Region abstandshalter = new Region();
        VBox.setVgrow(abstandshalter, Priority.ALWAYS);

        VBox werkzeugleiste = new VBox(10,
                titel,
                geradeButton,
                knickButton,
                weicheButton,
                zielButton,
                gepaeckButton,
                spiegelnButton,
                drehenButton,
                verschiebenButton,
                loeschenButton,
                abstandshalter,
                speichernButton,
                ladenButton,
                zuruecksetzenButton);
        werkzeugleiste.setPadding(new Insets(16));
        werkzeugleiste.setPrefWidth(190);
        werkzeugleiste.setStyle("-fx-background-color: white;");
        return werkzeugleiste;
    }

    private HBox erstelleAktionsleiste() {
        Button simulationButton = new Button("Zur Simulation");
        simulationButton.setStyle("-fx-background-color: " + AKTIONSFARBE + "; -fx-text-fill: white;");
        simulationButton.setOnAction(event -> simulationOeffnen.run());

        Button anleitungButton = new Button("Anleitung");
        anleitungButton.setOnAction(event -> zeigeAnleitung());

        HBox aktionsleiste = new HBox(12, simulationButton, anleitungButton);
        aktionsleiste.setAlignment(Pos.CENTER);
        aktionsleiste.setPadding(new Insets(4, 0, 12, 0));
        return aktionsleiste;
    }

    private void zeigeAnleitung() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Anleitung");
        dialog.setHeaderText("Editor bedienen");

        Label text = new Label("""
                Grundbedienung
                Wähle links ein Werkzeug aus und klicke danach auf ein freies Rasterfeld.

                Rechtsklick
                Mit Rechtsklick auf ein vorhandenes Element drehst du es im Uhrzeigersinn. Weichen werden dabei nicht gedreht.

                Elemente
                Gerade, Knick, Weiche und Ziel werden als Förderbandteile platziert. Ein Ziel braucht beim Platzieren einen Zielortnamen. Dieser Name muss mit dem Zielort eines Gepäckstücks übereinstimmen.

                Drehen und Spiegeln
                Elemente können alternativ mit dem Werkzeug Drehen gedreht werden. Spiegeln funktioniert nur bei Knicken.

                Verschieben
                Mit dem Werkzeug Verschieben wählst du ein vorhandenes Element aus. Danach bewegst du es mit den Pfeiltasten um jeweils ein Rasterfeld. Belegte Felder und Positionen außerhalb des Rasters sind gesperrt.

                Weichen
                Beim Platzieren einer Weiche wird ein Dialog geöffnet. Dort werden Eingang, Standardausgang und eine Regel festgelegt. Bei Abbrechen wird keine Weiche platziert.

                Gepäck
                Gepäck kann nur auf einem vorhandenen Förderelement platziert werden, nicht auf einem Ziel und nicht auf einem bereits belegten Feld.

                Validierung
                Rote Felder markieren Fehler im Förderweg. Gerade Elemente müssen in derselben Richtung weiterlaufen. Knicke müssen von der passenden Seite angefahren werden.

                Simulation
                Mit Zur Simulation wechselst du in die Simulationsansicht. Dort kannst du starten, pausieren, fortsetzen und zurücksetzen.
                """);
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");
        text.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(text);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportWidth(250);
        scrollPane.setPrefViewportHeight(360);
        scrollPane.setStyle("-fx-background-color: white;");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(scrollPane);
        dialogPane.getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private Button erstelleWerkzeugButton(String text, Werkzeug werkzeug) {
        Button button = new Button();
        Node symbol = erstelleWerkzeugSymbol(werkzeug);
        if (symbol != null) {
            button.setGraphic(erstelleWerkzeugInhalt(symbol, text, button));
        } else {
            button.setText(text);
        }
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER);
        button.setOnAction(event -> {
            controller.waehleWerkzeug(werkzeug);
            status.setStyle(STATUS_STANDARD);
            status.setText("Werkzeug: " + text);
        });
        return button;
    }

    private Node erstelleWerkzeugInhalt(Node symbol, String text, Button button) {
        StackPane iconBereich = new StackPane();
        iconBereich.setMinWidth(ICON_BREITE);
        iconBereich.setPrefWidth(ICON_BREITE);
        iconBereich.setAlignment(Pos.CENTER_LEFT);
        if (symbol != null) {
            iconBereich.getChildren().add(symbol);
        }

        Label textLabel = new Label(text);

        BorderPane inhalt = new BorderPane();
        inhalt.setLeft(iconBereich);
        inhalt.setCenter(textLabel);
        // an Button-Breite binden, damit Icon-Spalte und Text ueber alle Buttons hinweg gleich ausgerichtet sind
        inhalt.prefWidthProperty().bind(button.widthProperty().subtract(24));
        return inhalt;
    }

    private Node erstelleWerkzeugSymbol(Werkzeug werkzeug) {
        Position vorschauPosition = new Position(0, 0);
        return switch (werkzeug) {
            case GERADE -> new FoerderelementNode(new Gerade(vorschauPosition, Richtung.OST));
            case KNICK -> new FoerderelementNode(new Knick(vorschauPosition, Richtung.OST));
            case WEICHE -> new FoerderelementNode(new Weiche(vorschauPosition, Richtung.OST, Richtung.SUED));
            case ZIEL -> new FoerderelementNode(new Ziel(vorschauPosition, Richtung.OST, "Ziel"));
            case GEPAECK -> erstelleGepaeckSymbol();
            case SPIEGELN, DREHEN, VERSCHIEBEN, LOESCHEN -> null;
        };
    }

    private Node erstelleGepaeckSymbol() {
        StackPane rahmen = new StackPane(new GepaeckstueckNode());
        rahmen.setMinSize(30, 30);
        rahmen.setPrefSize(30, 30);
        rahmen.setMaxSize(30, 30);
        return rahmen;
    }

    private Label erstelleStatusleiste(AnlagenPlan anlagenPlan) {
        Label statusleiste = new Label("Anlagenplan: " + anlagenPlan.getName());
        statusleiste.setPadding(new Insets(8, 16, 8, 16));
        statusleiste.setStyle(STATUS_STANDARD);
        return statusleiste;
    }

    private void verarbeiteRasterklick(Position position) {
        if (controller.getAusgewaehltesWerkzeug() == Werkzeug.GEPAECK) {
            oeffneGepaeckDialog(position);
            return;
        }
        if (controller.getAusgewaehltesWerkzeug() == Werkzeug.VERSCHIEBEN) {
            verarbeiteVerschiebeAuswahl(position);
            return;
        }

        boolean platziert = controller.platziereAusgewaehltesElement(position);
        if (platziert && controller.getAusgewaehltesWerkzeug() == Werkzeug.LOESCHEN) {
            zeigeErfolgMitValidierung("Element gelöscht");
        } else if (platziert && controller.getAusgewaehltesWerkzeug() == Werkzeug.SPIEGELN) {
            zeigeErfolgMitValidierung("Knick gespiegelt");
        } else if (platziert && controller.getAusgewaehltesWerkzeug() == Werkzeug.DREHEN) {
            zeigeErfolgMitValidierung("Element gedreht");
        } else if (platziert && controller.getAusgewaehltesWerkzeug() == Werkzeug.WEICHE) {
            oeffneWeichenDialog(position);
        } else if (platziert && controller.getAusgewaehltesWerkzeug() == Werkzeug.ZIEL) {
            oeffneZielDialog(position);
        } else if (platziert) {
            zeigeErfolgMitValidierung("Element platziert: " + controller.getAusgewaehltesWerkzeug());
        } else if (controller.getAusgewaehltesWerkzeug() == Werkzeug.LOESCHEN) {
            status.setText("Kein Element zum Löschen vorhanden");
            status.setStyle("-fx-background-color: " + WARNFARBE + "; -fx-text-fill: white;");
        } else if (controller.getAusgewaehltesWerkzeug() == Werkzeug.SPIEGELN) {
            status.setText("Spiegeln ist nur für Knicke möglich");
            status.setStyle("-fx-background-color: " + AKZENTFARBE + "; -fx-text-fill: white;");
        } else if (controller.getAusgewaehltesWerkzeug() == Werkzeug.DREHEN) {
            status.setText("Kein drehbares Element vorhanden");
            status.setStyle("-fx-background-color: " + AKZENTFARBE + "; -fx-text-fill: white;");
        } else {
            status.setText("Rasterfeld ist bereits belegt");
            status.setStyle("-fx-background-color: " + AKZENTFARBE + "; -fx-text-fill: white;");
        }
    }

    private void verarbeiteVerschiebeAuswahl(Position position) {
        boolean ausgewaehlt = controller.waehleElementZumVerschieben(position);
        if (ausgewaehlt) {
            status.setStyle(STATUS_STANDARD);
            status.setText("Element ausgewählt - mit Pfeiltasten verschieben");
            requestFocus();
        } else {
            status.setText("Kein Element zum Verschieben vorhanden");
            status.setStyle("-fx-background-color: " + AKZENTFARBE + "; -fx-text-fill: white;");
        }
    }

    private void verarbeiteTastendruck(KeyEvent event) {
        if (controller.getAusgewaehltesWerkzeug() != Werkzeug.VERSCHIEBEN) {
            return;
        }

        Richtung richtung = richtungFuerTaste(event.getCode());
        if (richtung == null) {
            return;
        }

        event.consume();
        if (controller.getAusgewaehlteVerschiebePosition() == null) {
            status.setText("Erst ein Element zum Verschieben auswählen");
            status.setStyle("-fx-background-color: " + AKZENTFARBE + "; -fx-text-fill: white;");
            return;
        }

        boolean verschoben = controller.verschiebeAusgewaehltesElement(richtung);
        if (verschoben) {
            zeigeErfolgMitValidierung("Element verschoben");
        } else {
            status.setText("Element kann nicht auf dieses Feld verschoben werden");
            status.setStyle("-fx-background-color: " + AKZENTFARBE + "; -fx-text-fill: white;");
        }
    }

    private Richtung richtungFuerTaste(KeyCode taste) {
        return switch (taste) {
            case UP -> Richtung.NORD;
            case RIGHT -> Richtung.OST;
            case DOWN -> Richtung.SUED;
            case LEFT -> Richtung.WEST;
            default -> null;
        };
    }

    private void oeffneGepaeckDialog(Position position) {
        GepaeckDialog dialog = new GepaeckDialog(ermittleZielorte());
        dialog.showAndWait().ifPresent(ergebnis -> {
            boolean platziert = controller.platziereGepaeck(
                    position,
                    ergebnis.gewicht(),
                    ergebnis.typ(),
                    ergebnis.zielort());
            if (platziert) {
                status.setStyle(STATUS_STANDARD);
                status.setText("Gepäck platziert");
            } else {
                status.setText("Gepäck kann nur auf freiem Förderelement platziert werden");
                status.setStyle("-fx-background-color: " + AKZENTFARBE + "; -fx-text-fill: white;");
            }
        });
    }

    private List<String> ermittleZielorte() {
        List<String> zielorte = new ArrayList<>();
        for (int y = 0; y < anlagenPlan.getRaster().getHoehe(); y++) {
            for (int x = 0; x < anlagenPlan.getRaster().getBreite(); x++) {
                Position position = new Position(x, y);
                Foerderelement element = anlagenPlan.getRaster().getFeld(position).getElement();
                if (element instanceof Ziel ziel
                        && ziel.getZielname() != null
                        && !ziel.getZielname().isBlank()) {
                    zielorte.add(ziel.getZielname());
                }
            }
        }
        return zielorte.stream().distinct().toList();
    }

    private void oeffneZielDialog(Position position) {
        Foerderelement element = controller.getElement(position);
        if (!(element instanceof Ziel ziel)) {
            zeigeErfolgMitValidierung("Ziel platziert");
            return;
        }

        ZielDialog dialog = new ZielDialog();
        dialog.showAndWait().ifPresentOrElse(ergebnis -> {
            boolean gespeichert = controller.konfiguriereZiel(ziel, ergebnis.zielort());
            if (gespeichert) {
                zeigeErfolgMitValidierung("Ziel platziert: " + ergebnis.zielort());
            } else {
                status.setText("Zielort ungültig");
                status.setStyle("-fx-background-color: " + WARNFARBE + "; -fx-text-fill: white;");
            }
        }, () -> {
            controller.loescheElement(position);
            status.setStyle(STATUS_STANDARD);
            status.setText("Ziel nicht platziert");
        });
    }

    private void oeffneWeichenDialog(Position position) {
        Foerderelement element = controller.getElement(position);
        if (!(element instanceof Weiche weiche)) {
            zeigeErfolgMitValidierung("Weiche platziert");
            return;
        }

        WeichenDialog dialog = new WeichenDialog(ermittleZielorte());
        dialog.showAndWait().ifPresentOrElse(ergebnis -> {
            boolean gespeichert = controller.konfiguriereWeiche(
                    weiche,
                    ergebnis.eingangsrichtung(),
                    ergebnis.standardrichtung(),
                    ergebnis.regeln());
            if (gespeichert) {
                zeigeErfolgMitValidierung("Weiche konfiguriert");
            } else {
                status.setText("Weichenregel ungültig");
                status.setStyle("-fx-background-color: " + WARNFARBE + "; -fx-text-fill: white;");
            }
        }, () -> {
            controller.loescheElement(position);
            status.setStyle(STATUS_STANDARD);
            status.setText("Weiche nicht platziert");
        });
    }

    private void verarbeiteRasterRechtsklick(Position position) {
        boolean gedreht = controller.dreheElement(position);
        if (gedreht) {
            zeigeErfolgMitValidierung("Element gedreht");
        } else {
            status.setText("Kein drehbares Element vorhanden");
            status.setStyle("-fx-background-color: " + AKZENTFARBE + "; -fx-text-fill: white;");
        }
    }

    private void zeigeErfolgMitValidierung(String erfolgsmeldung) {
        ValidierungsErgebnis ergebnis = controller.getLetztesValidierungsErgebnis();
        if (ergebnis.istGueltig()) {
            status.setStyle(STATUS_STANDARD);
            status.setText(erfolgsmeldung + " - Förderweg gültig");
        } else {
            status.setStyle("-fx-background-color: " + WARNFARBE + "; -fx-text-fill: white;");
            status.setText(erfolgsmeldung + " - "
                    + ergebnis.getFehler().size() + " Validierungsfehler");
        }
    }

    private void validiereBeimOeffnen() {
        ValidierungsErgebnis ergebnis = controller.validiereAktuellenPlan();
        if (!ergebnis.istGueltig()) {
            status.setStyle("-fx-background-color: " + WARNFARBE + "; -fx-text-fill: white;");
            status.setText("Anlagenplan geladen - " + ergebnis.getFehler().size() + " Validierungsfehler");
        }
    }

    private void speichereAnlagenPlan() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Anlagenplan speichern");
        fileChooser.setInitialFileName(anlagenPlan.getName() + ".json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SortiFX JSON", "*.json"));

        Window window = getScene() == null ? null : getScene().getWindow();
        java.io.File datei = fileChooser.showSaveDialog(window);
        if (datei == null) {
            return;
        }

        try {
            speicherController.speichere(anlagenPlan, datei.toPath());
            status.setStyle(STATUS_STANDARD);
            status.setText("Anlagenplan gespeichert");
        } catch (Exception exception) {
            status.setText("Anlagenplan konnte nicht gespeichert werden");
            status.setStyle("-fx-background-color: " + WARNFARBE + "; -fx-text-fill: white;");
        }
    }

    private void ladeAnlagenPlan() {
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
            AnlagenPlan geladenerPlan = speicherController.lade(datei.toPath());
            planLaden.accept(geladenerPlan);
        } catch (Exception exception) {
            status.setText("Anlagenplan konnte nicht geladen werden");
            status.setStyle("-fx-background-color: " + WARNFARBE + "; -fx-text-fill: white;");
        }
    }

    private void setzeAnlagenPlanZurueck() {
        controller.reset();
        status.setStyle(STATUS_STANDARD);
        status.setText("Anlagenplan zurückgesetzt");
    }
}
