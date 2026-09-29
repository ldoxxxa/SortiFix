package de.hsrm.mi.swt.model.elemente;

import de.hsrm.mi.swt.model.Position;

/**
 * Zielstation einer Foerderanlage.
 * Hier endet der Weg eines passenden Gepaeckstuecks.
 */
public class Ziel extends Foerderelement {

    // wird von Nutzer eingeben und in Simulationslogik mit gepaeckstueck.getZiel() verglichen
    private String zielname;

    /**
     * Erstellt eine Zielstation.
     *
     * @param position Position im Raster
     * @param richtung Anschlussrichtung des Ziels
     * @param zielname Name, der mit dem Gepaeckziel verglichen wird
     */
    public Ziel(Position position, Richtung richtung, String zielname) {
        super(position, richtung);
        this.zielname = zielname;
    }

    /**
     * @return Name dieser Zielstation
     */
    public String getZielname() {
        return zielname;
    }

    /**
     * @param zielname neuer Name der Zielstation
     */
    public void setZielname(String zielname) {
        this.zielname = zielname;
    }
}
