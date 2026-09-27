grammar LenguajeY;

tokens { INDENT, DEDENT }

@parser::header {
import com.usac.contacto3d.errores.ErrorConMensaje;
}

@lexer::members {
    private IndentacionY indentacion = new IndentacionY(this);

    @Override
    public Token nextToken() {
        return indentacion.siguiente(super::nextToken);
    }

    @Override
    public void reset() {
        super.reset();
        if (indentacion != null) {
            indentacion.reiniciar();
        }
    }
}

programa
    : (SEC_ESTRUCTURAS NUEVA_LINEA estructura*)?
      SEC_FUNCIONES NUEVA_LINEA funcion*
      EOF
    ;

estructura
    : ESTRUCTURA ID DOSP NUEVA_LINEA INDENT campo+ DEDENT
    ;

campo
    : tipo ID dimensionConstante* NUEVA_LINEA
    ;

dimensionConstante
    : CORCH_A LIT_ENTERO CORCH_C
    ;

funcion
    : DEFINIR ID PAR_A listaParametros? PAR_C (FLECHA tipo)? DOSP bloque
    ;

listaParametros
    : parametro (COMA parametro)*
    ;

parametro
    : tipo ID                               # paramValor
    | (CORCH_A CORCH_C)+ tipo ID            # paramArreglo
    | LLAVE_A LLAVE_C ID ID                 # paramEstructura
    ;

tipo
    : ENTERO                                # tipoEntero
    | FLOTANTE                              # tipoFlotante
    | CADENA                                # tipoCadena
    | CARACTER                              # tipoCaracter
    | BOOL                                  # tipoBool
    | ID                                    # tipoEstructura
    ;

bloque
    : NUEVA_LINEA INDENT instruccion+ DEDENT
    ;

instruccion
    : instruccionSimple PUNTOYCOMA? NUEVA_LINEA # instSimple
    | estructura                            # instEstructura
    | sentenciaSi                           # instSi
    | sentenciaElegir                       # instElegir
    | sentenciaPara                         # instPara
    | sentenciaMientras                     # instMientras
    | sentenciaHacer                        # instHacer
    | INDENT instruccion+ DEDENT
      { notifyErrorListeners($INDENT, "Sangria inesperada: la instruccion anterior no abre un bloque. Se esperaba la misma sangria que la linea anterior", new ErrorConMensaje(this)); }
                                            # instSangriaInesperada
    ;

instruccionSimple
    : declaracion                           # simpleDeclaracion
    | asignacion                            # simpleAsignacion
    | incremento                            # simpleIncremento
    | llamada                               # simpleLlamada
    | IMPRIMIR PAR_A listaExpresiones? PAR_C # simpleImprimir
    | LEER PAR_A PAR_C                      # simpleLeer
    | RETORNAR expr?                        # simpleRetornar
    | ROMPER                                # simpleRomper
    | CONTINUAR                             # simpleContinuar
    ;

declaracion
    : tipo ID dimension* (ASIGNACION expr)?
    ;

dimension
    : CORCH_A expr CORCH_C
    ;

asignacion
    : acceso ASIGNACION expr
    ;

incremento
    : acceso (INCREMENTO | DECREMENTO)
    ;

sentenciaSi
    : SI PAR_A expr PAR_C ENTONCES DOSP? bloque
      ramaSino*
      ramaContrario?
    ;

ramaSino
    : SINO PAR_A expr PAR_C ENTONCES DOSP? bloque
    ;

ramaContrario
    : CONTRARIO DOSP? bloque
    ;

sentenciaElegir
    : ELEGIR PAR_A expr PAR_C LLAVE_A NUEVA_LINEA
      (INDENT caso* casoSiempre? DEDENT)?
      LLAVE_C NUEVA_LINEA
    ;

caso
    : CASO expr DOSP cuerpoCaso
    ;

casoSiempre
    : SIEMPRE DOSP cuerpoCaso
    ;

cuerpoCaso
    : bloque                                # cuerpoBloque
    | instruccionSimple PUNTOYCOMA? NUEVA_LINEA # cuerpoEnLinea
    | NUEVA_LINEA                           # cuerpoVacio
    ;

sentenciaPara
    : PARA PAR_A inicioPara PUNTOYCOMA expr PUNTOYCOMA actualizacionPara PAR_C DOSP bloque
    ;

inicioPara
    : declaracion                           # inicioDeclaracion
    | asignacion                            # inicioAsignacion
    ;

actualizacionPara
    : incremento                            # actualizacionIncremento
    | asignacion                            # actualizacionAsignacion
    ;

