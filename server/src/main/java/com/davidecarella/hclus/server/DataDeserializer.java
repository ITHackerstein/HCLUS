package com.davidecarella.hclus.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Il <i>deserializzatore</i> dei dati.
 */
public class DataDeserializer implements AutoCloseable {
    /**
     * Lo stream per la lettura di dati primitivi.
     */
    private final DataInputStream dataInputStream;

    /**
     * Costruisce il <i>deserializzatore</i> leggendo dati da {@code inputStream}.
     *
     * @param inputStream lo stream da cui si leggono i dati
     */
    public DataDeserializer(InputStream inputStream) {
        this.dataInputStream = new DataInputStream(inputStream);
    }

    /**
     * Legge un intero.
     *
     * @return l'intero letto dallo stream
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public int deserializeInt() throws IOException {
        return this.dataInputStream.readInt();
    }

    /**
     * Legge una stringa.
     *
     * @return la stringa letto dallo stream
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public String deserializeString() throws IOException {
        int length = this.deserializeInt();
        var stringBuilder = new StringBuilder();
        while (length-- > 0) {
            stringBuilder.append(this.dataInputStream.readChar());
        }
        return stringBuilder.toString();
    }

    @Override
    public void close() throws IOException {
        this.dataInputStream.close();
    }
}
