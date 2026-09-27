grammar PigLatin;

@parser::header {
import com.usac.contacto3d.errores.ErrorConMensaje;
}

programa
    : importacion*
      (SEC_VARIABILES declaracion*)?
      (SEC_MUNERA funcion*)?
      SEC_MAIOR instruccion* FINIS_MAIOR PUNTOYCOMA
      EOF
    ;

importacion
    : IMPORT ID (PUNTO ID)+ PUNTOYCOMA?
    ;

declaracion
    : declaracionVariable                   # declVariable
    | declaracionArreglo                    # declArreglo
    | STRUCTURA ID LLAVE_A declaracion* LLAVE_C FINIS PUNTOYCOMA
      { notifyErrorListeners($STRUCTURA, "PigLatin ya no define estructuras. Se esperaba importarla de un archivo .y", new ErrorConMensaje(this)); }
                                            # declStructuraObsoleta
    ;

declaracionVariable
    : ESTO ID DOSP tipoPrimitivo expr? PUNTOYCOMA           # varPrimitiva
    | ESTO ID DOSP (VERUM | FALSUS) PUNTOYCOMA              # varBoolSinTipo
    | ESTO ID DOSP ID listaValores PUNTOYCOMA?              # varEstructura
    | ESTO ID DOSP ID PUNTOYCOMA                            # varSinValor
    | ESTO ID DOSP nuevoObjeto PUNTOYCOMA                   # varObjeto
    ;

declaracionArreglo
    : SERIES ID dimension+ DOSP tipo (listaValores PUNTOYCOMA? | PUNTOYCOMA)
    ;

dimension
    : CORCH_A expr CORCH_C
    ;

listaValores
    : LLAVE_A (valor (COMA valor)*)? LLAVE_C
    ;

valor
    : expr                                  # valorExpr
    | listaValores                          # valorLista
    ;

tipo
    : tipoPrimitivo                         # tipoBase
    | ID                                    # tipoUsuario
    ;

tipoPrimitivo
    : NUMERUS                               # tipoNumerus
    | DECIMALIS                             # tipoDecimalis
    | TEXTUM                                # tipoTextum
    | LITTERA                               # tipoLittera
    | BOOL                                  # tipoBool
    ;

funcion
    : ACTIO ID PAR_A listaParametros? PAR_C cuerpoFuncion        # funcionActio
    | RATIO tipo ID PAR_A listaParametros? PAR_C cuerpoFuncion   # funcionRatio
    ;

listaParametros
    : parametro (COMA parametro)*
    ;

parametro
    : ESTO ID DOSP tipo
    ;

cuerpoFuncion
    : LLAVE_A bloqueVariables? instruccion* LLAVE_C FINIS PUNTOYCOMA
    ;

bloqueVariables
    : VAR_BLOQUE declaracion* CORCH_C
    ;

instruccion
    : declaracionVariable                   # instDeclVariable
    | declaracionArreglo                    # instDeclArreglo
    | asignacion                            # instAsignacion
    | incremento PUNTOYCOMA                 # instIncremento
    | acceso PUNTOYCOMA                     # instLlamada
    | sentenciaSi                           # instSi
    | sentenciaDum                          # instDum
    | sentenciaFacere                       # instFacere
    | sentenciaPer                          # instPer
    | PERGE PUNTOYCOMA                      # instPerge
    | INTERRUMPE PUNTOYCOMA                 # instInterrumpe
    | REDDERE expr? PUNTOYCOMA              # instReddere
    | IMPRIMIR expr (IMPRIMIR expr)* PUNTOYCOMA # instImprimir
    | acceso LEER PUNTOYCOMA?               # instLeerVariable
    | LEER PUNTOYCOMA?                      # instLeer
    ;

asignacion
    : acceso ASIGNACION expr PUNTOYCOMA                     # asignacionExpr
    | acceso ASIGNACION listaValores PUNTOYCOMA?            # asignacionLista
    ;

incremento
    : acceso (INCREMENTO | DECREMENTO)
    ;

sentenciaSi
    : SI PAR_A expr PAR_C bloque ramaAliterSi* ramaAliter? FINIS PUNTOYCOMA
    ;

ramaAliterSi
    : ALITER PAR_A expr PAR_C bloque
    ;

ramaAliter
    : ALITER bloque
    ;

