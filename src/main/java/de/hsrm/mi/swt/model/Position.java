package de.hsrm.mi.swt.model;

import java.util.Objects;

import de.hsrm.mi.swt.model.elemente.Richtung;

/**
 * Unveraenderliche Position im Raster.
 * X beschreibt die Spalte, Y beschreibt die Zeile.
 */
public class Position {
    private final int x;
    private final int y;

    /**
     * Erstellt eine Position mit Rasterkoordinaten.
     *
     * @param x Spalte im Raster
     * @param y Zeile im Raster
     */
    public Position(int x, int y){
        this.x = x;
        this.y = y;
    }

    /**
     * @return Spalte im Raster
     */
    public int getX() {
        return x;
    }

    /**
     * @return Zeile im Raster
     */
    public int getY() {
        return y;
    }

    /**
     * Berechnet die Nachbarposition in der angegebenen Richtung.
     *
     * @param richtung Richtung, in die gegangen wird
     * @return neue Position neben dieser Position
     */
    public Position move(Richtung richtung) {
        return switch(richtung) {
            case NORD -> new Position(x, y-1);
            case OST -> new Position(x+1, y);
            case SUED -> new Position(x, y+1);
            case WEST -> new Position(x-1, y);
        };
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Position other)) {
            return false;
        }
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode(){
        return Objects.hash(x,y);
    }

    
}
