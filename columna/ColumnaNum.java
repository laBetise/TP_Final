package columna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import celda.Celda;
import celda.CeldaNum;

public class ColumnaNum extends Columna<CeldaNum> {
    private List<CeldaNum> celdas;

    public ColumnaNum(List<CeldaNum> celdas) {
        this.celdas = celdas;
    }

    @Override
    public void ordenar(String orden){
        Collections.sort(celdas);

        if ("descendente".equals(orden)) {
            Collections.reverse(celdas);
        }
    }

    @Override
    public Celda obtenerValor(Integer indiceFila){
        // Asegúrate de que el índice de fila sea válido
        if (indiceFila >= 0 && indiceFila < celdas.size()) {
            return celdas.get(indiceFila); //celdas.get(indiceFila).getValor()?
        } else {
            throw new IllegalArgumentException("Índice de fila fuera de rango");
        }
    }

    @Override
    public void fijarValor(Integer indiceFIla, Celda valor) {
        // Asegúrate de que el índice de fila sea válido
        if (indiceFIla >= 0 && indiceFIla < celdas.size()) {
            celdas.set(indiceFIla, (CeldaNum) valor);
        } else {
            throw new IllegalArgumentException("Índice de fila fuera de rango");
        }
    }

    @Override
    public String tipoDato() {
        return "Numerica".toString();
    }

    @Override
    public int size() {
        return celdas.size();
    }

    public List<CeldaNum> getCeldas() {
        return celdas;
    }

    public void setCeldas(List<CeldaNum> celdas) {
        this.celdas = celdas;
    }
    
    @Override
    public void agregarCelda(Celda celda) {
        //TODO: excepcion ClassCast
        this.celdas.add((CeldaNum) celda);
    }

    //para probar cosas:
    public String toString() {
        String inicial = "";
        for (CeldaNum celda : celdas) {
            inicial += celda;
            inicial += ", ";
        }
        return inicial;
    }

    public static void main(String[] args) {
        CeldaNum celda1 = new CeldaNum(3);
        CeldaNum celda2 = new CeldaNum(1);
        CeldaNum celda3 = new CeldaNum(null);
        CeldaNum celda4 = new CeldaNum(2);

        List<CeldaNum> listaCeldas = new ArrayList<>();
        listaCeldas.add(celda1);
        listaCeldas.add(celda2);
        listaCeldas.add(celda3);
        listaCeldas.add(celda4);

        ColumnaNum col = new ColumnaNum(listaCeldas);

        System.out.println("original: " + col);

        col.ordenar("descendente");

        System.out.println("desc: "+ col);

        col.ordenar(null);

        System.out.println("asc: " + col);

    }
}
