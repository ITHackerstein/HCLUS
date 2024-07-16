package com.davidecarella.hclus.client.gui;

import com.davidecarella.hclus.client.communication.ServerConnection;
import com.davidecarella.hclus.client.exceptions.ServerException;
import com.davidecarella.hclus.common.ClusterDistanceMethod;
import com.davidecarella.hclus.common.Clustering;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * <p>La finestra principale del client.
 *
 * <p>Possiede tre schede:
 * <ol>
 *     <li>la scheda di connessione: permette di connettersi al server;</li>
 *     <li>la scheda del dataset: una volta connessi, permette di caricare un dataset sul server;</li>
 *     <li>la scheda del clustering: una volta connessi e caricati i dati, permette di creare o caricare un clustering.</li>
 * </ol>
 * Al di sotto delle tre schede troviamo il {@link DendrogramViewerWidget widget per la visualizzazione del dendrogramma}.
 */
public class MainWindow extends JFrame {
    /**
     * Il widget per la visualizzazione delle tre schede.
     */
    private JTabbedPane tbp_controls;

    /**
     * Il contenitore della scheda di connessione.
     */
    private JPanel pnl_connection;

    /**
     * L'etichetta per il campo dell'indirizzo.
     */
    private JLabel lbl_address;

    /**
     * Il campo dell'indirizzo.
     */
    private JTextField txt_address;

    /**
     * L'etichetta per il campo della porta.
     */
    private JLabel lbl_port;

    /**
     * Il campo della porta.
     */
    private JTextField txt_port;

    /**
     * Il widget per visualizzare lo stato della connessione.
     */
    private ConnectionStatusWidget connectionStatusWidget;

    /**
     * Il pulsante per connettersi al server.
     */
    private JButton btn_connect;

    /**
     * Il contenitore della scheda del dataset.
     */
    private JPanel pnl_dataset;

    /**
     * L'etichetta per il campo del nome della tabella.
     */
    private JLabel lbl_tableName;

    /**
     * Il contenitore del nome della tabella.
     */
    private JPanel pnl_tableName;

    /**
     * Il campo del nome della tabella.
     */
    private JTextField txt_tableName;

    /**
     * Il pulsante per mostrare i dataset disponibili sul server.
     */
    private JButton btn_showAvailableDatasets;

    /**
     * Il pulsante per caricare il dataset sul server.
     */
    private JButton btn_loadDataset;

    /**
     * L'etichetta per le informazioni sul dataset caricato.
     */
    private JLabel lbl_loadedDataset;

    /**
     * L'etichetta che contiene le informazioni sul dataset caricato.
     */
    private JLabel lbl_loadedDatasetInfo;

    /**
     * Il contenitore della scheda del clustering.
     */
    private JPanel pnl_clustering;

    /**
     * L'etichetta per la checkbox del nuovo clustering.
     */
    private JLabel lbl_newClustering;

    /**
     * La checkbox del nuovo clustering.
     */
    private JCheckBox chk_newClustering;

    /**
     * L'etichetta per il nome del clustering.
     */
    private JLabel lbl_clusteringName;

    /**
     * Il contenitore del nome del clustering.
     */
    private JPanel pnl_clusteringName;

    /**
     * Il campo del nome del clustering.
     */
    private JTextField txt_clusteringName;

    /**
     * Il pulsante per mostrare i clustering salvati sul server.
     */
    private JButton btn_showSavedClusterings;

    /**
     * L'etichetta per il metodo del calcolo della distanza utilizzato.
     */
    private JLabel lbl_distance;

    /**
     * Il modello dati per la combobox del metodo del calcolo della distanza.
     */
    private DefaultComboBoxModel<ClusterDistanceMethod> distanceModel;

    /**
     * Il combobox del metodo del calcolo della distanza
     */
    private JComboBox<ClusterDistanceMethod> cmb_distance;

    /**
     * L'etichetta per il campo della profondità.
     */
    private JLabel lbl_depth;

    /**
     * IL modello dati per il campo della profondità.
     */
    private SpinnerNumberModel depthModel;

