package com.davidecarella.data;

import com.davidecarella.exceptions.InvalidSizeException;

/**
 * Classe che rappresenta un esempio, ovvero un vettore di numeri reali.
 */
public class Example {
    /**
     * I valori memorizzati nell'esempio.
     */
    private double[] example;

    /**
     * Costruttore che inizializza un esempio di lunghezza {@code length} specificata come parametro.
     *
     * @param length lunghezza dell'esempio
     */
    public Example(int length) {
        this.example = new double[length];
    }

    /**
     * Cambia il valore in indice {@code index} con il valore {@code value}, entrambi specificati come parametro.
     *
     * @param index l'indice del valore che si vuole impostare
     * @param value il nuovo valore che si vuole impostare
     */
    public void set(int index, double value) {
        this.example[index] = value;
    }

    /**
     * Restituisce il valore in posizione {@code index}, specificato come parametro.
     *
     * @param index l'indice del valore che si vuole ottenere
     * @return il valore in posizione {@code index}
     */
    public double get(int index) {
        return this.example[index];
    }

    /**
     * Calcola la distanza euclidea tra l'esempio e un altro, {@code other}, specificato come parametro.
     *
     * @param other l'altro esempio con il quale si vuole calcolare la distanza
     * @return la distanza euclidea tra l'esempio e {@code other}
     * @throws InvalidSizeException quando questo esempio e {@code other} hanno lunghezza diversa
     */
    public double distance(Example other) throws InvalidSizeException {
        if (this.example.length != other.example.length) {
            throw new InvalidSizeException("Si può calcolare la distanza solo fra esempi con stessa lunghezza");
        }

        double result = 0.0;

        for (int i = 0; i < this.example.length; ++i) {
            double delta = this.example[i] - other.example[i];
            result += delta * delta;
        }

        return result;
    }

    /**
     * Restituisce una rappresentazione testuale dell'esempio.
     *
     * @return la rappresentazione testuale dell'esempio
     */
    @Override
    public String toString() {
        var stringBuilder = new StringBuilder();

        stringBuilder.append('[');
        for (int i = 0; i < this.example.length; ++i) {
            stringBuilder.append(this.example[i]);
            if (i != this.example.length - 1) {
                stringBuilder.append(',');
            }
        }
        stringBuilder.append(']');

        return stringBuilder.toString();
    }
}
