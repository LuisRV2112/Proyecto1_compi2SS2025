## Cada linea de MAIOR> debe dar error semantico ##
import objetos.Persona.z

VARIABILES>
esto p : novus Persona();
esto n : numerus 0;
esto t : textum "";

MAIOR>
n = null;
t = null;
>> null + 1;
>> p * null;
>> null;
si (n == null) {
    >> "no";
} finis;
FINIS;
