package parser;

import assem.Instruction;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sparc.Codegen;
import sparc.RegisterAlloc;
import translate.Translator;
import tree.LABEL;
import tree.Stm;
import tree.TreePrint;

public class Main {
    
    public static HashMap classes = new HashMap();
    public static Translator tr = new Translator();
    //ErrorHandler Object is has public methods shared between
    //Main and javacc generated classes to count errors and print formatted error
    //messages
    public static ErrorHandler er = new ErrorHandler();
    
    public static void main(String[] args) {
        //This constant controls the enable tracing
        //In future versions: use a Command Line parser lib to set this options
        final boolean tracing = false;
        
        List<List<Stm>> methodList = new ArrayList<>();

        //File where the output of enable_tracing() will go
        final String fileTracing = "fileTracing.txt";

        //Set filename in for correct error message display
        er.setFilename(args[0]);

        //File containing the code from command line arguments
        File filename = new File(args[0]);
        try {
            final MiniJavaScanner parserTc = new MiniJavaScanner(new FileInputStream(filename));
            final MiniJavaScanner parserSt = new MiniJavaScanner(new FileInputStream(filename));
            final MiniJavaScanner parserIr = new MiniJavaScanner(new FileInputStream(filename));

            //Controling tracing option
            if (!tracing) {
                parserTc.disable_tracing();
                parserSt.disable_tracing();
                parserIr.disable_tracing();
            }
            SymbolTable st = new SymbolTable();
            TypeCheck tc = new TypeCheck();
            st.visit(parserSt.Target());
            // System.out.println(classes);

            tc.visit(parserTc.Target());
            //If no errors where foundduring the lexical, synctactical and semantical 
            //analysis, then we build the IR trees.
            if (Main.er.getCounter() == 0) {

                //System.setOut(new PrintStream(new FileOutputStream(fileTracing)));
                tr.visit(parserIr.Target());
                PrintWriter pw = new PrintWriter(System.out);
                TreePrint tp = new TreePrint(pw);

                //tp.print(Translator.lseq);
                //Sets the output back to the standard output
                //Called after the setOut to print to a file
                final TreePrint p = new TreePrint(System.out);
                
                Translator.lseq.stream().forEach((lseq) -> {
                    List<Stm> traces = new ArrayList<>();
                    traces.addAll(canon.Main.transform(lseq));
                    methodList.add(traces);
                });

//                System.out.println("\nAfter trace scheduling");
//                methodList.stream().forEach((methodList1) -> {
//                    p.print(methodList1);
//                });
//                
                List<List<assem.Instruction>> ilist = new ArrayList<>();
                methodList.stream().map((traces) -> {
                    String[] methodNames = ((LABEL) traces.get(0)).label.toString().split("\\$");
                    Codegen gen = new Codegen(methodNames[0]+".",methodNames[0]+"."+methodNames[1]+".");
                    traces.stream().forEach((stm) -> {
                        gen.munchStm(stm);
                    });
                    return gen;
                }).forEach((gen) -> {
                    ilist.add(gen.getIlist());
                });
                //print temps
//                PrintWriter pwtemp = new PrintWriter(filename.getName().replace(".java", "") + "_temp.s");
//                ilist.stream().forEach((listAssembly) -> {
//                    listAssembly.forEach((instr) -> {
//                        pwtemp.print(instr.format() + " := ");
//                        pwtemp.println(instr.temps());
//                    });
//                });
//                pwtemp.flush();

                //start proper assembly
                final String headerMain = "! Author: Josemar Faustino da Cruz\n"
                        + ".file	\"" + filename.getName() + "\"\n"
                        + ".section	\".text\"\n"
                        + ".align 4\n"
                        + ".global main\n"
                        + ".proc	04\n"
                        + "main:";
                
                PrintWriter pwAssemb = new PrintWriter(filename.getName().replace(".java", "") + ".s");
                pwAssemb.println(headerMain);
                
                RegisterAlloc regAll = new RegisterAlloc();
                regAll.alloc(ilist.get(0));
                List<Instruction> llist = regAll.getAllocated();
                pwAssemb.println(llist.get(0).format());
                
                String[] epilogEnd = llist.get(0).assem.split("\\$");
                for (int k = 1; k < llist.size(); k++) {
                    pwAssemb.println(llist.get(k).format());
                }
                pwAssemb.println(epilogEnd[0] + "$" + epilogEnd[1] + "$epilogBegin:\n"
                        + "        exit_program 	! [my macro]");
                pwAssemb.flush();
                for (int i = 1; i < ilist.size(); i++) {
                    RegisterAlloc regAllMethod = new RegisterAlloc();
                    regAllMethod.alloc(ilist.get(i));
                    List<Instruction> slist = regAllMethod.getAllocated();
                    String[] namePrelude = slist.get(0).assem.split("\\$");
                    
                    pwAssemb.println(namePrelude[0] + "$" + namePrelude[1] + "$1:\n"
                            + "        save 	%sp, -400, %sp");
                    
                    for (int j = 0; j < slist.size(); j++) {
                        pwAssemb.println(slist.get(j).format());
                    }
                    
                    pwAssemb.println(namePrelude[0] + "$" + namePrelude[1] + "$epilogBegin:\n"
                            + "        ret 		! return from Fac$ComputeFac$1\n"
                            + "        restore 	! (in the delay slot)");
                    
                }
                pwAssemb.flush();
                
            }
            
        } catch (Exception e) {
            System.err.println(e);
        }
        //Prints total number of identified errors
        System.out.println(args[0] + " Total Errors = " + er.getCounter());
    }
}
