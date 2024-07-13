package com.davidecarella.hclus.server.clustering;

import com.davidecarella.hclus.common.Example;
import com.davidecarella.hclus.common.exceptions.ExampleSizeMismatchException;
import com.davidecarella.hclus.server.database.DatabaseService;
import com.davidecarella.hclus.server.exceptions.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe che rappresenta dei dati, ovvero una lista di esempi.
 */
public class Dataset {
    /**
     * La lista degli esempi.
     */
    private final List<Example> data = new ArrayList<>();

    /**
     * Costruisce i dati usando le righe di una tabella (con nome {@code tableName} specificato come parametro) dal
     * database.
     *
     * @param tableName la tabella le cui righe si vogliono usare come esempi
     * @throws NoDataException se ci dovesse essere un error durante il caricamento della tabella
     */
    public Dataset(String tableName) throws NoDataException {
        try {
            this.data.addAll(DatabaseService.getExamples(tableName));
        } catch (DatabaseConnectionException | SQLException | EmptySetException | MissingNumberException exception) {
            throw new NoDataException("Errore durante il caricamento degli esempi!", exception);
        }
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
     * <p>Calcola la matrice delle distanze tra coppie di esempi.
     *
     * <p>Ogni elemento in posizione \((i, j)\) contiene la distanza tra gli esempi in posizione \(i\) e \(j\).
     *
     * @return la matrice delle distanze
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
