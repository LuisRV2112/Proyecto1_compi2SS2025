/*
 * Gramatica de PigLatin (archivos .pig), el punto de entrada del programa.
 *
 * Parte de la gramatica de la Practica 1 (CodexLatinus) con los cambios del
 * Proyecto 1:
 *  - importa archivos .z y .y al inicio
 *  - ya no define estructuras: vienen de los .y
 *  - los literales de estructura son posicionales: {"Calle Real", 42}
 *  - objetos: novus Clase(args) y llamadas a metodos encadenadas
 *  - arreglos de varias dimensiones
 */
grammar PigLatin;

@parser::header {
import com.usac.contacto3d.errores.ErrorConMensaje;
}

/* ============================== PARSER ============================== */

// Las secciones van en linea y no como reglas propias a proposito: si un
// ciclo como declaracion* estuviera en su propia regla, ante un token
// inesperado ANTLR saldria de ella sin reportar y el error caeria aqui, donde
// la unica recuperacion posible es saltar hasta el EOF (se perderian todos los
// errores siguientes). En la misma regla ve la siguiente seccion y resincroniza.
programa
    : importacion*
      (SEC_VARIABILES declaracion*)?
      (SEC_MUNERA funcion*)?
      SEC_MAIOR instruccion* FINIS_MAIOR PUNTOYCOMA
      EOF
    ;

// import carpeta.Objeto1.z  — la ruta y la extension las valida el semantico
importacion
    : IMPORT ID (PUNTO ID)+ PUNTOYCOMA?
    ;

/* ---------- declaraciones ---------- */

declaracion
    : declaracionVariable                   # declVariable
    | declaracionArreglo                    # declArreglo
    // Valida en la Practica 1, ya no en el proyecto. Reconocerla da un mensaje
    // claro en vez de una cascada de errores sobre llaves y 'finis'.
    | STRUCTURA ID LLAVE_A declaracion* LLAVE_C FINIS PUNTOYCOMA
      { notifyErrorListeners($STRUCTURA, "PigLatin ya no define estructuras. Se esperaba importarla de un archivo .y", new ErrorConMensaje(this)); }
                                            # declStructuraObsoleta
    ;

// Las que terminan en '}' no necesitan ';' (confirmado por el auxiliar en la
// Practica 1); se acepta igual por tolerancia.
declaracionVariable
    : ESTO ID DOSP tipoPrimitivo expr? PUNTOYCOMA           # varPrimitiva
    // Forma rapida de la Practica 1: el tipo bool se deduce del literal
    | ESTO ID DOSP (VERUM | FALSUS) PUNTOYCOMA              # varBoolSinTipo
    | ESTO ID DOSP ID listaValores PUNTOYCOMA?              # varEstructura
    // Estructura u objeto sin inicializar
    | ESTO ID DOSP ID PUNTOYCOMA                            # varSinValor
    | ESTO ID DOSP nuevoObjeto PUNTOYCOMA                   # varObjeto
    ;

declaracionArreglo
    : SERIES ID dimension+ DOSP tipo (listaValores PUNTOYCOMA? | PUNTOYCOMA)
    ;

dimension
    : CORCH_A expr CORCH_C
    ;

// Inicializa arreglos y estructuras. Es posicional y se anida:
// {"Valeria", 25, {"Avenida Central", 500}}  o  { {3, 2, 1}, {4, 5, 6} }
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

/* ---------- funciones ---------- */

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

// VARIABILES[ ... ]: declaraciones locales al inicio de la funcion
bloqueVariables
    : VAR_BLOQUE declaracion* CORCH_C
    ;

/* ---------- instrucciones ---------- */

instruccion
    : declaracionVariable                   # instDeclVariable
    | declaracionArreglo                    # instDeclArreglo
    | asignacion                            # instAsignacion
    | incremento PUNTOYCOMA                 # instIncremento
    // La gramatica acepta cualquier acceso; que termine en una llamada
    // lo revisa el semantico
    | acceso PUNTOYCOMA                     # instLlamada
    | sentenciaSi                           # instSi
    | sentenciaDum                          # instDum
    | sentenciaFacere                       # instFacere
    | sentenciaPer                          # instPer
    | PERGE PUNTOYCOMA                      # instPerge
    | INTERRUMPE PUNTOYCOMA                 # instInterrumpe
    | REDDERE expr? PUNTOYCOMA              # instReddere
    | IMPRIMIR expr (IMPRIMIR expr)* PUNTOYCOMA # instImprimir
    // El enunciado escribe la lectura sin ';' ("comandante <<")
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

// El enunciado escribe el per sin "finis;"; la Practica 1 lo aceptaba con el
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

/* ---------- expresiones ---------- */

// La precedencia sale del orden: la primera alternativa binaria es la mas alta
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

// Una cadena de accesos: miObjeto.apellidos[0].getNombre(), calcularPoder(x)
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

/* ============================== LEXER =============================== */

// Marcadores de seccion
SEC_VARIABILES : 'VARIABILES>' ;
SEC_MUNERA     : 'MUNERA>' ;
SEC_MAIOR      : 'MAIOR>' ;
VAR_BLOQUE     : 'VARIABILES[' ;
FINIS_MAIOR    : 'FINIS' ;

// Tipos
NUMERUS   : 'numerus' ;
DECIMALIS : 'decimalis' ;
TEXTUM    : 'textum' ;
LITTERA   : 'littera' ;
BOOL      : 'bool' ;

// Palabras reservadas
IMPORT     : 'import' ;
ESTO       : 'esto' ;
STRUCTURA  : 'structura' ;   // solo para reportar que ya no se permite
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

// Operadores (los de dos caracteres ganan por coincidencia mas larga)
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

// Agrupacion y puntuacion
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

// Literales
LIT_DECIMAL  : [0-9]+ '.' [0-9]+ ;
LIT_ENTERO   : [0-9]+ ;
LIT_CADENA   : '"' (~["\\\r\n] | '\\' .)* '"' ;
LIT_CARACTER : '\'' (~['\\\r\n] | '\\' .) '\'' ;

ID : [a-zA-Z_] [a-zA-Z0-9_]* ;

// Al canal oculto y no a skip: el coloreado necesita verlos
COMENTARIO_BLOQUE : '##' .*? '##' -> channel(HIDDEN) ;
COMENTARIO_LINEA  : '//' ~[\r\n]* -> channel(HIDDEN) ;
WS                : [ \t\r\n\f]+ -> channel(HIDDEN) ;
