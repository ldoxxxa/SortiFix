package de.hsrm.mi.swt.model.elemente;

import de.hsrm.mi.swt.model.Position;

/**
 * Kurvenstück eines Förderbands.
 * Durch Spiegeln kann der Ausgang der Kurve auf die andere Seite zeigen.
 */
public class Knick extends Foerderelement {

    private boolean gespiegelt;

    /**
     * Erstellt einen Knick im Raster.
     *
     * @param position Position im Raster
     * @param richtung ungespiegelte Ausgangsrichtung des Knicks
     */
    public Knick(Position position, Richtung richtung) {
        super(position, richtung);
    }

    /**
     * @return true, wenn der Knick gespiegelt ist
     */
    public boolean isGespiegelt() {
        return gespiegelt;
    }

    /**
     * @param gespiegelt neuer Spiegelzustand
     */
    public void setGespiegelt(boolean gespiegelt) {
        this.gespiegelt = gespiegelt;
    }

    /**
     * Wechselt zwischen normaler und gespiegelter Darstellung.
     */
    public void spiegle() {
        gespiegelt = !gespiegelt;
    }

    /**
     * Liefert die Richtung, aus der Gepäck in den Knick hineinfahren darf.
     * Spiegeln verändert nur den Ausgang, nicht den Eingang.
     *
     * @return Eingangsrichtung des Knicks
     */
    public Richtung getEingangsrichtung() {
        return dreheRechts(getRichtung());
    }

    /**
     * Liefert die Richtung, in die Gepäck den Knick verlässt.
     *
     * @return Ausgangsrichtung des Knicks
     */
    public Richtung getAusgangsrichtung() {
        return gespiegelt ? gegenrichtung(getRichtung()) : getRichtung();
    }

    @Override
    public Position getNachfolgerPosition() {
        return getPosition().move(getAusgangsrichtung());
    }

    private Richtung dreheRechts(Richtung richtung) {
        return switch (richtung) {
            case NORD -> Richtung.OST;
            case OST -> Richtung.SUED;
            case SUED -> Richtung.WEST;
            case WEST -> Richtung.NORD;
        };
    }

    private Richtung gegenrichtung(Richtung richtung) {
        return switch (richtung) {
            case NORD -> Richtung.SUED;
            case SUED -> Richtung.NORD;
            case OST -> Richtung.WEST;
            case WEST -> Richtung.OST;
        };
    }
}
