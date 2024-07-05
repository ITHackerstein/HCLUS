package com.davidecarella.hclus.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Il server vero e proprio che si occuperà di mettersi in ascolto su una porta specificata e creare un gestore per ogni
 * client.
 *
 * @see ServerOneClient
 */
public class MultiServer {
    /**
     * La porta su cui il server si mette in ascolto
     */
    private final int port;

    /**
     * Costruisce il server mettendosi in ascolto sulla porta specificata, {@code port}, specificata come parametro.
     *
     * @param port la porta su cui si mette in ascolto il server
     */
    public MultiServer(int port) {
        this.port = port;
    }

    /**
     * Metodo che mette in ascolto il server per connessioni da parte di altri client. Per ogni nuova connessione
     * creerà un'istanza di {@link ServerOneClient} per gestire le sue richieste.
     */
    public void loop() {
        try (var serverSocket = new ServerSocket(this.port)) {
            System.out.printf("In ascolto sulla porta %d%n", this.port);

            while (true) {
                try {
                    var clientSocket = serverSocket.accept();
                    new ServerOneClient(clientSocket);
                } catch (IOException exception) {
                    System.out.println("Errore durante l'attesa per una connessione di un client!");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Errore durante la creazione del socket per il server!");
        }
    }
}
