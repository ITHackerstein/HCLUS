package com.davidecarella.hclus.client.communication;

import com.davidecarella.hclus.common.ClusterDistanceMethod;
import com.davidecarella.hclus.common.Clustering;
import com.davidecarella.hclus.common.Example;
import com.davidecarella.hclus.common.serialization.DataDeserializer;
import com.davidecarella.hclus.common.serialization.DataSerializer;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe che rappresenta la connessione con il server.
 */
public class ServerConnection {
    private static ServerConnection instance;

    public static ServerConnection the() {
        return instance;
    }

    /**
     * <p>Apre la connessione con il server in ascolto sull'indirizzo, {@code address}, e porta, {@code port}, entrambi
     * specificati come parametro.
     *
     * <p>Se una connessione era già aperta allora la chiude e ne apre un'altra.
     *
     * @param address l'indirizzo del server
     * @param port la porta del server
     * @throws IOException in caso di errori di I/O durante la connessione
     */
    public static void open(String address, int port) throws IOException {
        if (instance != null) {
            instance.closeConnection();
        }

        instance = new ServerConnection(address, port);
    }

    /**
     * Il socket per la connessione.
     */
    private final Socket socket;

    /**
     * Il <i>serializzatore</i> per inviare dati al server.
     */
    private final DataSerializer dataSerializer;

    /**
     * Il <i>serializzatore</i> per leggere dati dal server.
     */
    private final DataDeserializer dataDeserializer;

    /**
     * Costruisce una nuova connessione al server in ascolto sull'indirizzo, {@code address}, e porta, {@code port},
     * entrambi specificati come parametro.
     *
     * @param address l'indirizzo del server
     * @param port la porta del server
     * @throws IOException in caso di errori di I/O durante la connessione
     */
    private ServerConnection(String address, int port) throws IOException {
        this.socket = new Socket(address, port);

        // NOTE: NEVER, ever, swap this two lines here. Doing that will cause the application to block on the
        //       Socket#getInputStream method for whatever reason.
        this.dataSerializer = new DataSerializer(this.socket.getOutputStream());
        this.dataDeserializer = new DataDeserializer(this.socket.getInputStream());
    }

    /**
     * Invia la richiesta di caricamento dati dalla tabella con nome {@code tableName} specificato come parametro.
     *
     * @param tableName il nome della tabella da cui caricare i dati
     * @return il numero di esempi contenuti nel dataset caricato
     * @throws IOException in caso di errori di durante la comunicazione
     */
    public int loadDataset(String tableName) throws IOException {
        this.dataSerializer.serializeInt(0);
        this.dataSerializer.serializeString(tableName);

        var responseType = this.dataDeserializer.deserializeInt();
        if (responseType == 0) {
            return this.dataDeserializer.deserializeInt();
        }

        if (responseType == 1) {
            throw new IOException(this.dataDeserializer.deserializeString());
        } else {
            throw new IOException("Risposta non valida!");
        }
    }

    /**
     * Invia la richiesta di creazione di un clustering dati come parametro: profondità del dendrogramma
     * ({@code depth}), tipo di distanza da utilizzare ({@code distanceType}) e nome del file dove salvare il
     * dendrogramma ({@code fileName}).
     *
     * @param depth la profondità del dendrogramma
     * @param distanceType il tipo di distanza (0 per single-link, 1 per average-link)
     * @param fileName il nome del file dove salvare il dendrogramma
     * @return il dendrogramma creato dal server
     * @throws IOException in caso di errori di durante la comunicazione
     */
    public Clustering newClustering(int depth, int distanceType, String fileName) throws IOException {
        this.dataSerializer.serializeInt(1);
        this.dataSerializer.serializeInt(depth);
        this.dataSerializer.serializeInt(distanceType);
        this.dataSerializer.serializeString(fileName);

        var responseType = this.dataDeserializer.deserializeInt();
        if (responseType == 0) {
            return this.dataDeserializer.deserializeClustering();
        }

        if (responseType == 1) {
            throw new IOException(this.dataDeserializer.deserializeString());
        } else {
            throw new IOException("Risposta non valida!");
        }
    }

    /**
     * Invia la richiesta del caricamento del clustering memorizzato sul file il cui nome, {@code fileName}, è
     * specificato come parametro
     *
     * @param fileName il nome del file da cui caricare il dendrogramma
     * @return il dendrogramma caricato dal file
     * @throws IOException in caso di errori durante la comunicazione
     */
    public Clustering loadClustering(String fileName) throws IOException {
        this.dataSerializer.serializeInt(2);
        this.dataSerializer.serializeString(fileName);

        var responseType = this.dataDeserializer.deserializeInt();
        if (responseType == 0) {
            return this.dataDeserializer.deserializeClustering();
        }

        if (responseType == 1) {
            throw new IOException(this.dataDeserializer.deserializeString());
        } else {
            throw new IOException("Risposta non valida!");
        }
    }

    /**
     * Invia la richiesta di caricamento di esempi i cui indici, {@code indices}, sono specificati come parametro.
     *
     * @param indices la listi di indici degli esempi che si vuole conoscere
     * @return gli esempi corrispondenti agl indici richiesti
     * @throws IOException in caso di errori durante la comunicazione
     */
    public List<Example> getExamples(List<Integer> indices) throws IOException {
        this.dataSerializer.serializeInt(3);
        this.dataSerializer.serializeInt(indices.size());
        for (var index : indices) {
            this.dataSerializer.serializeInt(index);
        }

        var responseType = this.dataDeserializer.deserializeInt();
        if (responseType == 0) {
            var examples = new ArrayList<Example>();
            for (int i = 0; i < indices.size(); i++) {
                examples.add(this.dataDeserializer.deserializeExample());
            }
            return examples;
        }

        if (responseType == 1) {
            throw new IOException(this.dataDeserializer.deserializeString());
        } else {
            throw new IOException("Risposta non valida!");
        }
    }

    public List<ClusterDistanceMethod> getClusterDistanceMethods() throws IOException {
        this.dataSerializer.serializeInt(4);

        var responseType = this.dataDeserializer.deserializeInt();
        if (responseType == 0) {
            var count = this.dataDeserializer.deserializeInt();
            var clusterDistanceMethods = new ArrayList<ClusterDistanceMethod>();
            while (count-- > 0) {
                clusterDistanceMethods.add(this.dataDeserializer.deserializeClusterDistanceMethod());
            }
            return clusterDistanceMethods;
        }

        if (responseType == 1) {
            throw new IOException(this.dataDeserializer.deserializeString());
        } else {
            throw new IOException("Risposta non valida!");
        }
    }

    /**
     * Invia la richiesta di chiusura della connessione al server.
     *
     * @throws IOException in caso di errori durante la comunicazione
     */
    public void closeConnection() throws IOException {
        this.dataSerializer.serializeInt(5);

        var responseType = this.dataDeserializer.deserializeInt();
        if (responseType == 0) {
            return;
        }

        throw new IOException("Errore durante la chiusura della connessione!");
    }
}