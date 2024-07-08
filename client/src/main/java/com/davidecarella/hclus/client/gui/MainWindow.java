package com.davidecarella.hclus.client.gui;


import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private JTextField txt_address;
    private JTextField txt_port;
    private ConnectionStatusWidget connectionStatusWidget;
    private JButton btn_connect;

    private JTextField txt_tableName;
    private JButton btn_loadData;

    private JRadioButton rdb_singleLinkDistance;
    private JRadioButton rdb_averageLinkDistance;
    private SpinnerNumberModel depthModel;
    private JSpinner spn_depth;
    private JButton btn_mine;

    public MainWindow() {
        super("HCLUS - Client");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.initializeComponents();

        var pnl_main = new JPanel();
        pnl_main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        pnl_main.setLayout(new BoxLayout(pnl_main, BoxLayout.PAGE_AXIS));

        pnl_main.add(createConnectionPanel());
        pnl_main.add(createDataSourcePanel());
        pnl_main.add(createMineSettingsPanel());
        pnl_main.add(createGraphPanel());

        this.add(pnl_main);
        this.pack();

        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

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
        this.btn_mine = new JButton("Estrai");
        this.btn_mine.setEnabled(false);
    }

    private JPanel createConnectionPanel() {
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

    private JPanel createMineSettingsPanel() {
        this.spn_depth.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.spn_depth.getPreferredSize().height));

        var pnl_distance = new JPanel();
        pnl_distance.setLayout(new BoxLayout(pnl_distance, BoxLayout.LINE_AXIS));

        pnl_distance.add(new JLabel("Distanza: "));
        pnl_distance.add(this.rdb_singleLinkDistance);
        pnl_distance.add(this.rdb_averageLinkDistance);

        var pnl_depth = new JPanel();
        pnl_depth.setLayout(new BoxLayout(pnl_depth, BoxLayout.LINE_AXIS));

        pnl_depth.add(new JLabel("Profondità"));
        pnl_depth.add(Box.createRigidArea(new Dimension(5, 0)));
        pnl_depth.add(this.spn_depth);

        var pnl_settings = new JPanel();
        pnl_settings.setLayout(new BoxLayout(pnl_settings, BoxLayout.PAGE_AXIS));

        pnl_settings.add(pnl_distance);
        pnl_settings.add(pnl_depth);

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

    private JPanel createGraphPanel() {
        // TODO
        return new JPanel();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}