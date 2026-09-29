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

class EditorControllerTest {

    @Test
    void platziereGepaeckSpeichertUebergebeneEigenschaften() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(1, 1));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        EditorController controller = new EditorController(plan);

        boolean platziert = controller.platziereGepaeck(
                new Position(0, 0), 18.5, Gepaecktyp.FRACHT, "Terminal B");

        assertTrue(platziert);
        assertEquals(1, plan.getGepaeckstuecke().size());
        Gepaeckstueck gepaeck = plan.getGepaeckstuecke().get(0);
        assertEquals(18.5, gepaeck.getGewicht());
        assertEquals(Gepaecktyp.FRACHT, gepaeck.getTyp());
        assertEquals("Terminal B", gepaeck.getZiel());
        assertEquals(new Position(0, 0), gepaeck.getPosition());
    }

    @Test
    void platziereGepaeckLehntUngueltigeEigenschaftenAb() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(1, 1));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        EditorController controller = new EditorController(plan);

        boolean platziert = controller.platziereGepaeck(
                new Position(0, 0), 0.0, Gepaecktyp.KOFFER, "Terminal A");

        assertFalse(platziert);
        assertTrue(plan.getGepaeckstuecke().isEmpty());
    }

    @Test
    void resetLoeschtAlleElementeUndGepaeckstuecke() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        EditorController controller = new EditorController(plan);
        controller.platziereGepaeck(new Position(0, 0), 5.0, Gepaecktyp.KOFFER, "Terminal A");

        controller.reset();

        assertTrue(plan.getRaster().istFrei(new Position(0, 0)));
        assertTrue(plan.getGepaeckstuecke().isEmpty());
        assertTrue(controller.getLetztesValidierungsErgebnis().istGueltig());
    }

    @Test
    void konfiguriereZielSpeichertZielnamen() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(1, 1));
        Ziel ziel = new Ziel(new Position(0, 0), Richtung.WEST, "Ziel");
        plan.fuegeElementHinzu(ziel);
        EditorController controller = new EditorController(plan);

        boolean gespeichert = controller.konfiguriereZiel(ziel, "Terminal B");

        assertTrue(gespeichert);
        assertEquals("Terminal B", ziel.getZielname());
    }

    @Test
    void verschiebeAusgewaehltesElementAufFreiesFeld() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        Gerade gerade = new Gerade(new Position(0, 0), Richtung.OST);
        plan.fuegeElementHinzu(gerade);
        EditorController controller = new EditorController(plan);

        assertTrue(controller.waehleElementZumVerschieben(new Position(0, 0)));
        boolean verschoben = controller.verschiebeAusgewaehltesElement(Richtung.OST);

        assertTrue(verschoben);
        assertTrue(plan.getRaster().istFrei(new Position(0, 0)));
        assertEquals(gerade, plan.getRaster().getFeld(new Position(1, 0)).getElement());
        assertEquals(new Position(1, 0), gerade.getPosition());
    }

    @Test
    void verschiebeAusgewaehltesElementBlockiertBelegtesFeld() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        Gerade links = new Gerade(new Position(0, 0), Richtung.OST);
        Gerade rechts = new Gerade(new Position(1, 0), Richtung.OST);
        plan.fuegeElementHinzu(links);
        plan.fuegeElementHinzu(rechts);
        EditorController controller = new EditorController(plan);

        assertTrue(controller.waehleElementZumVerschieben(new Position(0, 0)));
        boolean verschoben = controller.verschiebeAusgewaehltesElement(Richtung.OST);

        assertFalse(verschoben);
        assertEquals(links, plan.getRaster().getFeld(new Position(0, 0)).getElement());
        assertEquals(rechts, plan.getRaster().getFeld(new Position(1, 0)).getElement());
    }

    @Test
    void verschiebeAusgewaehltesElementBlockiertRasterrand() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(1, 1));
        Gerade gerade = new Gerade(new Position(0, 0), Richtung.OST);
        plan.fuegeElementHinzu(gerade);
        EditorController controller = new EditorController(plan);

        assertTrue(controller.waehleElementZumVerschieben(new Position(0, 0)));
        boolean verschoben = controller.verschiebeAusgewaehltesElement(Richtung.WEST);

        assertFalse(verschoben);
        assertEquals(gerade, plan.getRaster().getFeld(new Position(0, 0)).getElement());
        assertEquals(new Position(0, 0), gerade.getPosition());
    }

    @Test
    void verschiebeAusgewaehltesElementNimmtGepaeckMit() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        Gerade gerade = new Gerade(new Position(0, 0), Richtung.OST);
        plan.fuegeElementHinzu(gerade);
        EditorController controller = new EditorController(plan);
        controller.platziereGepaeck(new Position(0, 0), 5.0, Gepaecktyp.KOFFER, "Terminal A");

        assertTrue(controller.waehleElementZumVerschieben(new Position(0, 0)));
        boolean verschoben = controller.verschiebeAusgewaehltesElement(Richtung.OST);

        assertTrue(verschoben);
        assertEquals(new Position(1, 0), plan.getGepaeckstuecke().get(0).getPosition());
    }
}
