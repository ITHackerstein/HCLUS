package com.davidecarella.hclus.server.database;

import com.davidecarella.hclus.server.exceptions.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Classe per gestire lo schema di una tabella nel database.
 */
public class TableSchema {
    /**
     * Connessione al DBMS.
     */
    private DbAccess db;

    /**
     * Classe che rappresenta una colonna all'interno della tabella.
     */
    public static class Column {
        /**
         * Il nome della colonna.
         */
        private String name;

        /**
         * Il tipo della colonna.
         */
        private String type;

        /**
         * Costruttore che inizializza una colonna con nome {@code name} e tipo {@code type}, entrambi specificati come
         * parametro
         *
         * @param name il nome della colonna
         * @param type il tipo della colonna
         */
        Column(String name, String type) {
            this.name = name;
            this.type = type;
        }

        /**
         * Restituisce il nome della colonna.
         *
         * @return il nome della colonna
         */
        public String getColumnName() {
            return name;
        }

        /**
         * Restituisce {@code true} se la colonna ha tipo numerico, {@code false} altrimenti.
         *
         * @return {@code true} se la colonna ha tipo numerico, {@code false} altrimenti
         */
        public boolean isNumber() {
            return type.equals("number");
        }

        /**
         * Restituisce una rappresentazione testuale della colonna.
         *
         * @return la rappresentazione testuale
         */
        @Override
        public String toString() {
            return name + ":" + type;
        }
    }

    /**
     * La lista delle colonne della tabella
     */
    private List<Column> tableSchema = new ArrayList<>();

    /**
     * Costruisce lo schema della tabella partendo dalla connessione a un DMBS, {@code db}, e un nome di una tabella,
     * {@code tableName}, entrambi specificati come parametro.
     *
     * @param db la connessione al DBMS
     * @param tableName il nome della tabella di cui si vuole costruire lo schema
     * @throws SQLException se ci dovessero essere errori durante l'acquisizione dello schema della tabella
     * @throws DatabaseConnectionException se ci dovessero essere errori durante la connessione al DBMS
     */
    public TableSchema(DbAccess db, String tableName) throws SQLException, DatabaseConnectionException {
        this.db = db;
        HashMap<String, String> mapSQL_JAVATypes = new HashMap<>();

        //http://java.sun.com/j2se/1.3/docs/guide/jdbc/getstart/mapping.html
        mapSQL_JAVATypes.put("CHAR", "string");
        mapSQL_JAVATypes.put("VARCHAR", "string");
        mapSQL_JAVATypes.put("LONGVARCHAR", "string");
        mapSQL_JAVATypes.put("BIT", "string");
        mapSQL_JAVATypes.put("SHORT", "number");
        mapSQL_JAVATypes.put("INT", "number");
        mapSQL_JAVATypes.put("LONG", "number");
        mapSQL_JAVATypes.put("FLOAT", "number");
        mapSQL_JAVATypes.put("DOUBLE", "number");

        Connection con = db.getConnection();
        DatabaseMetaData meta = con.getMetaData();
        ResultSet res = meta.getColumns(null, null, tableName, null);

        while (res.next()) {
            if (mapSQL_JAVATypes.containsKey(res.getString("TYPE_NAME"))) {
                tableSchema.add(new Column(
                    res.getString("COLUMN_NAME"),
                    mapSQL_JAVATypes.get(res.getString("TYPE_NAME"))
                ));
            }
        }

        res.close();
    }

    /**
     * Restituisce il numero di attributi della tabella
     *
     * @return il numero di attributi della tabella
     */
    public int getNumberOfAttributes() {
        return tableSchema.size();
    }

    /**
     * Restituisce la colonna in posizione {@code index}, specificata come parametro.
     *
     * @param index la posizione della colonna che si vuole ottenere
     * @return la colonna in posizione {@code index}
     */
    public Column getColumn(int index) {
        return tableSchema.get(index);
    }
}
