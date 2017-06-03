package ru.ifmo.ctddev.titova.hello;

/**
 * @author Sophia Titova.
 *         Utils for UDP Server and Client.
 */
class UDPUtils {
    /**
     * Returns number presentation of given string argument
     * or print error and stops working program with <code>exit(-1)</code>.
     *
     * @param arg  String view of arg.
     * @param name Name of argument.
     * @return Number presentation of string arg or prints name of invalid argument.
     */
    public static int getArg(String arg, String name) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            System.err.println("Invalid " + name);
            System.exit(-1);
        }
        return 0;
    }
}
