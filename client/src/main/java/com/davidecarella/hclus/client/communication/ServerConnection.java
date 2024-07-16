package com.davidecarella.hclus.client.communication;

import com.davidecarella.hclus.client.exceptions.ServerException;
import com.davidecarella.hclus.common.ClusterDistanceMethod;
import com.davidecarella.hclus.common.Clustering;
import com.davidecarella.hclus.common.Example;
import com.davidecarella.hclus.common.serialization.DataDeserializer;
import com.davidecarella.hclus.common.serialization.DataSerializer;

import java.io.IOException;
import java.net.Socket;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>La connessione con il server HCLUS.
 *
 * <p>È una classe <i>singleton</i> che permette di aprire una connessione con il metodo
 * {@link ServerConnection#open(String, int)} alla quale, una volta aperta, è possibile accedere all'istanza con il
 * metodo {@link ServerConnection#the()}.
 *
 * <p>Attraverso di essa è possibile inviare le richieste previste dal protocollo:
 * <ul>
 *     <li>{@code LoadDataset}, realizzata dal metodo {@link ServerConnection#loadDataset(String)};</li>
 *     <li>{@code GetDatasets}, realizzata dal metodo {@link ServerConnection#getDatasets()}</li>
 *     <li>{@code NewClustering}, realizzata dal metodo {@link ServerConnection#newClustering(int, int, String)};</li>
 *     <li>{@code LoadClustering}, realizzata dal metodo {@link ServerConnection#loadClustering(String)};</li>
 *     <li>{@code GetExamples}, realizzata dal metodo {@link ServerConnection#getExamples(List)};</li>
 *     <li>{@code GetClusterDistanceMethods}, realizzata dal metodo {@link ServerConnection#getClusterDistanceMethods()};</li>
 *     <li>{@code GetSavedClusterings}, realizzata dal metodo {@link ServerConnection#getSavedClusterings()};</li>
 *     <li>{@code CloseConnection}, realizzata dal metodo {@link ServerConnection#closeConnection()}.</li>
 * </ul>
 */
public class ServerConnection {
    /**
     * L'istanza della connessione al server HCLUS.
     */
    private static ServerConnection instance;

    /**
     * Restituisce l'istanza della connessione al server HCLUS.
     *
     * @return l'istanza della connessione al server HCLUS o {@code null} se non è connesso.
     */
    public static ServerConnection the() {
        return instance;
    }

    /**
     * Apre la connessione con il server in ascolto sull'indirizzo, {@code address}, e porta, {@code port}, entrambi
     * specificati come parametro.
     *
     * @param address l'indirizzo del server
     * @param port la porta del server
     * @throws IOException in caso di errori di I/O durante la connessione
     */
    public static void open(String address, int port) throws IOException {
        instance = new ServerConnection(address, port);
    }

    /**
     * Il socket che gestisce la connessione.
     */
    private final Socket socket;

    /**
     * Il <i>serializer</i> per inviare dati al server.
     */
    private final DataSerializer dataSerializer;

    /**
     * Il <i>deserializer</i> per ricevere dati dal server.
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
     * Invia la richiesta {@code LoadDataset} al server con argomento {@code tableName}, specificato come parametro.
     *
     * @param tableName il nome della tabella da cui caricare il dataset
     * @return il numero di esempi contenuti nel dataset caricato
     * @throws IOException in caso di errori di I/O durante la comunicazione
     * @throws ServerException in caso di un errore durante l'esecuzione della richiesta
     */
    public int loadDataset(String tableName) throws IOException, ServerException {
        this.dataSerializer.serializeInt(0);
        this.dataSerializer.serializeString(tableName);

        var responseType = this.dataDeserializer.deserializeInt();
        if (responseType == 0) {
            return this.dataDeserializer.deserializeInt();
        }

        if (responseType == 1) {
            throw new ServerException(this.dataDeserializer.deserializeString(), this.dataDeserializer.deserializeString());
        }

        throw new ServerException("Risposta non valida!", String.format("%d non è un tipo di risposta valido", responseType));
    }

    /**
     * Invia la richiesta {@code GetDatasets} al server.
     *
     * @return la lista dei dataset disponibili sul server
     * @throws IOException in caso di errori di I/O durante la comunicazione
     * @throws ServerException in caso di un errore durante l'esecuzione della richiesta
     */
    public List<String> getDatasets() throws IOException, ServerException {
        this.dataSerializer.serializeInt(1);

        var responseType = this.dataDeserializer.deserializeInt();
        if (responseType == 0) {
            var count = this.dataDeserializer.deserializeInt();
            var datasets = new ArrayList<String>();
            while (count-- > 0) {
                datasets.add(this.dataDeserializer.deserializeString());
            }
            return datasets;
        }

        if (responseType == 1) {
            throw new ServerException(this.dataDeserializer.deserializeString(), this.dataDeserializer.deserializeString());
        }

        throw new ServerException("Risposta non valida!", String.format("%d non è un tipo di risposta valido", responseType));
    }

    /**
     * Invia la richiesta {@code NewClustering} al server con argomenti: {@code depth}, {@code distanceId} e
     * {@code name}, specificati come parametro.
     *
     * @param depth la profondità del clustering
     * @param distanceId l'identificatore del metodo per il calcolo della distanza fra cluster
     * @param name il nome del clustering
     * @return il clustering creato dal server
     * @throws IOException in caso di errori di I/O durante la comunicazione
     * @throws ServerException in caso di un errore durante l'esecuzione della richiesta
     *
     * @see ServerConnection#getClusterDistanceMethods() il metodo per recuperare i metodi per il calolo della distanza
     *                                                   fra cluster disponibili sul server
     */
    public Clustering newClustering(int depth, int distanceId, String name) throws IOException, ServerException {
        this.dataSerializer.serializeInt(2);
        this.dataSerializer.serializeInt(depth);
        this.dataSerializer.serializeInt(distanceId);
        this.dataSerializer.serializeString(name);

        var responseType = this.dataDeserializer.deserializeInt();
        if (responseType == 0) {
            return this.dataDeserializer.deserializeClustering();
        }

        if (responseType == 1) {
            throw new ServerException(this.dataDeserializer.deserializeString(), this.dataDeserializer.deserializeString());
        }

        throw new ServerException("Risposta non valida!", String.format("%d non è un tipo di risposta valido", responseType));
    }

    /**
     * Invia la richiesta {@code LoadClustering} al server con argomento {@code name}, specificato come parametro.
     *
     * @param name il nome del clustering che si vuole caricare
     * @return il clustering caricato dal server
     * @throws IOException in caso di errori di I/O durante la comunicazione
     * @throws ServerException in caso di un errore durante l'esecuzione della richiesta
     */
    public Clustering loadClustering(String name) throws IOException, ServerException {
        this.dataSerializer.serializeInt(3);
        this.dataSerializer.serializeString(name);

        var responseType = this.dataDeserializer.deserializeInt();
        if (responseType == 0) {
            return this.dataDeserializer.deserializeClustering();
        }

        if (responseType == 1) {
            throw new ServerException(this.dataDeserializer.deserializeString(), this.dataDeserializer.deserializeString());
        }

        throw new ServerException("Risposta non valida!", String.format("%d non è un tipo di risposta valido", responseType));
    }

    /**
     * Invia la richiesta {@code} GetExamples al server con argomento {@code indices}, specificato come parametro.
     *
     * @param indices la lista degli indici degli esempi che si vuole recuperare
     * @return la lista degli esempi corrispondenti agl indici richiesti
     * @throws IOException in caso di errori di I/O durante la comunicazione
     * @throws ServerException in caso di un errore durante l'esecuzione della richiesta
     */
    public List<Example> getExamples(List<Integer> indices) throws IOException, ServerException {
        this.dataSerializer.serializeInt(4);
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
            throw new ServerException(this.dataDeserializer.deserializeString(), this.dataDeserializer.deserializeString());
        }

        throw new ServerException("Risposta non valida!", String.format("%d non è un tipo di risposta valido", responseType));
    }

    /**
     * Invia la richiesta {@code GetClusterDistanceMethods} al server.
     *
     * @return la lista dei metodi per il calcolo della distanza fra cluster
     * @throws IOException in caso di errori di I/O durante la comunicazione
     * @throws ServerException in caso di un errore durante l'esecuzione della richiesta
     */
    public List<ClusterDistanceMethod> getClusterDistanceMethods() throws IOException, ServerException {
        this.dataSerializer.serializeInt(5);

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
            throw new ServerException(this.dataDeserializer.deserializeString(), this.dataDeserializer.deserializeString());
        }

        throw new ServerException("Risposta non valida!", String.format("%d non è un tipo di risposta valido", responseType));
    }

    /**
     * Invia la richiesta {@code GetSavedClusterings} al server.
     *
     * @return la lista dei clustering salvati sul server
     * @throws IOException in caso di errori di I/O durante la comunicazione
     * @throws ServerException in caso di un errore durante l'esecuzione della richiesta
     */
    public List<String> getSavedClusterings() throws IOException, ServerException {
        this.dataSerializer.serializeInt(6);

        var responseType = this.dataDeserializer.deserializeInt();
        if (responseType == 0) {
            var count = this.dataDeserializer.deserializeInt();
            var clusterings = new ArrayList<String>();
            while (count-- > 0) {
                clusterings.add(this.dataDeserializer.deserializeString());
            }
            return clusterings;
        }

        if (responseType == 1) {
            throw new ServerException(this.dataDeserializer.deserializeString(), this.dataDeserializer.deserializeString());
        }

        throw new ServerException("Risposta non valida!", String.format("%d non è un tipo di risposta valido", responseType));
    }

    /**
     * Invia la richiesta {@code CloseConnection} al server.
     *
     * @throws IOException in caso di errori durante l'esecuzione della richiesta
     */
    public void closeConnection() throws IOException {
        this.dataSerializer.serializeInt(7);

        var responseType = this.dataDeserializer.deserializeInt();
        if (responseType == 0) {
            return;
        }

        throw new IOException("Errore durante la chiusura della connessione!");
    }
}