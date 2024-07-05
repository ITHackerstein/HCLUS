package com.davidecarella.hclus.server;

import com.davidecarella.hclus.server.clustering.HierarchicalClusterMiner;
import com.davidecarella.hclus.server.data.Data;
import com.davidecarella.hclus.server.distance.AverageLinkDistance;
import com.davidecarella.hclus.server.distance.ClusterDistance;
import com.davidecarella.hclus.server.distance.SingleLinkDistance;
import com.davidecarella.hclus.server.exceptions.InvalidDepthException;
import com.davidecarella.hclus.server.exceptions.InvalidSizeException;
import com.davidecarella.hclus.server.exceptions.NoDataException;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Classe che gestisce la connessione con un client.
 */
public class ServerOneClient extends Thread {
    /**
     * Il socket per la connessione con il client.
     */
    private final Socket clientSocket;

    /**
     * I dati eventualmente caricati dal server.
     */
    private Data data = null;

    /**
     * Costruisce il gestore del client e lancia il thread associato a esso a partire da {@code clientSocket}, fornito
     * come parametro.
     *
     * @param clientSocket il socket la connessione con il client
     */
    public ServerOneClient(Socket clientSocket) {
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

        try (var inputStream = new ObjectInputStream(this.clientSocket.getInputStream());
             var outputStream = new ObjectOutputStream(this.clientSocket.getOutputStream()))
        {
            while (true) {
                int requestType = -1;
                try {
                    var object = inputStream.readObject();
                    if (!(object instanceof Integer)) {
                        log("Tipo di richiesta non valido!");
                        continue;
                    }

                    requestType = (int) object;
                } catch (SocketException | EOFException ignored) {
                    return;
                } catch (IOException | ClassNotFoundException exception) {
                    log(String.format("Errore durante la lettura del tipo di richiesta: %s!", exception.getMessage()));
                    return;
                }

                switch (requestType) {
                    case 0 -> loadDataRequest(inputStream, outputStream);
                    case 1 -> mineRequest(inputStream, outputStream);
                    case 2 -> loadDendrogramFromFileRequest(inputStream, outputStream);
                    default -> {
                        try {
                            outputStream.writeObject("Richiesta sconosciuta!");
                        } catch (SocketException | EOFException ignored) {
                            return;
                        } catch (IOException exception) {
                            log(String.format("Errore durante la scrittura del messaggio: %s!", exception.getMessage()));
                            return;
                        }
                    }
                }
            }
        } catch (IOException exception) {
            log("Errore durante la creazione/chiusura delle stream di I/O per il socket!");
        } finally {
            tryCloseSocket();
        }
    }

    /**
     * Gestisce la richiesta del caricamento dei dati da una tabella il cui nome viene inviato dal client.
     *
     * @param inputStream lo stream di input del socket
     * @param outputStream lo stream di output del socket
     */
    private void loadDataRequest(ObjectInputStream inputStream, ObjectOutputStream outputStream) {
        String tableName = null;
        try {
            var object = inputStream.readObject();
            if (!(object instanceof String)) {
                log("Nome della tabella non valido!");
                return;
            }

            tableName = (String) object;
        } catch (SocketException | EOFException ignored) {
        } catch (IOException | ClassNotFoundException exception) {
            log(String.format("Errore durante la lettura/scrittura di oggetti: %s!", exception.getMessage()));
        }

        try {
            try {
                this.data = new Data(tableName);
                outputStream.writeObject("OK");
            } catch (NoDataException exception) {
                outputStream.writeObject(walkThrowable(exception));
            }
        } catch (SocketException | EOFException ignored) {
        } catch (IOException exception) {
            log(String.format("Errore durante la scrittura del messaggio: %s!", exception.getMessage()));
        }
    }

