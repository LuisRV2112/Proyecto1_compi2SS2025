package com.usac.contacto3d.ui;

import com.usac.contacto3d.simbolos.Simbolo;
import com.usac.contacto3d.simbolos.TablaSimbolos;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class PanelTablaSimbolos extends JPanel {

    private static final String[] COLUMNAS =
            {"Categoria", "Nombre", "Tipo", "Ambito", "Zona", "Posicion", "Celdas", "Archivo", "Linea", "Detalle"};

    private final List<Simbolo> simbolos = new ArrayList<>();
    private final Modelo modelo = new Modelo();

    public PanelTablaSimbolos() {
        super(new BorderLayout());
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(22);
        tabla.setBackground(TemaEditor.FONDO_PANEL);
        tabla.setForeground(TemaEditor.TEXTO);
        tabla.setGridColor(TemaEditor.BORDE);
        tabla.setSelectionBackground(TemaEditor.SELECCION);
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tabla.setAutoCreateRowSorter(true);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        int[] anchos = {90, 150, 110, 150, 70, 70, 60, 120, 50, 400};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(TemaEditor.FONDO_PANEL);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    public void mostrar(TablaSimbolos tabla) {
        simbolos.clear();
        if (tabla != null) {
            simbolos.addAll(tabla.getHistorial());
        }
        modelo.fireTableDataChanged();
    }

    private class Modelo extends AbstractTableModel {
        @Override public int getRowCount()             { return simbolos.size(); }
        @Override public int getColumnCount()          { return COLUMNAS.length; }
        @Override public String getColumnName(int col) { return COLUMNAS[col]; }

        @Override
        public Class<?> getColumnClass(int columna) {
            return columna == 5 || columna == 6 || columna == 8 ? Integer.class : String.class;
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            Simbolo s = simbolos.get(fila);
            boolean sinMemoria = s.getAlmacenamiento() == Simbolo.Almacenamiento.NINGUNO;
            return switch (columna) {
                case 0 -> s.getCategoria().getDescripcion();
                case 1 -> s.getNombre();
                case 2 -> String.valueOf(s.getTipo());
                case 3 -> s.getNombreAmbito();
                case 4 -> sinMemoria ? "-" : s.getAlmacenamiento().name();
                case 5 -> sinMemoria ? null : s.getPosicion();
                case 6 -> s.getTamanio();
                case 7 -> s.getArchivo();
                case 8 -> s.getLinea();
                default -> s.getDetalle();
            };
        }
    }
}
