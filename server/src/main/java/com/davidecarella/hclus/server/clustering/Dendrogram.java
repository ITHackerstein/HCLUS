package com.davidecarella.hclus.server.clustering;

import com.davidecarella.hclus.server.data.Data;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Classe che rappresenta un dendrogramma, ovvero un albero che rappresenta la composizione dei
 * {@link ClusterSet cluster set}.
 */
public class Dendrogram implements Iterable<ClusterSet>, Serializable {
    /**
     * La lista di ogni livello dell'albero.
     */
    private final ClusterSet[] tree;

    /**
     * Costruttore che inizializza un dendrogramma con profondità {@code depth}, specificata come parametro.
     *
     * @param depth la profondità del dendrogramma
     */
    public Dendrogram(int depth) {
        this.tree = new ClusterSet[depth];
    }

    /**
     * Inserisce {@code clusterSet} nel livello {@code level} dell'albero, entrambi specificati come parametro.
     *
     * @param clusterSet il {@link ClusterSet cluster set} che si vuole inserire nel livello {@code level}
     * @param level il livello dell'albero dove si vuole inserire {@code clusterSet}
     */
    public void setClusterSet(ClusterSet clusterSet, int level) {
        this.tree[level] = clusterSet;
    }

    /**
     * Restituisce il {@link ClusterSet cluster set} memorizzato al livello {@code level}, specificato come parametro.
     *
     * @param level il livello del {@link ClusterSet cluster set} che si vuole ottenere
     * @return il {@link ClusterSet cluster set} al livello {@code level}
     */
    public ClusterSet getClusterSet(int level) {
        return this.tree[level];
    }

    /**
     * Restituisce la profondità del dendrogramma.
     *
     * @return la profondità del dendrogramma
     */
    public int getDepth() {
        return this.tree.length;
    }

    /**
     * <p>Restituisce una rappresentazione testuale del dendrogramma.
     *
     * <p><b>NOTA</b>: questa rappresentazione contiene gli indici degli esempi e non gli esempi veri e propri, per una
     * rappresentazione più utile e accurata vedere {@link Dendrogram#toString(Data)}
     * @return la rappresentazione testuale del dendrogramma
     */
    @Override
    public String toString() {
        var stringBuilder = new StringBuilder();

        for (int i = 0; i < this.tree.length; ++i) {
            stringBuilder.append("level");
            stringBuilder.append(i);
            stringBuilder.append(":\n");
            stringBuilder.append(this.tree[i].toString());
            stringBuilder.append('\n');
        }

        return stringBuilder.toString();
    }

    /**
     * Restituisce una rappresentazione testuale del dendrogramma usando {@code data}, specificato come parametro, per
     * ricevere i valori degli esempi.
     *
     * @param data i dati che contengono gli esempi
     * @return una rappresentazione testuale del dendrogramma
     */
    public String toString(Data data) {
        var stringBuilder = new StringBuilder();

        for (int i = 0; i < this.tree.length; ++i) {
            stringBuilder.append("level");
            stringBuilder.append(i);
            stringBuilder.append(":\n");
            stringBuilder.append(this.tree[i].toString(data));
            stringBuilder.append('\n');
        }

        return stringBuilder.toString();
    }

    /**
     * Carica un dendrogramma da un file con nome {@code fileName}, specificato come parametro.
     *
     * @param fileName il nome del file da cui si vuole caricare il dendrogramma
     * @return il dendrogramma letto dal file
     * @throws IOException in caso di errori durante l'apertura/lettura/chiusura del file
     * @throws ClassNotFoundException in caso in cui nel file non sia salvato un dendrogramma
     */
    public static Dendrogram load(String fileName) throws IOException, ClassNotFoundException {
        try (FileInputStream fileStream = new FileInputStream(fileName);
             ObjectInputStream objectStream = new ObjectInputStream(fileStream))
        {
            return (Dendrogram) objectStream.readObject();
        }
    }

    /**
     * Salva il dendrogramma sul file con percorso {@code fileName}, specificato come parametro.
     *
     * @param fileName il percorso del file dove si vuole salvare il dendrogramma
     * @throws IOException in caso di errori durante il salvataggio sul file
     */
    public void salva(String fileName) throws IOException {
        try (FileOutputStream fileStream = new FileOutputStream(fileName);
             ObjectOutputStream objectStream = new ObjectOutputStream(fileStream))
        {
            objectStream.writeObject(this);
        }
    }

    @Override
    public Iterator<ClusterSet> iterator() {
        return Arrays.stream(this.tree).iterator();
    }
}
