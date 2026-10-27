package com.usac.contacto3d.ui;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Programa;
import com.usac.contacto3d.ast.expresiones.Literal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * El AST de un módulo como diagrama: cajas por nodo, de arriba hacia abajo, con nodos plegables.
 * Calcula las posiciones una vez y las usa para pintar en pantalla y para escribir el .svg, así que
 * los dos se ven igual. No depende de ningún componente: se puede probar sin ventana.
 */
public class DiagramaAst {

    public static final Font FUENTE = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Color FONDO = TemaEditor.FONDO;

    /** Programa en morado, declaraciones en azul, instrucciones en amarillo, expresiones en verde, literales en naranja. */
    public enum Categoria {
        PROGRAMA("Programa", new Color(0xC586C0)),
        DECLARACION("Declaracion", new Color(0x569CD6)),
        INSTRUCCION("Instruccion", new Color(0xDCDCAA)),
        EXPRESION("Expresion", new Color(0x4EC9B0)),
        LITERAL("Literal", new Color(0xCE9178));

        public final String nombre;
        public final Color color;
        public final Color relleno;

        Categoria(String nombre, Color color) {
            this.nombre = nombre;
            this.color = color;
            this.relleno = mezclar(color, FONDO, 0.18);
        }

        static Categoria de(Nodo nodo) {
            if (nodo instanceof Programa) {
                return PROGRAMA;
            }
            if (nodo instanceof Literal) {
                return LITERAL;
            }
            String paquete = nodo.getClass().getPackageName();
            if (paquete.endsWith(".declaraciones")) {
                return DECLARACION;
            }
            if (paquete.endsWith(".instrucciones")) {
                return INSTRUCCION;
            }
            return EXPRESION;
        }
    }

    public static final class Caja {
        public final Nodo nodo;
        public final Categoria categoria;
        public final List<Caja> hijos = new ArrayList<>();
        public final int profundidad;
        public final int descendientes;
        public boolean plegada;
        String texto;
        int x, y, ancho;
        private int anchoSubarbol;

        private Caja(Nodo nodo, int profundidad) {
            this.nodo = nodo;
            this.categoria = Categoria.de(nodo);
            this.profundidad = profundidad;
            int total = 0;
            for (Nodo hijo : nodo.getHijos()) {
                Caja caja = new Caja(hijo, profundidad + 1);
                hijos.add(caja);
                total += 1 + caja.descendientes;
            }
            this.descendientes = total;
        }

        public boolean tieneHijos()        { return !hijos.isEmpty(); }
        public List<Caja> hijosVisibles()  { return plegada ? List.of() : hijos; }
        public Rectangle limites()         { return new Rectangle(x, y, ancho, ALTO); }
    }

    /** Hasta qué profundidad se ve desplegado al principio: el programa, sus clases y funciones, y sus miembros. */
    public static final int NIVEL_INICIAL = 2;

    private static final int ALTO = 26;
    private static final int RELLENO_X = 10;
    private static final int SEPARACION_X = 14;
    private static final int SEPARACION_Y = 44;
    private static final int MARGEN = 24;
    private static final int MAXIMO_TEXTO = 48;

    private final Caja raiz;
    private int ancho;
    private int alto;

    public DiagramaAst(Programa programa) {
        raiz = new Caja(programa, 0);
        contraer();
    }

    public Caja getRaiz() { return raiz; }
    public int getAncho() { return ancho; }
    public int getAlto()  { return alto; }

    public void expandirTodo() {
        recorrer(raiz, c -> c.plegada = false);
    }

    public void contraer() {
        recorrer(raiz, c -> c.plegada = c.tieneHijos() && c.profundidad >= NIVEL_INICIAL);
    }

    /** Recalcula las posiciones con las medidas de la fuente. Hay que llamarlo después de plegar o desplegar. */
    public void ubicar(FontMetrics medidas) {
        medir(raiz, medidas);
        int profundidadMaxima = colocar(raiz, MARGEN, 0);
        ancho = raiz.anchoSubarbol + 2 * MARGEN;
        alto = MARGEN * 2 + (profundidadMaxima + 1) * ALTO + profundidadMaxima * SEPARACION_Y;
    }

    public Caja cajaEn(int px, int py) {
        return buscar(raiz, px, py);
    }

