/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package entities;

/**
 *
 * @author belenyardebuller
 */
/**
 * Representa los grupos sanguíneos válidos. Mapea directamente al ENUM del
 * campo grupo_sanguineo de la BD
 */
public enum TipoSangre {
    A_POS("A+"),
    A_NEG("A-"),
    B_POS("B+"),
    B_NEG("B-"),
    AB_POS("AB+"),
    AB_NEG("AB-"),
    O_POS("O+"),
    O_NEG("O-");

    private final String valor;

    /**
     * Constructor
     *
     * @param valor
     */
    TipoSangre(String valor) {
        this.valor = valor;
    }

    /**
     * Obtiene el valor del enum TipoSangre
     *
     *
     * @return valor
     */
    public String getValor() {
        return valor;
    }

    /**
     * Conversión desde valor de BD a tipo enum
     *
     * @param dbValue
     * @return
     */
    public static TipoSangre fromDbValue(String dbValue) {
        for (TipoSangre t : values()) {
            if (t.valor.equalsIgnoreCase(dbValue)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Valor de grupo sanguíneo inválido: " + dbValue);
    }

    /**
     * Devuelve el valor tal como se guarda en la BD
     */
    @Override
    public String toString() {
        return valor;
    }

}
