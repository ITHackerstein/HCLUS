package com.davidecarella.hclus.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Il server HCLUS che accetterà connessione da diversi client gestisti ognuno con un thread diverso.
 *
 * @see ClientHandler
 */
public class Server {
    /**
     * La porta su cui il server si mette in ascolto
     */
    private final int port;

    /**
     * Costruisce il server mettendosi in ascolto sulla porta specificata, {@code port}, specificata come parametro.
     *
     * @param port la porta su cui si mette in ascolto il server
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Il ciclo principale del server. Si mette in ascolto per connessione da parte di altri client e per ciascuna di
     * essa crea un'istanza di un {@link ClientHandler} per gestire le sue richieste.
     */
    public void loop() {
        try (var serverSocket = new ServerSocket(this.port)) {
            System.out.printf("In ascolto sulla porta %d%n", this.port);

            while (true) {
                try {
                    var clientSocket = serverSocket.accept();
                    new ClientHandler(clientSocket);
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
