/*
 * Florida Tech, CSE4251: Compiler Design.  Part of the compiler project from
 * "Modern Compiler Implementation in Java," 2nd edition, by Andrew W. Appel.
 */
package parser;

import syntax.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;

//class CloneSimpleClassDecl extends SimpleClassDecl{
//
//    public CloneSimpleClassDecl(Identifier ai, List<VarDecl> avl, List<MethodDecl> aml) {
//        super(ai, avl, aml);
//    }
//}
//import java.io.PrintWriter;
public class SymbolTable implements SyntaxTreeVisitor<Void> {

    private int tab = 0;
    private String currentClass = "";
    public ArrayList<HashMap> methods = new ArrayList<>();
    public ArrayList<HashMap> variables = new ArrayList<>();
    HashMap metDec = new HashMap();

    //  public HashMap methods = new HashMap();
    //  public HashMap variables = new HashMap();
    // Subcomponents of Program:  MainClass m; List<ClassDecl> cl;
    @Override
    public Void visit(Program n) {
        tab = 0;
        if (n == null) {
            System.out.println("// null Program!!");
        } else if (n.m == null) {
            System.out.println("// null Main class!!");
        } else {
            n.m.accept(this);
            n.cl.stream().forEach((c) -> {
                c.accept(this);
            });
            assert tab == 0;
        }
        return null;
    }

    // Subcomponents of MainClass:  Identifier i1, i2; Statement s;
    @Override
    public Void visit(MainClass n) {
        n.i2.accept(this);  // identifier:  name of arguments
        n.s.accept(this);   // statement:  body of main
        return null;
    }

    // Subcomponents of SimpleClassDecl: Identifier i; List<VarDecl> vl; List<MethodDecl> ml;
    @Override
    public Void visit(final SimpleClassDecl n) {
        n.i.accept(this);
        currentClass = n.i.toString();
        boolean controlMethods = true;
        //Test if there is a class with the same name
        if (Main.classes.containsKey(n.i.s + ".vars")) {
            Main.er.classAlreadyDeclared(n.i.lineNumber, n.i.columnNumber, n.i.s);
            controlMethods = false;
        } else {
            Main.classes.put(n.i.s + ".extends", "none");
            Main.classes.put(n.i.toString() + ".methods", new HashMap());
            Main.classes.put(n.i.toString() + ".vars", new HashMap());
        }
        if (controlMethods) { //if the class is already declared,
            //I skip adding its methods into the table
            for (VarDecl v : n.vl) {
                HashMap varDecl = (HashMap) Main.classes.get(n.i.toString() + ".vars");

                if (varDecl.containsKey(v.i.s)) {
                    //Variable already declared, call semantic error
                    Main.er.varAlreadyDeclared(v.i.lineNumber,
                            v.i.columnNumber,
                            n.i.s,
                            v.i.s);
                } else if (v.t.getName() != "int" && v.t.getName() != "boolean" && v.t.getName() != "int[]") {
                    if (!(Main.classes.containsKey(v.t.toString() + ".vars"))) {
                        Main.er.notDeclaredClass(v.t.lineNumber, v.t.columnNumber, v.t.toString());
                    } else {
                        varDecl.put(v.i.s, v.t.toString());
                    }
                } else {
                    //add var to the symbol table
                    varDecl.put(v.i.s, v.t.toString());
                }

                v.accept(this);
            }

            for (MethodDecl m : n.ml) {

                m.accept(this);
                HashMap metDec = (HashMap) Main.classes.get(n.i.toString() + ".methods");
                if (metDec.containsKey(m.i.s)) {
                    //Method already declared in class
                    Main.er.methodAlreadyDeclared(m.i.lineNumber,
                            m.i.columnNumber,
                            n.i.s,
                            m.i.s);
                } else {
                    //add the method to the symbol table, as well as its return type.
                    metDec.put(m.i.s, m.t);
                    //add the number of arguments the method takes
                    metDec.put(m.i.s+".args.num", m.fl.size());
                }

            }
        }//end if control methods
        //println ("}");
        // Does end with a newline
        return null;
    }

