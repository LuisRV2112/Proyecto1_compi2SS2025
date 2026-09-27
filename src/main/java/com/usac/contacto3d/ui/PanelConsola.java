package com.usac.contacto3d.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;

public class PanelConsola extends JPanel {

    private final JTextArea entrada = area(true);
    private final JTextArea salida = area(false);

    public PanelConsola() {
        super(new BorderLayout());
        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                seccion("Entrada del programa (una linea por lectura)", entrada),
                seccion("Salida", salida));
        division.setResizeWeight(0.3);
        division.setBorder(null);
        add(division, BorderLayout.CENTER);
    }

    public String getEntrada() {
        return entrada.getText();
    }

    public void mostrar(String texto) {
        salida.setText(texto);
        salida.setCaretPosition(0);
    }

    private static JTextArea area(boolean editable) {
        JTextArea area = new JTextArea();
        area.setEditable(editable);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBackground(TemaEditor.FONDO);
        area.setForeground(TemaEditor.TEXTO);
        area.setCaretColor(TemaEditor.CURSOR);
        area.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        return area;
    }

    private static JPanel seccion(String titulo, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel etiqueta = new JLabel(" " + titulo);
        etiqueta.setForeground(TemaEditor.TEXTO);
        panel.add(etiqueta, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
}
