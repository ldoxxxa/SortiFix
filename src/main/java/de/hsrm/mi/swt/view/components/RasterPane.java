package de.hsrm.mi.swt.view.components;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;

import de.hsrm.mi.swt.model.AnlagenPlan;
import de.hsrm.mi.swt.model.Position;
import de.hsrm.mi.swt.model.elemente.Foerderelement;
import de.hsrm.mi.swt.model.elemente.Weiche;
import de.hsrm.mi.swt.model.elemente.Weichenregel;
import de.hsrm.mi.swt.model.elemente.Ziel;
import de.hsrm.mi.swt.model.event.AnlagenplanEvent;
import de.hsrm.mi.swt.model.event.AnlagenplanListener;
import de.hsrm.mi.swt.model.gepaeck.Gepaeckstueck;
import de.hsrm.mi.swt.model.logik.ValidierungsErgebnis.Fehler;
import de.hsrm.mi.swt.model.raster.Raster;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * JavaFX-Komponente zur Darstellung des Rasters eines Anlagenplans.
 * Aktualisiert die Darstellung, sobald der Anlagenplan ein Model-Event meldet.
 */
public class RasterPane extends GridPane implements AnlagenplanListener {

    private static final int FELD_GROESSE = 34;
    private static final String RASTER_RAND = "#c9d9de";
    private static final String RASTER_FLAECHE = "#ffffff";
    private static final String FEHLER_FLAECHE = "#f8d7d7";

    private final AnlagenPlan anlagenPlan;
    private final Consumer<Position> klickAktion;
    private final Consumer<Position> rechtsklickAktion;
    private final Set<Position> fehlerPositionen = new HashSet<>();

    /**
     * Erstellt eine Rasterdarstellung fuer den uebergebenen Anlagenplan.
     *
     * @param anlagenPlan Anlagenplan, dessen Raster angezeigt wird
     */
    public RasterPane(AnlagenPlan anlagenPlan) {
        this(anlagenPlan, position -> {}, position -> {});
    }

    /**
     * Erstellt eine Rasterdarstellung mit einer Aktion fuer Rasterklicks.
     *
     * @param anlagenPlan Anlagenplan, dessen Raster angezeigt wird
     * @param klickAktion Aktion, die bei Klick auf ein Rasterfeld ausgefuehrt wird
     */
    public RasterPane(AnlagenPlan anlagenPlan, Consumer<Position> klickAktion) {
        this(anlagenPlan, klickAktion, position -> {});
    }

    /**
     * Erstellt eine Rasterdarstellung mit Aktionen fuer Links- und Rechtsklicks.
     *
     * @param anlagenPlan Anlagenplan, dessen Raster angezeigt wird
     * @param klickAktion Aktion, die bei Linksklick auf ein Rasterfeld ausgefuehrt wird
     * @param rechtsklickAktion Aktion, die bei Rechtsklick auf ein Rasterfeld ausgefuehrt wird
     */
    public RasterPane(AnlagenPlan anlagenPlan,
            Consumer<Position> klickAktion,
            Consumer<Position> rechtsklickAktion) {
        this.anlagenPlan = anlagenPlan;
        this.klickAktion = klickAktion;
        this.rechtsklickAktion = rechtsklickAktion;
        this.anlagenPlan.addListener(this);

        setPadding(new Insets(24));
        setAlignment(Pos.CENTER);
        setHgap(1);
        setVgap(1);
        zeichneRaster();
    }

    /**
     * Reagiert auf Aenderungen am Anlagenplan und zeichnet das Raster neu.
     *
     * @param event ausgeloestes Anlagenplan-Event
     */
    @Override
    public void onAnlagenplanEvent(AnlagenplanEvent event) {
        if (event instanceof AnlagenplanEvent.ValidierungAktualisiert validierung) {
            fehlerPositionen.clear();
            for (Fehler fehler : validierung.getErgebnis().getFehler()) {
                if (fehler.getPosition() != null) {
                    fehlerPositionen.add(fehler.getPosition());
                }
            }
        }

        zeichneRaster();
    }

