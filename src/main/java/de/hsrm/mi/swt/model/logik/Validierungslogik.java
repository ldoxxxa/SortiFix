package de.hsrm.mi.swt.model.logik;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hsrm.mi.swt.model.AnlagenPlan;
import de.hsrm.mi.swt.model.Position;
import de.hsrm.mi.swt.model.elemente.Foerderelement;
import de.hsrm.mi.swt.model.elemente.Kriterium;
import de.hsrm.mi.swt.model.elemente.Knick;
import de.hsrm.mi.swt.model.elemente.Richtung;
import de.hsrm.mi.swt.model.elemente.Weiche;
import de.hsrm.mi.swt.model.elemente.Weichenregel;
import de.hsrm.mi.swt.model.elemente.Ziel;
import de.hsrm.mi.swt.model.gepaeck.Gepaeckstueck;
import de.hsrm.mi.swt.model.logik.ValidierungsErgebnis.Fehler;
import de.hsrm.mi.swt.model.logik.ValidierungsErgebnis.FehlerTyp;
import de.hsrm.mi.swt.model.raster.Raster;
import de.hsrm.mi.swt.model.raster.Rasterfeld;

/**
 * Prueft den aktuellen Anlagenplan auf Fehler im Foerderweg.
 * Der EditorController ruft diese Klasse nach Aenderungen am Plan auf.
 */
public class Validierungslogik {

    /**
     * Prueft alle platzierten Foerderelemente im Raster.
     * Gesucht werden Sackgassen, unpassende Richtungen und einfache Schleifen.
     *
     * @param anlagenPlan Plan, der geprueft wird
     * @return Ergebnis mit allen gefundenen Fehlern
     */
    public ValidierungsErgebnis validiereFoerderweg(AnlagenPlan anlagenPlan) {
        List<Fehler> fehler = new ArrayList<>();
        Raster raster = anlagenPlan.getRaster();
        List<Foerderelement> alleElemente = alleElemente(raster);

        for (Foerderelement element : alleElemente) {
            // Zielfelder sind Endpunkte -> kein Nachfolger nötig, also Ausnahme = kein Fehler
            if (element instanceof Ziel) {
                continue;
            }

            // wo müsste Nachfolger liegen?
            List<Nachfolger> nachfolger = ermittleNachfolger(element);

            for (Nachfolger aktuellerNachfolger : nachfolger) {
                Position nachfolgerPos = aktuellerNachfolger.position();
                // liegt dort ein Element?
                if (!raster.istGueltig(nachfolgerPos) || raster.istFrei(nachfolgerPos)) {
                    fehler.add(new Fehler(
                        FehlerTyp.SACKGASSE,
                        "Kein Nachfolger an Position " + nachfolgerPos,
                        element.getPosition()
                    ));
                    continue; // weil Richtungsprüfung dann unnötig
                }

                // Richtung überprüfen
                Foerderelement nachfolgerElement = raster.getFeld(nachfolgerPos).getElement();
                if (!pruefeRichtungskompatibilitaet(
                        aktuellerNachfolger.ausgangsrichtung(), nachfolgerElement)) {
                    fehler.add(new Fehler(
                        FehlerTyp.UNGUELTIGE_RICHTUNG,
                        "Richtung passt nicht zwischen "
                            + element.getPosition() + " und " + nachfolgerPos,
                        element.getPosition()
                    ));
                }
            }
        }

        // Schleifenerkennung, nur wenn bisher keine Sackgassen gefunden
        if (fehler.isEmpty()) {
            fehler.addAll(pruefeAufSchleifen(raster, alleElemente));
        }

        fehler.addAll(pruefeGepaeckZiele(anlagenPlan, alleElemente));

        return fehler.isEmpty()
            ? ValidierungsErgebnis.gueltig()
            : ValidierungsErgebnis.ungueltig(fehler);
    }

    // gibt bei Weiche die Ausgangspositionen zurück: Standardausgang und konfigurierte Regelausgaenge
    private List<Nachfolger> ermittleNachfolger(Foerderelement element) {
        List<Nachfolger> nachfolger = new ArrayList<>();

        if (element instanceof Weiche weiche) {
            nachfolger.add(new Nachfolger(
                    weiche.getPosition().move(weiche.getStandardrichtung()),
                    weiche.getStandardrichtung()));
            for (Weichenregel regel : weiche.getRegeln()) {
                nachfolger.add(new Nachfolger(
                        weiche.getPosition().move(regel.getAusgangsrichtung()),
                        regel.getAusgangsrichtung()));
            }
        } else if (element instanceof Knick knick) {
            nachfolger.add(new Nachfolger(knick.getNachfolgerPosition(), knick.getAusgangsrichtung()));
        } else {
            nachfolger.add(new Nachfolger(element.getNachfolgerPosition(), element.getRichtung()));
        }

        return nachfolger;
    }

    // prüft, ob Richtungen von 2 Nachbarn zusammenpassen (1 Ele zeigt in Richtung, 2. kommt aus dieser Richtung)
    private boolean pruefeRichtungskompatibilitaet(Richtung ausgangsrichtung, Foerderelement zu) {
        if (zu instanceof Ziel) {
            return true;
        }
        if (zu instanceof Weiche weiche) {
            return weiche.getRichtung() == gegenrichtung(ausgangsrichtung);
        }
        if (zu instanceof Knick knick) {
            return eingangsrichtungFuerKnick(knick) == ausgangsrichtung;
        }
        return zu.getRichtung() == ausgangsrichtung;
    }

