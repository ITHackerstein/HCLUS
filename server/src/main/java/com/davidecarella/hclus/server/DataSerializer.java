package com.davidecarella.hclus.server;

import com.davidecarella.hclus.server.clustering.Cluster;
import com.davidecarella.hclus.server.clustering.ClusterSet;
import com.davidecarella.hclus.server.clustering.Dendrogram;
import com.davidecarella.hclus.server.data.Example;

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
     * Scrive un reale ({@code double}).
     *
     * @param v il reale da scrivere
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    public void serializeDouble(double v) throws IOException {
        this.dataOutputStream.writeDouble(v);
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

    /**
     * Scrive un esempio.
     *
     * @param example l'esempio da scrivere
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    public void serializeExample(Example example) throws IOException {
        this.serializeInt(example.getSize());
        for (var value : example) {
            this.serializeDouble(value);
        }
    }

    /**
     * Scrive un cluster.
     *
     * @param cluster il cluster da scrivere
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    public void serializeCluster(Cluster cluster) throws IOException {
        this.serializeInt(cluster.getSize());
        for (var index : cluster) {
            this.serializeInt(index);
        }
    }

    /**
     * Scrive un cluster set.
     *
     * @param clusterSet il cluster set da scrivere
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    public void serializeClusterSet(ClusterSet clusterSet) throws IOException {
        this.serializeInt(clusterSet.getSize());
        for (var cluster : clusterSet) {
            this.serializeCluster(cluster);
        }
    }

    /**
     * Scrive un dendrogramma.
     *
     * @param dendrogram il dendrogramma da scrivere
     * @throws IOException in caso di errori di I/O durante la scrittura
     */
    public void serializeDendrogram(Dendrogram dendrogram) throws IOException {
        this.serializeInt(dendrogram.getDepth());
        for (var clusterSet : dendrogram) {
            this.serializeClusterSet(clusterSet);
        }
    }

    @Override
    public void close() throws IOException {
        this.dataOutputStream.close();
    }
}
