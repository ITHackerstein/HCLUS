package com.davidecarella.hclus.server;

import com.davidecarella.hclus.common.Clustering;
import com.davidecarella.hclus.common.ClusteringStep;
import com.davidecarella.hclus.common.exceptions.ExampleSizeMismatchException;
import com.davidecarella.hclus.server.clustering.Dataset;
import com.davidecarella.hclus.server.clustering.HierarchicalClustering;
import com.davidecarella.hclus.common.Example;
import com.davidecarella.hclus.server.distance.AverageLinkDistance;
import com.davidecarella.hclus.server.distance.ClusterDistance;
import com.davidecarella.hclus.server.distance.SingleLinkDistance;
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
    private final int SUCCESS = 0;
    private final int ERROR = 1;

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
                        case 0 -> loadDataRequest(dataDeserializer, dataSerializer);
                        case 1 -> newClusteringRequest(dataDeserializer, dataSerializer);
                        case 2 -> loadClusteringRequest(dataDeserializer, dataSerializer);
                        case 3 -> getExamplesRequest(dataDeserializer, dataSerializer);
                        case 4 -> closeConnectionRequest(dataDeserializer, dataSerializer);
                        default -> {
                            dataSerializer.serializeInt(ERROR);
                            dataSerializer.serializeString("Richiesta non valida!");
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
    private void loadDataRequest(DataDeserializer dataDeserializer, DataSerializer dataSerializer) throws IOException {
        String tableName = dataDeserializer.deserializeString();

        try {
            this.dataset = new Dataset(tableName);
            dataSerializer.serializeInt(SUCCESS);
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
            dataSerializer.serializeString("I dati non sono stati ancora caricati!");
            return;
        }

        var depth = dataDeserializer.deserializeInt();
        ClusterDistance distance = switch (dataDeserializer.deserializeInt()) {
            case 0 -> new SingleLinkDistance();
            case 1 -> new AverageLinkDistance();
            default -> null;
        };

        if (distance == null) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Tipo di distanza non valida!");
            return;
        }

        String fileName = dataDeserializer.deserializeString();

        Clustering clustering;
        try {
            clustering = HierarchicalClustering.mine(this.dataset, distance, depth);
        } catch (InvalidDepthException | ExampleSizeMismatchException exception) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString(walkThrowable(exception));
            return;
        }

        try {
            HierarchicalClustering.save(clustering, fileName);
        } catch (IOException exception) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString(String.format("Errore durante il salvataggio del dendrogramma: %s!", exception.getMessage()));
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

        String fileName = dataDeserializer.deserializeString();

        Clustering clustering;
        try {
            clustering = HierarchicalClustering.load(fileName, this.dataset);
        } catch (FileNotFoundException exception) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString("Il file inserito non esiste!");
            return;
        } catch (IOException exception) {
            dataSerializer.serializeInt(ERROR);
            dataSerializer.serializeString(String.format("Errore durante il caricamento del dendrogramma: %s!", exception.getMessage()));
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
            if (index < 0 || index >= dataset.getNumberOfExamples()) {
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
