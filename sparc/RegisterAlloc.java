/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sparc;

import assem.Instruction;
import assem.OperationInstruction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import tree.NameOfTemp;

/**
 *
 * @author cruzj2012
 */
public class RegisterAlloc {

    private List<String> temps = new ArrayList<>();
    private final List<Instruction> allocated = new ArrayList<>();

    public List<Instruction> getAllocated() {
        return allocated;
    }
    HashMap<String, Object> lives = new HashMap<>();
    private final int availRegs = 7; //Total number of registers in Sparc 
    

    public void alloc(List<Instruction> instr) {
        int reg = 0;
        instr.stream().forEach((assembly) -> {
            if (!(assembly.temps().isEmpty())) {
                Set<NameOfTemp> np = assembly.temps();
                assembly.temps().stream().forEach((temp) -> {
                    if (temp.toString().contains("t0") || temp.toString().contains("t1")) {
                        temps.add(temp.toString());
                    }
                });

            }
        });

        instr.stream().forEach((inst) -> {
            if (inst.temps().isEmpty()) {
                allocated.add(inst);
            } else {
                List<String> allTemps = temps(inst.temps());
                String oldAss = inst.assem;
                for(String tmp:allTemps){
                    //checks if that temp is already in Live, means was needed in past
                    if (lives.containsKey(tmp)) {
                        int cont = (Integer)lives.get(tmp);
                        String register = "%l" + cont;
                        oldAss = oldAss.replace(tmp, register);
                        //temp is removed from list of temps for this method
                        temps.remove(tmp);
                        //if temp is not needed in future, then its removed
                        if (!temps.contains(tmp)) {
                            lives.remove(tmp);
                        }
                        //if temp was not needed, then it should be added now
                    } else {
                        //first we find the smallest register available
                        int min = -1;
                        for (int i = availRegs; i >= 0; i--) {
                            if (!lives.containsValue(i)) {
                                min = i;
                            }
                        }
                        //show error if run out of registers
                        if (min < 0) {
                            System.err.println("Compilation Error: "
                                    + "runned out of registers");
                        }
                        String register = "%l" + min;
                        oldAss = oldAss.replace(tmp, register);
                        temps.remove(tmp);
                        if(temps.contains(tmp)){
                            lives.put(tmp, min);
                        }
                    }
                    

                }
                allocated.add(new OperationInstruction(oldAss,inst.comment));
            }
        });
    }

    List<String> temps(Set<NameOfTemp> set) {
        List<String> temps = new ArrayList<>();
        set.stream().forEach((st) -> {
            if (st.toString().contains("t0") | st.toString().contains("t1")) {
                temps.add(st.toString());
            }
        });
        return temps;
    }
}
