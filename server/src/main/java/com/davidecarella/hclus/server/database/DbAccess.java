package com.davidecarella.hclus.server.database;

import com.davidecarella.hclus.server.exceptions.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * <p>Classe che gestisce la connessione al DBMS da cui vengono letti i dati.
 *
 * <p>Il DBMS in questo caso conterrà un database con nome {@code MapDB} al cui interno troviamo le tabelle su cui
 * effettuare il mining.
 */
public class DbAccess {
    /**
     * NOTA: Viene aggiunto solo per evitare il warning durante la generazione del JavaDoc.
     */
    public DbAccess() {}

    /**
     * Il nome completamente qualificato della classe {@code Driver}.
     */
	private final String DRIVER_CLASS_NAME = "org.mariadb.jdbc.Driver";

    /**
     * Il prefisso della stringa di connessione che indica il DMBS utilizzato.
     */
    private final String DBMS = "jdbc:mariadb";

    /**
     * L'indirizzo del DBMS.
     */
    private final String SERVER = "localhost";

    /**
     * Il nome del database.
     */
    private final String DATABASE = "hclus_db";

    /**
     * La porta utilizzata per la connessione al DBMS.
     */
    private final int PORT = 3306;

    /**
     * Il nome utente del DBMS.
     */
    private final String USER_ID = "hclus_user";

    /**
     * La password dell'utente con nome utente specificato in {@link DbAccess#USER_ID}.
     */
    private final String PASSWORD = "";

    /**
     * L'istanza di connessione al DMBS.
     */
    private Connection conn;

    /**
     * Inizializza la connessione al database.
     *
     * @throws DatabaseConnectionException se la connessione al database fallisce
     */
    public void initConnection() throws DatabaseConnectionException {
        try {
            Class.forName(DRIVER_CLASS_NAME);
        } catch (ClassNotFoundException exception) {
            throw new DatabaseConnectionException("Errore durante la connessione al database!", exception);
        }

        String connectionString = DBMS + "://" + SERVER + ":" + PORT + "/" + DATABASE + "?user=" + USER_ID + "&password=" + PASSWORD + "&serverTimezone=UTC";

        try {
            this.conn = DriverManager.getConnection(connectionString);
        } catch(SQLException exception) {
            throw new DatabaseConnectionException("Errore durante la connessione al database!", exception);
        }
    }

    /**
     * Si connette al DMBS se non lo ha già fatto e restituisce la connessione al database.
     *
     * @return la connessione al database
     * @throws DatabaseConnectionException se la connessione al database fallisce
     */
    public Connection getConnection() throws DatabaseConnectionException {
        // NOTA: Viene modificata leggermente l'implementazione del metodo per evitare che si connetta più volte al
        //       database

        if (this.conn == null) {
            this.initConnection();
        }

        return this.conn;
    }

    /**
     * Chiude la connessione al database.
     *
     * @throws SQLException se si verifica un errore durante la chiusura della connessione
     */
    public void closeConnection() throws SQLException {
        this.conn.close();
    }
}
