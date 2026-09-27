package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.expresiones.Expresion;

import java.util.List;

public class Elegir extends Nodo implements Instruccion {

    private final Expresion valor;
    private final List<Caso> casos;

    public Elegir(Expresion valor, List<Caso> casos, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
        this.casos = List.copyOf(casos);
    }

    public Expresion getValor()  { return valor; }
    public List<Caso> getCasos() { return casos; }

    @Override
    public String getEtiqueta() {
        return "Elegir";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(valor, casos);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarElegir(this);
    }
}
