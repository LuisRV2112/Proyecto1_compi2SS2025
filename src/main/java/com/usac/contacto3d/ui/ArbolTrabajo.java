package com.usac.contacto3d.ui;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class ArbolTrabajo extends JPanel {

    public interface Escucha {
        void abrir(Path archivo);
        void renombrado(Path antes, Path despues);
        void eliminado(Path ruta);
    }

    private final DefaultTreeModel modelo =
            new DefaultTreeModel(new DefaultMutableTreeNode("Abri una carpeta: Archivo > Abrir carpeta"));
    private final JTree arbol = new JTree(modelo);
    private Path raiz;
    private Escucha escucha;

    public ArbolTrabajo() {
        super(new BorderLayout());
        arbol.setRootVisible(true);
        arbol.setShowsRootHandles(true);
        arbol.setRowHeight(22);
        arbol.setCellRenderer(new Renderer());
        arbol.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                Path ruta = rutaEn(evento);
                if (evento.getClickCount() == 2 && ruta != null && Files.isRegularFile(ruta) && escucha != null) {
                    escucha.abrir(ruta);
                }
            }

            @Override public void mousePressed(MouseEvent evento)  { menuContextual(evento); }
            @Override public void mouseReleased(MouseEvent evento) { menuContextual(evento); }
        });
        JScrollPane scroll = new JScrollPane(arbol);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    public void setEscucha(Escucha escucha) {
        this.escucha = escucha;
    }

    public Path getRaiz() {
        return raiz;
    }

    public void abrirCarpeta(Path carpeta) {
        raiz = carpeta.toAbsolutePath().normalize();
        actualizar();
        arbol.expandRow(0);
    }

    public void actualizar() {
        if (raiz == null) {
            return;
        }
        Set<Path> desplegadas = new HashSet<>();
        Enumeration<TreePath> abiertas = arbol.getExpandedDescendants(new TreePath(modelo.getRoot()));
        while (abiertas != null && abiertas.hasMoreElements()) {
            if (((DefaultMutableTreeNode) abiertas.nextElement().getLastPathComponent()).getUserObject() instanceof Path p) {
                desplegadas.add(p);
            }
        }
        DefaultMutableTreeNode nodoRaiz = construir(raiz);
        modelo.setRoot(nodoRaiz);
        desplegar(nodoRaiz, desplegadas);
    }

    public Optional<Path> buscar(String nombreArchivo) {
        if (raiz == null) {
            return Optional.empty();
        }
        try (Stream<Path> recorrido = Files.walk(raiz)) {
            return recorrido.filter(p -> p.getFileName().toString().equals(nombreArchivo)).findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private DefaultMutableTreeNode construir(Path ruta) {
        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(ruta);
        if (Files.isDirectory(ruta)) {
            for (Path hijo : hijosOrdenados(ruta)) {
                nodo.add(construir(hijo));
            }
        }
        return nodo;
    }

    private static List<Path> hijosOrdenados(Path carpeta) {
        try (Stream<Path> hijos = Files.list(carpeta)) {
            return hijos.filter(p -> !p.getFileName().toString().startsWith("."))
                    .sorted(Comparator.comparing((Path p) -> !Files.isDirectory(p))
                            .thenComparing(p -> p.getFileName().toString().toLowerCase()))
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private void desplegar(DefaultMutableTreeNode nodo, Set<Path> desplegadas) {
        if (nodo.getUserObject() instanceof Path p && desplegadas.contains(p)) {
            arbol.expandPath(new TreePath(nodo.getPath()));
        }
        for (int i = 0; i < nodo.getChildCount(); i++) {
            desplegar((DefaultMutableTreeNode) nodo.getChildAt(i), desplegadas);
        }
    }

    private void menuContextual(MouseEvent evento) {
        if (!evento.isPopupTrigger() || raiz == null) {
            return;
        }
        int fila = arbol.getRowForLocation(evento.getX(), evento.getY());
        if (fila >= 0) {
            arbol.setSelectionRow(fila);
        }
        Path seleccion = seleccionada().orElse(raiz);
        boolean esRaiz = seleccion.equals(raiz);

        JPopupMenu menu = new JPopupMenu();
        menu.add(item("Nuevo archivo...", () -> crear(false)));
        menu.add(item("Nueva carpeta...", () -> crear(true)));
        menu.addSeparator();
        JMenuItem renombrar = item("Renombrar...", () -> renombrar(seleccion));
        JMenuItem eliminar = item("Eliminar", () -> eliminar(seleccion));
        renombrar.setEnabled(!esRaiz);
        eliminar.setEnabled(!esRaiz);
        menu.add(renombrar);
        menu.add(eliminar);
        menu.addSeparator();
        menu.add(item("Actualizar", this::actualizar));
        menu.show(arbol, evento.getX(), evento.getY());
    }

    private static JMenuItem item(String texto, Runnable accion) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(e -> accion.run());
        return item;
    }

    public void crear(boolean esCarpeta) {
        if (raiz == null) {
            return;
        }
        Path seleccion = seleccionada().orElse(raiz);
        Path carpeta = Files.isDirectory(seleccion) ? seleccion : seleccion.getParent();
        String nombre = pedirNombre(esCarpeta ? "Nombre de la carpeta:" : "Nombre del archivo (con extension .y, .z o .pig):", "");
        if (nombre == null) {
            return;
        }
        Path nueva = carpeta.resolve(nombre);
        try {
            if (esCarpeta) {
                Files.createDirectories(nueva);
            } else {
                Files.createFile(nueva);
            }
            actualizar();
            if (!esCarpeta && escucha != null) {
                escucha.abrir(nueva);
            }
        } catch (IOException e) {
            mostrarError("No se pudo crear '" + nombre + "': " + e.getMessage());
        }
    }

    private void renombrar(Path ruta) {
        String nombre = pedirNombre("Nuevo nombre:", ruta.getFileName().toString());
        if (nombre == null || nombre.equals(ruta.getFileName().toString())) {
            return;
        }
        Path destino = ruta.resolveSibling(nombre);
        try {
            Files.move(ruta, destino);
            actualizar();
            if (escucha != null) {
                escucha.renombrado(ruta, destino);
            }
        } catch (IOException e) {
            mostrarError("No se pudo renombrar: " + e.getMessage());
        }
    }

    private void eliminar(Path ruta) {
        String que = Files.isDirectory(ruta) ? "la carpeta '" + ruta.getFileName() + "' y todo su contenido"
                : "el archivo '" + ruta.getFileName() + "'";
        int respuesta = JOptionPane.showConfirmDialog(this, "Se va a eliminar " + que + ". No se puede deshacer.",
                "Eliminar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }
        try (Stream<Path> recorrido = Files.walk(ruta)) {
            List<Path> todo = new ArrayList<>(recorrido.toList());
            todo.sort(Comparator.reverseOrder());
            for (Path p : todo) {
                Files.delete(p);
            }
            actualizar();
            if (escucha != null) {
                escucha.eliminado(ruta);
            }
        } catch (IOException e) {
            mostrarError("No se pudo eliminar: " + e.getMessage());
        }
    }

    private String pedirNombre(String mensaje, String inicial) {
        String nombre = (String) JOptionPane.showInputDialog(this, mensaje, "Arbol de trabajo",
                JOptionPane.PLAIN_MESSAGE, null, null, inicial);
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        nombre = nombre.trim();
        if (nombre.contains("/") || nombre.contains("\\")) {
            mostrarError("El nombre no puede contener barras.");
            return null;
        }
        return nombre;
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Arbol de trabajo", JOptionPane.ERROR_MESSAGE);
    }

    private Optional<Path> seleccionada() {
        TreePath camino = arbol.getSelectionPath();
        if (camino != null && ((DefaultMutableTreeNode) camino.getLastPathComponent()).getUserObject() instanceof Path p) {
            return Optional.of(p);
        }
        return Optional.empty();
    }

    private Path rutaEn(MouseEvent evento) {
        TreePath camino = arbol.getPathForLocation(evento.getX(), evento.getY());
        if (camino != null && ((DefaultMutableTreeNode) camino.getLastPathComponent()).getUserObject() instanceof Path p) {
            return p;
        }
        return null;
    }

    private static class Renderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree arbol, Object valor, boolean seleccionado,
                                                      boolean expandido, boolean hoja, int fila, boolean foco) {
            super.getTreeCellRendererComponent(arbol, valor, seleccionado, expandido, hoja, fila, foco);
            if (((DefaultMutableTreeNode) valor).getUserObject() instanceof Path ruta) {
                setText(ruta.getFileName() == null ? ruta.toString() : ruta.getFileName().toString());
                setIcon(IconoArchivo.para(ruta));
            } else {
                setIcon(null);
            }
            return this;
        }
    }
}
