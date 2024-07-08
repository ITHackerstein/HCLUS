package com.davidecarella.hclus.client.gui;

import javax.swing.*;
import java.awt.*;

public class ConnectionStatusWidget extends JPanel {
    public enum Status {
        NOT_CONNECTED,
        CONNECTING,
        CONNECTED,
    }

    private static final int SIZE = 12;

    private Status status = Status.NOT_CONNECTED;

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
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

        // NOTE: Makes the circle anti-aliased
        ((Graphics2D) g).setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

        g.setColor(switch (this.status) {
            case NOT_CONNECTED -> Color.RED;
            case CONNECTING -> Color.YELLOW;
            case CONNECTED -> Color.GREEN;
        });

        g.fillArc(0, 0, SIZE, SIZE, 0, 360);
    }
}
