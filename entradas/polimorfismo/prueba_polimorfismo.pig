## Despacho dinamico: la misma variable Impresora ejecuta el metodo del objeto real ##
import polimorfismo.Impresora.z
import polimorfismo.ImpresoraPDF.z
import polimorfismo.ImpresoraExcel.z
import polimorfismo.ImpresoraColor.z

VARIABILES>
esto a : novus Impresora() : Impresora ;
esto b : novus ImpresoraPDF() : Impresora ;
esto c : novus ImpresoraExcel() : Impresora ;
esto d : novus ImpresoraColor() : Impresora ;
esto p : novus ImpresoraPDF() : ImpresoraPDF ;
series todas[4] : Impresora;

MAIOR>
>> "--- por variable ---";
a.imprimir();
b.imprimir();
c.imprimir();
d.imprimir();
>> "--- por this en un metodo heredado ---";
a.trabajo();
b.trabajo();
c.trabajo();
d.trabajo();
>> "--- por arreglo ---";
todas[0] = a;
todas[1] = b;
todas[2] = c;
todas[3] = d;
per (esto i : numerus 0; i < 4; i++) {
    >> todas[i].tipo();
} finis;
>> "--- metodo nuevo despues de uno sobrescrito ---";
p.comprimir();
p.imprimir();
FINIS;