sentenciaMientras
    : MIENTRAS PAR_A expr PAR_C HACER DOSP? bloque
    ;

sentenciaHacer
    : HACER DOSP bloque MIENTRAS PAR_A expr PAR_C PUNTOYCOMA? NUEVA_LINEA
    ;

expr
    : PAR_A expr PAR_C                                  # exprAgrupacion
    | (MENOS | NOT) expr                                # exprUnaria
    | expr (POR | DIV) expr                             # exprMulDiv
    | expr (MAS | MENOS) expr                           # exprSumRes
    | expr (MENOR | MAYOR | MENOR_IGUAL | MAYOR_IGUAL) expr # exprRelacional
    | expr (IGUALDAD | DIFERENTE) expr                  # exprIgualdad
    | expr AND expr                                     # exprAnd
    | expr OR expr                                      # exprOr
    | literal                                           # exprLiteral
    | llamada                                           # exprLlamada
    | acceso                                            # exprAcceso
    | LEER PAR_A PAR_C                                  # exprLeer
    | LLAVE_A listaExpresiones? LLAVE_C                 # exprLista
    ;

listaExpresiones
    : expr (COMA expr)*
    ;

llamada
    : ID PAR_A listaExpresiones? PAR_C
    ;

acceso
    : ID sufijo*
    ;

sufijo
    : PUNTO ID                              # sufijoCampo
    | CORCH_A expr CORCH_C                  # sufijoIndice
    ;

literal
    : LIT_ENTERO                            # litEntero
    | LIT_FLOTANTE                          # litFlotante
    | LIT_CADENA                            # litCadena
    | LIT_CARACTER                          # litCaracter
    | (VERDADERO | FALSO)                   # litBooleano
    ;

SEC_ESTRUCTURAS : '%estructuras' ;
SEC_FUNCIONES   : '%funciones' ;

ENTERO    : 'entero' ;
FLOTANTE  : 'flotante' ;
CADENA    : 'cadena' ;
CARACTER  : 'caracter' ;
BOOL      : 'bool' ;

ESTRUCTURA : 'estructura' ;
DEFINIR    : 'definir' ;
RETORNAR   : 'retornar' ;
SI         : 'si' ;
SINO       : 'sino' ;
CONTRARIO  : 'contrario' ;
ENTONCES   : 'entonces' ;
ELEGIR     : 'elegir' ;
CASO       : 'caso' ;
SIEMPRE    : 'siempre' ;
ROMPER     : 'romper' ;
CONTINUAR  : 'continuar' ;
PARA       : 'para' ;
MIENTRAS   : 'mientras' ;
HACER      : 'hacer' ;
IMPRIMIR   : 'imprimir' ;
LEER       : 'leer' ;
VERDADERO  : 'verdadero' ;
FALSO      : 'falso' ;

FLECHA      : '->' ;
INCREMENTO  : '++' ;
DECREMENTO  : '--' ;
IGUALDAD    : '==' ;
DIFERENTE   : '!=' ;
MENOR_IGUAL : '<=' ;
MAYOR_IGUAL : '>=' ;
AND         : '&&' ;
OR          : '||' ;
MAS         : '+' ;
MENOS       : '-' ;
POR         : '*' ;
DIV         : '/' ;
MENOR       : '<' ;
MAYOR       : '>' ;
NOT         : '!' ;
ASIGNACION  : '=' ;

PAR_A      : '(' ;
PAR_C      : ')' ;
CORCH_A    : '[' ;
CORCH_C    : ']' ;
LLAVE_A    : '{' ;
LLAVE_C    : '}' ;
PUNTO      : '.' ;
COMA       : ',' ;
DOSP       : ':' ;
PUNTOYCOMA : ';' ;

LIT_FLOTANTE : [0-9]+ '.' [0-9]+ ;
LIT_ENTERO   : [0-9]+ ;
LIT_CADENA   : '"' (~["\\\r\n] | '\\' .)* '"' ;
LIT_CARACTER : '\'' (~['\\\r\n] | '\\' .) '\'' ;

ID : [a-zA-Z_] [a-zA-Z0-9_]* ;

NUEVA_LINEA : '\r'? '\n' | '\r' ;

COMENTARIO_LINEA  : '//' ~[\r\n]* -> channel(HIDDEN) ;
COMENTARIO_BLOQUE : '/*' .*? '*/' -> channel(HIDDEN) ;
WS                : [ \t\f]+ -> channel(HIDDEN) ;
