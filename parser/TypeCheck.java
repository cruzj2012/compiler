/*
 * Florida Tech, CSE4251: Compiler Design.  Part of the compiler project from
 * "Modern Compiler Implementation in Java," 2nd edition, by Andrew W. Appel.
 */
package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import syntax.*;

//import java.io.PrintWriter;
public class TypeCheck implements SyntaxTreeVisitor<Object> {

    String currentClass = "";
    String currentMethod = "";
    String extend = "";

    //  public HashMap methods = new HashMap();
    //  public HashMap variables = new HashMap();
    public int nClass = 0;

    // Subcomponents of Program:  MainClass m; List<ClassDecl> cl;
    @Override
    public Type visit(Program n) {
        if (n == null) {
            System.out.println("// null Program!!");
        } else if (n.m == null) {
            System.out.println("// null Main class!!");
        } else {
            n.m.accept(this);
            n.cl.stream().forEach((c) -> {
                c.accept(this);
            });
            nClass = n.cl.size();
        }
        return null;
    }

    // Subcomponents of MainClass:  Identifier i1, i2; Statement s;
    @Override
    public Type visit(MainClass n) {
        n.i1.accept(this);
        n.i2.accept(this);  // identifier:  name of arguments
        n.s.accept(this);   // statement:  body of main
        return null;
    }

    // Subcomponents of SimpleClassDecl: Identifier i; List<VarDecl> vl; List<MethodDecl> ml;
    @Override
    public Type visit(final SimpleClassDecl n) {
        n.i.accept(this);
        currentClass = n.i.toString();
        extend = (String) Main.classes.get(currentClass + ".extends");

        n.vl.stream().forEach((v) -> {
            v.accept(this);
        });

        n.ml.stream().forEach((m) -> {
            m.accept(this);
        });
        return null;
    }

    // Subcomponents of ExtendingClassDecl: Identifier i, j; List<VarDecl> vl; List<MethodDecl> ml;
    @Override
    public Type visit(final ExtendingClassDecl n) {
        currentClass = n.i.toString();
        extend = (String) Main.classes.get(currentClass + ".extends");

        n.i.accept(this);
        n.j.accept(this);
        //  println (" {");  tab++;
        n.vl.stream().forEach((v) -> {
            v.accept(this);
        });
        n.ml.stream().forEach((m) -> {
            m.accept(this);
        });
        return null;
    }

    // Subcomponents of VarDecl:  Type t; Identifier i;
    @Override
    public Type visit(VarDecl n) {
        n.t.accept(this);   // Type t: no new line
        //  print (" ");
        n.i.accept(this);   // Identifier i: no new line
        //  println (";");
        // Does end with a newline
        return null;
    }

    // Subcomponents of MethodDecl:
    // Type t; Identifier i; List<Formal> fl; List<VarDecl> vl; List<Statement>t sl; Expression e;
    @Override
    public Type visit(MethodDecl n) {
        currentMethod = n.i.s;
        n.t.accept(this);
        n.i.accept(this);

        if (n.fl.size() > 0) {
            n.fl.get(0).accept(this);
            n.fl.subList(1, n.fl.size()).stream().forEach((f) -> {
                //we check for all the others
                f.accept(this);
            });
        }
        n.vl.stream().forEach((v) -> {
            v.accept(this);
        });
        n.sl.stream().forEach((s) -> {
            s.accept(this);
        });
        n.e.accept(this);     // Expression e: no new line
        return null;
    }

    // Subcomponents of Formal:  Type t; Identifier i;
    @Override
    public Type visit(Formal n) {
        n.t.accept(this);
        n.i.accept(this);
        // Does not end with a newline
        return null;
    }

    @Override
    public Object visit(IntArrayType n) {
        // Does not end with a newline
        return Type.THE_INT_ARRAY_TYPE.toString();
    }

    @Override
    public Object visit(BooleanType n) {
        return Type.THE_BOOLEAN_TYPE.toString();
    }

    public Object visit(IntegerType n) {
        return new IntegerType().toString();
    }

    // String s;
    @Override
    public Type visit(IdentifierType n) {
        //print (n.s);
        return null;//new IdentifierType(0,0,"");
    }

