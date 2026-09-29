package persistence.format;

import de.hsrm.mi.swt.model.AnlagenPlan;

/**
 * Format fuer das Speichern und Laden von Anlagenplaenen.
 */
public interface Speicherformat {

    /**
     * Wandelt einen Anlagenplan in eine speicherbare Textdarstellung um.
     *
     * @param anlagenPlan zu speichernder Plan
     * @return serialisierte Darstellung
     */
    String speichere(AnlagenPlan anlagenPlan);

    /**
     * Erstellt aus einer Textdarstellung wieder einen Anlagenplan.
     *
     * @param inhalt gespeicherter Inhalt
     * @return geladener Anlagenplan
     */
    AnlagenPlan lade(String inhalt);
}
