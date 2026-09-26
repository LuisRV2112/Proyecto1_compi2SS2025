package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;

import java.util.List;

/**
 * Valores entre llaves: {1, 2, 3}, { {1, 2}, {3, 4} } o {"Calle Real", 42}.
 *
 * Es un solo nodo para arreglos y estructuras a proposito: los literales son
 * posicionales, asi que { {1, 2}, x } es identico si inicializa una matriz o
 * una estructura con otra anidada. Solo el tipo esperado, que conoce el
 * semantico, dice cual es; el semantico lo deja en getTipo().
 */
public class LiteralLista extends Expresion {

    private final List<Expresion> elementos;

    public LiteralLista(List<Expresion> elementos, int linea, int columna) {
        super(linea, columna);
        this.elementos = List.copyOf(elementos);
    }

    public List<Expresion> getElementos() { return elementos; }

    @Override
    public String getEtiqueta() {
        return "{ } (" + elementos.size() + ")";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(elementos);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarLiteralLista(this);
    }
}
