package com.davidecarella.hclus.server.clustering;

import com.davidecarella.hclus.server.data.Data;
import com.davidecarella.hclus.server.distance.ClusterDistance;
import com.davidecarella.hclus.server.exceptions.ClusterSetFullException;
import com.davidecarella.hclus.server.exceptions.ClusterSetTooSmallException;
import com.davidecarella.hclus.server.exceptions.InvalidSizeException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Classe che rappresenta un insieme di {@link Cluster cluster}.
 */
public class ClusterSet implements Iterable<Cluster>, Serializable {
    /**
     * L'insieme dei cluster.
     */
    private final Cluster[] clusters;

    /**
     * L'indice successivo all'ultimo {@link Cluster cluster} memorizzato.
     */
    private int lastClusterIndex = 0;

    /**
     * Costruttore che inizializza un insieme di {@link Cluster cluster} con dimensione massima {@code maxSize},
     * specificata come parametro.
     *
     * @param maxSize la dimensione massima dell'insieme
     */
    public ClusterSet(int maxSize) {
        this.clusters = new Cluster[maxSize];
    }

    /**
     * Restituisce la dimensione del cluster set.
     *
     * @return la dimensione del cluster set
     */
    public int getSize() {
        return this.lastClusterIndex;
    }

    /**
     * Aggiunge il {@code cluster} specificato come parametro all'insieme.
     *
     * @param cluster il {@link Cluster cluster} da aggiungere all'insieme
     */
    public void add(Cluster cluster) {
        if (this.lastClusterIndex >= this.clusters.length) {
            throw new ClusterSetFullException("Il cluster set ha raggiunto la capienza massima");
        }

        for (int i = 0; i < this.lastClusterIndex; ++i) {
            if (cluster == this.clusters[i])
                return;
        }

        this.clusters[this.lastClusterIndex++] = cluster;
    }

    /**
     * Restituisce un nuovo cluster set che contiene gli stessi cluster fatta eccezione per i due
     * {@link Cluster cluster} tra loro più vicini che verranno uniti in un unico {@link Cluster cluster}.
     *
     * @see Cluster#mergeCluster(Cluster)
     *
     * @param distanceCalculator l'oggetto per il calcolo della distanza tra due {@link Cluster cluster}
     * @param data i dati
     * @return un nuovo cluster set in cui vengono uniti i due {@link Cluster cluster} più vicini fra loro
     * @throws ClusterSetTooSmallException quando il cluster set ha meno di due {@link Cluster cluster}
     * @throws InvalidSizeException quando ci sono due esempi con lunghezze diverse
     */
    public ClusterSet mergeClosestClusters(ClusterDistance distanceCalculator, Data data) throws ClusterSetTooSmallException, InvalidSizeException {
        if (this.lastClusterIndex < 2) {
            throw new ClusterSetTooSmallException("Ci devono essere almeno due cluster per poter effettuare l'unione");
        }

        Cluster firstCluster = null;
        Cluster secondCluster = null;
        var minDistance = Double.MAX_VALUE;

        for (int i = 0; i < this.lastClusterIndex; ++i) {
            for (int j = i + 1; j < this.lastClusterIndex; ++j) {
                var distance = distanceCalculator.distance(this.clusters[i], this.clusters[j], data);
                if (distance < minDistance) {
                    minDistance = distance;
                    firstCluster = this.clusters[i];
                    secondCluster = this.clusters[j];
                }
            }
        }

        assert firstCluster != null && secondCluster != null;

        var merged = firstCluster.mergeCluster(secondCluster);
        var newClusterSet = new ClusterSet(this.clusters.length - 1);
        for (int i = 0; i < this.lastClusterIndex; ++i) {
            if (this.clusters[i] != firstCluster) {
                if (this.clusters[i] != secondCluster) {
                    newClusterSet.add(this.clusters[i]);
                }
            } else {
                newClusterSet.add(merged);
            }
        }

        return newClusterSet;
    }

    /**
     * <p>Restituisce una rappresentazione testuale del cluster set.
     *
     * <p><b>NOTA</b>: questa rappresentazione contiene gli indici degli esempi e non gli esempi veri e propri, per una
     * rappresentazione più utile e accurata vedere {@link ClusterSet#toString(Data)}
     * @return la rappresentazione testuale del cluster set
     */
    @Override
    public String toString() {
        var stringBuilder = new StringBuilder();

        for (int i = 0; i < this.lastClusterIndex; ++i) {
            stringBuilder.append("cluster");
            stringBuilder.append(i);
            stringBuilder.append(':');
            stringBuilder.append(this.clusters[i].toString());
            stringBuilder.append('\n');
        }

        return stringBuilder.toString();
    }

    /**
     * Restituisce una rappresentazione testuale del cluster set usando {@code data}, specificato come parametro, per
     * ricevere i valori degli esempi.
     *
     * @param data i dati che contengono gli esempi
     * @return una rappresentazione testuale del cluster set
     */
    public String toString(Data data) {
        var stringBuilder = new StringBuilder();

        for (int i = 0; i < this.lastClusterIndex; ++i) {
            stringBuilder.append("cluster");
            stringBuilder.append(i);
            stringBuilder.append(':');
            stringBuilder.append(this.clusters[i].toString(data));
            stringBuilder.append('\n');
        }

        return stringBuilder.toString();
    }

    @Override
    public Iterator<Cluster> iterator() {
        return Arrays.stream(this.clusters).limit(this.lastClusterIndex).iterator();
    }
}
