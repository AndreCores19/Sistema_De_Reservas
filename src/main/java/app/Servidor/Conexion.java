package app.Servidor;

import java.io.PrintWriter;

class Conexion {
    final PrintWriter salida;
    final String idFuncionario;

    Conexion(PrintWriter salida, String idFuncionario) {
        this.salida = salida;
        this.idFuncionario = idFuncionario;
    }
}
