package de.hsrm.mi.swt.model.gepaeck;

/**
 * Einfache Kategorien fuer Gepaeckstuecke.
 */
public enum Gepaecktyp {
    HANDGEPAECK("Handgepäck"),
    KOFFER("Koffer"),
    FRACHT("Fracht");

    private final String anzeigeName;

    Gepaecktyp(String anzeigeName) {
        this.anzeigeName = anzeigeName;
    }

    @Override
    public String toString() {
        return anzeigeName;
    }
}
