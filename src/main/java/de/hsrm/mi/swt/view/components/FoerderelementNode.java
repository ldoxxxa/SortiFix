package de.hsrm.mi.swt.view.components;

import java.util.LinkedHashSet;
import java.util.Set;

import de.hsrm.mi.swt.model.elemente.Foerderelement;
import de.hsrm.mi.swt.model.elemente.Gerade;
import de.hsrm.mi.swt.model.elemente.Knick;
import de.hsrm.mi.swt.model.elemente.Richtung;
import de.hsrm.mi.swt.model.elemente.Weiche;
import de.hsrm.mi.swt.model.elemente.Ziel;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

/**
 * Visuelle Repraesentation eines Foerderelements im Raster.
 */
public class FoerderelementNode extends Pane {

    private static final double GROESSE = 30;
    private static final double MITTE = GROESSE / 2.0;
    private static final double STRICH_STAERKE = 6.0;

    private static final Color ELEMENT_FARBE = Color.web("#3c7280");
    private static final Color AKZENT_FARBE = Color.web("#619163");

    /**
     * Erstellt eine grafische Darstellung fuer das uebergebene Foerderelement.
     *
     * @param element darzustellendes Foerderelement
     */
    public FoerderelementNode(Foerderelement element) {
        setMinSize(GROESSE, GROESSE);
        setPrefSize(GROESSE, GROESSE);
        setMaxSize(GROESSE, GROESSE);

        zeichneElement(element);
        if (!(element instanceof Weiche)) {
            setRotate(rotationFuer(element.getRichtung()));
        }
    }

    private void zeichneElement(Foerderelement element) {
        if (element instanceof Gerade) {
            zeichneGerade();
        } else if (element instanceof Knick knick) {
            zeichneKnick(knick);
        } else if (element instanceof Weiche weiche) {
            zeichneWeiche(weiche);
        } else if (element instanceof Ziel) {
            zeichneZiel();
        }
    }

    private void zeichneGerade() {
        Line band = neueLinie(5, MITTE, 23, MITTE);
        Polygon pfeil = new Polygon(23, 8, 29, MITTE, 23, 22);
        pfeil.setFill(ELEMENT_FARBE);
        getChildren().addAll(band, pfeil);
    }

    private void zeichneKnick(Knick knick) {
        double endeX = knick.isGespiegelt() ? 7 : 23;
        getChildren().addAll(
                neueLinie(MITTE, 6, MITTE, MITTE),
                neueLinie(MITTE, MITTE, endeX, MITTE));
        Polygon pfeil = knick.isGespiegelt()
                ? new Polygon(7, 8, 1, MITTE, 7, 22)
                : new Polygon(23, 8, 29, MITTE, 23, 22);
        pfeil.setFill(ELEMENT_FARBE);
        getChildren().add(pfeil);
    }

    private void zeichneWeiche(Weiche weiche) {
        Set<Richtung> arme = new LinkedHashSet<>();
        arme.add(weiche.getRichtung());
        arme.add(weiche.getStandardrichtung());
        weiche.getRegeln().stream()
                .map(regel -> regel.getAusgangsrichtung())
                .distinct()
                .forEach(arme::add);

        arme.forEach(richtung -> getChildren().add(neueLinieZuRichtung(richtung)));

        Circle knoten = new Circle(MITTE, MITTE, 3.5, AKZENT_FARBE);
        getChildren().add(knoten);
    }

    private void zeichneZiel() {
        Circle ziel = new Circle(MITTE, MITTE, 10);
        ziel.setFill(Color.TRANSPARENT);
        ziel.setStroke(AKZENT_FARBE);
        ziel.setStrokeWidth(STRICH_STAERKE);

        Rectangle markierung = new Rectangle(11, 11, 8, 8);
        markierung.setFill(AKZENT_FARBE);
        getChildren().addAll(ziel, markierung);
    }

    private Line neueLinie(double startX, double startY, double endeX, double endeY) {
        Line linie = new Line(startX, startY, endeX, endeY);
        linie.setStroke(ELEMENT_FARBE);
        linie.setStrokeWidth(STRICH_STAERKE);
        linie.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        return linie;
    }

    private Line neueLinieZuRichtung(Richtung richtung) {
        return switch (richtung) {
            case NORD -> neueLinie(MITTE, MITTE, MITTE, 4);
            case OST -> neueLinie(MITTE, MITTE, 26, MITTE);
            case SUED -> neueLinie(MITTE, MITTE, MITTE, 26);
            case WEST -> neueLinie(MITTE, MITTE, 4, MITTE);
        };
    }

    private double rotationFuer(Richtung richtung) {
        return switch (richtung) {
            case OST -> 0;
            case SUED -> 90;
            case WEST -> 180;
            case NORD -> 270;
        };
    }
}
