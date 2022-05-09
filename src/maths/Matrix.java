package maths;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Class representing two-dimensional matrices of real values
 */
public class Matrix {
    protected final double[] values;

    private final int rows;
    private final int columns;

    private Double determinant = null;
    private final boolean isSquare;

    public Matrix(List<Double> values, int rows, int columns) {
        this(values.stream().mapToDouble(n -> n).toArray(), rows, columns);
    }

    public Matrix(double[] values, int rows, int columns) {
        if (values.length != rows * columns) {
            throw new IllegalArgumentException("maths.Matrix dimensions do not correspond to the values given");
        } else if (rows == 1 || columns == 1) {
            throw new IllegalArgumentException("Use maths.Vector3 class to create a vector");
        }

        this.values = values;
        this.rows = rows;
        this.columns = columns;
        isSquare = this.rows == this.columns;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("\n");

        for (int i = 0; i < rows; i++) {
            StringBuilder innerValues = new StringBuilder();
            for (int j = 0; j < columns; j++) {
                innerValues.append(values[j + i * columns]).append(" ");
            }
            innerValues.replace(innerValues.length() - 1, innerValues.length(), "");
            output.append(innerValues).append("\n");
        }

        output.replace(output.length() - 1, output.length(), "\n");
        return output.toString();
    }

    public double get(int row, int column) {
        return values[row * columns + column];
    }

    public Matrix add(Matrix matrix) {
        if (!Arrays.equals(this.getDimensions(), matrix.getDimensions())) {
            throw new IllegalArgumentException("Dimensions of matrices do not match");
        }

        double[] addedValues = Arrays.copyOf(this.values, values.length);
        for (int i = 0; i < addedValues.length; i++) {
            addedValues[i] = addedValues[i] + matrix.values[i];
        }

        return new Matrix(addedValues, rows, columns);
    }

    public int[] getDimensions() {
        return new int[] {rows, columns};
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public Matrix multiply(double scalar) {
        List<Double> multiplied = Arrays.stream(this.values).boxed().collect(Collectors.toList());
        multiplied.replaceAll(integer -> integer * scalar);
        return new Matrix(multiplied, rows, columns);
    }

    public Matrix multiply(Matrix matrix) {
        if (columns != matrix.rows) {
            throw new IllegalArgumentException("Matrices cannot be multiplied: this.columns != matrix.rows");
        }

        double[] newValues = new double[this.rows * matrix.columns];

        for (int i = 0; i < newValues.length; i++) {
            int rowNumber = i / matrix.columns;
            int columnNumber = i % matrix.columns;

            double value = 0;
            for (int j = 0; j < columns; j++) {
                double partialRowValue = values[rowNumber * columns + j];
                double partialColumnValue = matrix.values[j * matrix.columns + columnNumber];
                value += partialRowValue * partialColumnValue;
            }

            newValues[i] = value;
        }

        return new Matrix(newValues, this.rows, matrix.columns);
    }

    public Matrix transpose() {
        double[] newValues = Arrays.copyOf(values, values.length);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                newValues[j * rows + i] = values[i * columns + j];
            }
        }

        return new Matrix(newValues, columns, rows);
    }

    private double calculateDeterminant() {
        if (!isSquare) {
            throw new ArithmeticException("maths.Matrix should be square to compute determinant");
        }

        if (this.rows == 1 && this.columns == 1) {
            return this.get(0, 0);
        } else if (this.rows == 2 && this.columns == 2) {
            return this.get(0, 0) * this.get(1, 1) - this.get(0, 1) * this.get(1, 0);
        }


        double determinant = 0;
        for (int i = 0; i < this.columns; i++) {
            // Todo: make smarter (try to select rows/columns with zeros in the
            determinant += this.values[i] * getCofactor(0, i);
        }

        return determinant;
    }

    private double getCofactor(int row, int column) {
        return Math.pow(-1, row + column) * this.getMinor(row, column).calculateDeterminant();
    }

    private Matrix getMinor(int row, int column) {
        double[] minorValues = new double[(this.rows - 1) * (this.columns - 1)];

        int p = 0;
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                if (i != row && j != column) {
                    minorValues[p] = this.get(i, j);
                    p++;
                }
            }
        }

        return new Matrix(minorValues, this.rows - 1, this.columns - 1);
    }

    public double getDeterminant() {
        if (rows != columns) {
            throw new RuntimeException("Non square matrix does not have a determinant");
        }

        if (determinant == null) {
            determinant = this.calculateDeterminant();
        }

        return determinant;
    }

    public Matrix getInverse() {
        double determinant = getDeterminant();

        if (determinant == 0) {
            return null;
        }

        return getCofactorMatrix().transpose().multiply(1 / determinant);
    }

    private Matrix getCofactorMatrix() {
        double[] cofactors = new double[values.length];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                cofactors[i * columns + j] = getCofactor(i, j);
            }
        }

        return new Matrix(cofactors, rows, columns);
    }

    public static Matrix getXYRightHandedRotationMatrix(double degAngle) {
        double radAngle = Math.toRadians(degAngle);
        return new Matrix(new double[] {
                cos(radAngle), -sin(radAngle), 0,
                sin(radAngle), cos(radAngle), 0,
                0, 0, 1
        }, 3, 3);
    }

    public static Matrix getYZRightHandedRotationMatrixAlt(double degAngle) {
        double radAngle = Math.toRadians(degAngle);
        return new Matrix(new double[] {
                1, 0, 0,
                0, cos(radAngle), -sin(radAngle),
                0, sin(radAngle), cos(radAngle)
        }, 3, 3);
    }

    public static Matrix getXZRightHandedRotationMatrix(double degAngle) {
        double radAngle = Math.toRadians(degAngle);
        return new Matrix(new double[] {
                cos(radAngle), 0, -sin(radAngle),
                0, 1, 0,
                sin(radAngle), 0, cos(radAngle)
        }, 3, 3);
    }

    public static Matrix getXYLeftHandedRotationMatrix(double degAngle) {
        double radAngle = Math.toRadians(degAngle);
        return new Matrix(new double[] {
                cos(radAngle), sin(radAngle), 0,
                -sin(radAngle), cos(radAngle), 0,
                0, 0, 1
        }, 3, 3);
    }

    public static Matrix getYZLeftHandedRotationMatrix(double degAngle) {
        double radAngle = Math.toRadians(degAngle);
        return new Matrix(new double[] {
                1, 0, 0,
                0, cos(radAngle), sin(radAngle),
                0, -sin(radAngle), cos(radAngle)
        }, 3, 3);
    }

    public static Matrix getXZLeftHandedRotationMatrixAlt(double degAngle) {
        double radAngle = Math.toRadians(degAngle);
        return new Matrix(new double[] {
                cos(radAngle), 0, sin(radAngle),
                0, 1, 0,
                -sin(radAngle), 0, cos(radAngle)
        }, 3, 3);
    }
}