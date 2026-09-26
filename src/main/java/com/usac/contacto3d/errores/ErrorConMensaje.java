package com.usac.contacto3d.errores;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Marca un error que detecta nuestro propio codigo (acciones de la gramatica
 * o el manejo de indentacion) y que ya trae su mensaje en espanol.
 *
 * Hace falta porque ANTLR tambien reporta algunos errores sin excepcion
 * ("missing X", "extraneous input"): sin esta marca los listeners no sabrian
 * si el mensaje es nuestro o el de ANTLR en ingles.
 */
public class ErrorConMensaje extends RecognitionException {

    public ErrorConMensaje(Recognizer<?, ?> reconocedor) {
        super(reconocedor, reconocedor.getInputStream(),
                reconocedor instanceof Parser parser ? parser.getContext() : null);
    }
}
