public class Materia {

    private int clave;
    private String nombre;
    private double duracion;


    public Materia(int clave, String nombre, double duracion) {
        this.clave = clave;
        this.nombre = nombre;
        this.duracion = duracion;
    }


    public int getClave() {
        return clave;
    }


    public void setClave(int clave) {
        this.clave = clave;
    }


    public String getNombre() {
        return nombre;
    }


    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public double getDuracion() {
        return duracion;
    }


    public void setDuracion(double duracion) {
        this.duracion = duracion;
    }

}
