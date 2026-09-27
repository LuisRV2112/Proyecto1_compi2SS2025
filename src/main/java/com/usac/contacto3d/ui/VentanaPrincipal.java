package com.usac.contacto3d.ui;

import com.usac.contacto3d.Compilador;
import com.usac.contacto3d.c3d.InterpreteCuartetas;
import com.usac.contacto3d.c3d.ListaCuartetas;
import com.usac.contacto3d.errores.ListaErrores;
import com.usac.contacto3d.errores.TipoError;
import com.usac.contacto3d.semantico.ResultadoSemantico;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * La ventana: arbol de trabajo a la izquierda, editor con pestanas al centro,
 * y abajo los resultados de la ultima compilacion (errores, cuartetas, tabla
 * de simbolos, codigo C y consola).
 *
 * Se compila el archivo de la pestana activa: un .pig arrastra lo que importa;
 * un .y o .z se analiza solo. Antes se guardan los archivos abiertos, porque
 * el compilador lee del disco (los imports tambien).
 */
public class VentanaPrincipal extends JFrame {

    private static final String PREFERENCIA_CARPETA = "ultimaCarpeta";
    private static final long SEGUNDOS_EJECUCION = 10;

    /** Lo que se hace despues de compilar. */
    private enum Accion { ANALIZAR, GENERAR_C, EJECUTAR, GCC }

    private record Compilacion(Path archivo, ListaErrores errores, ResultadoSemantico resultado,
                               ListaCuartetas cuartetas, String codigoC) {
        boolean exitosa() {
            return cuartetas != null;
        }
    }

    private final Preferences preferencias = Preferences.userNodeForPackage(VentanaPrincipal.class);

    private final ArbolTrabajo arbol = new ArbolTrabajo();
    private final JTabbedPane editores = new JTabbedPane();
    private final PanelErrores panelErrores = new PanelErrores();
    private final RSyntaxTextArea areaCuartetas = areaSoloLectura();
    private final RSyntaxTextArea areaC = areaSoloLectura();
    private final PanelTablaSimbolos panelTabla = new PanelTablaSimbolos();
    private final PanelConsola consola = new PanelConsola();
    private final JTabbedPane inferior = new JTabbedPane();
    private final JLabel estado = new JLabel(" Abri una carpeta para empezar (Archivo > Abrir carpeta)");

    private Compilacion ultima;
    private boolean trabajando;

    public VentanaPrincipal() {
        super("Contacto 3xtrat3rr3str3D");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
        setJMenuBar(crearMenu());
        setContentPane(crearContenido());
        setSize(1400, 900);
        setLocationRelativeTo(null);

        arbol.setEscucha(new ArbolTrabajo.Escucha() {
            @Override public void abrir(Path archivo)                { abrirEnPestana(archivo); }
            @Override public void renombrado(Path antes, Path despues) { reubicarPestanas(antes, despues); }
            @Override public void eliminado(Path ruta)                { cerrarPestanasDentroDe(ruta); }
        });
        panelErrores.setAlSeleccionar(this::irAError);

        String anterior = preferencias.get(PREFERENCIA_CARPETA, null);
        if (anterior != null && Files.isDirectory(Path.of(anterior))) {
            abrirCarpeta(Path.of(anterior));
        }
    }

    /* =========================== construccion =========================== */

    private JPanel crearContenido() {
        inferior.addTab("Errores", panelErrores);
        inferior.addTab("Cuartetas", conScroll(areaCuartetas));
        inferior.addTab("Tabla de simbolos", panelTabla);
        inferior.addTab("Codigo C", conScroll(areaC));
        inferior.addTab("Consola", consola);

        editores.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        JSplitPane centro = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editores, inferior);
        centro.setResizeWeight(0.68);
        centro.setBorder(null);

