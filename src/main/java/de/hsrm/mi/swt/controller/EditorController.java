package de.hsrm.mi.swt.controller;

import java.util.List;

import de.hsrm.mi.swt.model.AnlagenPlan;
import de.hsrm.mi.swt.model.Position;
import de.hsrm.mi.swt.model.elemente.Foerderelement;
import de.hsrm.mi.swt.model.elemente.Gerade;
import de.hsrm.mi.swt.model.elemente.Knick;
import de.hsrm.mi.swt.model.elemente.Richtung;
import de.hsrm.mi.swt.model.elemente.Weiche;
import de.hsrm.mi.swt.model.elemente.Weichenregel;
import de.hsrm.mi.swt.model.elemente.Ziel;
import de.hsrm.mi.swt.model.gepaeck.Gepaeckstueck;
import de.hsrm.mi.swt.model.gepaeck.Gepaecktyp;
import de.hsrm.mi.swt.model.logik.ValidierungsErgebnis;
import de.hsrm.mi.swt.model.logik.ValidierungsErgebnis.Fehler;
import de.hsrm.mi.swt.model.logik.ValidierungsErgebnis.FehlerTyp;
import de.hsrm.mi.swt.model.logik.Validierungslogik;

/**
 * Controller fuer Bearbeitungsaktionen im Anlageneditor.
 */
public class EditorController {

    /**
     * Auswahl der aktuell zu platzierenden Elementart.
     */
    public enum Werkzeug {
        GERADE,
        KNICK,
        WEICHE,
        ZIEL,
        GEPAECK,
        SPIEGELN,
        DREHEN,
        VERSCHIEBEN,
        LOESCHEN
    }

    private final AnlagenPlan anlagenPlan;
    private final Validierungslogik validierungslogik = new Validierungslogik();
    private Werkzeug ausgewaehltesWerkzeug = Werkzeug.GERADE;
    private ValidierungsErgebnis letztesValidierungsErgebnis = ValidierungsErgebnis.gueltig();
    private Foerderelement ausgewaehltesElementZumVerschieben;
    private int gepaeckZaehler = 1;

    /**
     * Erstellt einen Controller fuer den uebergebenen Anlagenplan.
     *
     * @param anlagenPlan zu bearbeitender Anlagenplan
     */
    public EditorController(AnlagenPlan anlagenPlan) {
        this.anlagenPlan = anlagenPlan;
    }

    /**
     * Waehlt das Werkzeug aus, das beim naechsten Rasterklick verwendet wird.
     *
     * @param werkzeug auszuwaehlendes Werkzeug
     */
    public void waehleWerkzeug(Werkzeug werkzeug) {
        this.ausgewaehltesWerkzeug = werkzeug;
        if (werkzeug != Werkzeug.VERSCHIEBEN) {
            ausgewaehltesElementZumVerschieben = null;
        }
    }

    /**
     * Platziert das aktuell ausgewaehlte Foerderelement auf einem freien Rasterfeld.
     *
     * @param position Zielposition im Raster
     * @return true, wenn ein Element platziert wurde
     */
    public boolean platziereAusgewaehltesElement(Position position) {
        if (ausgewaehltesWerkzeug == Werkzeug.LOESCHEN) {
            return loescheElement(position);
        }
        if (ausgewaehltesWerkzeug == Werkzeug.SPIEGELN) {
            return spiegleKnick(position);
        }
        if (ausgewaehltesWerkzeug == Werkzeug.DREHEN) {
            return dreheElement(position);
        }
        if (ausgewaehltesWerkzeug == Werkzeug.VERSCHIEBEN) {
            return waehleElementZumVerschieben(position);
        }
        if (ausgewaehltesWerkzeug == Werkzeug.GEPAECK) {
            return platziereGepaeck(position);
        }

        if (!anlagenPlan.getRaster().istFrei(position)) {
            return false;
        }

        anlagenPlan.fuegeElementHinzu(erstelleElement(position));
        aktualisiereValidierung();
        return true;
    }