    // Subcomponents of Block statement:  StatementList sl;
    @Override
    public Type visit(Block n) {
        n.sl.stream().forEach((s) -> {
            s.accept(this);
        });
        return null;
    }

    // Subcomponents of If statement: Expression e; Statement s1,s2;
    @Override
    public Type visit(If n) {
        n.e.accept(this);
        blockStm(n.s1);
        blockStm(n.s2);
        return null;
    }

    private void blockStm(Statement s) {
        if (s instanceof Block) {
            ((Block) s).sl.stream().forEach((ss) -> {
                ss.accept(this);
            });
        } else {
            s.accept(this);
        }
    }

    // Subcomponents of While statement: Expression e, Statement s
    @Override
    public Type visit(final While n) {
        n.e.accept(this);
        blockStm(n.s);
        return null;
    }

    // Subcomponents of Print statement:  Expression e;
    @Override
    public Type visit(Print n) {
        n.e.accept(this);
        return null;
    }

    // subcomponents of Assignment statement:  Identifier i; Expression e;
    @Override
    public Object visit(Assign n) {
        String type = checkVarExists(n.i.accept(this).toString());

        if ("0".equals(type)) {
            Main.er.notDeclared(n.i.lineNumber,
                    n.i.columnNumber,
                    n.i.s);
            return "0";
        } else {
//            if ("int".equals(type) && (n.e.getClass() != Type.THE_INTEGER_TYPE)){
            Object oh = n.e.accept(this).toString();
            if (!(oh.toString().equals(type))) {
                Main.er.wrongAssign(n.i.lineNumber, n.i.columnNumber, type);
            }
        }

        return "0";
    }

    // Identifier i; Expression e1,e2; //not coded yet
    @Override
    public Object visit(ArrayAssign n) {
        String idArray = n.i.accept(this).toString();
        if ("0".equals(idArray)) {
            Main.er.notDeclared(n.i.lineNumber,
                    n.i.columnNumber,
                    n.i.s);
            return "0";
        }
        if (!"int".equals(n.e1.accept(this).toString())) {
            Main.er.wrongArrayPosition(n.i.lineNumber, n.i.columnNumber);
            return "0";
        }
        if (!"int".equals(n.e2.accept(this).toString())) {
            Main.er.wrongAssign(n.i.lineNumber, n.i.columnNumber, "int");
            return "0";
        }
        return "0";
    }

    private String checkVarExists(String id) {
        String ret = "0";
        do {

            {
                String varCheck = currentClass + "." + currentMethod + "." + id;
                String varCheckSuper = extend + "." + currentMethod + "." + id;
                if (Main.classes.containsKey(varCheck)) {
                    ret = (String) Main.classes.get(varCheck);
                    break;
                }
                if (Main.classes.containsKey(varCheckSuper)) {
                    ret = (String) Main.classes.get(varCheckSuper);
                    break;
                }
                if (currentClass.isEmpty()) { //might be checking a new Object decl
                    if (Main.classes.containsKey(id + ".vars")) {
                        ret = id;
                        break;
                    } else {
                        ret = "0";
                        break;
                    }
                } else {
                    HashMap hashVar = (HashMap) Main.classes.get(currentClass + ".vars");
                    HashMap hashMeth = (HashMap) Main.classes.get(currentClass + ".methods");

                    if (!(extend.equals("none"))) {
                        if (Main.classes.containsKey(varCheckSuper)) {
                            ret = (String) Main.classes.get(varCheckSuper);
                            break;
                        }
                        if (Main.classes.containsKey(varCheckSuper)) {
                            ret = (String) Main.classes.get(varCheckSuper);
                            break;
                        } else {
                            HashMap hashVarSuper = (HashMap) Main.classes.get(extend + ".vars");
                            HashMap hashMethSuper = (HashMap) Main.classes.get(extend + ".methods");
                            if (hashVarSuper.containsKey(id)) {
                                ret = (String) hashVarSuper.get(id);
                                break;

                            } else if (hashMethSuper.containsKey(id)) {
                                ret = (String) hashMethSuper.get(id);
                                break;
                            }
                        }
                    }

                    if (hashVar.containsKey(id)) {
                        ret = (String) hashVar.get(id);
                        break;

                    } else if (hashMeth.containsKey(id)) {
                        ret = (String) hashMeth.get(id);
                        break;
                    } else if (Main.classes.containsKey(id + ".vars")) {
                        ret = id;
                        break;
                    } else {
                        extend = (String) Main.classes.get(extend + ".extends");

                    }

                }
            }

        } while (!(extend.equals("none")));
        extend = (String) Main.classes.get(currentClass + ".extends");
        return ret;
    }

