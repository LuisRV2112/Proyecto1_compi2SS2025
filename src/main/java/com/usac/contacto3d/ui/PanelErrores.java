package com.usac.contacto3d.ui;

import com.usac.contacto3d.errores.ErrorCompilacion;
import com.usac.contacto3d.errores.ListaErrores;
import com.usac.contacto3d.errores.TipoError;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PanelErrores extends JPanel {

    private final Modelo modelo = new Modelo();
    private final JTable tabla = new JTable(modelo);

    private BiConsumer<String, Integer> alSeleccionar = (archivo, linea) -> { };

    public PanelErrores() {
        super(new BorderLayout());
        setBackground(TemaEditor.FONDO_PANEL);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);
        tabla.setBackground(TemaEditor.FONDO_PANEL);
        tabla.setForeground(TemaEditor.TEXTO);
        tabla.setGridColor(TemaEditor.BORDE);
        tabla.setSelectionBackground(TemaEditor.SELECCION);
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tabla.getTableHeader().setBackground(TemaEditor.BORDE);
        tabla.getTableHeader().setForeground(TemaEditor.TEXTO);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tabla.setDefaultRenderer(Object.class, new Renderer());

        int[] anchos = {90, 160, 60, 70, 130, 600};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
            if (i < anchos.length - 1) {
                tabla.getColumnModel().getColumn(i).setMinWidth(anchos[i] * 3 / 4);
            }
        }

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                if (evento.getClickCount() == 2) {
                    int fila = tabla.getSelectedRow();
                    if (fila >= 0) {
                        ErrorCompilacion error = modelo.errores.get(fila);
                        alSeleccionar.accept(error.getArchivo(), error.getLinea());
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(TemaEditor.FONDO_PANEL);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    public void setAlSeleccionar(BiConsumer<String, Integer> accion) {
        this.alSeleccionar = accion;
    }

    public void mostrar(ListaErrores errores) {
        modelo.reemplazar(errores == null ? List.of() : errores.getErrores());
    }

    public void limpiar() {
        modelo.reemplazar(List.of());
    }

    public int getCantidad() {
        return modelo.errores.size();
    }

    private static class Modelo extends AbstractTableModel {

        private static final String[] COLUMNAS =
                {"Tipo", "Archivo", "Linea", "Columna", "Lexema", "Descripcion"};

        private final List<ErrorCompilacion> errores = new ArrayList<>();

        void reemplazar(List<ErrorCompilacion> nuevos) {
            errores.clear();
            errores.addAll(nuevos);
            fireTableDataChanged();
        }

        @Override public int getRowCount()             { return errores.size(); }
        @Override public int getColumnCount()          { return COLUMNAS.length; }
        @Override public String getColumnName(int col) { return COLUMNAS[col]; }
        @Override public boolean isCellEditable(int f, int c) { return false; }

        @Override
        public Object getValueAt(int fila, int columna) {
            ErrorCompilacion error = errores.get(fila);
            return switch (columna) {
                case 0 -> error.getTipo().getDescripcion();
                case 1 -> error.getArchivo();
                case 2 -> error.getLinea();
                case 3 -> error.getColumna();
                case 4 -> error.getLexema();
                default -> error.getDescripcion();
            };
        }
    }

    private static class Renderer extends DefaultTableCellRenderer {

        private static final Color COLOR_LEXICO     = new Color(0xF44747);
        private static final Color COLOR_SINTACTICO = new Color(0xFF8C42);
        private static final Color COLOR_SEMANTICO  = new Color(0xDCDCAA);

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                                                       boolean seleccionado, boolean enfocado,
                                                       int fila, int columna) {
            Component componente = super.getTableCellRendererComponent(
                    tabla, valor, seleccionado, enfocado, fila, columna);

            if (!seleccionado) {
                componente.setBackground(TemaEditor.FONDO_PANEL);
                String tipo = String.valueOf(tabla.getValueAt(fila, 0));
                componente.setForeground(
                        tipo.equals(TipoError.LEXICO.getDescripcion())     ? COLOR_LEXICO :
                        tipo.equals(TipoError.SINTACTICO.getDescripcion()) ? COLOR_SINTACTICO :
                                                                            COLOR_SEMANTICO);
            }
            return componente;
        }
    }
}
