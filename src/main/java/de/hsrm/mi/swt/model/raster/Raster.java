package de.hsrm.mi.swt.model.raster;

import de.hsrm.mi.swt.model.Position;

/**
 * Zweidimensionales Raster der Foerderanlage.
 * Jedes Feld kennt seine Position und optional ein Foerderelement.
 */
public class Raster {

    private final Rasterfeld[][] felder; // 2D-Array aus Rasterfeld

    /**
     * Erstellt ein leeres Raster mit der angegebenen Groesse.
     *
     * @param breite Anzahl der Spalten
     * @param hoehe Anzahl der Zeilen
     */
    public Raster(int breite, int hoehe) {
        felder = new Rasterfeld[hoehe][breite];

        for (int y = 0; y < hoehe; y++) {
            for (int x = 0; x < breite; x++) {
                felder[y][x] = new Rasterfeld(new Position(x, y));
            }
        }
    }

    /**
     * Gibt das Rasterfeld an einer gueltigen Position zurueck.
     *
     * @param position Position im Raster
     * @return Rasterfeld an dieser Position
     */
    public Rasterfeld getFeld(Position position) {
        return felder[position.getY()][position.getX()];
    }

    /**
     * Prueft, ob eine Position innerhalb des Rasters liegt.
     *
     * @param position zu pruefende Position
     * @return true, wenn die Position im Raster liegt
     */
    public boolean istGueltig(Position position) {
        return position.getX() >= 0
                && position.getX() < felder[0].length
                && position.getY() >= 0
                && position.getY() < felder.length;
    }

    /**
     * Prueft, ob eine Position im Raster liegt und kein Element enthaelt.
     *
     * @param position zu pruefende Position
     * @return true, wenn das Feld frei ist
     */
    public boolean istFrei(Position position) {
        return istGueltig(position)
                && getFeld(position).istFrei();
    }

    /**
     * @return Anzahl der Spalten
     */
    public int getBreite() {
        return felder[0].length;
    }
    
    /**
     * @return Anzahl der Zeilen
     */
    public int getHoehe() {
        return felder.length;
    }
}
