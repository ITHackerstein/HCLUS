package com.davidecarella.database;

import com.davidecarella.data.Example;
import com.davidecarella.exceptions.DatabaseConnectionException;
import com.davidecarella.exceptions.EmptySetException;
import com.davidecarella.exceptions.MissingNumberException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilizzata per caricare i dati da una tabella del database.
 */
public class TableData {
    /**
     * Connessione al DBMS.
     */
    private DbAccess db;

    /**
     * Costruisce un'istanza con connessione al DBMS, {@code db}, specificata come parametro.
     *
     * @param db connessione al DBMS
     */
    public TableData(DbAccess db) {
        this.db = db;
    }

    /**
     * Restituisce una lista di esempi contenuti nella tabella con nome {@code tableName}, specificato come parametro.
     *
     * @param tableName il nome della tabella da cui si vuole leggere la lista di esempi
     * @return la lista di esempi contenuti nella tabella
     *
     * @throws DatabaseConnectionException se ci dovessero essere errori durante la connessione al database
     * @throws SQLException se ci dovessero essere errori durante le interrogazioni al database
     * @throws MissingNumberException se la tabella dovesse contenere degli attributi non numerici
     * @throws EmptySetException se la tabella dovesse essere vuota
     */
    public List<Example> getDistinctTransazioni(String tableName) throws DatabaseConnectionException, SQLException, MissingNumberException, EmptySetException {
        var tableSchema = new TableSchema(this.db, tableName);
        for (int i = 0; i < tableSchema.getNumberOfAttributes(); ++i) {
            if (!tableSchema.getColumn(i).isNumber()) {
                throw new MissingNumberException("La tabella contiene attributi non numerici!");
            }
        }

        var result = new ArrayList<Example>();
        try (var statement = db.getConnection().createStatement();
             var resultSet = statement.executeQuery("SELECT * FROM " + tableName))
        {
            while (resultSet.next()) {
                var example = new Example();
                for (int i = 0; i < tableSchema.getNumberOfAttributes(); ++i) {
                    example.add(resultSet.getDouble(i + 1));
                }
                result.add(example);
            }
        }

        if (result.isEmpty()) {
            throw new EmptySetException("La tabella è vuota!");
        }

        return result;
    }
}
