package de.hsrm.mi.swt.model.raster;

import de.hsrm.mi.swt.model.Position;
import de.hsrm.mi.swt.model.elemente.Foerderelement;

/**
 * Einzelnes Feld im Raster.
 * Ein Feld hat immer eine Position und kann ein Foerderelement enthalten.
 */
public class Rasterfeld {
    
    private final Position position;
    private Foerderelement element;

    /**
     * Erstellt ein leeres Rasterfeld.
     *
     * @param position feste Position dieses Feldes
     */
    public Rasterfeld(Position position) {
        this.position = position;
    }

    /**
     * @return Position dieses Feldes
     */
    public Position getPosition() {
        return position;
    }

    /**
     * @return Element auf diesem Feld oder null
     */
    public Foerderelement getElement() {
        return element;
    }

    /**
     * @param element neues Element oder null zum Leeren
     */
    public void setElement(Foerderelement element) {
        this.element = element;
    }

    /**
     * Prueft, ob auf diesem Feld kein Foerderelement liegt.
     *
     * @return true, wenn kein Element gesetzt ist
     */
    public boolean istFrei() {
        return element == null;
    }
}
