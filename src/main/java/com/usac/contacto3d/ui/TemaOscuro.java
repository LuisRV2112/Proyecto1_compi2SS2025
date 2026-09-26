package com.usac.contacto3d.ui;

import javax.swing.BorderFactory;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import java.awt.Color;
import java.awt.Font;

/**
 * Deja toda la ventana en modo oscuro SIN depender de FlatLaf.
 *
 * El editor y los paneles propios se pintan solos, pero la barra de menu, las
 * pestañas, los botones y las tablas los dibuja el look and feel, y el que trae
 * Java por defecto (Metal) los pinta en gris claro: la ventana quedaria oscura
 * con las orillas blancas.
 *
 * Se intenta FlatLaf por reflexion; si no esta, se recolorea Metal completo con
 * un MetalTheme propio. Cambiar solo las claves de UIManager no alcanza, porque
 * Metal pinta muchos bordes con los colores de su tema interno.
 */
public final class TemaOscuro {

    private static final Color FONDO       = new Color(0x1E1E1E);
    private static final Color FONDO_PANEL = new Color(0x252526);
    private static final Color FONDO_CLARO = new Color(0x2D2D30);
    private static final Color BORDE       = new Color(0x3E3E42);
    private static final Color TEXTO       = new Color(0xD4D4D4);
    private static final Color TEXTO_TENUE = new Color(0x9A9A9A);
    private static final Color SELECCION   = new Color(0x094771);
    private static final Color ACENTO      = new Color(0x569CD6);

    private TemaOscuro() {
    }

    /** @return el nombre del tema aplicado, para dejarlo en la consola */
    public static String aplicar() {
        String aplicado = intentarFlatLaf();
        if (aplicado == null) {
            aplicado = aplicarMetalOscuro();
        }
        ajustesComunes();
        textosEnEspaniol();
        return aplicado;
    }

    private static String intentarFlatLaf() {
        try {
            Class<?> clase = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
            UIManager.setLookAndFeel((LookAndFeel) clase.getDeclaredConstructor().newInstance());
            return "FlatLaf oscuro";
        } catch (Exception noDisponible) {
            return null;
        }
    }

    private static String aplicarMetalOscuro() {
        try {
            MetalLookAndFeel.setCurrentTheme(new TemaMetalOscuro());
            UIManager.setLookAndFeel(new MetalLookAndFeel());
            return "Metal oscuro (FlatLaf no disponible)";
        } catch (Exception e) {
            return "look and feel por defecto";
        }
    }

    private static void ajustesComunes() {
        poner("control", FONDO_PANEL);
        poner("Panel.background", FONDO_PANEL);
        poner("Viewport.background", FONDO_PANEL);
        poner("SplitPane.background", FONDO_PANEL);
        poner("SplitPaneDivider.draggingColor", ACENTO);

        poner("MenuBar.background", FONDO_PANEL);
        poner("MenuBar.foreground", TEXTO);
        poner("Menu.background", FONDO_PANEL);
        poner("Menu.foreground", TEXTO);
        poner("Menu.selectionBackground", SELECCION);
        poner("Menu.selectionForeground", Color.WHITE);
        poner("MenuItem.background", FONDO_PANEL);
        poner("MenuItem.foreground", TEXTO);
        poner("MenuItem.selectionBackground", SELECCION);
        poner("MenuItem.selectionForeground", Color.WHITE);
        poner("MenuItem.acceleratorForeground", TEXTO_TENUE);
        poner("PopupMenu.background", FONDO_PANEL);
        poner("Separator.foreground", BORDE);
        poner("Separator.background", FONDO_PANEL);

        poner("Button.background", FONDO_CLARO);
        poner("Button.foreground", TEXTO);
        poner("Button.select", SELECCION);
        UIManager.put("Button.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));

        poner("ToolBar.background", FONDO_PANEL);
        poner("ToolBar.foreground", TEXTO);
        UIManager.put("ToolBar.border", BorderFactory.createEmptyBorder());

        poner("TabbedPane.background", FONDO_PANEL);
        poner("TabbedPane.foreground", TEXTO);
        poner("TabbedPane.contentAreaColor", FONDO_PANEL);
        poner("TabbedPane.selected", FONDO_CLARO);
        poner("TabbedPane.selectHighlight", BORDE);
        poner("TabbedPane.highlight", FONDO_CLARO);
        poner("TabbedPane.light", BORDE);
        poner("TabbedPane.shadow", BORDE);
        poner("TabbedPane.darkShadow", BORDE);
        poner("TabbedPane.borderHighlightColor", BORDE);
        poner("TabbedPane.focus", ACENTO);

        poner("Table.background", FONDO_PANEL);
        poner("Table.foreground", TEXTO);
        poner("Table.gridColor", BORDE);
        poner("Table.selectionBackground", SELECCION);
        poner("Table.selectionForeground", Color.WHITE);
        poner("TableHeader.background", FONDO_CLARO);
        poner("TableHeader.foreground", TEXTO);

        poner("ScrollPane.background", FONDO_PANEL);
        poner("ScrollBar.background", FONDO_PANEL);
        poner("ScrollBar.track", FONDO_PANEL);
        poner("ScrollBar.thumb", new Color(0x4A4A4F));
        poner("ScrollBar.thumbHighlight", new Color(0x5A5A5F));
        poner("ScrollBar.thumbShadow", BORDE);
        poner("ScrollBar.thumbDarkShadow", BORDE);
        poner("ScrollBar.shadow", FONDO_PANEL);
        poner("ScrollBar.darkShadow", FONDO_PANEL);
        poner("ScrollBar.highlight", FONDO_PANEL);

        poner("Label.foreground", TEXTO);
        poner("TextField.background", FONDO);
        poner("TextField.foreground", TEXTO);
        poner("TextField.caretForeground", TEXTO);
        poner("TextArea.background", FONDO_PANEL);
        poner("TextArea.foreground", TEXTO);

        poner("ToolTip.background", FONDO_CLARO);
        poner("ToolTip.foreground", TEXTO);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(BORDE));

        poner("OptionPane.background", FONDO_PANEL);
        poner("OptionPane.messageForeground", TEXTO);
        poner("ComboBox.background", FONDO_CLARO);
        poner("ComboBox.foreground", TEXTO);

        // El arbol de trabajo y los dialogos de archivo tienen piezas propias.
        poner("Tree.background", FONDO_PANEL);
        poner("Tree.foreground", TEXTO);
        poner("Tree.textBackground", FONDO_PANEL);
        poner("Tree.textForeground", TEXTO);
        poner("Tree.selectionBackground", SELECCION);
        poner("Tree.selectionForeground", Color.WHITE);
        poner("Tree.selectionBorderColor", BORDE);
        poner("Tree.hash", BORDE);
        poner("List.background", FONDO_PANEL);
        poner("List.foreground", TEXTO);
        poner("List.selectionBackground", SELECCION);
        poner("List.selectionForeground", Color.WHITE);
        poner("FileChooser.background", FONDO_PANEL);
        poner("FileChooser.foreground", TEXTO);
    }

