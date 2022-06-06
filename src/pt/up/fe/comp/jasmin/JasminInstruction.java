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
    private int lastreg;
    private JasminUtils jasminUtils;
    private static int atLeastOneCond = 0;

    JasminInstruction(ClassUnit classUnit, Method method)
    {
        this.classUnit = classUnit;
        this.method = method;

        method.buildVarTable();
        this.labels = method.getLabels();
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
            allRegs.add(d1.getVirtualReg());
        }
        sort(allRegs);

        return allRegs.size() == 0 ? 0 : allRegs.get(allRegs.size()-1);
    }

    public String getCode(Instruction instruction){
        var code = new StringBuilder();

        FunctionClassMap<Instruction, String> instructionMap = new FunctionClassMap<>();

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
        var methodName = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");;

        for(Element element : instruction.getListOfOperands()){
            code.append("\t" +this.jasminUtils.loadElement(element, this.varTable) );
        }
        code.append("\tinvokestatic " +this.jasminUtils.getFullyQualifiedName(methodClass));
        code.append("/" + methodName + "(");

        var operandsTypes = instruction.getListOfOperands().stream()
                .map(element ->this.jasminUtils.getJasminType(element.getType()))
                .collect(Collectors.joining());
        code.append(operandsTypes).append(")").append(this.jasminUtils.getJasminType(instruction.getReturnType()) + '\n');

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

    private String getCodeNewInstr(CallInstruction instruction)
    {
        var code = new StringBuilder();
        var firstArg = ((Operand) instruction.getFirstArg());

        code.append("\tnew " + firstArg.getName() + "\n");
        code.append("\tdup\n");

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
        var o1 = (Operand) instruction.getDest();

        code.append(getCode(instruction.getRhs()));
        code.append("\t"+ this.jasminUtils.storeElement(o1, this.varTable));

        return code.toString();
    }

    public String getCode(SingleOpInstruction instruction)
    {
        return "\t" + this.jasminUtils.loadElement(instruction.getSingleOperand(), this.varTable);
    }

    public String getCode(CallInstruction instruction)
    {

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

        code.append("if_icmp" + operation + " COND_TRUE_" + this.atLeastOneCond+ "\n");
        code.append("\ticonst_0\n");
        code.append("\tgoto CONTINUE_" + this.atLeastOneCond+ "\n");
        code.append("COND_TRUE_" + this.atLeastOneCond + ":\n");
        code.append("\ticonst_1\n");
        code.append("CONTINUE_" + this.atLeastOneCond +":");

        return code.toString();
    }

    public String createLogicOpCode(String operation, Element op)
    {
        if(op.isLiteral())
        {
            var leftLiteral = (LiteralElement) op;
            return (leftLiteral.getLiteral().equals("0")) ? "pop" : "i" + operation;
        }
        else
        {
            return "i" + operation;
        }
    }

    public String getCode(BinaryOpInstruction instruction)
    {
        var code = new StringBuilder();
        Operation op = instruction.getOperation();

        code.append("\t" + this.jasminUtils.loadElement(instruction.getLeftOperand(), this.varTable));
        code.append("\t" + this.jasminUtils.loadElement(instruction.getRightOperand(), this.varTable));
        code.append("\t");

        switch(op.getOpType()){
            case DIVI32:
            case DIV:
                code.append("idiv");
                break;
            case MULI32:
            case MUL:
                code.append("imul");
                break;
            case SUBI32:
            case SUB:
                code.append("isub");
                break;
            case ADDI32:
            case ADD:
                code.append("iadd");
                break;
            case EQ:
                break;
            case NEQ:
                break;
            case GTH:
                this.atLeastOneCond++;
                code.append(createCmpCode("gt"));
                break;
            case LTH:
                this.atLeastOneCond++;
                code.append(createCmpCode("lt"));
                break;
            case ANDI32:
                code.append("iand");
            case AND:
                break;
            case ANDB:
                code.append(createLogicOpCode("and", instruction.getLeftOperand()));
                break;
            case ORI32:
                code.append("ior");
                break;
            case OR:
                break;
            case ORB:
                code.append(createLogicOpCode("or", instruction.getLeftOperand()));
                break;
            case LTE:
                break;
            case GTE:
                break;
            case XOR:
                code.append("ixor");
                break;
            case NOTB:
                code.append("iconst_0\n");
                code.append("\tixor");
                break;
            default:
                throw new NotImplementedException(op.getOpType());
        }
        code.append("\n");
        return code.toString();
    }

    public String getCode(GotoInstruction instruction)
    {
        return "\tgoto " + instruction.getLabel() + "\n";
    }

    public String getCode(SingleOpCondInstruction instruction) {
        var code = new StringBuilder();

        code.append(getCode(instruction.getCondition()));
        code.append("\tifne " + instruction.getLabel() + "\n");



        System.out.println("CONDITION : " + instruction.getCondition());
        System.out.println(getCode(instruction.getCondition()));
        System.out.println("LABEL : " + instruction.getLabel());
        System.out.println("OPERANDS : " + instruction.getOperands());

        return code.toString();
    }

    public String getCode(OpCondInstruction instruction) {
        var code = new StringBuilder();

        code.append(getCode(instruction.getCondition()));
        code.append("\tifne " + instruction.getLabel() + "\n");



        System.out.println("CONDITION : " + instruction.getCondition());
        //System.out.println(getCode(instruction.getCondition()));
        System.out.println("LABEL : " + instruction.getLabel());
        System.out.println("OPERANDS : " + instruction.getOperands());

        return code.toString();
    }
}
