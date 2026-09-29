package de.hsrm.mi.swt.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hsrm.mi.swt.model.elemente.Foerderelement;
import de.hsrm.mi.swt.model.elemente.Weiche;
import de.hsrm.mi.swt.model.elemente.Weichenregel;
import de.hsrm.mi.swt.model.event.AnlagenplanEvent;
import de.hsrm.mi.swt.model.event.AnlagenplanListener;
import de.hsrm.mi.swt.model.gepaeck.Gepaeckstueck;
import de.hsrm.mi.swt.model.raster.Raster;
import de.hsrm.mi.swt.model.raster.Rasterfeld;

/**
 * Zentrale Datenklasse der Anlage.
 * Sie verwaltet Raster, Foerderelemente, Gepaeckstuecke und informiert Listener ueber Aenderungen.
 */
public class AnlagenPlan {

    private String name;
    private final LocalDateTime erstelltAm;
    private LocalDateTime geaendertAm; // wird bei jedem fireEvent(...) automatisch aktualisiert

    private final Raster raster;

    // Gepäckstücke in Liste für saubere Trennung, damit man bei Bewegung nur position am Objekt ändern muss
    // & AnlagenPlan.getGepaeckstuecke() nötzlich für Iteration in Simulationslogik
    private final List<Gepaeckstueck> gepaeckstuecke = new ArrayList<>();

    private final List<AnlagenplanListener> listeners = new ArrayList<>();

    /**
     * Erstellt einen Anlagenplan mit Namen und Raster.
     *
     * @param name Anzeigename des Plans
     * @param raster Raster der Anlage
     */
    public AnlagenPlan(String name, Raster raster) {
        this.name = name;
        this.raster = raster;
        this.erstelltAm = LocalDateTime.now();
        this.geaendertAm = this.erstelltAm;
    }

    /**
     * Registriert einen Listener, der Plan-Aenderungen erhalten soll.
     *
     * @param listener neuer Listener
     */
    public void addListener(AnlagenplanListener listener) {
        listeners.add(listener);
    }

    /**
     * Entfernt einen vorher registrierten Listener.
     *
     * @param listener zu entfernender Listener
     */
    public void removeListener(AnlagenplanListener listener) {
        listeners.remove(listener);
    }

    private void fireEvent(AnlagenplanEvent event) {
        geaendertAm = LocalDateTime.now();
        for (AnlagenplanListener listener : listeners) {
            listener.onAnlagenplanEvent(event);
        }
    }

    /**
     * @return Anzeigename des Plans
     */
    public String getName() {
        return name;
    }

    /**
     * @param name neuer Anzeigename des Plans
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Erstellzeitpunkt des Plans
     */
    public LocalDateTime getErstelltAm() {
        return erstelltAm;
    }

    /**
     * @return Zeitpunkt der letzten Aenderung
     */
    public LocalDateTime getGeaendertAm() {
        return geaendertAm;
    }

    /**
     * @return Raster der Anlage
     */
    public Raster getRaster() {
        return raster;
    }

    /**
     * Gibt alle Gepaeckstuecke zurueck.
     * Aenderungen sollen ueber die Methoden dieser Klasse laufen, damit Events ausgeloest werden.
     *
     * @return nicht veraenderbare Liste der Gepaeckstuecke
     */
    public List<Gepaeckstueck> getGepaeckstuecke() {
        // verhindert, dass Controller versehentlich anlagen(...).add(...) am Oberserver vorbei aufruft ohne Event
        // jede Änderung muss über fuegeGepaeckHinzu / entferneGepaeck laufen
        return Collections.unmodifiableList(gepaeckstuecke);
    }

    /**
     * Legt ein Foerderelement auf sein Rasterfeld.
     *
     * @param element Element, das hinzugefuegt wird
     */
    public void fuegeElementHinzu(Foerderelement element) {
        Rasterfeld feld = raster.getFeld(element.getPosition());
        feld.setElement(element);
        fireEvent(new AnlagenplanEvent.ElementHinzugefuegt(element));
    }

    /**
     * Entfernt ein Foerderelement aus dem Raster.
     *
     * @param element Element, das entfernt wird
     */
    public void entferneElement(Foerderelement element) {
        Rasterfeld feld = raster.getFeld(element.getPosition());
        if (feld.getElement() == element) {
            feld.setElement(null);
        }
        fireEvent(new AnlagenplanEvent.ElementEntfernt(element));
    }

