package com.davidecarella.hclus.client.gui;

import com.davidecarella.hclus.client.communication.ServerConnection;
import com.davidecarella.hclus.common.ClusterDistanceMethod;
import com.davidecarella.hclus.common.Clustering;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

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
     * Il campo del nome della tabella.
     */
    private JTextField txt_tableName;

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
        this.txt_tableName = new JTextField();
        this.btn_loadDataset = new JButton("Carica");
        this.lbl_loadedDataset = new JLabel("Attuale");
        this.lbl_loadedDataset.setVisible(false);
        this.lbl_loadedDatasetInfo = new JLabel();
        this.lbl_loadedDatasetInfo.setVisible(false);
        this.lbl_loadedDatasetInfo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, this.lbl_loadedDatasetInfo.getFont().getSize()));

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

        this.pnl_dataset.add(this.txt_tableName, new GridBagConstraints(
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
                    JOptionPane.showMessageDialog(this, exception.getMessage(), this.getTitle(), JOptionPane.ERROR_MESSAGE);
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
                    JOptionPane.showMessageDialog(this, "Porta non valida!", this.getTitle(), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (port <= 0 || port > 65535) {
                    this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.NOT_CONNECTED);
                    JOptionPane.showMessageDialog(this, "Porta non valida!", this.getTitle(), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    ServerConnection.open(address, port);

                    this.tbp_controls.setEnabledAt(1, true);
                    this.lbl_tableName.setEnabled(true);
                    this.txt_tableName.setEnabled(true);
                    this.btn_loadDataset.setEnabled(true);
                    this.distanceModel.addAll(ServerConnection.the().getClusterDistanceMethods());

                    this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.CONNECTED);
                } catch (IOException exception) {
                    exception.printStackTrace();
                    this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.NOT_CONNECTED);
                    JOptionPane.showMessageDialog(this, String.format("Errore durante la connessione: %s!", exception.getMessage()), this.getTitle(), JOptionPane.ERROR_MESSAGE);
                }
            } finally {
                this.lbl_address.setEnabled(true);
                this.txt_address.setEnabled(true);
                this.lbl_port.setEnabled(true);
                this.txt_port.setEnabled(true);
                this.btn_connect.setEnabled(true);
            }
        });

        this.btn_loadDataset.addActionListener(event -> {
            this.lbl_tableName.setEnabled(false);
            this.txt_tableName.setEnabled(false);
            this.btn_loadDataset.setEnabled(false);

            try {
                if (ServerConnection.the() == null) {
                    JOptionPane.showMessageDialog(this, "Nessuna connessione al server!", this.getTitle(), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                this.resetClusteringTab();

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

                    JOptionPane.showMessageDialog(this, "Dataset caricato con successo!", this.getTitle(), JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException exception) {
                    JOptionPane.showMessageDialog(this, String.format("Errore durante il caricamento del dataset: %s!", exception.getMessage()), this.getTitle(), JOptionPane.INFORMATION_MESSAGE);
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
                    JOptionPane.showMessageDialog(this, "Nessuna connessione al server!", this.getTitle(), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    this.showSavedClusteringsDialog(ServerConnection.the().getSavedClusterings());
                } catch (IOException exception) {
                    JOptionPane.showMessageDialog(this, String.format("Errore durante la lettura dei clustering salvati: %s!", exception.getMessage()), this.getTitle(), JOptionPane.ERROR_MESSAGE);
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
                    JOptionPane.showMessageDialog(this, "Nessuna connessione al server!", this.getTitle(), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    var fileName = this.txt_clusteringName.getText();

                    Clustering clustering;
                    if (this.chk_newClustering.isSelected()) {
                        var depth = (int) this.depthModel.getValue();
                        var selectedDistance = this.cmb_distance.getSelectedItem();
                        if (selectedDistance == null) {
                            JOptionPane.showMessageDialog(this, "Bisogna selezionare un metodo per il calcolo della distanza!", this.getTitle(), JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        var distanceId = ((ClusterDistanceMethod) selectedDistance).id();

                        clustering = ServerConnection.the().newClustering(depth, distanceId, fileName);
                    } else {
                        clustering = ServerConnection.the().loadClustering(fileName);
                    }

                    this.dendrogramViewerWidget.setClustering(clustering);
                } catch (IOException exception) {
                    String message;
                    if (this.chk_newClustering.isSelected()) {
                        message = String.format("Errore durante la creazione del nuovo clustering: %s", exception.getMessage());
                    } else {
                        message = String.format("Errore durante il caricamento del clustering: %s", exception.getMessage());
                    }

                    JOptionPane.showMessageDialog(this, message, this.getTitle(), JOptionPane.ERROR_MESSAGE);
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
     * <p>Mostra una finestra di dialogo che contiene la lista dei clustering salvati sul server {@code savedClusterings},
     * specificata come parametro.
     *
     * <p>Una volta selezionato il clustering, premendo il pulsante di conferma questo viene inserito nel campo del
     * nome del clustering.
     *
     * @param savedClusterings la lista dei clustering salvati sul server
     */
    private void showSavedClusteringsDialog(List<String> savedClusterings) {
        var model = new DefaultListModel<String>();
        model.addAll(savedClusterings);

        if (model.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessun clustering salvato sul server", this.getTitle(), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        var list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        var scrollPane = new JScrollPane(list);

        var confirmButton = new JButton("Ok");
        var cancelButton = new JButton("Annulla");
        var buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
        buttons.add(confirmButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(cancelButton);

        var mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(scrollPane);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(buttons);

        var dialog = new JDialog(this, this.getTitle(), true);
        dialog.getContentPane().add(mainPanel);

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() != MouseEvent.BUTTON1 || mouseEvent.getClickCount() != 2) {
                    return;
                }

                Rectangle listBounds = list.getCellBounds(list.getFirstVisibleIndex(), list.getLastVisibleIndex());
                if (listBounds == null || !listBounds.contains(mouseEvent.getPoint())) {
                    return;
                }

                var index = list.locationToIndex(mouseEvent.getPoint());
                if (index == -1) {
                    return;
                }

                txt_clusteringName.setText(model.get(index));
                dialog.setVisible(false);
            }
        });

        confirmButton.addActionListener(dialogEvent -> {
            var selection = list.getSelectedValue();
            if (selection != null) {
                this.txt_clusteringName.setText(selection);
            }

            dialog.setVisible(false);
        });

        cancelButton.addActionListener(dialogEvent -> dialog.setVisible(false));

        dialog.setResizable(false);
        dialog.setPreferredSize(new Dimension(250, 200));
        dialog.setLocationRelativeTo(null);
        dialog.pack();
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