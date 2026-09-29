package de.hsrm.mi.swt.model.elemente;

import java.util.ArrayList;
import java.util.List;

import de.hsrm.mi.swt.model.Position;

/**
 * Weiche mit einem Eingang und mehreren moeglichen Ausgaengen.
 * Regeln entscheiden, welcher Ausgang fuer ein Gepaeckstueck benutzt wird.
 */
public class Weiche extends Foerderelement {
    private Richtung standardrichtung;
    private boolean istKonfiguriert;
    private final List<Weichenregel> regeln = new ArrayList<>();

    /**
     * Erstellt eine Weiche.
     *
     * @param position Position im Raster
     * @param eingangsrichtung Richtung, aus der Gepaeck in die Weiche kommt
     * @param standardrichtung Ausgang, wenn keine Regel passt
     */
    public Weiche(Position position, Richtung eingangsrichtung, Richtung standardrichtung) {
        super(position, eingangsrichtung); // von Foerderelement
        this.standardrichtung = standardrichtung;
        this.istKonfiguriert = false;
    }
    
    /**
     * @return Ausgang, wenn keine Regel passt
     */
    public Richtung getStandardrichtung() {
        return standardrichtung;
    }

    /**
     * @param standardrichtung neuer Standardausgang
     */
    public void setStandardrichtung(Richtung standardrichtung) {
        this.standardrichtung = standardrichtung;
    }

    /**
     * @return true, wenn die Weiche im Dialog konfiguriert wurde
     */
    public boolean isKonfiguriert() {
        return istKonfiguriert;
    }

    /**
     * @param konfiguriert neuer Konfigurationszustand
     */
    public void setKonfiguriert(boolean konfiguriert) {
        this.istKonfiguriert = konfiguriert;
    }

    /**
     * @return veraenderbare Liste der Weichenregeln
     */
    public List<Weichenregel> getRegeln() {
        return regeln;
    }

    /**
     * Fuegt eine Routingregel zur Weiche hinzu.
     *
     * @param regel neue Regel
     */
    public void addRegel(Weichenregel regel) {
        regeln.add(regel);
    }

    /**
     * Entfernt eine Routingregel aus der Weiche.
     *
     * @param regel zu entfernende Regel
     */
    public void removeRegel(Weichenregel regel) {
        regeln.remove(regel);
    }
}
