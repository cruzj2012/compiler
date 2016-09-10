/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sparc;

import assem.Comment;
import assem.Instruction;
import assem.LabelInstruction;
import assem.MoveInstruction;
import assem.OperationInstruction;
import java.util.ArrayList;
import java.util.List;
import parser.Main;
import tree.*;

/**
 *
 * @author cruzj2012
 */
public class Codegen {

    private final String nopInstr = "	nop";
    private final String nopComment = "(do nothing in the delay slot)";
    private final String classMethod;
    private final String className;

    public Codegen(String className, String classMethod) {
        this.classMethod = classMethod;
        this.className = className;
    }

    private final List<assem.Instruction> ilist = new ArrayList<>();

    public List<Instruction> getIlist() {
        return ilist;
    }

    private void emit(assem.Instruction inst) {
        ilist.add(inst);

    }

    void munchMove(MEM dst, Exp src) {

        if (dst.exp instanceof BINOP && ((BINOP) dst.exp).binop == BINOP.PLUS
                && ((BINOP) dst.exp).left instanceof BINOP) {
            //MOVE(MEM(BINOP(PLUS,e1,CONST(i))),e2)
            NameOfTemp left = munchExp(dst);
            String leftS = left.toString();
            NameOfTemp srcExp = munchExp(src);
            String srcE = srcExp.toString();
            if (Main.tr.frame.containsKey(classMethod + leftS)) {
                leftS = (String) Main.tr.frame.get(classMethod + leftS);
            }

            if (Main.tr.frame.containsKey(className + leftS)) {
                leftS = (String) Main.tr.frame.get(className + leftS);
            }
            if (Main.tr.frame.containsKey(classMethod + srcE)) {
                srcE = (String) Main.tr.frame.get(classMethod + srcE);
                if (srcE.contains("fp")) {
                    srcExp = localLoad(srcE);
                    srcE = srcExp.toString();
                }
            }

            if (Main.tr.frame.containsKey(className + srcE)) {
                srcE = (String) Main.tr.frame.get(className + srcE);
                if (srcE.contains("i0")) {
                    srcExp = localLoad(srcE);
                    srcE = srcExp.toString();
                }
            }
            emit(new OperationInstruction("	st	" + srcE + "," + "[" + leftS + "]", "MEM[" + leftS + "] := " + srcExp, left, srcExp));

        } else if (src instanceof MEM) {
            if (((MEM) src).exp instanceof BINOP) {
                // all possible combinations for BINOP
                if (((BINOP) ((MEM) src).exp).binop == BINOP.PLUS) {
                    //all possible combinations for PLUS
                    if (((BINOP) ((MEM) src).exp).left instanceof BINOP) {
                        NameOfTemp left = munchExp(dst.exp);
                        String leftS = left.toString();
                        if (Main.tr.frame.containsKey(classMethod + leftS)) {
                            leftS = (String) Main.tr.frame.get(classMethod + leftS);
                        }

                        if (Main.tr.frame.containsKey(className + leftS)) {
                            leftS = (String) Main.tr.frame.get(className + leftS);
                        }
                        NameOfTemp srcExp = munchExp(src);
                        NameOfTemp r = NameOfTemp.generateTemp();
                        emit(new OperationInstruction("	ld	[" + srcExp + "]," + r, r + " := " + "MEM[" + srcExp + "]", srcExp, r));
                        emit(new OperationInstruction("	st	" + r + "," + "[" + leftS + "]", "MEM[" + leftS + "] := " + r, left, r));
                    }
                }
            }
//            //MOVE(MEM(BINOP(PLUS,CONST(i),e1)),e2)
//            NameOfTemp left = munchExp(((BINOP) dst.exp).right);
//            NameOfTemp srcExp = munchExp(src);
//            emit(new OperationInstruction("	st	", dst.toString().replace("\n", "") + " and " + src.toString(), left, srcExp));

        } else if (dst.exp instanceof BINOP && ((BINOP) dst.exp).binop == BINOP.MINUS
                && ((BINOP) dst.exp).right instanceof CONST) {
            //MOVE(MEM(BINOP(MINUS,e1,CONST(i))),e2)
            NameOfTemp offsetNT = munchExp(((BINOP) dst.exp).left);
            String offset = offsetNT.toString();
            if (Main.tr.frame.containsKey(classMethod + offset)) {
                offset = (String) Main.tr.frame.get(classMethod + offset);
            }
            NameOfTemp tExp = munchExp(src);
            String sExp = tExp.toString();

            if (Main.tr.frame.containsKey(classMethod + sExp)) {
                sExp = (String) Main.tr.frame.get(classMethod + sExp);
            }
            emit(new OperationInstruction("	st	" + sExp + ",[" + offset + "]",
                    "MEM[" + offset + "] := " + sExp, tExp, offsetNT));

        } else if (dst.exp instanceof BINOP && ((BINOP) dst.exp).binop == BINOP.MINUS
                && ((BINOP) dst.exp).left instanceof CONST) {

            String offset = munchExp(((BINOP) dst.exp).right).toString();
            if (Main.tr.frame.containsKey(classMethod + offset)) {
                offset = (String) Main.tr.frame.get(classMethod + offset);
            }
            NameOfTemp ssT = munchExp(src);
            String ss = ssT.toString();

            if (Main.tr.frame.containsKey(classMethod + ss)) {
                ss = (String) Main.tr.frame.get(classMethod + ss);
            }
            emit(new OperationInstruction("	st	" + ss + ",[" + offset + "]",
                    "MEM[" + offset + "] := " + ss, ssT));

        } else if (dst.exp instanceof BINOP && ((BINOP) dst.exp).binop == BINOP.MUL
                && ((BINOP) dst.exp).right instanceof CONST) {
            //MOVE(MEM(BINOP(MUL,e1,CONST(i))),e2)
            munchExp(((BINOP) dst.exp).left);
            munchExp(src);
            emit(new OperationInstruction("	st	", dst.toString().replace("\n", "")));

        } else if (dst.exp instanceof BINOP && ((BINOP) dst.exp).binop == BINOP.MUL
                && ((BINOP) dst.exp).left instanceof CONST) {
            munchExp(((BINOP) dst.exp).right);
            munchExp(src);
            emit(new OperationInstruction("	st	", dst.toString().replace("\n", "")));

        } else if (src instanceof MEM) {
            //MOVE(MEM(e1),MEM(e2)) I suppose this should never happen in SPARC
            //not sure thoug 
            NameOfTemp left = munchExp(dst.exp);
            NameOfTemp exp = munchExp(((MEM) src).exp);

            String leftS = left.toString();
            String expS = exp.toString();

            if (Main.tr.frame.containsKey(classMethod + expS)) {
                expS = (String) Main.tr.frame.get(classMethod + expS);
            }

            if (Main.tr.frame.containsKey(classMethod + leftS)) {
                leftS = (String) Main.tr.frame.get(classMethod + leftS);
            }

            if (Main.tr.frame.containsKey(className + expS)) {
                expS = (String) Main.tr.frame.get(className + expS);
            }

            if (Main.tr.frame.containsKey(className + leftS)) {
                leftS = (String) Main.tr.frame.get(className + leftS);
            }
            emit(new OperationInstruction("	st	" + expS + ",[" + leftS + "]", "MEM[" + leftS + "] := " + expS, left, exp));

            //MOVE(MEM(e1),e2)
        } else if (dst.exp instanceof TEMP && src instanceof CONST) {
            NameOfTemp left = munchExp(dst.exp);
            NameOfTemp exp = munchExp(src);

            String leftS = left.toString();
            String expS = exp.toString();

            if (Main.tr.frame.containsKey(classMethod + leftS)) {
                leftS = (String) Main.tr.frame.get(classMethod + leftS);
            }

            if (Main.tr.frame.containsKey(className + leftS)) {
                leftS = (String) Main.tr.frame.get(className + leftS);
            }

            emit(new OperationInstruction("	st	" + expS + ",[" + leftS + "]", "MEM[" + leftS + "] := " + exp, left, exp));

        } else if (dst.exp instanceof TEMP && src instanceof TEMP) {
            NameOfTemp left = munchExp(dst.exp);
            NameOfTemp exp = munchExp(src);

            String leftS = left.toString();
            String expS = exp.toString();

            if (Main.tr.frame.containsKey(classMethod + expS)) {
                expS = (String) Main.tr.frame.get(classMethod + expS);
                if (expS.contains("fp")) {
                    exp = localLoad(expS);
                    expS = exp.toString();
                }
            }

            if (Main.tr.frame.containsKey(className + expS)) {
                expS = (String) Main.tr.frame.get(className + expS);
                if (expS.contains("i0")) {
                    exp = localLoad(expS);
                    expS = exp.toString();
                }
            }

            if (Main.tr.frame.containsKey(classMethod + leftS)) {
                leftS = (String) Main.tr.frame.get(classMethod + leftS);
            }

            if (Main.tr.frame.containsKey(className + leftS)) {
                leftS = (String) Main.tr.frame.get(className + leftS);
            }

            emit(new OperationInstruction("	st	" + expS + ",[" + leftS + "]", "MEM[" + leftS + "] := " + exp, left, exp));
        } else {
            NameOfTemp left = munchExp(dst.exp);
            NameOfTemp exp = munchExp(src);

            String leftS = left.toString();
            String expS = exp.toString();

            if (Main.tr.frame.containsKey(classMethod + expS)) {
                expS = (String) Main.tr.frame.get(classMethod + expS);
            }

            if (Main.tr.frame.containsKey(classMethod + leftS)) {
                leftS = (String) Main.tr.frame.get(classMethod + leftS);
            }

            if (Main.tr.frame.containsKey(className + expS)) {
                expS = (String) Main.tr.frame.get(className + expS);
            }

            if (Main.tr.frame.containsKey(className + leftS)) {
                leftS = (String) Main.tr.frame.get(className + leftS);
            }

            //
            emit(new OperationInstruction("	st	" + expS + ",[" + leftS + "]", "MEM[" + left + "] := " + exp, left, exp));
        }

    }

