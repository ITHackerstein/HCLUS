package com.davidecarella;

import com.davidecarella.clustering.HierarchicalClusterMiner;
import com.davidecarella.data.Data;
import com.davidecarella.distance.AverageLinkDistance;
import com.davidecarella.distance.ClusterDistance;
import com.davidecarella.distance.SingleLinkDistance;
import com.davidecarella.exceptions.InvalidDepthException;
import com.davidecarella.exceptions.InvalidSizeException;
import com.davidecarella.exceptions.NoDataException;
import com.davidecarella.utils.Keyboard;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Classe utilizzata per testare le classi realizzate.
 */
public class MainTest {
    private static int readChoice(String prompt, int minimum, int maximum) {
        int choice;
        while (true) {
            System.out.print(prompt);
            var input = Keyboard.readString();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                System.out.println("Scelta non valida!");
                continue;
            }

            if (choice < minimum || choice > maximum) {
                System.out.println("Scelta non valida!");
            } else {
                break;
            }
        }

        return choice;
    }

    private static Data loadData() {
        while (true) {
            System.out.print("Inserisci il nome della tabella dal quale vuoi caricare i dati: ");
            var tableName = Keyboard.readString();

            try {
                return new Data(tableName);
            } catch (NoDataException exception) {
                System.out.println("Errore durante il caricamento dei dati dalla tabella specificata: ");

                Throwable current = exception;
                int indent = 2;
                while (current != null) {
                    System.out.print(" ".repeat(indent));
                    System.out.println(current.toString());

                    current = current.getCause();
                    indent += 2;
                }
            }
        }
    }

    private static ClusterDistance chooseDistance() {
        System.out.println("Seleziona il tipo di distanza che si vuole utilizzare:");
        System.out.println("  1. Single-Link");
        System.out.println("  2. Average-Link");
        return switch (readChoice("Scelta: ", 1, 2)) {
            case 1 -> new SingleLinkDistance();
            case 2 -> new AverageLinkDistance();
            // NOTE: This can never happen
            default -> null;
        };
    }

    private static void loadMinerFromFile(Data data) {
        while (true) {
            System.out.print("Inserisci il percorso del miner: ");
            var fileName = Keyboard.readString();

            try {
                var miner = HierarchicalClusterMiner.load(fileName);
                if (miner.getDendrogramDepth() > data.getNumberOfExamples()) {
                    System.out.println("IL miner inserito ha una profondità non valida!");
                    continue;
                }

                System.out.println(miner);
                System.out.println(miner.toString(data));
                return;
            } catch (FileNotFoundException exception) {
                System.out.println("Il file inserito non esiste!");
            } catch (IOException | ClassNotFoundException exception) {
                System.out.printf("Errore durante la lettura del file: %s!%n", exception.getMessage());
            }
        }
    }

    private static void newMiner(Data data) {
        var distance = chooseDistance();

        while (true) {
            System.out.print("Inserisci la profondità del dendrogramma: ");
            var input = Keyboard.readString();
            int depth;
            try {
                depth = Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                System.out.println("Profondità non valida!");
                continue;
            }

            if (depth <= 0) {
                System.out.println("Profondità non valida!");
                continue;
            }

            try {
                var miner = new HierarchicalClusterMiner(depth);
                miner.mine(data, distance);
                System.out.println(miner);
                System.out.println(miner.toString(data));

                while (true) {
                    System.out.print("Inserisci il percorso dove si vuole salvare il miner: ");
                    var fileName = Keyboard.readString();

                    try {
                        miner.salva(fileName);
                        return;
                    } catch (IOException exception) {
                        System.out.printf("Errore durante il salvataggio su file: %s%n", exception.getMessage());
                    }
                }
            } catch (InvalidDepthException exception) {
                System.out.printf("Profondità non valida: %s!%n", exception.getMessage());
            } catch (InvalidSizeException exception) {
                System.out.printf("I dati forniti non sono validi: %s!%n", exception.getMessage());
                return;
            }
        }
    }

    /**
     * Metodo principale dell'applicazione.
     *
     * @param args argomenti forniti a linea di comando
     */
    public static void main(String[] args) {
        var data = loadData();
        System.out.println(data);

        try {
            double[][] distanceMatrix = data.distance();

            System.out.println("Distance matrix:\n");
            for (int i = 0; i < distanceMatrix.length; ++i) {
                for (int j = 0; j < distanceMatrix.length; ++j)
                    System.out.print(distanceMatrix[i][j]+"\t");
                System.out.println();
            }
        } catch (InvalidSizeException exception) {
            System.out.println(exception.getMessage());
            return;
        }

        System.out.println("Selezionare da dove si vuole caricare il dendrogramma: ");
        System.out.println("  1. Da file");
        System.out.println("  2. Nuovo");
        switch (readChoice("Scelta: ", 1, 2)) {
            case 1 -> loadMinerFromFile(data);
            case 2 -> newMiner(data);
        }
    }
}