    /**
     * Il campo della profondità.
     */
    private JSpinner spn_depth;

    /**
     * Il pulsante per estrarre il clustering.
     */
    private JButton btn_mine;

    /**
     * Il widget per la visualizzazione del dendrogramma.
     */
    private DendrogramViewerWidget dendrogramViewerWidget;

    /**
     * Costruisce la finestra principale del programma.
     */
    public MainWindow() {
        super("HCLUS - Client");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.createConnectionTab();
        this.createDatasetTab();
        this.createClusteringTab();
        this.dendrogramViewerWidget = new DendrogramViewerWidget();
        this.dendrogramViewerWidget.setEnabled(false);

        this.tbp_controls = new JTabbedPane();
        this.tbp_controls.addTab("Connessione", this.pnl_connection);
        this.tbp_controls.addTab("Dataset", this.pnl_dataset);
        this.tbp_controls.addTab("Clustering", this.pnl_clustering);
        this.tbp_controls.setEnabledAt(1, false);
        this.tbp_controls.setEnabledAt(2, false);

        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
        this.getContentPane().add(this.tbp_controls);
        this.getContentPane().add(this.dendrogramViewerWidget);

        this.createEventListeners();

        this.setMinimumSize(new Dimension(400, 600));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
     * Crea la scheda di connessione.
     */
    private void createConnectionTab() {
        this.lbl_address = new JLabel("Indirizzo");
        this.txt_address = new JTextField();
        this.lbl_port = new JLabel("Porta");
        this.txt_port = new JTextField();
        this.btn_connect = new JButton("Connetti");
        this.connectionStatusWidget = new ConnectionStatusWidget();

        this.pnl_connection = new JPanel();
        this.pnl_connection.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        var layout = new GridBagLayout();
        layout.columnWidths = new int[]{0, 0, 0};
        layout.rowHeights = new int[]{0, 0, 0, 0};
        layout.columnWeights = new double[]{0.0, 1.0, 1e-4};
        layout.rowWeights = new double[]{0.0, 0.0, 0.0, 1e-4};
        this.pnl_connection.setLayout(layout);

        this.pnl_connection.add(this.lbl_address, new GridBagConstraints(
            0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5),
            0, 0
        ));

        this.pnl_connection.add(this.txt_address, new GridBagConstraints(
            1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0),
            0, 0
        ));

