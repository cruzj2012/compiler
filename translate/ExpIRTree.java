/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package translate;

import tree.CJUMP;
import tree.EVAL;
import tree.Exp;
import tree.JUMP;
import tree.LABEL;
import tree.NameOfLabel;
import tree.SEQ;
import tree.Stm;

/**
 *
 * @author cruzj2012
 */
class ExpIRTree extends LazyIRTree {

    private final tree.Exp exp;

    ExpIRTree(tree.Exp e) {
        exp = e;
    }

    @Override
    tree.Exp asExp() {
        return exp;
    }

    @Override
    tree.Stm asStm() {
        return new tree.EVAL(exp);
    }

// unCx ....
    @Override
    Stm asCond(NameOfLabel t, NameOfLabel f) {
        return new EVAL(exp); //To change body of generated methods, choose Tools | Templates.
    }

}

class StmIRTree extends LazyIRTree {

    private final tree.Stm stm;

    StmIRTree(tree.Stm s) {
        stm = s;
    }

    @Override
    tree.Stm asStm() {
        return stm;
    }
// asExp, asCOnd not implemented

    @Override
    Exp asExp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    Stm asCond(NameOfLabel t, NameOfLabel f) {
        //case And

        CJUMP cp = (CJUMP) stm;
        return new CJUMP(cp.relop, cp.left, cp.right, t, f);//To change body of generated methods, choose Tools | Templates.
    }
}

class IfThenElseExp extends LazyIRTree {

    private final LazyIRTree cond, e2, e3;
    final NameOfLabel t = NameOfLabel.generateLabel("if", "then");
    final NameOfLabel f = NameOfLabel.generateLabel("if", "else");
    final NameOfLabel join = NameOfLabel.generateLabel("if", "end");

    IfThenElseExp(LazyIRTree c, LazyIRTree thenClause, LazyIRTree elseClause) {
        //assert cond!=null; 
        //assert e2!=null;
        cond = c;
        e2 = thenClause;
        e3 = elseClause;

    }

    @Override
    public Exp asExp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //public Stm asStm() { /*...*/ }
    //public Stm asCond (Label tt, Label ff) { /* ... */ }
    Stm asCond(LABEL t, LABEL f) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    Stm asStm() {
        //NameOfTemp result = NameOfTemp.generateTemp();
        //final Stm seq;
        return new SEQ(cond.asCond(t, f),
                new SEQ(new LABEL(t),
                        new SEQ(e2.asStm(), //      result := then expr
                                new SEQ(new JUMP(join), //      goto join
                                        new SEQ(new LABEL(f), // F:
                                                new SEQ(e3.asStm(), //      result := else expr
                                                        new LABEL(join)))))));                  // join:

        //return seq;
    }

    @Override
    Stm asCond(NameOfLabel t, NameOfLabel f) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

class WhileLoop extends LazyIRTree {

    private final NameOfLabel testWhile = NameOfLabel.generateLabel("test", "While");
    private final NameOfLabel bodyWhile = NameOfLabel.generateLabel("body", "While");
    private final NameOfLabel doneWhile = NameOfLabel.generateLabel("done", "While");

    private final LazyIRTree cond, blockStm;

    public WhileLoop(LazyIRTree cond, LazyIRTree blockStm) {
        this.cond = cond;
        this.blockStm = blockStm;
    }

    @Override
    Exp asExp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    Stm asStm() {
        
        return new SEQ(
                new LABEL(testWhile),
                new SEQ(
                        cond.asCond(doneWhile, bodyWhile),
                        new SEQ(
                                new SEQ(
                                        new LABEL(bodyWhile),
                                        new SEQ(
                                                blockStm.asStm(),
                                                new JUMP(testWhile))
                                ),
                                new LABEL(doneWhile)
                        )
                )
        );
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    Stm asCond(NameOfLabel t, NameOfLabel f) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