    /** Textos de los dialogos de Swing, fijos para no depender del sistema. */
    private static void textosEnEspaniol() {
        UIManager.put("OptionPane.yesButtonText", "Si");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        UIManager.put("OptionPane.okButtonText", "Aceptar");

        UIManager.put("FileChooser.openDialogTitleText", "Abrir");
        UIManager.put("FileChooser.saveDialogTitleText", "Guardar");
        UIManager.put("FileChooser.lookInLabelText", "Buscar en:");
        UIManager.put("FileChooser.saveInLabelText", "Guardar en:");
        UIManager.put("FileChooser.fileNameLabelText", "Nombre del archivo:");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Tipo de archivo:");
        UIManager.put("FileChooser.openButtonText", "Abrir");
        UIManager.put("FileChooser.saveButtonText", "Guardar");
        UIManager.put("FileChooser.cancelButtonText", "Cancelar");
        UIManager.put("FileChooser.upFolderToolTipText", "Subir un nivel");
        UIManager.put("FileChooser.homeFolderToolTipText", "Carpeta personal");
        UIManager.put("FileChooser.newFolderToolTipText", "Crear una carpeta nueva");
        UIManager.put("FileChooser.acceptAllFileFilterText", "Todos los archivos");
    }

    private static void poner(String clave, Color color) {
        UIManager.put(clave, new ColorUIResource(color));
    }

    /** Paleta oscura para Metal: esto es lo que elimina el gris de las orillas. */
    private static class TemaMetalOscuro extends DefaultMetalTheme {

        private static final ColorUIResource PRIMARIO_1   = new ColorUIResource(0x094771);
        private static final ColorUIResource PRIMARIO_2   = new ColorUIResource(0x0E5A8A);
        private static final ColorUIResource PRIMARIO_3   = new ColorUIResource(0x1177BB);
        private static final ColorUIResource SECUNDARIO_1 = new ColorUIResource(BORDE);
        private static final ColorUIResource SECUNDARIO_2 = new ColorUIResource(FONDO_CLARO);
        private static final ColorUIResource SECUNDARIO_3 = new ColorUIResource(FONDO_PANEL);
        private static final ColorUIResource TEXTO_UI     = new ColorUIResource(TEXTO);
        private static final ColorUIResource BLANCO       = new ColorUIResource(FONDO_PANEL);

        private static final FontUIResource FUENTE =
                new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 12);

        @Override public String getName() { return "Contacto3D Oscuro"; }

        @Override protected ColorUIResource getPrimary1()   { return PRIMARIO_1; }
        @Override protected ColorUIResource getPrimary2()   { return PRIMARIO_2; }
        @Override protected ColorUIResource getPrimary3()   { return PRIMARIO_3; }
        @Override protected ColorUIResource getSecondary1() { return SECUNDARIO_1; }
        @Override protected ColorUIResource getSecondary2() { return SECUNDARIO_2; }
        @Override protected ColorUIResource getSecondary3() { return SECUNDARIO_3; }
        @Override protected ColorUIResource getBlack()      { return TEXTO_UI; }
        @Override protected ColorUIResource getWhite()      { return BLANCO; }

        @Override public ColorUIResource getControlTextColor() { return TEXTO_UI; }
        @Override public ColorUIResource getSystemTextColor()  { return TEXTO_UI; }
        @Override public ColorUIResource getUserTextColor()    { return TEXTO_UI; }
        @Override public ColorUIResource getMenuForeground()   { return TEXTO_UI; }
        @Override public ColorUIResource getInactiveControlTextColor() {
            return new ColorUIResource(TEXTO_TENUE);
        }
        @Override public ColorUIResource getInactiveSystemTextColor() {
            return new ColorUIResource(TEXTO_TENUE);
        }

        @Override public FontUIResource getControlTextFont() { return FUENTE; }
        @Override public FontUIResource getMenuTextFont()    { return FUENTE; }
        @Override public FontUIResource getSystemTextFont()  { return FUENTE; }
        @Override public FontUIResource getUserTextFont()    { return FUENTE; }
        @Override public FontUIResource getWindowTitleFont() { return FUENTE; }
    }
}
