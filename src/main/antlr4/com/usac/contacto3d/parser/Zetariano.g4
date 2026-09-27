grammar Zetariano;

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

instruccion
    : bloque                                # instBloque
    | declaracionVariable PUNTOYCOMA        # instDeclaracion
    | asignacion PUNTOYCOMA                 # instAsignacion
    | incremento PUNTOYCOMA                 # instIncremento
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

sentenciaIf
    : IF PAR_A expr PAR_C instruccion (ELSE instruccion)?
    ;

sentenciaSwitch
    : SWITCH PAR_A expr PAR_C LLAVE_A seccionSwitch* LLAVE_C
    ;

seccionSwitch
    : CASE expr DOSP instruccion*           # seccionCase
    | DEFAULT DOSP instruccion*             # seccionDefault
    ;

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

INT      : 'int' ;
DOUBLE   : 'double' ;
CHAR     : 'char' ;
BOOLEAN  : 'boolean' ;
STRING   : 'String' ;
VOID     : 'void' ;

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

COMENTARIO_LINEA  : '//' ~[\r\n]* -> channel(HIDDEN) ;
COMENTARIO_BLOQUE : '/*' .*? '*/' -> channel(HIDDEN) ;
WS                : [ \t\r\n\f]+ -> channel(HIDDEN) ;
