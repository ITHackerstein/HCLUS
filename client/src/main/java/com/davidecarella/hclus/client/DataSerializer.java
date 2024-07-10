package com.davidecarella.hclus.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Il <i>serializzatore</i> dei dati inviati dal client.
 */
public class DataSerializer implements AutoCloseable {
    /**
     * Lo stream per la scrittura di dati primitivi.
     */
    private final DataOutputStream dataOutputStream;

    /**
     * Costruisce il <i>serializzatore</i> scrivendo dati su {@code outputStream}.
     *
     * @param outputStream lo stream dove si scrivono i dati
     */
    public DataSerializer(OutputStream outputStream) {
        this.dataOutputStream = new DataOutputStream(outputStream);
    }

    /**
     * Scrive un intero.
     *
     * @param v l'intero da scrivere
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    public void serializeInt(int v) throws IOException {
        this.dataOutputStream.writeInt(v);
    }

    /**
     * Scrive una stringa.
     *
     * @param s la stringa da scrivere
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    public void serializeString(String s) throws IOException {
        this.dataOutputStream.writeInt(s.length());
        this.dataOutputStream.writeChars(s);
    }

    @Override
    public void close() throws IOException {
        this.dataOutputStream.close();
    }
}
