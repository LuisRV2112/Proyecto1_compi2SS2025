package com.usac.contacto3d.ui;

import com.usac.contacto3d.parser.LenguajeYLexer;

import org.antlr.v4.runtime.Lexer;
import org.fife.ui.rsyntaxtextarea.Token;

/**
 * Coloreado de Y?: el tipo de cada token lo decide LenguajeYLexer (el mismo
 * lexer del compilador), no RSyntaxTextArea.
 *
 * Los INDENT/DEDENT que agrega el lexer miden cero caracteres, asi que
 * ColoreadorBase no les asigna texto.
 */
public class ColoreadorY extends ColoreadorBase {

    @Override
    protected Lexer crearLexer(String texto) {
        return construir(texto, LenguajeYLexer::new);
    }

    @Override
    protected int colorDe(int tipo) {
        return switch (tipo) {
            case LenguajeYLexer.SEC_ESTRUCTURAS, LenguajeYLexer.SEC_FUNCIONES -> Token.PREPROCESSOR;
            case LenguajeYLexer.ENTERO, LenguajeYLexer.FLOTANTE, LenguajeYLexer.CADENA,
                 LenguajeYLexer.CARACTER, LenguajeYLexer.BOOL -> Token.DATA_TYPE;
            case LenguajeYLexer.ESTRUCTURA, LenguajeYLexer.DEFINIR, LenguajeYLexer.RETORNAR,
                 LenguajeYLexer.SI, LenguajeYLexer.SINO, LenguajeYLexer.CONTRARIO, LenguajeYLexer.ENTONCES,
                 LenguajeYLexer.ELEGIR, LenguajeYLexer.CASO, LenguajeYLexer.SIEMPRE, LenguajeYLexer.ROMPER,
                 LenguajeYLexer.CONTINUAR, LenguajeYLexer.PARA, LenguajeYLexer.MIENTRAS,
                 LenguajeYLexer.HACER -> Token.RESERVED_WORD;
            case LenguajeYLexer.IMPRIMIR, LenguajeYLexer.LEER -> Token.FUNCTION;
            case LenguajeYLexer.VERDADERO, LenguajeYLexer.FALSO -> Token.LITERAL_BOOLEAN;
            case LenguajeYLexer.LIT_ENTERO -> Token.LITERAL_NUMBER_DECIMAL_INT;
            case LenguajeYLexer.LIT_FLOTANTE -> Token.LITERAL_NUMBER_FLOAT;
            case LenguajeYLexer.LIT_CADENA -> Token.LITERAL_STRING_DOUBLE_QUOTE;
            case LenguajeYLexer.LIT_CARACTER -> Token.LITERAL_CHAR;
            case LenguajeYLexer.ID -> Token.IDENTIFIER;
            case LenguajeYLexer.COMENTARIO_LINEA -> Token.COMMENT_EOL;
            case LenguajeYLexer.COMENTARIO_BLOQUE -> Token.COMMENT_MULTILINE;
            case LenguajeYLexer.WS -> Token.WHITESPACE;
            case LenguajeYLexer.PAR_A, LenguajeYLexer.PAR_C, LenguajeYLexer.CORCH_A, LenguajeYLexer.CORCH_C,
                 LenguajeYLexer.LLAVE_A, LenguajeYLexer.LLAVE_C, LenguajeYLexer.COMA, LenguajeYLexer.PUNTO,
                 LenguajeYLexer.DOSP, LenguajeYLexer.PUNTOYCOMA -> Token.SEPARATOR;
            default -> Token.OPERATOR;
        };
    }

    @Override protected String getAperturaBloque() { return "/*"; }
    @Override protected String getCierreBloque()   { return "*/"; }
}
