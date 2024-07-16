package com.davidecarella.hclus.common.serialization;

import com.davidecarella.hclus.common.ClusterDistanceMethod;
import com.davidecarella.hclus.common.Clustering;
import com.davidecarella.hclus.common.ClusteringStep;
import com.davidecarella.hclus.common.Example;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Il <i>serializer</i> dei dati implementati secondo il protocollo.
 */
public class DataSerializer implements AutoCloseable {
    /**
     * Lo stream di output dei dati.
     */
    private final DataOutputStream dataOutputStream;

    /**
     * Costruisce il <i>serializer</i> utilizzando {@code outputStream}, specificata come parametro, come stream di
     * output.
     *
     * @param outputStream lo stream di input
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
     * Scrive un reale.
     *
     * @param v il reale da scrivere
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    public void serializeDouble(double v) throws IOException {
        this.dataOutputStream.writeDouble(v);
    }

    /**
     * Scrive un carattere.
     *
     * @param c il carattere da scrivere
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    public void serializeChar(char c) throws IOException {
        this.dataOutputStream.writeChar(c);
    }

    /**
     * Scrive una stringa.
     *
     * @param v la stringa scrivere
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    public void serializeString(String v) throws IOException {
        this.dataOutputStream.writeInt(v.length());
        this.dataOutputStream.writeChars(v);
    }

    /**
     * Scrive un esempio.
     *
     * @param example l'esempio da scrivere
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    public void serializeExample(Example example) throws IOException {
        this.dataOutputStream.writeInt(example.getSize());
        for (var value : example) {
            this.dataOutputStream.writeDouble(value);
        }
    }

    /**
     * Scrive un metodo per il calcolo della distanza fra cluster.
     *
     * @param clusterDistanceMethod il metodo per il calcolo della distanza fra cluster da scrivere
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    public void serializeClusterDistanceMethod(ClusterDistanceMethod clusterDistanceMethod) throws IOException {
        this.dataOutputStream.writeInt(clusterDistanceMethod.id());
        this.serializeString(clusterDistanceMethod.displayName());
    }

    /**
     * Scrive un passo di un clustering.
     *
     * @param clusteringStep il passo di un clustering da scrivere
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    public void serializeClusteringStep(ClusteringStep clusteringStep) throws IOException {
        this.dataOutputStream.writeInt(clusteringStep.firstClusterIndex());
        this.dataOutputStream.writeInt(clusteringStep.secondClusterIndex());
        this.dataOutputStream.writeInt(clusteringStep.newClusterSize());
    }

    /**
     * Scrive un clustering.
     *
     * @param clustering il clustering da scrivere
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    public void serializeClustering(Clustering clustering) throws IOException {
        this.dataOutputStream.writeInt(clustering.exampleCount());
        this.dataOutputStream.writeInt(clustering.steps().length + 1);
        for (var step : clustering.steps()) {
            this.serializeClusteringStep(step);
        }
    }

    /**
     * Chiude il <i>serializer</i>.
     *
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    @Override
    public void close() throws IOException {
        this.dataOutputStream.close();
    }
}
