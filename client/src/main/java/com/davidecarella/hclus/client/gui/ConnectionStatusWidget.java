package com.davidecarella.hclus.client.gui;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Widget per la visualizzazione dello stato della connessione con il server.
 *
 * <p>È un piccolo contenitore che mostra un cerchio il cui colore indica lo stato della connessione:
 * <ul>
 *     <li>Rosso: il client non è connesso al server;</li>
 *     <li>Giallo: un tentativo di connessione al server è in corso;</li>
 *     <li>Verde: il client è connesso.</li>
 * </ul>
 */
class ConnectionStatusWidget extends JPanel {
    /**
     * I possibili stati della connessione.
     */
    enum Status {
        /**
         * Non connesso.
         */
        NOT_CONNECTED,

        /**
         * Connessione in corso.
         */
        CONNECTING,

        /**
         * Connesso.
         */
        CONNECTED,
    }

    /**
     * La dimensione (in pixel) del cerchio mostrato.
     */
    private static final int SIZE = 12;

    /**
     * Lo stato attuale della connessione.
     */
    private Status status = Status.NOT_CONNECTED;

    /**
     * Costruttore di default.
     */
    ConnectionStatusWidget() {}

    /**
     * Restituisce lo stato attuale della connessione.
     *
     * @return lo stato attuale della connessione.
     */
    Status getStatus() {
        return this.status;
    }

    /**
     * Imposta lo stato della connessione.
     *
     * @param status il nuovo stato della connessione.
     */
    void setStatus(Status status) {
        this.status = status;
        this.repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SIZE, SIZE);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(SIZE, SIZE);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(SIZE, SIZE);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        ((Graphics2D) g).setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

        g.setColor(switch (this.status) {
            case NOT_CONNECTED -> Color.RED;
            case CONNECTING -> Color.YELLOW;
            case CONNECTED -> Color.GREEN;
        });

        g.fillArc(0, 0, SIZE, SIZE, 0, 360);
    }
}
