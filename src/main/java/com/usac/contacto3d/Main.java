package com.usac.contacto3d;

import com.usac.contacto3d.ui.TemaOscuro;

/**
 * Punto de entrada.
 *
 * Por ahora solo aplica el tema y avisa el estado del proyecto; la ventana
 * principal se construye en la fase de interfaz.
 */
public class Main {

    public static void main(String[] args) {
        String tema = TemaOscuro.aplicar();
        System.out.println("Contacto 3xtrat3rr3str3D");
        System.out.println("Tema aplicado: " + tema);
        System.out.println();
        System.out.println("Esqueleto listo. Siguiente paso: las gramaticas (FASE_1).");
    }
}
