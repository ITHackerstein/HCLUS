package com.davidecarella.hclus.server.clustering;

import com.davidecarella.hclus.server.data.Data;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>Classe che rappresenta un cluster, ovvero un insieme di esempi.
 *
 * <p>Internamente non vengono memorizzati gli esempi veri e propri ma i loro indici in una istanza di
 * {@link Data}, per questo motivo quando viene utilizzata la parola "esempio" ci si riferisce
 * più precisamente al loro indice.
 */
public class Cluster implements Iterable<Integer>, Cloneable, Serializable {
    /**
     * L'insieme degli esempi.
     */
    private Set<Integer> exampleIndices = new TreeSet<>();

    /**
     * Costruttore di default che crea un cluster vuoto.
     */
    public Cluster() {}

    /**
     * Aggiunge un nuovo indice esempio con indice {@code exampleIndex}, specificato come parametro, all'insieme.
     *
     * @param exampleIndex l'esempio da inserire all'insieme
     */
    public void addData(int exampleIndex) {
        this.exampleIndices.add(exampleIndex);
    }

    /**
     * Restituisce la dimensione del cluster.
     *
     * @return la dimensione del cluster.
     */
    public int getSize() {
        return this.exampleIndices.size();
    }

    /**
     * Crea una copia del cluster.
     *
     * @return una copia del cluster
     */
    @SuppressWarnings("unchecked")
    @Override
    public Cluster clone() {
        try {
            var copy = (Cluster) super.clone();
            copy.exampleIndices = (Set<Integer>) ((TreeSet<Integer>) this.exampleIndices).clone();
            return copy;
        } catch (CloneNotSupportedException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Restituisce l'unione del cluster con un altro, {@code other}, specificato come parametro.
     *
     * @param other l'altro esempio con cui si vuole fare l'unione
     * @return un cluster che contiene l'insieme unione del cluster e {@code other}
     */
    public Cluster mergeCluster(Cluster other) {
        var merged = this.clone();
        merged.exampleIndices.addAll(other.exampleIndices);
        return merged;
    }

    /**
     * <p>Restituisce una rappresentazione testuale del cluster.
     *
     * <p><b>NOTA</b>: questa rappresentazione contiene gli indici degli esempi e non gli esempi veri e propri, per una
     * rappresentazione più utile e accurata vedere {@link Cluster#toString(Data)}
     * @return la rappresentazione testuale del cluster
     */
    @Override
    public String toString() {
        if (this.exampleIndices.isEmpty())
            return "";

        var stringBuilder = new StringBuilder();

        var iterator = this.iterator();
        stringBuilder.append(iterator.next());
        while (iterator.hasNext()) {
            stringBuilder.append(", ").append(iterator.next());
        }

        return stringBuilder.toString();
    }

    /**
     * Restituisce una rappresentazione testuale del cluster usando {@code data}, specificato come parametro, per
     * ricevere i valori degli esempi.
     *
     * @param data i dati che contengono gli esempi
     * @return una rappresentazione testuale del cluster
     */
    public String toString(Data data) {
        var stringBuilder = new StringBuilder();

        for (var exampleIndex : this) {
            stringBuilder.append('<');
            stringBuilder.append(data.getExample(exampleIndex));
            stringBuilder.append('>');
        }

        return stringBuilder.toString();
    }

    @Override
    public Iterator<Integer> iterator() {
        return this.exampleIndices.iterator();
    }
}
