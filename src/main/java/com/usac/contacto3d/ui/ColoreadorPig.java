package com.usac.contacto3d.ui;

import com.usac.contacto3d.parser.PigLatinLexer;

import org.antlr.v4.runtime.Lexer;
import org.fife.ui.rsyntaxtextarea.Token;

public class ColoreadorPig extends ColoreadorBase {

    @Override
    protected Lexer crearLexer(String texto) {
        return construir(texto, PigLatinLexer::new);
    }

    @Override
    protected int colorDe(int tipo) {
        return switch (tipo) {
            case PigLatinLexer.SEC_VARIABILES, PigLatinLexer.SEC_MUNERA, PigLatinLexer.SEC_MAIOR,
                 PigLatinLexer.VAR_BLOQUE, PigLatinLexer.FINIS_MAIOR, PigLatinLexer.IMPORT -> Token.PREPROCESSOR;
            case PigLatinLexer.NUMERUS, PigLatinLexer.DECIMALIS, PigLatinLexer.TEXTUM, PigLatinLexer.LITTERA,
                 PigLatinLexer.BOOL -> Token.DATA_TYPE;
            case PigLatinLexer.ESTO, PigLatinLexer.SERIES, PigLatinLexer.NOVUS, PigLatinLexer.FINIS,
                 PigLatinLexer.SI, PigLatinLexer.ALITER, PigLatinLexer.DUM, PigLatinLexer.FACERE,
                 PigLatinLexer.PER, PigLatinLexer.PERGE, PigLatinLexer.INTERRUMPE, PigLatinLexer.ACTIO,
                 PigLatinLexer.RATIO, PigLatinLexer.REDDERE, PigLatinLexer.NON,
                 PigLatinLexer.STRUCTURA -> Token.RESERVED_WORD;
            case PigLatinLexer.IMPRIMIR, PigLatinLexer.LEER -> Token.FUNCTION;
            case PigLatinLexer.VERUM, PigLatinLexer.FALSUS -> Token.LITERAL_BOOLEAN;
            case PigLatinLexer.LIT_ENTERO -> Token.LITERAL_NUMBER_DECIMAL_INT;
            case PigLatinLexer.LIT_DECIMAL -> Token.LITERAL_NUMBER_FLOAT;
            case PigLatinLexer.LIT_CADENA -> Token.LITERAL_STRING_DOUBLE_QUOTE;
            case PigLatinLexer.LIT_CARACTER -> Token.LITERAL_CHAR;
            case PigLatinLexer.ID -> Token.IDENTIFIER;
            case PigLatinLexer.COMENTARIO_LINEA -> Token.COMMENT_EOL;
            case PigLatinLexer.COMENTARIO_BLOQUE -> Token.COMMENT_MULTILINE;
            case PigLatinLexer.WS -> Token.WHITESPACE;
            case PigLatinLexer.PAR_A, PigLatinLexer.PAR_C, PigLatinLexer.CORCH_A, PigLatinLexer.CORCH_C,
                 PigLatinLexer.LLAVE_A, PigLatinLexer.LLAVE_C, PigLatinLexer.COMA, PigLatinLexer.PUNTO,
                 PigLatinLexer.DOSP, PigLatinLexer.PUNTOYCOMA -> Token.SEPARATOR;
            default -> Token.OPERATOR;
        };
    }

    @Override protected String getAperturaBloque() { return "##"; }
    @Override protected String getCierreBloque()   { return "##"; }
}