    // Subcomponents of ExtendingClassDecl: Identifier i, j; List<VarDecl> vl; List<MethodDecl> ml;
    @Override
    public Void visit(final ExtendingClassDecl n) {
        n.i.accept(this);
        currentClass = n.i.toString();
        boolean controlMethods = true;
        //Test if there is a class with the same name
        if (Main.classes.containsKey(n.i.s + ".vars")) {
            Main.er.classAlreadyDeclared(n.i.lineNumber, n.i.columnNumber, n.i.s);
            controlMethods = false;
        } else {
            if (!(Main.classes.containsKey(n.j.s + ".vars"))) {
                Main.er.notDeclaredClass(n.j.lineNumber, n.j.columnNumber, n.j.s);
            }
            Main.classes.put(n.i.s + ".extends", n.j.s);
            Main.classes.put(n.i.toString() + ".methods", new HashMap());
            Main.classes.put(n.i.toString() + ".vars", new HashMap());
        }
        if (controlMethods) { //if the class is already declared,
            //I skip adding its methods into the table
            for (VarDecl v : n.vl) {
                HashMap varDecl = (HashMap) Main.classes.get(n.i.toString() + ".vars");

                if (varDecl.containsKey(v.i.s)) {
                    //Variable already declared, call semantic error
                    Main.er.varAlreadyDeclared(v.i.lineNumber,
                            v.i.columnNumber,
                            n.i.s,
                            v.i.s);
                } else {
                    //add var to the symbol table
                    varDecl.put(v.i.s, v.t.toString());
                }

                v.accept(this);
            }

            for (MethodDecl m : n.ml) {
                m.accept(this);
                HashMap metDec = (HashMap) Main.classes.get(n.i.toString() + ".methods");
                if (metDec.containsKey(m.i.s)) {
                    //Method already declared in class
                    Main.er.methodAlreadyDeclared(m.i.lineNumber,
                            m.i.columnNumber,
                            n.i.s,
                            m.i.s);
                } else {
                    metDec.put(m.i.s, m.t);
                    metDec.put(m.i.s+".args.num", m.fl.size());
                }

            }
        }//end if control methods
        //println ("}");
        // Does end with a newline
        return null;
    }

    // Subcomponents of VarDecl:  Type t; Identifier i;
    public Void visit(VarDecl n) {
        n.t.accept(this);   // Type t: no new line
        //  print (" ");
        n.i.accept(this);   // Identifier i: no new line
        //  println (";");
        // Does end with a newline
        return null;
    }

    // Subcomponents of MethodDecl:
    // Type t; Identifier i; List<Formal> fl; List<VarDecl> vl; List<Statement>t sl; Expression e;
    public Void visit(MethodDecl n) {
        n.t.accept(this);
        //  print (" ");
        n.i.accept(this);
        //print (" (");
        int formalTypes = 0;

        if (n.fl.size() > 0) {
            n.fl.get(0).accept(this);
            //We dont need to check if the first parameter of a method is repeated
            Main.classes.put(
                    currentClass
                    + "." + n.i
                    + "." + n.fl.get(0).i,
                    n.fl.get(0).t.toString()
            );

            Main.classes.put(
                    currentClass
                    + "." + n.i
                    + "." + formalTypes,
                    n.fl.get(0).t.toString()
            );
            formalTypes += 1;
            // Loop over all formals excluding the first one
            for (Formal f : n.fl.subList(1, n.fl.size())) {
                //we check for all the others

                if (Main.classes.containsKey(
                        currentClass
                        + "." + n.i.s
                        + "." + f.i.s)) {
                    Main.er.parMethodAlreadyDecl(f.i.lineNumber,
                            f.i.columnNumber,
                            n.i.s,
                            f.i.s);
                } else {

                    Main.classes.put(currentClass
                            + "." + n.i.s + "." + f.i.s, f.t.toString());
                    Main.classes.put(currentClass
                            + "." + n.i.s + "." + formalTypes, f.t.toString());
                    Main.classes.put(
                            currentClass
                            + "." + n.i
                            + "." + formalTypes,
                            n.fl.get(formalTypes).t.toString()
                    );
                    formalTypes += 1;

                }

                //      print (", ");
                f.accept(this);
            }
        }
        //print (")");
        //println (" {"); tab++;
        for (VarDecl v : n.vl) {
            if (Main.classes.containsKey(currentClass
                    + "." + n.i + "." + v.i)) {

                Main.er.varMethodAlreadyDecl(v.i.lineNumber,
                        v.i.columnNumber,
                        n.i.s, v.i.s);
            } else {
                Main.classes.put(currentClass
                        + "." + n.i + "." + v.i, v.t.toString());
            }

            v.accept(this);
        }
        for (Statement s : n.sl) {
            s.accept(this);
        }
        n.e.accept(this);      // Expression e: no new line
        return null;
    }

