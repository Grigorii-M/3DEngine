import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class OBJParser {
    public ArrayList<Triangle> parse(File file) {
        if (!file.getName().matches(".*\\.obj")) {
            throw new IllegalArgumentException("File format " + file.getName().substring(file.getName().lastIndexOf('.')) + " is not supported");
        }

        ArrayList<Triangle> triangles = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] bytes = fileInputStream.readAllBytes();
            String[] lines = new String(bytes, StandardCharsets.UTF_8).split("\n");

            ArrayList<Vertex> vertices = new ArrayList<>();
            for (String line : lines) {
                if (line.matches("^v\\s+.*")) {
                    String[] terms = line.split("\\s+");
                    // Todo: remove this and add zoom
                    Vertex v = new Vertex(100 * Double.parseDouble(terms[1]), 100 * Double.parseDouble(terms[2]), 100 * Double.parseDouble(terms[3]));
                    vertices.add(v);
                }

                if (line.matches("^f\\s+.*")) {
                    String[] terms = line.split("\\s+");
                    int[] verticesIndexes = new int[3];

                    for (int i = 0; i < verticesIndexes.length; i++) {
                        verticesIndexes[i] = Integer.parseInt(terms[i + 1].split("/")[0]);
                    }
                    triangles.add(new Triangle(vertices.get(verticesIndexes[0] - 1), vertices.get(verticesIndexes[1] - 1), vertices.get(verticesIndexes[2] - 1), Color.WHITE));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return triangles;
    }
}
