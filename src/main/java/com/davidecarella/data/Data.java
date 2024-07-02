package com.davidecarella.data;

import com.davidecarella.exceptions.InvalidSizeException;

/**
 * Classe che rappresenta dei dati, ovvero una lista di esempi.
 */
public class Data {
    /**
     * La lista degli esempi.
     */
    private Example[] data;
    /**
     * Il numero degli esempi.
     */
    private int numberOfExamples;

    /**
     * Costruttore che inizializza dei dati con dei valori di esempio.
     */
    public Data() {
        this.data = new Example[5];

        Example example;

        example = new Example(3);
        example.set(0, 1.0);
        example.set(1, 2.0);
        example.set(2, 0.0);
        this.data[0] = example;

        example = new Example(3);
        example.set(0, 0.0);
        example.set(1, 1.0);
        example.set(2, -1.0);
        this.data[1] = example;

        example = new Example(3);
        example.set(0, 1.0);
        example.set(1, 3.0);
        example.set(2, 5.0);
        this.data[2] = example;

        example = new Example(3);
        example.set(0, 1.0);
        example.set(1, 3.0);
        example.set(2, 4.0);
        this.data[3] = example;

        example = new Example(3);
        example.set(0, 2.0);
        example.set(1, 2.0);
        example.set(2, 0.0);
        this.data[4] = example;

        this.numberOfExamples = 5;
    }

    /**
     * Restituisce il numero di esempi contenuti.
     *
     * @return il numero di esempi contenuti
     */
    public int getNumberOfExamples() {
        return this.numberOfExamples;
    }

    /**
     * Restituisce l'esempio con indice {@code index}, specificato come parametro.
     *
     * @param index l'indice dell'esempio che si vuole ottenere
     * @return l'esempio con indice {@code index}
     */
    public Example getExample(int index) {
        return this.data[index];
    }

    /**
     * Restituisce la matrice triangolare superiore delle distanze euclidee fra gli esempi dei dati.
     *
     * @return la matrice triangolare superiore delle distanze
     */
    public double[][] distance() throws InvalidSizeException {
        double[][] distance = new double[this.numberOfExamples][this.numberOfExamples];

        for (int i = 0; i < this.numberOfExamples; i++) {
            for (int j = 0; j < this.numberOfExamples; j++) {
                distance[i][j] = j <= i ? 0 : this.data[i].distance(this.data[j]);
            }
        }

        return distance;
    }

    /**
     * Restituisce una rappresentazione testuale dei dati.
     *
     * @return la rappresentazione testuale dei dati
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < this.numberOfExamples; i++) {
            stringBuilder.append(i);
            stringBuilder.append(':');
            stringBuilder.append(this.data[i]);
            stringBuilder.append('\n');
        }

        return stringBuilder.toString();
    }
}
