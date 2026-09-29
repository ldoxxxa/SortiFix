package de.hsrm.mi.swt.controller;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hsrm.mi.swt.model.AnlagenPlan;
import de.hsrm.mi.swt.model.Position;
import de.hsrm.mi.swt.model.gepaeck.Gepaeckstueck;
import de.hsrm.mi.swt.model.logik.Simulationslogik;
import de.hsrm.mi.swt.model.logik.Simulationslogik.SimulationsSchritt;
import de.hsrm.mi.swt.model.logik.Simulationslogik.SimulationsSchritt.Ereignis;
import de.hsrm.mi.swt.model.logik.ValidierungsErgebnis;
import de.hsrm.mi.swt.model.logik.Validierungslogik;

/**
 * Controller fuer die Simulationsansicht.
 */
public class SimulationsController {

    private final AnlagenPlan anlagenPlan;
    private final Validierungslogik validierungslogik = new Validierungslogik();
    private final Simulationslogik simulationslogik = new Simulationslogik();
    private final Set<Gepaeckstueck> gemeldeteProbleme = new HashSet<>();
    private final Map<Gepaeckstueck, Position> startPositionen = new LinkedHashMap<>();
    private ValidierungsErgebnis letztesValidierungsErgebnis = ValidierungsErgebnis.gueltig();
    private String letzteSimulationsMeldung;

    /**
     * Erstellt einen Controller fuer den uebergebenen Anlagenplan.
     *
     * @param anlagenPlan zu simulierender Anlagenplan
     */
    public SimulationsController(AnlagenPlan anlagenPlan) {
        this.anlagenPlan = anlagenPlan;
    }

    /**
     * Prueft, ob die Simulation starten darf.
     *
     * @return true, wenn ein gueltiger Foerderweg und Gepaeck vorhanden sind
     */
    public boolean kannSimulationStarten() {
        letztesValidierungsErgebnis = validierungslogik.validiereFoerderweg(anlagenPlan);
        anlagenPlan.validierungAktualisiert(letztesValidierungsErgebnis);
        return !anlagenPlan.getGepaeckstuecke().isEmpty()
                && letztesValidierungsErgebnis.istGueltig();
    }

    /**
     * Setzt die Simulation in den laufenden Zustand.
     */
    public void starteSimulation() {
        gemeldeteProbleme.clear();
        startPositionen.clear();
        for (Gepaeckstueck gepaeckstueck : anlagenPlan.getGepaeckstuecke()) {
            startPositionen.put(gepaeckstueck, gepaeckstueck.getPosition());
        }
        letzteSimulationsMeldung = null;
        simulationslogik.starten(anlagenPlan);
    }

    /**
     * Pausiert die laufende Simulation, ohne Positionen zu veraendern.
     */
    public void pausiereSimulation() {
        simulationslogik.pausieren();
    }

    /**
     * Setzt eine pausierte Simulation an derselben Stelle fort.
     */
    public void setzeSimulationFort() {
        simulationslogik.fortsetzen();
    }

    /**
     * Berechnet und uebernimmt einen Simulationsschritt.
     *
     * @return true, wenn weitere Schritte moeglich sind
     */
    public boolean fuehreSimulationsschrittAus() {
        List<SimulationsSchritt> schritte = simulationslogik.berechneNaechstenSchritt(anlagenPlan);
        if (schritte.isEmpty()) {
            return false;
        }

        simulationslogik.wendeSchritteAn(anlagenPlan, schritte);
        letzteSimulationsMeldung = null;
        boolean fehlerAufgetreten = false;
        for (SimulationsSchritt schritt : schritte) {
            anlagenPlan.gepaeckGeaendert(schritt.getGepaeckstueck());
            if (schritt.getEreignis() == Ereignis.HERUNTERGEFALLEN
                    && gemeldeteProbleme.add(schritt.getGepaeckstueck())) {
                letzteSimulationsMeldung = "Gepäckstück ist vom Band gefallen";
                fehlerAufgetreten = true;
            } else if (schritt.getEreignis() == Ereignis.FALSCHES_ZIEL
                    && gemeldeteProbleme.add(schritt.getGepaeckstueck())) {
                letzteSimulationsMeldung = "Gepäckstück hat ein falsches Ziel erreicht";
                fehlerAufgetreten = true;
            }
        }

        if (fehlerAufgetreten) {
            simulationslogik.stoppen(anlagenPlan);
            stelleStartPositionenWiederHer();
            return false;
        }

        if (habenAlleGepaeckstueckeIhrZielErreicht(schritte)) {
            simulationslogik.stoppen(anlagenPlan);
            stelleStartPositionenWiederHer();
            return false;
        }
        return true;
    }

    /**
     * Gibt die zuletzt bei einem Simulationsschritt neu aufgetretene Meldung zurueck.
     *
     * @return neue Meldung oder null
     */
    public String getLetzteSimulationsMeldung() {
        return letzteSimulationsMeldung;
    }

    /**
     * Stoppt die Simulation.
     */
    public void stoppeSimulation() {
        simulationslogik.stoppen(anlagenPlan);
    }

    /**
     * Setzt nur die laufende Simulation auf ihre Startpositionen zurueck.
     */
    public void reset() {
        stelleStartPositionenWiederHer();
        gemeldeteProbleme.clear();
        letzteSimulationsMeldung = null;
        simulationslogik.stoppen(anlagenPlan);
    }

    private boolean habenAlleGepaeckstueckeIhrZielErreicht(List<SimulationsSchritt> schritte) {
        return !anlagenPlan.getGepaeckstuecke().isEmpty()
                && schritte.size() == anlagenPlan.getGepaeckstuecke().size()
                && schritte.stream().allMatch(schritt -> schritt.getEreignis() == Ereignis.ZIEL_ERREICHT);
    }

    private void stelleStartPositionenWiederHer() {
        for (Map.Entry<Gepaeckstueck, Position> eintrag : startPositionen.entrySet()) {
            eintrag.getKey().setPosition(eintrag.getValue());
            anlagenPlan.gepaeckGeaendert(eintrag.getKey());
        }
    }
}
