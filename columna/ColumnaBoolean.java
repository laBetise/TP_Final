package columna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import celda.Celda;
import celda.CeldaBoolean;
import etiqueta.Etiqueta;
import etiqueta.EtiquetaString;

public class ColumnaBoolean extends Columna {
    private Etiqueta etiqueta;
    private List<CeldaBoolean> celdas;

    public ColumnaBoolean(Etiqueta etiqueta, List<CeldaBoolean> celdas) {
        this.etiqueta = etiqueta;
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
            celdas.set(indiceFIla, (CeldaBoolean) valor);
        } else {
            throw new IllegalArgumentException("Índice de fila fuera de rango");
        }
    }

    @Override
    public String tipoDato() {
        return "Booleana".toString();
    }

    public Etiqueta getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(Etiqueta etiqueta) {
        this.etiqueta = etiqueta;
    }

    public List<CeldaBoolean> getCeldas() {
        return celdas;
    }

    public void setCeldas(List<CeldaBoolean> celdas) {
        this.celdas = celdas;
    }

    //para probar cosas:
    public String toString() {
        String inicial = "";
        for (CeldaBoolean celda : celdas) {
            inicial += celda.getValor();
            inicial += ", ";
        }
        return inicial;
    }

    public static void main(String[] args) {
        CeldaBoolean celda1 = new CeldaBoolean(true);
        CeldaBoolean celda2 = new CeldaBoolean(false);
        CeldaBoolean celda3 = new CeldaBoolean(1);
        CeldaBoolean celda4 = new CeldaBoolean(0);

        List<CeldaBoolean> listaCeldas = new ArrayList<>();
        listaCeldas.add(celda1);
        listaCeldas.add(celda2);
        listaCeldas.add(celda3);
        listaCeldas.add(celda4);

        EtiquetaString etiqueta = new EtiquetaString("columna");

        ColumnaBoolean col = new ColumnaBoolean(etiqueta, listaCeldas);

        System.out.println("original: " + col);

        col.ordenar("descendente");

        System.out.println("desc: "+ col);

        col.ordenar(null);

        System.out.println("asc: " + col);

    }
}
