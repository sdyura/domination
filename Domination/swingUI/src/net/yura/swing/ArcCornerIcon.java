package net.yura.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;
import javax.swing.ScrollPaneConstants;
import java.awt.geom.RoundRectangle2D;

/**
 * WARNING!! this most likely does not support disabled auto-icon as it is not an ImageIcon
 */
public class ArcCornerIcon implements Icon {

    private final String corner;
    private final Icon base;

    public ArcCornerIcon(Icon base, String corner) {
        this.base = base;
        this.corner = corner;
    }

    @Override
    public void paintIcon(Component cmpnt, Graphics grphcs, int x, int y) {

        int w = getIconWidth();
        int h = getIconHeight();
        int r = Math.min(w, h);
        int xOffset = - r/2;
        int yOffset = - r/2;
        if (ScrollPaneConstants.UPPER_LEFT_CORNER.equals(corner)) {
            xOffset = 0;
            yOffset = 0;
        }
        else if (ScrollPaneConstants.UPPER_RIGHT_CORNER.equals(corner)) {
            xOffset = -r;
            yOffset = 0;
        }
        else if (ScrollPaneConstants.LOWER_LEFT_CORNER.equals(corner)) {
            xOffset = 0;
            yOffset = -r;
        }
        else if (ScrollPaneConstants.LOWER_RIGHT_CORNER.equals(corner)) {
            xOffset = -r;
            yOffset = -r;
        }

        if (grphcs instanceof Graphics2D) {
            ((Graphics2D)grphcs).clip(new RoundRectangle2D.Double(x + xOffset, y + yOffset, w + r, h + r, r * 2, r * 2));
        }

        base.paintIcon(cmpnt, grphcs, x, y);
    }

    @Override
    public int getIconWidth() {
        return base.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return base.getIconHeight();
    }
}
