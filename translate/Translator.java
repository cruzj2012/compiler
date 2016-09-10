/*
 * Florida Tech, CSE4251: Compiler Design.  Part of the compiler project from
 * "Modern Compiler Implementation in Java," 2nd edition, by Andrew W. Appel.
 */
package translate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import parser.Main;
import syntax.*;
import tree.BINOP;
import tree.CALL;
import tree.CJUMP;
import tree.CONST;
import tree.EVAL;
import tree.JUMP;
import tree.LABEL;
import tree.NAME;
import tree.NameOfLabel;
import tree.SEQ;
import tree.Stm;
import tree.TEMP;
import tree.Exp;
import tree.MEM;
import tree.MOVE;

//import java.io.PrintWriter;
public class Translator implements SyntaxTreeVisitor<LazyIRTree> {

    public HashMap<String, Object> frameControl = new HashMap<>();
    public HashMap<String, Object> frame = new HashMap<>();

    String currentClass = "";
    String currentMethod = "";

    String througMethod = "";

    //  public Hashtable methods = new Hashtable();
    //  public Hashtable variables = new Hashtable();
    public int nClass = 0;
    public static List<Stm> lseq = new ArrayList<>();
    public static List<List> lmethods = new ArrayList<>();

    // Subcomponents of Program:  MainClass m; List<ClassDecl> cl;
    @Override
    public LazyIRTree visit(Program n) {
        frame.put("input", "none");
        frameControl.put("input", "none");
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
    public LazyIRTree visit(MainClass n) {
        n.i1.accept(this);
        n.i2.accept(this);
        currentClass = n.i1.s;
        currentMethod = "main";

        SEQ sqm = new SEQ(new LABEL(NameOfLabel.concat(currentClass, currentMethod, "preludeEnd")),
                new SEQ(
                        n.s.accept(this).asStm(),
                        new JUMP(NameOfLabel.concat(currentClass, currentMethod, "epilogBegin")
                        )
                ));
        lseq.add(sqm);
        //lmethods.add(new ArrayList().add(sqm));
        return null;
    }

    // Subcomponents of SimpleClassDecl: Identifier i; List<VarDecl> vl; List<MethodDecl> ml;
    @Override
    public LazyIRTree visit(final SimpleClassDecl n) {
        n.i.accept(this);
        currentClass = n.i.toString();

        for (int i = 0; i < n.vl.size(); i++) {
            frameControl.put(NameOfLabel.concat(currentClass, n.vl.get(i).i.s), "%i0+" + 4 * (i + 1));
            n.vl.get(i).accept(this);
        }

        n.vl.stream().forEach((v) -> {
            v.accept(this);
            frameControl.put(currentClass, v);
        });

        n.ml.stream().forEach((m) -> {
            lseq.add(
                    new SEQ(
                            new LABEL(NameOfLabel.concat(currentClass, m.i.s, "preludeEnd")),
                            new SEQ(
                                    m.accept(this).asStm(),
                                    new JUMP(NameOfLabel.concat(currentClass, m.i.s, "epilogBegin"))
                            )));

        });
        return null;
    }

    // Subcomponents of ExtendingClassDecl: Identifier i, j; List<VarDecl> vl; List<MethodDecl> ml;
    @Override
    public LazyIRTree visit(final ExtendingClassDecl n) {
        n.i.accept(this);
        n.j.accept(this);
        //  println (" {");  tab++;
        n.vl.stream().forEach((v) -> {
            v.accept(this);
        });
        n.ml.stream().forEach((m) -> {
            LABEL lb = new LABEL(NameOfLabel.concat(currentClass, n.i.s));
            //SEQ sqm = new SEQ(LABEL.generateLABEL(n.i1.s,"main"),new StmIRTree(lb).asStm());    
            lseq.add(new SEQ(
                    new LABEL(NameOfLabel.concat(currentClass, m.i.s, "1")),
                    new SEQ(
                            new LABEL(NameOfLabel.concat(currentClass, m.i.s, "preludeEnd")),
                            new SEQ(
                                    m.accept(this).asStm(),
                                    new JUMP(NameOfLabel.concat(currentClass, n.i.s, "epilogBegin"))
                            ))));

        });
        return null;
    }

    // Subcomponents of VarDecl:  Type t; Identifier i;
    @Override
    public LazyIRTree visit(VarDecl n) {
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
    public LazyIRTree visit(MethodDecl n) {

        currentMethod = n.i.s;

        SEQ seq = null;
        SEQ seq1;

        for (int i = 0; i < n.fl.size(); i++) {
            Formal fl = n.fl.get(i);
            frameControl.put(NameOfLabel.concat(currentClass, currentMethod, fl.i.s), "%i" + (i + 1));
        }

        for (int i = 0; i < n.vl.size(); i++) {
            VarDecl vd = n.vl.get(i);
            frameControl.put(NameOfLabel.concat(currentClass, currentMethod, vd.i.s), "%fp-" + 4 * (i + 1));
        }

        if (n.sl.isEmpty()) {
            return new StmIRTree(new MOVE(
                    new TEMP("%i0"),
                    n.e.accept(this).asExp()
            )); //call method that
        } else {
            MOVE expRreturn = new MOVE(
                    //This is a return. Should always be %i0 the
                    //not sure, maybe could be %o0
                    new TEMP("%i0"),
                    n.e.accept(this).asExp());

//            for (int i = 0; i < 1; i++) {
//                seq = new SEQ(
//                        n.sl.get(i).accept(this).asStm(),
//                        new MOVE(
//                                //This is a return. Should always be %i0 the
//                                //not sure, maybe could be %o0
//                                new TEMP("%i0"),
//                                n.e.accept(this).asExp()
//                        ) //call method that
//
//                );
//
//            }
            if (n.sl.size() == 1) {
                seq1 = new SEQ(n.sl.get(0).accept(this).asStm(), expRreturn);
                return new StmIRTree(seq1);
            } else {
                seq1 = new SEQ(n.sl.get(0).accept(this).asStm(), n.sl.get(1).accept(this).asStm());

                for (int i = 2; i < n.sl.size(); i++) {
                    seq1 = new SEQ(seq1, n.sl.get(i).accept(this).asStm());
                }
                return new StmIRTree(new SEQ(seq1, expRreturn));

            }

//            for (int i = 0; i < n.sl.size(); i++) {
//                seq1 = new SEQ(n.sl.get(i).accept(this).asStm(), seq);
//                seq = seq1;
//            }
            //n.e.accept(this);      // Expression e: no new line
//            return new StmIRTree(seq);
        }
    }
///***
// * Returns handling
// * @return new MOVE which is the final part of a method
// * 
// */
//    public LazyIRTree methodReturns() {
//        return new StmIRTree(new MOVE(
//                new TEMP(NameOfTemp.generateTemp()),
//                new MEM(
//                        new BINOP(BINOP.MINUS,
//                                new TEMP(NameOfTemp.generateTemp()),
//                                new CONST(4))
//                )
//        ));
//    }

    // Subcomponents of Formal:  Type t; Identifier i;
    @Override
    public LazyIRTree visit(Formal n) {
        n.t.accept(this);
        n.i.accept(this);
        // Does not end with a newline
        return null;
    }

    @Override
    public LazyIRTree visit(IntArrayType n) {

        // Does not end with a newline
        return null;
    }

    @Override
    public LazyIRTree visit(BooleanType n) {
        return null;
    }

    @Override
    public LazyIRTree visit(IntegerType n) {
        return null;
    }

    // String s;
    @Override
    public LazyIRTree visit(IdentifierType n) {

        //print (n.s);
        return null;//new IdentifierType(0,0,"");
    }

    // Subcomponents of Block statement:  StatementList sl;
    @Override
    public LazyIRTree visit(Block n) {
        SEQ seq;
        SEQ seq1;
        if (n.sl.size() == 1) {
            return new StmIRTree(n.sl.get(0).accept(this).asStm());

        } else {
            seq = new SEQ(
                    n.sl.get(0).accept(this).asStm(),
                    n.sl.get(1).accept(this).asStm()
            );

            for (int i = 2; i < n.sl.size(); i++) {
                seq1 = new SEQ(seq, n.sl.get(i).accept(this).asStm());
                seq = seq1;
            }
            //n.e.accept(this);      // Expression e: no new line
            return new StmIRTree(seq);// never to be reached, in an ideal world, which is not this
        }
    }

    // Subcomponents of If statement: Expression e; Statement s1,s2;
    @Override
    public LazyIRTree visit(If n) {
        //System.out.println(n.s2.getClass());
        IfThenElseExp ite = new IfThenElseExp(n.e.accept(this), n.s1.accept(this), n.s2.accept(this));
        //System.out.println(ite.getClass());
        return ite;
    }

    // Subcomponents of While statement: Expression e, Statement s
    @Override
    public LazyIRTree visit(final While n) {
        //System.out.println("test");
        LazyIRTree exp = n.e.accept(this);

        LazyIRTree stms = n.s.accept(this);
        if (exp.asStm() instanceof CJUMP) {
            CJUMP whil = (CJUMP) (exp.asStm());
            CJUMP w2 = new CJUMP(CJUMP.GE, whil.left, whil.right, whil.iffalse, whil.iftrue);
            WhileLoop whilLop = new WhileLoop(new StmIRTree(w2), stms);
            return whilLop;
        }
        WhileLoop whilLop = new WhileLoop(exp, stms);
        return whilLop;
    }

    // Subcomponents of Print statement:  Expression e;
    @Override
    public LazyIRTree visit(Print n) {
        //Check here
        EVAL ev
                = new EVAL(
                        new CALL(
                                new NAME(NameOfLabel.concat("print")),
                                n.e.accept(this).asExp()));

        return new StmIRTree(ev);
    }

    // subcomponents of Assignment statement:  Identifier i; Expression e;
    @Override
    public LazyIRTree visit(Assign n) {
        if (n.e instanceof NewArray) {

            return new StmIRTree(new MOVE(n.i.accept(this).asExp(),
                    n.e.accept(this).asExp()));
        } else {
            return new StmIRTree(new MOVE(
                    new MEM(
                            n.i.accept(this).asExp()
                    ),
                    n.e.accept(this).asExp()
            ));
        }
    }

    // Identifier i; Expression e1,e2;
    @Override
    public LazyIRTree visit(ArrayAssign n) {

        return new StmIRTree(new MOVE(
                new MEM(
                        new BINOP(BINOP.PLUS,
                                new BINOP(BINOP.MUL,
                                        new BINOP(BINOP.PLUS,
                                                n.e1.accept(this).asExp(),
                                                new CONST((1))),
                                        new CONST(4)), 
                                new MEM(n.i.accept(this).asExp()))
                ),
                n.e2.accept(this).asExp()
        ));

    }

    @Override
    public LazyIRTree visit(And n) {
        return new StmIRTree(
                new CJUMP(
                        CJUMP.EQ,
                        n.e1.accept(this).asExp(),
                        n.e2.accept(this).asExp(),
                        "",
                        "")
        );
    }

    // Expression e1,e2;
    @Override
    public LazyIRTree visit(LessThan n) {

        return new StmIRTree(
                new CJUMP(CJUMP.LT,
                        n.e1.accept(this).asExp(),
                        n.e2.accept(this).asExp(),
                        "", ""
                )
        );
    }

    // Expression e1,e2;
    @Override
    public LazyIRTree visit(Plus n) {
        return new ExpIRTree(new BINOP(
                BINOP.PLUS,
                n.e1.accept(this).asExp(),
                n.e2.accept(this).asExp()));
    }

    // Expression e1,e2;
    @Override
    public LazyIRTree visit(Minus n
    ) {
        return new ExpIRTree(new BINOP(
                BINOP.MINUS,
                n.e1.accept(this).asExp(),
                n.e2.accept(this).asExp()));
    }

    // Expression e1,e2;
    @Override
    public LazyIRTree visit(Times n) {
        return new ExpIRTree(new BINOP(
                BINOP.MUL,
                n.e1.accept(this).asExp(),
                n.e2.accept(this).asExp()));
    }

    // Expression e1,e2;
    public LazyIRTree visit(ArrayLookup n) {
        return new ExpIRTree(
                new MEM(
                        new BINOP(BINOP.PLUS,
                                new BINOP(BINOP.MUL,
                                        new BINOP(BINOP.PLUS,
                                                n.e2.accept(this).asExp(),
                                                new CONST((1))),
                                        new CONST(4)), 
                                new MEM(n.e1.accept(this).asExp()))
                )
                
        );
    }

    // Expression e;
    @Override
    public LazyIRTree visit(ArrayLength n) {
        //n.e.accept(this);
        return new ExpIRTree(new MEM(new MEM(n.e.accept(this).asExp())));

    }

    // Subcomponents of Call:  Expression e; Identifier i; ExpressionList el;
    @Override
    public LazyIRTree visit(Call n) {
        if (n.e instanceof NewObject) {
            String lb1 = ((TEMP) (n.e.accept(this)).asExp()).temp.toString();
            String lb2 = n.i.s;
            frame.put(lb1, "%i0");

            CALL cl;

            List<Exp> le = new ArrayList<>();
            int bytesMem = 0;
            if (Main.classes.get(lb1 + ".vars") != null) {
                HashMap justForSize = (HashMap) Main.classes.get(lb1 + ".vars");
                bytesMem = justForSize.size() * 4;
            }
            //le.add(new TEMP(NameOfTemp.generateTemp()));
            le.add(new CALL(new NAME("alloc_object"), new CONST((bytesMem))));
            for (int i = 0; i < n.el.size(); i++) {
                le.add(n.el.get(i).accept(this).asExp());
            }

            cl = new CALL(
                    new NAME(NameOfLabel.concat(lb1, lb2, "1")),
                    le
            );

            return new ExpIRTree(cl);
        } else {
            List<Exp> le = new ArrayList<>();
            le.add(new TEMP("%i0")); //first argument of a CALL that is not a new object
            //should be always the input of that same object.
            //I am not sure about this. Perhaps a better idea would be to
            //have a control structure for the Call telling me that is now a branch
            //and as such I should pass the first parameter as being the memory address
            //for the object being allocated.
            for (int i = 0; i < n.el.size(); i++) {
                le.add(n.el.get(i).accept(this).asExp());
            }
            String classToCall = currentClass;
            String methodToCall = currentMethod;
            if (!(n.e instanceof This) && !(n.e instanceof Call)) {
                classToCall = ((IdentifierExp) n.e).s;
            }

            if (!(currentMethod.equals(n.i.s))) {
                methodToCall = n.i.s;
            }

            return new ExpIRTree(
                    new CALL(
                            new NAME(NameOfLabel.concat(classToCall, methodToCall, "1")),
                            le));

        }

    }

    @Override
    public LazyIRTree visit(True n) {
        return new ExpIRTree(CONST.TRUE);
    }

    @Override
    public LazyIRTree visit(False n) {

        return new ExpIRTree(CONST.FALSE);
    }

    @Override
    public LazyIRTree visit(IntegerLiteral n) {
        return new ExpIRTree(new CONST(n.i));
    }

    @Override
    public LazyIRTree visit(IdentifierExp n) {
        TEMP itemp = new TEMP(n.s);
        if (frameControl.containsKey(NameOfLabel.concat(currentClass, currentMethod, n.s))) {
            frame.put(currentClass + "." + currentMethod + "." + itemp.temp.toString(),
                    frameControl.get(NameOfLabel.concat(currentClass, currentMethod, n.s)));
        }
        return new ExpIRTree(itemp);
    }

    @Override
    public LazyIRTree visit(This n) {
        return new ExpIRTree(new TEMP(currentClass));
    }

    // Expression e;
    @Override
    public LazyIRTree visit(NewArray n) {
        LazyIRTree arrayValue = n.e.accept(this);
        return new ExpIRTree(
                new CALL(new NAME("alloc_array"),
                        arrayValue.asExp(),
                        new CONST(4)
                ));
    }

    // Identifier i;
    @Override
    public LazyIRTree visit(NewObject n) {
        return new ExpIRTree(new TEMP(n.i.s));
    }

    // Expression e;
    @Override
    public LazyIRTree visit(Not n) {
        return new ExpIRTree(CONST.FALSE); //not implemented
    }

    @Override
    public LazyIRTree visit(Identifier n) {
        TEMP itemp = new TEMP(n.s);
        if (frameControl.containsKey(NameOfLabel.concat(currentClass, currentMethod, n.s))) {
            frame.put(currentClass + "." + currentMethod + "." + itemp.temp.toString(),
                    frameControl.get(NameOfLabel.concat(currentClass, currentMethod, n.s)));
        }
        if (frameControl.containsKey(NameOfLabel.concat(currentClass, n.s))) {
            frame.put(currentClass + "." + itemp.temp.toString(),
                    frameControl.get(NameOfLabel.concat(currentClass, n.s)));
        }
        return new ExpIRTree(itemp);
    }
}
