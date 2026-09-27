package com.usac.contacto3d.semantico;

import com.usac.contacto3d.ast.Programa;
import com.usac.contacto3d.ast.declaraciones.Importacion;
import com.usac.contacto3d.errores.ErrorCompilacion;
import com.usac.contacto3d.errores.ListaErrores;
import com.usac.contacto3d.errores.TipoError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ResolvedorImports {

    @FunctionalInterface
    public interface Cargador {
        Programa cargar(Path archivo) throws IOException;
    }

    private static final int PROFUNDIDAD_BUSQUEDA = 8;

    private final ListaErrores errores;
    private final Cargador cargador;

    public ResolvedorImports(ListaErrores errores, Cargador cargador) {
        this.errores = errores;
        this.cargador = cargador;
    }

    public List<Programa> resolver(Path rutaPrincipal, Programa principal) throws IOException {
        List<Programa> modulos = new ArrayList<>();
        modulos.add(principal);

        Path base = rutaPrincipal.toAbsolutePath().getParent();
        Set<Path> cargados = new HashSet<>();
        cargados.add(rutaPrincipal.toRealPath());

        for (Importacion importacion : principal.getImportaciones()) {
            String extension = importacion.getExtension();
            if (!extension.equals("y") && !extension.equals("z")) {
                error(importacion, "Solo se pueden importar archivos .y o .z; se importo un ." + extension);
                continue;
            }
            Path ruta = buscar(base, importacion);
            if (ruta == null) {
                error(importacion, "No se encontro el archivo importado '" + importacion.getRutaRelativa()
                        + "'. Se esperaba en la carpeta de " + principal.getArchivo() + " o debajo de ella");
                continue;
            }
            if (!cargados.add(ruta.toRealPath())) {
                continue;
            }
            Programa modulo = cargador.cargar(ruta);
            if (modulo != null) {
                modulos.add(modulo);
            }
        }
        return modulos;
    }

    private Path buscar(Path base, Importacion importacion) throws IOException {
        Path relativa = Path.of(importacion.getRutaRelativa());
        List<Path> candidatos = new ArrayList<>();
        candidatos.add(base.resolve(relativa));
        if (base.getParent() != null) {
            candidatos.add(base.getParent().resolve(relativa));
        }
        candidatos.add(base.resolve(importacion.getNombreArchivo()));

        for (Path candidato : candidatos) {
            if (Files.isRegularFile(candidato)) {
                return candidato;
            }
        }

        List<Path> encontrados;
        try (Stream<Path> recorrido = Files.walk(base, PROFUNDIDAD_BUSQUEDA)) {
            encontrados = recorrido
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals(importacion.getNombreArchivo()))
                    .sorted()
                    .toList();
        }
        return encontrados.stream().filter(p -> p.endsWith(relativa)).findFirst()
                .orElse(encontrados.isEmpty() ? null : encontrados.get(0));
    }

    private void error(Importacion importacion, String descripcion) {
        errores.agregar(new ErrorCompilacion(TipoError.SEMANTICO, importacion.getArchivo(),
                String.join(".", importacion.getPartes()), descripcion,
                importacion.getLinea(), importacion.getColumna()));
    }
}