        this.pnl_connection.add(this.lbl_port, new GridBagConstraints(
            0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5),
            0, 0
        ));

        this.pnl_connection.add(this.txt_port, new GridBagConstraints(
            1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0),
            0, 0
        ));

        this.pnl_connection.add(this.connectionStatusWidget, new GridBagConstraints(
            0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.CENTER,
            new Insets(0, 0, 0, 5), 0, 0
        ));

        this.pnl_connection.add(this.btn_connect, new GridBagConstraints(
            1, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0
        ));
    }

    /**
     * Crea la scheda del dataset.
     */
    private void createDatasetTab() {
        this.lbl_tableName = new JLabel("Tabella");
        this.pnl_tableName = new JPanel();
        this.txt_tableName = new JTextField();
        this.btn_showAvailableDatasets = new JButton("...");
        this.btn_loadDataset = new JButton("Carica");
        this.lbl_loadedDataset = new JLabel("Attuale");
        this.lbl_loadedDataset.setVisible(false);
        this.lbl_loadedDatasetInfo = new JLabel();
        this.lbl_loadedDatasetInfo.setVisible(false);
        this.lbl_loadedDatasetInfo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, this.lbl_loadedDatasetInfo.getFont().getSize()));

        {
            var layout = new GridBagLayout();
            layout.columnWidths = new int[]{0, 0, 0};
            layout.rowHeights = new int[]{0, 0};
            layout.columnWeights = new double[]{1.0, 0.0, 1e-4};
            layout.rowWeights = new double[]{0.0, 1e-4};
            this.pnl_tableName.setLayout(layout);
        }

        this.pnl_tableName.add(this.txt_tableName, new GridBagConstraints(
            0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 5), 0, 0
        ));

        this.pnl_tableName.add(this.btn_showAvailableDatasets, new GridBagConstraints(
            1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 0, 0), 0, 0
        ));

        this.btn_showAvailableDatasets.setPreferredSize(new Dimension(
            this.btn_showAvailableDatasets.getPreferredSize().width, this.txt_tableName.getPreferredSize().height
        ));

        this.pnl_dataset = new JPanel();
        this.pnl_dataset.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        var layout = new GridBagLayout();
        layout.columnWidths = new int[]{0, 0, 0};
        layout.rowHeights = new int[]{0, 0, 0, 0};
        layout.columnWeights = new double[]{0.0, 1.0, 1e-4};
        layout.rowWeights = new double[]{0.0, 0.0, 0.0, 1e-4};
        this.pnl_dataset.setLayout(layout);

        this.pnl_dataset.add(this.lbl_tableName, new GridBagConstraints(
            0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0
        ));

        this.pnl_dataset.add(this.pnl_tableName, new GridBagConstraints(
            1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0
        ));

        this.pnl_dataset.add(this.btn_loadDataset, new GridBagConstraints(
            1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0
        ));

        this.pnl_dataset.add(this.lbl_loadedDataset, new GridBagConstraints(
            0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 0, 5), 0, 0
        ));

        this.pnl_dataset.add(this.lbl_loadedDatasetInfo, new GridBagConstraints(
            1, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 0, 0), 0, 0
        ));
    }

    /**
     * Crea la scheda del clustering.
     */
    private void createClusteringTab() {
        this.lbl_newClustering = new JLabel("Nuovo clustering");
        this.chk_newClustering = new JCheckBox();
        this.chk_newClustering.setSelected(true);
        this.lbl_clusteringName = new JLabel("Nome");
        this.txt_clusteringName = new JTextField();
        this.btn_showSavedClusterings = new JButton("...");
        this.lbl_distance = new JLabel("Distanza");
        this.distanceModel = new DefaultComboBoxModel<>();
        this.cmb_distance = new JComboBox<>(this.distanceModel);
        this.lbl_depth = new JLabel("Profondità");
        this.depthModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
        this.spn_depth = new JSpinner(depthModel);
        this.btn_mine = new JButton("Estrai");

        this.pnl_clusteringName = new JPanel();
        {
            var layout = new GridBagLayout();
            layout.columnWidths = new int[]{0, 0, 0};
            layout.rowHeights = new int[]{0, 0};
            layout.columnWeights = new double[]{1.0, 0.0, 1e-4};
            layout.rowWeights = new double[]{0.0, 1e-4};
            this.pnl_clusteringName.setLayout(layout);
        }

        this.pnl_clusteringName.add(this.txt_clusteringName, new GridBagConstraints(
            0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 5), 0, 0
        ));

        this.pnl_clusteringName.add(this.btn_showSavedClusterings, new GridBagConstraints(
            1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 0, 0), 0, 0
        ));

        this.btn_showSavedClusterings.setPreferredSize(new Dimension(
            this.btn_showSavedClusterings.getPreferredSize().width, this.txt_clusteringName.getPreferredSize().height
        ));

        this.pnl_clustering = new JPanel();
        this.pnl_clustering.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        var layout = new GridBagLayout();
        layout.columnWidths = new int[]{0, 0, 0};
        layout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
        layout.columnWeights = new double[]{0.0, 1.0, 1e-4};
        layout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1e-4};
        this.pnl_clustering.setLayout(layout);

        this.pnl_clustering.add(this.lbl_newClustering, new GridBagConstraints(
            0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0
        ));

        this.pnl_clustering.add(this.chk_newClustering, new GridBagConstraints(
            1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0
        ));

        this.pnl_clustering.add(this.lbl_clusteringName, new GridBagConstraints(
            0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0
        ));

        this.pnl_clustering.add(this.pnl_clusteringName, new GridBagConstraints(
            1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0
        ));

        this.pnl_clustering.add(this.lbl_distance, new GridBagConstraints(
            0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0
        ));

        this.pnl_clustering.add(this.cmb_distance, new GridBagConstraints(
            1, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0
        ));

        this.pnl_clustering.add(this.lbl_depth, new GridBagConstraints(
            0, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0
        ));

        this.pnl_clustering.add(this.spn_depth, new GridBagConstraints(
            1, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0
        ));

        this.pnl_clustering.add(this.btn_mine, new GridBagConstraints(
            1, 4, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0
        ));
    }

    /**
     * Reimposta la scheda del dataset.
     */
    private void resetDatasetTab() {
        this.tbp_controls.setEnabledAt(1, false);
        this.lbl_tableName.setEnabled(false);
        this.txt_tableName.setEnabled(false);
        this.btn_showAvailableDatasets.setEnabled(false);
        this.btn_loadDataset.setEnabled(false);
        this.lbl_loadedDataset.setVisible(false);
        this.lbl_loadedDatasetInfo.setVisible(false);
        this.lbl_loadedDatasetInfo.setText("");
    }

    /**
     * Reimposta la scheda del clustering.
     */
    private void resetClusteringTab() {
        this.tbp_controls.setEnabledAt(2, false);
        this.lbl_newClustering.setEnabled(false);
        this.chk_newClustering.setEnabled(false);
        this.lbl_clusteringName.setEnabled(false);
        this.txt_clusteringName.setEnabled(false);
        this.btn_showSavedClusterings.setEnabled(false);
        this.lbl_distance.setEnabled(false);
        this.cmb_distance.setEnabled(false);
        this.lbl_depth.setEnabled(false);
        this.spn_depth.setEnabled(false);
        this.btn_mine.setEnabled(false);
        this.dendrogramViewerWidget.setEnabled(false);
        this.dendrogramViewerWidget.setClustering(null);
    }

    /**
     * Crea gli ascoltatori degli eventi.
     */
    private void createEventListeners() {
        this.btn_connect.addActionListener(event -> {
            try {
                this.lbl_address.setEnabled(false);
                this.txt_address.setEnabled(false);
                this.lbl_port.setEnabled(false);
                this.txt_port.setEnabled(false);
                this.btn_connect.setEnabled(false);
                this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.CONNECTING);

                try {
                    if (ServerConnection.the() != null) {
                        ServerConnection.the().closeConnection();

                        this.resetDatasetTab();
                        this.resetClusteringTab();

                        this.distanceModel.removeAllElements();
                        this.depthModel.setMaximum(Integer.MAX_VALUE);

                        this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.NOT_CONNECTED);
                    }
                } catch (IOException exception) {
                    this.showErrorDialog("Errore durante la chiusura della connessione!", exception.getMessage());
                    this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.NOT_CONNECTED);
                    return;
                }

                var address = this.txt_address.getText();
                var portString = this.txt_port.getText();

                int port;
                try {
                    port = Integer.parseInt(portString);
                } catch (NumberFormatException exception) {
                    this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.NOT_CONNECTED);
                    this.showErrorDialog("Porta non valida!", "");
                    return;
                }

                if (port <= 0 || port > 65535) {
                    this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.NOT_CONNECTED);
                    this.showErrorDialog("Porta non valida!", "");
                    return;
                }

                try {
                    ServerConnection.open(address, port);

                    this.tbp_controls.setEnabledAt(1, true);
                    this.lbl_tableName.setEnabled(true);
                    this.txt_tableName.setEnabled(true);
                    this.btn_showAvailableDatasets.setEnabled(true);
                    this.btn_loadDataset.setEnabled(true);
                    this.distanceModel.addAll(ServerConnection.the().getClusterDistanceMethods());

                    this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.CONNECTED);
                } catch (ServerException exception) {
                    this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.NOT_CONNECTED);
                    this.showErrorDialog(exception.getMessage(), exception.getDetails());
                } catch (IOException exception) {
                    this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.NOT_CONNECTED);
                    this.showErrorDialog("Errore di I/O durante la comunicazione!", exception.getMessage());
                }
            } finally {
                this.lbl_address.setEnabled(true);
                this.txt_address.setEnabled(true);
                this.lbl_port.setEnabled(true);
                this.txt_port.setEnabled(true);
                this.btn_connect.setEnabled(true);
            }
        });

        this.btn_showAvailableDatasets.addActionListener(event -> {
            this.btn_showAvailableDatasets.setEnabled(false);

            try {
                if (ServerConnection.the() == null) {
                    this.showErrorDialog("Nessuna connessione al server!", "");
                    return;
                }

                try {
                    var availableDatasets = ServerConnection.the().getDatasets();
                    if (availableDatasets.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Nessun dataset disponibile!", this.getTitle(), JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    this.showSuggestionDialog(availableDatasets, this.txt_tableName::setText);
                } catch (ServerException exception) {
                    this.showErrorDialog(exception.getMessage(), exception.getDetails());
                } catch (IOException exception) {
                    this.showErrorDialog("Errore di I/O durante la comunicazione!", exception.getMessage());
                }
            } finally {
                this.btn_showAvailableDatasets.setEnabled(true);
            }
        });

        this.btn_loadDataset.addActionListener(event -> {
            this.lbl_tableName.setEnabled(false);
            this.txt_tableName.setEnabled(false);
            this.btn_loadDataset.setEnabled(false);

            try {
                if (ServerConnection.the() == null) {
                    this.showErrorDialog("Nessuna connessione al server!", "");
                    return;
                }

                this.resetClusteringTab();

                this.lbl_loadedDataset.setVisible(false);
                this.lbl_loadedDatasetInfo.setVisible(false);
                this.lbl_loadedDatasetInfo.setText("");

                try {
                    var exampleCount = ServerConnection.the().loadDataset(this.txt_tableName.getText());
                    this.lbl_loadedDataset.setVisible(true);
                    this.lbl_loadedDatasetInfo.setVisible(true);
                    this.lbl_loadedDatasetInfo.setText(String.format("%s - %d esempi", this.txt_tableName.getText(), exampleCount));

                    this.tbp_controls.setEnabledAt(2, true);
                    this.lbl_newClustering.setEnabled(true);
                    this.chk_newClustering.setEnabled(true);
                    this.lbl_clusteringName.setEnabled(true);
                    this.txt_clusteringName.setEnabled(true);
                    this.btn_showSavedClusterings.setEnabled(true);
                    this.lbl_distance.setEnabled(true);
                    this.cmb_distance.setEnabled(true);
                    this.lbl_depth.setEnabled(true);
                    this.spn_depth.setEnabled(true);
                    this.depthModel.setMaximum(exampleCount);
                    this.btn_mine.setEnabled(true);
                } catch (ServerException exception) {
                    this.showErrorDialog(exception.getMessage(), exception.getDetails());
                } catch (IOException exception) {
                    this.showErrorDialog("Errore di I/O durante la comunicazione!", exception.getMessage());
                }
            } finally {
                this.lbl_tableName.setEnabled(true);
                this.txt_tableName.setEnabled(true);
                this.btn_loadDataset.setEnabled(true);
            }
        });

        this.chk_newClustering.addItemListener(event -> {
            if (this.chk_newClustering.isSelected()) {
                this.lbl_distance.setEnabled(true);
                this.cmb_distance.setEnabled(true);
                this.lbl_depth.setEnabled(true);
                this.spn_depth.setEnabled(true);
            } else {
                this.lbl_distance.setEnabled(false);
                this.cmb_distance.setEnabled(false);
                this.lbl_depth.setEnabled(false);
                this.spn_depth.setEnabled(false);
            }
        });

        this.btn_showSavedClusterings.addActionListener(event -> {
            this.btn_showSavedClusterings.setEnabled(false);

            try {
                if (ServerConnection.the() == null) {
                    this.showErrorDialog("Nessuna connessione al server!", "");
                    return;
                }

                try {
                    var savedClusterings = ServerConnection.the().getSavedClusterings();
                    if (savedClusterings.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Nessun clustering salvato sul server!", this.getTitle(), JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    this.showSuggestionDialog(savedClusterings, this.txt_clusteringName::setText);
                } catch (ServerException exception) {
                    this.showErrorDialog(exception.getMessage(), exception.getDetails());
                } catch (IOException exception) {
                    this.showErrorDialog("Errore di I/O durante la comunicazione!", exception.getMessage());
                }
            } finally {
                this.btn_showSavedClusterings.setEnabled(true);
            }
        });

        this.btn_mine.addActionListener(event -> {
            this.lbl_newClustering.setEnabled(false);
            this.chk_newClustering.setEnabled(false);
            this.lbl_clusteringName.setEnabled(false);
            this.txt_clusteringName.setEnabled(false);
            this.lbl_distance.setEnabled(false);
            this.cmb_distance.setEnabled(false);
            this.lbl_depth.setEnabled(false);
            this.spn_depth.setEnabled(false);
            this.btn_mine.setEnabled(false);
            this.dendrogramViewerWidget.setEnabled(false);

            try {
                if (ServerConnection.the() == null) {
                    this.showErrorDialog("Nessuna connessione al server!", "");
                    return;
                }

                try {
                    var fileName = this.txt_clusteringName.getText();

                    Clustering clustering;
                    if (this.chk_newClustering.isSelected()) {
                        var depth = (int) this.depthModel.getValue();
                        var selectedDistance = this.cmb_distance.getSelectedItem();
                        if (selectedDistance == null) {
                            this.showErrorDialog("Bisogna selezionare un metodo per il calcolo della distanza!", "");
                            return;
                        }
                        var distanceId = ((ClusterDistanceMethod) selectedDistance).id();

                        clustering = ServerConnection.the().newClustering(depth, distanceId, fileName);
                    } else {
                        clustering = ServerConnection.the().loadClustering(fileName);
                    }

                    this.dendrogramViewerWidget.setClustering(clustering);
                    this.dendrogramViewerWidget.requestFocus();
                } catch (ServerException exception) {
                    this.showErrorDialog(exception.getMessage(), exception.getDetails());
                } catch (IOException exception) {
                    this.showErrorDialog("Errore di I/O durante la comunicazione!", exception.getMessage());
                }
            } finally {
                this.lbl_newClustering.setEnabled(true);
                this.chk_newClustering.setEnabled(true);
                this.lbl_clusteringName.setEnabled(true);
                this.txt_clusteringName.setEnabled(true);

                if (this.chk_newClustering.isSelected()) {
                    this.lbl_distance.setEnabled(true);
                    this.cmb_distance.setEnabled(true);
                    this.lbl_depth.setEnabled(true);
                    this.spn_depth.setEnabled(true);
                }

                this.btn_mine.setEnabled(true);
                this.dendrogramViewerWidget.setEnabled(true);
            }
        });
    }

    /**
     * Mostra una finestra di dialogo che contiene un messaggio d'errore, {@code error}, specificato come parametro e
     * degli eventuali dettagli che lo specificano, {@code details}, specificati come parametro.
     *
     * @param message il messaggio d'errore da visualizzare
     * @param details i dettagli del messaggio d'errore
     */
    private void showErrorDialog(String message, String details) {
        if (details == null || details.isBlank()) {
            JOptionPane.showMessageDialog(this, message, this.getTitle(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        var icn_error = new JLabel(UIManager.getIcon("OptionPane.errorIcon"));
        var lbl_message = new JLabel(message);
        var txt_details = new JTextArea(10, 40);
        var scr_details = new JScrollPane(txt_details);
        txt_details.setText(details);
        txt_details.setFont(new Font(Font.MONOSPACED, Font.PLAIN, txt_details.getFont().getSize()));
        txt_details.setEditable(false);
        var pnl_message = new JPanel();
        pnl_message.setLayout(new BoxLayout(pnl_message, BoxLayout.LINE_AXIS));
        pnl_message.add(icn_error);
        pnl_message.add(Box.createRigidArea(new Dimension(10, 0)));
        pnl_message.add(lbl_message);

        var btn_ok = new JButton("Ok");
        var pnl_buttons = new JPanel();
        pnl_buttons.setLayout(new BoxLayout(pnl_buttons, BoxLayout.LINE_AXIS));
        pnl_buttons.add(btn_ok);

        var pnl_main = new JPanel();
        pnl_main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnl_main.setLayout(new BoxLayout(pnl_main, BoxLayout.PAGE_AXIS));

        pnl_message.setAlignmentX(Component.RIGHT_ALIGNMENT);
        scr_details.setAlignmentX(Component.RIGHT_ALIGNMENT);
        pnl_buttons.setAlignmentX(Component.RIGHT_ALIGNMENT);

        pnl_main.add(pnl_message);
        pnl_main.add(Box.createRigidArea(new Dimension(0, 5)));
        pnl_main.add(scr_details);
        pnl_main.add(Box.createRigidArea(new Dimension(0, 5)));
        pnl_main.add(pnl_buttons);

        var dialog = new JDialog(this, this.getTitle(), true);
        dialog.getContentPane().add(pnl_main);

        btn_ok.addActionListener(event -> dialog.setVisible(false));

        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Mostra una finestra di dialogo che contiene i suggerimenti per l'utente {@code suggestions}, specificati come
     * parametro. Se viene selezionato uno dei suggerimenti allora viene richiamata la funzione {@code useSuggestion},
     * specificata come parametro, con il suggerimento selezionato come argomento.
     *
     * @param suggestions la lista dei clustering salvati sul server
     * @param useSuggestion la funzione che verrà richiamata una volta scelto il suggerimento
     */
    private void showSuggestionDialog(List<String> suggestions, Consumer<String> useSuggestion) {
        if (suggestions.isEmpty()) {
            useSuggestion.accept(null);
            return;
        }

        var suggestionsModel = new DefaultListModel<String>();
        suggestionsModel.addAll(suggestions);

        var lst_suggestions = new JList<>(suggestionsModel);
        lst_suggestions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lst_suggestions.setLayoutOrientation(JList.VERTICAL);
        var scr_suggestions = new JScrollPane(lst_suggestions);

        var btn_ok = new JButton("Ok");
        var btn_cancel = new JButton("Annulla");
        var pnl_buttons = new JPanel();
        pnl_buttons.setLayout(new BoxLayout(pnl_buttons, BoxLayout.LINE_AXIS));
        pnl_buttons.add(btn_ok);
        pnl_buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        pnl_buttons.add(btn_cancel);

        var pnl_main = new JPanel();
        pnl_main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnl_main.setLayout(new BoxLayout(pnl_main, BoxLayout.PAGE_AXIS));
        pnl_main.add(scr_suggestions);
        pnl_main.add(Box.createRigidArea(new Dimension(0, 5)));
        pnl_main.add(pnl_buttons);

        var dialog = new JDialog(this, this.getTitle(), true);
        dialog.getContentPane().add(pnl_main);

        lst_suggestions.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() != MouseEvent.BUTTON1 || mouseEvent.getClickCount() != 2) {
                    return;
                }

                Rectangle listBounds = lst_suggestions.getCellBounds(lst_suggestions.getFirstVisibleIndex(), lst_suggestions.getLastVisibleIndex());
                if (listBounds == null || !listBounds.contains(mouseEvent.getPoint())) {
                    return;
                }

                var index = lst_suggestions.locationToIndex(mouseEvent.getPoint());
                if (index == -1) {
                    return;
                }

                useSuggestion.accept(suggestionsModel.get(index));
                dialog.setVisible(false);
            }
        });

        btn_ok.addActionListener(dialogEvent -> {
            var selection = lst_suggestions.getSelectedValue();
            if (selection != null) {
                useSuggestion.accept(selection);
            }

            dialog.setVisible(false);
        });

        btn_cancel.addActionListener(dialogEvent -> dialog.setVisible(false));

        dialog.setResizable(false);
        dialog.setPreferredSize(new Dimension(350, 200));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Metodo principale dell'applicazione.
     *
     * @param args argomenti passati a linea di comando
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}