    //MOVE(TEMP(t1),e) -> code for all calls and moves here
    void munchMove(TEMP dst, Exp src) {

        NameOfTemp templab = munchExp(dst);
        String tempS = templab.toString();
        //munchExp(src);
        if (src instanceof CALL) {
            //move the output into a register
            NameOfTemp regLength = munchExp(src);
            String regl = regLength.toString();
            if (Main.tr.frame.containsKey(classMethod + regl)) {
                regl = (String) Main.tr.frame.get(classMethod + regl);
            }
            if (Main.tr.frame.containsKey(className + regl)) {
                regl = (String) Main.tr.frame.get(className + regl);

            }

            if (((CALL) src).func.toString().contains("alloc_array")) {

                if (Main.tr.frame.containsKey(classMethod + tempS)) {
                    tempS = (String) Main.tr.frame.get(classMethod + tempS);
                }
                if (Main.tr.frame.containsKey(className + tempS)) {
                    tempS = (String) Main.tr.frame.get(className + tempS);

                }
                emit(new OperationInstruction("	st	%o0,[" + tempS + "]",
                        "MEM[" + tempS + "] := " + tempS, templab));
                emit(new OperationInstruction("	st	" + regl + ",[%o0]",
                        "MEM[%0] := " + regl + " lenght of array", regLength));

            } else {

                emit(new OperationInstruction("	mov	%o0," + tempS,
                        templab + " := " + tempS, templab));

            }

        } else if (templab.toString().contains("%i0")) {
            //this is for the return statement
            NameOfTemp nRet = munchExp(src);
            String nRetS = nRet.toString();
            if (Main.tr.frame.containsKey(classMethod + nRet.toString())) {
                nRetS = (String) Main.tr.frame.get(classMethod + nRetS);
                if (nRetS.contains("fp")) { //we know its a local access
                    //we should subtract the value offset
                    String offset = nRetS.substring(4);
                    NameOfTemp ret = localMemSubtract(offset);
                    nRet = localLoad(ret);
                    nRetS = nRet.toString();
                }
            }
            if (Main.tr.frame.containsKey(className + nRet.toString())) {
                nRetS = (String) Main.tr.frame.get(className + nRetS);
                if (nRetS.contains("i0")) { //we know its a local access
                    //we should subtract the value offset
                    String offset = nRetS.substring(4);
                    NameOfTemp ret = localMemAdd(offset);
                    nRet = localLoad(ret);
                    nRetS = nRet.toString();
                }
            }
            emit(new OperationInstruction("	mov	" + nRetS + ",%i0", "return " + nRet, nRet));

        } else {
            NameOfTemp expSrclab = munchExp(src);
            String expSrclabS = expSrclab.toString();
            //NameOfTemp load = NameOfTemp.generateTemp();
            if (Main.tr.frame.containsKey(classMethod + expSrclab.toString())) {
                expSrclabS = (String) Main.tr.frame.get(classMethod + expSrclab.toString());
                if (expSrclabS.contains("fp")) { //we know its a local access
                    //we should subtract the value offset
                    String offset = expSrclabS.substring(4);
                    expSrclab = localMemSubtract(offset);
                    expSrclabS = expSrclab.toString();
                }

            }

            if (Main.tr.frame.containsKey(className + expSrclab.toString())) {
                expSrclabS = (String) Main.tr.frame.get(className + expSrclab.toString());
                if (expSrclabS.contains("i0")) { //we know its a local access
                    //we should subtract the value offset
                    String offset = expSrclabS.substring(4);
                    expSrclab = localMemAdd(offset);
                    expSrclabS = expSrclab.toString();
                }

            }

            emit(new MoveInstruction("	mov	" + expSrclabS + "," + templab,
                    templab + " := " + expSrclabS,
                    templab,
                    expSrclab));
        }
    }