        arbol.setPreferredSize(new Dimension(260, 400));
        JSplitPane principal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, arbol, centro);
        principal.setDividerLocation(260);
        principal.setBorder(null);

        estado.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.add(crearBarra(), BorderLayout.NORTH);
        contenido.add(principal, BorderLayout.CENTER);
        contenido.add(estado, BorderLayout.SOUTH);
        return contenido;
    }

    private JToolBar crearBarra() {
        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.add(boton("Abrir carpeta", this::elegirCarpeta));
        barra.add(boton("Guardar", this::guardar));
        barra.add(boton("Guardar todo", this::guardarTodo));
        barra.addSeparator();
        barra.add(boton("Analizar (F5)", () -> compilar(Accion.ANALIZAR)));
        barra.add(boton("Generar C (F6)", () -> compilar(Accion.GENERAR_C)));
        barra.add(boton("Ejecutar (F7)", () -> compilar(Accion.EJECUTAR)));
        barra.add(boton("gcc y ejecutar (F8)", () -> compilar(Accion.GCC)));
        return barra;
    }

    private JMenuBar crearMenu() {
        JMenu archivo = new JMenu("Archivo");
        archivo.add(item("Abrir carpeta...", KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, this::elegirCarpeta));
        archivo.add(item("Abrir archivo...", KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                this::elegirArchivo));
        archivo.add(item("Nuevo archivo...", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, () -> crearEnArbol(false)));
        archivo.add(item("Nueva carpeta...", 0, 0, () -> crearEnArbol(true)));
        archivo.addSeparator();
        archivo.add(item("Guardar", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK, this::guardar));
        archivo.add(item("Guardar como...", 0, 0, this::guardarComo));
        archivo.add(item("Guardar todo", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                this::guardarTodo));
        archivo.add(item("Cerrar pestana", KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK,
                () -> actual().ifPresent(this::cerrarPestana)));
        archivo.addSeparator();
        archivo.add(item("Descargar proyecto (.zip)...", 0, 0, this::exportarProyecto));
        archivo.addSeparator();
        archivo.add(item("Salir", 0, 0, this::salir));

        JMenu compilar = new JMenu("Compilar");
        compilar.add(item("Analizar", KeyEvent.VK_F5, 0, () -> compilar(Accion.ANALIZAR)));
        compilar.add(item("Generar codigo C", KeyEvent.VK_F6, 0, () -> compilar(Accion.GENERAR_C)));
        compilar.add(item("Ejecutar (interprete de cuartetas)", KeyEvent.VK_F7, 0, () -> compilar(Accion.EJECUTAR)));
        compilar.add(item("Compilar con gcc y ejecutar", KeyEvent.VK_F8, 0, () -> compilar(Accion.GCC)));
        compilar.addSeparator();
        compilar.add(item("Guardar codigo C como...", 0, 0, this::guardarCodigoC));

        JMenu ayuda = new JMenu("Ayuda");
        ayuda.add(item("Acerca de", 0, 0, () -> JOptionPane.showMessageDialog(this,
                "Contacto 3xtrat3rr3str3D\nProyecto 1 - Organizacion de Lenguajes y Compiladores 2\n"
                        + "USAC, Centro Universitario de Occidente, 2026\n\n"
                        + "Compila Y?, Zetariano y PigLatin a codigo de tres direcciones y a C.",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE)));

        JMenuBar barra = new JMenuBar();
        barra.add(archivo);
        barra.add(compilar);
        barra.add(ayuda);
        return barra;
    }

    private static JMenuItem item(String texto, int tecla, int modificadores, Runnable accion) {
        JMenuItem item = new JMenuItem(texto);
        if (tecla != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(tecla, modificadores));
        }
        item.addActionListener(e -> accion.run());
        return item;
    }

    private static JButton boton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setFocusable(false);
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    private static RSyntaxTextArea areaSoloLectura() {
        RSyntaxTextArea area = new RSyntaxTextArea();
        TemaEditor.aplicar(area);
        // Sin coloreado: el enunciado prohibe librerias para colorear, y este texto no es de nuestros lenguajes
        area.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        return area;
    }

    private static JComponent conScroll(RSyntaxTextArea area) {
        RTextScrollPane scroll = new RTextScrollPane(area);
        scroll.setBorder(null);
        TemaEditor.aplicarAlMargen(scroll.getGutter());
        return scroll;
    }

    /* ======================= archivos y carpetas ======================= */

    public void abrirCarpeta(Path carpeta) {
        arbol.abrirCarpeta(carpeta);
        preferencias.put(PREFERENCIA_CARPETA, carpeta.toAbsolutePath().toString());
        setTitle("Contacto 3xtrat3rr3str3D - " + carpeta.toAbsolutePath().normalize());
        estado(" Carpeta abierta: " + carpeta.toAbsolutePath().normalize());
    }

    private void elegirCarpeta() {
        JFileChooser selector = selector();
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            abrirCarpeta(selector.getSelectedFile().toPath());
        }
    }

    private void elegirArchivo() {
        JFileChooser selector = selector();
        selector.setFileFilter(new FileNameExtensionFilter("Y?, Zetariano y PigLatin (*.y, *.z, *.pig)", "y", "z", "pig"));
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path archivo = selector.getSelectedFile().toPath();
            if (arbol.getRaiz() == null) {
                abrirCarpeta(archivo.toAbsolutePath().getParent());
            }
            abrirEnPestana(archivo);
        }
    }

    private void crearEnArbol(boolean carpeta) {
        if (arbol.getRaiz() == null) {
            estado(" Primero abri una carpeta: los archivos se crean dentro de ella");
            return;
        }
        arbol.crear(carpeta);
    }

    private JFileChooser selector() {
        return new JFileChooser(arbol.getRaiz() == null ? null : arbol.getRaiz().toFile());
    }

    private void abrirEnPestana(Path archivo) {
        Optional<PestanaEditor> abierta = pestanas().stream()
                .filter(p -> p.getArchivo().toAbsolutePath().equals(archivo.toAbsolutePath())).findFirst();
        if (abierta.isPresent()) {
            editores.setSelectedComponent(abierta.get());
            return;
        }
        try {
            PestanaEditor pestana = new PestanaEditor(archivo);
            editores.addTab(pestana.getTitulo(), pestana);
            int indice = editores.indexOfComponent(pestana);
            JLabel titulo = new JLabel(pestana.getTitulo(), IconoArchivo.para(archivo), JLabel.LEFT);
            editores.setTabComponentAt(indice, encabezado(pestana, titulo));
            pestana.setAlCambiarEstado(() -> titulo.setText(pestana.getTitulo()));
            if (ultima != null) {
                pestana.marcarErrores(ultima.errores());
            }
            editores.setSelectedComponent(pestana);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir " + archivo + ": " + e.getMessage(),
                    "Abrir", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Titulo de la pestana con su boton de cerrar. */
    private JPanel encabezado(PestanaEditor pestana, JLabel titulo) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        panel.setOpaque(false);
        JButton cerrar = new JButton("x");
        cerrar.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        cerrar.setContentAreaFilled(false);
        cerrar.setFocusable(false);
        cerrar.setToolTipText("Cerrar");
        cerrar.addActionListener(e -> cerrarPestana(pestana));
        panel.add(titulo);
        panel.add(cerrar);
        return panel;
    }

    private void cerrarPestana(PestanaEditor pestana) {
        if (pestana.isModificado()) {
            int respuesta = JOptionPane.showConfirmDialog(this,
                    pestana.getArchivo().getFileName() + " tiene cambios sin guardar. Guardarlos?",
                    "Cerrar", JOptionPane.YES_NO_CANCEL_OPTION);
            if (respuesta == JOptionPane.CANCEL_OPTION || respuesta == JOptionPane.CLOSED_OPTION) {
                return;
            }
            if (respuesta == JOptionPane.YES_OPTION && !guardar(pestana)) {
                return;
            }
        }
        editores.remove(pestana);
    }

    private void guardar() {
        actual().ifPresent(pestana -> {
            if (guardar(pestana)) {
                estado(" Guardado " + pestana.getArchivo().getFileName());
            }
        });
    }

    private boolean guardar(PestanaEditor pestana) {
        try {
            pestana.guardar();
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar: " + e.getMessage(), "Guardar",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean guardarTodo() {
        boolean todo = true;
        int guardados = 0;
        for (PestanaEditor pestana : pestanas()) {
            if (pestana.isModificado()) {
                todo &= guardar(pestana);
                guardados++;
            }
        }
        estado(" " + guardados + " archivo(s) guardado(s)");
        return todo;
    }

    private void guardarComo() {
        actual().ifPresent(pestana -> {
            JFileChooser selector = selector();
            selector.setSelectedFile(pestana.getArchivo().toFile());
            if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    pestana.guardarComo(selector.getSelectedFile().toPath());
                    arbol.actualizar();
                    estado(" Guardado como " + pestana.getArchivo());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "No se pudo guardar: " + e.getMessage(), "Guardar como",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /** "Descargar" la carpeta de trabajo completa como un .zip. */
    private void exportarProyecto() {
        Path raiz = arbol.getRaiz();
        if (raiz == null) {
            estado(" No hay una carpeta abierta para descargar");
            return;
        }
        guardarTodo();
        JFileChooser selector = selector();
        selector.setSelectedFile(raiz.resolveSibling(raiz.getFileName() + ".zip").toFile());
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path destino = selector.getSelectedFile().toPath();
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(destino));
             Stream<Path> recorrido = Files.walk(raiz)) {
            for (Path archivo : recorrido.filter(Files::isRegularFile).toList()) {
                if (archivo.toAbsolutePath().equals(destino.toAbsolutePath())) {
                    continue;   // el propio zip, si se guardo dentro de la carpeta
                }
                zip.putNextEntry(new ZipEntry(raiz.getFileName() + "/" + raiz.relativize(archivo).toString().replace('\\', '/')));
                Files.copy(archivo, zip);
                zip.closeEntry();
            }
            estado(" Proyecto descargado en " + destino);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo crear el zip: " + e.getMessage(), "Descargar",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reubicarPestanas(Path antes, Path despues) {
        for (PestanaEditor pestana : pestanas()) {
            Path archivo = pestana.getArchivo();
            if (archivo.startsWith(antes)) {
                pestana.setArchivo(despues.resolve(antes.relativize(archivo)));
            }
        }
    }

    private void cerrarPestanasDentroDe(Path ruta) {
        for (PestanaEditor pestana : pestanas()) {
            if (pestana.getArchivo().startsWith(ruta)) {
                editores.remove(pestana);
            }
        }
    }

    private void salir() {
        boolean hayCambios = pestanas().stream().anyMatch(PestanaEditor::isModificado);
        if (hayCambios) {
            Object[] opciones = {"Guardar todo y salir", "Salir sin guardar", "Cancelar"};
            int respuesta = JOptionPane.showOptionDialog(this, "Hay archivos con cambios sin guardar.", "Salir",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, opciones, opciones[0]);
            if (respuesta == 2 || respuesta == JOptionPane.CLOSED_OPTION || (respuesta == 0 && !guardarTodo())) {
                return;
            }
        }
        dispose();
        System.exit(0);
    }

    /* =========================== compilacion =========================== */

    private void compilar(Accion accion) {
        if (trabajando) {
            return;
        }
        Optional<PestanaEditor> activa = actual();
        if (activa.isEmpty()) {
            estado(" Abri un archivo .pig, .y o .z para compilarlo");
            return;
        }
        if (!guardarTodo()) {
            return;
        }
        Path archivo = activa.get().getArchivo();
        trabajando = true;
        estado(" Compilando " + archivo.getFileName() + "...");

        new SwingWorker<Compilacion, Void>() {
            @Override
            protected Compilacion doInBackground() throws Exception {
                Compilador compilador = new Compilador();
                ResultadoSemantico resultado = compilador.compilar(archivo);
                ListaCuartetas cuartetas = null;
                String codigoC = null;
                if (resultado != null && !compilador.getErrores().hayErrores()) {
                    cuartetas = compilador.generarCuartetas(resultado);
                    codigoC = compilador.traducirAC(cuartetas);
                }
                compilador.getErrores().ordenar();
                return new Compilacion(archivo, compilador.getErrores(), resultado, cuartetas, codigoC);
            }

            @Override
            protected void done() {
                trabajando = false;
                try {
                    mostrar(get());
                } catch (Exception e) {
                    estado(" Error interno al compilar: " + e.getCause());
                    return;
                }
                boolean ejecutar = accion == Accion.EJECUTAR || accion == Accion.GCC;
                if (ejecutar && !ultima.exitosa()) {
                    // La pestana Errores ya quedo seleccionada; la consola dice por que no corrio
                    consola.mostrar("No se ejecuto: hay " + ultima.errores().cantidad()
                            + " error(es). Corregilos (ver la pestana Errores) y volve a ejecutar.");
                } else if (ejecutar && ultima.resultado().principal() == null) {
                    // Un .y o .z solo define funciones, estructuras o clases: no hay MAIOR> que correr
                    consola.mostrar("No hay nada que ejecutar: " + ultima.archivo().getFileName()
                            + " no tiene programa principal.\n\nSolo un .pig tiene MAIOR>. Abri el .pig que importa "
                            + "este archivo, dejalo en la pestana activa y ejecuta desde ahi.");
                    inferior.setSelectedComponent(consola);
                    estado(" Nada que ejecutar: el programa principal esta en un .pig");
                } else if (ultima.exitosa()) {
                    switch (accion) {
                        case ANALIZAR -> { }
                        case GENERAR_C -> guardarCodigoCEnSalida();
                        case EJECUTAR -> ejecutarConInterprete();
                        case GCC -> ejecutarConGcc();
                    }
                }
            }
        }.execute();
    }

    private void mostrar(Compilacion compilacion) {
        ultima = compilacion;
        ListaErrores errores = compilacion.errores();
        panelErrores.mostrar(errores);
        pestanas().forEach(p -> p.marcarErrores(errores));
        panelTabla.mostrar(compilacion.resultado() == null ? null : compilacion.resultado().tabla());
        areaCuartetas.setText(compilacion.exitosa() ? compilacion.cuartetas().toString() : "");
        areaCuartetas.setCaretPosition(0);
        areaC.setText(compilacion.exitosa() ? compilacion.codigoC() : "");
        areaC.setCaretPosition(0);

        String archivo = compilacion.archivo().getFileName().toString();
        if (errores.hayErrores()) {
            estado(String.format(" %s: %d error(es) - %d lexico(s), %d sintactico(s), %d semantico(s)",
                    archivo, errores.cantidad(), errores.getPorTipo(TipoError.LEXICO).size(),
                    errores.getPorTipo(TipoError.SINTACTICO).size(), errores.getPorTipo(TipoError.SEMANTICO).size()));
            inferior.setSelectedComponent(panelErrores);
        } else {
            estado(String.format(" %s: sin errores - %d cuartetas, %d simbolos", archivo,
                    compilacion.cuartetas().getCuartetas().size(), compilacion.resultado().tabla().getHistorial().size()));
            inferior.setSelectedIndex(1);
        }
    }

    /** Doble clic en un error: abre su archivo (puede ser un import) y va a la linea. */
    private void irAError(String nombreArchivo, int linea) {
        Optional<PestanaEditor> abierta = pestanas().stream()
                .filter(p -> p.getArchivo().getFileName().toString().equals(nombreArchivo)).findFirst();
        if (abierta.isEmpty()) {
            Optional<Path> encontrado = arbol.buscar(nombreArchivo);
            if (encontrado.isEmpty() && ultima != null) {
                Path junto = ultima.archivo().resolveSibling(nombreArchivo);
                encontrado = Files.exists(junto) ? Optional.of(junto) : Optional.empty();
            }
            encontrado.ifPresent(this::abrirEnPestana);
            abierta = actual().filter(p -> p.getArchivo().getFileName().toString().equals(nombreArchivo));
        }
        abierta.ifPresent(p -> {
            editores.setSelectedComponent(p);
            p.irALinea(linea);
        });
    }

    /* ========================= codigo C y ejecucion ========================= */

    /** El .c queda en la carpeta salida/ del proyecto (o junto al archivo si no hay carpeta abierta). */
    private void guardarCodigoCEnSalida() {
        Path base = arbol.getRaiz() != null ? arbol.getRaiz() : ultima.archivo().toAbsolutePath().getParent();
        Path destino = base.resolve("salida").resolve(nombreSinExtension(ultima.archivo()) + ".c");
        try {
            Files.createDirectories(destino.getParent());
            Files.writeString(destino, ultima.codigoC(), StandardCharsets.UTF_8);
            arbol.actualizar();
            inferior.setSelectedIndex(3);
            estado(" Codigo C guardado en " + destino + "   (compilar: gcc " + destino.getFileName() + " -o programa)");
        } catch (IOException e) {
            estado(" No se pudo guardar el codigo C: " + e.getMessage());
        }
    }

    private void guardarCodigoC() {
        if (ultima == null || !ultima.exitosa()) {
            estado(" Primero compila un programa sin errores (F5)");
            return;
        }
        JFileChooser selector = selector();
        selector.setSelectedFile(new java.io.File(nombreSinExtension(ultima.archivo()) + ".c"));
        if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.writeString(selector.getSelectedFile().toPath(), ultima.codigoC(), StandardCharsets.UTF_8);
                arbol.actualizar();
                estado(" Codigo C guardado en " + selector.getSelectedFile());
            } catch (IOException e) {
                estado(" No se pudo guardar el codigo C: " + e.getMessage());
            }
        }
    }

    private void ejecutarConInterprete() {
        ListaCuartetas cuartetas = ultima.cuartetas();
        String entrada = consola.getEntrada();
        enSegundoPlano("Ejecutando con el interprete...", () -> InterpreteCuartetas.ejecutar(cuartetas, entrada),
                "Ejecutado con el interprete de cuartetas");
    }

    /** Compila el .c con gcc en una carpeta temporal y lo corre con la entrada de la consola. */
    private void ejecutarConGcc() {
        String codigo = ultima.codigoC();
        String entrada = consola.getEntrada();
        enSegundoPlano("Compilando con gcc y ejecutando...", () -> {
            Path carpeta = Files.createTempDirectory("contacto3d");
            Path fuente = carpeta.resolve("programa.c");
            Path ejecutable = carpeta.resolve("programa");
            Files.writeString(fuente, codigo, StandardCharsets.UTF_8);

            Process gcc;
            try {
                gcc = new ProcessBuilder("gcc", fuente.toString(), "-o", ejecutable.toString())
                        .redirectErrorStream(true).start();
            } catch (IOException e) {
                return "No se encontro gcc. Instalalo o usa Ejecutar (F7), que corre las cuartetas sin C.";
            }
            String mensajesGcc = new String(gcc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (gcc.waitFor() != 0) {
                return "gcc no pudo compilar el programa:\n" + mensajesGcc;
            }

            Path archivoSalida = carpeta.resolve("salida.txt");
            Path archivoEntrada = carpeta.resolve("entrada.txt");
            Files.writeString(archivoEntrada, entrada, StandardCharsets.UTF_8);
            Process programa = new ProcessBuilder(ejecutable.toString())
                    .redirectInput(archivoEntrada.toFile())
                    .redirectOutput(archivoSalida.toFile())
                    .redirectErrorStream(true).start();
            boolean termino = programa.waitFor(SEGUNDOS_EJECUCION, TimeUnit.SECONDS);
            if (!termino) {
                programa.destroyForcibly();
            }
            String salida = Files.readString(archivoSalida, StandardCharsets.UTF_8);
            return termino ? salida : salida + "\n[Se detuvo: tardo mas de " + SEGUNDOS_EJECUCION + " segundos]";
        }, "Compilado con gcc y ejecutado");
    }

    @FunctionalInterface
    private interface Tarea {
        String ejecutar() throws Exception;
    }

    private void enSegundoPlano(String mensaje, Tarea tarea, String alTerminar) {
        trabajando = true;
        estado(" " + mensaje);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return tarea.ejecutar();
            }

            @Override
            protected void done() {
                trabajando = false;
                try {
                    String salida = get();
                    consola.mostrar(salida.isEmpty() ? "(el programa termino sin imprimir nada)" : salida);
                    estado(" " + alTerminar);
                } catch (Exception e) {
                    consola.mostrar("Error: " + e.getCause());
                    estado(" No se pudo ejecutar");
                }
                inferior.setSelectedComponent(consola);
            }
        }.execute();
    }

    /* =========================== utilidades =========================== */

    private List<PestanaEditor> pestanas() {
        List<PestanaEditor> lista = new ArrayList<>();
        for (int i = 0; i < editores.getTabCount(); i++) {
            lista.add((PestanaEditor) editores.getComponentAt(i));
        }
        return lista;
    }

    private Optional<PestanaEditor> actual() {
        return Optional.ofNullable((PestanaEditor) editores.getSelectedComponent());
    }

    private void estado(String texto) {
        estado.setText(texto);
    }

    private static String nombreSinExtension(Path archivo) {
        String nombre = archivo.getFileName().toString();
        int punto = nombre.lastIndexOf('.');
        return punto > 0 ? nombre.substring(0, punto) : nombre;
    }
}
