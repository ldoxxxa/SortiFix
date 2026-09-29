package persistence.format;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.hsrm.mi.swt.model.AnlagenPlan;
import de.hsrm.mi.swt.model.Position;
import de.hsrm.mi.swt.model.elemente.Foerderelement;
import de.hsrm.mi.swt.model.elemente.Gerade;
import de.hsrm.mi.swt.model.elemente.Knick;
import de.hsrm.mi.swt.model.elemente.Kriterium;
import de.hsrm.mi.swt.model.elemente.Richtung;
import de.hsrm.mi.swt.model.elemente.Weiche;
import de.hsrm.mi.swt.model.elemente.Weichenregel;
import de.hsrm.mi.swt.model.elemente.Ziel;
import de.hsrm.mi.swt.model.gepaeck.Gepaeckstueck;
import de.hsrm.mi.swt.model.gepaeck.Gepaecktyp;
import de.hsrm.mi.swt.model.raster.Raster;
import de.hsrm.mi.swt.model.raster.Rasterfeld;

/**
 * JSON-basiertes Speicherformat fuer Anlagenplaene.
 */
public class JsonSpeicherFormat implements Speicherformat {

    @Override
    public String speichere(AnlagenPlan anlagenPlan) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"name\": ").append(text(anlagenPlan.getName())).append(",\n");
        json.append("  \"raster\": {\n");
        json.append("    \"breite\": ").append(anlagenPlan.getRaster().getBreite()).append(",\n");
        json.append("    \"hoehe\": ").append(anlagenPlan.getRaster().getHoehe()).append("\n");
        json.append("  },\n");
        json.append("  \"elemente\": [\n");
        schreibeElemente(json, anlagenPlan);
        json.append("  ],\n");
        json.append("  \"gepaeckstuecke\": [\n");
        schreibeGepaeck(json, anlagenPlan);
        json.append("  ]\n");
        json.append("}\n");
        return json.toString();
    }

    @Override
    public AnlagenPlan lade(String inhalt) {
        Object wert = new Parser(inhalt).parse();
        Map<String, Object> wurzel = alsObjekt(wert, "JSON-Wurzel");
        Map<String, Object> rasterJson = alsObjekt(wurzel.get("raster"), "raster");

        AnlagenPlan plan = new AnlagenPlan(
                alsText(wurzel.get("name"), "name"),
                new Raster(alsInt(rasterJson.get("breite"), "breite"),
                        alsInt(rasterJson.get("hoehe"), "hoehe")));

        for (Object elementJson : alsListe(wurzel.get("elemente"), "elemente")) {
            plan.fuegeElementHinzu(ladeElement(alsObjekt(elementJson, "element")));
        }
        for (Object gepaeckJson : alsListe(wurzel.get("gepaeckstuecke"), "gepaeckstuecke")) {
            plan.fuegeGepaeckHinzu(ladeGepaeck(alsObjekt(gepaeckJson, "gepaeck")));
        }
        plan.planGeladen();
        return plan;
    }

    private void schreibeElemente(StringBuilder json, AnlagenPlan anlagenPlan) {
        List<Foerderelement> elemente = alleElemente(anlagenPlan.getRaster());
        for (int i = 0; i < elemente.size(); i++) {
            Foerderelement element = elemente.get(i);
            json.append("    {\n");
            json.append("      \"typ\": ").append(text(elementTyp(element))).append(",\n");
            schreibePositionUndRichtung(json, element.getPosition(), element.getRichtung(), "      ");
            if (element instanceof Knick knick) {
                json.append(",\n      \"gespiegelt\": ").append(knick.isGespiegelt());
            } else if (element instanceof Ziel ziel) {
                json.append(",\n      \"zielname\": ").append(text(ziel.getZielname()));
            } else if (element instanceof Weiche weiche) {
                json.append(",\n      \"standardrichtung\": ").append(text(weiche.getStandardrichtung().name()));
                json.append(",\n      \"konfiguriert\": ").append(weiche.isKonfiguriert());
                json.append(",\n      \"regeln\": [\n");
                schreibeRegeln(json, weiche.getRegeln());
                json.append("      ]");
            }
            json.append("\n    }");
            if (i < elemente.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
    }

    private void schreibeRegeln(StringBuilder json, List<Weichenregel> regeln) {
        for (int i = 0; i < regeln.size(); i++) {
            Weichenregel regel = regeln.get(i);
            json.append("        {\n");
            json.append("          \"kriterium\": ").append(text(regel.getKriterium().name())).append(",\n");
            json.append("          \"wert\": ").append(text(regel.getWert())).append(",\n");
            json.append("          \"ausgangsrichtung\": ").append(text(regel.getAusgangsrichtung().name())).append(",\n");
            json.append("          \"prioritaet\": ").append(regel.getPrioritaet()).append("\n");
            json.append("        }");
            if (i < regeln.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
    }

    private void schreibeGepaeck(StringBuilder json, AnlagenPlan anlagenPlan) {
        List<Gepaeckstueck> gepaeckstuecke = anlagenPlan.getGepaeckstuecke();
        for (int i = 0; i < gepaeckstuecke.size(); i++) {
            Gepaeckstueck gepaeck = gepaeckstuecke.get(i);
            json.append("    {\n");
            json.append("      \"id\": ").append(text(gepaeck.getId())).append(",\n");
            json.append("      \"gewicht\": ").append(gepaeck.getGewicht()).append(",\n");
            json.append("      \"typ\": ").append(text(gepaeck.getTyp().name())).append(",\n");
            json.append("      \"ziel\": ").append(text(gepaeck.getZiel())).append(",\n");
            schreibePosition(json, gepaeck.getPosition(), "      ");
            json.append("\n    }");
            if (i < gepaeckstuecke.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
    }

    private void schreibePositionUndRichtung(StringBuilder json, Position position,
            Richtung richtung, String einzug) {
        schreibePosition(json, position, einzug);
        json.append(",\n").append(einzug).append("\"richtung\": ").append(text(richtung.name()));
    }

    private void schreibePosition(StringBuilder json, Position position, String einzug) {
        json.append(einzug).append("\"x\": ").append(position.getX()).append(",\n");
        json.append(einzug).append("\"y\": ").append(position.getY());
    }

    private Foerderelement ladeElement(Map<String, Object> elementJson) {
        String typ = alsText(elementJson.get("typ"), "typ");
        Position position = ladePosition(elementJson);
        Richtung richtung = Richtung.valueOf(alsText(elementJson.get("richtung"), "richtung"));

        return switch (typ) {
            case "GERADE" -> new Gerade(position, richtung);
            case "KNICK" -> ladeKnick(elementJson, position, richtung);
            case "WEICHE" -> ladeWeiche(elementJson, position, richtung);
            case "ZIEL" -> new Ziel(position, richtung, alsText(elementJson.get("zielname"), "zielname"));
            default -> throw new IllegalArgumentException("Unbekannter Elementtyp: " + typ);
        };
    }

    private Knick ladeKnick(Map<String, Object> elementJson, Position position, Richtung richtung) {
        Knick knick = new Knick(position, richtung);
        knick.setGespiegelt(alsBoolean(elementJson.getOrDefault("gespiegelt", false), "gespiegelt"));
        return knick;
    }

    private Weiche ladeWeiche(Map<String, Object> elementJson, Position position, Richtung richtung) {
        Weiche weiche = new Weiche(position, richtung,
                Richtung.valueOf(alsText(elementJson.get("standardrichtung"), "standardrichtung")));
        weiche.setKonfiguriert(alsBoolean(elementJson.getOrDefault("konfiguriert", false), "konfiguriert"));
        for (Object regelJson : alsListe(elementJson.get("regeln"), "regeln")) {
            Map<String, Object> regelObjekt = alsObjekt(regelJson, "regel");
            weiche.addRegel(new Weichenregel(
                    Kriterium.valueOf(alsText(regelObjekt.get("kriterium"), "kriterium")),
                    alsText(regelObjekt.get("wert"), "wert"),
                    Richtung.valueOf(alsText(regelObjekt.get("ausgangsrichtung"), "ausgangsrichtung")),
                    alsInt(regelObjekt.get("prioritaet"), "prioritaet")));
        }
        return weiche;
    }

    private Gepaeckstueck ladeGepaeck(Map<String, Object> gepaeckJson) {
        return new Gepaeckstueck(
                alsText(gepaeckJson.get("id"), "id"),
                alsDouble(gepaeckJson.get("gewicht"), "gewicht"),
                Gepaecktyp.valueOf(alsText(gepaeckJson.get("typ"), "typ")),
                alsText(gepaeckJson.get("ziel"), "ziel"),
                ladePosition(gepaeckJson));
    }

    private Position ladePosition(Map<String, Object> json) {
        return new Position(alsInt(json.get("x"), "x"), alsInt(json.get("y"), "y"));
    }

    private String elementTyp(Foerderelement element) {
        if (element instanceof Gerade) {
            return "GERADE";
        }
        if (element instanceof Knick) {
            return "KNICK";
        }
        if (element instanceof Weiche) {
            return "WEICHE";
        }
        if (element instanceof Ziel) {
            return "ZIEL";
        }
        throw new IllegalArgumentException("Unbekanntes Foerderelement: " + element.getClass().getName());
    }

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

    private String text(String wert) {
        if (wert == null) {
            return "null";
        }
        return "\"" + wert
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    private static Map<String, Object> alsObjekt(Object wert, String feld) {
        if (wert instanceof Map<?, ?> objekt) {
            Map<String, Object> ergebnis = new LinkedHashMap<>();
            for (Map.Entry<?, ?> eintrag : objekt.entrySet()) {
                ergebnis.put(String.valueOf(eintrag.getKey()), eintrag.getValue());
            }
            return ergebnis;
        }
        throw new IllegalArgumentException("JSON-Feld ist kein Objekt: " + feld);
    }

    private static List<Object> alsListe(Object wert, String feld) {
        if (wert instanceof List<?> liste) {
            return new ArrayList<>(liste);
        }
        throw new IllegalArgumentException("JSON-Feld ist keine Liste: " + feld);
    }

    private static String alsText(Object wert, String feld) {
        if (wert instanceof String text) {
            return text;
        }
        throw new IllegalArgumentException("JSON-Feld ist kein Text: " + feld);
    }

    private static int alsInt(Object wert, String feld) {
        if (wert instanceof Number zahl) {
            return zahl.intValue();
        }
        throw new IllegalArgumentException("JSON-Feld ist keine Zahl: " + feld);
    }

    private static double alsDouble(Object wert, String feld) {
        if (wert instanceof Number zahl) {
            return zahl.doubleValue();
        }
        throw new IllegalArgumentException("JSON-Feld ist keine Zahl: " + feld);
    }

    private static boolean alsBoolean(Object wert, String feld) {
        if (wert instanceof Boolean bool) {
            return bool;
        }
        throw new IllegalArgumentException("JSON-Feld ist kein Wahrheitswert: " + feld);
    }

    private static class Parser {
        private final String text;
        private int pos;

        Parser(String text) {
            this.text = text;
        }

        Object parse() {
            Object wert = parseWert();
            ueberspringeLeerzeichen();
            if (pos != text.length()) {
                throw new IllegalArgumentException("Unerwarteter Inhalt ab Position " + pos);
            }
            return wert;
        }

        private Object parseWert() {
            ueberspringeLeerzeichen();
            if (istAmEnde()) {
                throw new IllegalArgumentException("Unerwartetes Ende");
            }
            char zeichen = text.charAt(pos);
            return switch (zeichen) {
                case '{' -> parseObjekt();
                case '[' -> parseListe();
                case '"' -> parseString();
                case 't', 'f' -> parseBoolean();
                case 'n' -> parseNull();
                default -> parseZahl();
            };
        }

        private Map<String, Object> parseObjekt() {
            erwarte('{');
            Map<String, Object> objekt = new LinkedHashMap<>();
            ueberspringeLeerzeichen();
            if (naechstesIst('}')) {
                return objekt;
            }
            do {
                String schluessel = parseString();
                ueberspringeLeerzeichen();
                erwarte(':');
                objekt.put(schluessel, parseWert());
                ueberspringeLeerzeichen();
            } while (naechstesIst(','));
            erwarte('}');
            return objekt;
        }

        private List<Object> parseListe() {
            erwarte('[');
            List<Object> liste = new ArrayList<>();
            ueberspringeLeerzeichen();
            if (naechstesIst(']')) {
                return liste;
            }
            do {
                liste.add(parseWert());
                ueberspringeLeerzeichen();
            } while (naechstesIst(','));
            erwarte(']');
            return liste;
        }

        private String parseString() {
            erwarte('"');
            StringBuilder ergebnis = new StringBuilder();
            while (!istAmEnde()) {
                char zeichen = text.charAt(pos++);
                if (zeichen == '"') {
                    return ergebnis.toString();
                }
                if (zeichen == '\\') {
                    if (istAmEnde()) {
                        throw new IllegalArgumentException("Ungueltige Escape-Sequenz");
                    }
                    ergebnis.append(parseEscape(text.charAt(pos++)));
                } else {
                    ergebnis.append(zeichen);
                }
            }
            throw new IllegalArgumentException("Nicht abgeschlossener Textwert");
        }

        private char parseEscape(char zeichen) {
            return switch (zeichen) {
                case '"' -> '"';
                case '\\' -> '\\';
                case '/' -> '/';
                case 'b' -> '\b';
                case 'f' -> '\f';
                case 'n' -> '\n';
                case 'r' -> '\r';
                case 't' -> '\t';
                default -> throw new IllegalArgumentException("Unbekannte Escape-Sequenz: \\" + zeichen);
            };
        }

        private Boolean parseBoolean() {
            if (text.startsWith("true", pos)) {
                pos += 4;
                return true;
            }
            if (text.startsWith("false", pos)) {
                pos += 5;
                return false;
            }
            throw new IllegalArgumentException("Ungueltiger Wahrheitswert ab Position " + pos);
        }

        private Object parseNull() {
            if (text.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new IllegalArgumentException("Ungueltiger Nullwert ab Position " + pos);
        }

        private Number parseZahl() {
            int start = pos;
            if (text.charAt(pos) == '-') {
                pos++;
            }
            while (!istAmEnde() && Character.isDigit(text.charAt(pos))) {
                pos++;
            }
            if (!istAmEnde() && text.charAt(pos) == '.') {
                pos++;
                while (!istAmEnde() && Character.isDigit(text.charAt(pos))) {
                    pos++;
                }
            }
            String zahl = text.substring(start, pos);
            try {
                return zahl.contains(".") ? Double.parseDouble(zahl) : Integer.parseInt(zahl);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Ungueltige Zahl: " + zahl, e);
            }
        }

        private void ueberspringeLeerzeichen() {
            while (!istAmEnde() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }

        private boolean naechstesIst(char erwartet) {
            ueberspringeLeerzeichen();
            if (!istAmEnde() && text.charAt(pos) == erwartet) {
                pos++;
                return true;
            }
            return false;
        }

        private void erwarte(char erwartet) {
            ueberspringeLeerzeichen();
            if (istAmEnde() || text.charAt(pos) != erwartet) {
                throw new IllegalArgumentException("Erwartet '" + erwartet + "' an Position " + pos);
            }
            pos++;
        }

        private boolean istAmEnde() {
            return pos >= text.length();
        }
    }
}