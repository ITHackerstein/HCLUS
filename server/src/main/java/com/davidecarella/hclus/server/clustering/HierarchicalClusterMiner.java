package com.davidecarella.hclus.server.clustering;

import com.davidecarella.hclus.server.data.Data;
import com.davidecarella.hclus.server.distance.ClusterDistance;
import com.davidecarella.hclus.server.exceptions.ClusterSetTooSmallException;
import com.davidecarella.hclus.server.exceptions.InvalidDepthException;
import com.davidecarella.hclus.server.exceptions.InvalidSizeException;

import java.io.*;

/**
 * Classe che si occupa di effettuare l'operazione di "mining", ovvero di creare il dendrogramma a partire dai dati
 * forniti
 */
public class HierarchicalClusterMiner implements Serializable {
    /**
     * Il dendogramma.
     */
    private Dendrogram dendrogram;

    /**
     * Costruttore che inizializza il miner con un dendrogramma di profondità {@code depth}, specificata come parametro.
     *
     * @param depth la profondità del dendrogramma
     */
    public HierarchicalClusterMiner(int depth) {
        this.dendrogram = new Dendrogram(depth);
    }

    /**
     * Restituisce la profondità del dendrogramma memorizzato.
     *
     * @return la profondità del dendrogramma memorizzato
     */
    public int getDendrogramDepth() { return this.dendrogram.getDepth(); }

    /**
     * Crea il dendrogramma utilizzando i dati forniti da {@code data} e l'oggetto per calcolare la distanza
     * {@code distanceCalculator}, entrambi forniti come parametro.
     *
     * @param data i dati
     * @param distanceCalculator l'oggetto per calcolare la distanza
     * @throws InvalidDepthException quando la profondità del dendrogramma supera il numero di esempi in {@code data}
     * @throws InvalidSizeException quando ci sono due esempi con lunghezze diverse
     */
    public void mine(Data data, ClusterDistance distanceCalculator) throws InvalidDepthException, InvalidSizeException {
        if (this.dendrogram.getDepth() <= 0 || this.dendrogram.getDepth() > data.getNumberOfExamples()) {
            throw new InvalidDepthException("La profondità del dendrogramma deve essere al massimo pari al numero di esempi nel dataset");
        }

        var firstLevel = new ClusterSet(data.getNumberOfExamples());
        for (int i = 0; i < data.getNumberOfExamples(); i++) {
            var cluster = new Cluster();
            cluster.addData(i);
            firstLevel.add(cluster);
        }

        this.dendrogram.setClusterSet(firstLevel, 0);

        for (int level = 1; level < this.dendrogram.getDepth(); ++level) {
            ClusterSet newLevel = null;
            try {
                newLevel = this.dendrogram.getClusterSet(level - 1).mergeClosestClusters(distanceCalculator, data);
            } catch (ClusterSetTooSmallException ignored) {
            }

            assert newLevel != null;
            this.dendrogram.setClusterSet(newLevel, level);
        }
    }

    /**
     * Carica un miner da un file con nome {@code fileName}, specificato come parametro.
     *
     * @param fileName il nome del file da cui si vuole caricare il miner
     * @return l'istanza del miner letta dal file
     * @throws IOException in caso di errori durante l'apertura/lettura/chiusura del file
     * @throws ClassNotFoundException in caso in cui nel file non sia salvata un'istanza del miner
     */
    public static HierarchicalClusterMiner load(String fileName) throws IOException, ClassNotFoundException {
        try (FileInputStream fileStream = new FileInputStream(fileName);
             ObjectInputStream objectStream = new ObjectInputStream(fileStream))
        {
            return (HierarchicalClusterMiner) objectStream.readObject();
        }
    }

    /**
     * Salva il miner sul file con percorso {@code fileName}, specificato come parametro.
     *
     * @param fileName il percorso del file dove si vuole salvare il miner
     * @throws IOException in caso di errori durante il salvataggio sul file
     */
    public void salva(String fileName) throws IOException {
        try (FileOutputStream fileStream = new FileOutputStream(fileName);
             ObjectOutputStream objectStream = new ObjectOutputStream(fileStream))
        {
            objectStream.writeObject(this);
        }
    }

    /**
     * <p>Restituisce una rappresentazione testuale del miner.
     *
     * <p><b>NOTA</b>: questa rappresentazione contiene gli indici degli esempi e non gli esempi veri e propri, per una
     * rappresentazione più utile e accurata vedere {@link Dendrogram#toString(Data)}
     * @return la rappresentazione testuale del miner
     */
    @Override
    public String toString() {
        return this.dendrogram.toString();
    }

    /**
     * Restituisce una rappresentazione testuale del miner usando {@code data}, specificato come parametro, per
     * ricevere i valori degli esempi.
     *
     * @param data i dati che contengono gli esempi
     * @return una rappresentazione testuale del miner
     */
    public String toString(Data data) {
        return this.dendrogram.toString(data);
    }
}