    // ---------------------------------------------------------------- posiciones

    private static void medir(Caja caja, FontMetrics medidas) {
        // Un literal puede traer \n o \t: en pantalla descuadran la caja y en el .svg no son XML válido
        String etiqueta = caja.nodo.getEtiqueta().replaceAll("\\p{Cntrl}", " ");
        if (etiqueta.length() > MAXIMO_TEXTO) {
            etiqueta = etiqueta.substring(0, MAXIMO_TEXTO - 1) + "…";
        }
        if (caja.plegada) {
            etiqueta += "  [+" + caja.descendientes + "]";
        }
        caja.texto = etiqueta;
        caja.ancho = medidas.stringWidth(etiqueta) + 2 * RELLENO_X;

        int hijos = 0;
        for (Caja hijo : caja.hijosVisibles()) {
            medir(hijo, medidas);
            hijos += hijo.anchoSubarbol;
        }
        if (!caja.hijosVisibles().isEmpty()) {
            hijos += SEPARACION_X * (caja.hijosVisibles().size() - 1);
        }
        caja.anchoSubarbol = Math.max(caja.ancho, hijos);
    }

    /** Coloca el subárbol desde {@code izquierda}: el padre centrado sobre sus hijos. Devuelve la profundidad máxima. */
    private static int colocar(Caja caja, int izquierda, int nivel) {
        caja.y = MARGEN + nivel * (ALTO + SEPARACION_Y);
        caja.x = izquierda + (caja.anchoSubarbol - caja.ancho) / 2;
        List<Caja> hijos = caja.hijosVisibles();
        if (hijos.isEmpty()) {
            return nivel;
        }
        int ocupado = SEPARACION_X * (hijos.size() - 1);
        for (Caja hijo : hijos) {
            ocupado += hijo.anchoSubarbol;
        }
        int cursor = izquierda + (caja.anchoSubarbol - ocupado) / 2;
        int maxima = nivel;
        for (Caja hijo : hijos) {
            maxima = Math.max(maxima, colocar(hijo, cursor, nivel + 1));
            cursor += hijo.anchoSubarbol + SEPARACION_X;
        }
        return maxima;
    }

    private static Caja buscar(Caja caja, int px, int py) {
        if (caja.limites().contains(px, py)) {
            return caja;
        }
        for (Caja hijo : caja.hijosVisibles()) {
            Caja encontrada = buscar(hijo, px, py);
            if (encontrada != null) {
                return encontrada;
            }
        }
        return null;
    }

    private static void recorrer(Caja caja, java.util.function.Consumer<Caja> accion) {
        accion.accept(caja);
        caja.hijos.forEach(h -> recorrer(h, accion));
    }

    // ---------------------------------------------------------------- pantalla

