package esteticaturnos.controller;

import java.util.List;

public class ValidacionException extends Exception {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }

    public ValidacionException(List<String> errores) {
        super(String.join("\n", errores));
    }
}