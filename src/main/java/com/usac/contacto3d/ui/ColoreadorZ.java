package com.usac.contacto3d.ui;

import com.usac.contacto3d.parser.ZetarianoLexer;

import org.antlr.v4.runtime.Lexer;
import org.fife.ui.rsyntaxtextarea.Token;

public class ColoreadorZ extends ColoreadorBase {

    @Override
    protected Lexer crearLexer(String texto) {
        return construir(texto, ZetarianoLexer::new);
    }

    @Override
    protected int colorDe(int tipo) {
        return switch (tipo) {
            case ZetarianoLexer.INT, ZetarianoLexer.DOUBLE, ZetarianoLexer.CHAR, ZetarianoLexer.BOOLEAN,
                 ZetarianoLexer.STRING, ZetarianoLexer.VOID -> Token.DATA_TYPE;
            case ZetarianoLexer.PUBLIC, ZetarianoLexer.PRIVATE, ZetarianoLexer.CLASS -> Token.PREPROCESSOR;
            case ZetarianoLexer.NEW, ZetarianoLexer.THIS, ZetarianoLexer.IF, ZetarianoLexer.ELSE,
                 ZetarianoLexer.SWITCH, ZetarianoLexer.CASE, ZetarianoLexer.DEFAULT, ZetarianoLexer.BREAK,
                 ZetarianoLexer.CONTINUE, ZetarianoLexer.RETURN, ZetarianoLexer.FOR, ZetarianoLexer.WHILE,
                 ZetarianoLexer.DO -> Token.RESERVED_WORD;
            case ZetarianoLexer.PRINTLN, ZetarianoLexer.PRINT, ZetarianoLexer.READLN -> Token.FUNCTION;
            case ZetarianoLexer.TRUE, ZetarianoLexer.FALSE, ZetarianoLexer.NULL -> Token.LITERAL_BOOLEAN;
            case ZetarianoLexer.LIT_ENTERO -> Token.LITERAL_NUMBER_DECIMAL_INT;
            case ZetarianoLexer.LIT_DECIMAL -> Token.LITERAL_NUMBER_FLOAT;
            case ZetarianoLexer.LIT_CADENA -> Token.LITERAL_STRING_DOUBLE_QUOTE;
            case ZetarianoLexer.LIT_CARACTER -> Token.LITERAL_CHAR;
            case ZetarianoLexer.ID -> Token.IDENTIFIER;
            case ZetarianoLexer.COMENTARIO_LINEA -> Token.COMMENT_EOL;
            case ZetarianoLexer.COMENTARIO_BLOQUE -> Token.COMMENT_MULTILINE;
            case ZetarianoLexer.WS -> Token.WHITESPACE;
            case ZetarianoLexer.PAR_A, ZetarianoLexer.PAR_C, ZetarianoLexer.CORCH_A, ZetarianoLexer.CORCH_C,
                 ZetarianoLexer.LLAVE_A, ZetarianoLexer.LLAVE_C, ZetarianoLexer.COMA, ZetarianoLexer.PUNTO,
                 ZetarianoLexer.DOSP, ZetarianoLexer.PUNTOYCOMA -> Token.SEPARATOR;
            default -> Token.OPERATOR;
        };
    }

    @Override protected String getAperturaBloque() { return "/*"; }
    @Override protected String getCierreBloque()   { return "*/"; }
}
