package de.hsrm.mi.swt.view.components;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Visuelle Darstellung eines Gepaeckstuecks im Raster.
 */
public class GepaeckstueckNode extends Pane {

    private static final double GROESSE = 18;

    /**
     * Erstellt die kleine rote Markierung fuer ein Gepaeckstueck.
     */
    public GepaeckstueckNode() {
        setMinSize(GROESSE, GROESSE);
        setPrefSize(GROESSE, GROESSE);
        setMaxSize(GROESSE, GROESSE);

        Circle kreis = new Circle(GROESSE / 2.0, GROESSE / 2.0, GROESSE / 2.0);
        kreis.setFill(Color.web("#de3535"));
        kreis.setStroke(Color.WHITE);
        kreis.setStrokeWidth(2);
        getChildren().add(kreis);
    }
}
