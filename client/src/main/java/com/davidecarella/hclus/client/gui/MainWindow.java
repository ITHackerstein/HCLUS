package com.davidecarella.hclus.client.gui;

import com.davidecarella.hclus.client.communication.ServerConnection;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * TODO: Do the actual drawing
 * TODO: Load from file
 * TODO: Avoid saving using actual path (maybe use the actual database or store using a generated file name in a
 *       specific folder and maybe new request to list saved dendrograms)
 * TODO: New distances (add a method to get the possible distances)
 * TODO: Fix comments all around the code
 *
 * <p>La finestra principale del client.
 *
 * <p>È divisa in 4 sezioni:
 * <ul>
 *     <li>la sezione di connessione: consente all'utente di inserire i dati necessari per connettersi al server;</li>
 *     <li>
 *         la sezione dei dati: consente all'utente, connesso al server, di inserire il nome della tabella del server da
 *                              cui caricare i dati;
 *     </li>
 *     <li>
 *         la sezione delle impostazioni: consente all'utente, connesso al server, di inserire le impostazioni per poter
 *                                        creare un nuovo dendrogramma;
 *     </li>
 *     <li>
 *         la sezione di visualizzazione: in cui viene visualizzato il dendrogramma.
 *     </li>
 * </ul>
 */
public class MainWindow extends JFrame {
    /**
     * Il campo di testo per l'inserimento dell'indirizzo del server.
     */
    private JTextField txt_address;

    /**
     * Il campo di testo per l'inserimento della porta del server.
     */
    private JTextField txt_port;

    /**
     * Il widget dello stato di connessione al server.
     */
    private ConnectionStatusWidget connectionStatusWidget;

    /**
     * Il pulsante per connettersi al server.
     */
    private JButton btn_connect;

    /**
     * Il campo di testo per l'inserimento del nome della tabella da cui caricare i dati.
     */
    private JTextField txt_tableName;

    /**
     * Il pulsante per caricare i dati dal server.
     */
    private JButton btn_loadData;

    /**
     * L'opzione per il selezionamento della distanza single-link.
     */
    private JRadioButton rdb_singleLinkDistance;

    /**
     * L'opzione per il selezionamento della distanza average-link.
     */
    private JRadioButton rdb_averageLinkDistance;

    /**
     * Il modello dati di {@link MainWindow#spn_depth}.
     */
    private SpinnerNumberModel depthModel;

    /**
     * Il campo numerico per l'inserimento della profondità.
     */
    private JSpinner spn_depth;

    /**
     * Il campo di testo per l'inserimento del nome del file.
     */
    private JTextField txt_fileName;

    /**
     * IL pulsante per creare un nuovo dendrogramma.
     */
    private JButton btn_mine;

    /**
     * IL widget per la visualizzazione del dendrogramma.
     */
    private DendrogramViewerWidget dendrogramViewerWidget;

    /**
     * La connessione al server.
     */
    private ServerConnection serverConnection;

