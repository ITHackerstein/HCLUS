package com.davidecarella.hclus.server;

/**
 * <p>Classe principale del progetto.
 *
 * <p>Si occupa di creare un server in ascolto sulla porta specificata come argomento da linea di comando.
 *
 * @see Server
 */
public class Main {
    /**
     * Costruttore di default.
     */
    private Main() {}

    /**
     * Metodo principale del progetto.
     *
     * @param args gli argomenti passati da linea di comando
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java -jar server.jar <port>");
            return;
        }

        try {
           int port = Integer.parseInt(args[0]);
           if (port < 0 || port > 65535) {
               System.out.println("Porta non valida!");
               return;
           }

           var server = new Server(port);
           server.loop();
        } catch (NumberFormatException exception) {
            System.out.println("Porta non valida!");
        }
    }
}