package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import javax.tools.JavaFileManager;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.sort;

public class JasminInstruction {
    private HashMap<String, Descriptor> varTable;
    private Method method;
    private ClassUnit classUnit;
    private int lastreg;
    private JasminUtils jasminUtils;

    JasminInstruction(ClassUnit classUnit, Method method)
    {
        this.classUnit = classUnit;
        this.method = method;
        method.buildVarTable();
        this.varTable = method.getVarTable();
        this.lastreg = this.getLastReg();
        this.jasminUtils = new JasminUtils(this.classUnit);
    }

    public int getLastReg() {

        var var2 = this.varTable.entrySet().iterator();
        ArrayList<Integer> allRegs = new ArrayList<>();

        while(var2.hasNext()) {
            Map.Entry<String, Descriptor> entry = (Map.Entry)var2.next();
            String key = (String)entry.getKey();
            Descriptor d1 = (Descriptor)entry.getValue();
            //System.out.println("\t\tVar name: " + key + " scope: " + d1.getScope() + " virtual register: " + d1.getVirtualReg());
            allRegs.add(d1.getVirtualReg());
        }
        sort(allRegs);

        return allRegs.get(allRegs.size()-1);
    }



    public String getCode(Instruction instruction){
        FunctionClassMap<Instruction, String> instructionMap = new FunctionClassMap<>();

        instructionMap.put(CallInstruction.class, this::getCode);
        instructionMap.put(PutFieldInstruction.class, this::getCode);
        instructionMap.put(GetFieldInstruction.class, this::getCode);
        instructionMap.put(AssignInstruction.class, this::getCode);
        instructionMap.put(ReturnInstruction.class, this::getCode);
        instructionMap.put(SingleOpInstruction.class, this::getCode);
        return instructionMap.apply(instruction);

        //throw new NotImplementedException(instruction.getInstType());

    }

    private String getCodeInvokeStatic(CallInstruction instruction)
    {
        var code = new StringBuilder();
        var methodClass = ((Operand) instruction.getFirstArg()).getName();
        var methodName = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");;

        code.append("invokestatic " +this.jasminUtils.getFullyQualifiedName(methodClass));
        code.append("/" + methodName + "(");

        //TODO-Exemplo
        var operandsTypes = instruction.getListOfOperands().stream()
                .map(element ->this.jasminUtils.getJasminType(element.getType()))
                .collect(Collectors.joining());
        code.append(operandsTypes).append(")").append(this.jasminUtils.getJasminType(instruction.getReturnType()) + '\n');

        return code.toString();

    }

    //TODO
    private String getCodeInvokeVirtual(CallInstruction instruction)
    {
        var code = new StringBuilder();

        var firstArg = ((Operand) instruction.getFirstArg());
        var secondArg = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");

        code.append("\t"+this.jasminUtils.loadElement(firstArg, this.varTable));

        for(Element element : instruction.getListOfOperands()){
            code.append("\t" +this.jasminUtils.loadElement(element, this.varTable) );

        }

        code.append("\tinvokevirtual " +this.jasminUtils. getJasminType(firstArg.getType()) +  "/" + secondArg + "(");

        var operandsTypes = instruction.getListOfOperands().stream()
                .map(element ->this.jasminUtils.getJasminType(element.getType()))
                .collect(Collectors.joining());
        code.append(operandsTypes).append(")").append(this.jasminUtils.getJasminType(instruction.getReturnType()) + '\n');

        return code.toString();

    }

    private String getCodeNewInstr(CallInstruction instruction)
    {
        var code = new StringBuilder();
        var firstArg = ((Operand) instruction.getFirstArg());

        //TODO
        //instruction.getListOfOperands();

        code.append("\tnew " + firstArg.getName() + "\n");
        code.append("\tdup\n");

        return code.toString();
    }

