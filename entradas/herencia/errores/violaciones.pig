## Errores de herencia y encapsulamiento: ver los comentarios de cada .z ##
import errores.Base.z
import errores.Hija.z
import errores.Nieta.z
import errores.CicloA.z
import errores.CicloB.z
import errores.SinPadre.z

VARIABILES>
esto b : novus Base(1);
esto h : novus Hija(1, 2);

MAIOR>
>> b.pub;
>> b.prot;
>> b.sinModificador;
b = h;
h = b;
FINIS;
