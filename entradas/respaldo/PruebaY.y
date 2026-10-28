%estructuras

estructura Punto:
    entero x
    entero y

estructura Alumno:
    cadena nombre
    entero notas[3]
    Punto pos

%funciones

definir yVariables():
    entero a = 7
    flotante f = 2.5
    cadena c = "hola"
    caracter l = 'k'
    bool b = verdadero
    a = a + 3
    f = f * 2
    c = c + " mundo"
    imprimir(a, f, c, l, b)

definir yLectura():
    leer()
    cadena nombre = leer()
    nombre = nombre + "!"
    imprimir("Y leyo: " + nombre)

definir yCondicionales(entero x) -> cadena:
    si (x > 10) entonces
        retornar "mayor"
    sino (x == 10) entonces
        retornar "igual"
    contrario
        retornar "menor"

definir yElegir(entero op):
    elegir(op) {
        caso 1:
            imprimir("uno")
            romper
        caso 2:
        caso 3:
            imprimir("dos o tres")
        caso 4:
            imprimir("cae al cuatro")
            romper
        siempre:
            imprimir("otro")
    }

definir yCiclos():
    entero suma = 0
    para(entero i = 0; i < 10; i++):
        si (i == 3) entonces
            continuar
        si (i == 7) entonces
            romper
        suma = suma + i
    imprimir("para:", suma)
    entero k = 0
    mientras (k < 5) hacer
        k++
    imprimir("mientras:", k)
    entero j = 0
    hacer:
        j = j + 2
    mientras(j < 9)
    imprimir("hacer:", j)

definir yArreglos():
    entero v[4]
    entero m[2][3] = { {1, 2, 3}, {4, 5, 6} }
    entero c[2][2][2] = { { {1, 2}, {3, 4} }, { {5, 6}, {7, 8} } }
    para(entero i = 0; i < 4; i++):
        v[i] = i * i
    imprimir(v[3], m[1][2], c[1][0][1], v[1] + m[0][0])

definir modificar([] entero arr, {} Alumno a):
    arr[0] = 99
    a.nombre = "Modificado"
    a.pos.x = 5

definir factorial(entero n) -> entero:
    si (n <= 1) entonces
        retornar 1
    retornar n * factorial(n - 1)

definir promedio(flotante a, flotante b) -> flotante:
    retornar (a + b) / 2

definir yEstructurasYFunciones():
    entero arr[2]
    entero ns[3] = {10, 20, 30}
    Punto p = {1, 2}
    Alumno al = {"Ana", ns, p}
    imprimir(al.nombre, al.notas[1], al.pos.y)
    modificar(arr, al)
    imprimir(arr[0], al.nombre, al.pos.x)
    imprimir(factorial(5), promedio(3.0, 4.0))