    private String getCodeInvokeSpecial(CallInstruction instruction)
    {
        var code = new StringBuilder();

        var firstArg = ((Operand) instruction.getFirstArg());
        var methodName = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");;

        code.append("\tinvokespecial " +this.jasminUtils.getJasminType(firstArg.getType()));
        code.append("/" + methodName + "(");

        //TODO-Exemplo
        var operandsTypes = instruction.getListOfOperands().stream()
                .map(element ->this.jasminUtils.getJasminType(element.getType()))
                .collect(Collectors.joining());

        code.append(operandsTypes).append(")").append(this.jasminUtils.getJasminType(instruction.getReturnType()) + '\n');
        code.append("\t" +this.jasminUtils.storeElement(firstArg, this.varTable, this.lastreg));

        return code.toString();


    }


    public String getCode(PutFieldInstruction instruction) {

        var code = new StringBuilder();

        var firstArg = ((Operand) instruction.getFirstOperand());
        var secondArg = instruction.getSecondOperand();
        String secondArgStr = "";
        var thirdArg = instruction.getThirdOperand();

        code.append("\t" + this.jasminUtils.loadElement(firstArg, this.varTable));
        code.append("\t" + this.jasminUtils.loadElement(thirdArg, this.varTable));

        code.append("\tputfield ");
        if (secondArg.isLiteral()) {
            secondArgStr = ((LiteralElement)secondArg).getLiteral();
        } else {
            var o1 = (Operand)secondArg;
            secondArgStr = o1.getName();

        }

        code.append(this.jasminUtils.getFieldSpecs(firstArg, secondArgStr) + " ");
        code.append(this.jasminUtils.getJasminType(thirdArg.getType()) + "\n");


        return code.toString();
    }

    public String getCode(GetFieldInstruction instruction) {

        var code = new StringBuilder();

        var firstArg = ((Operand) instruction.getFirstOperand());
        var secondArg = instruction.getSecondOperand();
        String secondArgStr = "";

        code.append("\t" + this.jasminUtils.loadElement(firstArg, this.varTable));

        code.append("\tgetfield ");
        if (secondArg.isLiteral()) {
            secondArgStr = ((LiteralElement)secondArg).getLiteral();
        } else {
            var o1 = (Operand)secondArg;
            secondArgStr = o1.getName();

        }

        code.append(this.jasminUtils.getFieldSpecs(firstArg, secondArgStr) + " ");
        code.append(this.jasminUtils.getJasminType(instruction.getFieldType()) + "\n");


        return code.toString();
    }

    public String getCode(ReturnInstruction instruction)
    {
        var code = new StringBuilder();

        if(!instruction.hasReturnValue())
            return "";

        code.append("\t" + this.jasminUtils.getJasminType(instruction.getOperand().getType()).toLowerCase());
        code.append("return\n");

        return code.toString();
    }

    public String getCode(AssignInstruction instruction)
    {
        var code = new StringBuilder();
        var o1 = (Operand) instruction.getDest();

        System.out.println("TYPE OF ASSIGN: " + instruction.getTypeOfAssign());

        code.append(getCode(instruction.getRhs()));

        //code.append("\t"+ this.jasminUtils.storeElement(instruction.getDest(), this.varTable, this.lastreg));
        //code.append("aload_0");
        //code.append(this.jasminUtils.storeElement(instruction.getRhs(), this.varTable, this.lastreg))

        return code.toString();
    }

    //TODO - Ver a questão dos registos
    public String getCode(SingleOpInstruction instruction)
    {
        var code = new StringBuilder();
        instruction.show();

        code.append("\t"+this.jasminUtils.loadElement(instruction.getSingleOperand(), this.varTable));
        code.append("\t" +this.jasminUtils.storeElement(instruction.getSingleOperand(), this.varTable, this.lastreg));

        return code.toString();
    }


    public String getCode(CallInstruction instruction) {

        switch(instruction.getInvocationType()){
            case invokestatic:
                return getCodeInvokeStatic(instruction);
            case invokevirtual:
                return getCodeInvokeVirtual(instruction);
            case NEW:
                return getCodeNewInstr(instruction);
            case invokespecial:
                return getCodeInvokeSpecial(instruction);
        }

        throw new NotImplementedException(instruction.getInstType());
    }
}
