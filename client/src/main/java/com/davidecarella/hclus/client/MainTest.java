package com.davidecarella.hclus.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * <p>Classe principale del progetto.
 *
 * <p>Il client si occuperà di connettersi a un server il cui indirizzo e porta sono specificati come argomenti da
 * linea di comando, manderà una richiesta di caricamento dei dati e in seguito farà scegliere all'utente se caricare
 * un dendrogramma da file o crearne uno nuova da una tabella del database. Nel primo caso viene inviato il nome del
 * file da cui si vuole caricare il dendrogramma (scritto da tastiera dall'utente) e il server risponde con una stringa
 * che rappresenta il dendrogramma. Nel secondo caso, invece, vengono inviate al server profondità e tipo di distanza
 * (scritte da tastiera dall'utente) e il server creerà il dendrogramma e ne invierà la rappresentazione testuale.
 * Infine il client esce.
 */
public class MainTest {
    /**
     * Stream di output del socket.
     */
    private ObjectOutputStream out;

    /**
     * Stream di input del socket.
     */
    private ObjectInputStream in;

    /**
     * Costruisce la classe connettendosi al server con indirizzo {@code ip} e porta {@code port} (specificate come
     * parametro).
     *
     * @param ip l'indirizzo IP del server
     * @param port la porta del server
     * @throws IOException se non si riescono a creare gli stream di IO del socket
     */
    public MainTest(String ip, int port) throws IOException {
        InetAddress addr = InetAddress.getByName(ip);
        System.out.println("addr = " + addr);
        Socket socket = new Socket(addr, port);
        System.out.println(socket);

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Stampa il menu di selezione dell'operazione dopo aver caricato i dati.
     *
     * @return la scelta fatta dall'utente
     */
    private int menu() {
        int answer;

        System.out.println("Scegli una opzione");
        do {
            System.out.println("(1) Carica Dendrogramma da File");
            System.out.println("(2) Apprendi Dendrogramma da Database");
            System.out.print("Risposta:");
            answer = Keyboard.readInt();
        }
        while (answer <= 0 || answer > 2);

        return answer;
    }

    /**
     * Manda al server la richiesta del caricamento dei dati.
     *
     * @throws IOException se si dovessero verificare errori durante la ricezione/invio di oggetti dagli stream di IO
     * @throws ClassNotFoundException se si dovessero verificare errori durante la ricezione/invio di oggetti dagli
     *                                stream di IO
     */
    private void loadDataOnServer() throws IOException, ClassNotFoundException {
        boolean flag = false;
        do {
            System.out.println("Nome tabella:");
            String tableName = Keyboard.readString();
            out.writeObject(0);
            out.writeObject(tableName);
            String risposta = (String) (in.readObject());
            if (risposta.equals("OK")) {
                flag = true;
            } else {
                System.out.println(risposta);
            }
        } while (!flag);
    }

    /**
     * Manda al server la richiesta del caricamento di un dendrogramma dal file.
     *
     * @throws IOException se si dovessero verificare errori durante la ricezione/invio di oggetti dagli stream di IO
     * @throws ClassNotFoundException se si dovessero verificare errori durante la ricezione/invio di oggetti dagli
     *                                stream di IO
     */
    private void loadDendrogramFromFileOnServer() throws IOException, ClassNotFoundException {
        System.out.println("Inserire il nome dell'archivio (comprensivo di estensione):");
        String fileName = Keyboard.readString();

        out.writeObject(2);
        out.writeObject(fileName);
        String risposta = (String) (in.readObject());
        if (risposta.equals("OK")) {
            System.out.println(in.readObject()); // stampo il dendrogramma che il server mi sta inviando
        } else {
            System.out.println(risposta); // stampo il messaggio di errore
        }
    }

    /**
     * Manda al server la richiesta della creazione di un nuovo dendrogramma a partire da una tabella nel database.
     *
     * @throws IOException se si dovessero verificare errori durante la ricezione/invio di oggetti dagli stream di IO
     * @throws ClassNotFoundException se si dovessero verificare errori durante la ricezione/invio di oggetti dagli
     *                                stream di IO
     */
    private void mineDendrogramOnServer() throws IOException, ClassNotFoundException {
        out.writeObject(1);
        System.out.println("Introdurre la profondit  del dendrogramma");
        int depth = Keyboard.readInt();
        out.writeObject(depth);
        int dType = -1;
        do {
            System.out.println("Distanza: single-link (1), average-link (2):");
            dType = Keyboard.readInt();
        } while (dType <= 0 || dType > 2);
        out.writeObject(dType);

        String risposta = (String) (in.readObject());
        if (risposta.equals("OK")) {
            System.out.println(in.readObject()); // stampo il dendrogramma che il server mi sta inviando
            System.out.println("Inserire il nome dell'archivio (comprensivo di estensione):");
            String fileName = Keyboard.readString();
            out.writeObject(fileName);
        } else {
            System.out.println(risposta); // stampo il messaggio di errore
        }
    }

    /**
     * Metodo principale del progetto.
     *
     * @param args argomenti da linea di comando
     */
    public static void main(String[] args) {
        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        MainTest main = null;
        try {
            main = new MainTest(ip, port);

            main.loadDataOnServer();
            int scelta = main.menu();
            if (scelta == 1) {
                main.loadDendrogramFromFileOnServer();
            } else {
                main.mineDendrogramOnServer();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
        }
    }
}


