// Funciones de cadenas para prueba_cadenas.pig.
%funciones

definir saludo(cadena n) -> cadena:
    cadena r = "Bienvenido, " + n + "!"
    si (n == "Ana") entonces
        r = r + " (admin)"
    retornar r

definir repetir(cadena s, entero veces) -> cadena:
    cadena r = ""
    para(entero i = 0; i < veces; i++):
        r = r + s
    retornar r

definir mostrar(bool b):
    imprimir("y dice: ", b)
