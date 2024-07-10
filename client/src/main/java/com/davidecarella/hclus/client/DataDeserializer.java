package com.davidecarella.hclus.client;

import com.davidecarella.hclus.client.data.Cluster;
import com.davidecarella.hclus.client.data.ClusterSet;
import com.davidecarella.hclus.client.data.Dendrogram;
import com.davidecarella.hclus.client.data.Example;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.TreeSet;

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
     * Legge un numero reale ({@code double}).
     *
     * @return il {@code double} letto dallo stream
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public double deserializeDouble() throws IOException {
        return this.dataInputStream.readDouble();
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

    /**
     * Legge un cluster.
     *
     * @return il cluster letto dallo stream
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public Cluster deserializeCluster() throws IOException {
        var size = this.deserializeInt();
        var indices = new TreeSet<Integer>();
        while (size-- > 0) {
            indices.add(this.deserializeInt());
        }
        return new Cluster(indices);
    }

    /**
     * Legge un cluster set.
     *
     * @return il cluster set letto dallo stream
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public ClusterSet deserializeClusterSet() throws IOException {
        var size = this.deserializeInt();
        var clusters = new ArrayList<Cluster>();
        while (size-- > 0) {
            clusters.add(this.deserializeCluster());
        }
        return new ClusterSet(clusters);
    }

    /**
     * Legge un dendrogramma.
     *
     * @return il dendrogramma letto dallo stream
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public Dendrogram deserializeDendrogram() throws IOException {
        var depth = this.deserializeInt();
        var tree = new ArrayList<ClusterSet>();
        while (depth-- > 0) {
            tree.add(this.deserializeClusterSet());
        }
        return new Dendrogram(tree);
    }

    /**
     * Legge un esempio dallo stream.
     *
     * @return l'esempio letto dallo stream
     * @throws IOException in caso di errori di I/O durante la lettura
     */
    public Example deserializeExample() throws IOException {
        var size = this.deserializeInt();
        var values = new ArrayList<Double>();
        while (size-- > 0) {
            values.add(this.deserializeDouble());
        }
        return new Example(values);
    }

    @Override
    public void close() throws IOException {
        this.dataInputStream.close();
    }
}
