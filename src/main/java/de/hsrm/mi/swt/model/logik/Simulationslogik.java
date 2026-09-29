package de.hsrm.mi.swt.model.logik;

import java.util.ArrayList;
import java.util.List;

import de.hsrm.mi.swt.model.AnlagenPlan;
import de.hsrm.mi.swt.model.Position;
import de.hsrm.mi.swt.model.elemente.Foerderelement;
import de.hsrm.mi.swt.model.elemente.Knick;
import de.hsrm.mi.swt.model.elemente.Weiche;
import de.hsrm.mi.swt.model.elemente.Ziel;
import de.hsrm.mi.swt.model.elemente.Richtung;
import de.hsrm.mi.swt.model.gepaeck.Gepaeckstueck;
import de.hsrm.mi.swt.model.raster.Raster;

/**
 * Berechnet die Bewegung der Gepaeckstuecke durch die Anlage.
 * Die Klasse veraendert Positionen erst, wenn berechnete Schritte angewendet werden.
 */
public class Simulationslogik {

    // entscheidet an Weichen die Logik
    private final Routinglogik routinglogik = new Routinglogik();

    /**
     * Aktueller Zustand der Simulation.
     */
    public enum SimulationsStatus {
        LAEUFT, // aktiv
        PAUSIERT, // eingefroren
        GESTOPPT // noch nie gestartet oder nach Stop
    }

    private SimulationsStatus status = SimulationsStatus.GESTOPPT;

    /**
     * @return aktueller Simulationsstatus
     */
    public SimulationsStatus getStatus() {
        return status;
    }

    /**
     * Startet die Simulation.
     *
     * @param anlagenPlan Plan, der simuliert wird
     */
    public void starten(AnlagenPlan anlagenPlan) {
        status = SimulationsStatus.LAEUFT;
    }

    /**
     * Pausiert die Simulation, ohne Positionen zu veraendern.
     */
    public void pausieren() {
        status = SimulationsStatus.PAUSIERT;
    }

    /**
     * Setzt eine pausierte Simulation fort.
     */
    public void fortsetzen() {
        status = SimulationsStatus.LAEUFT;
    }

    /**
     * Stoppt die Simulation.
     *
     * @param anlagenPlan Plan, dessen Simulation beendet wird
     */
    public void stoppen(AnlagenPlan anlagenPlan) {
        status = SimulationsStatus.GESTOPPT;
    }

    /**
     * Berechnet fuer jedes Gepaeckstueck den naechsten Schritt.
     *
     * @param anlagenPlan aktueller Anlagenplan
     * @return berechnete Simulationsschritte
     */
    public List<SimulationsSchritt> berechneNaechstenSchritt(AnlagenPlan anlagenPlan) {
        List<SimulationsSchritt> schritte = new ArrayList<>();

        // wenn Simulation nicht läuft, leere Liste zurückgeben, nichts bewegen
        if (status != SimulationsStatus.LAEUFT) {
            return schritte;
        }

        Raster raster = anlagenPlan.getRaster();

        for (Gepaeckstueck gepaeckstueck : anlagenPlan.getGepaeckstuecke()) {
            Position aktuellePos = gepaeckstueck.getPosition();
            Foerderelement aktuellesElement = raster.getFeld(aktuellePos).getElement();

            // kein Element unter Gepäckstück, heißt heruntergefallen
            if (aktuellesElement == null) {
                schritte.add(new SimulationsSchritt(gepaeckstueck,
                    SimulationsSchritt.Ereignis.HERUNTERGEFALLEN, aktuellePos));
                continue;
            }

            // Gepäckstück steht auf Zielfeld -> prüfen ob richtige Ziel
            if (aktuellesElement instanceof Ziel ziel) {
                if (ziel.getZielname() != null &&
                    ziel.getZielname().equals(gepaeckstueck.getZiel())) {
                    schritte.add(new SimulationsSchritt(gepaeckstueck,
                        SimulationsSchritt.Ereignis.ZIEL_ERREICHT, aktuellePos));
                } else {
                    schritte.add(new SimulationsSchritt(gepaeckstueck,
                        SimulationsSchritt.Ereignis.FALSCHES_ZIEL, aktuellePos));
                }
                continue;
            }

            // Richtung bestimmen: bei Weiche = Routinglogik, bei Knick = berechneter Ausgang
            Richtung naechsteRichtung;
            if (aktuellesElement instanceof Weiche weiche) {
                naechsteRichtung = routinglogik.ermittleRichtung(weiche, gepaeckstueck);
            } else if (aktuellesElement instanceof Knick knick) {
                naechsteRichtung = knick.getAusgangsrichtung();
            } else {
                naechsteRichtung = aktuellesElement.getRichtung();
            }

            Position naechstePos = aktuellePos.move(naechsteRichtung);

            // nächste Position existiert nicht (Kartenrand) -> an aktueller Position gefallen
            // nächste Position existiert, aber ist leer -> ein Schritt hinein, dort gefallen
            // sonst -> normale Bewegung
            if (!raster.istGueltig(naechstePos)) {
                schritte.add(new SimulationsSchritt(gepaeckstueck,
                    SimulationsSchritt.Ereignis.HERUNTERGEFALLEN, aktuellePos));
            } else if (raster.istFrei(naechstePos)) {
                schritte.add(new SimulationsSchritt(gepaeckstueck,
                    SimulationsSchritt.Ereignis.HERUNTERGEFALLEN, naechstePos));
            } else {
                schritte.add(new SimulationsSchritt(gepaeckstueck,
                    SimulationsSchritt.Ereignis.BEWEGT, naechstePos));
            }
        }

        return schritte;
    }

