package matriz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import Exceptions.EtiquetaInvalidaException;
import Exceptions.TablasNoConcatenablesException;
import lector.LectorCSV;
import lector.exceptions.ArchivoNoEncontradoException;
import lector.exceptions.CSVParserException;

import celda.Celda;
import celda.CeldaBoolean;
import celda.CeldaNum;
import celda.CeldaString;

import columna.Columna;
import columna.ColumnaBoolean;
import columna.ColumnaNum;
import columna.ColumnaString;

import etiqueta.Etiqueta;
import etiqueta.EtiquetaNum;
import etiqueta.EtiquetaString;

import fila.Fila;

public class Tabla {
    List<Columna> columnas;
    Map<Etiqueta, Integer> colLabels;
    Map<Etiqueta, Integer> rowLabels;
    boolean tieneEtiquetaCol = false;
    boolean tieneEtiquetaFila = false;

    public Tabla() {
        columnas = new ArrayList<>();
        colLabels = new LinkedHashMap<>();
        rowLabels = new LinkedHashMap<>();
    }

    public Tabla(int cantidadColumnas, String[] etiquetas) {
        this();
        if (cantidadColumnas != etiquetas.length)
            throw new IllegalArgumentException("La longitud de etiquetas no coincide.");
        setEtiquetasColumnas(etiquetas);
    }

    public Tabla(Tabla m) {
        this.copiarTabla(m);
    }

    // public Tabla copia(Tabla origen) {
    // Tabla nueva = new Tabla(this);
    // nueva.columnas = new ArrayList<>();
    // for (Columna columna : columnas) {
    // if (columna instanceof ColumnaNum) { // TODO: demas clases
    // ColumnaNum columnaNum = (ColumnaNum) columna;
    // Columna columnaNueva = new ColumnaNum(new ArrayList<CeldaNum>());
    // for (CeldaNum celda : columnaNum.getCeldas()) {
    // columnaNueva.agregarCelda(celda.copia());
    // }
    // }
    // }
    // }

    public Tabla(Object[][] matriz, boolean tieneEncabezadosColumnas, boolean tieneEncabezadosFilas) {
        this.tieneEtiquetaCol = tieneEncabezadosColumnas;
        this.tieneEtiquetaFila = tieneEncabezadosFilas;
        int cantidadColumnas = matriz[0].length;
        this.columnas = new ArrayList<>();
        colLabels = new LinkedHashMap<>();
        rowLabels = new LinkedHashMap<>();

        int inicioFila = tieneEncabezadosFilas ? 1 : 0;
        int inicioColumna = tieneEncabezadosColumnas ? 1 : 0;

        for (int i = inicioFila; i < cantidadColumnas; i++) {
            List<Celda> celdas = new ArrayList<>();
            for (int j = inicioColumna; j < matriz.length; j++) {
                Celda celda = crearCelda(matriz[j][i]);
                celdas.add(celda);
            }
            Columna columna = crearColumna(celdas);
            this.columnas.add(columna);
            if (tieneEncabezadosColumnas) {
                Etiqueta etiqueta = new EtiquetaString(matriz[0][i].toString());
                this.colLabels.put(etiqueta, i - inicioFila);
            } else {
                Etiqueta etiqueta = new EtiquetaNum(i - inicioFila);
                this.colLabels.put(etiqueta, i - inicioFila);
            }
        }

        for (int i = inicioColumna; i < matriz.length; i++) {
            Etiqueta etiqueta;
            if (tieneEncabezadosFilas) {
                etiqueta = new EtiquetaString(matriz[i][0].toString());
            } else {
                etiqueta = new EtiquetaNum(i - inicioColumna);
            }
            this.rowLabels.put(etiqueta, i - inicioColumna);
        }
    }

    public Tabla(int[][] matriz, boolean tieneEncabezadosColumnas, boolean tieneEncabezadosFilas) {
        this(convertirMatrizANumber(matriz), tieneEncabezadosColumnas, tieneEncabezadosFilas);
    }

    public Tabla(float[][] matriz, boolean tieneEncabezadosColumnas, boolean tieneEncabezadosFilas) {
        this(convertirMatrizANumber(matriz), tieneEncabezadosColumnas, tieneEncabezadosFilas);
    }

    public Tabla(double[][] matriz, boolean tieneEncabezadosColumnas, boolean tieneEncabezadosFilas) {
        this(convertirMatrizANumber(matriz), tieneEncabezadosColumnas, tieneEncabezadosFilas);
    }

