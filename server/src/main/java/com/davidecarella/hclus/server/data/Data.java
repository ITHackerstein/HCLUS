package com.davidecarella.hclus.server.data;

import com.davidecarella.hclus.server.database.DbAccess;
import com.davidecarella.hclus.server.database.TableData;
import com.davidecarella.hclus.server.exceptions.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe che rappresenta dei dati, ovvero una lista di esempi.
 */
public class Data {
    /**
     * La lista degli esempi.
     */
    private List<Example> data = new ArrayList<>();

    /**
     * Costruttore che inizializza dei dati con dei valori di esempio.
     */
    public Data() {
        Example example;

        example = new Example();
        example.add(1.0);
        example.add(2.0);
        example.add(0.0);
        this.data.add(example);

        example = new Example();
        example.add(0.0);
        example.add(1.0);
        example.add(-1.0);
        this.data.add(example);

        example = new Example();
        example.add(1.0);
        example.add(3.0);
        example.add(5.0);
        this.data.add(example);

        example = new Example();
        example.add(1.0);
        example.add(3.0);
        example.add(4.0);
        this.data.add(example);

        example = new Example();
        example.add(2.0);
        example.add(2.0);
        example.add(0.0);
        this.data.add(example);
    }

    public Data(String tableName) throws NoDataException {
        var db = new DbAccess();
        var tableData = new TableData(db);

        try {
            this.data.addAll(tableData.getDistinctTransazioni(tableName));
        } catch (DatabaseConnectionException | SQLException | EmptySetException | MissingNumberException exception) {
            throw new NoDataException("Errore durante il caricamento degli esempi!", exception);
        }
    }

    /**
     * Restituisce il numero di esempi contenuti.
     *
     * @return il numero di esempi contenuti
     */
    public int getNumberOfExamples() {
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
     * Restituisce la matrice triangolare superiore delle distanze euclidee fra gli esempi dei dati.
     *
     * @return la matrice triangolare superiore delle distanze
     */
    public double[][] distance() throws InvalidSizeException {
        double[][] distance = new double[this.data.size()][this.data.size()];

        for (int i = 0; i < this.data.size(); ++i) {
            for (int j = 0; j < this.data.size(); ++j) {
                distance[i][j] = j <= i ? 0 : this.data.get(i).distance(this.data.get(j));
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
        var stringBuilder = new StringBuilder();

        int index = 0;
        var iterator = this.data.iterator();
        while (iterator.hasNext()) {
            stringBuilder.append(index);
            stringBuilder.append(':');
            stringBuilder.append(iterator.next());
            stringBuilder.append('\n');
        }

        return stringBuilder.toString();
    }
}
