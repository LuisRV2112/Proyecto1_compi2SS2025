/*
 * Gramatica de Zetariano (archivos .z)
 *
 * Orientado a objetos, basado en Java. Un archivo contiene una sola clase;
 * que se llame igual que el archivo lo verifica el semantico, no la gramatica.
 *
 * Encapsulamiento, herencia y polimorfismo no se contemplan en este proyecto:
 * public/private se aceptan para no romper codigo estilo Java, pero no
 * significan nada.
 */
grammar Zetariano;

/* ============================== PARSER ============================== */

programa
    : clase EOF
    ;

clase
    : modificador? CLASS ID LLAVE_A miembro* LLAVE_C
    ;

modificador
    : PUBLIC                                # modPublico
    | PRIVATE                               # modPrivado
    ;

// Atributo y metodo con tipo comparten "tipo ID" y se separan despues, en
// restoMiembro. Si fueran alternativas completas, un "int edad" sin ';'
// no encajaria en ninguna y el error listaria todo lo que puede iniciar un
// miembro en vez de decir que faltaba '(', '=', ',' o ';'.
miembro
    : modificador? ID PAR_A listaParametros? PAR_C bloque                  # miembroConstructor
    | modificador? VOID ID PAR_A listaParametros? PAR_C bloque             # miembroMetodoVoid
    | modificador? tipo ID restoMiembro                                    # miembroConTipo
    ;

restoMiembro
    : PAR_A listaParametros? PAR_C bloque                                  # restoMetodo
    | (ASIGNACION expr)? (COMA declarador)* PUNTOYCOMA                     # restoAtributo
    ;

listaParametros
    : parametro (COMA parametro)*
    ;

// Arreglos y objetos pasan por referencia sin marca especial, como en Java
parametro
    : tipo ID
    ;

tipo
    : tipoBase (CORCH_A CORCH_C)*
    ;

tipoBase
    : INT                                   # tipoInt
    | DOUBLE                                # tipoDouble
    | CHAR                                  # tipoChar
    | BOOLEAN                               # tipoBoolean
    | STRING                                # tipoString
    | ID                                    # tipoClase
    ;

bloque
    : LLAVE_A instruccion* LLAVE_C
    ;

/* ---------- instrucciones ---------- */

instruccion
    : bloque                                # instBloque
    | declaracionVariable PUNTOYCOMA        # instDeclaracion
    | asignacion PUNTOYCOMA                 # instAsignacion
    | incremento PUNTOYCOMA                 # instIncremento
    // La gramatica acepta cualquier acceso; que termine en una llamada
    // ("x;" no es instruccion) lo revisa el semantico
    | acceso PUNTOYCOMA                     # instLlamada
    | sentenciaIf                           # instIf
    | sentenciaSwitch                       # instSwitch
    | sentenciaFor                          # instFor
    | sentenciaWhile                        # instWhile
    | sentenciaDoWhile                      # instDoWhile
    | PRINTLN PAR_A expr? PAR_C PUNTOYCOMA  # instPrintln
    | PRINT PAR_A expr PAR_C PUNTOYCOMA     # instPrint
    | READLN PAR_A PAR_C PUNTOYCOMA         # instReadln
    | BREAK PUNTOYCOMA                      # instBreak
    | CONTINUE PUNTOYCOMA                   # instContinue
    | RETURN expr? PUNTOYCOMA               # instReturn
    ;

declaracionVariable
    : tipo declarador (COMA declarador)*
    ;

declarador
    : ID (ASIGNACION expr)?
    ;

asignacion
    : acceso (ASIGNACION | MAS_IGUAL | MENOS_IGUAL | POR_IGUAL) expr
    ;

incremento
    : acceso (INCREMENTO | DECREMENTO)      # incrementoPostfijo
    | (INCREMENTO | DECREMENTO) acceso      # incrementoPrefijo
    ;

// Cuerpo sin llaves = una sola instruccion. El else ambiguo se asocia al
// if mas cercano: ANTLR toma la alternativa codiciosa, igual que Java.
sentenciaIf
    : IF PAR_A expr PAR_C instruccion (ELSE instruccion)?
    ;

// Hay fall-through: las instrucciones de una seccion siguen de largo a la
// siguiente si no hay break, asi que cada seccion es solo una etiqueta con
// lo que venga despues. Que haya un solo default lo revisa el semantico.
sentenciaSwitch
    : SWITCH PAR_A expr PAR_C LLAVE_A seccionSwitch* LLAVE_C
    ;

seccionSwitch
    : CASE expr DOSP instruccion*           # seccionCase
    | DEFAULT DOSP instruccion*             # seccionDefault
    ;

