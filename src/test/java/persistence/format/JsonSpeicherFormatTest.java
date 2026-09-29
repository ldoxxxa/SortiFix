package persistence.format;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import de.hsrm.mi.swt.model.AnlagenPlan;
import de.hsrm.mi.swt.model.Position;
import de.hsrm.mi.swt.model.elemente.Foerderelement;
import de.hsrm.mi.swt.model.elemente.Gerade;
import de.hsrm.mi.swt.model.elemente.Knick;
import de.hsrm.mi.swt.model.elemente.Kriterium;
import de.hsrm.mi.swt.model.elemente.Richtung;
import de.hsrm.mi.swt.model.elemente.Weiche;
import de.hsrm.mi.swt.model.elemente.Weichenregel;
import de.hsrm.mi.swt.model.elemente.Ziel;
import de.hsrm.mi.swt.model.gepaeck.Gepaeckstueck;
import de.hsrm.mi.swt.model.gepaeck.Gepaecktyp;
import de.hsrm.mi.swt.model.raster.Raster;

class JsonSpeicherFormatTest {

    @Test
    void speichereUndLadeAnlagenplanMitElementenWeichenregelnUndGepaeck() {
        AnlagenPlan plan = new AnlagenPlan("Testplan", new Raster(4, 3));
        Knick knick = new Knick(new Position(1, 0), Richtung.SUED);
        knick.setGespiegelt(true);
        Weiche weiche = new Weiche(new Position(2, 0), Richtung.WEST, Richtung.SUED);
        weiche.setKonfiguriert(true);
        weiche.addRegel(new Weichenregel(Kriterium.GEPÄCKART, "FRACHT", Richtung.OST, 1));

        plan.fuegeElementHinzu(new Gerade(new Position(0, 0), Richtung.OST));
        plan.fuegeElementHinzu(knick);
        plan.fuegeElementHinzu(weiche);
        plan.fuegeElementHinzu(new Ziel(new Position(2, 1), Richtung.NORD, "Terminal B"));
        plan.fuegeGepaeckHinzu(new Gepaeckstueck(
                "G7", 23.5, Gepaecktyp.FRACHT, "Terminal B", new Position(0, 0)));

        JsonSpeicherFormat format = new JsonSpeicherFormat();
        AnlagenPlan geladen = format.lade(format.speichere(plan));

        assertEquals("Testplan", geladen.getName());
        assertEquals(4, geladen.getRaster().getBreite());
        assertEquals(3, geladen.getRaster().getHoehe());

        assertInstanceOf(Gerade.class, elementAn(geladen, 0, 0));
        Knick geladenerKnick = assertInstanceOf(Knick.class, elementAn(geladen, 1, 0));
        assertEquals(Richtung.SUED, geladenerKnick.getRichtung());
        assertTrue(geladenerKnick.isGespiegelt());

        Weiche geladeneWeiche = assertInstanceOf(Weiche.class, elementAn(geladen, 2, 0));
        assertEquals(Richtung.WEST, geladeneWeiche.getRichtung());
        assertEquals(Richtung.SUED, geladeneWeiche.getStandardrichtung());
        assertTrue(geladeneWeiche.isKonfiguriert());
        assertEquals(1, geladeneWeiche.getRegeln().size());
        assertEquals(Kriterium.GEPÄCKART, geladeneWeiche.getRegeln().get(0).getKriterium());

        Ziel ziel = assertInstanceOf(Ziel.class, elementAn(geladen, 2, 1));
        assertEquals("Terminal B", ziel.getZielname());

        List<Gepaeckstueck> gepaeckstuecke = geladen.getGepaeckstuecke();
        assertEquals(1, gepaeckstuecke.size());
        assertEquals("G7", gepaeckstuecke.get(0).getId());
        assertEquals(23.5, gepaeckstuecke.get(0).getGewicht());
        assertEquals(Gepaecktyp.FRACHT, gepaeckstuecke.get(0).getTyp());
        assertEquals("Terminal B", gepaeckstuecke.get(0).getZiel());
        assertEquals(new Position(0, 0), gepaeckstuecke.get(0).getPosition());
    }

    private Foerderelement elementAn(AnlagenPlan plan, int x, int y) {
        return plan.getRaster().getFeld(new Position(x, y)).getElement();
    }
}
