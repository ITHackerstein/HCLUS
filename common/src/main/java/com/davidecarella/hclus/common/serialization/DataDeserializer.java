package com.davidecarella.hclus.common.serialization;

import com.davidecarella.hclus.common.ClusterDistanceMethod;
import com.davidecarella.hclus.common.Clustering;
import com.davidecarella.hclus.common.ClusteringStep;
import com.davidecarella.hclus.common.Example;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Il <i>deserializer</i> dei dati implementato secondo il protocollo.
 */
public class DataDeserializer implements AutoCloseable {
    /**
     * Lo stream di input dei dati.
     */
    private final DataInputStream dataInputStream;

    /**
     * Costruisce il <i>deserializer</i> utilizzando {@code inputStream}, specificata come parametro, come stream di
     * input.
     *
     * @param inputStream lo stream di input
     */
    public DataDeserializer(InputStream inputStream) {
        this.dataInputStream = new DataInputStream(inputStream);
    }

    /**
     * Legge un intero.
     *
     * @return l'intero
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public int deserializeInt() throws IOException {
        return this.dataInputStream.readInt();
    }

    /**
     * Legge un reale.
     *
     * @return il reale
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public double deserializeDouble() throws IOException {
        return this.dataInputStream.readDouble();
    }

    /**
     * Legge un carattere.
     *
     * @return il carattere
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public char deserializeChar() throws IOException {
        return this.dataInputStream.readChar();
    }

    /**
     * Legge una stringa.
     *
     * @return la stringa
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public String deserializeString() throws IOException {
        var size = this.dataInputStream.readInt();
        var stringBuilder = new StringBuilder();
        while (size-- > 0) {
            stringBuilder.append(this.dataInputStream.readChar());
        }
        return stringBuilder.toString();
    }

    /**
     * Legge un esempio.
     *
     * @return l'esempio
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public Example deserializeExample() throws IOException {
        var size = this.dataInputStream.readInt();
        var example = new Example();
        while (size-- > 0) {
            example.add(this.dataInputStream.readDouble());
        }
        return example;
    }

    /**
     * Legge un metodo per il calcolo della distanza fra cluster.
     *
     * @return il metodo per il calcolo della distanza fra cluster
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public ClusterDistanceMethod deserializeClusterDistanceMethod() throws IOException {
        return new ClusterDistanceMethod(
            this.dataInputStream.readInt(),
            this.deserializeString()
        );
    }

    /**
     * Legge un passo di un clustering.
     *
     * @return un passo di un clustering
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public ClusteringStep deserializeClusteringStep() throws IOException {
        return new ClusteringStep(
            this.dataInputStream.readInt(),
            this.dataInputStream.readInt(),
            this.dataInputStream.readInt()
        );
    }

    /**
     * Legge un clustering.
     *
     * @return il clustering
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public Clustering deserializeClustering() throws IOException {
        var exampleCount = this.dataInputStream.readInt();
        var depth = this.dataInputStream.readInt();
        var steps = new ClusteringStep[depth - 1];
        for (int i = 0; i < depth - 1; ++i) {
            steps[i] = this.deserializeClusteringStep();
        }
        return new Clustering(exampleCount, steps);
    }

    /**
     * Chiude il <i>deserializer</i>.
     *
     * @throws IOException in caso di errori di I/O durante la chiusura
     */
    @Override
    public void close() throws IOException {
        this.dataInputStream.close();
    }
}