// Las tres partes son opcionales: for (;;) es un ciclo infinito
sentenciaFor
    : FOR PAR_A inicioFor? PUNTOYCOMA expr? PUNTOYCOMA listaActualizacion? PAR_C instruccion
    ;

inicioFor
    : declaracionVariable                   # inicioDeclaracion
    | listaActualizacion                    # inicioAsignaciones
    ;

listaActualizacion
    : actualizacion (COMA actualizacion)*
    ;

actualizacion
    : asignacion                            # actualizacionAsignacion
    | incremento                            # actualizacionIncremento
    ;

sentenciaWhile
    : WHILE PAR_A expr PAR_C instruccion
    ;

sentenciaDoWhile
    : DO instruccion WHILE PAR_A expr PAR_C PUNTOYCOMA
    ;

/* ---------- expresiones ---------- */

// La precedencia sale del orden: la primera alternativa binaria es la mas alta
expr
    : PAR_A expr PAR_C                                      # exprAgrupacion
    | incremento                                            # exprIncremento
    | (MENOS | NOT) expr                                    # exprUnaria
    | expr (POR | DIV | MOD) expr                           # exprMulDivMod
    | expr (MAS | MENOS) expr                               # exprSumRes
    | expr (MENOR | MAYOR | MENOR_IGUAL | MAYOR_IGUAL) expr # exprRelacional
    | expr (IGUALDAD | DIFERENTE) expr                      # exprIgualdad
    | expr AND expr                                         # exprAnd
    | expr OR expr                                          # exprOr
    | <assoc=right> expr INTERROGACION expr DOSP expr       # exprTernario
    | literal                                               # exprLiteral
    | acceso                                                # exprAcceso
    | NEW ID PAR_A listaExpresiones? PAR_C                  # exprNuevoObjeto
    | NEW tipoBase (CORCH_A expr CORCH_C)+                  # exprNuevoArreglo
    | LLAVE_A listaExpresiones? LLAVE_C                     # exprArregloLiteral
    | READLN PAR_A PAR_C                                    # exprReadln
    ;

listaExpresiones
    : expr (COMA expr)*
    ;

// Una cadena de accesos: persona.direccion.getCalle(), matriz[i][j], this.nombre
acceso
    : raizAcceso sufijo*
    ;

raizAcceso
    : ID PAR_A listaExpresiones? PAR_C      # raizLlamada
    | ID                                    # raizId
    | THIS                                  # raizThis
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
    | (TRUE | FALSE)                        # litBooleano
    | NULL                                  # litNulo
    ;

/* ============================== LEXER =============================== */

// Tipos
INT      : 'int' ;
DOUBLE   : 'double' ;
CHAR     : 'char' ;
BOOLEAN  : 'boolean' ;
STRING   : 'String' ;
VOID     : 'void' ;

// Palabras reservadas
PUBLIC   : 'public' ;
PRIVATE  : 'private' ;
CLASS    : 'class' ;
NEW      : 'new' ;
THIS     : 'this' ;
NULL     : 'null' ;
TRUE     : 'true' ;
FALSE    : 'false' ;
IF       : 'if' ;
ELSE     : 'else' ;
SWITCH   : 'switch' ;
CASE     : 'case' ;
DEFAULT  : 'default' ;
BREAK    : 'break' ;
CONTINUE : 'continue' ;
RETURN   : 'return' ;
FOR      : 'for' ;
WHILE    : 'while' ;
DO       : 'do' ;
PRINTLN  : 'println' ;
PRINT    : 'print' ;
READLN   : 'readln' ;

// Operadores (los de dos caracteres ganan por coincidencia mas larga)
INCREMENTO    : '++' ;
DECREMENTO    : '--' ;
MAS_IGUAL     : '+=' ;
MENOS_IGUAL   : '-=' ;
POR_IGUAL     : '*=' ;
IGUALDAD      : '==' ;
DIFERENTE     : '!=' ;
MENOR_IGUAL   : '<=' ;
MAYOR_IGUAL   : '>=' ;
AND           : '&&' ;
OR            : '||' ;
MAS           : '+' ;
MENOS         : '-' ;
POR           : '*' ;
DIV           : '/' ;
MOD           : '%' ;
MENOR         : '<' ;
MAYOR         : '>' ;
NOT           : '!' ;
ASIGNACION    : '=' ;
INTERROGACION : '?' ;

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
COMENTARIO_LINEA  : '//' ~[\r\n]* -> channel(HIDDEN) ;
COMENTARIO_BLOQUE : '/*' .*? '*/' -> channel(HIDDEN) ;
WS                : [ \t\r\n\f]+ -> channel(HIDDEN) ;