    /**
     * <p>Costruttore della finestra.
     *
     * <p>Crea la finestra impostandone il suo layout e la visualizza.
     */
    public MainWindow() {
        super("HCLUS - Client");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.initializeComponents();
        this.initializeListeners();

        var pnl_main = new JPanel();
        pnl_main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        pnl_main.setLayout(new BoxLayout(pnl_main, BoxLayout.PAGE_AXIS));

        pnl_main.add(createConnectionPanel());
        pnl_main.add(createDataSourcePanel());
        pnl_main.add(createMineSettingsPanel());
        pnl_main.add(this.dendrogramViewerWidget);

        this.add(pnl_main);
        this.pack();

        this.setMinimumSize(this.getPreferredSize());
        this.setResizable(true);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
     * Metodo utilizzato per inizializzare ogni componente della finestra.
     */
    private void initializeComponents() {
        this.txt_address = new JTextField();
        this.txt_port = new JTextField();
        this.connectionStatusWidget = new ConnectionStatusWidget();
        this.btn_connect = new JButton("Connetti");

        this.txt_tableName = new JTextField();
        this.txt_tableName.setEnabled(false);
        this.btn_loadData = new JButton("Carica");
        this.btn_loadData.setEnabled(false);

        this.rdb_singleLinkDistance = new JRadioButton("Single-Link");
        this.rdb_singleLinkDistance.setEnabled(false);
        this.rdb_averageLinkDistance = new JRadioButton("Average-Link");
        this.rdb_averageLinkDistance.setEnabled(false);
        var buttonGroup = new ButtonGroup();
        buttonGroup.add(this.rdb_singleLinkDistance);
        buttonGroup.add(this.rdb_averageLinkDistance);

        this.depthModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
        this.spn_depth = new JSpinner(this.depthModel);
        this.spn_depth.setEnabled(false);
        this.txt_fileName = new JTextField();
        this.txt_fileName.setEnabled(false);
        this.btn_mine = new JButton("Estrai");
        this.btn_mine.setEnabled(false);

        this.dendrogramViewerWidget = new DendrogramViewerWidget();
    }

    /**
     * Metodo utilizzato per inizializzare gli ascoltatori degli eventi per ogni elemento.
     */
    private void initializeListeners() {
        this.btn_connect.addActionListener(event -> {
            this.btn_connect.setEnabled(false);
            this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.CONNECTING);

            var address = this.txt_address.getText();
            var portString = this.txt_port.getText();
            int port;
            try {
                port = Integer.parseInt(portString);

                if (port < 1 || port > 65535) {
                    this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.NOT_CONNECTED);
                    this.btn_connect.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "Porta non valida!", this.getTitle(), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException exception) {
                this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.NOT_CONNECTED);
                this.btn_connect.setEnabled(true);
                JOptionPane.showMessageDialog(this, "Porta non valida!", this.getTitle(), JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                this.serverConnection = new ServerConnection(address, port);
                this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.CONNECTED);
                this.btn_connect.setEnabled(false);
                this.txt_tableName.setEnabled(true);
                this.btn_loadData.setEnabled(true);
            } catch (IOException exception) {
                this.connectionStatusWidget.setStatus(ConnectionStatusWidget.Status.NOT_CONNECTED);
                this.btn_connect.setEnabled(true);
                JOptionPane.showMessageDialog(this, "Errore durante la connessione: " + exception.getMessage(), this.getTitle(), JOptionPane.ERROR_MESSAGE);
            }
        });

        this.btn_loadData.addActionListener(event -> {
            this.btn_loadData.setEnabled(false);

            try {
                this.serverConnection.loadData(this.txt_tableName.getText());
                JOptionPane.showMessageDialog(this, "Dati caricati con successo!", this.getTitle(), JOptionPane.INFORMATION_MESSAGE);

                this.rdb_singleLinkDistance.setEnabled(true);
                this.rdb_averageLinkDistance.setEnabled(true);
                this.spn_depth.setEnabled(true);
                this.txt_fileName.setEnabled(true);
                this.btn_mine.setEnabled(true);
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(this, "Errore durante il caricamento dei dati: " + exception.getMessage(), this.getTitle(), JOptionPane.ERROR_MESSAGE);
            }

            this.btn_loadData.setEnabled(true);
        });