    /**
     * Meldet, dass ein bestehendes Element geaendert wurde.
     * Der konkrete Wert wurde vorher bereits am Element gesetzt.
     *
     * @param element geaendertes Element
     */
    public void elementGeaendert(Foerderelement element) {
        fireEvent(new AnlagenplanEvent.ElementGeaendert(element));
    }

    /**
     * Verschiebt ein Element von seiner aktuellen Position auf eine neue Position.
     *
     * @param element zu verschiebendes Element
     * @param neuePosition neue Position im Raster
     */
    public void verschiebeElement(Foerderelement element, Position neuePosition) {
        Rasterfeld altesFeld = raster.getFeld(element.getPosition());
        if (altesFeld.getElement() == element) {
            altesFeld.setElement(null);
        }

        element.setPosition(neuePosition);
        raster.getFeld(neuePosition).setElement(element);

        fireEvent(new AnlagenplanEvent.ElementGeaendert(element));
    }

    /**
     * Ersetzt die Regeln einer Weiche und markiert sie als konfiguriert.
     *
     * @param weiche zu konfigurierende Weiche
     * @param neueRegeln neue Routingregeln
     */
    public void konfiguriereWeiche(Weiche weiche, List<Weichenregel> neueRegeln) {
        weiche.getRegeln().clear();
        weiche.getRegeln().addAll(neueRegeln);
        weiche.setKonfiguriert(true);

        fireEvent(new AnlagenplanEvent.WeicheKonfiguriert(weiche));
    }

    /**
     * Meldet ein neues Validierungsergebnis an die View.
     *
     * @param ergebnis aktuelles Ergebnis der Validierung
     */
    public void validierungAktualisiert(de.hsrm.mi.swt.model.logik.ValidierungsErgebnis ergebnis) {
        fireEvent(new AnlagenplanEvent.ValidierungAktualisiert(ergebnis));
    }

    /**
     * Fuegt ein Gepaeckstueck zur Anlage hinzu.
     *
     * @param gepaeckstueck neues Gepaeckstueck
     */
    public void fuegeGepaeckHinzu(Gepaeckstueck gepaeckstueck) {
        gepaeckstuecke.add(gepaeckstueck);
        fireEvent(new AnlagenplanEvent.GepaeckHinzugefuegt(gepaeckstueck));
    }

    /**
     * Entfernt ein Gepaeckstueck aus der Anlage.
     *
     * @param gepaeckstueck zu entfernendes Gepaeckstueck
     */
    public void entferneGepaeck(Gepaeckstueck gepaeckstueck) {
        gepaeckstuecke.remove(gepaeckstueck);
        fireEvent(new AnlagenplanEvent.GepaeckEntfernt(gepaeckstueck));
    }

    /**
     * Meldet, dass ein Gepaeckstueck geaendert wurde, zum Beispiel seine Position.
     *
     * @param gepaeckstueck geaendertes Gepaeckstueck
     */
    public void gepaeckGeaendert(Gepaeckstueck gepaeckstueck) {
        fireEvent(new AnlagenplanEvent.GepaeckGeaendert(gepaeckstueck));
    }

    /**
     * Prueft, ob bereits Gepaeck auf einer Position liegt.
     *
     * @param position zu pruefende Position
     * @return true, wenn dort ein Gepaeckstueck steht
     */
    public boolean istFeldDurchGepaeckBelegt(Position position) {
        return gepaeckstuecke.stream()
                .anyMatch(g -> g.getPosition().equals(position));
    }

    /**
     * Meldet, dass ein Plan geladen wurde und die View neu zeichnen soll.
     */
    public void planGeladen() {
        fireEvent(new AnlagenplanEvent.PlanGeladen());
    }

    /**
     * Entfernt alle Foerderelemente und Gepaeckstuecke aus dem Plan.
     * Rastergroesse und Name bleiben erhalten, die View zeichnet sich komplett neu.
     */
    public void alleZuruecksetzen() {
        for (int y = 0; y < raster.getHoehe(); y++) {
            for (int x = 0; x < raster.getBreite(); x++) {
                raster.getFeld(new Position(x, y)).setElement(null);
            }
        }
        gepaeckstuecke.clear();
        fireEvent(new AnlagenplanEvent.PlanGeladen());
    }
}