    // Expression e1,e2;
    @Override
    public Object visit(And n
    ) {
        Object e1 = n.e1.accept(this);
        Object e2 = n.e2.accept(this);
        if ("0".equals(e1.toString()) || "0".equals(e2.toString())) {
            return "0";
        } else if (!(n.e1.accept(this).toString()
                .equals(Type.THE_BOOLEAN_TYPE.toString()))) {
            Main.er.wrongType(n.e1.lineNumber,
                    n.e1.columnNumber, Type.THE_BOOLEAN_TYPE.toString());
            return "0";

        } else if (!(n.e2.accept(this).toString()
                .equals(Type.THE_BOOLEAN_TYPE.toString()))) {
            Main.er.wrongType(n.e2.lineNumber,
                    n.e2.columnNumber, Type.THE_BOOLEAN_TYPE.toString());
            return "0";
        } else {

            return Type.THE_BOOLEAN_TYPE.toString();
        }
    }

    // Expression e1,e2;
    @Override
    public Object visit(LessThan n
    ) {
        Object e1 = n.e1.accept(this);
        Object e2 = n.e2.accept(this);
        if ("0".equals(e1.toString()) || "0".equals(e2.toString())) {
            return "0";
        } else if (!(n.e1.accept(this).toString()
                .equals(Type.THE_INTEGER_TYPE.toString()))) {
            Main.er.wrongType(n.e1.lineNumber,
                    n.e1.columnNumber, Type.THE_INTEGER_TYPE.toString());
            return "0";

        } else if (!(n.e2.accept(this).toString()
                .equals(Type.THE_INTEGER_TYPE.toString()))) {
            Main.er.wrongType(n.e2.lineNumber,
                    n.e2.columnNumber, Type.THE_INTEGER_TYPE.toString());
            return "0";
        } else {

            return Type.THE_BOOLEAN_TYPE.toString();
        }
    }

    // Expression e1,e2;
    @Override
    public Object visit(Plus n
    ) {
        Object e1 = n.e1.accept(this);
        Object e2 = n.e2.accept(this);
        if ("0".equals(e1.toString()) || "0".equals(e2.toString())) {
            return "0";
        } else if (!(n.e1.accept(this).toString()
                .equals(Type.THE_INTEGER_TYPE.toString()))) {
            Main.er.wrongType(n.e1.lineNumber,
                    n.e1.columnNumber, Type.THE_INTEGER_TYPE.toString());
            return "0";

        } else if (!(n.e2.accept(this).toString()
                .equals(Type.THE_INTEGER_TYPE.toString()))) {
            Main.er.wrongType(n.e2.lineNumber,
                    n.e2.columnNumber, Type.THE_INTEGER_TYPE.toString());
            return "0";
        } else {

            return Type.THE_INTEGER_TYPE.toString();
        }
    }

    // Expression e1,e2;
    @Override
    public Object visit(Minus n
    ) {
        Object e1 = n.e1.accept(this);
        Object e2 = n.e2.accept(this);
        if ("0".equals(e1.toString()) || "0".equals(e2.toString())) {
            return "0";
        } else if (!(n.e1.accept(this).toString()
                .equals(Type.THE_INTEGER_TYPE.toString()))) {
            Main.er.wrongType(n.e1.lineNumber,
                    n.e1.columnNumber, Type.THE_INTEGER_TYPE.toString());
            return "0";

        } else if (!(n.e2.accept(this).toString()
                .equals(Type.THE_INTEGER_TYPE.toString()))) {
            Main.er.wrongType(n.e2.lineNumber,
                    n.e2.columnNumber, Type.THE_INTEGER_TYPE.toString());
            return "0";
        } else {

            return Type.THE_INTEGER_TYPE.toString();
        }
    }

