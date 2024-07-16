package com.davidecarella.hclus.common;

import com.davidecarella.hclus.common.exceptions.ExampleSizeMismatchException;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;

/**
 * Un esempio, ovvero una lista di numeri reali.
 */
public class Example implements Iterable<Double> {
    /**
     * La lista di valori memorizzati nell'esempio.
     */
    private final List<Double> example;

    /**
     * Costruttore che inizializza un esempio vuoto.
     */
    public Example() {
        this.example = new LinkedList<>();
    }

    /**
     * Restituisce la dimensione dell'esempio.
     *
     * @return la dimensione dell'esempio
     */
    public int getSize() { return this.example.size(); }

    /**
     * Aggiunge il valore {@code value}, specificato come parametro.
     *
     * @param value il valore che si vuole aggiungere
     */
    public void add(double value) {
        this.example.add(value);
    }

    /**
     * Calcola la distanza euclidea tra l'esempio e un altro, {@code other}, specificato come parametro.
     *
     * @param other l'altro esempio con il quale si vuole calcolare la distanza
     * @return la distanza euclidea tra l'esempio e {@code other}
     * @throws ExampleSizeMismatchException in caso in cui gli esempi dovessero avere dimensione diversa
     */
    public double distance(Example other) throws ExampleSizeMismatchException {
        if (this.getSize() != other.getSize()) {
            throw new ExampleSizeMismatchException("Gli esempi hanno dimensione diversa!");
        }

        double dist = 0.0;

        var thisIterator = this.iterator();
        var otherIterator = other.iterator();
        while (thisIterator.hasNext() && otherIterator.hasNext()) {
            var delta = thisIterator.next() - otherIterator.next();
            dist += delta * delta;
        }

        return Math.sqrt(dist);
    }

    /**
     * Restituisce una rappresentazione testuale dell'esempio.
     *
     * @return la rappresentazione testuale dell'esempio
     */
    @Override
    public String toString() {
        if (this.example.isEmpty()) {
            return "[]";
        }

        var stringBuilder = new StringBuilder();

        stringBuilder.append('[');
        var iterator = this.iterator();
        stringBuilder.append(iterator.next());
        while (iterator.hasNext()) {
            stringBuilder.append(',').append(iterator.next());
        }
        stringBuilder.append(']');

        return stringBuilder.toString();
    }

    /**
     * Restituisce un {@link java.util.Iterator iteratore} per la lista dei valori memorizzati nell'esempio.
     *
     * @return l'iteratore per la lista di valori
     */
    @Override
    public Iterator<Double> iterator() {
        return this.example.iterator();
    }
}
