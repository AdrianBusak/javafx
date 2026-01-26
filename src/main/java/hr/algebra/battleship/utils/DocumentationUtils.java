package hr.algebra.battleship.utils;

import hr.algebra.battleship.exception.ReflectionException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class DocumentationUtils {

    private DocumentationUtils() {}

    private static final String PATH_WITH_CLASSES = "target/classes/";
    private static final String HTML_DOCUMENTATION_FILE_NAME = "doc/documentation.html";
    private static final String CLASS_FILE_NAME_EXTENSION = ".class";

    public static void generateHtmlDocumentationFile() throws IOException {
        // ✅ STVORI DIREKTORIJ AKO NE POSTOJI
        Path docDir = Paths.get("doc");
        if (!Files.exists(docDir)) {
            Files.createDirectories(docDir);
            System.out.println("✅ Direktorij 'doc' je stvoren!");
        }

        Path start = Paths.get(PATH_WITH_CLASSES);
        try (Stream<Path> stream = Files.walk(start, Integer.MAX_VALUE)) {
            List<String> classList = stream
                    .filter(f -> f.getFileName().toString().endsWith(CLASS_FILE_NAME_EXTENSION)
                            && Character.isUpperCase(f.getFileName().toString().charAt(0)))
                    .map(String::valueOf)
                    .sorted()
                    .toList();

            String htmlString = generateHtmlDocumentationCode(classList);
            Files.writeString(Path.of(HTML_DOCUMENTATION_FILE_NAME), htmlString);
            System.out.println("✅ Dokumentacija je uspješno generiirana!");
        }
    }

    private static String generateHtmlDocumentationCode(List<String> classList) {
        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>⚓ Battleship Dokumentacija</title>
                    <style>
                        body { font-family: Arial, sans-serif; background: #f5f5f5; padding: 20px; }
                        .container { max-width: 900px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
                        h1 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 10px; }
                        h2 { color: #007bff; margin-top: 30px; }
                        h3 { color: #555; margin-top: 15px; background: #f9f9f9; padding: 8px; border-radius: 4px; }
                        .method { background: #e8f4f8; padding: 10px; margin: 8px 0; border-left: 3px solid #007bff; font-family: monospace; font-size: 13px; }
                        .constructor { background: #fff3cd; padding: 10px; margin: 8px 0; border-left: 3px solid #ffc107; font-family: monospace; font-size: 13px; }
                        .package { color: #28a745; font-weight: bold; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>⚓ BATTLESHIP - Dokumentacija</h1>
                        <p>Kompletan pregled svih klasa, konstruktora i metoda.</p>
                """);

        String currentPackage = "";
        for (String className : classList) {
            String fullClassName = className
                    .substring(PATH_WITH_CLASSES.length(), className.length() - CLASS_FILE_NAME_EXTENSION.length())
                    .replace("\\", ".");

            String packageName = fullClassName.substring(0, fullClassName.lastIndexOf('.'));

            if (!packageName.equals(currentPackage)) {
                currentPackage = packageName;
                html.append("<h2 class='package'>📦 ").append(packageName).append("</h2>\n");
            }

            try {
                Class<?> clazz = Class.forName(fullClassName);
                String simpleClassName = clazz.getSimpleName();

                html.append("<h3>").append(simpleClassName).append("</h3>\n");

                // Konstruktori
                if (clazz.getConstructors().length > 0) {
                    for (Constructor<?> constructor : clazz.getConstructors()) {
                        html.append("<div class='constructor'>🔧 ").append(constructor).append("</div>\n");
                    }
                } else {
                    html.append("<div class='constructor'>❌ Nema konstruktora</div>\n");
                }

                // Metode
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                        html.append("<div class='method'>⚙️ ").append(method).append("</div>\n");
                    }
                }

            } catch (ClassNotFoundException e) {
                throw new ReflectionException("Greška pri učitavanju klase: " + fullClassName, e);
            }
        }

        html.append("""
                        <hr style="margin-top: 40px;">
                        <p style="text-align: center; color: #666; font-size: 12px;">
                            Generirano automatski | ⚓ Battleship 2026
                        </p>
                    </div>
                </body>
                </html>
                """);

        return html.toString();
    }
}