    public Tabla(String rutaArchivo, boolean tieneEncabezadosColumnas, boolean tieneEncabezadosFilas) {
        this.tieneEtiquetaCol = tieneEncabezadosColumnas;
        this.tieneEtiquetaFila = tieneEncabezadosFilas;
        LectorCSV lector = new LectorCSV();
        this.colLabels = new LinkedHashMap<>();
        this.rowLabels = new LinkedHashMap<>();
        try {
            List<String> lineas = lector.leer(rutaArchivo);
            List<Columna> cols = lector.parserColumnas(lineas, tieneEncabezadosColumnas);
            this.columnas = cols;
            if (tieneEncabezadosColumnas) {
                setEtiquetasColumnas(lector.getEncabezados());
            } else {
                List<Etiqueta> etiquetaCol = new ArrayList<>();
                for (int i = 0; i < cols.size(); i++) {
                    Etiqueta etiqueta = new EtiquetaNum(i);
                    etiquetaCol.add(etiqueta);
                }
                setEtiquetasColumnas(etiquetaCol);
            }
            List<Etiqueta> etiquetasFilas = new ArrayList<>();
            for (int i = 0; i < this.columnas.get(0).size(); i++) {
                if (tieneEncabezadosFilas) {
                    EtiquetaString etiqueta = new EtiquetaString(
                            this.columnas.get(0).obtenerValor(i).getValor().toString());
                    etiquetasFilas.add(etiqueta);
                    this.columnas.remove(0);
                } else {
                    EtiquetaNum etiqueta = new EtiquetaNum(i);
                    etiquetasFilas.add(etiqueta);
                }
            }
            setEtiquetasFilas(etiquetasFilas);
        } catch (ArchivoNoEncontradoException | CSVParserException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    // TODO: si tiene encabezado no deberia tomar una columna sin etiqueta y
    // viceversa
    public void agregarColumnaString(List<String> columna) {
        List<CeldaString> celdas = new ArrayList<>();
        for (String string : columna) {
            CeldaString celda = new CeldaString(string);
            celdas.add(celda);
        }
        ColumnaString col = new ColumnaString(celdas);
        this.colLabels.put(new EtiquetaNum(this.columnas.size()), this.columnas.size());
        this.columnas.add(col);
    }

    public void agregarColumnaString(List<String> columna, String encabezado) {
        List<CeldaString> celdas = new ArrayList<>();
        for (String string : columna) {
            CeldaString celda = new CeldaString(string);
            celdas.add(celda);
        }
        ColumnaString col = new ColumnaString(celdas);
        this.colLabels.put(new EtiquetaString(encabezado), this.columnas.size());
        this.columnas.add(col);
    }

    public void agregarColumnaNum(List<Number> columna) {
        List<CeldaNum> celdas = new ArrayList<>();
        for (Number num : columna) {
            CeldaNum celda = new CeldaNum(num);
            celdas.add(celda);
        }
        ColumnaNum col = new ColumnaNum(celdas);
        this.colLabels.put(new EtiquetaNum(this.columnas.size()), this.columnas.size());
        this.columnas.add(col);
    }

    public void agregarColumnaNum(List<Number> columna, String encabezado) {
        List<CeldaNum> celdas = new ArrayList<>();
        for (Number num : columna) {
            CeldaNum celda = new CeldaNum(num);
            celdas.add(celda);
        }
        ColumnaNum col = new ColumnaNum(celdas);
        this.colLabels.put(new EtiquetaString(encabezado), this.columnas.size());
        this.columnas.add(col);
    }

    public void agregarColumnaBoolean(List<Boolean> columna) {
        List<CeldaBoolean> celdas = new ArrayList<>();
        for (Boolean bool : columna) {
            CeldaBoolean celda = new CeldaBoolean(bool);
            celdas.add(celda);
        }
        ColumnaBoolean col = new ColumnaBoolean(celdas);
        this.colLabels.put(new EtiquetaNum(this.columnas.size()), this.columnas.size());
        this.columnas.add(col);
    }

    public void agregarColumnaBoolean(List<Boolean> columna, String encabezado) {
        List<CeldaBoolean> celdas = new ArrayList<>();
        for (Boolean bool : columna) {
            CeldaBoolean celda = new CeldaBoolean(bool);
            celdas.add(celda);
        }
        ColumnaBoolean col = new ColumnaBoolean(celdas);
        this.colLabels.put(new EtiquetaString(encabezado), this.columnas.size());
        this.columnas.add(col);
    }

    private List<Etiqueta> convertirAEtiqueta(String[] nombres) {
        List<Etiqueta> salida = new ArrayList<>();
        for (int i = 0; i < nombres.length; i++) {
            Etiqueta etiqueta = Etiqueta.crear(nombres[i]);
            salida.add(etiqueta);
        }
        return salida;
    }

    private Etiqueta convertirAEtiqueta(String nombre) {
        Etiqueta etiqueta = Etiqueta.crear(nombre);
        return etiqueta;
    }

    // TODO: validar etiqueta para armar las exceptions

    private List<Etiqueta> convertirAEtiqueta(int[] nombres) {
        List<Etiqueta> salida = new ArrayList<>();
        for (int i = 0; i < nombres.length; i++) {
            Etiqueta etiqueta = Etiqueta.crear(nombres[i]);
            salida.add(etiqueta);
        }
        return salida;
    }

    private Etiqueta convertirAEtiqueta(int nombre) {
        Etiqueta etiqueta = Etiqueta.crear(nombre);
        return etiqueta;
    }

    private void setEtiquetasFilas(List<Etiqueta> etiquetas) {
        rowLabels.clear();
        for (int i = 0; i < columnas.get(0).size(); i++) {
            rowLabels.put(etiquetas.get(i), i);
        }
        this.tieneEtiquetaFila = true;
    }

    public void setEtiquetasFilas(String[] etiquetas) {
        setEtiquetasFilas(convertirAEtiqueta(etiquetas));
    }

    public void setEtiquetasFilas(int[] etiquetas) {
        setEtiquetasFilas(convertirAEtiqueta(etiquetas));
    }

    private void setEtiquetasColumnas(List<Etiqueta> etiquetas) {
        colLabels.clear();
        // TODO: validar tamaño de lista
        for (int j = 0; j < columnas.size(); j++) {
            colLabels.put(etiquetas.get(j), j);
        }
        this.tieneEtiquetaCol = true;
    }

    public void setEtiquetasColumnas(String[] etiquetas) {
        setEtiquetasColumnas(convertirAEtiqueta(etiquetas));
    }

    public void setEtiquetasColumnas(int[] etiquetas) {
        setEtiquetasColumnas(convertirAEtiqueta(etiquetas));
    }

    public List<Etiqueta> obtenerEtiquetasColumnas() {
        List<Etiqueta> salida = new ArrayList<>();
        for (Etiqueta etiqueta : colLabels.keySet()) {
            salida.add(etiqueta);
        }
        return salida;
    }

    public List<Etiqueta> obtenerEtiquetasFilas() {
        List<Etiqueta> salida = new ArrayList<>();
        for (Etiqueta etiqueta : rowLabels.keySet()) {
            salida.add(etiqueta);
        }
        return salida;
    }

    public List<Object> obtenerNombreEtiquetasColumnas() {
        List<Object> salida = new ArrayList<>();
        for (Etiqueta etiqueta : colLabels.keySet()) {
            salida.add(etiqueta.getNombre());
        }
        return salida;
    }

    public List<Object> obtenerNombreEtiquetasFilas() {
        List<Object> salida = new ArrayList<>();
        for (Etiqueta etiqueta : rowLabels.keySet()) {
            salida.add(etiqueta.getNombre());
        }
        return salida;
    }

    public Celda obtenerCelda(Etiqueta etiquetaFila, Etiqueta etiquetaColumna) throws EtiquetaInvalidaException {
        if (!rowLabels.containsKey(etiquetaFila)) {
            throw new EtiquetaInvalidaException();
        }
        if (!colLabels.containsKey(etiquetaColumna)) {
            throw new EtiquetaInvalidaException();
        }
        return columnas.get(colLabels.get(etiquetaColumna)).obtenerValor(rowLabels.get(etiquetaFila));
    }

    public Celda obtenerCelda(String etiquetaFila, String etiquetaColumna) throws EtiquetaInvalidaException {
        return obtenerCelda(convertirAEtiqueta(etiquetaFila), convertirAEtiqueta(etiquetaColumna));
    }

    public Celda obtenerCelda(int etiquetaFila, int etiquetaColumna) throws EtiquetaInvalidaException {
        return obtenerCelda(convertirAEtiqueta(etiquetaFila), convertirAEtiqueta(etiquetaColumna));
    }

    public Celda obtenerCelda(String etiquetaFila, int etiquetaColumna) throws EtiquetaInvalidaException {
        return obtenerCelda(convertirAEtiqueta(etiquetaFila), convertirAEtiqueta(etiquetaColumna));
    }

    public Celda obtenerCelda(int etiquetaFila, String etiquetaColumna) throws EtiquetaInvalidaException {
        return obtenerCelda(convertirAEtiqueta(etiquetaFila), convertirAEtiqueta(etiquetaColumna));
    }

    private void cambiarValor(Etiqueta etiquetaFila, Etiqueta etiquetaColumna, Object valorNuevo) {
        try {
            Celda celdaBorrada = obtenerCelda(etiquetaFila, etiquetaColumna);
            Object valorAnterior = celdaBorrada.getValor();
            columnas.get(colLabels.get(etiquetaColumna)).fijarValor(rowLabels.get(etiquetaFila), valorNuevo);
            System.out.println(
                    "Se cambio el valor de la celda[" + etiquetaFila + ", " + etiquetaColumna + "]\n- Valor anterior: "
                            + valorAnterior + "\n- Valor nuevo: " + valorNuevo);
        } catch (EtiquetaInvalidaException e) {
            System.out.println(e.getMessage());
        }
    }

    public void cambiarValor(String etiquetaFila, String etiquetaColumna, Object valor)
            throws EtiquetaInvalidaException {
        cambiarValor(convertirAEtiqueta(etiquetaFila), convertirAEtiqueta(etiquetaColumna), valor);
    }

    public void cambiarValor(int etiquetaFila, int etiquetaColumna, Object valor) throws EtiquetaInvalidaException {
        cambiarValor(convertirAEtiqueta(etiquetaFila), convertirAEtiqueta(etiquetaColumna), valor);
    }

    public void cambiarValor(String etiquetaFila, int etiquetaColumna, Object valor) throws EtiquetaInvalidaException {
        cambiarValor(convertirAEtiqueta(etiquetaFila), convertirAEtiqueta(etiquetaColumna), valor);
    }

    public void cambiarValor(int etiquetaFila, String etiquetaColumna, Object valor) {
        cambiarValor(convertirAEtiqueta(etiquetaFila), convertirAEtiqueta(etiquetaColumna), valor);
    }

    public int obtenerCantidadFilas() {
        int cantidadFilas = columnas.get(0).size();
        return cantidadFilas;
    }

    public int obtenerCantidadColumnas() {
        int cantidadColumnas = columnas.size();
        return cantidadColumnas;
    }

    private Columna obtenerColumna(Etiqueta etiquetaColumna) {
        Columna columnaPedida = columnas.get(colLabels.get(etiquetaColumna));
        return columnaPedida;
    }

    public Columna obtenerColumna(String etiquetaColumnaNombre) {
        try {
            Etiqueta etiquetaColumna = getEtiquetaColumna(etiquetaColumnaNombre);
            return obtenerColumna(etiquetaColumna);
        } catch (EtiquetaInvalidaException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Columna obtenerColumna(Integer etiquetaColumnaNombre) {
        try {
            Etiqueta etiquetaColumna = getEtiquetaColumna(etiquetaColumnaNombre);
            return obtenerColumna(etiquetaColumna);
        } catch (EtiquetaInvalidaException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Tabla ordernarPorColumnas(String[] columnasOrden, String queOrden) {
        List<Etiqueta> orden = new ArrayList<>(rowLabels.keySet());
        int n = orden.size();
        boolean huboCambio;
        Etiqueta[] etiquetasColumnas = new Etiqueta[columnasOrden.length];
        try {
            for (int i = 0; i < columnasOrden.length; i++) {
                Etiqueta etiqueta = getEtiquetaColumna(columnasOrden[i]);
                etiquetasColumnas[i] = etiqueta;
            }
        } catch (EtiquetaInvalidaException e) {
            System.out.println(e.getMessage() + " La etiqueta debe estar en: " + colLabels.keySet().toString());
        }

        do {
            huboCambio = false;
            for (int i = 1; i < n; i++) {
                Etiqueta etiquetaPrevia = orden.get(i - 1);
                Etiqueta etiquetaActual = orden.get(i);

                // Obtener filas con seleccion de columnas a ordenar
                Fila filaPrevia = getFila(etiquetaPrevia, etiquetasColumnas);
                Fila filaActual = getFila(etiquetaActual, etiquetasColumnas);

                if (queOrden.equalsIgnoreCase("ascendente")) {
                    if (filaPrevia.compareTo(filaActual) > 0) {
                        orden.set(i - 1, orden.get(i));
                        orden.set(i, etiquetaPrevia);
                        huboCambio = true;
                    }
                } else if (queOrden.equalsIgnoreCase("descendente")) {
                    if (filaPrevia.compareTo(filaActual) < 0) {
                        orden.set(i - 1, orden.get(i));
                        orden.set(i, etiquetaPrevia);
                        huboCambio = true;
                    }
                } else {
                    throw new IllegalArgumentException("Indique un orden : 'ascendente' o 'descendente'");
                }
            }
            n--;
        } while (huboCambio);

        Tabla nuevaTabla = new Tabla(this);
        nuevaTabla.generarRowLabelsOrdenado(orden);
        return nuevaTabla;
    }

    public Fila getFila(Etiqueta etiquetaFila, Etiqueta[] etiquetasColumnas) {
        List<Celda> retorno = new ArrayList<>();
        if (!rowLabels.containsKey(etiquetaFila)) {
            throw new IllegalArgumentException();
        }
        for (Etiqueta etiqueta : etiquetasColumnas) {
            if (!colLabels.containsKey(etiqueta)) {
                throw new IllegalArgumentException();
            }
            try {
                retorno.add(obtenerCelda(etiquetaFila, etiqueta));
            } catch (EtiquetaInvalidaException e) {
                e.getMessage();
            }

        }
        return new Fila(retorno);
    }

    private void generarRowLabelsOrdenado(List<Etiqueta> orden) {// TODO validar que este en tabla

        for (Etiqueta etiqueta : orden) {
            Integer indice = rowLabels.get(etiqueta);
            rowLabels.remove(etiqueta);
            rowLabels.put(etiqueta, indice);
        }
    }

    public void ordenar(Etiqueta etiquetaColumna, String orden) {
        Columna columna = columnas.get(colLabels.get(etiquetaColumna));
        columna.ordenar(orden);
    }

    // TODO: parece que ningun metodo funciona por lo de abajo, :)
    // tener que pasarle una instancia de etiqueta es incomodo para trabajar, no es
    // mejor que reciba un string o un int? (en los demás métodos también)
    public void eliminarColumna(String etiquetaNombre) {
        try {
            Etiqueta etiqueta = getEtiquetaColumna(etiquetaNombre);
            Columna columna = obtenerColumna(etiquetaNombre);
            this.columnas.remove(columna);
            this.colLabels.remove(etiqueta);
        } catch (EtiquetaInvalidaException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    // en caso de que la etiqueta sea numerica
    public void eliminarColumna(Integer etiquetaNombre) {
        try {
            Etiqueta etiqueta = getEtiquetaColumna(etiquetaNombre);
            Columna columna = obtenerColumna(etiquetaNombre);
            this.columnas.remove(columna);
            this.colLabels.remove(etiqueta);
        } catch (EtiquetaInvalidaException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    // TODO: metodos que usen el indice de la etiqueta, eliminarColumnaPorIndice(int
    // indice), etc.

    private Etiqueta getEtiquetaColumna(String valor) throws EtiquetaInvalidaException {
        for (Etiqueta etiqueta : this.colLabels.keySet()) {
            if (etiqueta.getNombre().equals(valor)) {
                return etiqueta;
            }
        }
        throw new EtiquetaInvalidaException();
    }

    private Etiqueta getEtiquetaColumna(Integer valor) throws EtiquetaInvalidaException {
        for (Etiqueta etiqueta : this.colLabels.keySet()) {
            if (etiqueta.getNombre().equals(valor)) {
                return etiqueta;
            }
        }
        throw new EtiquetaInvalidaException();
    }

    public Etiqueta getEtiquetaFila(String valor) throws EtiquetaInvalidaException {
        for (Etiqueta etiqueta : this.rowLabels.keySet()) {
            if (etiqueta.getNombre().equals(valor)) {
                return etiqueta;
            }
        }
        throw new EtiquetaInvalidaException();
    }

    public Etiqueta getEtiquetaFila(Integer valor) throws EtiquetaInvalidaException {
        for (Etiqueta etiqueta : this.rowLabels.keySet()) {
            if (etiqueta.getNombre().equals(valor)) {
                return etiqueta;
            }
        }
        throw new EtiquetaInvalidaException();
    }

    private Celda crearCelda(Object valor) {
        Celda celda;
        if (valor instanceof Boolean) {
            celda = new CeldaBoolean((Boolean) valor);
        } else if (valor instanceof Number) {
            celda = new CeldaNum((Number) valor);
        } else if (valor instanceof String) {
            celda = new CeldaString((String) valor);
        } else {
            // TODO: excepcion propia
            throw new IllegalArgumentException("Tipo de datos no compatible en la matriz");
        }
        return celda;
    }

    private Columna crearColumna(List<Celda> celdas) {
        // Identificar el tipo de columna
        if (celdas.get(0) instanceof CeldaBoolean) {
            List<CeldaBoolean> booleanCeldas = new ArrayList<>();
            for (Celda celda : celdas) {
                booleanCeldas.add((CeldaBoolean) celda);
            }
            return new ColumnaBoolean(booleanCeldas);
        } else if (celdas.get(0) instanceof CeldaNum) {
            List<CeldaNum> numCeldas = new ArrayList<>();
            for (Celda celda : celdas) {
                numCeldas.add((CeldaNum) celda);
            }
            return new ColumnaNum(numCeldas);
        } else if (celdas.get(0) instanceof CeldaString) {
            List<CeldaString> stringCeldas = new ArrayList<>();
            for (Celda celda : celdas) {
                stringCeldas.add((CeldaString) celda);
            }
            return new ColumnaString(stringCeldas);
        } else {
            throw new IllegalArgumentException("Tipo de celda desconocido");
        }
    }

    public boolean tieneEncabezadosColumnas() {
        return tieneEtiquetaCol;
    }

    public boolean tieneEtiquetasFilas() {
        return tieneEtiquetaFila;
    }

    private static Number[][] convertirMatrizANumber(int[][] matriz) {
        int cantidadFilas = matriz.length;
        int cantidadColumnas = matriz[0].length;
        Number[][] matrizNumber = new Number[cantidadFilas][cantidadColumnas];
        for (int i = 0; i < cantidadFilas; i++) {
            for (int j = 0; j < cantidadColumnas; j++) {
                matrizNumber[i][j] = (Number) matriz[i][j];
            }
        }
        return matrizNumber;
    }

    private static Number[][] convertirMatrizANumber(float[][] matriz) {
        int cantidadFilas = matriz.length;
        int cantidadColumnas = matriz[0].length;
        Number[][] matrizNumber = new Number[cantidadFilas][cantidadColumnas];
        for (int i = 0; i < cantidadFilas; i++) {
            for (int j = 0; j < cantidadColumnas; j++) {
                matrizNumber[i][j] = (Number) matriz[i][j];
            }
        }
        return matrizNumber;
    }

    private static Number[][] convertirMatrizANumber(double[][] matriz) {
        int cantidadFilas = matriz.length;
        int cantidadColumnas = matriz[0].length;
        Number[][] matrizNumber = new Number[cantidadFilas][cantidadColumnas];
        for (int i = 0; i < cantidadFilas; i++) {
            for (int j = 0; j < cantidadColumnas; j++) {
                matrizNumber[i][j] = (Number) matriz[i][j];
            }
        }
        return matrizNumber;
    }

    public static Tabla copiarTabla(Tabla origen) {
        Tabla nueva = new Tabla();
        nueva.colLabels.putAll(origen.colLabels);
        nueva.rowLabels.putAll(origen.rowLabels);
        nueva.tieneEtiquetaCol = origen.tieneEtiquetaCol;
        nueva.tieneEtiquetaFila = origen.tieneEtiquetaFila;
        nueva.columnas = new ArrayList<>();
        for (Columna columna : origen.columnas) {
            if (columna instanceof ColumnaNum) {
                ColumnaNum columnaNum = (ColumnaNum) columna;
                ColumnaNum columnaNueva = new ColumnaNum(new ArrayList<CeldaNum>());
                for (CeldaNum celda : columnaNum.getCeldas()) {
                    columnaNueva.agregarCelda(celda.copia());
                }
                nueva.columnas.add(columnaNueva);
            } else if (columna instanceof ColumnaString) {
                ColumnaString columnaString = (ColumnaString) columna;
                ColumnaString columnaNueva = new ColumnaString(new ArrayList<CeldaString>());
                for (CeldaString celda : columnaString.getCeldas()) {
                    columnaNueva.agregarCelda(celda.copia());
                }
                nueva.columnas.add(columnaNueva);
            } else {
                ColumnaBoolean columnaBoolean = (ColumnaBoolean) columna;
                ColumnaBoolean columnaNueva = new ColumnaBoolean(new ArrayList<CeldaBoolean>());
                for (CeldaBoolean celda : columnaBoolean.getCeldas()) {
                    columnaNueva.agregarCelda(celda.copia());
                }
                nueva.columnas.add(columnaNueva);
            }
        }
        return nueva;
    }

    public <T extends Celda> Tabla concatenar(Tabla otraTabla)
            throws TablasNoConcatenablesException {
        if (mismasColumnas(this, otraTabla)) {
            Tabla nuevaTabla = copiarTabla(this);
            for (Etiqueta etiqueta : nuevaTabla.obtenerEtiquetasColumnas()) {
                Columna<T> columna = nuevaTabla.obtenerColumna(etiqueta);
                Columna<T> columnaOtraTabla = otraTabla.obtenerColumna(etiqueta);
                for (T celda : columnaOtraTabla.getCeldas()) {
                    columna.agregarCelda(celda);
                }
            }
            return nuevaTabla;
        } else {
            throw new TablasNoConcatenablesException();
        }
    }

    private static boolean mismasColumnas(Tabla tabla1, Tabla tabla2) {
        List<String> columnas1 = tabla1.obtenerNombreEtiquetasColumnas().stream()
                .map(x -> x.toString()).collect(Collectors.toList());
        List<String> columnas2 = tabla2.obtenerNombreEtiquetasColumnas().stream()
                .map(x -> x.toString()).collect(Collectors.toList());

        Collections.sort(columnas1);
        Collections.sort(columnas2);

        return columnas1.equals(columnas2);
    }

    public Tabla filtrar(Etiqueta col, char operador, Celda valor) { // TODO: String col, Object valor
        Map<Character, Predicate<Celda>> operadores = new HashMap<>();
        operadores.put('<', e -> e.compareTo(valor) < 0);
        operadores.put('>', e -> e.compareTo(valor) > 0);
        operadores.put('=', e -> e.compareTo(valor) == 0);
        operadores.put('!', e -> e.compareTo(valor) != 0);
        // operadores.put('<=', e -> e.compareTo(valor) <= 0);
        // operadores.put('>=', e -> e.compareTo(valor) >= 0);

        Predicate<Celda> condicion = operadores.get(operador);
        List<Etiqueta> salida = new ArrayList<>();

        if (condicion != null) {
            for (Etiqueta rowLabel : rowLabels.keySet()) {
                try {
                    Celda valorAComparar = obtenerCelda(rowLabel, col);
                    if (condicion.test(valorAComparar)) {
                        salida.add(rowLabel);
                    }
                } catch (EtiquetaInvalidaException e) {
                    e.getMessage();
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
        Tabla nueva = new Tabla(this);
        // nueva.generarRowLabelsOrdenado(salida);
        List<Etiqueta> auxiliar = new ArrayList<>();
        for (Etiqueta fila : nueva.rowLabels.keySet()) {
            if (!(salida.contains(fila))) {
                auxiliar.add(fila);
            }
        }
        for (Etiqueta etiqueta : auxiliar) {
            nueva.rowLabels.remove(etiqueta);
        }
        return nueva;

    }

    public void mostrarTabla() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        int anchoEtiquetaFila = obtenerAnchoEtiquetasFilas();
        String sep = " | ";
        int anchoTotal = 0;

        if (tieneEtiquetaFila) {
            out.append(" ".repeat(anchoEtiquetaFila)).append(sep);
        }

        if (tieneEtiquetaCol) {
            for (Etiqueta encabezado : colLabels.keySet()) {
                int anchoColumna = obtenerAnchoColumna(encabezado);
                anchoTotal += anchoColumna + sep.length();
                out.append(String.format("%-" + anchoColumna + "s", encabezado)).append(sep);
            }
        }
        out.append("\n");
        out.append("-".repeat(anchoTotal + anchoEtiquetaFila));
        out.append("\n");

        for (Etiqueta fila : rowLabels.keySet()) {
            if (tieneEtiquetaFila) {
                out.append(String.format("%-" + anchoEtiquetaFila + "s", fila)).append(sep);
            }
            for (Etiqueta columna : colLabels.keySet()) {
                try {
                    Celda valorCelda = obtenerCelda(fila, columna);
                    int anchoColumna = obtenerAnchoColumna(columna);
                    out.append(String.format("%-" + anchoColumna + "s", valorCelda)).append(sep);
                } catch (EtiquetaInvalidaException e) {
                    e.getMessage();
                }
            }
            out.append("\n");
        }

        return out.toString();
    }

    private int obtenerAnchoColumna(Etiqueta etiquetaColumna) {
        int anchoMaximo = etiquetaColumna.toString().length();

        for (Etiqueta etiquetaFila : rowLabels.keySet()) {
            try {
                Celda valorCelda = obtenerCelda(etiquetaFila, etiquetaColumna);
                int longitudValor = valorCelda.toString().length();
                if (longitudValor > anchoMaximo) {
                    anchoMaximo = longitudValor;
                }
            } catch (EtiquetaInvalidaException e) {
                e.printStackTrace();
            }
        }

        return anchoMaximo;
    }

    private int obtenerAnchoEtiquetasFilas() {
        int anchoMaximo = 0;

        for (Etiqueta etiquetaFila : rowLabels.keySet()) {
            int longitudEtiqueta = etiquetaFila.toString().length();
            if (longitudEtiqueta > anchoMaximo) {
                anchoMaximo = longitudEtiqueta;
            }
        }

        return anchoMaximo;
    }

    public static void main(String[] args) {
        String[][] matriz = new String[3][3];

        matriz[0][0] = "Nombre";
        matriz[0][1] = "Apellido";
        matriz[0][2] = "Edad";
        matriz[1][0] = "Martín";
        matriz[1][1] = "Gutierrez";
        matriz[1][2] = "23";
        matriz[2][0] = "Javier";
        matriz[2][1] = "Moreno";
        matriz[2][2] = "34";

        // matriz[0][0] = 0;
        // matriz[0][1] = 1;
        // matriz[0][2] = 2;
        // matriz[1][0] = 3;
        // matriz[1][1] = 4;
        // matriz[1][2] = 5;
        // matriz[2][0] = 6;
        // matriz[2][1] = 7;
        // matriz[2][2] = 8;

        Tabla tabla = new Tabla(matriz, true, false);
        Tabla tabla2 = copiarTabla(tabla);
        // List<Number> nuevaCol = new ArrayList<>();
        // nuevaCol.add(10);
        // nuevaCol.add(7);
        // tabla.agregarColumnaNum(nuevaCol, "Nota");
        // String[] etiquetas = { "Hola", "Mundo", "JAVA" };
        // tabla.setEtiquetasColumnas(etiquetas);
        tabla.cambiarValor(0, "Nombre", "juean");

        System.out.println(tabla);
        System.out.println(tabla2);
        Tabla tabla3;
        try {
            tabla3 = tabla.concatenar(tabla2);
            System.out.println(tabla3);
        } catch (TablasNoConcatenablesException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // System.out.println(tabla.obtenerEtiquetasColumnas());
        // System.out.println(tabla.obtenerEtiquetasFilas());
        // tabla.mostrarTabla();
        // System.out.println(tabla.toString());
        // System.out.println(tabla.obtenerEtiquetasColumnas());
        // System.out.println(tabla.obtenerEtiquetasFilas());

        // System.out.println(tabla2.toString());

        // System.out.println("etiquetas de columna");
        // System.out.println(tabla.colLabels.keySet());
        // System.out.println(tabla.colLabels.values());

        // System.out.println("etiquetas de fila");
        // System.out.println(tabla.rowLabels.keySet());
        // System.out.println(tabla.rowLabels.values());

        // System.out.println("Borro la columna edad");
        // tabla.eliminarColumna("Edad");
        // System.out.println(tabla.toString());

        // System.out.println("etiquetas de columna");
        // System.out.println(tabla.colLabels.keySet());
        // System.out.println(tabla.colLabels.values());

        // System.out.println("etiquetas de fila");
        // System.out.println(tabla.rowLabels.keySet());
        // System.out.println(tabla.rowLabels.values());

        // System.out.println("columna Apellido: " + tabla.obtenerColumna("Apellido"));

        // String[] etiquetas = { "Hola", "Mundo", "JAVA" };
        // String[] etiquetasFila = { "Alumno1", "Alumno2" };

        // tabla.setEtiquetasColumnas(etiquetas);
        // System.out.println(tabla);
        // tabla.setEtiquetasFilas(etiquetasFila);
        // System.out.println(tabla);

        // Etiqueta etiqueta = new EtiquetaString("Apellido");

        // CeldaString celda = new CeldaString("25");

        // Tabla tablaFiltrada = tabla.filtrar(etiqueta, '>', celda);

        // System.out.println(tablaFiltrada);

        Tabla pokemon = new Tabla("E:/java_workspace/TP_Final/Pokemon.csv", true, false);
        // // System.out.println(pokemon);
        Tabla pokemon2 = new Tabla("E:/java_workspace/TP_Final/Pokemon.csv", true, false);
        // System.out.println(pokemon.obtenerEtiquetasColumnas());
        // System.out.println(pokemon.obtenerEtiquetasFilas());
        // String[] etiqeutas = { "Attack", "HP" };
        // System.out.println(pokemon.ordernarPorColumnas(etiqeutas, "descendente"));

        // pokemon.cambiarValor(0, "Type 1", "pokemon");
        // System.out.println(pokemon);
        // pokemon.mostrarTabla();

        // try {
        // Tabla pokemon3 = pokemon.concatenar(pokemon2);
        // System.out.println(pokemon3);
        // } catch (TablasNoConcatenablesException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

    }
}
