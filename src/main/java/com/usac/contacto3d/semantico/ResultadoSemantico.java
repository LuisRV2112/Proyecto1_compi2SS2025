package com.usac.contacto3d.semantico;

import com.usac.contacto3d.ast.Programa;
import com.usac.contacto3d.simbolos.SimboloFuncion;
import com.usac.contacto3d.simbolos.TablaSimbolos;

import java.util.List;

public record ResultadoSemantico(List<Programa> modulos, TablaSimbolos tabla,
                                 SimboloFuncion principal, int tamanioGlobales) {
}
