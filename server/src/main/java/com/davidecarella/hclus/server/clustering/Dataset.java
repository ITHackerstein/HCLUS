package com.davidecarella.hclus.server.clustering;

import com.davidecarella.hclus.common.Example;
import com.davidecarella.hclus.common.exceptions.ExampleSizeMismatchException;
import com.davidecarella.hclus.server.database.DatabaseService;
import com.davidecarella.hclus.server.exceptions.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Un dataset, ovvero una lista di esempi.
 */
public class Dataset {
    /**
     * La lista degli esempi.
     */
    private final List<Example> data = new ArrayList<>();

    /**
     * Costruisce il dataset usando le righe di una tabella (con nome {@code tableName} specificato come parametro) dal
     * database.
     *
     * @param tableName la tabella le cui righe si vogliono usare come esempi
     * @throws DatabaseConnectionException in caso di errori durante la connessione al database
     * @throws SQLException in caso di errori durante l'esecuzione delle query sul database
     * @throws EmptySetException in caso la tabella sia vuota
     * @throws MissingNumberException in caso la tabella contenga attributi non numerici
     */
    public Dataset(String tableName) throws DatabaseConnectionException, SQLException, EmptySetException, MissingNumberException {
        this.data.addAll(DatabaseService.getExamples(tableName));
    }

    /**
     * Restituisce il numero di esempi contenuti.
     *
     * @return il numero di esempi contenuti
     */
    public int getExampleCount() {
        return this.data.size();
    }

    /**
     * Restituisce l'esempio con indice {@code index}, specificato come parametro.
     *
     * @param index l'indice dell'esempio che si vuole ottenere
     * @return l'esempio con indice {@code index}
     */
    public Example getExample(int index) {
        return this.data.get(index);
    }

    /**
     * Calcola la matrice delle distanze tra coppie di esempi. Ogni elemento in posizione \((i, j)\) contiene
     * la distanza tra gli esempi in posizione \(i\) e \(j\).
     *
     * @return la matrice delle distanze
     * @throws ExampleSizeMismatchException in caso in cui gli esempi dovessero avere dimensione diversa
     */
    public double[][] computeDistanceMatrix() throws ExampleSizeMismatchException {
        var distances = new double[this.data.size()][this.data.size()];

        for (int i = 0; i < this.data.size(); i++) {
            distances[i][i] = 0.0;
            for (int j = i + 1; j < this.data.size(); j++) {
                distances[i][j] = distances[j][i] = this.data.get(i).distance(this.data.get(j));
            }
        }

        return distances;
    }

    /**
     * Restituisce una rappresentazione testuale dei dati.
     *
     * @return la rappresentazione testuale dei dati
     */
    @Override
    public String toString() {
        var stringBuilder = new StringBuilder();

        int index = 0;
        for (var example : this.data) {
            stringBuilder.append(index);
            stringBuilder.append(':');
            stringBuilder.append(example);
            stringBuilder.append('\n');
            ++index;
        }

        return stringBuilder.toString();
    }
}
