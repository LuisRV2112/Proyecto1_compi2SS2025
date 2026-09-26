// Errores semanticos a proposito en Y?. Cada linea marcada debe reportar exactamente eso.

%estructuras

estructura Punto:
    entero x
    entero y
    entero x                      // campo repetido en una estructura

estructura Nodo:
    entero valor
    Nodo siguiente                // estructura que se contiene a si misma

estructura Caja:
    Fantasma contenido            // tipo no declarado

%funciones

definir suma(entero a, entero b) -> entero:
    retornar a + b

definir suma(entero a, entero b) -> entero:     // funcion repetida (misma firma)
    retornar a

definir variables():
    entero a = 5
    entero a = 6                  // redeclaracion en el mismo ambito
    b = 3                         // variable no declarada
    entero c = "texto"            // asignacion incompatible
    entero d = 3.5                // asignacion que pierde informacion
    flotante e = 7                // valido: entero -> flotante amplia
    cadena f = "a" - 1            // la cadena solo admite '+'

definir estructuras():
    Punto p = {1, 2}
    p.z = 4                       // campo inexistente
    entero n = 5
    n.x = 1                       // acceso a miembro sobre algo que no es estructura
    Punto q = {1, 2, 3, 4}        // la lista no coincide con los campos
    imprimir(p)                   // no se puede imprimir una estructura

definir arreglos(entero i):
    entero arr[5]
    entero m[3][2]
    arr[7] = 1                    // indice fuera de rango (constante)
    arr[2 + 3] = 1                // indice fuera de rango (2 + 3 = 5)
    arr[i] = 1                    // valido: no se puede evaluar
    m[1] = 4                      // cantidad de indices distinta a las dimensiones
    entero cero[0]                // dimension no positiva

definir llamadas():
    entero r = suma(1)            // cantidad de argumentos
    r = suma(1, "dos")            // tipo de argumento
    r = restar(1, 2)              // funcion no declarada

definir control(entero n) -> entero:
    si (n) entonces               // condicion no booleana
        romper                    // romper fuera de un ciclo
    continuar                     // continuar fuera de un ciclo
    elegir(n) {
        caso 1:
            romper
        caso 1:                   // caso repetido
            romper
        siempre:
            romper
    }
    retornar "texto"              // retorno de tipo incorrecto

definir sinRetorno(entero n) -> entero:     // no retorna en todos los caminos
    si (n > 0) entonces
        retornar 1

definir inalcanzable() -> entero:
    retornar 1
    imprimir("nunca")             // codigo inalcanzable