    // Expression e1,e2;
    @Override
    public Object visit(Times n
    ) {
        Object e1 = n.e1.accept(this);
        Object e2 = n.e2.accept(this);
        if ("0".equals(e1.toString()) || "0".equals(e2.toString())) {
            return "0";
        } else if (!(n.e1.accept(this).toString()
                .equals(Type.THE_INTEGER_TYPE.toString()))) {
            Main.er.wrongType(n.e1.lineNumber,
                    n.e1.columnNumber, Type.THE_INTEGER_TYPE.toString());
            return "0";

        } else if (!(n.e2.accept(this).toString()
                .equals(Type.THE_INTEGER_TYPE.toString()))) {
            Main.er.wrongType(n.e2.lineNumber,
                    n.e2.columnNumber, Type.THE_INTEGER_TYPE.toString());
            return "0";
        } else {

            return Type.THE_INTEGER_TYPE.toString();
        }
    }

    // Expression e1,e2;
    public Object visit(ArrayLookup n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return Type.THE_INTEGER_TYPE.toString();
    }

    // Expression e;
    @Override
    public Object visit(ArrayLength n) {
        n.e.accept(this);
        return Type.THE_INTEGER_TYPE.toString();
    }

    private List listOfExtends(String obClass) {
        List<String> allPossible = new ArrayList<>();
        String has;
        has = (String) Main.classes.get(obClass + ".extends");
        while (!has.equals("none")) {
            if (!has.equals("none")) {
                allPossible.add(has);
            }
            has = (String) Main.classes.get(has + ".extends");
        }

        return allPossible;
    }

