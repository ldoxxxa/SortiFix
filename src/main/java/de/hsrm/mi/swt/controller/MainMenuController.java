package de.hsrm.mi.swt.controller;

import de.hsrm.mi.swt.model.AnlagenPlan;
import de.hsrm.mi.swt.model.raster.Raster;

/**
 * Controller fuer die zentrale Anwendungsansicht.
 * Verwaltet aktuell den initialen Anlagenplan, der in der Oberflaeche angezeigt wird.
 */
public class MainMenuController {

    private static final int STANDARD_RASTER_BREITE = 20;
    private static final int STANDARD_RASTER_HOEHE = 14;

    private AnlagenPlan anlagenPlan;

    /**
     * Erstellt einen neuen Controller mit einem leeren Standard-Anlagenplan.
     */
    public MainMenuController() {
        erstelleNeuenAnlagenPlan(STANDARD_RASTER_BREITE, STANDARD_RASTER_HOEHE);
    }

    /**
     * Erstellt einen neuen leeren Anlagenplan mit der angegebenen Rastergroesse.
     *
     * @param breite Anzahl der Spalten
     * @param hoehe Anzahl der Zeilen
     */
    public void erstelleNeuenAnlagenPlan(int breite, int hoehe) {
        this.anlagenPlan = new AnlagenPlan(
                "Neuer Anlagenplan",
                new Raster(breite, hoehe));
    }

    /**
     * Setzt den aktuell geoeffneten Anlagenplan.
     *
     * @param anlagenPlan geladener Anlagenplan
     */
    public void setAnlagenPlan(AnlagenPlan anlagenPlan) {
        this.anlagenPlan = anlagenPlan;
    }

    /**
     * Gibt den aktuell geoeffneten Anlagenplan zurueck.
     *
     * @return aktueller Anlagenplan
     */
    public AnlagenPlan getAnlagenPlan() {
        return anlagenPlan;
    }

    /**
     * @return Standardbreite fuer neue Anlagenplaene
     */
    public int getStandardRasterBreite() {
        return STANDARD_RASTER_BREITE;
    }

    /**
     * @return Standardhoehe fuer neue Anlagenplaene
     */
    public int getStandardRasterHoehe() {
        return STANDARD_RASTER_HOEHE;
    }
}
