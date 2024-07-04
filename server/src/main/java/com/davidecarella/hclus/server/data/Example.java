package com.davidecarella.hclus.server.data;

import com.davidecarella.hclus.server.exceptions.InvalidSizeException;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

/**
 * Classe che rappresenta un esempio, ovvero una lista di numeri reali.
 */
public class Example implements Iterable<Double> {
    /**
     * La lista di valori memorizzati nell'esempio.
     */
    private List<Double> example;

    /**
     * Costruttore che inizializza un esempio vuoto.
     */
    public Example() {
        this.example = new LinkedList<>();
    }

    /**
     * Aggiunge il valore {@code value}, specificato come parametro.
     *
     * @param value il valore che si vuole aggiungere
     */
    public void add(double value) {
        this.example.add(value);
    }

    /**
     * Restituisce il valore in posizione {@code index}, specificato come parametro.
     *
     * @param index l'indice del valore che si vuole ottenere
     * @return il valore in posizione {@code index}
     */
    public double get(int index) {
        return this.example.get(index);
    }

    /**
     * Calcola la distanza euclidea tra l'esempio e un altro, {@code other}, specificato come parametro.
     *
     * @param other l'altro esempio con il quale si vuole calcolare la distanza
     * @return la distanza euclidea tra l'esempio e {@code other}
     * @throws InvalidSizeException quando questo esempio e {@code other} hanno lunghezza diversa
     */
    public double distance(Example other) throws InvalidSizeException {
        if (this.example.size() != other.example.size()) {
            throw new InvalidSizeException("Si può calcolare la distanza solo fra esempi con stessa lunghezza");
        }

        double result = 0.0;
        var firstValueIterator = this.iterator();
        var secondValueIterator = other.iterator();

        while (firstValueIterator.hasNext() && secondValueIterator.hasNext()) {
            double delta = firstValueIterator.next() - secondValueIterator.next();
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
