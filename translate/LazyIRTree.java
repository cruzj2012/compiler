/*
 * This abstract class is given in its entirety in Appel, Section 7.2, page 159.
 * But the class name 'Exp' seems a particularly bad choice; so do the method names.
 *
 */

package translate;
import tree.Exp;
import tree.NameOfLabel;
import tree.Stm;

// Lazy view of intermediate representation trees
abstract public class LazyIRTree {
   abstract Exp asExp();                      // ESEQ (asStm(), CONST(0))
   abstract Stm asStm();                      // EVAL (asExp())
   abstract Stm asCond (NameOfLabel t, NameOfLabel f);    // CJUMP (=, asExp(), CONST(0), t, f)
}
