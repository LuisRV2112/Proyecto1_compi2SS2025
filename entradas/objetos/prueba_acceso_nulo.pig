## Acceso sobre null: compila, pero en ejecucion debe detenerse con un error ##
import objetos.Persona.z

VARIABILES>
esto vacio : null : Persona ;

MAIOR>
>> "antes del acceso";
>> vacio.getNombre();
>> "esto no se imprime";
FINIS;