sentenciaDum
    : DUM PAR_A expr PAR_C bloque FINIS PUNTOYCOMA
    ;

sentenciaFacere
    : FACERE bloque DUM PAR_A expr PAR_C PUNTOYCOMA
    ;

sentenciaPer
    : PER PAR_A inicioPer PUNTOYCOMA expr PUNTOYCOMA actualizacionPer PAR_C bloque
      (FINIS PUNTOYCOMA?)?
    ;

inicioPer
    : ESTO ID DOSP tipo expr                # inicioDeclaracion
    | acceso ASIGNACION expr                # inicioAsignacion
    ;

actualizacionPer
    : incremento                            # actualizacionIncremento
    | acceso ASIGNACION expr                # actualizacionAsignacion
    ;

bloque
    : LLAVE_A instruccion* LLAVE_C
    ;

expr
    : PAR_A expr PAR_C                                      # exprAgrupacion
    | incremento                                            # exprIncremento
    | (MENOS | NON) expr                                    # exprUnaria
    | expr (POR | DIV) expr                                 # exprMulDiv
    | expr (MAS | MENOS) expr                               # exprSumRes
    | expr (MENOR | MAYOR | MENOR_IGUAL | MAYOR_IGUAL) expr # exprRelacional
    | expr (IGUALDAD | DIFERENTE) expr                      # exprIgualdad
    | expr AND expr                                         # exprAnd
    | expr OR expr                                          # exprOr
    | literal                                               # exprLiteral
    | acceso                                                # exprAcceso
    | nuevoObjeto                                           # exprNuevoObjeto
    ;

nuevoObjeto
    : NOVUS ID PAR_A listaExpresiones? PAR_C
    ;

listaExpresiones
    : expr (COMA expr)*
    ;

acceso
    : raizAcceso sufijo*
    ;

raizAcceso
    : ID PAR_A listaExpresiones? PAR_C      # raizLlamada
    | ID                                    # raizId
    ;

sufijo
    : PUNTO ID PAR_A listaExpresiones? PAR_C # sufijoMetodo
    | PUNTO ID                              # sufijoAtributo
    | CORCH_A expr CORCH_C                  # sufijoIndice
    ;

literal
    : LIT_ENTERO                            # litEntero
    | LIT_DECIMAL                           # litDecimal
    | LIT_CADENA                            # litCadena
    | LIT_CARACTER                          # litCaracter
    | (VERUM | FALSUS)                      # litBooleano
    ;

SEC_VARIABILES : 'VARIABILES>' ;
SEC_MUNERA     : 'MUNERA>' ;
SEC_MAIOR      : 'MAIOR>' ;
VAR_BLOQUE     : 'VARIABILES[' ;
FINIS_MAIOR    : 'FINIS' ;

NUMERUS   : 'numerus' ;
DECIMALIS : 'decimalis' ;
TEXTUM    : 'textum' ;
LITTERA   : 'littera' ;
BOOL      : 'bool' ;

IMPORT     : 'import' ;
ESTO       : 'esto' ;
STRUCTURA  : 'structura' ;
SERIES     : 'series' ;
NOVUS      : 'novus' ;
FINIS      : 'finis' ;
SI         : 'si' ;
ALITER     : 'aliter' ;
DUM        : 'dum' ;
FACERE     : 'facere' ;
PER        : 'per' ;
PERGE      : 'perge' ;
INTERRUMPE : 'interrumpe' ;
ACTIO      : 'actio' ;
RATIO      : 'ratio' ;
REDDERE    : 'reddere' ;
VERUM      : 'verum' ;
FALSUS     : 'falsus' ;
NON        : 'non' ;

IMPRIMIR    : '>>' ;
LEER        : '<<' ;
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

LIT_DECIMAL  : [0-9]+ '.' [0-9]+ ;
LIT_ENTERO   : [0-9]+ ;
LIT_CADENA   : '"' (~["\\\r\n] | '\\' .)* '"' ;
LIT_CARACTER : '\'' (~['\\\r\n] | '\\' .) '\'' ;

ID : [a-zA-Z_] [a-zA-Z0-9_]* ;

COMENTARIO_BLOQUE : '##' .*? '##' -> channel(HIDDEN) ;
COMENTARIO_LINEA  : '//' ~[\r\n]* -> channel(HIDDEN) ;
WS                : [ \t\r\n\f]+ -> channel(HIDDEN) ;