    tree.NameOfTemp localLoad(tree.NameOfTemp n) {
        NameOfTemp r = NameOfTemp.generateTemp();
        emit(new OperationInstruction("	ld	[" + n.toString() + "]," + r, r + " := [" + n + "]", r, n));
        return r;
    }

    tree.NameOfTemp localLoad(String n) {
        NameOfTemp r = NameOfTemp.generateTemp();
        emit(new OperationInstruction("	ld	[" + n + "]," + r, r + " := [" + n + "]", r));
        return r;
    }

    tree.NameOfTemp localMemSubtract(String offset) {
        NameOfTemp r = NameOfTemp.generateTemp();
        emit(new OperationInstruction("	sub	%fp," + offset + "," + r, r + " := [" + offset + "]", r));
        return r;
    }

    tree.NameOfTemp localMemAdd(String offset) {
        NameOfTemp r = NameOfTemp.generateTemp();
        emit(new OperationInstruction("	add	%i0," + offset + "," + r, r + " := [" + offset + "]", r));
        return r;
    }

    tree.NameOfTemp munchExp(tree.Exp e
    ) {

        if (e instanceof MEM) {
            //   all possible combinations of MEM
            if (((MEM) e).exp instanceof BINOP) {
                // all possible combinations for BINOP
                if (((BINOP) ((MEM) e).exp).binop == BINOP.PLUS) {
                    //all possible combinations for PLUS
                    if (((BINOP) ((MEM) e).exp).left instanceof BINOP) {
                        NameOfTemp posM = munchExp(((BINOP) ((BINOP) ((BINOP) ((MEM) e).exp).left).left).left);
                        String right = posM.toString();
                        NameOfTemp mul = NameOfTemp.generateTemp();
                        NameOfTemp sum = NameOfTemp.generateTemp();

                        if (Main.tr.frame.containsKey(classMethod + right)) {
                            right = (String) Main.tr.frame.get(classMethod + right);
                            if (right.contains("fp")) {
                                posM = localLoad(right);
                                right = posM.toString();
                            }
                        }
                        if (Main.tr.frame.containsKey(className + right)) {
                            right = (String) Main.tr.frame.get(className + right);
                            if (right.contains("i0")) {
                                posM = localLoad(right);
                                right = posM.toString();
                            }
                        }
                        emit(new OperationInstruction("	add	" + right + ",1," + sum, sum + " := " + sum + " + 1 ", posM, sum));
                        emit(new OperationInstruction("	smul	" + sum + ",4," + mul, mul + " := " + sum + " * 4", sum, mul));
                        NameOfTemp ad = NameOfTemp.generateTemp();
                        String temp = ((TEMP) ((MEM) ((BINOP) ((MEM) e).exp).right).exp).temp.toString();
                        if (Main.tr.frame.containsKey(classMethod + temp)) {
                            temp = (String) Main.tr.frame.get(classMethod + temp);
                        }

                        if (Main.tr.frame.containsKey(className + temp)) {
                            temp = (String) Main.tr.frame.get(className + temp);
                        }
                        emit(new OperationInstruction("	ld	[" + temp + "]," + ad, ad + ":= MEM[" + temp + "]", ad));
                        NameOfTemp r = NameOfTemp.generateTemp();
                        emit(new OperationInstruction("	add	" + ad + "," + mul + "," + r, r + " := " + mul + " + " + ad, mul, ad, r));
                        return r;

                    }
                }
            }
//                    //END MEM PLUS
//                } else 

//                    if (((BINOP) ((MEM) e).exp).binop == BINOP.MINUS) {
//                    //all possible combinations for MINUS
//                    if (((BINOP) ((MEM) e).exp).left instanceof CONST) {
//                        NameOfTemp r = NameOfTemp.generateTemp();
//                        String lb = munchExp(((BINOP) ((MEM) e).exp).right).toString();
//
//                        if (Main.tr.frame.containsKey(classMethod + lb)) {
//                            lb = (String) Main.tr.frame.get(classMethod + lb);
//                        }
//
//                        emit(new OperationInstruction("	ld	" + r + ",[" + lb + "]",
//                                "ld :" + ((CONST) ((BINOP) ((MEM) e).exp).left).value,
//                                r, munchExp(((BINOP) ((MEM) e).exp).right)));
//                        return r;
//                    } else {
//                        NameOfTemp r = NameOfTemp.generateTemp();
//                        NameOfTemp expM = munchExp(((BINOP) ((MEM) e).exp).left);
//                        String left = expM.toString();
//                        if (Main.tr.frame.containsKey(classMethod + left)) {
//                            left = (String) Main.tr.frame.get(classMethod + left);
//                        }
//                        emit(new OperationInstruction("	ld	[" + left + "]," + r,
//                                r + " := MEM[" + left + "]", r, expM));
//                        return r;
//
//                    }
//                    //END MEM MINUS
//                } else 
//                        if (((BINOP) ((MEM) e).exp).binop == BINOP.MUL) {
//                    //all possible combinations for MUL
//                    if (((BINOP) ((MEM) e).exp).left instanceof CONST) {
//                        NameOfTemp r = NameOfTemp.generateTemp();
//                        emit(new OperationInstruction("	ld " + r,
//                                "ld :" + ((CONST) ((BINOP) ((MEM) e).exp).left).value,
//                                r, munchExp(((BINOP) ((MEM) e).exp).right)));
//                        return r;
//                    } else {
//                        NameOfTemp r = NameOfTemp.generateTemp();
//                        emit(new OperationInstruction("	ld	" + r,
//                                "ld :" + ((CONST) ((BINOP) ((MEM) e).exp).right).value,
//                                r, munchExp(((BINOP) ((MEM) e).exp).left)));
//                        return r;
//                    }
//                } //end MEM MUL
            //END MEM BINOP (what about LESS THAN)
//            } else 
            if (((MEM) e).exp instanceof CONST) {
                //MEM CONST
                NameOfTemp r = NameOfTemp.generateTemp();
                emit(new OperationInstruction("	ld	" + r,
                        "ld" + ((CONST) ((MEM) e).exp).value, r));
                return r;
                //END MEM CONST
            } else if (e instanceof MEM) {
                //ALL OTHER MEM EXP CASES HANDLED HERE
                MEM expM = (MEM) e;

                NameOfTemp r = NameOfTemp.generateTemp();
                NameOfTemp expL = munchExp(expM.exp);
                String left = expL.toString();

                if (Main.tr.frame.containsKey(classMethod + left)) {
                    left = (String) Main.tr.frame.get(classMethod + left);
                    if (left.contains("fp")) {
                        expL = localLoad(left);
                        left = expL.toString();
                    }
                }

                if (Main.tr.frame.containsKey(className + left)) {
                    left = (String) Main.tr.frame.get(className + left);
                    if (left.contains("i0")) {
                        expL = localLoad(left);
                        left = expL.toString();
                    }
                }

                emit(new OperationInstruction("	ld	[" + left + "]," + r,
                        r + " := MEM[" + left + "]", r, expL));
                return r;
            }
        } else if (e instanceof BINOP) {
            //check all BINOPS
            if (((BINOP) e).binop == BINOP.PLUS) {
                //check all BINOPS PLUS
                if (((BINOP) e).right instanceof CONST) {
                    NameOfTemp r = NameOfTemp.generateTemp();
                    NameOfTemp expL = munchExp(((BINOP) e).left);
                    String left = expL.toString();
                    String value = Integer.toString(((CONST) ((BINOP) e).right).value);
                    if (Main.tr.frame.containsKey(classMethod + left)) {
                        left = (String) Main.tr.frame.get(classMethod + left);
                        if (left.contains("fp")) {
                            expL = localLoad(left);
                            left = expL.toString();
                        }
                    }

                    if (Main.tr.frame.containsKey(className + left)) {
                        left = (String) Main.tr.frame.get(className + left);
                        if (left.contains("i0")) {
                            expL = localLoad(left);
                            left = expL.toString();
                        }
                    }

                    emit(new OperationInstruction("	add	"
                            + left
                            + "," + value + "," + r,
                            expL + " + " + value, r, expL
                    ));
                    return r;
                } else if (((BINOP) e).left instanceof CONST) {
                    NameOfTemp r = NameOfTemp.generateTemp();
                    NameOfTemp expR = munchExp(((BINOP) e).right);
                    String right = expR.toString();
                    String value = Integer.toString(((CONST) ((BINOP) e).left).value);

                    if (Main.tr.frame.containsKey(classMethod + right)) {
                        right = (String) Main.tr.frame.get(classMethod + right);
                        if (right.contains("fp")) {
                            expR = localLoad(right);
                            right = expR.toString();
                        }
                    }
                    if (Main.tr.frame.containsKey(className + right)) {
                        right = (String) Main.tr.frame.get(className + right);
                        if (right.contains("i0")) {
                            expR = localLoad(right);
                            right = expR.toString();
                        }
                    }
                    emit(new OperationInstruction("	add	"
                            + value
                            + "," + right + "," + r,
                            r + " := " + value + " + " + expR,
                            r, expR
                    ));
                    return r;
                } else {
                    //both sides are expressions
                    NameOfTemp r = NameOfTemp.generateTemp();

                    NameOfTemp expR = munchExp(((BINOP) e).right);
                    String right = expR.toString();
                    NameOfTemp expL = munchExp(((BINOP) e).left);
                    String left = expL.toString();

                    if (Main.tr.frame.containsKey(classMethod + right)) {
                        right = (String) Main.tr.frame.get(classMethod + right);
                        if (right.contains("fp")) {
                            expR = localLoad(right);
                            right = expR.toString();
                        }
                    }
                    if (Main.tr.frame.containsKey(classMethod + left)) {
                        left = (String) Main.tr.frame.get(classMethod + left);
                        if (left.contains("fp")) {
                            expL = localLoad(left);
                            left = expL.toString();
                        }
                    }

                    if (Main.tr.frame.containsKey(className + right)) {
                        right = (String) Main.tr.frame.get(className + right);
                        if (right.contains("i0")) {
                            expR = localLoad(right);
                            right = expR.toString();
                        }
                    }
                    if (Main.tr.frame.containsKey(className + left)) {
                        left = (String) Main.tr.frame.get(className + left);
                        if (left.contains("i0")) {
                            expL = localLoad(left);
                            left = expL.toString();
                        }
                    }

                    emit(new OperationInstruction("	add	" + left + "," + right + "," + r,
                            r + " := " + left + " + " + right, expL, expR, r)
                    );
                    return r;
                }
                //END BINOP PLUS
            } else if (((BINOP) e).binop == BINOP.MINUS) {
                //check all BINOPS MINUS
                if (((BINOP) e).right instanceof CONST) {
                    NameOfTemp r = NameOfTemp.generateTemp();
                    NameOfTemp expL = munchExp(((BINOP) e).left);
                    String left = expL.toString();
                    String value = Integer.toString(((CONST) ((BINOP) e).right).value);
                    if (Main.tr.frame.containsKey(classMethod + left)) {
                        left = (String) Main.tr.frame.get(classMethod + left);
                        if (left.contains("fp")) {
                            expL = localLoad(left);
                            left = expL.toString();
                        }
                    }

                    if (Main.tr.frame.containsKey(className + left)) {
                        left = (String) Main.tr.frame.get(className + left);
                        if (left.contains("i0")) {
                            expL = localLoad(left);
                            left = expL.toString();
                        }
                    }

                    emit(new OperationInstruction("	sub	"
                            + left
                            + "," + value + "," + r,
                            expL + " - " + value, r, expL
                    ));
                    return r;
                } else if (((BINOP) e).left instanceof CONST) {
                    NameOfTemp r = NameOfTemp.generateTemp();
                    NameOfTemp expR = munchExp(((BINOP) e).right);
                    String right = expR.toString();
                    String value = Integer.toString(((CONST) ((BINOP) e).left).value);

                    if (Main.tr.frame.containsKey(classMethod + right)) {
                        right = (String) Main.tr.frame.get(classMethod + right);
                        if (right.contains("fp")) {
                            expR = localLoad(right);
                            right = expR.toString();
                        }
                    }

                    if (Main.tr.frame.containsKey(className + right)) {
                        right = (String) Main.tr.frame.get(className + right);
                        if (right.contains("i0")) {
                            expR = localLoad(right);
                            right = expR.toString();
                        }
                    }
                    NameOfTemp noRigt = NameOfTemp.generateTemp();
                    emit(new OperationInstruction("	set	" + value + "," + noRigt, noRigt, noRigt));
                    emit(new OperationInstruction("	sub	"
                            + noRigt
                            + "," + right + "," + r,
                            r + " := " + noRigt + " - " + expR,
                            r, expR, noRigt
                    ));
                    return r;
                } else {
                    //both sides are expressions
                    NameOfTemp r = NameOfTemp.generateTemp();

                    NameOfTemp expR = munchExp(((BINOP) e).right);
                    String right = expR.toString();
                    NameOfTemp expL = munchExp(((BINOP) e).left);
                    String left = expL.toString();

                    if (Main.tr.frame.containsKey(classMethod + right)) {
                        right = (String) Main.tr.frame.get(classMethod + right);
                        if (right.contains("fp")) {
                            expR = localLoad(right);
                            right = expR.toString();
                        }
                    }
                    if (Main.tr.frame.containsKey(classMethod + left)) {
                        left = (String) Main.tr.frame.get(classMethod + left);
                        if (left.contains("fp")) {
                            expL = localLoad(left);
                            left = expL.toString();
                        }
                    }

                    if (Main.tr.frame.containsKey(className + right)) {
                        right = (String) Main.tr.frame.get(className + right);
                        if (right.contains("i0")) {
                            expR = localLoad(right);
                            right = expR.toString();
                        }
                    }
                    if (Main.tr.frame.containsKey(className + left)) {
                        left = (String) Main.tr.frame.get(className + left);
                        if (left.contains("i0")) {
                            expL = localLoad(left);
                            left = expL.toString();
                        }
                    }

                    emit(new OperationInstruction("	sub	" + left + "," + right + "," + r,
                            r + " := " + left + " - " + right, expL, expR, r)
                    );
                    return r;
                }
                //END BINOP MINUS
            } else if (((BINOP) e).binop == BINOP.MUL) {
                //check all BINOPS PLUS
                if (((BINOP) e).right instanceof CONST) {
                    NameOfTemp r = NameOfTemp.generateTemp();
                    NameOfTemp expL = munchExp(((BINOP) e).left);
                    String left = expL.toString();
                    String value = Integer.toString(((CONST) ((BINOP) e).right).value);
                    if (Main.tr.frame.containsKey(classMethod + left)) {
                        left = (String) Main.tr.frame.get(classMethod + left);
                        if (left.contains("fp")) {
                            expL = localLoad(left);
                            left = expL.toString();
                        }
                    }

                    if (Main.tr.frame.containsKey(className + left)) {
                        left = (String) Main.tr.frame.get(className + left);
                        if (left.contains("i0")) {
                            expL = localLoad(left);
                            left = expL.toString();
                        }
                    }

                    emit(new OperationInstruction("	smul	"
                            + left
                            + "," + value + "," + r,
                            expL + " * " + value, r, expL
                    ));
                    return r;
                } else if (((BINOP) e).left instanceof CONST) {
                    NameOfTemp r = NameOfTemp.generateTemp();
                    NameOfTemp expR = munchExp(((BINOP) e).right);
                    String right = expR.toString();
                    String value = Integer.toString(((CONST) ((BINOP) e).left).value);

                    if (Main.tr.frame.containsKey(classMethod + right)) {
                        right = (String) Main.tr.frame.get(classMethod + right);
                        if (right.contains("fp")) {
                            expR = localLoad(right);
                            right = expR.toString();
                        }
                    }

                    if (Main.tr.frame.containsKey(className + right)) {
                        right = (String) Main.tr.frame.get(className + right);
                        if (right.contains("i0")) {
                            expR = localLoad(right);
                            right = expR.toString();
                        }
                    }
                    emit(new OperationInstruction("	smul	"
                            + value
                            + "," + right + "," + r,
                            r + " := " + value + " * " + expR,
                            r, expR
                    ));
                    return r;
                } else {
                    //both sides are expressions
                    NameOfTemp r = NameOfTemp.generateTemp();

                    NameOfTemp expR = munchExp(((BINOP) e).right);
                    String right = expR.toString();
                    NameOfTemp expL = munchExp(((BINOP) e).left);
                    String left = expL.toString();

                    if (Main.tr.frame.containsKey(classMethod + right)) {
                        right = (String) Main.tr.frame.get(classMethod + right);
                        if (right.contains("fp")) {
                            expR = localLoad(right);
                            right = expR.toString();
                        }
                    }
                    if (Main.tr.frame.containsKey(classMethod + left)) {
                        left = (String) Main.tr.frame.get(classMethod + left);
                        if (left.contains("fp")) {
                            expL = localLoad(left);
                            left = expL.toString();
                        }
                    }

                    if (Main.tr.frame.containsKey(className + right)) {
                        right = (String) Main.tr.frame.get(className + right);
                        if (right.contains("i0")) {
                            expR = localLoad(right);
                            right = expR.toString();
                        }
                    }
                    if (Main.tr.frame.containsKey(className + left)) {
                        left = (String) Main.tr.frame.get(className + left);
                        if (left.contains("i0")) {
                            expL = localLoad(left);
                            left = expL.toString();
                        }
                    }

                    emit(new OperationInstruction("	smul	" + left + "," + right + "," + r,
                            r + " := " + left + " * " + right, expL, expR, r)
                    );
                    return r;
                }
            }
        } else if (e instanceof CONST) { //never to be reached
            NameOfTemp r = NameOfTemp.generateTemp();
            //Main.tr.frame.put(r.toString(), r);
            emit(new OperationInstruction("	set	" + ((CONST) e).value + "," + r, "set " + r + " := " + ((CONST) e).value, r));
            return r;
        } else if (e instanceof TEMP) {
            return ((TEMP) e).temp;
        } else if (e instanceof CALL) { //this can only be a print
            //simpleAlloc(false);
            CALL c = (CALL) e;
            ExpList args = c.args;
            int i = 0;
            NameOfTemp nTemp = null;
            emit(new Comment("	prepare to call subprocedure '" + ((NAME) c.func).label.toString() + "'"));
            if (args != null) {
                if (args.head instanceof CONST) {
                    nTemp = munchExp(new CONST(((CONST) args.head).value));
                }
            }
            while (args != null) {
                if (args.head instanceof CONST) {
                    emit(
                            new OperationInstruction(
                                    "	set	" + ((CONST) args.head).value + ", %o" + i,
                                    "%o" + i + " := " + ((CONST) args.head).value));
                    args = args.tail;
                    i++;
                } else {
                    nTemp = munchExp(args.head);
                    String temp = nTemp.toString();
                    if (Main.tr.frame.containsKey(classMethod + temp)) {
                        temp = (String) Main.tr.frame.get(classMethod + temp);
                        if (temp.contains("fp")) {
                            nTemp = localLoad(temp);
                            temp = nTemp.toString();
                        }
                    }
                    if (Main.tr.frame.containsKey(className + temp)) {
                        temp = (String) Main.tr.frame.get(className + temp);
                        if (temp.contains("i0")) {
                            nTemp = localLoad(temp);
                            temp = nTemp.toString();
                        }
                    }
                    emit(new OperationInstruction("	mov	" + temp + ", %o" + i,
                            "%o" + i + " := " + temp, nTemp));
                    args = args.tail;
                    i++;
                }
            }
            emit(new OperationInstruction("	call	" + ((NAME) c.func).label.toString()));
            emit(new OperationInstruction(nopInstr, nopComment));
            return nTemp;
        } else {
            emit(new Comment("	" + e.toString().replace("\n", "") + ": not implemented yet"));
            return NameOfTemp.generateTemp("Not Implemented yet");
        }
        return null;
    }

