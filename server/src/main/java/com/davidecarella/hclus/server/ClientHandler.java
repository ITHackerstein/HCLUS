package com.davidecarella.hclus.server;

import com.davidecarella.hclus.common.ClusterDistanceMethod;
import com.davidecarella.hclus.common.Clustering;
import com.davidecarella.hclus.common.exceptions.ExampleSizeMismatchException;
import com.davidecarella.hclus.server.clustering.Dataset;
import com.davidecarella.hclus.server.clustering.HierarchicalClustering;
import com.davidecarella.hclus.common.Example;
import com.davidecarella.hclus.server.database.DatabaseService;
import com.davidecarella.hclus.server.distance.*;
import com.davidecarella.hclus.server.exceptions.*;
import com.davidecarella.hclus.common.serialization.DataDeserializer;
import com.davidecarella.hclus.common.serialization.DataSerializer;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Classe che gestisce la connessione con un client.
 */
public class ClientHandler extends Thread {
    /**
     * Intero che rappresenta una risposta con esito positivo.
     */
    private static final int SUCCESS = 0;

    /**
     * Intero che rappresenta una risposta con esito negativo.
     */
    private static final int ERROR = 1;

    /**
     * La lista dei metodi per il calcolo della distanza fra cluster.
     */
    private static final ClusterDistance[] AVAILABLE_DISTANCE_METHODS = new ClusterDistance[] {
        new AverageLinkDistance(),
        new CentroidLinkDistance(),
        new CompleteLinkDistance(),
        new MedianLinkDistance(),
        new SingleLinkDistance(),
        new WardLinkDistance(),
        new WeightedAverageLinkDistance()
    };

    /**
     * La directory dove vengono salvati i clustering.
     */
    private static final String CLUSTERINGS_DIRECTORY = "clusterings";

    /**
     * L'espressione regolare per il nome di un clustering.
     */
    private static final String CLUSTERING_NAME_REGEX = "^[a-zA-Z0-9_]+$";

    /**
     * L'espressione regolare per il nome del file di un clustering.
     */
    private static final String CLUSTERING_FILE_REGEX = "^[a-zA-Z0-9_]+\\.hclus$";

    /**
     * Il socket che gestisce la connessione con il client.
     */
    private final Socket clientSocket;

    /**
     * L'eventuale dataset caricato.
     */
    private Dataset dataset = null;

    /**
     * Costruisce il gestore del client e lancia il thread associato.
     *
     * @param clientSocket il socket che gestisce la connessione con il client
     */
    public ClientHandler(Socket clientSocket) {
        super(String.format("HCLUS-Client(%s, %d)",
            clientSocket.getInetAddress().getHostAddress(),
            clientSocket.getPort()
        ));

        this.clientSocket = clientSocket;
        this.start();
    }

    /**
     * Entry point del thread.
     */
    @Override
    public void run() {
        log("Connesso");

        try (var dataDeserializer = new DataDeserializer(this.clientSocket.getInputStream());
             var dataSerializer = new DataSerializer(this.clientSocket.getOutputStream()))
        {
            while (!this.clientSocket.isClosed()) {
                try {
                    var requestType = dataDeserializer.deserializeInt();
                    switch (requestType) {
                        case 0 -> loadDatasetRequest(dataDeserializer, dataSerializer);
                        case 1 -> getDatasetsRequest(dataDeserializer, dataSerializer);
                        case 2 -> newClusteringRequest(dataDeserializer, dataSerializer);
                        case 3 -> loadClusteringRequest(dataDeserializer, dataSerializer);
                        case 4 -> getExamplesRequest(dataDeserializer, dataSerializer);
                        case 5 -> getClusterDistanceMethodsRequest(dataDeserializer, dataSerializer);
                        case 6 -> getSavedClusterings(dataDeserializer, dataSerializer);
                        case 7 -> closeConnectionRequest(dataDeserializer, dataSerializer);
                        default -> {
                            log(String.format("Richiesta non valida (%d)!", requestType));
                            dataSerializer.serializeInt(ERROR);
                            dataSerializer.serializeString("Richiesta non valida!");
                            dataSerializer.serializeString(String.format("%d non è un tipo valido di richiesta", requestType));
                        }
                    }
                } catch (SocketException | EOFException ignored) {
                    return;
                } catch (IOException exception) {
                    log(String.format("Errore durante la comunicazione: %s!", exception.getMessage()));
                }
            }
        } catch (IOException exception) {
            log("Errore durante la creazione/chiusura delle stream di I/O per il socket!");
        } finally {
            tryCloseSocket();
        }
    }

