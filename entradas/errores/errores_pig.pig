## Errores a proposito en PigLatin. Cada uno esta comentado con lo que se espera reportar. ##
import carpeta                          // SINTACTICO: falta '.Archivo.ext'
import ejemplo.Funciones.y

VARIABILES>
esto edad : numerus 20                  // SINTACTICO: falta ';' (se reporta en la linea siguiente)
esto nombre : textum "x";
esto costo : numerus 10 $ 2;            // LEXICO: '$'
structura Punto { esto x : numerus; } finis;   // SINTACTICO: PigLatin ya no define estructuras
esto dir : Direccion {calle: "Real", numero: 42};   // SINTACTICO: los literales ahora son posicionales

MAIOR>
>> "Hola" ;
si (edad > 18) {
    >> "adulto";
}                                       // SINTACTICO: falta 'finis;'
dum edad < 100 {                        // SINTACTICO: faltan los parentesis
    edad++;
} finis;
esto obj : novus Persona(1, 2;          // SINTACTICO: falta ')'
edad = 3 +;                             // SINTACTICO: falta el operando
>> "fin" ;
// SINTACTICO: falta "FINIS;" al final del archivo
