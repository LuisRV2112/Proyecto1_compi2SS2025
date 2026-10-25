package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.expresiones.Expresion;
import com.usac.contacto3d.simbolos.SimboloFuncion;

import java.util.List;

/** super(args): llama al constructor de la clase padre. Solo es valida dentro de un constructor. */
public class LlamadaSuper extends Nodo implements Instruccion {

    private final List<Expresion> argumentos;

    public LlamadaSuper(List<Expresion> argumentos, int linea, int columna) {
        super(linea, columna);
        this.argumentos = List.copyOf(argumentos);
    }

    public List<Expresion> getArgumentos() { return argumentos; }

    private SimboloFuncion constructor;

    public SimboloFuncion getConstructor() { return constructor; }
    public void setConstructor(SimboloFuncion constructor) { this.constructor = constructor; }

    @Override
    public String getEtiqueta() {
        return "Llamada super()";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(argumentos);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarLlamadaSuper(this);
    }
}
