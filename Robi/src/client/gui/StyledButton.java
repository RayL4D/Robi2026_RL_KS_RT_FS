package client.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

/**
 * Bouton stylise avec le theme sombre Robi.
 * Gere le hover, le click, et les variantes (primaire, secondaire, danger).
 */
public class StyledButton extends JButton {

    private Color bgColor;
    private Color hoverColor;
    private Color pressColor;
    private boolean hovered = false;
    private boolean pressed = false;

    /**
     * Cree un bouton avec le style par defaut (fond clair, accent au survol).
     *
     * @param text le texte du bouton
     */
    public StyledButton(String text) {
        this(text, Theme.BG_LIGHT, Theme.ACCENT, Theme.ACCENT_DARK);
    }

    /**
     * Cree un bouton avec des couleurs personnalisees.
     *
     * @param text le texte du bouton
     * @param bg la couleur de fond normale
     * @param hover la couleur de fond au survol
     * @param press la couleur de fond au clic
     */
    public StyledButton(String text, Color bg, Color hover, Color press) {
        super(text);
        this.bgColor = bg;
        this.hoverColor = hover;
        this.pressColor = press;

        setFont(Theme.FONT_UI);
        setForeground(Theme.TEXT_PRIMARY);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    /**
     * Cree un bouton avec le style primaire (accent bleu).
     *
     * @param text le texte du bouton
     * @return le bouton stylise primaire
     */
    public static StyledButton primary(String text) {
        return new StyledButton(text, Theme.ACCENT, Theme.ACCENT_HOVER, Theme.ACCENT_DARK);
    }

    /**
     * Cree un bouton avec le style danger (rouge).
     *
     * @param text le texte du bouton
     * @return le bouton stylise danger
     */
    public static StyledButton danger(String text) {
        return new StyledButton(text, Theme.ERROR, new Color(255, 110, 110), new Color(200, 60, 60));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg;
        if (!isEnabled()) {
            bg = Theme.BG_MEDIUM;
            setForeground(Theme.TEXT_MUTED);
        } else if (pressed) {
            bg = pressColor;
            setForeground(Color.WHITE);
        } else if (hovered) {
            bg = hoverColor;
            setForeground(Color.WHITE);
        } else {
            bg = bgColor;
            setForeground(Theme.TEXT_PRIMARY);
        }

        g2d.setColor(bg);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

        g2d.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width + 20, Math.max(d.height, 32));
    }
}
