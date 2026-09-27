// Ejemplo de Y? que usa todas las construcciones del lenguaje.
// Sirve de regresion: debe parsear con 0 errores.

/* Comentario de bloque
   en varias lineas */

%estructuras

estructura Direccion:
    cadena calle
    entero numero

estructura Ciudadano:
    entero edad
    cadena nombre
    flotante promedio
    caracter letra
    bool activo
    entero notas[10]
    entero tablero[3][3]
    Direccion direccion      // estructura anidada

%funciones

definir sinRetorno(entero miEntero):
    miEntero = 90 * 10

definir conRetorno(entero miEntero) -> entero :
    retornar 160

definir calcularPoder(entero fuerza) -> entero:
    retornar fuerza * 2

definir precedencia() -> entero:
    // 2 + 3 * 4 debe agruparse como 2 + (3 * 4)
    entero a = 2 + 3 * 4
    entero b = (2 + 3) * 4
    entero c = -a + b / 2 - 1
    bool d = !(a < b) && b >= c || a != c
    retornar a

definir porReferencia([] entero arreglo, [][] entero matriz, {} Ciudadano p, entero n):
    arreglo[0] = n
    matriz[1][2] = arreglo[0] + 1
    p.nombre = "Ana"
    p.direccion.numero = 42
    p.notas[3] = p.notas[2] * 2

definir condicionales(entero x) -> cadena:
    cadena resultado = ""
    si (x > 10) entonces
        resultado = "mayor"
    sino (x == 10) entonces
        resultado = "igual"
    sino (x < 0) entonces
        resultado = "negativo"
    contrario
        resultado = "menor"
    retornar resultado

definir seleccion(entero opcion):
    elegir(opcion) {
        caso 1:
            imprimir("uno")
            romper
        caso 2:
        caso 3:
            imprimir("dos o tres")    // el 2 cae al 3 (fall-through)
            romper
        caso 4: imprimir("cuatro")
        siempre:
            imprimir("otro")
    }

definir ciclos(entero n):
    para(entero i = 0; i < n; i++):
        si (i == 5) entonces
            continuar
        mientras (n > 0) hacer
            n--
            si (n == 3) entonces
                romper
    entero j = 0
    hacer:
        j = j + 1
    mientras(j < 10)
    para(j = 10; j > 0; j = j - 2):
        imprimir(j)

definir entradaSalida():
    leer()                        // solo lee, no guarda
    cadena nombre = leer()
    nombre = leer();              // el ';' final es opcional
    caracter letra = 'x'
    flotante pi = 3.1416
    bool listo = verdadero
    imprimir("Hola " + nombre, letra, pi, falso)

definir arreglos():
    entero lista[5]
    entero matriz[3][2] = { {1, 2}, {3, 4}, {5, 6} }
    entero cubo[2][2][2] = {
        { {1, 2}, {3, 4} },
        { {5, 6}, {7, 8} }
    }
    Ciudadano gente[3]
    Direccion casa = {"Calle Real", 42}
    Ciudadano alguien = {20, "Luis", 85.5, 'L', verdadero, lista, matriz, casa}
    gente[0].edad = 30
    porReferencia(lista, matriz, alguien, 7)

definir anidado(entero n) -> entero:
	// indentado con tabs (1 tab = 4 columnas)
	si (n <= 0) entonces
		retornar 0
	si (n > 0) entonces
		para(entero i = 0; i < n; i++):
			si (i > 2) entonces
				mientras (i < 5) hacer
					i++
	retornar anidado(n - 1) + conRetorno(n)

definir estructuraLocal() -> flotante:
    // Se pueden declarar estructuras dentro de las funciones
    estructura Punto:
        entero x
        entero y
        flotante promedio
    Punto p1 = {10, 20, 85.5}
    Punto p2 = {5, 15, 90.0};
    Punto p3
    p3 = p1
    entero contador = 0
    mientras(contador < 5) hacer
        contador++;
        si(contador == 2) entonces
            continuar;
    retornar p1.promedio + p2.promedio
