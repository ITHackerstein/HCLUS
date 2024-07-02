package com.davidecarella;

/**
 * Classe che rappresenta un dendrogramma, ovvero un albero che rappresenta la composizione dei
 * {@link ClusterSet cluster set}.
 */
class Dendrogram {
    /**
     * La lista di ogni livello dell'albero.
     */
    private ClusterSet[] tree;

    /**
     * Costruttore che inizializza un dendrogramma con profondità {@code depth}, specificata come parametro.
     *
     * @param depth la profondità del dendrogramma
     */
    Dendrogram(int depth) {
        this.tree = new ClusterSet[depth];
    }

    /**
     * Inserisce {@code clusterSet} nel livello {@code level} dell'albero, entrambi specificati come parametro.
     *
     * @param clusterSet il {@link ClusterSet cluster set} che si vuole inserire nel livello {@code level}
     * @param level il livello dell'albero dove si vuole inserire {@code clusterSet}
     */
    void setClusterSet(ClusterSet clusterSet, int level) {
        this.tree[level] = clusterSet;
    }

    /**
     * Restituisce il {@link ClusterSet cluster set} memorizzato al livello {@code level}, specificato come parametro.
     *
     * @param level il livello del {@link ClusterSet cluster set} che si vuole ottenere
     * @return il {@link ClusterSet cluster set} al livello {@code level}
     */
    ClusterSet getClusterSet(int level) {
        return this.tree[level];
    }

    /**
     * Restituisce la profondità del dendrogramma.
     *
     * @return la profondità del dendrogramma
     */
    int getDepth() {
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
    String toString(Data data) {
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
}