    /**
     * Platziert ein einfaches Gepaeckstueck auf einem Foerderelement.
     *
     * @param position Position des Foerderelements
     * @return true, wenn ein Gepaeckstueck platziert wurde
     */
    public boolean platziereGepaeck(Position position) {
        return platziereGepaeck(position, 10.0, Gepaecktyp.KOFFER, "Ziel");
    }

    /**
     * Platziert ein Gepaeckstueck mit den uebergebenen Eigenschaften.
     *
     * @param position Position des Foerderelements
     * @param gewicht Gewicht des Gepaeckstuecks
     * @param typ Art des Gepaeckstuecks
     * @param ziel Zielort des Gepaeckstuecks
     * @return true, wenn ein Gepaeckstueck platziert wurde
     */
    public boolean platziereGepaeck(Position position, double gewicht, Gepaecktyp typ, String ziel) {
        if (!anlagenPlan.getRaster().istGueltig(position)
                || anlagenPlan.getRaster().istFrei(position)
                || anlagenPlan.istFeldDurchGepaeckBelegt(position)
                || anlagenPlan.getRaster().getFeld(position).getElement() instanceof Ziel
                || gewicht <= 0
                || typ == null
                || ziel == null
                || ziel.isBlank()) {
            return false;
        }

        Gepaeckstueck gepaeckstueck = new Gepaeckstueck(
                "G" + gepaeckZaehler++,
                gewicht,
                typ,
                ziel.trim(),
                position);
        anlagenPlan.fuegeGepaeckHinzu(gepaeckstueck);
        return true;
    }

    /**
     * Entfernt ein vorhandenes Foerderelement an der Position.
     *
     * @param position Position des zu entfernenden Elements
     * @return true, wenn ein Element entfernt wurde
     */
    public boolean loescheElement(Position position) {
        if (!anlagenPlan.getRaster().istGueltig(position)) {
            return false;
        }

        Gepaeckstueck gepaeckAufFeld = anlagenPlan.getGepaeckstuecke().stream()
                .filter(g -> g.getPosition().equals(position))
                .findFirst()
                .orElse(null);
        if (gepaeckAufFeld != null) {
            anlagenPlan.entferneGepaeck(gepaeckAufFeld);
            return true;
        }

        if (anlagenPlan.getRaster().istFrei(position)) {
            return false;
        }

        Foerderelement element = anlagenPlan.getRaster().getFeld(position).getElement();
        anlagenPlan.entferneElement(element);
        aktualisiereValidierung();
        return true;
    }

    /**
     * Dreht ein vorhandenes Foerderelement an der Position im Uhrzeigersinn.
     *
     * @param position Position des zu drehenden Elements
     * @return true, wenn ein Element gedreht wurde
     */
    public boolean dreheElement(Position position) {
        if (!anlagenPlan.getRaster().istGueltig(position)
                || anlagenPlan.getRaster().istFrei(position)) {
            return false;
        }

        Foerderelement element = anlagenPlan.getRaster().getFeld(position).getElement();
        if (element instanceof Weiche) {
            return false;
        }
        element.setRichtung(naechsteRichtung(element.getRichtung()));
        anlagenPlan.elementGeaendert(element);
        aktualisiereValidierung();
        return true;
    }

    /**
     * Spiegelt einen vorhandenen Knick an der Position.
     *
     * @param position Position des zu spiegelnden Knicks
     * @return true, wenn ein Knick gespiegelt wurde
     */
    public boolean spiegleKnick(Position position) {
        if (!anlagenPlan.getRaster().istGueltig(position)
                || anlagenPlan.getRaster().istFrei(position)) {
            return false;
        }

        Foerderelement element = anlagenPlan.getRaster().getFeld(position).getElement();
        if (!(element instanceof Knick knick)) {
            return false;
        }

        knick.spiegle();
        anlagenPlan.elementGeaendert(knick);
        aktualisiereValidierung();
        return true;
    }