    /**
     * <p>Gestisce la richiesta di mining dei dati.
     *
     * <p>Il client dovrà inviare la profondità del dendrogramma seguita dal tipo di distanza:
     * <ul>
     *     <li>{@code 1}: single-link</li>
     *     <li>{@code 2}: average-link</li>
     * </ul>
     * A quel punto se tutto va a buon fine il server manda una stringa che rappresenta il dendrogramma generato
     * e il client invia il percorso del file dove si salverà il dendrogramma generato.
     *
     * @param inputStream lo stream di input del socket
     * @param outputStream lo stream di output del socket
     */
    private void mineRequest(ObjectInputStream inputStream, ObjectOutputStream outputStream) {
        if (this.data == null) {
            try {
                outputStream.writeObject("I dati non sono stati ancora caricati!");
            } catch (SocketException | EOFException ignored) {
            } catch (IOException exception) {
                log(String.format("Errore durante la scrittura del messaggio: %s!", exception.getMessage()));
            }

            return;
        }

        int depth = 0;
        ClusterDistance distance = null;
        try {
            Object object;

            object = inputStream.readObject();
            if (!(object instanceof Integer)) {
                outputStream.writeObject("Profondità del dendrogramma non valida!");
                return;
            }

            depth = (int) object;

            object = inputStream.readObject();
            if (!(object instanceof Integer distanceType) || distanceType < 1 || distanceType > 2) {
                outputStream.writeObject("Tipo di distanza non valida!");
                return;
            }

            distance = switch (distanceType) {
                case 1 -> new SingleLinkDistance();
                case 2 -> new AverageLinkDistance();
                default -> throw new IllegalStateException("Valore inaspettato: " + distanceType);
            };
        } catch (SocketException | EOFException ignored) {
            return;
        } catch (IOException | ClassNotFoundException exception) {
            log(String.format("Errore durante la lettura/scrittura di oggetti: %s!", exception.getMessage()));
            return;
        }

        var miner = new HierarchicalClusterMiner(depth);
        try {
            try {
                miner.mine(this.data, distance);

                outputStream.writeObject("OK");
                outputStream.writeObject(miner.toString(this.data));

                var object = inputStream.readObject();
                if (!(object instanceof String fileName)) {
                    outputStream.writeObject("Percorso del file non valido!");
                    return;
                }

                try {
                    miner.salva(fileName);
                } catch (IOException exception) {
                    log(String.format("Errore durante il salvataggio del dendrogramma: %s!", exception.getMessage()));
                }
            } catch (InvalidDepthException | InvalidSizeException exception) {
                outputStream.writeObject(walkThrowable(exception));
            }
        } catch (SocketException | EOFException ignored) {
        } catch (IOException | ClassNotFoundException exception) {
            log(String.format("Errore durante la lettura/scrittura del percorso del file dove salvare il dendrogramma: %s!", exception.getMessage()));
        }
    }

    /**
     * <p>Gestisce la richiesta di caricamento di un dendrogramma da un file presente sul server.
     *
     * <p>Il client caricherà il percorso del file che desidera caricare e il server proverà a inviare come risposta
     * la stringa che rappresenta il dendrogramma contenuto nel file.
     *
     * @param inputStream lo stream di input del socket
     * @param outputStream lo stream di output del socket
     */
    private void loadDendrogramFromFileRequest(ObjectInputStream inputStream, ObjectOutputStream outputStream) {
        if (this.data == null) {
            try {
                outputStream.writeObject("I dati non sono stati ancora caricati!");
            } catch (SocketException | EOFException ignored) {
            } catch (IOException exception) {
                log(String.format("Errore durante la scrittura del messaggio: %s!", exception.getMessage()));
            }

            return;
        }

        String fileName = null;
        try {
            var object = inputStream.readObject();
            if (!(object instanceof String)) {
                outputStream.writeObject("Nome del file non valido!");
                return;
            }

            fileName = (String) object;
        } catch (SocketException | EOFException ignored) {
            return;
        } catch (IOException | ClassNotFoundException exception) {
            log(String.format("Errore durante la lettura/scrittura di oggetti: %s!", exception.getMessage()));
            return;
        }

        HierarchicalClusterMiner miner = null;
        try {
            try {
                miner = HierarchicalClusterMiner.load(fileName);

                if (miner.getDendrogramDepth() > this.data.getNumberOfExamples()) {
                    outputStream.writeObject("Profondità del dendrogramma non valida!");
                    return;
                }

                outputStream.writeObject("OK");
                outputStream.writeObject(miner.toString(this.data));
            } catch (FileNotFoundException exception) {
                outputStream.writeObject("Il file inserito non esiste!");
            } catch (IOException | ClassNotFoundException exception) {
                outputStream.writeObject(String.format("Errore durante il caricamento del dendrogramma: %s!", exception.getMessage()));
            }
        } catch (SocketException | EOFException ignored) {
        } catch (IOException exception) {
            log(String.format("Errore durante la scrittura di oggetti: %s!", exception.getMessage()));
        }
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
    private void log(String message) {
        // FIXME: We should have synchronized logging since we are using concurrent threads
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
