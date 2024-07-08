package com.davidecarella.hclus.server.data;

import com.davidecarella.hclus.server.database.DatabaseService;
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
    private final List<Example> data = new ArrayList<>();

    /**
     * Costruisce i dati usando le righe di una tabella (con nome {@code tableName} specificato come parametro) dal
     * database.
     *
     * @param tableName la tabella le cui righe si vogliono usare come esempi
     * @throws NoDataException se ci dovesse essere un error durante il caricamento della tabella
     */
    public Data(String tableName) throws NoDataException {
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
