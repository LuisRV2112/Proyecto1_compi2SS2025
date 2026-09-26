package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.Visitante;

import java.util.List;

/**
 * Valor constante. El valor ya viene convertido: Integer, Double, String (sin
 * comillas y con los escapes resueltos), Character, Boolean, o null para el
 * null de Zetariano.
 */
public class Literal extends Expresion {

    private final Object valor;

    public Literal(Object valor, Tipo tipo, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
        setTipo(tipo);
    }

    public Object getValor() { return valor; }

    @Override
    public String getEtiqueta() {
        String texto = switch (valor) {
            case null -> "null";
            case String s -> "\"" + s.replace("\n", "\\n") + "\"";
            case Character c -> "'" + c + "'";
            default -> valor.toString();
        };
        return texto + " (" + getTipo() + ")";
    }

    @Override
    public List<Nodo> getHijos() {
        return List.of();
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarLiteral(this);
    }
}
