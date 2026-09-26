// Errores a proposito en Y?. Cada uno esta comentado con lo que se espera reportar.

%estructuras

estructura Punto:
    entero x
    entero y = 5                  // SINTACTICO: un campo no lleva inicializacion

%funciones

definir lexicos():
    entero a = 10 @ 2             // LEXICO: '@' no pertenece al lenguaje
    cadena s = "hola" $           // LEXICO: '$'

definir sinEntonces(entero n):
    si (n > 0)                    // SINTACTICO: falta 'entonces'
        n = 1

definir parentesis(entero n):
    imprimir("falta cerrar"       // SINTACTICO: falta ')'
    n = 2

definir sangriaInconsistente(entero n):
    si (n > 0) entonces
        n = 1
      n = 2                       // LEXICO: sangria de 6 columnas, no coincide con 4 ni con 8
    n = 3

definir bloqueInesperado():
    entero a = 1
        a = 2                     // SINTACTICO: INDENT sin una instruccion que abra bloque

definir sinDosPuntos(entero n) -> entero
    retornar n                    // SINTACTICO: falta ':' antes del cuerpo

definir operadorIncompleto():
    entero b = 3 *                // SINTACTICO: falta el operando derecho
    b = 4
