package de.hsrm.mi.swt.model.gepaeck;

import de.hsrm.mi.swt.model.Position;

/**
 * Beschreibt ein Gepaeckstueck in der Simulation.
 * Es speichert Eigenschaften und die aktuelle Position im Raster.
 */
public class Gepaeckstueck {

    private final String id; // automatische Generierung später in AnlagenPlan
    private double gewicht;
    private Gepaecktyp typ;
    private String ziel;
    private Position position;

    /**
     * Erstellt ein Gepaeckstueck fuer den Anlagenplan.
     *
     * @param id eindeutige Kennung des Gepaeckstuecks
     * @param gewicht Gewicht in Kilogramm
     * @param typ Art des Gepaecks
     * @param ziel Name der Zielstation
     * @param position aktuelle Position im Raster
     */
    public Gepaeckstueck(String id, double gewicht, Gepaecktyp typ, String ziel, Position position) {
        this.id = id;
        this.gewicht = gewicht;
        this.typ = typ;
        this.ziel = ziel;
        this.position = position;
    }

    /**
     * @return eindeutige Kennung
     */
    public String getId() {
        return id;
    }

    /**
     * @return Gewicht in Kilogramm
     */
    public double getGewicht() {
        return gewicht;
    }

    /**
     * @param gewicht neues Gewicht in Kilogramm
     */
    public void setGewicht(double gewicht) {
        this.gewicht = gewicht;
    }

    /**
     * @return Gepaeckart
     */
    public Gepaecktyp getTyp() {
        return typ;
    }

    /**
     * @param typ neue Gepaeckart
     */
    public void setTyp(Gepaecktyp typ) {
        this.typ = typ;
    }

    /**
     * @return Zielname des Gepaeckstuecks
     */
    public String getZiel() {
        return ziel;
    }

    /**
     * @param ziel neuer Zielname
     */
    public void setZiel(String ziel) {
        this.ziel = ziel;
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
}
