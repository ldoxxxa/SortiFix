package de.hsrm.mi.swt.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.hsrm.mi.swt.model.AnlagenPlan;
import de.hsrm.mi.swt.model.Position;
import de.hsrm.mi.swt.model.elemente.Gerade;
import de.hsrm.mi.swt.model.elemente.Richtung;
import de.hsrm.mi.swt.model.elemente.Ziel;
import de.hsrm.mi.swt.model.gepaeck.Gepaeckstueck;
import de.hsrm.mi.swt.model.gepaeck.Gepaecktyp;
import de.hsrm.mi.swt.model.raster.Raster;

class SimulationsControllerTest {

    @Test
    void fuehreSimulationsschrittAusBewegtGepaeckAufNachfolgerfeld() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        Gepaeckstueck gepaeck = new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "A", new Position(0, 0));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeElementHinzu(new Ziel(new Position(1, 0), Richtung.WEST, "A"));
        plan.fuegeGepaeckHinzu(gepaeck);
        SimulationsController controller = new SimulationsController(plan);

        assertTrue(controller.kannSimulationStarten());
        controller.starteSimulation();
        boolean laeuftWeiter = controller.fuehreSimulationsschrittAus();

        assertTrue(laeuftWeiter);
        assertEquals(new Position(1, 0), gepaeck.getPosition());
    }

    @Test
    void fuehreSimulationsschrittAusSetztPositionenZurueckWennAlleZieleErreichtSind() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        Gepaeckstueck gepaeck = new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "A", new Position(0, 0));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeElementHinzu(new Ziel(new Position(1, 0), Richtung.WEST, "A"));
        plan.fuegeGepaeckHinzu(gepaeck);
        SimulationsController controller = new SimulationsController(plan);

        controller.starteSimulation();
        assertTrue(controller.fuehreSimulationsschrittAus());
        boolean laeuftWeiter = controller.fuehreSimulationsschrittAus();

        assertFalse(laeuftWeiter);
        assertEquals(new Position(0, 0), gepaeck.getPosition());
    }

    @Test
    void fuehreSimulationsschrittAusSetztPositionenZurueckWennFehlerAuftritt() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        Gepaeckstueck gepaeck = new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "A", new Position(0, 0));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeGepaeckHinzu(gepaeck);
        SimulationsController controller = new SimulationsController(plan);

        controller.starteSimulation();
        boolean laeuftWeiter = controller.fuehreSimulationsschrittAus();

        assertFalse(laeuftWeiter);
        assertEquals(new Position(0, 0), gepaeck.getPosition());
        assertEquals("Gepäckstück ist vom Band gefallen", controller.getLetzteSimulationsMeldung());
    }

    @Test
    void resetSetztNurSimulationZurueckUndLaesstAnlagenplanBestehen() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        Gepaeckstueck gepaeck = new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "A", new Position(0, 0));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeElementHinzu(new Ziel(new Position(1, 0), Richtung.WEST, "A"));
        plan.fuegeGepaeckHinzu(gepaeck);
        SimulationsController controller = new SimulationsController(plan);
        controller.starteSimulation();
        controller.fuehreSimulationsschrittAus();

        controller.reset();

        assertFalse(plan.getRaster().istFrei(new Position(0, 0)));
        assertEquals(1, plan.getGepaeckstuecke().size());
        assertEquals(new Position(0, 0), gepaeck.getPosition());
        assertFalse(controller.fuehreSimulationsschrittAus());
    }

    @Test
    void resetOhneGestarteteSimulationLoeschtNichts() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(1, 1));
        Gepaeckstueck gepaeck = new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "A", new Position(0, 0));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeGepaeckHinzu(new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "A", new Position(0, 0)));
        SimulationsController controller = new SimulationsController(plan);

        controller.reset();

        assertFalse(plan.getRaster().istFrei(new Position(0, 0)));
        assertEquals(1, plan.getGepaeckstuecke().size());
        assertFalse(controller.kannSimulationStarten());
    }
}