    /**
     * Waehlt ein vorhandenes Element aus, das anschliessend per Pfeiltasten verschoben werden kann.
     *
     * @param position Position des auszuwaehlenden Elements
     * @return true, wenn ein Element ausgewaehlt wurde
     */
    public boolean waehleElementZumVerschieben(Position position) {
        Foerderelement element = getElement(position);
        if (element == null) {
            ausgewaehltesElementZumVerschieben = null;
            return false;
        }

        ausgewaehltesElementZumVerschieben = element;
        return true;
    }

    /**
     * Verschiebt das ausgewaehlte Element um ein Feld in die angegebene Richtung.
     * Das Ziel muss im Raster liegen und darf weder durch ein Element noch durch Gepaeck belegt sein.
     *
     * @param richtung Bewegungsrichtung
     * @return true, wenn das Element verschoben wurde
     */
    public boolean verschiebeAusgewaehltesElement(Richtung richtung) {
        if (ausgewaehltesElementZumVerschieben == null || richtung == null) {
            return false;
        }

        Position altePosition = ausgewaehltesElementZumVerschieben.getPosition();
        Position neuePosition = altePosition.move(richtung);
        if (!anlagenPlan.getRaster().istGueltig(neuePosition)
                || !anlagenPlan.getRaster().istFrei(neuePosition)
                || anlagenPlan.istFeldDurchGepaeckBelegt(neuePosition)) {
            return false;
        }

        Gepaeckstueck gepaeckAufElement = anlagenPlan.getGepaeckstuecke().stream()
                .filter(gepaeck -> gepaeck.getPosition().equals(altePosition))
                .findFirst()
                .orElse(null);

        anlagenPlan.verschiebeElement(ausgewaehltesElementZumVerschieben, neuePosition);
        if (gepaeckAufElement != null) {
            gepaeckAufElement.setPosition(neuePosition);
            anlagenPlan.gepaeckGeaendert(gepaeckAufElement);
        }
        aktualisiereValidierung();
        return true;
    }

    /**
     * Gibt die Position des aktuell zum Verschieben ausgewaehlten Elements zurueck.
     *
     * @return Position des ausgewaehlten Elements oder null, wenn keines ausgewaehlt ist
     */
    public Position getAusgewaehlteVerschiebePosition() {
        return ausgewaehltesElementZumVerschieben == null
                ? null
                : ausgewaehltesElementZumVerschieben.getPosition();
    }

    /**
     * Konfiguriert eine Weiche mit Standardrichtung und Routingregeln.
     *
     * @param weiche zu konfigurierende Weiche
     * @param eingangsrichtung Eingangsrichtung der Weiche
     * @param standardrichtung Standardrichtung der Weiche
     * @param regeln Routingregeln der Weiche
     * @return true, wenn die Regeln gueltig waren und gespeichert wurden
     */
    public boolean konfiguriereWeiche(Weiche weiche,
            Richtung eingangsrichtung,
            Richtung standardrichtung,
            List<Weichenregel> regeln) {
        ValidierungsErgebnis regelErgebnis = validierungslogik.validiereWeichenregeln(regeln);
        if (!regelErgebnis.istGueltig()) {
            letztesValidierungsErgebnis = regelErgebnis;
            anlagenPlan.validierungAktualisiert(regelErgebnis);
            return false;
        }
        if (!sindWeichenrichtungenUnterschiedlich(eingangsrichtung, standardrichtung, regeln)) {
            letztesValidierungsErgebnis = ValidierungsErgebnis.ungueltig(List.of(new Fehler(
                    FehlerTyp.UNGUELTIGE_RICHTUNG,
                    "Eingang und Ausgänge der Weiche müssen unterschiedliche Richtungen haben",
                    weiche.getPosition())));
            anlagenPlan.validierungAktualisiert(letztesValidierungsErgebnis);
            return false;
        }

        weiche.setRichtung(eingangsrichtung);
        weiche.setStandardrichtung(standardrichtung);
        anlagenPlan.konfiguriereWeiche(weiche, regeln);
        aktualisiereValidierung();
        return true;
    }

