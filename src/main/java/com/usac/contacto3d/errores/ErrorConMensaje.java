package com.usac.contacto3d.errores;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class ErrorConMensaje extends RecognitionException {

    public ErrorConMensaje(Recognizer<?, ?> reconocedor) {
        super(reconocedor, reconocedor.getInputStream(),
                reconocedor instanceof Parser parser ? parser.getContext() : null);
    }
}
