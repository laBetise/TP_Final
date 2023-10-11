package columna;

import celda.Celda;

public abstract class Columna {

    public abstract void ordenar(String orden);

    public abstract void fijarValor(Integer indiceFila, Celda valor);

    public abstract Celda obtenerValor(Integer indiceFila); //no deberia devolver el valor de la celda?
    // podria ser obtenerCelda()
    public abstract String tipoDato();
    
}
