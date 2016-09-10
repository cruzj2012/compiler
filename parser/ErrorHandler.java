package parser;

public class ErrorHandler {

    private String filename;
    private int errorCounter = 0;

    private void increaseCounter() {
        errorCounter++;
    }

    public int getCounter() {
        return errorCounter;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void PrintFormatedError(ParseException pe) {
        increaseCounter();
        String eol = System.getProperty("line.separator", "");
        StringBuffer expected = new StringBuffer();
        int maxSize = 0;
        for (int i = 0; i < pe.expectedTokenSequences.length; i++) {
            if (maxSize < pe.expectedTokenSequences[i].length) {
                maxSize = pe.expectedTokenSequences[i].length;
            }
            for (int j = 0; j < pe.expectedTokenSequences[i].length; j++) {
                expected.append(pe.tokenImage[pe.expectedTokenSequences[i][j]]).append("");
            }
            if (pe.expectedTokenSequences[i][pe.expectedTokenSequences[i].length - 1] != 0) {
                expected.append("");
            }
            expected.append(eol).append("");
        }
        System.err.print(filename + ":"
                + pe.currentToken.next.beginLine
                + "." + pe.currentToken.beginColumn
                + " Syntax Error: expecting a " + expected.toString()
                + " received a " + pe.currentToken.toString()
        );

    }

    /**
     * *
     * Print formatted semantic errors
     *
     * @param l error line number
     * @param c error column number
     */
    public void PrintSemanticError(int l, int c) {
        increaseCounter();
        System.err.print(filename + ":"
                + l + "."
                + c
                + " Semantic error!"
        );

    }

    /**
     * *
     *
     * @param l line number
     * @param c column number
     * @param s class name
     */
    public void classAlreadyDeclared(int l, int c, String s) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + "."
                + c + ":"
                + " Error: Class \"" + s + "\" already declared."
        );

    }

    /**
     * *
     *
     * @param l line number
     * @param c column number
     * @param cl class name
     * @param v variable name
     */
    public void varAlreadyDeclared(int l, int c, String cl, String v) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + "."
                + c + ":"
                + " Error: Variable \"" + v + "\" "
                + "already declared in class \"" + cl + "\"."
        );

    }

    /**
     * *
     * When a method is already declared in a class
     *
     * @param l line number
     * @param c column number
     * @param cl class name
     * @param mt method name
     */
    public void methodAlreadyDeclared(int l, int c, String cl, String mt) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + "."
                + c + ":"
                + " Error: Method \"" + mt + "\" "
                + "already declared in class \"" + cl + "\"."
        );
    }

    /**
     * *
     *
     * @param l line number
     * @param c column number
     * @param mt method name
     * @param v parameter name
     */
    public void parMethodAlreadyDecl(int l, int c, String mt, String v) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + "."
                + c + ":"
                + " Error: Parameter \"" + v + "\" "
                + "already declared in the scope of method \"" + mt + "\"."
        );
    }

    /**
     * *
     *
     * @param l line number
     * @param c column number
     * @param mt method name
     * @param v variable name
     */
    public void varMethodAlreadyDecl(int l, int c, String mt, String v) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + "."
                + c + ":"
                + " Error: Variable \"" + v + "\" "
                + "already declared in the scope of method \"" + mt + "\"."
        );
    }

    public void wrongType(int l, int c, String var, String type) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + ":"
                + c + ":"
                + " Error: Invalid operation. Variable \"" + var + "\" not of " + type + "+ type"
        );

    }

    public void wrongType(int l, int c, String type) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + ":"
                + c + ":"
                + " Error: Invalid operation. Both sides must be of " + type + " type"
        );

    }

    public void wrongArrayPosition(int l, int c) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + ":"
                + c + ":"
                + " Error: Array positions must be of int type."
        );

    }

    public void wrongAssign(int l, int c, String type) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + ":"
                + c + ":"
                + " Error: Invalid asignment operation. Type " + type + " is required."
        );

    }

    public void notDeclared(int l, int c, String var) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + ":"
                + c + ":"
                + " Error: Variable \"" + var + "\" not declared."
        );

    }

    public void notDeclaredMeth(int l, int c, String var) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + ":"
                + c + ":"
                + " Error: Method \"" + var + "\" not declared."
        );
    }

    public void notDeclaredClass(int l, int c, String var) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + ":"
                + c + ":"
                + " Error: Class \"" + var + "\" not found."
        );

    }


    public void methodNotFoundInClass(int l, int c, String meth, String classe) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + ":"
                + c + ":"
                + " Error: Method \"" + meth + "\" not found in class \"" + classe + "\"."
        );

    }

    public void incorrectNumberofArgs(int l, int c, String meth, int requiredArgs, int foundArgs) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + ":"
                + c + ":"
                + " Error: Method \"" + meth + "\" requires "
                + requiredArgs + " argument(s). Passing "
                +foundArgs+" arguments in the call."
        );

    }

    public void incorrectTypeofArgs(int l, int c, String meth, String requiredArgs, String foundArgs) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + ":"
                + c + ":"
                + " Error: Method \"" + meth + "\" requires \""
                + requiredArgs + "\". Found \""
                +foundArgs+"\" in the call."
        );
    }

    public void invalidExpression(int l, int c) {
        increaseCounter();
        System.err.println(filename + ":"
                + l + ":"
                + c + ":"
                + " Error: Not a valid object."
        );
    }
}