    private void zeichneRaster() {
        getChildren().clear();

        Raster raster = anlagenPlan.getRaster();
        for (int y = 0; y < raster.getHoehe(); y++) {
            for (int x = 0; x < raster.getBreite(); x++) {
                Position position = new Position(x, y);
                add(erstelleRasterfeld(position, raster.getFeld(position).getElement()), x, y);
            }
        }
    }

    private StackPane erstelleRasterfeld(Position position, Foerderelement element) {
        Rectangle hintergrund = new Rectangle(FELD_GROESSE, FELD_GROESSE);
        hintergrund.setFill(Color.web(
                fehlerPositionen.contains(position) ? FEHLER_FLAECHE : RASTER_FLAECHE));
        hintergrund.setStroke(Color.web(RASTER_RAND));

        StackPane feld = new StackPane(hintergrund);
        feld.setMinSize(FELD_GROESSE, FELD_GROESSE);
        feld.setPrefSize(FELD_GROESSE, FELD_GROESSE);
        feld.setMaxSize(FELD_GROESSE, FELD_GROESSE);
        feld.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                rechtsklickAktion.accept(position);
            } else if (event.getButton() == MouseButton.PRIMARY) {
                klickAktion.accept(position);
            }
        });

        Optional<Gepaeckstueck> gepaeckAufFeld = findeGepaeck(position);

        if (element != null) {
            feld.getChildren().add(new FoerderelementNode(element));
        }
        if (gepaeckAufFeld.isPresent()) {
            feld.getChildren().add(new GepaeckstueckNode());
        }

        erstelleTooltip(element, gepaeckAufFeld).ifPresent(tooltip -> Tooltip.install(feld, tooltip));
        return feld;
    }

    private Optional<Gepaeckstueck> findeGepaeck(Position position) {
        return anlagenPlan.getGepaeckstuecke().stream()
                .filter(gepaeck -> gepaeck.getPosition().equals(position))
                .findFirst();
    }

    private Optional<Tooltip> erstelleTooltip(Foerderelement element,
            Optional<Gepaeckstueck> gepaeckAufFeld) {
        StringJoiner text = new StringJoiner("\n\n");

        if (element instanceof Ziel ziel) {
            text.add(beschreibeZiel(ziel));
        } else if (element instanceof Weiche weiche) {
            text.add(beschreibeWeiche(weiche));
        }

        gepaeckAufFeld.map(this::beschreibeGepaeck).ifPresent(text::add);

        String tooltipText = text.toString();
        if (tooltipText.isBlank()) {
            return Optional.empty();
        }
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(320);
        return Optional.of(tooltip);
    }

    private String beschreibeZiel(Ziel ziel) {
        return """
               Ziel
               Zielort: """ + ziel.getZielname();
    }

    private String beschreibeWeiche(Weiche weiche) {
        StringJoiner text = new StringJoiner("\n");
        text.add("Weiche");
        text.add("Eingangsrichtung: " + weiche.getRichtung());
        text.add("Standardrichtung: " + weiche.getStandardrichtung());
        if (weiche.getRegeln().isEmpty()) {
            text.add("Regeln: keine");
        } else {
            text.add("Regeln:");
            for (Weichenregel regel : weiche.getRegeln()) {
                text.add("- " + regel.getKriterium() + " = " + regel.getWert()
                        + " -> " + regel.getAusgangsrichtung());
            }
        }
        return text.toString();
    }

    private String beschreibeGepaeck(Gepaeckstueck gepaeck) {
        return "Gepäckstück\n"
                + "ID: " + gepaeck.getId() + "\n"
                + "Gewicht: " + formatiereGewicht(gepaeck.getGewicht()) + " kg\n"
                + "Gepäckart: " + gepaeck.getTyp() + "\n"
                + "Zielort: " + gepaeck.getZiel();
    }

    private String formatiereGewicht(double gewicht) {
        if (gewicht == Math.rint(gewicht)) {
            return Long.toString(Math.round(gewicht));
        }
        return Double.toString(gewicht);
    }
}