    /**
     * Gestisce la richiesta {@code LoadData}.
     *
     * @param dataDeserializer il <i>deserializer</i> per ricevere dati dal client
     * @param dataSerializer il <i>serializer</i> per inviare dati al client
     * @throws IOException in caso di errori di I/O durante la comunicazione
     */
    private void loadDatasetRequest(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        String tableName = dataDeserializer.deserializeString();
        log(String.format("Ricevuta richiesta `LoadDataset(tableName=%s)`", tableName));

        this.dataset = null;

        try {
            this.dataset = new Dataset(tableName);
            log(String.format("Richiesta `LoadDataset(tableName=%s)` eseguita con successo", tableName));
            dataSerializer.serializeInt(SUCCESS);
            dataSerializer.serializeInt(this.dataset.getExampleCount());
        } catch (DatabaseConnectionException | SQLException exception) {
            log(String.format("Errore durante l'esecuzione della richiesta `LoadDataset(tableName=%s)`!", tableName));
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Errore durante il caricamento del dataset!");
            dataSerializer.serializeString(throwableStackTraceToString(exception));
        } catch (EmptySetException | MissingNumberException exception) {
            log(String.format("Errore durante l'esecuzione della richiesta `LoadDataset(tableName=%s)`!", tableName));
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString(exception.getMessage());
            dataSerializer.serializeString("");
        }
    }

    /**
     * Gestisce la richiesta {@code LoadData}.
     *
     * @param dataDeserializer il <i>deserializer</i> per ricevere dati dal client
     * @param dataSerializer il <i>serializer</i> per inviare dati al client
     * @throws IOException in caso di errori di I/O durante la comunicazione
     */
    private void getDatasetsRequest(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        log("Ricevuta richiesta `GetDatasets`");

        try {
            var datasets = DatabaseService.getAvailableDatasets();
            dataSerializer.serializeInt(SUCCESS);
            dataSerializer.serializeInt(datasets.size());
            for (var dataset : datasets) {
                dataSerializer.serializeString(dataset);
            }
            log("Richiesta `GetDatasets` eseguita con successo");
        } catch (DatabaseConnectionException | SQLException exception) {
            log("Errore durante l'esecuzione della richiesta `GetDatasets`!");
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Errore durante la lettura dei dataset disponibili!");
            dataSerializer.serializeString(throwableStackTraceToString(exception));
        }
    }

    /**
     * Gestisce la richiesta {@code NewClustering}.
     *
     * @param dataDeserializer il <i>deserializer</i> per ricevere dati dal client
     * @param dataSerializer il <i>serializer</i> per inviare dati al client
     * @throws IOException in caso di errori di I/O durante la comunicazione
     */
    private void newClusteringRequest(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        var depth = dataDeserializer.deserializeInt();
        var distanceId = dataDeserializer.deserializeInt();
        var name = dataDeserializer.deserializeString();

        log(String.format("Ricevuta richiesta `NewClustering(depth=%d, distanceId=%d, name=%s)`", depth, distanceId, name));

        if (this.dataset == null) {
            log(String.format("Errore durante l'esecuzione della richiesta `NewClustering(depth=%d, distanceId=%d, name=%s)`!", depth, distanceId, name));
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Nessun dataset caricato!");
            return;
        }

        if (distanceId < 0 || distanceId >= AVAILABLE_DISTANCE_METHODS.length) {
            log(String.format("Errore durante l'esecuzione della richiesta `NewClustering(depth=%d, distanceId=%d, name=%s)`!", depth, distanceId, name));
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Tipo di distanza non valida!");
            return;
        }

        if (!name.matches(CLUSTERING_NAME_REGEX)) {
            log(String.format("Errore durante l'esecuzione della richiesta `NewClustering(depth=%d, distanceId=%d, name=%s)`!", depth, distanceId, name));
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Nome non valido!");
            return;
        }

        Clustering clustering;
        try {
            clustering = HierarchicalClustering.mine(this.dataset, AVAILABLE_DISTANCE_METHODS[distanceId], depth);
        } catch (InvalidDepthException | ExampleSizeMismatchException exception) {
            log(String.format("Errore durante l'esecuzione della richiesta `NewClustering(depth=%d, distanceId=%d, name=%s)`!", depth, distanceId, name));
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString(exception.getMessage());
            dataSerializer.serializeString("");
            return;
        }

        try {
            var clusteringsDir = new File(CLUSTERINGS_DIRECTORY);
            if (!clusteringsDir.exists()) {
                if (!clusteringsDir.mkdirs()) {
                    dataSerializer.serializeInt(ERROR);
                    dataSerializer.serializeString("Non è stato possibile creare la directory per i clustering!");
                    dataSerializer.serializeString("");
                    return;
                }
            } else if (!clusteringsDir.isDirectory()) {
                dataSerializer.serializeInt(ERROR);
                dataSerializer.serializeString(String.format("Esiste già un file con nome '%s'!", CLUSTERINGS_DIRECTORY));
                dataSerializer.serializeString("");
                return;
            }

            var path = clusteringsDir.toPath().resolve(String.format("%s.hclus", name));
            HierarchicalClustering.save(clustering, path.toString());
        } catch (IOException exception) {
            log(String.format("Errore durante l'esecuzione della richiesta `NewClustering(depth=%d, distanceId=%d, name=%s)`!", depth, distanceId, name));
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Errore durante il salvataggio del dendrogramma!");
            dataSerializer.serializeString(throwableStackTraceToString(exception));
            return;
        }

        log(String.format("Richiesta `NewClustering(depth=%d, distanceId=%d, name=%s)` eseguita con successo", depth, distanceId, name));
        dataSerializer.serializeInt(SUCCESS);
        dataSerializer.serializeClustering(clustering);
    }

