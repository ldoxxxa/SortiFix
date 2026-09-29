package de.hsrm.mi.swt.model.elemente;

/**
 * Regel fuer eine Weiche.
 * Sie sagt: Wenn ein Kriterium passt, soll ein bestimmter Ausgang genutzt werden.
 */
public class Weichenregel {
    // z.B. Gewicht > 20kg -> geradeaus 
    // kriterium = GEWICHT, wert = "20", ausgangsrichtung = SUED
    private Kriterium kriterium; 
    private String wert;
    private Richtung ausgangsrichtung;
    private int prioritaet;

    /**
     * Erstellt eine Weichenregel.
     *
     * @param kriterium Eigenschaft, die geprueft wird
     * @param wert Vergleichswert als Text
     * @param ausgangsrichtung Ausgang, wenn die Regel passt
     * @param prioritaet Reihenfolge der Pruefung, kleinere Zahl zuerst
     */
    public Weichenregel(Kriterium kriterium, String wert, Richtung ausgangsrichtung, int prioritaet) {
        this.kriterium = kriterium;
        this.wert = wert;
        this.ausgangsrichtung = ausgangsrichtung;
        this.prioritaet = prioritaet;
    }

    /**
     * @return Kriterium, das geprueft wird
     */
    public Kriterium getKriterium() { return kriterium; }

    /**
     * @return Vergleichswert der Regel
     */
    public String getWert() { return wert; }

    /**
     * @return Ausgang, wenn die Regel passt
     */
    public Richtung getAusgangsrichtung() { return ausgangsrichtung; }

    /**
     * @return Prioritaet der Regel
     */
    public int getPrioritaet() { return prioritaet; }

    /**
     * @param kriterium neues Kriterium
     */
    public void setKriterium(Kriterium kriterium) { this.kriterium = kriterium; }

    /**
     * @param wert neuer Vergleichswert
     */
    public void setWert(String wert) { this.wert = wert; }

    /**
     * @param ausgangsrichtung neuer Ausgang
     */
    public void setAusgangsrichtung(Richtung ausgangsrichtung) { this.ausgangsrichtung = ausgangsrichtung; }

    /**
     * @param prioritaet neue Prioritaet
     */
    public void setPrioritaet(int prioritaet) { this.prioritaet = prioritaet; }

}
