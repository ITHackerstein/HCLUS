package com.davidecarella;

/**
 * Classe che si occupa di effettuare l'operazione di "mining", ovvero di creare il dendrogramma a partire dai dati
 * forniti
 */
public class HierarchicalClusterMiner {
    /**
     * Il dendogramma.
     */
    private Dendrogram dendrogram;

    /**
     * Costruttore che inizializza il miner con un dendrogramma di profondità {@code depth}, specificata come parametro.
     *
     * @param depth la profondità del dendrogramma
     */
    HierarchicalClusterMiner(int depth) {
        this.dendrogram = new Dendrogram(depth);
    }

    /**
     * Crea il dendrogramma utilizzando i dati forniti da {@code data} e l'oggetto per calcolare la distanza
     * {@code distanceCalculator}, entrambi forniti come parametro.
     *
     * @param data i dati
     * @param distanceCalculator l'oggetto per calcolare la distanza
     */
    void mine(Data data, ClusterDistance distanceCalculator) {
        var firstLevel = new ClusterSet(data.getNumberOfExamples());
        for (int i = 0; i < data.getNumberOfExamples(); i++) {
            var cluster = new Cluster();
            cluster.addData(i);
            firstLevel.add(cluster);
        }

        this.dendrogram.setClusterSet(firstLevel, 0);

        for (int level = 1; level < data.getNumberOfExamples(); ++level) {
            var newLevel = this.dendrogram.getClusterSet(level - 1).mergeClosestClusters(distanceCalculator, data);
            this.dendrogram.setClusterSet(newLevel, level);
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
    String toString(Data data) {
        return this.dendrogram.toString(data);
    }
}
