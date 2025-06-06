package com.mumble.app.Panels;

import javax.swing.*;
import java.awt.*;

/**
 * A ScrollablePanel provides overriden methods with scrollable interactions
 */
public class ScrollablePanel extends JPanel implements Scrollable {

    public ScrollablePanel() {
        super();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 100;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getPreferredSize().width < getParent().getWidth(); // stretch to fill horizontal space
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false; // grow vertically with content
    }
}