package de.hsrm.mi.swt.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import de.hsrm.mi.swt.model.AnlagenPlan;
import persistence.format.JsonSpeicherFormat;
import persistence.format.Speicherformat;

/**
 * Controller zum Speichern und Laden von Anlagenplaenen.
 */
public class SpeicherController {

    private final Speicherformat speicherformat;

    /**
     * Erstellt einen Speichercontroller mit JSON-Format.
     */
    public SpeicherController() {
        this(new JsonSpeicherFormat());
    }

    /**
     * Erstellt einen Speichercontroller mit austauschbarem Speicherformat.
     *
     * @param speicherformat verwendetes Format
     */
    public SpeicherController(Speicherformat speicherformat) {
        this.speicherformat = speicherformat;
    }

    /**
     * Speichert einen Anlagenplan in eine Datei.
     *
     * @param anlagenPlan zu speichernder Plan
     * @param pfad Zieldatei
     * @throws IOException wenn die Datei nicht geschrieben werden kann
     */
    public void speichere(AnlagenPlan anlagenPlan, Path pfad) throws IOException {
        Files.writeString(pfad, speicherformat.speichere(anlagenPlan));
    }

    /**
     * Laedt einen Anlagenplan aus einer Datei.
     *
     * @param pfad Quelldatei
     * @return geladener Plan
     * @throws IOException wenn die Datei nicht gelesen werden kann
     */
    public AnlagenPlan lade(Path pfad) throws IOException {
        return speicherformat.lade(Files.readString(pfad));
    }
}
