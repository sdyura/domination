package net.yura.domination.lobby.client;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import net.yura.domination.guishared.RiskUIUtil;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.swing.GraphicsUtil;
import net.yura.swing.ImageIcon;

public class GameSidePanel {

    private final static String product = RiskUtil.GAME_NAME + " Lobby Client";
    private final static String version = "0.3";
    
    private JSplitPane me;

    private JLabel nameLabel;

    public void setGameName(String name) {
	nameLabel.setText(name);
    }

    public GameSidePanel(JProgressBar timer, JButton startButton, JPanel playerListArea, JPanel chatBoxArea) {
        ResourceBundle resb = TranslationBundle.getBundle();

        //setReplay(false);

        final ImageIcon borderimage = new ImageIcon( GameSidePanel.class.getResource("back.jpg") );


        final Box sidepanelTop = new Box(javax.swing.BoxLayout.Y_AXIS);
        final Box sidepanelBottom = new Box(javax.swing.BoxLayout.Y_AXIS);/* {

                public void paintComponent(java.awt.Graphics g) {

                        java.awt.Image img = borderimage.getImage();

                        int w = img.getWidth(this);
                        int h = img.getHeight(this);

                        for (int i = 0; i < getWidth(); i += w) for (int j = 0; j < getHeight(); j += h) {

                                g.drawImage(img, i, j, this);

                        }
                }

        };*/




        final JButton aboutButton = new JButton( resb.getString( "mainmenu.about") );

        aboutButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                        RiskUIUtil.openAbout(RiskUIUtil.findParentFrame(me), product, version);
                }
        });


        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel( new GridLayout(1,2,5,5) );
        panel3.setBorder( new EmptyBorder(5,5,5,5) );

        Insets insets = new Insets( startButton.getMargin().top ,0, startButton.getMargin().bottom ,0);
        startButton.setMargin(insets);
        aboutButton.setMargin(insets);

        nameLabel = new JLabel();

        panel1.add( nameLabel );
        panel2.add( timer );
        panel3.add( startButton );
        panel3.add( aboutButton );

        sidepanelTop.add( panel1 );
        sidepanelTop.add( panel2 );
        sidepanelTop.add( playerListArea );
        sidepanelBottom.add( panel3 );
        sidepanelBottom.add( chatBoxArea );

        int bigPadding = 20;

        JSplitPane sidepanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        sidepanel.setDividerSize(bigPadding);
        sidepanel.setTopComponent(sidepanelTop);
        sidepanel.setBottomComponent(sidepanelBottom);

        panel2.setBorder( BorderFactory.createMatteBorder(bigPadding, 0, bigPadding, 0, borderimage ) );
        ((BasicSplitPaneUI)sidepanel.getUI()).getDivider().setBorder( BorderFactory.createMatteBorder(0, 0, bigPadding, 0, borderimage ) );
        chatBoxArea.setBorder( BorderFactory.createMatteBorder(bigPadding, 0, 0, 0, borderimage ) );
        sidepanel.setBorder( BorderFactory.createMatteBorder(bigPadding, bigPadding, bigPadding, bigPadding, borderimage ) );


        playerListArea.setPreferredSize(GraphicsUtil.newDimension(160, 120));

        sidepanel.setPreferredSize(GraphicsUtil.newDimension(200, 600));

        me = sidepanel;
    }

    public JSplitPane getPanel() {
        return me;
    }
}