    /** Pinta lo que cae dentro de {@code visible} (en coordenadas del diagrama, sin zoom). */
    public void pintar(Graphics2D g, Rectangle visible, Caja resaltada) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(FUENTE);
        pintarAristas(g, raiz, visible);
        pintarCajas(g, raiz, visible, resaltada);
    }

    private void pintarAristas(Graphics2D g, Caja caja, Rectangle visible) {
        g.setColor(TemaEditor.BORDE.brighter());
        g.setStroke(new BasicStroke(1.2f));
        for (Caja hijo : caja.hijosVisibles()) {
            int x1 = caja.x + caja.ancho / 2;
            int y1 = caja.y + ALTO;
            int x2 = hijo.x + hijo.ancho / 2;
            int y2 = hijo.y;
            int medio = (y1 + y2) / 2;
            if (visible.intersects(new Rectangle(Math.min(x1, x2), y1, Math.abs(x2 - x1) + 1, y2 - y1))) {
                Path2D.Float camino = new Path2D.Float();
                camino.moveTo(x1, y1);
                camino.lineTo(x1, medio);
                camino.lineTo(x2, medio);
                camino.lineTo(x2, y2);
                g.draw(camino);
            }
            pintarAristas(g, hijo, visible);
        }
    }

    private void pintarCajas(Graphics2D g, Caja caja, Rectangle visible, Caja resaltada) {
        if (visible.intersects(caja.limites())) {
            RoundRectangle2D.Float forma = new RoundRectangle2D.Float(caja.x, caja.y, caja.ancho, ALTO, 8, 8);
            g.setColor(caja.categoria.relleno);
            g.fill(forma);
            g.setColor(caja == resaltada ? Color.WHITE : caja.categoria.color);
            g.setStroke(new BasicStroke(caja == resaltada || caja.plegada ? 2f : 1.2f));
            g.draw(forma);
            FontMetrics medidas = g.getFontMetrics();
            g.setColor(TemaEditor.TEXTO);
            g.drawString(caja.texto, caja.x + (caja.ancho - medidas.stringWidth(caja.texto)) / 2f,
                    caja.y + (ALTO - medidas.getHeight()) / 2f + medidas.getAscent());
        }
        for (Caja hijo : caja.hijosVisibles()) {
            pintarCajas(g, hijo, visible, resaltada);
        }
    }

    // ---------------------------------------------------------------- svg

    /** El diagrama tal como se ve (con lo plegado plegado), como un .svg independiente. */
    public String aSvg() {
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(String.format(Locale.ROOT,
                   "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">%n",
                   ancho, alto, ancho, alto))
           .append("<title>").append(escapar(raiz.nodo.getEtiqueta())).append("</title>\n")
           .append(String.format("<rect width=\"100%%\" height=\"100%%\" fill=\"%s\"/>%n", hex(FONDO)))
           .append(String.format("<g fill=\"none\" stroke=\"%s\" stroke-width=\"1.2\">%n", hex(TemaEditor.BORDE.brighter())));
        aristasSvg(raiz, svg);
        svg.append("</g>\n")
           .append(String.format("<g font-family=\"DejaVu Sans, Segoe UI, Arial, sans-serif\" font-size=\"%d\" "
                   + "text-anchor=\"middle\" dominant-baseline=\"central\">%n", FUENTE.getSize()));
        cajasSvg(raiz, svg);
        svg.append("</g>\n</svg>\n");
        return svg.toString();
    }

    private static void aristasSvg(Caja caja, StringBuilder svg) {
        for (Caja hijo : caja.hijosVisibles()) {
            int x1 = caja.x + caja.ancho / 2;
            int y1 = caja.y + ALTO;
            int x2 = hijo.x + hijo.ancho / 2;
            int medio = (y1 + hijo.y) / 2;
            svg.append(String.format("<path d=\"M%d %dV%dH%dV%d\"/>%n", x1, y1, medio, x2, hijo.y));
            aristasSvg(hijo, svg);
        }
    }

    private static void cajasSvg(Caja caja, StringBuilder svg) {
        String ubicacion = caja.nodo.getArchivo().isEmpty() ? ""
                : " (" + caja.nodo.getArchivo() + ":" + caja.nodo.getLinea() + ")";
        svg.append("<g><title>").append(escapar(caja.nodo.getEtiqueta() + ubicacion)).append("</title>")
           .append(String.format("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" rx=\"4\" fill=\"%s\" stroke=\"%s\" stroke-width=\"%s\"/>",
                   caja.x, caja.y, caja.ancho, ALTO, hex(caja.categoria.relleno), hex(caja.categoria.color),
                   caja.plegada ? "2" : "1.2"))
           .append(String.format("<text x=\"%d\" y=\"%d\" fill=\"%s\">%s</text></g>%n",
                   caja.x + caja.ancho / 2, caja.y + ALTO / 2, hex(TemaEditor.TEXTO), escapar(caja.texto)));
        for (Caja hijo : caja.hijosVisibles()) {
            cajasSvg(hijo, svg);
        }
    }

    private static String escapar(String texto) {
        return texto.replaceAll("\\p{Cntrl}", " ").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String hex(Color color) {
        return String.format("#%06X", color.getRGB() & 0xFFFFFF);
    }

    private static Color mezclar(Color color, Color fondo, double proporcion) {
        return new Color(
                (int) Math.round(color.getRed() * proporcion + fondo.getRed() * (1 - proporcion)),
                (int) Math.round(color.getGreen() * proporcion + fondo.getGreen() * (1 - proporcion)),
                (int) Math.round(color.getBlue() * proporcion + fondo.getBlue() * (1 - proporcion)));
    }
}
