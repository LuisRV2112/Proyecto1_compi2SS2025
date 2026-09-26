// Programa de prueba con salida conocida (prueba_calculos.esperado).

%estructuras

estructura Punto:
    entero x
    entero y

estructura Alumno:
    cadena nombre
    entero notas[3]
    Punto posicion

%funciones

definir aritmetica():
    entero a = 7
    entero b = 2
    imprimir(a + b * 3 - (a - b) * 2)
    imprimir(a / b)
    flotante f = a
    imprimir(f / b)
    imprimir(-a + 10)
    caracter c = 'A'
    imprimir(c + 1)
    imprimir("a=" + a + ", f=" + f + ", c=" + c + ", ok=" + (a > b))
    imprimir(2 + 3 * 4)

definir condiciones(entero n):
    si (n > 10) entonces
        imprimir("mayor")
    sino (n == 10) entonces
        imprimir("igual")
    contrario
        imprimir("menor")

definir ciclos():
    entero suma = 0
    para(entero i = 0; i < 10; i++):
        si (i == 3) entonces
            continuar
        si (i == 8) entonces
            romper
        suma = suma + i
    imprimir("para: " + suma)
    entero k = 0
    mientras(k < 5) hacer
        k++
    imprimir("mientras: " + k)
    entero veces = 0
    hacer:
        veces++
    mientras(veces < 3)
    imprimir("hacer: " + veces)
    entero internos = 0
    para(entero x = 0; x < 3; x++):
        para(entero y = 0; y < 3; y++):
            si (y == 1) entonces
                romper
            internos++
    imprimir("anidados: " + internos)

definir elegirCaso(entero n):
    elegir(n) {
        caso 1:
            imprimir("uno")
            romper
        caso 2:
            imprimir("dos")
        caso 3:
            imprimir("tres")
            romper
        siempre:
            imprimir("otro")
    }

definir fibonacci(entero n) -> entero:
    si (n < 2) entonces
        retornar n
    retornar fibonacci(n - 1) + fibonacci(n - 2)

definir mover({} Punto p, entero dx):
    p.x = p.x + dx

definir copia(Punto p):
    p.x = 999

definir sumar([] entero datos, entero n) -> entero:
    entero total = 0
    para(entero i = 0; i < n; i++):
        total = total + datos[i]
    retornar total

definir estructuras():
    Punto p = {1, 2}
    mover(p, 10)
    imprimir("mover: " + p.x)
    copia(p)
    imprimir("copia: " + p.x)
    Punto q
    q = p
    q.y = 50
    imprimir("q: " + q.x + "," + q.y + " p: " + p.y)
    Alumno a
    a.nombre = "Ana"
    a.notas[0] = 90
    a.notas[2] = 70
    a.posicion.x = 5
    imprimir(a.nombre + " " + a.notas[0] + " " + a.notas[1] + " " + a.notas[2] + " " + a.posicion.x)
    Alumno grupo[2]
    grupo[1].notas[1] = 42
    grupo[1].posicion.y = 7
    imprimir("grupo: " + grupo[1].notas[1] + " " + grupo[1].posicion.y + " " + grupo[0].notas[1])

definir matrices():
    entero m[3][2] = { {1, 2}, {3, 4}, {5, 6} }
    entero total = 0
    para(entero i = 0; i < 3; i++):
        para(entero j = 0; j < 2; j++):
            total = total + m[i][j] * (i + 1)
    imprimir("matriz: " + total)
    imprimir("m[2][1] = " + m[2][1])
    entero datos[4] = {10, 20, 30, 40}
    imprimir("suma: " + sumar(datos, 4))
    entero cubo[2][2][2] = { { {1, 2}, {3, 4} }, { {5, 6}, {7, 8} } }
    imprimir("cubo: " + cubo[1][0][1])

definir fueraDeRango():
    entero arr[3]
    entero i = 3
    arr[i] = 1
    imprimir("no deberia llegar")
