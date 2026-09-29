package de.hsrm.mi.swt.model.event;

/**
 * Observer-Schnittstelle fuer Klassen, die auf Aenderungen im AnlagenPlan reagieren.
 */
public interface AnlagenplanListener {
    /**
     * Wird aufgerufen, wenn sich der Anlagenplan geaendert hat.
     *
     * @param event beschreibt die Art der Aenderung
     */
    void onAnlagenplanEvent(AnlagenplanEvent event);
}
