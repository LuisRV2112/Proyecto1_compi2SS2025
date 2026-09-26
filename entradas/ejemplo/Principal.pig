##
    Ejemplo de PigLatin que usa todas las construcciones del lenguaje.
    Es el punto de entrada: importa la clase Persona y las funciones y
    estructuras de Funciones.y. Debe parsear con 0 errores.
##
import ejemplo.Persona.z
import ejemplo.Funciones.y

VARIABILES>
esto edad : numerus 20;
esto cifrado : falsus;                  // forma rapida: el tipo bool sale del literal
esto activo : bool verum;
esto comandante : textum "Estudiante X";
esto fuerza : numerus 10;
esto total : numerus fuerza * 2;
esto gravedad : decimalis 9.81;
esto inicial : littera 'a';
esto sinValor : numerus;

series mis_enteros[3] : numerus {1, 1, 1};
series nombres[2] : textum;
series matriz[2][3] : numerus { {3, 2, 1}, {4, 5, 6} }

// Estructuras importadas de Funciones.y, con literales posicionales
esto mi_direccion : Direccion {"Calle Real", 42};
esto otra : Direccion {"Avenida Central", 500}
series resistencia[3] : Ciudadano;

// Objetos de Persona.z
esto miObjeto : novus Persona("Profesor", 12);
esto otroObjeto : novus Persona();
esto sinObjeto : Persona;
series misObjetos[10] : Persona;

MUNERA>
// Funcion con retorno y variables locales
ratio numerus doble(esto n : numerus) {
    VARIABILES[
        esto resultado : numerus n * 2;
        series temporal[2] : numerus;
    ]
    reddere resultado;
} finis;

// Funcion sin retorno
actio saludar(esto nombre : textum, esto veces : numerus) {
    per (esto i : numerus 0; i < veces; i++) {
        >> "Hola " >> nombre;
    }
} finis;

MAIOR>
>> "Hola comandante!" ;
>> "Ingresa tu nombre por favor" ;
comandante <<
>> "Bienvenido" >> comandante ;
<<

// 2 + 3 * 4 debe agruparse como 2 + (3 * 4)
total = 2 + 3 * 4;
total = (2 + 3) * 4 / 2 - -1;
cifrado = non (edad < 18) && edad >= 10 || edad != 5;
fuerza++;
fuerza--;
nombres[0] = "Capitan Esparragos";
nombres[1] = nombres[0] + " clon";
matriz[1][2] = mis_enteros[0] + 1;

esto ciudadano : Ciudadano {20, "Valeria", 85.5, 'V', verum, mis_enteros, matriz, {"Avenida Central", 500}};
esto ciudadano2 : Ciudadano {12, miObjeto.getNombre(), 70.5, 'M', falsus, mis_enteros, matriz, mi_direccion};
mi_direccion = {"Calle Nueva", 7};

// Objetos: atributos, metodos y cadenas de accesos
esto anidado : novus Persona(miObjeto.getNombre(), 30);
miObjeto.nombre = "Yennifer";
misObjetos[9] = novus Persona("Luis", 40);
misObjetos[9].saludar();
comandante = misObjetos[9].getAmigo().getNombre();
>> "Nace en: " >> miObjeto.calcularAnioNacimiento(2026);

// Funciones de Funciones.y y de MUNERA>
>> "Tu poder es: " >> calcularPoder(fuerza);
saludar(comandante, 3);
total = doble(total) + calcularPoder(2);

si (edad >= 18) {
    cifrado = verum;
    fuerza = 12;
} finis;

si (edad > 60) {
    >> "mayor";
} aliter (edad > 18) {
    >> "adulto";
} aliter {
    >> "menor";
} finis;

dum (fuerza < 100) {
    fuerza = fuerza + 10;
    si (fuerza == 50) {
        perge;
    } finis;
} finis;

facere {
    fuerza = fuerza - 1;
    si (fuerza < 0) {
        interrumpe;
    } finis;
} dum (fuerza > 10);

per (esto j : numerus 0; j < 3; j++) {
    >> j;
} finis;

per (total = 10; total > 0; total = total - 2) {
    >> total;
}

>> "La puerta esta cifrada?" >> cifrado ;

FINIS;
