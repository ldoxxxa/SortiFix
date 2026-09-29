package de.hsrm.mi.swt.model.event;

import de.hsrm.mi.swt.model.elemente.Foerderelement;
import de.hsrm.mi.swt.model.elemente.Weiche;
import de.hsrm.mi.swt.model.gepaeck.Gepaeckstueck;
import de.hsrm.mi.swt.model.logik.ValidierungsErgebnis;

/**
 * Ereignis, das eine Aenderung am Anlagenplan beschreibt.
 * Die View nutzt diese Events, um sich nach Model-Aenderungen neu zu zeichnen.
 */
public interface AnlagenplanEvent {

    /**
     * Event fuer ein neu platziertes Foerderelement.
     */
    class ElementHinzugefuegt implements AnlagenplanEvent {
        private final Foerderelement element;

        /**
         * @param element hinzugefuegtes Element
         */
        public ElementHinzugefuegt(Foerderelement element) {
            this.element = element;
        }

        /**
         * @return hinzugefuegtes Element
         */
        public Foerderelement getElement() {
            return element;
        }
    }

    /**
     * Event fuer ein entferntes Foerderelement.
     */
    class ElementEntfernt implements AnlagenplanEvent {
        private final Foerderelement element;

        /**
         * @param element entferntes Element
         */
        public ElementEntfernt(Foerderelement element) {
            this.element = element;
        }

        /**
         * @return entferntes Element
         */
        public Foerderelement getElement() {
            return element;
        }
    }

    /**
     * Event fuer ein geaendertes Foerderelement.
     */
    class ElementGeaendert implements AnlagenplanEvent {
        private final Foerderelement element;

        /**
         * @param element geaendertes Element
         */
        public ElementGeaendert(Foerderelement element) {
            this.element = element;
        }

        /**
         * @return geaendertes Element
         */
        public Foerderelement getElement() {
            return element;
        }
    }

    /**
     * Event fuer eine geaenderte Weichenkonfiguration.
     */
    class WeicheKonfiguriert implements AnlagenplanEvent {
        private final Weiche weiche;

        /**
         * @param weiche konfigurierte Weiche
         */
        public WeicheKonfiguriert(Weiche weiche) {
            this.weiche = weiche;
        }

        /**
         * @return konfigurierte Weiche
         */
        public Weiche getWeiche() {
            return weiche;
        }
    }

    /**
     * Event fuer ein neu platziertes Gepaeckstueck.
     */
    class GepaeckHinzugefuegt implements AnlagenplanEvent {
        private final Gepaeckstueck gepaeckstueck;

        /**
         * @param gepaeckstueck hinzugefuegtes Gepaeckstueck
         */
        public GepaeckHinzugefuegt(Gepaeckstueck gepaeckstueck) {
            this.gepaeckstueck = gepaeckstueck;
        }

        /**
         * @return hinzugefuegtes Gepaeckstueck
         */
        public Gepaeckstueck getGepaeckstueck() {
            return gepaeckstueck;
        }
    }

    /**
     * Event fuer ein entferntes Gepaeckstueck.
     */
    class GepaeckEntfernt implements AnlagenplanEvent {
        private final Gepaeckstueck gepaeckstueck;

        /**
         * @param gepaeckstueck entferntes Gepaeckstueck
         */
        public GepaeckEntfernt(Gepaeckstueck gepaeckstueck) {
            this.gepaeckstueck = gepaeckstueck;
        }

        /**
         * @return entferntes Gepaeckstueck
         */
        public Gepaeckstueck getGepaeckstueck() {
            return gepaeckstueck;
        }
    }

    /**
     * Event fuer ein geaendertes Gepaeckstueck.
     */
    class GepaeckGeaendert implements AnlagenplanEvent {
        private final Gepaeckstueck gepaeckstueck;

        /**
         * @param gepaeckstueck geaendertes Gepaeckstueck
         */
        public GepaeckGeaendert(Gepaeckstueck gepaeckstueck) {
            this.gepaeckstueck = gepaeckstueck;
        }

        /**
         * @return geaendertes Gepaeckstueck
         */
        public Gepaeckstueck getGepaeckstueck() {
            return gepaeckstueck;
        }
    }

    /**
     * Event fuer ein neues Ergebnis der Foerderweg-Validierung.
     */
    class ValidierungAktualisiert implements AnlagenplanEvent {
        private final ValidierungsErgebnis ergebnis;

        /**
         * @param ergebnis neues Validierungsergebnis
         */
        public ValidierungAktualisiert(ValidierungsErgebnis ergebnis) {
            this.ergebnis = ergebnis;
        }

        /**
         * @return neues Validierungsergebnis
         */
        public ValidierungsErgebnis getErgebnis() {
            return ergebnis;
        }
    }

    /**
     * Event fuer einen geladenen Plan.
     * Es braucht keine Zusatzdaten, weil die View den Plan komplett neu lesen kann.
     */
    class PlanGeladen implements AnlagenplanEvent {
        // kein Zusatzdatum nötig -> die View liest sich den kompletten AnlagenPlan einfach neu aus
    }
}