    // Subcomponents of Call:  Expression e; Identifier i; ExpressionList el;
    @Override
    public Object visit(Call n)   {
        /**
         * *
         * Creating a new object from existing class.
         *
         * We have to check whether the class exists or not.
         *
         * Differences
         *
         * For: new Object().Method(var1,var2);
         *
         * Or for: var3.Method(var4,var4);
         */
        String exp1 = n.e.accept(this).toString(); //this is checked either by NewObject or by idExp
        if(exp1.equals("")){
            Main.er.invalidExpression(n.i.lineNumber - 1, n.i.columnNumber - 1);
            return "0";
        }
        if(exp1.equals(Type.THE_BOOLEAN_TYPE.toString()) ||
               exp1.equals(Type.THE_INTEGER_TYPE.toString()) ||
                exp1.equals(Type.THE_INT_ARRAY_TYPE.toString())){
            Main.er.invalidExpression(n.i.lineNumber - 2, n.i.columnNumber - 2);
            return "0";
        }
                
        if (exp1.equals("0")) {
            return "0";
        } else {

            HashMap methods = (HashMap) Main.classes.get(exp1 + ".methods");
            int methodArgs = 0;
            List<String> argu = new ArrayList<>();
            String argMeth = "";
            String ret = "0";
            if (!methods.containsKey(n.i.s) && !extend.equals("none")) {
                while (!extend.equals("none")) {
                    HashMap methodsExt = (HashMap) Main.classes.get(extend + ".methods");
                    if (methodsExt.containsKey(n.i.s)) {
                        methodArgs = (int) methodsExt.get(n.i.s + ".args.num");
                        for (int i = 0; i < methodArgs; i++) {
                            argu.add((String) Main.classes.get(extend + "." + n.i.s + "." + i));
                            argMeth += (String) Main.classes.get(extend + "." + n.i.s + "." + i) + ",";
                        }
                        ret = (String) methodsExt.get(n.i.s).toString();
                        extend = (String) Main.classes.get(exp1 + ".extends");
                        break;
                    } else {
                        extend = (String) Main.classes.get(extend + ".extends");
                    }
                }
            } else {
                methodArgs = (int) methods.get(n.i.s + ".args.num");
                for (int i = 0; i < methodArgs; i++) {
                    argu.add((String) Main.classes.get(exp1 + "." + n.i.s + "." + i));
                    argMeth += (String) Main.classes.get(exp1 + "." + n.i.s + "." + i) + ",";
                }
                if (!(methods.containsKey(n.i.s))) {
                    Main.er.methodNotFoundInClass(n.i.lineNumber, n.i.columnNumber, n.i.s, exp1);
                    return ret;
                }
                ret = methods.get(n.i.s).toString();
            }
            int listSize = n.el.size();
            //int methodArgs = (int) methods.get(n.i.s + ".args.num");

            if (listSize != methodArgs) {
                Main.er.incorrectNumberofArgs(
                        n.i.lineNumber,
                        n.i.columnNumber,
                        n.i.s,
                        methodArgs,
                        n.el.size());
                return ret;

            } else if (listSize > 0) {
                String argCall = n.el.get(0).accept(this).toString() + ",";
                String argCallsub = argCall.substring(0, argCall.length() - 1);
                // Loop over all actuals excluding the first one
                if (!(argCallsub.equals(Type.THE_BOOLEAN_TYPE.toString())
                        || argCallsub.equals(Type.THE_INTEGER_TYPE.toString())
                        || argCallsub.equals(Type.THE_INT_ARRAY_TYPE.toString()))) {
                    List<String> possible = listOfExtends(argCallsub);

                    for (int i = 0; i < possible.size(); i++) {
                        if (argMeth.contains(possible.get(i))) {
                            return ret;
                        }
                    }
                }

//                for (int i = 0; i < methodArgs; i++) {
//                    argMeth += (String) Main.classes.get(exp1 + "." + n.i.s + "." + i) + ",";
//                }
                for (Expression e : n.el.subList(1, n.el.size())) {
                    argCall += e.accept(this).toString() + ",";
                }
                argMeth = argMeth.substring(0, argMeth.length() - 1);
                argCall = argCall.substring(0, argCall.length() - 1);
                if (!(argCall.equals(argMeth))) {
                    Main.er.incorrectTypeofArgs(n.i.lineNumber,
                            n.i.columnNumber,
                            n.i.s,
                            argMeth,
                            argCall);
                    return "0";
                } else {
                    return ret;
                }

            } else {
                return ret;
            }
        }
    }

    @Override
    public Object visit(True n
    ) {
        return Type.THE_BOOLEAN_TYPE.toString();
    }

    @Override
    public Object visit(False n
    ) {
        return Type.THE_BOOLEAN_TYPE.toString();
    }

    @Override
    public Object visit(IntegerLiteral n
    ) {
        return Type.THE_INTEGER_TYPE.toString();
    }

    @Override
    public Object visit(IdentifierExp n
    ) {
        if (checkVarExists(n.s).equals("0")) {
            Main.er.notDeclared(n.lineNumber, n.columnNumber, n.s);
        }
        return checkVarExists(n.s);
    }

    @Override
    public Object visit(This n
    ) {
        return currentClass;
    }

    // Expression e;
    @Override
    public Object visit(NewArray n
    ) {
        return Type.THE_INT_ARRAY_TYPE.toString();
    }

    // Identifier i;
    @Override
    public Object visit(NewObject n
    ) {
        String object = checkVarExists(n.i.s);
        if (object.equals("0")) {
            Main.er.notDeclaredClass(n.i.lineNumber, n.i.columnNumber, n.i.s);
            return object;
        } else {
            //currentClass = n.i.s;
            return n.i.s;
        }
    }

    // Expression e;
    @Override
    public Object visit(Not n
    ) {
        String check = (String) n.e.accept(this);
        if (!(check.equals(Type.THE_BOOLEAN_TYPE.toString()))) {
            return "0";
        } else if (!(n.e.accept(this).toString().equals(Type.THE_BOOLEAN_TYPE.toString()))) {
            return "0";
        } else {
            return Type.THE_BOOLEAN_TYPE.toString();
        }
    }

    @Override
    public Object visit(Identifier n
    ) {
        return new Identifier(n.lineNumber, n.columnNumber, n.s);
    }
}
