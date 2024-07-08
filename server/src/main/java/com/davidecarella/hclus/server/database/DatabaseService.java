package com.davidecarella.hclus.server.database;

import com.davidecarella.hclus.server.data.Example;
import com.davidecarella.hclus.server.exceptions.DatabaseConnectionException;
import com.davidecarella.hclus.server.exceptions.EmptySetException;
import com.davidecarella.hclus.server.exceptions.MissingNumberException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Classe che gestisce la connessione al DBMS da cui vengono letti i dati.
 *
 * <p>Il DBMS in questo caso conterrà un database con nome {@code MapDB} al cui interno troviamo le tabelle su cui
 * effettuare il mining.
 */
public class DatabaseService {
    /**
     * Costruttore di default nascosto per evitare la costruzione della classe
     */
    private DatabaseService() {}

    /**
     * La stringa di connessione utilizzata per connettersi al database.
     */
    private static final String CONNECTION_STRING = "jdbc:mariadb://127.0.0.1:3306/hclus_db?user=hclus_user&password=";

    /**
     * La lista dei tipi numerici in SQL.
     */
    private static final List<Integer> NUMERIC_TYPES = List.of(
        Types.BIGINT,
        Types.DECIMAL,
        Types.DOUBLE,
        Types.FLOAT,
        Types.INTEGER,
        Types.NUMERIC,
        Types.REAL,
        Types.SMALLINT
    );

    /**
     * Restituisce una connessione al database.
     *
     * @throws DatabaseConnectionException se la connessione al database fallisce
     */
    private static Connection getConnection() throws DatabaseConnectionException {
        try {
            return DriverManager.getConnection(CONNECTION_STRING);
        } catch(SQLException exception) {
            throw new DatabaseConnectionException("Errore durante la connessione al database!", exception);
        }
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
    public static List<Example> getExamples(String tableName) throws DatabaseConnectionException, SQLException, MissingNumberException, EmptySetException {
        try (var connection = getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("SELECT * FROM " + tableName))
        {
            var metadata = resultSet.getMetaData();
            var columnCount = metadata.getColumnCount();

            for (int i = 0; i < columnCount; i++) {
                if (!NUMERIC_TYPES.contains(metadata.getColumnType(i + 1))) {
                    throw new MissingNumberException("La tabella contiene attributi non numerici!");
                }
            }

            var examples = new ArrayList<Example>();
            while (resultSet.next()) {
                var example = new Example();
                for (int i = 0; i < columnCount; ++i) {
                    example.add(resultSet.getDouble(i + 1));
                }
                examples.add(example);
            }

            if (examples.isEmpty()) {
                throw new EmptySetException("La tabella è vuota!");
            }

            return examples;
        }
    }
}
