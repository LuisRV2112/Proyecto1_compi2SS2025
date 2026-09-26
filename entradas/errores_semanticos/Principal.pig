## Errores semanticos a proposito en PigLatin. Importa los otros dos archivos. ##
import errores_semanticos.Utilidades.y
import errores_semanticos.Figura.z

VARIABILES>
esto p : Punto {1, 2};
esto f : novus Forma(3);
esto g : Forma {3, 4.5};                // un objeto no se crea con una lista
esto h : novus Punto();                 // Punto es estructura, no clase
esto total : numerus "cien";            // asignacion incompatible

MAIOR>
p <<                                    // no se puede leer en una estructura
total = multiplicar(2, 3);              // funcion no declarada
perge;                                  // continuar fuera de un ciclo
>> f;                                   // no se puede imprimir un objeto
FINIS;
