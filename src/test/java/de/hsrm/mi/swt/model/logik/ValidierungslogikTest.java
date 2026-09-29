package de.hsrm.mi.swt.model.logik;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.hsrm.mi.swt.model.AnlagenPlan;
import de.hsrm.mi.swt.model.Position;
import de.hsrm.mi.swt.model.elemente.Gerade;
import de.hsrm.mi.swt.model.elemente.Knick;
import de.hsrm.mi.swt.model.elemente.Kriterium;
import de.hsrm.mi.swt.model.elemente.Richtung;
import de.hsrm.mi.swt.model.elemente.Weichenregel;
import de.hsrm.mi.swt.model.elemente.Ziel;
import de.hsrm.mi.swt.model.gepaeck.Gepaeckstueck;
import de.hsrm.mi.swt.model.gepaeck.Gepaecktyp;
import de.hsrm.mi.swt.model.logik.ValidierungsErgebnis.FehlerTyp;
import de.hsrm.mi.swt.model.raster.Raster;

class ValidierungslogikTest {

    private final Validierungslogik validierungslogik = new Validierungslogik();

    @Test
    void validiereFoerderwegAkzeptiertGeradenWegZumZiel() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(3, 1));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeElementHinzu(new Gerade(new Position(1, 0), Richtung.OST));
        plan.fuegeElementHinzu(new Ziel(new Position(2, 0), Richtung.WEST, "A"));

        ValidierungsErgebnis ergebnis = validierungslogik.validiereFoerderweg(plan);

        assertTrue(ergebnis.istGueltig());
        assertTrue(ergebnis.getFehler().isEmpty());
    }

    @Test
    void validiereFoerderwegMeldetSackgasseWennNachfolgerFehlt() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));

        ValidierungsErgebnis ergebnis = validierungslogik.validiereFoerderweg(plan);

        assertFalse(ergebnis.istGueltig());
        assertEquals(1, ergebnis.getFehler().size());
        assertEquals(FehlerTyp.SACKGASSE, ergebnis.getFehler().get(0).getTyp());
        assertEquals(new Position(0, 0), ergebnis.getFehler().get(0).getPosition());
    }

    @Test
    void validiereFoerderwegMeldetUngueltigeRichtungWennNachfolgerEntgegenZeigt() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(3, 1));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeElementHinzu(new Gerade(new Position(1, 0), Richtung.WEST));

        ValidierungsErgebnis ergebnis = validierungslogik.validiereFoerderweg(plan);

        assertFalse(ergebnis.istGueltig());
        assertEquals(FehlerTyp.UNGUELTIGE_RICHTUNG, ergebnis.getFehler().get(0).getTyp());
        assertEquals(new Position(0, 0), ergebnis.getFehler().get(0).getPosition());
    }

    @Test
    void validiereFoerderwegMeldetUngueltigeRichtungWennGeradeInAndersAusgerichteteGeradeFuehrt() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 2));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeElementHinzu(new Gerade(new Position(1, 0), Richtung.SUED));
        plan.fuegeElementHinzu(new Ziel(new Position(1, 1), Richtung.NORD, "A"));

        ValidierungsErgebnis ergebnis = validierungslogik.validiereFoerderweg(plan);

        assertFalse(ergebnis.istGueltig());
        assertTrue(ergebnis.getFehler().stream()
                .anyMatch(fehler -> fehler.getTyp() == FehlerTyp.UNGUELTIGE_RICHTUNG
                        && fehler.getPosition().equals(new Position(0, 0))));
    }

    @Test
    void validiereFoerderwegMeldetUngueltigeRichtungWennGeradeSeitlichInKnickFuehrt() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(3, 1));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeElementHinzu(new Knick(new Position(1, 0), Richtung.OST));
        plan.fuegeElementHinzu(new Ziel(new Position(2, 0), Richtung.WEST, "A"));

        ValidierungsErgebnis ergebnis = validierungslogik.validiereFoerderweg(plan);

        assertFalse(ergebnis.istGueltig());
        assertTrue(ergebnis.getFehler().stream()
                .anyMatch(fehler -> fehler.getTyp() == FehlerTyp.UNGUELTIGE_RICHTUNG
                        && fehler.getPosition().equals(new Position(0, 0))));
    }

    @Test
    void validiereFoerderwegAkzeptiertPassendeGeradeInKnick() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 2));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.SUED));
        plan.fuegeElementHinzu(new Knick(new Position(0, 1), Richtung.OST));
        plan.fuegeElementHinzu(new Ziel(new Position(1, 1), Richtung.WEST, "A"));

        ValidierungsErgebnis ergebnis = validierungslogik.validiereFoerderweg(plan);

        assertTrue(ergebnis.istGueltig());
    }

    @Test
    void validiereFoerderwegMeldetSchleifeWennPfadImKreisFuehrt() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 2));
        Knick obenLinks = new Knick(new Position(0, 0), Richtung.WEST);
        obenLinks.setGespiegelt(true);
        Knick obenRechts = new Knick(new Position(1, 0), Richtung.NORD);
        obenRechts.setGespiegelt(true);
        Knick untenRechts = new Knick(new Position(1, 1), Richtung.OST);
        untenRechts.setGespiegelt(true);
        Knick untenLinks = new Knick(new Position(0, 1), Richtung.SUED);
        untenLinks.setGespiegelt(true);
        plan.fuegeElementHinzu(obenLinks);
        plan.fuegeElementHinzu(obenRechts);
        plan.fuegeElementHinzu(untenRechts);
        plan.fuegeElementHinzu(untenLinks);

        ValidierungsErgebnis ergebnis = validierungslogik.validiereFoerderweg(plan);

        assertFalse(ergebnis.istGueltig());
        assertEquals(1, ergebnis.getFehler().size());
        assertEquals(FehlerTyp.SCHLEIFE, ergebnis.getFehler().get(0).getTyp());
    }

    @Test
    void validiereFoerderwegMeldetFehlendesZielFuerGepaeck() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeElementHinzu(new Ziel(new Position(1, 0), Richtung.WEST, "Terminal A"));
        plan.fuegeGepaeckHinzu(new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "Terminal B", new Position(0, 0)));

        ValidierungsErgebnis ergebnis = validierungslogik.validiereFoerderweg(plan);

        assertFalse(ergebnis.istGueltig());
        assertTrue(ergebnis.getFehler().stream()
                .anyMatch(fehler -> fehler.getTyp() == FehlerTyp.ZIEL_FEHLT
                        && fehler.getPosition().equals(new Position(0, 0))));
    }

    @Test
    void validiereFoerderwegAkzeptiertGepaeckWennPassendesZielExistiert() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(2, 1));
        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeElementHinzu(new Ziel(new Position(1, 0), Richtung.WEST, "Terminal A"));
        plan.fuegeGepaeckHinzu(new Gepaeckstueck(
                "G1", 12.0, Gepaecktyp.KOFFER, "Terminal A", new Position(0, 0)));

        ValidierungsErgebnis ergebnis = validierungslogik.validiereFoerderweg(plan);

        assertTrue(ergebnis.istGueltig());
    }

    @Test
    void validiereWeichenregelnMeldetLeerenWertUndFehlendeAusgangsrichtung() {
        Weichenregel regel = new Weichenregel(Kriterium.ZIELORT, " ", null, 1);

        ValidierungsErgebnis ergebnis = validierungslogik.validiereWeichenregeln(
                java.util.List.of(regel));

        assertFalse(ergebnis.istGueltig());
        assertEquals(2, ergebnis.getFehler().size());
        assertTrue(ergebnis.getFehler().stream()
                .allMatch(fehler -> fehler.getTyp() == FehlerTyp.UNGUELTIGE_RICHTUNG));
    }

    @Test
    void validiereWeichenregelnMeldetNichtNumerischenGewichtswert() {
        Weichenregel regel = new Weichenregel(Kriterium.GEWICHT, "schwer", Richtung.OST, 1);

        ValidierungsErgebnis ergebnis = validierungslogik.validiereWeichenregeln(
                java.util.List.of(regel));

        assertFalse(ergebnis.istGueltig());
        assertEquals(1, ergebnis.getFehler().size());
        assertEquals(FehlerTyp.UNGUELTIGE_RICHTUNG, ergebnis.getFehler().get(0).getTyp());
    }
}