    /**
     * Uebernimmt berechnete Schritte in den Plan.
     * Bei BEWEGT und HERUNTERGEFALLEN wird die Position des Gepaeckstuecks aktualisiert,
     * damit ein heruntergefallenes Gepaeckstueck sichtbar auf dem Feld liegt, wo es abgestuerzt ist.
     *
     * @param anlagenPlan aktueller Anlagenplan
     * @param schritte zuvor berechnete Schritte
     */
    public void wendeSchritteAn(AnlagenPlan anlagenPlan,
            List<SimulationsSchritt> schritte) {
        for (SimulationsSchritt schritt : schritte) {
            switch (schritt.getEreignis()) {
                case BEWEGT, HERUNTERGEFALLEN -> schritt.getGepaeckstueck()
                        .setPosition(schritt.getNeuePosition());
                case ZIEL_ERREICHT, FALSCHES_ZIEL -> {}
            }
        }
    }

    /**
     * Prueft, ob sich kein Gepaeckstueck mehr bewegt.
     *
     * @param anlagenPlan aktueller Anlagenplan
     * @param schritte zuletzt berechnete Schritte
     * @return true, wenn die Simulation beendet werden kann
     */
    public boolean istSimulationBeendet(AnlagenPlan anlagenPlan,
            List<SimulationsSchritt> schritte) {
        return schritte.stream().noneMatch(s ->
            s.getEreignis() == SimulationsSchritt.Ereignis.BEWEGT);
    }

    /**
     * Beschreibt, was in einem Simulationsschritt mit einem Gepaeckstueck passiert.
     */
    public static class SimulationsSchritt {

        /**
         * Moegliche Ergebnisse eines einzelnen Simulationsschritts.
         */
        public enum Ereignis {
            // 4 mögl. Ereignisse pro Gepäckstück pro Schritt
            BEWEGT, // normaler Transport zur nächsten Position
            ZIEL_ERREICHT, // richtiges Ziel erreicht
            FALSCHES_ZIEL, // Zielfeld erreicht, aber falsch
            HERUNTERGEFALLEN // kein gültiges nächstes ELement
        }

        private final Gepaeckstueck gepaeckstueck;
        private final Ereignis ereignis;
        private final Position neuePosition;

        /**
         * Erstellt einen Simulationsschritt.
         *
         * @param gepaeckstueck betroffenes Gepaeckstueck
         * @param ereignis Ergebnis des Schritts
         * @param neuePosition Zielposition oder aktuelle Position bei Fehlern
         */
        public SimulationsSchritt(Gepaeckstueck gepaeckstueck,
                Ereignis ereignis, Position neuePosition) {
            this.gepaeckstueck = gepaeckstueck;
            this.ereignis = ereignis;
            this.neuePosition = neuePosition;
        }

        /**
         * @return betroffenes Gepaeckstueck
         */
        public Gepaeckstueck getGepaeckstueck() { return gepaeckstueck; }

        /**
         * @return Ergebnis des Schritts
         */
        public Ereignis getEreignis() { return ereignis; }

        /**
         * @return Zielposition oder aktuelle Position bei Fehlern
         */
        public Position getNeuePosition() { return neuePosition; }
    }
}
