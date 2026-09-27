package com.usac.contacto3d.ui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Iconos del arbol de trabajo, dibujados en codigo: una carpeta, o un cuadro
 * con la inicial del lenguaje (Y, Z, P) en su color. No hacen falta imagenes.
 */
public final class IconoArchivo implements Icon {

    private static final int TAMANIO = 16;

    public static final IconoArchivo CARPETA = new IconoArchivo(new Color(0xDCB67A), null);
    public static final IconoArchivo Y = new IconoArchivo(new Color(0x4EC9B0), "Y");
    public static final IconoArchivo ZETARIANO = new IconoArchivo(new Color(0x569CD6), "Z");
    public static final IconoArchivo PIGLATIN = new IconoArchivo(new Color(0xC586C0), "P");
    public static final IconoArchivo C = new IconoArchivo(new Color(0xD7BA7D), "C");
    public static final IconoArchivo OTRO = new IconoArchivo(new Color(0x858585), "");

    private final Color color;
    private final String letra;

    private IconoArchivo(Color color, String letra) {
        this.color = color;
        this.letra = letra;
    }

    public static IconoArchivo para(Path ruta) {
        if (Files.isDirectory(ruta)) {
            return CARPETA;
        }
        String nombre = ruta.getFileName().toString();
        if (nombre.endsWith(".pig")) return PIGLATIN;
        if (nombre.endsWith(".y")) return Y;
        if (nombre.endsWith(".z")) return ZETARIANO;
        if (nombre.endsWith(".c")) return C;
        return OTRO;
    }

    @Override
    public void paintIcon(Component componente, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        if (letra == null) {
            g2.fillRoundRect(x, y + 3, 7, 4, 2, 2);          // pestania de la carpeta
            g2.fillRoundRect(x, y + 5, TAMANIO - 1, 10, 3, 3);
        } else {
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x + 1, y + 1, TAMANIO - 3, TAMANIO - 3, 4, 4);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            FontMetrics medidas = g2.getFontMetrics();
            int ancho = medidas.stringWidth(letra);
            g2.drawString(letra, x + (TAMANIO - ancho) / 2, y + (TAMANIO + medidas.getAscent()) / 2 - 2);
        }
        g2.dispose();
    }

    @Override public int getIconWidth()  { return TAMANIO; }
    @Override public int getIconHeight() { return TAMANIO; }
}
