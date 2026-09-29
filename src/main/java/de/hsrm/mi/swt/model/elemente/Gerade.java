package de.hsrm.mi.swt.model.elemente;

import de.hsrm.mi.swt.model.Position;

/**
 * Gerades Foerderbandstueck.
 * Es transportiert Gepaeck in seine eingestellte Richtung.
 */
public class Gerade extends Foerderelement {
    /**
     * Erstellt ein gerades Foerderelement.
     *
     * @param position Position im Raster
     * @param richtung Transportrichtung
     */
    public Gerade(Position position, Richtung richtung) {
        super(position, richtung);
    } 
}