    /**
     * Gestisce la richiesta {@code LoadClustering}.
     *
     * @param dataDeserializer il <i>deserializer</i> per ricevere dati dal client
     * @param dataSerializer il <i>serializer</i> per inviare dati al client
     * @throws IOException in caso di errori di I/O durante la comunicazione
     */
    private void loadClusteringRequest(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        var name = dataDeserializer.deserializeString();

        log(String.format("Ricevuta richiesta `LoadClustering(name=%s)`", name));

        if (this.dataset == null) {
            log(String.format("Errore durante l'esecuzione della richiesta `LoadClustering(name=%s)`!", name));
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Nessun dataset caricato!");
            dataSerializer.serializeString("");
            return;
        }

        if (!name.matches(CLUSTERING_NAME_REGEX)) {
            log(String.format("Errore durante l'esecuzione della richiesta `LoadClustering(name=%s)`!", name));
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Nome non valido!");
            dataSerializer.serializeString("");
            return;
        }

        Clustering clustering;
        try {
            clustering = HierarchicalClustering.load(String.format("%s/%s.hclus", CLUSTERINGS_DIRECTORY, name), this.dataset);
        } catch (FileNotFoundException exception) {
            log(String.format("Errore durante l'esecuzione della richiesta `LoadClustering(name=%s)`!", name));
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Il file inserito non esiste!");
            dataSerializer.serializeString("");
            return;
        } catch (IOException exception) {
            log(String.format("Errore durante l'esecuzione della richiesta `LoadClustering(name=%s)`!", name));
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Errore durante il caricamento del dendrogramma!");
            dataSerializer.serializeString(throwableStackTraceToString(exception));
            return;
        } catch (InvalidDepthException | InvalidClusterIndexException exception) {
            log(String.format("Errore durante l'esecuzione della richiesta `LoadClustering(name=%s)`!", name));
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString(exception.getMessage());
            dataSerializer.serializeString("");
            return;
        }

        log(String.format("Richiesta `LoadClustering(name=%s)` eseguita con successo", name));
        dataSerializer.serializeInt(SUCCESS);
        dataSerializer.serializeClustering(clustering);
    }

    /**
     * Gestisce la richiesta {@code GetExamples}.
     *
     * @param dataDeserializer il <i>deserializer</i> per ricevere dati dal client
     * @param dataSerializer il <i>serializer</i> per inviare dati al client
     * @throws IOException in caso di errori di I/O durante la comunicazione
     */
    private void getExamplesRequest(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        var indices = new ArrayList<Integer>();
        var indexCount = dataDeserializer.deserializeInt();
        while (indexCount-- > 0) {
            indices.add(dataDeserializer.deserializeInt());
        }

        log(String.format("Ricevuta richiesta `GetExamples(indexCount=%d)`", indices.size()));

        if (indices.stream().anyMatch(index -> index < 0 || index >= this.dataset.getExampleCount())) {
            log(String.format("Errore durante l'esecuzione della richiesta `GetExamples(indexCount=%d)`!", indices.size()));
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Uno o più indici non validi!");
            dataSerializer.serializeString("");
            return;
        }

        if (this.dataset == null) {
            log(String.format("Errore durante l'esecuzione della richiesta `GetExamples(indexCount=%d)`!", indices.size()));
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Nessun dataset caricato!");
            dataSerializer.serializeString("");
            return;
        }

        dataSerializer.serializeInt(SUCCESS);
        for (var index : indices) {
            dataSerializer.serializeExample(dataset.getExample(index));
        }

        log(String.format("Richiesta `GetExamples(indexCount=%d)` eseguita con successo", indices.size()));
    }

