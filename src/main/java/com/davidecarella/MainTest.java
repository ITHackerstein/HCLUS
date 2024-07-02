package com.davidecarella;

import com.davidecarella.clustering.HierarchicalClusterMiner;
import com.davidecarella.data.Data;
import com.davidecarella.distance.AverageLinkDistance;
import com.davidecarella.distance.ClusterDistance;
import com.davidecarella.distance.SingleLinkDistance;
import com.davidecarella.exceptions.InvalidDepthException;
import com.davidecarella.exceptions.InvalidSizeException;
import com.davidecarella.utils.Keyboard;

/**
 * Classe utilizzata per testare le classi realizzate.
 */
public class MainTest {
    private static ClusterDistance chooseDistance() {
        System.out.println("Seleziona il tipo di distanza che si vuole utilizzare:");
        System.out.println("  1. Single-Link");
        System.out.println("  2. Average-Link");

        int choice;
        while (true) {
            System.out.print("Scelta: ");
            var input = Keyboard.readString();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                System.out.println("Scelta non valida!");
                continue;
            }

            if (choice < 1 || choice > 2) {
                System.out.println("Scelta non valida!");
            } else {
                break;
            }
        }

        return switch (choice) {
            case 1 -> new SingleLinkDistance();
            case 2 -> new AverageLinkDistance();
            // NOTE: This can never happen
            default -> null;
        };
    }

    /**
     * Metodo principale dell'applicazione.
     *
     * @param args argomenti forniti a linea di comando
     */
    public static void main(String[] args) {
        Data data = new Data();
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

            HierarchicalClusterMiner clustering = new HierarchicalClusterMiner(depth);
            try {
                clustering.mine(data, distance);
                System.out.println(clustering);
                System.out.println(clustering.toString(data));
                break;
            } catch (InvalidDepthException exception) {
                System.out.printf("Profondità non valida: %s!%n", exception.getMessage());
            } catch (InvalidSizeException exception) {
                System.out.printf("I dati forniti non sono validi: %s!%n", exception.getMessage());
                return;
            }
        }
    }
}