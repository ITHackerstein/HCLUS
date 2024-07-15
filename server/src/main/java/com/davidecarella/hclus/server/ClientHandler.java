package com.davidecarella.hclus.server;

import com.davidecarella.hclus.common.ClusterDistanceMethod;
import com.davidecarella.hclus.common.Clustering;
import com.davidecarella.hclus.common.exceptions.ExampleSizeMismatchException;
import com.davidecarella.hclus.server.clustering.Dataset;
import com.davidecarella.hclus.server.clustering.HierarchicalClustering;
import com.davidecarella.hclus.common.Example;
import com.davidecarella.hclus.server.distance.*;
import com.davidecarella.hclus.server.exceptions.InvalidClusterIndexException;
import com.davidecarella.hclus.server.exceptions.InvalidDepthException;
import com.davidecarella.hclus.server.exceptions.NoDataException;
import com.davidecarella.hclus.common.serialization.DataDeserializer;
import com.davidecarella.hclus.common.serialization.DataSerializer;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Classe che gestisce la connessione con un client.
 */
public class ClientHandler extends Thread {
    private static final int SUCCESS = 0;
    private static final int ERROR = 1;

    private static final ClusterDistance[] AVAILABLE_DISTANCE_METHODS = new ClusterDistance[] {
        new AverageLinkDistance(),
        new CentroidLinkDistance(),
        new CompleteLinkDistance(),
        new MedianLinkDistance(),
        new SingleLinkDistance(),
        new WardLinkDistance(),
        new WeightedAverageLinkDistance()
    };

    private static final String CLUSTERINGS_DIRECTORY = "clusterings";
    private static final String CLUSTERING_NAME_REGEX = "^[a-zA-Z0-9_]+$";
    private static final String CLUSTERING_FILE_REGEX = "^[a-zA-Z0-9_]+\\.hclus$";

    /**
     * Il socket per la connessione con il client.
     */
    private final Socket clientSocket;

    /**
     * I dati eventualmente caricati dal server.
     */
    private Dataset dataset = null;

