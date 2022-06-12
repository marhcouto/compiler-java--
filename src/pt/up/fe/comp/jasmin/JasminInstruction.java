package pt.up.fe.comp.jasmin;
import org.eclipse.jgit.util.io.IsolatedOutputStream;
import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.sort;

public class JasminInstruction {
    private final HashMap<String, Instruction> labels;
    private HashMap<String, Descriptor> varTable;
    private Method method;
    private ClassUnit classUnit;
    private JasminUtils jasminUtils;
    private int stackCounter;
    JasminInstruction(ClassUnit classUnit, Method method)
    {
        this.classUnit = classUnit;
        this.method = method;

        method.buildVarTable();
        this.labels = method.getLabels();
        this.varTable = method.getVarTable();
        this.jasminUtils = new JasminUtils(this.classUnit);
        this.stackCounter = 0;
    }

    @Deprecated
    public int getLastReg() {

        var var2 = this.varTable.entrySet().iterator();
        ArrayList<Integer> allRegs = new ArrayList<>();

        while(var2.hasNext()) {
            Map.Entry<String, Descriptor> entry = (Map.Entry)var2.next();
            String key = (String)entry.getKey();
            Descriptor d1 = (Descriptor)entry.getValue();
            allRegs.add(d1.getVirtualReg());
        }
        sort(allRegs);

        return allRegs.size() == 0 ? 0 : allRegs.get(allRegs.size()-1);
    }

    public String getCode(Instruction instruction){
        var code = new StringBuilder();

        FunctionClassMap<Instruction, String> instructionMap = new FunctionClassMap<>();

        System.out.println(instruction);

        instructionMap.put(CallInstruction.class, this::getCode);
        instructionMap.put(PutFieldInstruction.class, this::getCode);
        instructionMap.put(GetFieldInstruction.class, this::getCode);
        instructionMap.put(AssignInstruction.class, this::getCode);
        instructionMap.put(ReturnInstruction.class, this::getCode);
        instructionMap.put(SingleOpInstruction.class, this::getCode);
        instructionMap.put(UnaryOpInstruction.class, this::getCode);
        instructionMap.put(BinaryOpInstruction.class, this::getCode);
        instructionMap.put(GotoInstruction.class, this::getCode);
        instructionMap.put(SingleOpCondInstruction.class, this::getCode);
        instructionMap.put(OpCondInstruction.class, this::getCode);
        instructionMap.put(CondBranchInstruction.class, this::getCode);

        var labels = method.getLabels(instruction);

        if(labels.size() != 0)
            code.append(labels.get(0) + ":\n");

        code.append(instructionMap.apply(instruction));

        return code.toString();
    }

    private String getCodeInvokeStatic(CallInstruction instruction)
    {
        var code = new StringBuilder();
        var methodClass = ((Operand) instruction.getFirstArg()).getName();
        var methodName = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");

        for(Element element : instruction.getListOfOperands()){
            code.append("\t" +this.jasminUtils.loadElement(element, this.varTable) );
        }
        code.append("\tinvokestatic " +this.jasminUtils.getFullyQualifiedName(methodClass));
        code.append("/" + methodName + "(");

        var operandsTypes = instruction.getListOfOperands().stream()
                .map(element ->this.jasminUtils.getJasminType(element.getType()))
                .collect(Collectors.joining());
        code.append(operandsTypes).append(")").append(this.jasminUtils.getJasminType(instruction.getReturnType()) + "\n");


        return code.toString();

    }

    private String createListOperands(CallInstruction instruction)
    {
        return instruction.getListOfOperands().stream()
                .map(element ->this.jasminUtils.getJasminType(element.getType()))
                .collect(Collectors.joining());
    }

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
        code.append(createListOperands(instruction));
        code.append(")").append(this.jasminUtils.getJasminType(instruction.getReturnType()) + '\n');

