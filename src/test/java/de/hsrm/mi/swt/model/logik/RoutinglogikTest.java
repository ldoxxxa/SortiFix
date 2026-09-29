package de.hsrm.mi.swt.model.logik;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import de.hsrm.mi.swt.model.Position;
import de.hsrm.mi.swt.model.elemente.Kriterium;
import de.hsrm.mi.swt.model.elemente.Richtung;
import de.hsrm.mi.swt.model.elemente.Weiche;
import de.hsrm.mi.swt.model.elemente.Weichenregel;
import de.hsrm.mi.swt.model.gepaeck.Gepaeckstueck;
import de.hsrm.mi.swt.model.gepaeck.Gepaecktyp;

class RoutinglogikTest {

    private final Routinglogik routinglogik = new Routinglogik();

    @Test
    void ermittleRichtungVerwendetPassendeRegelMitHoechsterPrioritaet() {
        Weiche weiche = new Weiche(new Position(1, 1), Richtung.WEST, Richtung.OST);
        weiche.addRegel(new Weichenregel(Kriterium.GEWICHT, "10", Richtung.NORD, 2));
        weiche.addRegel(new Weichenregel(Kriterium.GEPÄCKART, "KOFFER", Richtung.SUED, 1));
        Gepaeckstueck gepaeck = new Gepaeckstueck(
                "G1", 15.0, Gepaecktyp.KOFFER, "A", new Position(1, 1));

        Richtung richtung = routinglogik.ermittleRichtung(weiche, gepaeck);

        assertEquals(Richtung.SUED, richtung);
    }

    @Test
    void ermittleRichtungVerwendetStandardrichtungWennKeineRegelPasst() {
        Weiche weiche = new Weiche(new Position(1, 1), Richtung.WEST, Richtung.OST);
        weiche.addRegel(new Weichenregel(Kriterium.ZIELORT, "B", Richtung.NORD, 1));
        Gepaeckstueck gepaeck = new Gepaeckstueck(
                "G1", 8.0, Gepaecktyp.HANDGEPAECK, "A", new Position(1, 1));

        Richtung richtung = routinglogik.ermittleRichtung(weiche, gepaeck);

        assertEquals(Richtung.OST, richtung);
    }
}
