package de.hsrm.mi.swt.model.logik;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.hsrm.mi.swt.model.AnlagenPlan;
import de.hsrm.mi.swt.model.Position;
import de.hsrm.mi.swt.model.elemente.Gerade;
import de.hsrm.mi.swt.model.elemente.Knick;
import de.hsrm.mi.swt.model.elemente.Richtung;
import de.hsrm.mi.swt.model.elemente.Ziel;
import de.hsrm.mi.swt.model.gepaeck.Gepaeckstueck;
import de.hsrm.mi.swt.model.gepaeck.Gepaecktyp;
import de.hsrm.mi.swt.model.logik.Simulationslogik.SimulationsSchritt;
import de.hsrm.mi.swt.model.logik.Simulationslogik.SimulationsSchritt.Ereignis;
import de.hsrm.mi.swt.model.raster.Raster;

class SimulationslogikTest {

    @Test
    void berechneNaechstenSchrittBewegtGepaeckAufNachfolgerfeld() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        Gepaeckstueck gepaeck = new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "A", new Position(0, 0));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeElementHinzu(new Ziel(new Position(1, 0), Richtung.WEST, "A"));
        plan.fuegeGepaeckHinzu(gepaeck);
        Simulationslogik simulationslogik = new Simulationslogik();
        simulationslogik.starten(plan);

        List<SimulationsSchritt> schritte = simulationslogik.berechneNaechstenSchritt(plan);
        simulationslogik.wendeSchritteAn(plan, schritte);

        assertEquals(1, schritte.size());
        assertEquals(Ereignis.BEWEGT, schritte.get(0).getEreignis());
        assertEquals(new Position(1, 0), schritte.get(0).getNeuePosition());
        assertEquals(new Position(1, 0), gepaeck.getPosition());
    }

    @Test
    void berechneNaechstenSchrittNutztGespiegeltenKnickAusgang() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        Gepaeckstueck gepaeck = new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "A", new Position(1, 0));
        Knick knick = new Knick(new Position(1, 0), Richtung.OST);
        knick.setGespiegelt(true);
        plan.fuegeElementHinzu(new Ziel(new Position(0, 0), Richtung.OST, "A"));
        plan.fuegeElementHinzu(knick);
        plan.fuegeGepaeckHinzu(gepaeck);
        Simulationslogik simulationslogik = new Simulationslogik();
        simulationslogik.starten(plan);

        List<SimulationsSchritt> schritte = simulationslogik.berechneNaechstenSchritt(plan);

        assertEquals(1, schritte.size());
        assertEquals(Ereignis.BEWEGT, schritte.get(0).getEreignis());
        assertEquals(new Position(0, 0), schritte.get(0).getNeuePosition());
    }

    @Test
    void berechneNaechstenSchrittMeldetZielErreichtBeiPassendemZiel() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(1, 1));
        Gepaeckstueck gepaeck = new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "A", new Position(0, 0));
        plan.fuegeElementHinzu(new Ziel(new Position(0, 0), Richtung.WEST, "A"));
        plan.fuegeGepaeckHinzu(gepaeck);
        Simulationslogik simulationslogik = new Simulationslogik();
        simulationslogik.starten(plan);

        List<SimulationsSchritt> schritte = simulationslogik.berechneNaechstenSchritt(plan);

        assertEquals(1, schritte.size());
        assertEquals(Ereignis.ZIEL_ERREICHT, schritte.get(0).getEreignis());
        assertEquals(new Position(0, 0), gepaeck.getPosition());
        assertTrue(simulationslogik.istSimulationBeendet(plan, schritte));
    }

    @Test
    void berechneNaechstenSchrittMeldetHeruntergefallenWennNachfolgerfeldLeerIst() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        Gepaeckstueck gepaeck = new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "A", new Position(0, 0));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeGepaeckHinzu(gepaeck);
        Simulationslogik simulationslogik = new Simulationslogik();
        simulationslogik.starten(plan);

        List<SimulationsSchritt> schritte = simulationslogik.berechneNaechstenSchritt(plan);
        simulationslogik.wendeSchritteAn(plan, schritte);

        assertEquals(1, schritte.size());
        assertEquals(Ereignis.HERUNTERGEFALLEN, schritte.get(0).getEreignis());
        assertEquals(new Position(1, 0), schritte.get(0).getNeuePosition());
        assertEquals(new Position(1, 0), gepaeck.getPosition());
        assertTrue(simulationslogik.istSimulationBeendet(plan, schritte));
    }

    @Test
    void berechneNaechstenSchrittMeldetFalschesZielBeiUnpassendemZielnamen() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(1, 1));
        Gepaeckstueck gepaeck = new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "A", new Position(0, 0));
        plan.fuegeElementHinzu(new Ziel(new Position(0, 0), Richtung.WEST, "B"));
        plan.fuegeGepaeckHinzu(gepaeck);
        Simulationslogik simulationslogik = new Simulationslogik();
        simulationslogik.starten(plan);

        List<SimulationsSchritt> schritte = simulationslogik.berechneNaechstenSchritt(plan);

        assertEquals(1, schritte.size());
        assertEquals(Ereignis.FALSCHES_ZIEL, schritte.get(0).getEreignis());
        assertEquals(new Position(0, 0), schritte.get(0).getNeuePosition());
        assertTrue(simulationslogik.istSimulationBeendet(plan, schritte));
    }

    @Test
    void berechneNaechstenSchrittLiefertKeineSchritteWennSimulationGestopptIst() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        Gepaeckstueck gepaeck = new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "A", new Position(0, 0));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeElementHinzu(new Ziel(new Position(1, 0), Richtung.WEST, "A"));
        plan.fuegeGepaeckHinzu(gepaeck);
        Simulationslogik simulationslogik = new Simulationslogik();

        List<SimulationsSchritt> schritte = simulationslogik.berechneNaechstenSchritt(plan);

        assertTrue(schritte.isEmpty());
        assertEquals(new Position(0, 0), gepaeck.getPosition());
    }

    @Test
    void berechneNaechstenSchrittLiefertKeineSchritteWennSimulationPausiertIst() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        Gepaeckstueck gepaeck = new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "A", new Position(0, 0));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeElementHinzu(new Ziel(new Position(1, 0), Richtung.WEST, "A"));
        plan.fuegeGepaeckHinzu(gepaeck);
        Simulationslogik simulationslogik = new Simulationslogik();
        simulationslogik.starten(plan);
        simulationslogik.pausieren();

        List<SimulationsSchritt> schritte = simulationslogik.berechneNaechstenSchritt(plan);

        assertTrue(schritte.isEmpty());
        assertEquals(new Position(0, 0), gepaeck.getPosition());
    }
}
