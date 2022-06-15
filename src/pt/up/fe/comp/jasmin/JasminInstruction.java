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
    private static int conditionalId = 0;


    JasminInstruction(ClassUnit classUnit, Method method)
    {
        this.classUnit = classUnit;
        this.method = method;
        this.labels = method.getLabels();
        this.varTable = method.getVarTable();
        this.jasminUtils = new JasminUtils(this.classUnit);
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

    public int getStackLimit()
    {
        return this.jasminUtils.getStackLimit();
    }

    public String getCode(Instruction instruction){
        return this.getCode(instruction, false);
    }

    public String getCode(Instruction instruction, boolean isAssign){
        var code = new StringBuilder();

        FunctionClassMap<Instruction, String> instructionMap = new FunctionClassMap<>();

        //instructionMap.put(CallInstruction.class, this::getCode);
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

        if(labels.size() != 0) {
            for (String label : labels) {
                code.append(label + ":\n");
            }
        }
        if(instruction instanceof CallInstruction){
            code.append(getCode((CallInstruction) instruction, isAssign));
        } else {
            code.append(instructionMap.apply(instruction));
        }

        return code.toString();
    }

    private String getCodeInvokeStatic(CallInstruction instruction, boolean isAssign)
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

        this.jasminUtils.updateStackLimit();
        for(int i = 0; i < instruction.getListOfOperands().size(); i++){
            this.jasminUtils.subCurrentStack();
        }

        if(!instruction.getReturnType().getTypeOfElement().equals(ElementType.VOID)
          && !isAssign){
            code.append("\tpop\n");
            this.jasminUtils.updateStackLimit();
            this.jasminUtils.subCurrentStack();
        }

        this.jasminUtils.addCurrentStack();

        return code.toString();

    }

    private String createListOperands(CallInstruction instruction)
    {
        return instruction.getListOfOperands().stream()
                .map(element ->this.jasminUtils.getJasminType(element.getType(), true))
                .collect(Collectors.joining());
    }

    private String getCodeInvokeVirtual(CallInstruction instruction, boolean isAssign)
    {
        var code = new StringBuilder();

        var firstArg = ((Operand) instruction.getFirstArg());
        var secondArg = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");

        code.append("\t"+this.jasminUtils.loadElement(firstArg, this.varTable));

        for(Element element : instruction.getListOfOperands()){
            code.append("\t" +this.jasminUtils.loadElement(element, this.varTable) );
        }

        code.append("\tinvokevirtual " +this.jasminUtils.getJasminType(firstArg.getType()) +  "/" + secondArg + "(");
        code.append(createListOperands(instruction));
        code.append(")").append(this.jasminUtils.getJasminType(instruction.getReturnType()) + '\n');

        this.jasminUtils.updateStackLimit();
        this.jasminUtils.subCurrentStack();
        for(int i = 0; i < instruction.getListOfOperands().size(); i++){
            this.jasminUtils.subCurrentStack();
        }

        if(!method.getReturnType().getTypeOfElement().equals(ElementType.VOID)
         && !isAssign){
            code.append("\tpop\n");
            this.jasminUtils.updateStackLimit();
            this.jasminUtils.subCurrentStack();
        }

        this.jasminUtils.addCurrentStack();

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

        this.jasminUtils.updateStackLimit();
        for(int i = 0; i < instruction.getListOfOperands().size(); i++){
            this.jasminUtils.subCurrentStack();
        }

        this.jasminUtils.addCurrentStack();

        return code.toString();
    }

    private String getCodeInvokeSpecial(CallInstruction instruction, boolean isAssign)
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


        this.jasminUtils.updateStackLimit();
        this.jasminUtils.subCurrentStack();
        for(int i = 0; i < instruction.getListOfOperands().size(); i++){
            this.jasminUtils.subCurrentStack();
        }

        if(!instruction.getReturnType().getTypeOfElement().equals(ElementType.VOID)
         && !isAssign){
            code.append("\tpop\n");
            /*this.jasminUtils.updateStackLimit();
            this.jasminUtils.subCurrentStack();*/
            return code.toString();
        }

        this.jasminUtils.addCurrentStack();

        return code.toString();

    }

    private String getCodeLdc(CallInstruction instruction)
    {

        return "\t" + this.jasminUtils.loadElement(instruction.getFirstArg(), this.varTable );
    }

    private String getArrayLength(CallInstruction instruction){
        var code = new StringBuilder();
        instruction.show();
        code.append("\t" + this.jasminUtils.loadElement(instruction.getFirstArg(), this.varTable));
        code.append("\t" + "arraylength\n");

        this.jasminUtils.updateStackLimit();
        this.jasminUtils.subCurrentStack();
        this.jasminUtils.addCurrentStack();

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

        this.jasminUtils.updateStackLimit();
        this.jasminUtils.subCurrentStack();
        this.jasminUtils.subCurrentStack();

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

        this.jasminUtils.updateStackLimit();
        this.jasminUtils.subCurrentStack();
        this.jasminUtils.addCurrentStack();

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

        this.jasminUtils.updateStackLimit();
        this.jasminUtils.subCurrentStack();

        return code.toString();
    }


    public String getCode(AssignInstruction instruction)
    {
        var code = new StringBuilder();
        Operand o1 = (Operand) instruction.getDest();
        if(o1 instanceof ArrayOperand) {
            code.append(jasminUtils.loadArrayRefAndIndex((ArrayOperand) o1, varTable));

        }

        code.append(getCode(instruction.getRhs(), true));
        code.append(this.jasminUtils.storeElement(o1, this.varTable));

        return code.toString();
    }

    public String getCode(SingleOpInstruction instruction)
    {
        return "\t" + this.jasminUtils.loadElement(instruction.getSingleOperand(), this.varTable);

    }

    public String getCode(CallInstruction instruction, boolean isAssign)
    {

        switch(instruction.getInvocationType()){
            case invokestatic:
                return getCodeInvokeStatic(instruction, isAssign);
            case invokevirtual:
                return getCodeInvokeVirtual(instruction, isAssign);
            case NEW:
                return getCodeNewInstr(instruction);
            case invokespecial:
                return getCodeInvokeSpecial(instruction, isAssign);
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
                code.append("\tifne TRUE" + conditionalId + "\n");
                this.jasminUtils.updateStackLimit();
                this.jasminUtils.subCurrentStack();
                code.append("\ticonst_1\n");
                code.append("\tgoto FALSE" + conditionalId + "\n");
                code.append("TRUE" + conditionalId + ":\n");
                code.append("\ticonst_0\n");
                code.append("FALSE"+conditionalId+":\n");
                this.jasminUtils.addCurrentStack();
                conditionalId++;
                break;
            default:
                throw new NotImplementedException(op.getOpType());
        }


        return code.toString();
    }

    private String createArithmeticCode(String operation, Element leftOperand, Element rightOperand)
    {
        var code = new StringBuilder();

        code.append("\t" + this.jasminUtils.loadElement(leftOperand, this.varTable));
        code.append("\t" + this.jasminUtils.loadElement(rightOperand, this.varTable));
        code.append("\t" + operation + "\n");

        this.jasminUtils.updateStackLimit();
        this.jasminUtils.subCurrentStack();
        this.jasminUtils.subCurrentStack();
        this.jasminUtils.addCurrentStack();

        return code.toString();
    }

    private String createBranchCode(String operation, Element leftOperand, Element rightOperand)
    {
        var code = new StringBuilder();
        LiteralElement literalElement = null;
        Operand operand = null;
        String prefix = "_icmp";
        List<String> listBitwiseOperations = new ArrayList<>(Arrays.asList("ne", "eq", "and", "or"));
        List<String> listCmpOperations = new ArrayList<>(Arrays.asList("lt", "le", "gt", "ge"));


        if(leftOperand.isLiteral() && rightOperand.isLiteral()) {
            boolean result = false;
            if(listBitwiseOperations.contains(operation))
                result = doBitwiseOptimization(operation, ((LiteralElement) leftOperand).getLiteral(),((LiteralElement) rightOperand).getLiteral());
            else if(listCmpOperations.contains(operation)) {
                result = doCmpOptmization(operation, ((LiteralElement) leftOperand).getLiteral(),((LiteralElement) rightOperand).getLiteral());
            }
            this.jasminUtils.addCurrentStack();
            if (result) return "\ticonst_1\n";
            else return "\ticonst_0\n";
        }
        else if(leftOperand.isLiteral() && !rightOperand.isLiteral())
        {
            literalElement = (LiteralElement) leftOperand;
            operand = (Operand) rightOperand;
        }
        else if(!leftOperand.isLiteral() && rightOperand.isLiteral())
        {
            literalElement = (LiteralElement) rightOperand;
            operand = (Operand) leftOperand;
        }

        if(literalElement != null && literalElement.getLiteral().equals("0"))
        {
            prefix = "";
        }

        if(prefix.equals(""))
        {
            code.append("\t" + this.jasminUtils.loadElement(operand, this.varTable));
            this.jasminUtils.updateStackLimit();
            this.jasminUtils.subCurrentStack();
        }
        else
        {

            code.append("\t" + this.jasminUtils.loadElement(leftOperand, this.varTable));
            code.append("\t" + this.jasminUtils.loadElement(rightOperand, this.varTable));
            this.jasminUtils.updateStackLimit();
            this.jasminUtils.subCurrentStack();
            this.jasminUtils.subCurrentStack();
        }


        code.append("\t" + "if" + prefix + operation + " FALSE" + conditionalId + "\n" );
        code.append("\ticonst_0\n");
        code.append("\tgoto TRUE" + conditionalId +"\n");
        code.append("FALSE" + conditionalId + ":\n");
        code.append("\ticonst_1\n");
        code.append("TRUE" + conditionalId + ":\n");
        this.jasminUtils.addCurrentStack();
        conditionalId++;
        return code.toString();
    }

    private boolean doCmpOptmization(String operation, String leftLiteral, String rightLiteral) {
        int left = Integer.parseInt(leftLiteral);
        int right = Integer.parseInt(rightLiteral);

        switch(operation) {

            case "lt":
                return left < right;
            case "le":
                return  left <= right;
            case "gt":
                return left > right;
            case "ge":
                return left >= right;
            default:
                return false;
        }
    }

    private boolean doBitwiseOptimization(String operation, String leftLiteral, String rightLiteral)
    {
        boolean left = (leftLiteral.equals("1")) ? true : false;
        boolean right = (rightLiteral.equals("1")) ? true : false;

        switch(operation) {

            case "and":
                return left && right;
            case "or":
                return  left || right;
            case "eq":
                return left == right;
            case "ne":
                return left != right;
            default:
                return false;
        }


    }


    private String doLogicalOptimizationIfNeeded(String operation ,LiteralElement literalElement)
    {

        if(operation.equals("and") && literalElement.getLiteral().equals("0"))
            return "\ticonst_0\n";
        else if(operation.equals("or") && literalElement.getLiteral().equals("1"))
            return "\ticonst_1\n";
        else return "";
    }

    private String createOrAndCode(String operation, Element leftOperand, Element rightOperand)
    {
        var code = new StringBuilder();
        LiteralElement literal = null;
        String result = "";

        if(leftOperand.isLiteral() && rightOperand.isLiteral()) {
            this.jasminUtils.addCurrentStack();
            boolean res = doBitwiseOptimization(operation, ((LiteralElement) leftOperand).getLiteral(), ((LiteralElement) rightOperand).getLiteral());
            if (res) return "\ticonst_1\n";
            else return "\ticonst_0\n";
        }
        else if(leftOperand.isLiteral() && !rightOperand.isLiteral())
        {
            literal = (LiteralElement) leftOperand;
        }
        else if(!leftOperand.isLiteral() && rightOperand.isLiteral())
        {
            literal = (LiteralElement) rightOperand;
        }

        if(literal != null)
            result = doLogicalOptimizationIfNeeded(operation, literal);

        if(result != "") return result;

            code.append("\t" + this.jasminUtils.loadElement(leftOperand, this.varTable));
            code.append("\tifeq" + " FALSE" + conditionalId + "\n");
            this.jasminUtils.updateStackLimit();
            this.jasminUtils.subCurrentStack();
            code.append("\t" + this.jasminUtils.loadElement(rightOperand, this.varTable));
            code.append("\tifeq" + " FALSE" + conditionalId + "\n");
            this.jasminUtils.updateStackLimit();
            this.jasminUtils.subCurrentStack();
            code.append("\ticonst_1\n");
            code.append("\tgoto TRUE" + conditionalId + "\n");
            code.append("FALSE" + conditionalId + ":\n");
            code.append("\ticonst_0\n");
            code.append("TRUE" + conditionalId + ":\n");
            this.jasminUtils.addCurrentStack();
            conditionalId++;

        return code.toString();
    }

    private String doDivOptimizationIfNeeded(Element leftOperand, Element rightOperand)
    {
        LiteralElement literalElement = null;
        Element operand = null;

        if(rightOperand.isLiteral() && !leftOperand.isLiteral())
        {
            literalElement = (LiteralElement) rightOperand;
            operand = leftOperand;
        }
        else if(rightOperand.isLiteral() && leftOperand.isLiteral())
        {
            literalElement = (LiteralElement) rightOperand;
            operand = leftOperand;
        }

        if(literalElement != null)
        {
            int numShifts = this.jasminUtils.checkIfIsPower2(Integer.parseInt(literalElement.getLiteral()));


            if(numShifts != -1)
            {
                Type type = new Type(ElementType.INT32);
                LiteralElement newLiteral = new LiteralElement(Integer.toString(numShifts), type);
                return createArithmeticCode("ishr", operand,  newLiteral);
            }
        }
        return "";

    }

    private String doMulOptimizationIfNeeded( Element leftOperand, Element rightOperand)
    {
        LiteralElement literalElement = null;
        Element operand = null;

        if(rightOperand.isLiteral() && !leftOperand.isLiteral())
        {
            literalElement = (LiteralElement) rightOperand;
            operand = leftOperand;
        }
        else if(!rightOperand.isLiteral() && leftOperand.isLiteral())
        {
            literalElement = (LiteralElement) leftOperand;
            operand = rightOperand;
        }
        else if(rightOperand.isLiteral() && leftOperand.isLiteral())
        {
            literalElement = (LiteralElement) leftOperand;
            operand = rightOperand;
        }

        if(literalElement != null)
        {
            int numShifts = this.jasminUtils.checkIfIsPower2(Integer.parseInt(literalElement.getLiteral()));

            if(operand instanceof LiteralElement && numShifts == -1)
            {

                numShifts = this.jasminUtils.checkIfIsPower2(Integer.parseInt(((LiteralElement)operand).getLiteral()));
                operand = literalElement;
            }

            if(numShifts != -1)
            {
                Type type = new Type(ElementType.INT32);
                LiteralElement newLiteral = new LiteralElement(Integer.toString(numShifts), type);
                return createArithmeticCode("ishl" , operand,  newLiteral);
            }
        }
        return "";

    }


    public String getCode(BinaryOpInstruction instruction)
    {
        var code = new StringBuilder();
        Operation op = instruction.getOperation();
        String optimizedCode;

        Element leftOperand = instruction.getLeftOperand();
        Element rightOperand= instruction.getRightOperand();

        switch(op.getOpType()){
            case DIV:
                optimizedCode = doDivOptimizationIfNeeded(leftOperand, rightOperand);
                if(optimizedCode == "")
                    code.append(createArithmeticCode("idiv", leftOperand, rightOperand));
                else code.append(optimizedCode);

                break;
            case MUL:
                optimizedCode = doMulOptimizationIfNeeded(leftOperand, rightOperand);
                if(optimizedCode == "")
                    code.append(createArithmeticCode("imul", leftOperand, rightOperand));
                else code.append(optimizedCode);
                this.jasminUtils.updateStackLimit();
                break;
            case SUB:
                code.append(createArithmeticCode("isub", leftOperand, rightOperand));
                break;
            case ADD:
                code.append(createArithmeticCode("iadd", leftOperand, rightOperand));
                break;
            case EQ:
                code.append(createBranchCode("eq", leftOperand,rightOperand));
                break;
            case NEQ:
                code.append(createBranchCode("ne", leftOperand,rightOperand));
                break;
            case GTH:
                code.append(createBranchCode("gt", leftOperand, rightOperand));
                break;
            case LTH:
                code.append(createBranchCode("lt", leftOperand, rightOperand));
                break;
            case AND:
                code.append(this.jasminUtils.loadElement(leftOperand, this.varTable));
                code.append(this.jasminUtils.loadElement(rightOperand, this.varTable));
                this.jasminUtils.updateStackLimit();
                this.jasminUtils.subCurrentStack();
                this.jasminUtils.subCurrentStack();
                this.jasminUtils.addCurrentStack();
                code.append("\tiand\n");
                break;
            case ANDB:
                code.append(createOrAndCode("and", leftOperand, rightOperand));
                break;
            case OR:
                code.append(this.jasminUtils.loadElement(leftOperand, this.varTable));
                code.append(this.jasminUtils.loadElement(rightOperand, this.varTable));
                code.append("ior\n");
                this.jasminUtils.updateStackLimit();
                this.jasminUtils.subCurrentStack();
                this.jasminUtils.subCurrentStack();
                this.jasminUtils.addCurrentStack();
                break;
            case ORB:
                code.append(createOrAndCode("or", leftOperand, rightOperand));
                break;
            case LTE:
                code.append(createBranchCode("le", leftOperand, rightOperand));
                break;
            case GTE:
                code.append(createBranchCode("ge", leftOperand, rightOperand));
                break;
            case XOR:
                code.append(this.jasminUtils.loadElement(leftOperand, this.varTable));
                code.append(this.jasminUtils.loadElement(rightOperand, this.varTable));
                code.append("ixor\n");
                this.jasminUtils.updateStackLimit();
                this.jasminUtils.subCurrentStack();
                this.jasminUtils.subCurrentStack();
                this.jasminUtils.addCurrentStack();
                break;
            case NOTB:
                code.append("\tif_ne TRUE" + conditionalId + "\n");
                code.append("\ticonst_1\n");
                code.append("\tgoto FALSE" + conditionalId + "\n");
                code.append("TRUE" + conditionalId + ":\n");
                code.append("\ticonst_0");
                code.append("FALSE"+conditionalId+":\n");
                conditionalId++;
                break;
            default:
                throw new NotImplementedException(op.getOpType());
        }

        this.jasminUtils.updateStackLimit();
        /*this.jasminUtils.subCurrentStack();
        this.jasminUtils.subCurrentStack();
        this.jasminUtils.addCurrentStack();*/

        return code.toString();
    }

    public String getCode(GotoInstruction instruction)
    {
        return "\tgoto " + instruction.getLabel() + "\n";
    }

    public String getCode(SingleOpCondInstruction instruction) {
        var code = new StringBuilder();

        code.append(getCode(instruction.getCondition()));
        code.append("\tifne " + instruction.getLabel() + " \n");

        this.jasminUtils.updateStackLimit();
        this.jasminUtils.subCurrentStack();

        return code.toString();
    }

    public String getCode(OpCondInstruction instruction) {
        var code = new StringBuilder();

        code.append(getCode(instruction.getCondition()));
        code.append("\tifne " + instruction.getLabel() + "\n");

        this.jasminUtils.updateStackLimit();
        this.jasminUtils.subCurrentStack();

        return code.toString();
    }

    public String getCode(CondBranchInstruction instruction) {
        var code = new StringBuilder();

        code.append(getCode(instruction.getCondition()));
        code.append(instruction.getLabel() + "\n");

        return code.toString();
    }

    public static void reset()
    {
        conditionalId = 0;
    }
}
