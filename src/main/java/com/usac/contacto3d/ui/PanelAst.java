package com.usac.contacto3d.ui;

import com.usac.contacto3d.ast.Programa;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PanelAst extends JPanel {

    private static final double ZOOM_MINIMO = 0.2;
    private static final double ZOOM_MAXIMO = 3.0;
    private static final double PASO_ZOOM = 1.2;

    private final JComboBox<String> modulos = new JComboBox<>();
    private final JLabel etiquetaZoom = new JLabel("100%");
    private final Lienzo lienzo = new Lienzo();
    private final JScrollPane scroll = new JScrollPane(lienzo);
    private final List<DiagramaAst> diagramas = new ArrayList<>();

    private DiagramaAst actual;
    private DiagramaAst.Caja bajoElMouse;
    private double escala = 1.0;
    private BiConsumer<String, Integer> alIrALinea = (archivo, linea) -> { };

    public PanelAst() {
        super(new BorderLayout());
        add(crearBarra(), BorderLayout.NORTH);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(DiagramaAst.FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getHorizontalScrollBar().setUnitIncrement(24);
        add(scroll, BorderLayout.CENTER);
        modulos.addActionListener(e -> seleccionar(modulos.getSelectedIndex()));
    }

    public void setAlIrALinea(BiConsumer<String, Integer> accion) {
        this.alIrALinea = accion;
    }

    public void mostrar(List<Programa> programas) {
        diagramas.clear();
        modulos.removeAllItems();
        for (Programa programa : programas) {
            diagramas.add(new DiagramaAst(programa));
            modulos.addItem(programa.getArchivo().isEmpty() ? programa.getEtiqueta() : programa.getArchivo());
        }
        seleccionar(diagramas.isEmpty() ? -1 : 0);
    }

    private JToolBar crearBarra() {
        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.add(new JLabel(" Modulo: "));
        modulos.setPreferredSize(new Dimension(200, 28));
        modulos.setMaximumSize(new Dimension(200, 28));
        barra.add(modulos);
        barra.addSeparator();
        barra.add(boton("Expandir todo", "Despliega todos los nodos", () -> plegado(true)));
        barra.add(boton("Contraer", "Vuelve a la vista inicial: clases, funciones y sus miembros", () -> plegado(false)));
        barra.addSeparator();
        barra.add(boton("-", "Alejar (Ctrl + rueda)", () -> zoomCentrado(escala / PASO_ZOOM)));
        etiquetaZoom.setPreferredSize(new Dimension(48, 20));
        etiquetaZoom.setHorizontalAlignment(JLabel.CENTER);
        barra.add(etiquetaZoom);
        barra.add(boton("+", "Acercar (Ctrl + rueda)", () -> zoomCentrado(escala * PASO_ZOOM)));
        barra.add(boton("Real", "Tamaño real (100%)", () -> zoomCentrado(1.0)));
        barra.add(boton("Ajustar", "Que el diagrama quepa en la vista", this::ajustar));
        barra.addSeparator();
        barra.add(boton("Exportar SVG...", "Guarda el diagrama, tal como se ve, como imagen .svg", this::exportarSvg));
        barra.addSeparator();
        barra.add(leyenda());
        return barra;
    }

    private static JButton boton(String texto, String ayuda, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setToolTipText(ayuda);
        boton.setFocusable(false);
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    private static JPanel leyenda() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        for (DiagramaAst.Categoria categoria : DiagramaAst.Categoria.values()) {
            JLabel etiqueta = new JLabel(categoria.nombre);
            etiqueta.setForeground(categoria.color);
            panel.add(etiqueta);
        }
        return panel;
    }

    private void seleccionar(int indice) {
        actual = indice >= 0 && indice < diagramas.size() ? diagramas.get(indice) : null;
        bajoElMouse = null;
        reubicar();
        centrarEnRaiz();
    }

    private void centrarEnRaiz() {
        if (actual == null) {
            mover(0, 0);
            return;
        }
        DiagramaAst.Caja raiz = actual.getRaiz();
        int centro = (int) ((raiz.x + raiz.ancho / 2.0) * escala);
        mover(centro - scroll.getViewport().getExtentSize().width / 2, 0);
    }

    private void plegado(boolean expandir) {
        if (actual == null) {
            return;
        }
        if (expandir) {
            actual.expandirTodo();
        } else {
            actual.contraer();
        }
        reubicar();
        centrarEnRaiz();
    }

    private void reubicar() {
        if (actual != null) {
            actual.ubicar(lienzo.getFontMetrics(DiagramaAst.FUENTE));
        }
        actualizarTamano();
    }

    private void actualizarTamano() {
        Dimension tamano = actual == null ? new Dimension(400, 40)
                : new Dimension((int) Math.ceil(actual.getAncho() * escala), (int) Math.ceil(actual.getAlto() * escala));
        lienzo.setPreferredSize(tamano);
        lienzo.setSize(tamano);
        lienzo.revalidate();
        lienzo.repaint();
        etiquetaZoom.setText(Math.round(escala * 100) + "%");
    }

    private void zoom(double nueva, Point ancla) {
        nueva = Math.max(ZOOM_MINIMO, Math.min(ZOOM_MAXIMO, nueva));
        JViewport vista = scroll.getViewport();
        Point posicion = vista.getViewPosition();
        double dx = ancla.x / escala;
        double dy = ancla.y / escala;
        int enPantallaX = ancla.x - posicion.x;
        int enPantallaY = ancla.y - posicion.y;
        escala = nueva;
        actualizarTamano();
        mover((int) (dx * escala) - enPantallaX, (int) (dy * escala) - enPantallaY);
    }

    private void zoomCentrado(double nueva) {
        Rectangle visible = scroll.getViewport().getViewRect();
        zoom(nueva, new Point(visible.x + visible.width / 2, visible.y + visible.height / 2));
    }

    private void ajustar() {
        if (actual == null) {
            return;
        }
        Dimension vista = scroll.getViewport().getExtentSize();
        escala = Math.max(ZOOM_MINIMO, Math.min(1.0,
                Math.min(vista.getWidth() / actual.getAncho(), vista.getHeight() / actual.getAlto())));
        actualizarTamano();
        mover(0, 0);
    }

    private void mover(int x, int y) {
        JViewport vista = scroll.getViewport();
        Dimension extension = vista.getExtentSize();
        int maxX = Math.max(0, lienzo.getWidth() - extension.width);
        int maxY = Math.max(0, lienzo.getHeight() - extension.height);
        vista.setViewPosition(new Point(Math.max(0, Math.min(maxX, x)), Math.max(0, Math.min(maxY, y))));
    }

    private void alternar(DiagramaAst.Caja caja) {
        Point posicion = scroll.getViewport().getViewPosition();
        int enPantallaX = (int) (caja.x * escala) - posicion.x;
        int enPantallaY = (int) (caja.y * escala) - posicion.y;
        caja.plegada = !caja.plegada;
        reubicar();
        mover((int) (caja.x * escala) - enPantallaX, (int) (caja.y * escala) - enPantallaY);
    }

    private void exportarSvg() {
        if (actual == null) {
            JOptionPane.showMessageDialog(this, "Primero analiza un programa (F5).", "Exportar SVG",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String modulo = String.valueOf(modulos.getSelectedItem());
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Exportar el AST de " + modulo);
        selector.setFileFilter(new FileNameExtensionFilter("Imagen SVG (*.svg)", "svg"));
        selector.setSelectedFile(new File(System.getProperty("user.home"), modulo.replace('.', '_') + "_ast.svg"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path destino = selector.getSelectedFile().toPath();
        if (!destino.getFileName().toString().toLowerCase().endsWith(".svg")) {
            destino = destino.resolveSibling(destino.getFileName() + ".svg");
        }
        if (Files.exists(destino) && JOptionPane.showConfirmDialog(this, destino.getFileName() + " ya existe. ¿Reemplazarlo?",
                "Exportar SVG", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Files.writeString(destino, actual.aSvg(), StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(this, "AST guardado en " + destino, "Exportar SVG",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar: " + e.getMessage(), "Exportar SVG",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private final class Lienzo extends JComponent {

        private Point inicioArrastre;
        private Point vistaAlArrastrar;

        Lienzo() {
            setOpaque(true);
            setToolTipText("");
            setBorder(BorderFactory.createEmptyBorder());
            MouseAdapter raton = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evento) {
                    DiagramaAst.Caja caja = cajaEn(evento.getPoint());
                    if (caja == null || !SwingUtilities.isLeftMouseButton(evento)) {
                        return;
                    }
                    if (evento.getClickCount() == 1 && caja.tieneHijos()) {
                        alternar(caja);
                    } else if (evento.getClickCount() == 2 && !caja.nodo.getArchivo().isEmpty()) {
                        alIrALinea.accept(caja.nodo.getArchivo(), caja.nodo.getLinea());
                    }
                }

                @Override
                public void mousePressed(MouseEvent evento) {
                    inicioArrastre = evento.getLocationOnScreen();
                    vistaAlArrastrar = scroll.getViewport().getViewPosition();
                }

                @Override
                public void mouseDragged(MouseEvent evento) {
                    if (inicioArrastre == null) {
                        return;
                    }
                    Point ahora = evento.getLocationOnScreen();
                    mover(vistaAlArrastrar.x - (ahora.x - inicioArrastre.x),
                            vistaAlArrastrar.y - (ahora.y - inicioArrastre.y));
                }

                @Override
                public void mouseReleased(MouseEvent evento) {
                    inicioArrastre = null;
                }

                @Override
                public void mouseMoved(MouseEvent evento) {
                    DiagramaAst.Caja caja = cajaEn(evento.getPoint());
                    if (caja != bajoElMouse) {
                        bajoElMouse = caja;
                        repaint();
                    }
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent evento) {
                    if (evento.isControlDown()) {
                        zoom(evento.getWheelRotation() < 0 ? escala * PASO_ZOOM : escala / PASO_ZOOM, evento.getPoint());
                    } else {
                        scroll.dispatchEvent(SwingUtilities.convertMouseEvent(Lienzo.this, evento, scroll));
                    }
                }
            };
            addMouseListener(raton);
            addMouseMotionListener(raton);
            addMouseWheelListener(raton);
        }

        private DiagramaAst.Caja cajaEn(Point punto) {
            return actual == null ? null : actual.cajaEn((int) (punto.x / escala), (int) (punto.y / escala));
        }

        @Override
        public String getToolTipText(MouseEvent evento) {
            DiagramaAst.Caja caja = cajaEn(evento.getPoint());
            if (caja == null) {
                return null;
            }
            String ubicacion = caja.nodo.getArchivo().isEmpty() ? ""
                    : "<br>" + caja.nodo.getArchivo() + ", linea " + caja.nodo.getLinea() + " (doble clic para ir)";
            String plegado = !caja.tieneHijos() ? ""
                    : "<br>" + (caja.plegada ? "Clic para desplegar " + caja.descendientes + " nodo(s)" : "Clic para plegar");
            return "<html>" + caja.nodo.getEtiqueta().replace("&", "&amp;").replace("<", "&lt;")
                    + ubicacion + plegado + "</html>";
        }

        @Override
        protected void paintComponent(Graphics grafico) {
            Graphics2D g = (Graphics2D) grafico.create();
            g.setColor(DiagramaAst.FONDO);
            Rectangle recorte = g.getClipBounds() != null ? g.getClipBounds() : new Rectangle(getSize());
            g.fill(recorte);
            if (actual == null) {
                g.setColor(TemaEditor.TEXTO);
                g.setFont(DiagramaAst.FUENTE);
                g.drawString("Analiza un programa (F5) para ver su AST.", 16, 24);
            } else {
                g.scale(escala, escala);
                Rectangle visible = new Rectangle(
                        (int) (recorte.x / escala) - 1, (int) (recorte.y / escala) - 1,
                        (int) Math.ceil(recorte.width / escala) + 2, (int) Math.ceil(recorte.height / escala) + 2);
                actual.pintar(g, visible, bajoElMouse);
            }
            g.dispose();
        }
    }
}
