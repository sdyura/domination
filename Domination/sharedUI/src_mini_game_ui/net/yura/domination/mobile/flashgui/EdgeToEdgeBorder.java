package net.yura.domination.mobile.flashgui;

import net.yura.domination.engine.ColorUtil;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.border.Border;
import net.yura.mobile.gui.border.MatteBorder;
import net.yura.mobile.gui.components.Component;

public class EdgeToEdgeBorder implements Border {

    private final Border border;

    public EdgeToEdgeBorder(Border border) {
        this.border = border;
    }

    public EdgeToEdgeBorder(int color) {
        MatteBorder tint = new MatteBorder(0, 0, 0, 0, color);
        tint.paintCenter = true;
        border = tint;
    }

    public void paintBorder(Component cmpnt, Graphics2D g, int w, int h) {

        DesktopPane dp = cmpnt.getDesktopPane();
        int xOnScreen = cmpnt.getXOnScreen();
        int yOnScreen = cmpnt.getYOnScreen();
        int rOnScreen = xOnScreen + w;
        int bOnScreen = yOnScreen + h;

        boolean fillLeft = xOnScreen == 0;
        boolean fillTop = yOnScreen == 0;
        boolean fillRight = rOnScreen == dp.getWidth();
        boolean fillBottom = bOnScreen == dp.getHeight();

        int clipXOnScreen = xOnScreen + g.getClipX();
        int clipYOnScreen = yOnScreen + g.getClipY();
        int clipROnScreen = clipXOnScreen + g.getClipWidth();
        int clipBOnScreen = clipYOnScreen + g.getClipHeight();

        int startX = (fillLeft && clipXOnScreen < 0) ? clipXOnScreen: 0;
        int startY = (fillTop && clipYOnScreen < 0) ? clipYOnScreen: 0;
        int width = ((fillRight && clipROnScreen > rOnScreen) ? clipROnScreen - xOnScreen : w) - startX;
        int height = ((fillBottom && clipBOnScreen > bOnScreen) ? clipBOnScreen - yOnScreen : h) - startY;

        g.translate(startX, startY);
        border.paintBorder(cmpnt, g, width, height);
        g.translate(-startX, -startY);

        // for debugging:
        //g.setColor(0xFFFF0000);
        //g.drawRect(startX, startY, width-1, height-1);
    }

    public int getTop() {
        return 0;
    }

    public int getBottom() {
        return 0;
    }

    public int getRight() {
        return 0;
    }

    public int getLeft() {
        return 0;
    }

    public boolean isBorderOpaque() {
        return border.isBorderOpaque();
    }
}