    private Richtung eingangsrichtungFuerKnick(Knick knick) {
        return knick.getEingangsrichtung();
    }

    // gibt entgegengesetzte Richtung zurück
    // NORD <-> SUED, OST <-> WEST
    private Richtung gegenrichtung(Richtung richtung) {
        return switch (richtung) {
            case NORD -> Richtung.SUED;
            case SUED -> Richtung.NORD;
            case OST  -> Richtung.WEST;
            case WEST -> Richtung.OST;
        };
    }

    /**
     * Sucht Schleifen, indem der Foerderweg ab jedem Element verfolgt wird.
     */
    private List<Fehler> pruefeAufSchleifen(Raster raster,
            List<Foerderelement> alleElemente) {
        List<Fehler> fehler = new ArrayList<>();
        Set<Position> globalBesucht = new HashSet<>();

        for (Foerderelement start : alleElemente) {
            if (globalBesucht.contains(start.getPosition())) {
                continue;
            }

            Set<Position> pfad = new HashSet<>();
            Foerderelement aktuell = start;

            while (aktuell != null && !(aktuell instanceof Ziel)) {
                Position pos = aktuell.getPosition();

                if (pfad.contains(pos)) {
                    // Position wurde im aktuellen Pfad schon besucht = Schleife
                    fehler.add(new Fehler(
                        FehlerTyp.SCHLEIFE,
                        "Schleife erkannt an Position " + pos,
                        pos
                    ));
                    break;
                }

                pfad.add(pos);
                globalBesucht.add(pos);

                // nächstes Element im Förderweg
                Position nachfolgerPos = ermittleErsteNachfolgerPosition(aktuell);
                if (!raster.istGueltig(nachfolgerPos) || raster.istFrei(nachfolgerPos)) {
                    break; // Sackgasse wurde oben schon gemeldet
                }
                aktuell = raster.getFeld(nachfolgerPos).getElement();
            }
        }

        return fehler;
    }

    private Position ermittleErsteNachfolgerPosition(Foerderelement element) {
        if (element instanceof Weiche weiche) {
            return weiche.getPosition().move(weiche.getStandardrichtung());
        }
        return element.getNachfolgerPosition();
    }

    private List<Fehler> pruefeGepaeckZiele(AnlagenPlan anlagenPlan, List<Foerderelement> alleElemente) {
        List<Fehler> fehler = new ArrayList<>();
        Set<String> zielnamen = new HashSet<>();
        for (Foerderelement element : alleElemente) {
            if (element instanceof Ziel ziel && ziel.getZielname() != null && !ziel.getZielname().isBlank()) {
                zielnamen.add(ziel.getZielname());
            }
        }

        for (Gepaeckstueck gepaeckstueck : anlagenPlan.getGepaeckstuecke()) {
            if (!zielnamen.contains(gepaeckstueck.getZiel())) {
                fehler.add(new Fehler(
                        FehlerTyp.ZIEL_FEHLT,
                        "Kein Ziel für Gepäckziel " + gepaeckstueck.getZiel(),
                        gepaeckstueck.getPosition()));
            }
        }
        return fehler;
    }

    /**
     * Prueft Regeln einer Weiche auf Vollstaendigkeit und gueltige Werte.
     *
     * @param regeln Regeln aus dem Weichendialog
     * @return Validierungsergebnis fuer diese Regeln
     */
    public ValidierungsErgebnis validiereWeichenregeln(List<Weichenregel> regeln) {
        List<Fehler> fehler = new ArrayList<>();

        for (Weichenregel regel : regeln) {
            if (regel.getWert() == null || regel.getWert().isBlank()) {
                fehler.add(new Fehler(
                    FehlerTyp.UNGUELTIGE_RICHTUNG,
                    "Regel ohne Vergleichswert",
                    null // keine Pos, da Weichenregel kein Rasterobjekt ist
                ));
            }

            if (regel.getAusgangsrichtung() == null) {
                fehler.add(new Fehler(
                    FehlerTyp.UNGUELTIGE_RICHTUNG,
                    "Regel ohne Ausgangsrichtung",
                    null
                ));
            }

            // typmäßige Prüfung
            if (regel.getKriterium() == Kriterium.GEWICHT) {
                try {
                    Double.parseDouble(regel.getWert());
                } catch (NumberFormatException e) {
                    fehler.add(new Fehler(
                        FehlerTyp.UNGUELTIGE_RICHTUNG,
                        "Wert für GEWICHT muss eine Zahl sein, war: "
                            + regel.getWert(),
                        null
                    ));
                }
            }
        }

        return fehler.isEmpty()
            ? ValidierungsErgebnis.gueltig()
            : ValidierungsErgebnis.ungueltig(fehler);
    }

    // Hilfsmethode: sammelt alle Förderelemente aus Raster in Liste, iteriert über alle Rasterfelder & gibt nur belegte zurück
    private List<Foerderelement> alleElemente(Raster raster) {
        List<Foerderelement> elemente = new ArrayList<>();
        for (int y = 0; y < raster.getHoehe(); y++) {
            for (int x = 0; x < raster.getBreite(); x++) {
                Rasterfeld feld = raster.getFeld(new Position(x, y));
                if (!feld.istFrei()) {
                    elemente.add(feld.getElement());
                }
            }
        }
        return elemente;
    }

    private record Nachfolger(Position position, Richtung ausgangsrichtung) {
    }
}
