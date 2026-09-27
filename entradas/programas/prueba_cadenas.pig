## Lectura con conversion, cadenas y formato de flotantes. La entrada esta en prueba_cadenas.entrada ##
import programas.Textos.y

VARIABILES>
esto nombre : textum;
esto edad : numerus;
esto altura : decimalis;
esto inicial : littera;
esto acepta : bool;

MAIOR>
>> "Nombre:";
nombre <<
>> "Edad:";
edad <<
altura <<
inicial <<
acepta <<
>> "Hola " >> nombre >> ", en 5 anios tendras " >> edad + 5;
>> "altura doble: " >> altura * 2;
>> "inicial: " >> inicial >> " acepta: " >> acepta;
>> saludo(nombre);
>> "iguales: " >> (nombre == "Ana");
>> "flotantes: " >> 0.1 + 0.2 >> " " >> 10.0 / 4 >> " " >> 1.0 / 3 >> " " >> -2.5 >> " " >> 7 / 2.0;
>> repetir("ab", 3);
mostrar(verum);
FINIS;
