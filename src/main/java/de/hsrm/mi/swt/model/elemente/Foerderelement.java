package de.hsrm.mi.swt.model.elemente;

import de.hsrm.mi.swt.model.Position;

/**
 * Gemeinsame Oberklasse fuer alle Elemente auf dem Foerderband.
 * Jedes Element hat eine Position und eine Grundrichtung.
 */
public abstract class Foerderelement {

    private Position position;
    private Richtung richtung; // Transportrichtung

    /**
     * Erstellt ein Foerderelement an einer Position.
     *
     * @param position Position im Raster
     * @param richtung Grundrichtung des Elements
     */
    public Foerderelement(Position position, Richtung richtung) {
        this.position = position;
        this.richtung = richtung;
    }

    /**
     * @return aktuelle Position im Raster
     */
    public Position getPosition() {
        return position;
    }

    /**
     * @param position neue Position im Raster
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * @return Grundrichtung des Elements
     */
    public Richtung getRichtung() {
        return richtung;
    }

    /**
     * @param richtung neue Grundrichtung
     */
    public void setRichtung(Richtung richtung) {
        this.richtung = richtung;
    }

    /**
     * Liefert die Position, an der das naechste Element erwartet wird.
     * Die Validierungslogik prueft danach, ob dort wirklich etwas Passendes liegt.
     *
     * @return erwartete Nachfolgerposition
     */
    public Position getNachfolgerPosition() {
        return position.move(richtung);
    }
    
}
