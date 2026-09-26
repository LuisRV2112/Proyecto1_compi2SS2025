import programas.Cuenta.z
import programas.Marcador.z

VARIABILES>
esto c : novus Cuenta("Ana");
esto d : novus Cuenta("Luis", 50.5);
series cuentas[2] : Cuenta;

MAIOR>
c.depositar(25.5);
>> c.resumen();
>> "retiro grande: " >> c.retirar(1000.0);
>> "retiro: " >> c.retirar(20.0);
>> c.resumen();
>> d.resumen();
c.setRespaldo(d);
>> c.getRespaldo().resumen();
cuentas[1] = novus Cuenta("Eva");
>> cuentas[1].getSaldo();
>> "2^10 = " >> c.potencia(2, 10);
d.tabla();
esto m : novus Marcador();
m.siguiente();
>> "marcador: " >> m.siguiente() >> " " >> m.etiqueta;
>> cuentas[0].getSaldo();
FINIS;
