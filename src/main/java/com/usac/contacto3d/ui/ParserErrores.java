package com.usac.contacto3d.ui;

import com.usac.contacto3d.errores.ErrorCompilacion;
import com.usac.contacto3d.errores.ListaErrores;
import com.usac.contacto3d.errores.TipoError;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;

import javax.swing.text.Element;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Marca los errores sobre el propio codigo, con subrayado ondulado y tooltip.
 *
 * NO vuelve a analizar nada: recibe la lista de errores que ya produjo el
 * compilador y solo la traduce a marcas que RSyntaxTextArea sabe dibujar.
 *
 * Filtra por archivo, porque el editor muestra uno a la vez pero la lista
 * contiene los errores de todos los archivos compilados.
 */
public class ParserErrores extends AbstractParser {

    private static final Color COLOR_LEXICO     = new Color(0xF44747);
    private static final Color COLOR_SINTACTICO = new Color(0xFF8C42);
    private static final Color COLOR_SEMANTICO  = new Color(0xDCDCAA);

    private List<ErrorCompilacion> errores = new ArrayList<>();
    private String archivoMostrado = "";

    /** Actualiza las marcas del archivo que se esta viendo. */
    public void setErrores(ListaErrores lista, String archivoMostrado) {
        this.archivoMostrado = archivoMostrado == null ? "" : archivoMostrado;
        this.errores = new ArrayList<>();
        if (lista == null) {
            return;
        }
        for (ErrorCompilacion error : lista.getErrores()) {
            if (error.getArchivo().isEmpty() || error.getArchivo().equals(this.archivoMostrado)) {
                errores.add(error);
            }
        }
    }

    public void limpiar() {
        this.errores = new ArrayList<>();
    }

    @Override
    public ParseResult parse(RSyntaxDocument documento, String estilo) {
        DefaultParseResult resultado = new DefaultParseResult(this);

        Element raiz = documento.getDefaultRootElement();
        int totalLineas = raiz.getElementCount();

        for (ErrorCompilacion error : errores) {
            int linea = error.getLinea() - 1;   // RSyntaxTextArea cuenta desde 0
            if (linea < 0 || linea >= totalLineas) {
                continue;
            }

            Element elemento = raiz.getElement(linea);
            int inicioLinea = elemento.getStartOffset();
            int finLinea = elemento.getEndOffset() - 1;

            int inicio = Math.min(inicioLinea + Math.max(0, error.getColumna() - 1), finLinea);
            int largo = error.getLexema().isEmpty() ? finLinea - inicio : error.getLexema().length();

            // Algunos errores se ubican en la declaracion pero hablan de otra palabra de la
            // linea (el tipo no declarado de un campo): se subraya esa palabra, la mas cercana
            int encontrado = buscarEnLinea(documento, inicioLinea, finLinea, error.getLexema(), inicio);
            if (encontrado >= 0) {
                inicio = encontrado;
            }
            largo = Math.max(1, Math.min(largo, finLinea - inicio));

            DefaultParserNotice aviso = new DefaultParserNotice(
                    this, "[" + error.getTipo().getDescripcion() + "] " + error.getDescripcion(),
                    linea, inicio, largo);

            aviso.setLevel(error.getTipo() == TipoError.SEMANTICO
                    ? ParserNotice.Level.WARNING
                    : ParserNotice.Level.ERROR);
            aviso.setColor(colorDe(error.getTipo()));

            resultado.addNotice(aviso);
        }
        return resultado;
    }

    /** Posicion de la aparicion del lexema mas cercana a 'cerca', o -1 si no esta en la linea. */
    private static int buscarEnLinea(RSyntaxDocument documento, int inicioLinea, int finLinea,
                                     String lexema, int cerca) {
        if (lexema.isBlank() || lexema.startsWith("<")) {
            return -1;   // "<fin de linea>" y compania no son texto de la linea
        }
        try {
            String texto = documento.getText(inicioLinea, finLinea - inicioLinea);
            int mejor = -1;
            for (int i = texto.indexOf(lexema); i >= 0; i = texto.indexOf(lexema, i + 1)) {
                int posicion = inicioLinea + i;
                if (mejor < 0 || Math.abs(posicion - cerca) < Math.abs(mejor - cerca)) {
                    mejor = posicion;
                }
            }
            return mejor;
        } catch (javax.swing.text.BadLocationException e) {
            return -1;
        }
    }

    private Color colorDe(TipoError tipo) {
        return switch (tipo) {
            case LEXICO     -> COLOR_LEXICO;
            case SINTACTICO -> COLOR_SINTACTICO;
            case SEMANTICO  -> COLOR_SEMANTICO;
        };
    }
}