    // Subcomponents of Formal:  Type t; Identifier i;
    public Void visit(Formal n) {
        n.t.accept(this);
        n.i.accept(this);
        // Does not end with a newline
        return null;
    }

    public Void visit(IntArrayType n) {
        // Does not end with a newline
        return null;
    }

    public Void visit(BooleanType n) {
        return null;
    }

    public Void visit(IntegerType n) {
        return null;
    }

    // String s;
    public Void visit(IdentifierType n) {
        //print (n.s);
        return null;
    }

    // Subcomponents of Block statement:  StatementList sl;
    public Void visit(Block n) {
        for (Statement s : n.sl) {
            s.accept(this);
        }
        return null;
    }

    // Subcomponents of If statement: Expression e; Statement s1,s2;
    public Void visit(If n) {
        n.e.accept(this);
        blockStm(n.s1);
        blockStm(n.s2);
        return null;
    }

    private void blockStm(Statement s) {
        if (s instanceof Block) {
            for (Statement ss : ((Block) s).sl) {
                ss.accept(this);
            }
        } else {
            s.accept(this);
        }
    }

    // Subcomponents of While statement: Expression e, Statement s
    public Void visit(final While n) {
        n.e.accept(this);
        blockStm(n.s);
        return null;
    }

    // Subcomponents of Print statement:  Expression e;
    public Void visit(Print n) {
        n.e.accept(this);
        return null;
    }

    // subcomponents of Assignment statement:  Identifier i; Expression e;
    public Void visit(Assign n) {
        n.i.accept(this);
        n.e.accept(this);
        return null;
    }

    // Identifier i; Expression e1,e2;
    public Void visit(ArrayAssign n) {
        n.i.accept(this);
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Expression e1,e2;
    public Void visit(And n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Expression e1,e2;
    public Void visit(LessThan n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Expression e1,e2;
    public Void visit(Plus n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Expression e1,e2;
    public Void visit(Minus n) {
        //System.out.println(n.e1.accept(this).getClass());
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Expression e1,e2;
    public Void visit(Times n) {
        n.e2.accept(this);
        return null;
    }

    // Expression e1,e2;
    public Void visit(ArrayLookup n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Expression e;
    public Void visit(ArrayLength n) {
        n.e.accept(this);
        return null;
    }

    // Subcomponents of Call:  Expression e; Identifier i; ExpressionList el;
    public Void visit(Call n) {
        n.e.accept(this);
        n.i.accept(this);

        if (n.el.size() > 0) {
            n.el.get(0).accept(this);

            // Loop over all actuals excluding the first one
            for (Expression e : n.el.subList(1, n.el.size())) {
                e.accept(this);
            }
        }
        return null;
    }

    public Void visit(True n) {
        return null;
    }

    public Void visit(False n) {
        return null;
    }

    public Void visit(IntegerLiteral n) {
        return null;
    }

    public Void visit(IdentifierExp n) {
        return null;
    }

    public Void visit(This n) {
        return null;
    }

    // Expression e;
    public Void visit(NewArray n) {
        return null;
    }

    // Identifier i;
    public Void visit(NewObject n) {
        return null;
    }

    // Expression e;
    public Void visit(Not n) {
        n.e.accept(this);
        return null;
    }

    // String s;
    public Void visit(Identifier n) {
        return null;
    }
}