        return code.toString();

    }

    //ver invokevirtual
    private String getCodeNewInstr(CallInstruction instruction)
    {
        var code = new StringBuilder();
        var firstArg = ((Operand) instruction.getFirstArg());

        for(Element element : instruction.getListOfOperands()){
            code.append("\t" +this.jasminUtils.loadElement(element, this.varTable) );
        }

        code.append("\tnew");
        switch (firstArg.getType().getTypeOfElement()){
            case INT32:

                break;
            case CLASS:
                code.append("\t" + firstArg.getName() + "\n");
                code.append("\tdup\n");
                break;
            case ARRAYREF:
                code.append("array int");
                break;
            case OBJECTREF:
                code.append("\t" + firstArg.getName() + "\n");

                break;
            default:
                throw new NotImplementedException(firstArg.getType().getTypeOfElement());
        }
        code.append("\n");
        //code.append("\tdup\n");

        return code.toString();
    }

    private String getCodeInvokeSpecial(CallInstruction instruction)
    {
        var code = new StringBuilder();

        var firstArg = ((Operand) instruction.getFirstArg());
        var methodName = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");;

        code.append("\t" + this.jasminUtils.loadElement(instruction.getFirstArg(), this.varTable));

        for(Element element : instruction.getListOfOperands()){
            code.append("\t" +this.jasminUtils.loadElement(element, this.varTable) );
        }

        code.append("\tinvokespecial " +this.jasminUtils.getJasminType(firstArg.getType()));
        code.append("/" + methodName + "(");
        code.append(createListOperands(instruction));
        code.append(")").append(this.jasminUtils.getJasminType(instruction.getReturnType()) + '\n');

        return code.toString();

    }

    private String getCodeLdc(CallInstruction instruction)
    {
        return "\t" + this.jasminUtils.loadElement(instruction.getFirstArg(), this.varTable );
    }

    private String getArrayLength(CallInstruction instruction){
        var code = new StringBuilder();
        System.out.println("get array length");
        instruction.show();
        code.append("\t" + this.jasminUtils.loadElement(instruction.getFirstArg(), this.varTable));
        code.append("\t" + "arraylength\n");
        return code.toString();
    }

    public String getCode(PutFieldInstruction instruction) {

        var code = new StringBuilder();

        var firstArg = ((Operand) instruction.getFirstOperand());
        var secondArg = instruction.getSecondOperand();
        var thirdArg = instruction.getThirdOperand();
        String secondArgStr = "";

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

        code.append("\t" + this.jasminUtils.loadElement(instruction.getOperand(), this.varTable));
        code.append("\t" + this.jasminUtils.getJasminReturnType(instruction.getOperand().getType().getTypeOfElement()));
        code.append("return\n");

        return code.toString();
    }


    public String getCode(AssignInstruction instruction)
    {
        var code = new StringBuilder();
        Operand o1 = (Operand) instruction.getDest();
        if(o1 instanceof ArrayOperand) {
            code.append(jasminUtils.loadArrayRefAndIndex((ArrayOperand) o1, varTable));
        }
        code.append(getCode(instruction.getRhs()));
        code.append(this.jasminUtils.storeElement(o1, this.varTable));


        return code.toString();
    }

    public String getCode(SingleOpInstruction instruction)
    {
        return "\t" + this.jasminUtils.loadElement(instruction.getSingleOperand(), this.varTable);
    }

    public String getCode(CallInstruction instruction)
    {
        this.stackCounter = 1; // ?
        switch(instruction.getInvocationType()){
            case invokestatic:
                return getCodeInvokeStatic(instruction);
            case invokevirtual:
                return getCodeInvokeVirtual(instruction);
            case NEW:
                return getCodeNewInstr(instruction);
            case invokespecial:
                return getCodeInvokeSpecial(instruction);
            case ldc:
                return getCodeLdc(instruction);
            case arraylength:
                return getArrayLength(instruction);
        }

        throw new NotImplementedException(instruction.getInvocationType());
    }

    public String getCode(UnaryOpInstruction instruction)
    {
        var code = new StringBuilder();
        Operation op = instruction.getOperation();
        code.append("\t"+this.jasminUtils.loadElement(instruction.getOperand(), varTable));

        switch(op.getOpType()){
            case NOT:
            case NOTB:
                /*code.append("\t"+this.jasminUtils.loadElement(instruction.getOperand(), varTable));
                code.append("\ticonst_0\n");
                code.append("\tixor\n");*/
                break;
            default:
                throw new NotImplementedException(op.getOpType());
        }
        return code.toString();
    }

    public String createCmpCode(String operation)
    {
        var code = new StringBuilder();

        code.append("if_icmp" + operation + " ");



        return code.toString();
    }

    private String createLogicOpCode(String operation, Element leftOperand, Element rightOperand)
    {
        var code = new StringBuilder();
        code.append("\t" + this.jasminUtils.loadElement(leftOperand, this.varTable));
        code.append("\t" + this.jasminUtils.loadElement(rightOperand, this.varTable));

        if(leftOperand.isLiteral())
        {
            var leftLiteral = (LiteralElement) leftOperand;
            code.append("\t" + ((leftLiteral.getLiteral().equals("0")) ? "pop" : "i") + operation + "\n");
        }
        else
        {
            code.append("\ti" + operation + "\n");
        }
        return code.toString();
    }

    private String createArithmeticCode(String operation, Element leftOperand, Element rightOperand)
    {
        var code = new StringBuilder();
        code.append("\t" + this.jasminUtils.loadElement(leftOperand, this.varTable));
        code.append("\t" + this.jasminUtils.loadElement(rightOperand, this.varTable));
        code.append("\t" + operation + "\n");
        return code.toString();
    }

    private String createBranchCode(String operation, Element leftOperand, Element rightOperand)
    {
        var code = new StringBuilder();

        //TODO FAZER PARA VARIAS LABELS
        code.append("\t" + this.jasminUtils.loadElement(leftOperand, this.varTable));
        code.append("\t" + this.jasminUtils.loadElement(rightOperand, this.varTable));
        code.append("\t" + "if_icmp" + operation + " FALSE" + "\n" );
        code.append("\ticonst_0\n");
        code.append("\tgoto TRUE \n");
        code.append("FALSE:\n");
        code.append("\ticonst_1\n");
        code.append("TRUE:\n");

        return code.toString();
    }


    private String createOrAndCode(Element leftOperand, Element rightOperand)
    {
        var code = new StringBuilder();

        //otimização
        /*if(leftOperand.isLiteral() && rightOperand.isLiteral())
        {
            LiteralElement leftLiteral = (LiteralElement) leftOperand;
            LiteralElement rightLiteral = (LiteralElement) rightOperand;

            boolean result = Boolean.parseBoolean(leftLiteral.getLiteral()) && Boolean.parseBoolean(rightLiteral.getLiteral());

            if(result) code.append("\ticonst_1\n");
            else code.append("\ticonst_0\n");

        }*/




        //TODO VERIFICAR SE NAO EXISTEM ESTAS LABELS
        code.append("\t" + this.jasminUtils.loadElement(leftOperand, this.varTable));
        code.append("\tifeq" + " STEP\n");
        code.append("\t" + this.jasminUtils.loadElement(rightOperand, this.varTable));
        code.append("\tifeq" + " STEP\n");
        code.append("\ticonst_1\n");
        code.append("\tgoto TRUE\n");
        code.append("STEP:\n");
        code.append("\ticonst_0\n");
        code.append("TRUE:\n");

        return code.toString();
    }


    //TODO: Arrays
    //TODO: Limit stack
    //TODO: Limit locals

    public String getCode(BinaryOpInstruction instruction)
    {
        var code = new StringBuilder();
        Operation op = instruction.getOperation();

        var leftOperand = instruction.getLeftOperand();
        var rightOperand= instruction.getRightOperand();

        switch(op.getOpType()){
            case DIV:
                code.append(createArithmeticCode("idiv", leftOperand, rightOperand));
                break;
            case MUL:
                code.append(createArithmeticCode("imul", leftOperand, rightOperand));
                break;
            case SUB:
                code.append(createArithmeticCode("isub", leftOperand, rightOperand));
                break;
            case ADD:
                code.append(createArithmeticCode("iadd", leftOperand, rightOperand));
                break;
            case EQ:
                break;
            case NEQ:
                break;
            case GTH:
                code.append(createBranchCode("gt", leftOperand, rightOperand));
                break;
            case LTH:
                code.append(createBranchCode("lt", leftOperand, rightOperand));
                break;
            case AND:
                code.append("\tiand\n");
                break;
            case ANDB:
                code.append(createOrAndCode(leftOperand, rightOperand));
                break;
            case OR:
                code.append("ior\n");
                break;
            case ORB:
                code.append(createOrAndCode(leftOperand, rightOperand));
                break;
            case LTE:
                code.append(createBranchCode("le", leftOperand, rightOperand));
                break;
            case GTE:
                code.append(createBranchCode("ge", leftOperand, rightOperand));
                break;
            case XOR:
                code.append("ixor\n");
                break;
            case NOTB:
                code.append("iconst_0\n");
                code.append("\tixor\n");
                break;
            default:
                throw new NotImplementedException(op.getOpType());
        }

        return code.toString();
    }

    public String getCode(GotoInstruction instruction)
    {
        return "\tgoto " + instruction.getLabel() + "\n";
    }

    public String getCode(SingleOpCondInstruction instruction) {
        var code = new StringBuilder();

        code.append(getCode(instruction.getCondition()));
        code.append("\tifeq " + instruction.getLabel() + ": \n");

        return code.toString();
    }

    public String getCode(OpCondInstruction instruction) {
        var code = new StringBuilder();

        code.append(getCode(instruction.getCondition()));
        code.append("\tifeq " + instruction.getLabel() + "\n");

        return code.toString();
    }

    public String getCode(CondBranchInstruction instruction) {
        var code = new StringBuilder();

        System.out.println("COND BRANCH");
        code.append("NOT IMPLEMENTED");
        //code.append(getCode(instruction.getCondition()));
        //code.append(instruction.getLabel() + "\n");

        return code.toString();
    }
}
