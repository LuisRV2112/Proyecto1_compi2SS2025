package com.usac.contacto3d;

import com.usac.contacto3d.ui.TemaOscuro;
import com.usac.contacto3d.ui.VentanaPrincipal;

import javax.swing.SwingUtilities;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Punto de entrada de la interfaz. Con una carpeta como argumento la abre
 * como arbol de trabajo; sin argumentos reabre la ultima que se uso.
 *
 * Para compilar desde consola, sin ventana, esta Compilador.main.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TemaOscuro.aplicar();
            VentanaPrincipal ventana = new VentanaPrincipal();
            if (args.length > 0 && Files.isDirectory(Path.of(args[0]))) {
                ventana.abrirCarpeta(Path.of(args[0]));
            }
            ventana.setVisible(true);
        });
    }
}
