package client.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * FlowLayout ameliore qui recalcule la hauteur preferee du conteneur
 * lorsque les composants passent a la ligne suivante.
 * Le FlowLayout standard ne met pas a jour preferredSize quand il wrappe,
 * ce qui cause des composants masques. WrapLayout corrige ce comportement.
 */
public class WrapLayout extends FlowLayout {

    /**
     * Construit un WrapLayout avec l'alignement et les espaces specifies.
     *
     * @param align l'alignement horizontal (LEFT, CENTER, RIGHT)
     * @param hgap l'espace horizontal entre les composants
     * @param vgap l'espace vertical entre les composants
     */
    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return computeSize(target);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension d = computeSize(target);
        d.width = 0;
        return d;
    }

    private Dimension computeSize(Container target) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getWidth();

            // Au premier affichage, la largeur est 0 -> fallback sur le parent
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }

            Insets insets = target.getInsets();
            int maxWidth = targetWidth - insets.left - insets.right;
            int hgap = getHgap();
            int vgap = getVgap();

            int x = 0;
            int y = insets.top + vgap;
            int rowHeight = 0;

            for (int i = 0; i < target.getComponentCount(); i++) {
                Component c = target.getComponent(i);
                if (!c.isVisible()) {
                    continue;
                }

                Dimension d = c.getPreferredSize();

                // Passage a la ligne si depassement
                if (x > 0 && x + d.width > maxWidth) {
                    y += rowHeight + vgap;
                    x = 0;
                    rowHeight = 0;
                }

                x += d.width + hgap;
                rowHeight = Math.max(rowHeight, d.height);
            }

            y += rowHeight + vgap + insets.bottom;

            return new Dimension(targetWidth, y);
        }
    }
}
