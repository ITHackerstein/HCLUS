package com.davidecarella;

/**
 * <p>Classe che rappresenta un cluster, ovvero un insieme di esempi.
 *
 * <p>Internamente non vengono memorizzati gli esempi veri e propri ma i loro indici in una istanza di
 * {@link com.davidecarella.Data}, per questo motivo quando viene utilizzata la parola "esempio" ci si riferisce
 * più precisamente al loro indice.
 */
class Cluster {
    /**
     * L'insieme degli esempi.
     */
    private int[] clusteredData = new int[0];

    /**
     * Costruttore di default che crea un cluster vuoto.
     */
    public Cluster() {}

    /**
     * Aggiunge un nuovo indice esempio con indice {@code exampleIndex}, specificato come parametro, all'insieme. Se
     * dovesse essere già presente nell'insieme allora non fa nulla.
     *
     * @param exampleIndex l'esempio da inserire all'insieme
     */
    void addData(int exampleIndex) {
        for (int i = 0; i < this.clusteredData.length; ++i) {
            if (exampleIndex == this.clusteredData[i])
                return;
        }

        var newClusteredData = new int[this.clusteredData.length + 1];
        System.arraycopy(this.clusteredData, 0, newClusteredData, 0, this.clusteredData.length);
        this.clusteredData = newClusteredData;
        this.clusteredData[this.clusteredData.length - 1] = exampleIndex;
    }

    /**
     * Restituisce la dimensione del cluster.
     *
     * @return la dimensione del cluster.
     */
    int getSize() {
        return this.clusteredData.length;
    }

    /**
     * Restituisce l'esempio con indice {@code index}, specificato come parametro.
     *
     * @param index l'indice dell'esempio che si vuole ottenere
     * @return l'esempio con indice {@code index}
     */
    int getElement(int index) {
        return this.clusteredData[index];
    }

    /**
     * Crea una copia del cluster.
     *
     * @return una copia del cluster
     */
    Cluster createACopy() {
        var copy = new Cluster();

        for (int i = 0; i < this.clusteredData.length; ++i) {
            copy.addData(this.clusteredData[i]);
        }

        return copy;
    }

    /**
     * Restituisce l'unione del cluster con un altro, {@code other}, specificato come parametro.
     *
     * @param other l'altro esempio con cui si vuole fare l'unione
     * @return un cluster che contiene l'insieme unione del cluster e {@code other}
     */
    Cluster mergeCluster(Cluster other) {
        var merged = new Cluster();

        for (int i = 0; i < this.clusteredData.length; ++i) {
            merged.addData(this.clusteredData[i]);
        }

        for (int i = 0; i < other.clusteredData.length; ++i) {
            merged.addData(other.clusteredData[i]);
        }

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
        var stringBuilder = new StringBuilder();

        for (int i = 0; i < this.clusteredData.length; ++i) {
            stringBuilder.append(this.clusteredData[i]);
            if (i != this.clusteredData.length - 1) {
                stringBuilder.append(',');
            }
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
    String toString(Data data) {
        var stringBuilder = new StringBuilder();

        for (int i = 0; i < this.clusteredData.length; ++i) {
            stringBuilder.append('<');
            stringBuilder.append(data.getExample(this.clusteredData[i]));
            stringBuilder.append('>');
        }

        return stringBuilder.toString();
    }
}