    /**
     * Costruisce il gestore del client e lancia il thread associato a esso a partire da {@code clientSocket}, fornito
     * come parametro.
     *
     * @param clientSocket il socket la connessione con il client
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
     * Metodo lanciato al lancio del thread che gestisce le richieste del client.
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
                        case 1 -> newClusteringRequest(dataDeserializer, dataSerializer);
                        case 2 -> loadClusteringRequest(dataDeserializer, dataSerializer);
                        case 3 -> getExamplesRequest(dataDeserializer, dataSerializer);
                        case 4 -> getClusterDistanceMethodsRequest(dataDeserializer, dataSerializer);
                        case 5 -> getSavedClusterings(dataDeserializer, dataSerializer);
                        case 6 -> closeConnectionRequest(dataDeserializer, dataSerializer);
                        default -> {
                            dataSerializer.serializeInt(ERROR);
                            dataSerializer.serializeString("Richiesta non valida");
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
     * Gestisce la richiesta del caricamento dei dati.
     *
     * @param dataDeserializer il <i>deserializzatore</i> dei dati inviati dal client
     * @param dataSerializer il <i>serializzatore</i> dei dati inviati dal server
     */
    private void loadDatasetRequest(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        String tableName = dataDeserializer.deserializeString();

        try {
            this.dataset = new Dataset(tableName);
            dataSerializer.serializeInt(SUCCESS);
            dataSerializer.serializeInt(this.dataset.getExampleCount());
        } catch (NoDataException exception) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString(walkThrowable(exception));
        }
    }

    /**
     * Gestisce la richiesta per la creazione di un nuovo clustering dei dati.
     *
     * @param dataDeserializer il <i>deserializzatore</i> dei dati inviati dal client
     * @param dataSerializer il <i>serializzatore</i> dei dati inviati dal server
     */
    private void newClusteringRequest(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        if (this.dataset == null) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("I dati non sono stati ancora caricati");
            return;
        }

        var depth = dataDeserializer.deserializeInt();
        var distanceId = dataDeserializer.deserializeInt();
        if (distanceId < 0 || distanceId >= AVAILABLE_DISTANCE_METHODS.length) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Tipo di distanza non valida");
            return;
        }
        var name = dataDeserializer.deserializeString();

        if (!name.matches(CLUSTERING_NAME_REGEX)) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Nome non valido");
            return;
        }

        Clustering clustering;
        try {
            clustering = HierarchicalClustering.mine(this.dataset, AVAILABLE_DISTANCE_METHODS[distanceId], depth);
        } catch (InvalidDepthException | ExampleSizeMismatchException exception) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString(walkThrowable(exception));
            return;
        }

        try {
            var clusteringsDir = new File(CLUSTERINGS_DIRECTORY);
            if (!clusteringsDir.exists()) {
                if (!clusteringsDir.mkdirs()) {
                    throw new IOException("Non è stato possibile creare la directory per i clustering");
                }
            } else if (!clusteringsDir.isDirectory()) {
                throw new IOException(String.format("Esiste già un file con nome '%s'", CLUSTERINGS_DIRECTORY));
            }

            var path = clusteringsDir.toPath().resolve(String.format("%s.hclus", name));
            HierarchicalClustering.save(clustering, path.toString());
        } catch (IOException exception) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString(String.format("Errore durante il salvataggio del dendrogramma: %s", exception.getMessage()));
            return;
        }

        dataSerializer.serializeInt(SUCCESS);
        dataSerializer.serializeClustering(clustering);
    }

    /**
     * Gestisce la richiesta di caricamento di un clustering da un file.
     *
     * @param dataDeserializer il <i>deserializzatore</i> dei dati inviati dal client
     * @param dataSerializer il <i>serializzatore</i> dei dati inviati dal server
     */
    private void loadClusteringRequest(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        if (this.dataset == null) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("I dati non sono stati ancora caricati!");
            return;
        }

        var name = dataDeserializer.deserializeString();
        if (!name.matches(CLUSTERING_NAME_REGEX)) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Nome non valido!");
            return;
        }

        Clustering clustering;
        try {
            clustering = HierarchicalClustering.load(String.format("%s/%s.hclus", CLUSTERINGS_DIRECTORY, name), this.dataset);
        } catch (FileNotFoundException exception) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Il file inserito non esiste");
            return;
        } catch (IOException exception) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString(String.format("Errore durante il caricamento del dendrogramma: %s", exception.getMessage()));
            return;
        } catch (InvalidDepthException | InvalidClusterIndexException exception) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString(exception.getMessage());
            return;
        }

        dataSerializer.serializeInt(SUCCESS);
        dataSerializer.serializeClustering(clustering);
    }

    /**
     * Gestisce la richiesta di recupero degli esempi.
     *
     * @param dataDeserializer il <i>deserializzatore</i> dei dati inviati dal client
     * @param dataSerializer il <i>serializzatore</i> dei dati inviati dal server
     */
    private void getExamplesRequest(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        if (this.dataset == null) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("I dati non sono stati ancora caricati!");
            return;
        }

        var examples = new ArrayList<Example>();
        var indexCount = dataDeserializer.deserializeInt();
        while (indexCount-- > 0) {
            var index = dataDeserializer.deserializeInt();
            if (index < 0 || index >= dataset.getExampleCount()) {
                dataSerializer.serializeInt(ERROR);
                dataSerializer.serializeString("Uno o più indici non validi!");
                return;
            }

            examples.add(dataset.getExample(index));
        }

        dataSerializer.serializeInt(SUCCESS);
        for (var example : examples) {
            dataSerializer.serializeExample(example);
        }
    }

    /**
     * Gestisce la richiesta di recupero dei metodi di distanze fra cluster.
     *
     * @param dataDeserializer il <i>deserializzatore</i> dei dati inviati dal client
     * @param dataSerializer il <i>serializzatore</i> dei dati inviati dal server
     * @throws IOException in caso di errori di I/O
     */
    private void getClusterDistanceMethodsRequest(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        dataSerializer.serializeInt(SUCCESS);
        dataSerializer.serializeInt(AVAILABLE_DISTANCE_METHODS.length);
        for (int i = 0; i < AVAILABLE_DISTANCE_METHODS.length; ++i) {
            dataSerializer.serializeClusterDistance(new ClusterDistanceMethod(i, AVAILABLE_DISTANCE_METHODS[i].getName()));
        }
    }

    private void getSavedClusterings(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        var clusteringsDir = new File(CLUSTERINGS_DIRECTORY);
        if (!clusteringsDir.exists()) {
            if (!clusteringsDir.mkdirs()) {
                throw new IOException("Non è stato possibile creare la directory per i clustering");
            }
        } else if (!clusteringsDir.isDirectory()) {
            throw new IOException(String.format("Esiste già un file con nome '%s'", CLUSTERINGS_DIRECTORY));
        }

        var clusteringNames = clusteringsDir.list((dir, name) -> name.matches(CLUSTERING_FILE_REGEX));
        if (clusteringNames == null) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Errore durante la lettura dei clustering salvati");
            return;
        }

        dataSerializer.serializeInt(SUCCESS);
        dataSerializer.serializeInt(clusteringNames.length);
        for (var clusteringName : clusteringNames) {
            dataSerializer.serializeString(clusteringName.substring(0, clusteringName.indexOf(".hclus")));
        }
    }

    /**
     * Gestisce la richiesta di chiusura della connessione.
     *
     * @param dataDeserializer il <i>deserializzatore</i> dei dati inviati dal client
     * @param dataSerializer il <i>serializzatore</i> dei dati inviati dal server
     */
    private void closeConnectionRequest(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
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
     * Semplice metodo per fare logging di eventuali messaggi (errore/informazione) che possono occorrere durante il
     * ciclo di vita del thread.
     *
     * @param message il messaggio che si vuole stampare
     */
    synchronized private void log(String message) {
        System.out.printf("[%s] %s%n", this.getName(), message);
    }

    /**
     * Dato un errore/eccezione, {@code throwable}, specificato come parametro stampa il suo messaggio e se contiene
     * degli altri errori/eccezioni che lo causano allora provvede a stampare anche loro.
     *
     * @param throwable l'errore/eccezione che si vuole stampare
     */
    private static String walkThrowable(Throwable throwable) {
        if (throwable.getCause() == null) {
            return String.format("%s%n", throwable.getMessage());
        }

        var stringBuilder = new StringBuilder();

        var current = throwable;
        int level = 0;

        while (current != null) {
            int spaceCount = Math.max(level - 1, 0);
            stringBuilder.append(" ".repeat(spaceCount));
            if (level != 0) {
                stringBuilder.append('\\');
            }
            stringBuilder.append("- ").append(current.getMessage()).append(System.lineSeparator());

            current = current.getCause();
            ++level;
        }

        return stringBuilder.toString();
    }
}