        this.btn_mine.addActionListener(event -> {
            this.btn_mine.setEnabled(false);

            try {
                if (!this.rdb_singleLinkDistance.isSelected() && !this.rdb_averageLinkDistance.isSelected()) {
                    JOptionPane.showMessageDialog(this, "Bisogna selezionare un tipo di distanza!", this.getTitle(), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int depth = (int) this.depthModel.getValue();
                int distanceType = this.rdb_singleLinkDistance.isSelected() ? 0 : 1;
                var clustering = this.serverConnection.newClustering(depth, distanceType, this.txt_fileName.getText());
                this.dendrogramViewerWidget.setClustering(clustering);
                JOptionPane.showMessageDialog(this, "Dendrogramma creato con successo!", this.getTitle(), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(this, "Errore durante la creazione del dendrogramma: " + exception.getMessage(), this.getTitle(), JOptionPane.ERROR_MESSAGE);
            }

            this.btn_mine.setEnabled(true);
        });
    }

    /**
     * Metodo per la creazione della sezione di connessione.
     *
     * @return la sezione di connessione
     */
    private JPanel createConnectionPanel() {
        this.txt_address.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.txt_address.getPreferredSize().height));
        this.txt_port.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.txt_port.getPreferredSize().height));

        var pnl_address = new JPanel();
        pnl_address.setLayout(new BoxLayout(pnl_address, BoxLayout.LINE_AXIS));

        pnl_address.add(new JLabel("Indirizzo"));
        pnl_address.add(Box.createRigidArea(new Dimension(5, 0)));
        pnl_address.add(this.txt_address);

        var pnl_port = new JPanel();
        pnl_port.setLayout(new BoxLayout(pnl_port, BoxLayout.LINE_AXIS));

        pnl_port.add(new JLabel("Porta"));
        pnl_port.add(Box.createRigidArea(new Dimension(5, 0)));
        pnl_port.add(this.txt_port);

        var pnl_connectionSettings = new JPanel();
        pnl_connectionSettings.setLayout(new BoxLayout(pnl_connectionSettings, BoxLayout.PAGE_AXIS));

        pnl_connectionSettings.add(pnl_address);
        pnl_connectionSettings.add(pnl_port);

        var pnl_connect = new JPanel();
        pnl_connect.setLayout(new BoxLayout(pnl_connect, BoxLayout.LINE_AXIS));

        pnl_connect.add(this.btn_connect);
        pnl_connect.add(Box.createRigidArea(new Dimension(5, 0)));
        pnl_connect.add(this.connectionStatusWidget);

        var pnl_connection = new JPanel();
        pnl_connection.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Connessione"));
        pnl_connection.setLayout(new BoxLayout(pnl_connection, BoxLayout.LINE_AXIS));

        pnl_connectionSettings.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        pnl_connect.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        pnl_connection.add(pnl_connectionSettings);
        pnl_connection.add(Box.createRigidArea(new Dimension(5, 0)));
        pnl_connection.add(pnl_connect);

        return pnl_connection;
    }

    /**
     * Metodo per la creazione della sezione dei dati.
     *
     * @return la sezione dei dati
     */
    private JPanel createDataSourcePanel() {
        this.txt_tableName.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.txt_tableName.getPreferredSize().height));

        var pnl_dataSource = new JPanel();
        pnl_dataSource.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dati"));
        pnl_dataSource.setLayout(new BoxLayout(pnl_dataSource, BoxLayout.LINE_AXIS));

        pnl_dataSource.add(new JLabel("Tabella"));
        pnl_dataSource.add(Box.createRigidArea(new Dimension(5, 0)));
        pnl_dataSource.add(this.txt_tableName);
        pnl_dataSource.add(Box.createRigidArea(new Dimension(5, 0)));
        pnl_dataSource.add(this.btn_loadData);

        return pnl_dataSource;
    }

    /**
     * Metodo per la creazione della sezione delle impostazioni.
     *
     * @return la sezione delle impostazioni
     */
    private JPanel createMineSettingsPanel() {
        this.spn_depth.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.spn_depth.getPreferredSize().height));
        this.txt_fileName.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.txt_address.getPreferredSize().height));

        var pnl_distance = new JPanel();
        pnl_distance.setLayout(new BoxLayout(pnl_distance, BoxLayout.LINE_AXIS));

        pnl_distance.add(new JLabel("Distanza"));
        pnl_distance.add(Box.createRigidArea(new Dimension(5, 0)));
        pnl_distance.add(this.rdb_singleLinkDistance);
        pnl_distance.add(this.rdb_averageLinkDistance);

        var pnl_depth = new JPanel();
        pnl_depth.setLayout(new BoxLayout(pnl_depth, BoxLayout.LINE_AXIS));

        pnl_depth.add(new JLabel("Profondità"));
        pnl_depth.add(Box.createRigidArea(new Dimension(5, 0)));
        pnl_depth.add(this.spn_depth);

        var pnl_fileName = new JPanel();
        pnl_fileName.setLayout(new BoxLayout(pnl_fileName, BoxLayout.LINE_AXIS));

        pnl_fileName.add(new JLabel("File"));
        pnl_fileName.add(Box.createRigidArea(new Dimension(5, 0)));
        pnl_fileName.add(this.txt_fileName);

        var pnl_settings = new JPanel();
        pnl_settings.setLayout(new BoxLayout(pnl_settings, BoxLayout.PAGE_AXIS));

        pnl_distance.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl_depth.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl_fileName.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnl_settings.add(pnl_distance);
        pnl_settings.add(pnl_depth);
        pnl_settings.add(pnl_fileName);

        var pnl_mineSettings = new JPanel();
        pnl_mineSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Impostazioni"));
        pnl_mineSettings.setLayout(new BoxLayout(pnl_mineSettings, BoxLayout.LINE_AXIS));

        pnl_settings.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        this.btn_mine.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        pnl_mineSettings.add(pnl_settings);
        pnl_mineSettings.add(Box.createRigidArea(new Dimension(5, 0)));
        pnl_mineSettings.add(this.btn_mine);

        return pnl_mineSettings;
    }

    /**
     * Metodo principale dell'applicazione che crea un'istanza della finestra.
     *
     * @param args argomenti da linea di comando
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}