    /**
     * Setzt den Zielnamen einer Zielstation.
     *
     * @param ziel zu konfigurierende Zielstation
     * @param zielname Zielortname
     * @return true, wenn der Zielname gespeichert wurde
     */
    public boolean konfiguriereZiel(Ziel ziel, String zielname) {
        if (ziel == null || zielname == null || zielname.isBlank()) {
            return false;
        }

        ziel.setZielname(zielname.trim());
        anlagenPlan.elementGeaendert(ziel);
        aktualisiereValidierung();
        return true;
    }

    /**
     * Gibt das Foerderelement an einer Position zurueck.
     *
     * @param position Position im Raster
     * @return Element an der Position oder null, wenn dort keines liegt
     */
    public Foerderelement getElement(Position position) {
        if (!anlagenPlan.getRaster().istGueltig(position)
                || anlagenPlan.getRaster().istFrei(position)) {
            return null;
        }
        return anlagenPlan.getRaster().getFeld(position).getElement();
    }

    /**
     * Gibt das aktuell aktive Werkzeug zurueck.
     *
     * @return ausgewaehltes Werkzeug
     */
    public Werkzeug getAusgewaehltesWerkzeug() {
        return ausgewaehltesWerkzeug;
    }

    /**
     * Gibt das zuletzt berechnete Validierungsergebnis zurueck.
     *
     * @return letztes Validierungsergebnis
     */
    public ValidierungsErgebnis getLetztesValidierungsErgebnis() {
        return letztesValidierungsErgebnis;
    }

    /**
     * Validiert den aktuellen Anlagenplan und meldet das Ergebnis an die View.
     *
     * @return aktuelles Validierungsergebnis
     */
    public ValidierungsErgebnis validiereAktuellenPlan() {
        aktualisiereValidierung();
        return letztesValidierungsErgebnis;
    }

    /**
     * Setzt den Anlagenplan vollstaendig zurueck: entfernt alle Foerderelemente und Gepaeckstuecke,
     * setzt die Validierung auf gueltig und den Gepaeck-Zaehler auf den Ausgangswert.
     */
    public void reset() {
        anlagenPlan.alleZuruecksetzen();
        ausgewaehltesElementZumVerschieben = null;
        gepaeckZaehler = 1;
        letztesValidierungsErgebnis = ValidierungsErgebnis.gueltig();
        anlagenPlan.validierungAktualisiert(letztesValidierungsErgebnis);
    }

    private Foerderelement erstelleElement(Position position) {
        return switch (ausgewaehltesWerkzeug) {
            case GERADE -> new Gerade(position, Richtung.OST);
            case KNICK -> new Knick(position, Richtung.OST);
            case WEICHE -> new Weiche(position, Richtung.OST, Richtung.SUED);
            case ZIEL -> new Ziel(position, Richtung.OST, "Ziel");
            case GEPAECK -> throw new IllegalStateException("Gepäckwerkzeug erzeugt kein Förderelement");
            case SPIEGELN -> throw new IllegalStateException("Spiegelwerkzeug erzeugt kein Element");
            case DREHEN -> throw new IllegalStateException("Drehwerkzeug erzeugt kein Element");
            case VERSCHIEBEN -> throw new IllegalStateException("Verschiebewerkzeug erzeugt kein Element");
            case LOESCHEN -> throw new IllegalStateException("Löschwerkzeug erzeugt kein Element");
        };
    }

    private Richtung naechsteRichtung(Richtung richtung) {
        return switch (richtung) {
            case NORD -> Richtung.OST;
            case OST -> Richtung.SUED;
            case SUED -> Richtung.WEST;
            case WEST -> Richtung.NORD;
        };
    }

    private boolean sindWeichenrichtungenUnterschiedlich(Richtung eingangsrichtung,
            Richtung standardrichtung,
            List<Weichenregel> regeln) {
        if (eingangsrichtung == standardrichtung) {
            return false;
        }
        return regeln.stream()
                .map(Weichenregel::getAusgangsrichtung)
                .noneMatch(richtung -> richtung == eingangsrichtung || richtung == standardrichtung);
    }

    private void aktualisiereValidierung() {
        letztesValidierungsErgebnis = validierungslogik.validiereFoerderweg(anlagenPlan);
        anlagenPlan.validierungAktualisiert(letztesValidierungsErgebnis);
    }
}
