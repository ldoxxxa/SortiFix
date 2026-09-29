package de.hsrm.mi.swt.model.logik;

import java.util.Comparator;
import java.util.List;

import de.hsrm.mi.swt.model.elemente.Richtung;
import de.hsrm.mi.swt.model.elemente.Weiche;
import de.hsrm.mi.swt.model.elemente.Weichenregel;
import de.hsrm.mi.swt.model.gepaeck.Gepaeckstueck;

/**
 * Entscheidet waehrend der Simulation, welchen Ausgang eine Weiche nimmt.
 * Dafuer werden die Regeln der Weiche mit dem Gepaeckstueck verglichen.
 */
public class Routinglogik {

    /**
     * Ermittelt die naechste Richtung fuer ein Gepaeckstueck auf einer Weiche.
     *
     * @param weiche Weiche, auf der das Gepaeck liegt
     * @param gepaeckstueck Gepaeckstueck, das weitergeleitet wird
     * @return passende Ausgangsrichtung oder Standardrichtung
     */
    public Richtung ermittleRichtung(Weiche weiche, Gepaeckstueck gepaeckstueck) {
        // Regeln nach prio sortieren (niedrigste Zahl = höchste Prio)
        List<Weichenregel> sortiertRegeln = weiche.getRegeln().stream()
                .sorted(Comparator.comparingInt(Weichenregel::getPrioritaet)) // sortiert aufsteigend nach Prio, bevor sie durchlaufen werden
                .toList();

        // erste zutreffende Regeln gewinnt
        // z.B. Regel 1 (Prio 1): GEPAECKART == FRACHT -> trifft zu -> return WEST -> anderen Regeln werden nicht mehr geprüft
        for (Weichenregel regel : sortiertRegeln) {
            if (regelTrifftZu(regel, gepaeckstueck)) {
                return regel.getAusgangsrichtung(); // sofort zurück, Rest wird ignoriert
            }
        }

        // keine Regel trifft zu -> Standardrichtung
        return weiche.getStandardrichtung();
    }

    // prüft, ob einzelne Weichenregel auf das Gepäckstücj zutrifft
    private boolean regelTrifftZu(Weichenregel regel, Gepaeckstueck gepaeckstueck) {
        return switch (regel.getKriterium()) {
            case ZIELORT -> gepaeckstueck.getZiel().equals(regel.getWert());
            case GEWICHT -> gepaeckstueck.getGewicht() >= Double.parseDouble(regel.getWert());
            case GEPÄCKART -> gepaeckstueck.getTyp().name().equals(regel.getWert());
        };
    }
}