    /**
     * Gestisce la richiesta {@code GetClusterDistanceMethods}.
     *
     * @param dataDeserializer il <i>deserializer</i> per ricevere dati dal client
     * @param dataSerializer il <i>serializer</i> per inviare dati al client
     * @throws IOException in caso di errori di I/O durante la comunicazione
     */
    private void getClusterDistanceMethodsRequest(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        log("Ricevuta richiesta `GetClusterDistanceMethods`");

        dataSerializer.serializeInt(SUCCESS);
        dataSerializer.serializeInt(AVAILABLE_DISTANCE_METHODS.length);
        for (int i = 0; i < AVAILABLE_DISTANCE_METHODS.length; ++i) {
            dataSerializer.serializeClusterDistanceMethod(new ClusterDistanceMethod(i, AVAILABLE_DISTANCE_METHODS[i].getName()));
        }

        log("Richiesta `GetClusterDistanceMethods` eseguita con successo");
    }

    /**
     * Gestisce la richiesta {@code GetSavedClusterings}.
     *
     * @param dataDeserializer il <i>deserializer</i> per ricevere dati dal client
     * @param dataSerializer il <i>serializer</i> per inviare dati al client
     * @throws IOException in caso di errori di I/O durante la comunicazione
     */
    private void getSavedClusterings(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        log("Ricevuta richiesta `GetClusterDistanceMethods`");

        var clusteringsDir = new File(CLUSTERINGS_DIRECTORY);
        if (!clusteringsDir.exists()) {
            if (!clusteringsDir.mkdirs()) {
                log("Errore durante l'esecuzione della richiesta `GetClusterDistanceMethods`!");
                dataSerializer.serializeInt(ERROR);
                dataSerializer.serializeString("Non è stato possibile creare la directory per i clustering!");
                dataSerializer.serializeString("");
                return;
            }
        } else if (!clusteringsDir.isDirectory()) {
            log("Errore durante l'esecuzione della richiesta `GetClusterDistanceMethods`!");
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString(String.format("Esiste già un file con nome '%s'!", CLUSTERINGS_DIRECTORY));
            dataSerializer.serializeString("");
            return;
        }

        var clusteringNames = clusteringsDir.list((dir, name) -> name.matches(CLUSTERING_FILE_REGEX));
        if (clusteringNames == null) {
            log("Errore durante l'esecuzione della richiesta `GetClusterDistanceMethods`!");
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Errore durante la lettura dei clustering salvati!");
            dataSerializer.serializeString("");
            return;
        }

        Arrays.sort(clusteringNames, String::compareToIgnoreCase);
        dataSerializer.serializeInt(SUCCESS);
        dataSerializer.serializeInt(clusteringNames.length);
        for (var name : clusteringNames) {
            dataSerializer.serializeString(name.substring(0, name.indexOf(".hclus")));
        }

        log("Richiesta `GetClusterDistanceMethods` eseguita con successo");
    }

    /**
     * Gestisce la richiesta {@code CloseConnection}.
     *
     * @param dataDeserializer il <i>deserializer</i> per ricevere dati dal client
     * @param dataSerializer il <i>serializer</i> per inviare dati al client
     * @throws IOException in caso di errori di I/O durante la comunicazione
     */
    private void closeConnectionRequest(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        log("Ricevuta richiesta `CloseConnection`");
        dataSerializer.serializeInt(SUCCESS);
        tryCloseSocket();
    }

    /**
     * Prova a chiudere il socket, in caso in cui dovesse esserci un errore lo segnala in output.
     */
    private void tryCloseSocket() {
        try {
            this.clientSocket.close();
            log("Connessione chiusa");
        } catch (IOException exception) {
            log("Errore durante la chiusura del socket!");
        }
    }

    /**
     * Si occupa di fare logging di eventuali messaggi (errore/informazione) che possono verificarsi durante il ciclo
     * di vita del thread.
     *
     * @param message il messaggio che si vuole loggare
     */
    synchronized private void log(String message) {
        System.out.printf("[%s] %s%n", this.getName(), message);
    }

    /**
     * Restituisce lo stack trace in forma testuale di un eccezione/errore {@code throwable}, specificato come
     * parametro.
     *
     * @param throwable l'eccezione/errore di cui si vuole ottenere la rappresentazione testuale del proprio stack trace
     * @return la rappresentazione testaule delllo stack trace dell'eccezione/errore
     */
    private static String throwableStackTraceToString(Throwable throwable) {
        var stringWriter = new StringWriter();
        var printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
