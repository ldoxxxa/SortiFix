package de.hsrm.mi.swt.model.logik;
import java.util.Collections;
import java.util.List;
import de.hsrm.mi.swt.model.Position;

/**
 * Ergebnis einer Validierung.
 * Es enthaelt entweder keine Fehler oder eine unveraenderbare Fehlerliste.
 */
public class ValidierungsErgebnis {

    /**
     * Arten von Fehlern, die beim Pruefen eines Foerderwegs auftreten koennen.
     */
    public enum FehlerTyp {
        SACKGASSE, // keinen gültigen Nachfolger
        UNGUELTIGE_RICHTUNG, // zwei Nachbarn zeigen nicht zusammen
        SCHLEIFE, // Förderweg führt im Kreis
        ZIEL_FEHLT // Gepaeckstueck verweist auf einen Zielort ohne Zielstation
    }

    /**
     * Beschreibt einen einzelnen Fehler aus der Validierungslogik.
     */
    public static class Fehler {
        private final FehlerTyp typ;
        private final String beschreibung;
        private final Position position;

        /**
         * Erstellt einen Validierungsfehler.
         *
         * @param typ Art des Fehlers
         * @param beschreibung lesbare Fehlermeldung
         * @param position Position im Raster, falls vorhanden
         */
        public Fehler(FehlerTyp typ, String beschreibung, Position position) {
            this.typ = typ;
            this.beschreibung = beschreibung;
            this.position = position;
        }

        /**
         * @return Art des Fehlers
         */
        public FehlerTyp getTyp() { return typ; }

        /**
         * @return lesbare Beschreibung des Fehlers
         */
        public String getBeschreibung() { return beschreibung; }

        /**
         * @return Position des Fehlers oder null
         */
        public Position getPosition() { return position; }
    }

    private final List<Fehler> fehler;

    private ValidierungsErgebnis(List<Fehler> fehler) {
        this.fehler = fehler;
    }

    /**
     * Erstellt ein gueltiges Ergebnis ohne Fehler.
     *
     * @return gueltiges Validierungsergebnis
     */
    public static ValidierungsErgebnis gueltig() {
        return new ValidierungsErgebnis(Collections.emptyList());
    }

    /**
     * Erstellt ein ungueltiges Ergebnis mit Fehlern.
     *
     * @param fehler gefundene Fehler
     * @return ungueltiges Validierungsergebnis
     */
    public static ValidierungsErgebnis ungueltig(List<Fehler> fehler) {
        // verhindert, dass jemand nach Erzeugen noch Fehler zur Liste hinzufügt
        return new ValidierungsErgebnis(Collections.unmodifiableList(fehler)); 
    }

    /**
     * Prueft, ob keine Fehler vorhanden sind.
     *
     * @return true, wenn die Validierung erfolgreich war
     */
    public boolean istGueltig() {
        return fehler.isEmpty();
    }

    /**
     * Gibt die gefundenen Fehler zurueck.
     *
     * @return unveraenderbare Fehlerliste
     */
    public List<Fehler> getFehler() {
        return fehler;
    }
}