    void munchLabel(Stm s) {
        //emit(new Comment())
        final String lab = ((LABEL) s).label.toString();
//        String[] lab = ((LABEL)s).label.toString().split("\\$");
//        emit(new LabelInstruction(lab[0]+"$"+lab[1], null));
        emit(new LabelInstruction(lab + ":", ((LABEL) s).label));
    }

    void munchMove(Exp dst, Exp src
    ) {
        //MOVE (d,e)
        if (dst instanceof MEM) {
            munchMove((MEM) dst, src);
        } else if (dst instanceof TEMP) {
            munchMove((TEMP) dst, src);
        }
    }
    //this is the first munch stm which receives all the items of the list
    //of statements build by the CANON package

    public void munchStm(Stm s) {
        //MOVE
        if (s instanceof MOVE) {
            munchMove(((MOVE) s).dst, ((MOVE) s).src);
        } else if (s instanceof CJUMP) {
            emit(new Comment("conditional jump"));
            CJUMP cj = (CJUMP) s;

            NameOfTemp expL = munchExp(cj.left);
            String left = expL.toString();
            NameOfTemp expR = munchExp(cj.right);
            String right = expR.toString();

            if (Main.tr.frame.containsKey(classMethod + right)) {
                right = (String) Main.tr.frame.get(classMethod + right);
                if (right.contains("fp")) {
                    expR = localLoad(right);
                    right = expR.toString();
                }
            }
            if (Main.tr.frame.containsKey(classMethod + left)) {
                left = (String) Main.tr.frame.get(classMethod + left);
                if (left.contains("fp")) {
                    expL = localLoad(left);
                    left = expL.toString();
                }
            }

            if (Main.tr.frame.containsKey(className + right)) {
                right = (String) Main.tr.frame.get(className + right);
                if (right.contains("i0")) {
                    expR = localLoad(right);
                    right = expR.toString();
                }
            }
            if (Main.tr.frame.containsKey(className + left)) {
                left = (String) Main.tr.frame.get(className + left);
                if (left.contains("i0")) {
                    expL = localLoad(left);
                    left = expL.toString();
                }
            }

            String branch = "bl";

            if (cj.relop == CJUMP.GE) {
                branch = "bge";
            }

            emit(new OperationInstruction("	cmp	" + left + "," + right, "compare " + left + " and " + right, expL, expR));
            emit(new OperationInstruction("	" + branch + "	" + cj.iftrue, " branch true to " + cj.iftrue));
            emit(new OperationInstruction(nopInstr, nopComment));

        } else if (s instanceof EVAL) {
            //statement that CALLS prints
            //register = register + 1;
            munchExp(((EVAL) s).exp);
        } else if (s instanceof JUMP) {
            //register = register + 1;
            emit(new OperationInstruction("	ba	" + ((NAME) ((JUMP) s).exp).label.toString(), "unconditional GOTO"));
            emit(new OperationInstruction(nopInstr, nopComment));
        } else if (s instanceof LABEL) {

            munchLabel(s);

        }

    }
}
