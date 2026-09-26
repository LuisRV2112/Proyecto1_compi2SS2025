package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.simbolos.SimboloEstructura;
import com.usac.contacto3d.simbolos.SimboloFuncion;

import java.util.List;

/** new Clase(args) de Zetariano y novus Clase(args) de PigLatin. El objeto vive en el heap. */
public class NuevoObjeto extends Expresion {

    private final String clase;
    private final List<Expresion> argumentos;

    public NuevoObjeto(String clase, List<Expresion> argumentos, int linea, int columna) {
        super(linea, columna);
        this.clase = clase;
        this.argumentos = List.copyOf(argumentos);
    }

    public String getClase()               { return clase; }
    public List<Expresion> getArgumentos() { return argumentos; }

    /** La clase, con su tamanio. Lo resuelve el semantico; el generador lo usa en vez de volver a buscar el nombre. */
    private SimboloEstructura simboloClase;

    public SimboloEstructura getSimboloClase() { return simboloClase; }
    public void setSimboloClase(SimboloEstructura simboloClase) { this.simboloClase = simboloClase; }

    /** La sobrecarga elegida; null si la clase no declara constructores (el implicito, sin parametros). */
    private SimboloFuncion constructor;

    public SimboloFuncion getConstructor() { return constructor; }
    public void setConstructor(SimboloFuncion constructor) { this.constructor = constructor; }

    @Override
    public String getEtiqueta() {
        return "Nuevo " + clase + "()";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(argumentos);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarNuevoObjeto(this);
    }
}
