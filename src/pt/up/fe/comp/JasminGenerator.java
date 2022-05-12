package pt.up.fe.comp;

import
        org.specs.comp.ollir.*;
import org.specs.comp.ollir.Type;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.io.File;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;

public class JasminGenerator implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {


        ollirResult.getOllirClass().show();

        ClassUnit ollirClass = ollirResult.getOllirClass();
        String jasminStr = createJasminStr(ollirClass);
        showJasmin(jasminStr);

        //(String className, String jasminCode, List< Report > reports, Map<String, String> config)

        var jasminResult = new JasminResult(jasminStr);

        System.out.println("1");
        File outputDir = new File("test/fixtures/public/testing");
        System.out.println("2");
        jasminResult.compile(outputDir);
        System.out.println("3");

        return null;
    }
    //TODO - Criar um ficheiro com superclass
    //TODO - Criar um ficheiro com packages
    //TODO - Criar um ficheiro com class public
    //TODO - Criar um ficheiro com fields com todos os tipos
    //TODO - Criar metodos e testar cada hipotese

    public void showJasmin(String jasminStr)
    {
        System.out.println(jasminStr);
    }

    public String createJasminStr(ClassUnit ollirClass){

        String jasminHeader = this.createJasminHeader(ollirClass);
        String jasminFields = this.createJasminFields(ollirClass.getFields());
        String jasminMethods = this.createJasminMethods(ollirClass.getMethods());

        return jasminHeader + "\n" + jasminFields + "\n" + jasminMethods + "\n";
    }

    public String convertFieldTypeToPrimitiveJasmin(Type type){

        //TODO - Add other types
        switch (type.toString())
        {
            case ("INT32"):
                return "I";
        }

        return "";
    }

    public String createField(Field field)
    {
        String fieldStr = ".field ";

        String accModifiers = field.getFieldAccessModifier().name();
        String accessModifiers = createAccessSpecsStr(accModifiers, field.isStaticField(), field.isFinalField());

        fieldStr += accessModifiers + field.getFieldName() + " " ;
        String fieldType = convertFieldTypeToPrimitiveJasmin(field.getFieldType());

        if(field.isInitialized())
            fieldType += "=" + field.getInitialValue();

        return fieldStr + fieldType;
    }

    public String createJasminFields(ArrayList<Field> fields){

        System.out.println("Printing "+ fields.size() + " Fields");
        String fieldsStr = "";

        for (Field field : fields)
        {
            String fieldStr = createField(field);
            fieldsStr += fieldStr;
        }

        fieldsStr += "\n";
        System.out.println(fieldsStr);
        return fieldsStr;
    }

    public String createConstructMethod(){

        String constructStr = "public";


        return constructStr;

    }

    public String getReturnValueJasmin(Type returnValue)
    {
        switch (returnValue.toString())
        {
            case "VOID":
                return "V";
            default:
                return "";
        }

    }

    /*public String getInstrType(InstructionType instructionType)
    {
        switch (instructionType.toString())
        {
            case "CALL":
                return "(";
            default:
                return "";
        }

    }*/

    public String createStatements(ArrayList<Instruction> instructions)
    {
        String instrStr = "";
        for (Instruction instruction : instructions) {
            instruction.show();
            instrStr += createInstruction(instruction);
            break;

        }
        return instrStr;
    }

    public String createInstruction(Instruction instruction){
        InstructionType instrType = instruction.getInstType();
        String instrStr = "\t";
        Operand o1;
        switch (instrType)
        {
            case ASSIGN:
                AssignInstruction assign = (AssignInstruction) instruction;
                System.out.print("\t" + assign.getInstType() + " ");

                o1 = (Operand) assign.getDest();
                System.out.print("Operand: " + o1.getName() + " " + o1.getType());

                instrStr += createInstruction(assign.getRhs());
                instrStr += "putfield ";
                if(o1.isParameter()){
                    instrStr += o1.getParamId();
                } else {
                    //TODO maybe: Classname/Varname
                    instrStr += o1.getClass().getName()+ "/" + o1.getName() + " ";
                }
                instrStr += o1.getType().getTypeOfElement(); //TODO verify
                instrStr += "\n\t";
                System.out.print(" = ");

                break;
            case CALL:

                CallInstruction ins = (CallInstruction) instruction;
                CallType invocationType = ins.getInvocationType();

                Element e1 = ins.getFirstArg();
                Element secondArg = ins.getSecondArg();
                Integer numOperands = ins.getNumOperands();
                ArrayList<Element> listOfOperands = ins.getListOfOperands();
                String signature = "";
                System.out.println("num of operands" + numOperands);

                if (e1.isLiteral()) { // if the e1 is not a literal, then it is a variable
                    System.out.print("Literal: " + ((LiteralElement) e1).getLiteral());
                    signature += ((LiteralElement) e1).getLiteral();
                } else {
                    o1 = (Operand) e1;
                    System.out.print("Operand: " + o1.getName() + " " + o1.getType());
                    //instrStr += sout    instruction.getClass().getClass().getSuperclass();
                    signature += instruction.getClass().getClass().getSuperclass().getName().replace('.', '/');

                }

                if (numOperands > 1) {

                    if (invocationType != CallType.NEW) { // only new type instructions do not have a field with second arg
                        e1 = secondArg;
                        if (e1.isLiteral()) { // if the e1 is not a literal, then it is a variable
                            System.out.print(", Literal: " + ((LiteralElement) e1).getLiteral());
                            signature += "/" + ((LiteralElement) e1).getLiteral().replace("\"", "");
                        } else {
                            o1 = (Operand) e1;
                            System.out.print(", Operand: " + o1.getName() + " " + o1.getType());
                        }
                    }
                    ArrayList<Element> otherArgs = listOfOperands;
                    for (Element arg : otherArgs) {
                        System.out.println("here");
                        if (arg.isLiteral()) { // if the e1 is not a literal, then it is a variable
                            System.out.print(", Literal: " + ((LiteralElement) arg).getLiteral());
                        } else {
                            o1 = (Operand) arg;
                            System.out.print(", Operand: " + o1.getName() + " " + o1.getType());
                        }
                    }
                }

                Type returnType = ins.getReturnType();
                String returnTypeStr = getReturnValueJasmin(returnType);

                System.out.println("ins: "+ins.getInvocationType() +" " + secondArg + returnTypeStr + "()" + returnType);


                    /*for (CallType callType : CallType.values()) {

                        System.out.println(instrType.getClass());

                        if(instrType.getClass()== callType.ordinal())
                        {
                            System.out.println(callType);
                            System.out.println("igual");
                        }
                    }*/

                instrStr += invocationType + " " + signature + "()" + returnTypeStr ;
                break;
            case BRANCH:
                CondBranchInstruction branchInstruction = (CondBranchInstruction) instruction;
                OpInstruction cond = (OpInstruction) branchInstruction.getCondition();
                for(Element elem : branchInstruction.getOperands()){
                    instrStr += loadElement(elem);
                }
                instrStr += getConditional(cond.getOperation().getOpType());
                instrStr += " " + branchInstruction.getLabel() + "\n";
                break;
            case GOTO:
                GotoInstruction gotoInstruction = (GotoInstruction) instruction;
                instrStr += "goto " + gotoInstruction.getLabel() + "\n";
                break;
            default:
                instrStr += "";
        }
        return instrStr;
    }

    private String getConditional(OperationType type){
        switch(type){
            case GTEI32:
            case GTE:
                return "if_icmpge";
            case GTH:
                return "if_icmpgt";
            case LTE:
                return "if_icmple";
            case LTH:
                return "if_icmplt";
            case EQ:
                return "if_acmpeq";
            case NEQ:
                return "if_acmpne";
            case EQI32:
                return "if_icmpeq";
            case NEQI32:
                return "if_icmpne";
            case NOT:
                return "ifne";
            case XOR:
            case XORI32:
                return "ixor";
            case NOTB:
                return "";

            default:
                return "ola";

        }
    }

    private String loadElement(Element element){
        String instrStr ="";
        if(element.isLiteral()){
            LiteralElement lit = (LiteralElement) element;
            instrStr = "ldc " + lit.getLiteral() + " \n";
        } else{
            Operand operand = (Operand) element;
            ElementType elemType = operand.getType().getTypeOfElement();
            if(operand.isParameter()) {
                switch (elemType) {
                    case INT32:
                        instrStr = "iload " + operand.getParamId();
                        break;
                    case BOOLEAN:
                        instrStr = "iload " + operand.getParamId();
                        break;
                    case ARRAYREF:
                        instrStr = "aload " + operand.getParamId();
                        break;
                    case OBJECTREF:
                        instrStr = "aload " + operand.getParamId();
                        break;
                    case CLASS:
                        instrStr = "aload " + operand.getParamId();
                        break;
                    case THIS:
                        instrStr = "aload " + operand.getParamId();
                        break;
                    case STRING:
                        instrStr = "aload " + operand.getParamId();
                        break;
                    case VOID:
                        break;
                }
            }
            else{
                instrStr += "aload 0\n";
                instrStr += "getfield " + element.getClass().getName()+"/"+operand.getName() + " ";

            }
            instrStr += "\n";
        }
        return instrStr;
    }

    //TODO - Refactoring
    public String createMethod(Method method) {

        String methodStr = ".method ";
        System.out.println(method.getMethodName());
        String accessSpecs = "";

        Type returnType = method.getReturnType();
        ArrayList<Element> params = method.getParams();


        if(method.isConstructMethod())
        {
            //createConstructMethod();
            accessSpecs = "public ";
            System.out.println("construct method");
            String returnTypeStr = getReturnValueJasmin(returnType);
            //String instructionType = getInstrType(instrType);
            String paramStr = "";

            for (Element param : params)
            {
                paramStr += convertFieldTypeToPrimitiveJasmin(param.getType());
            }


            methodStr += accessSpecs + "<init>(" + paramStr + ")" + returnTypeStr + "\n";

        }
        else
        {
            String accModifiers = method.getMethodAccessModifier().name();
            accessSpecs = createAccessSpecsStr(accModifiers, method.isStaticMethod(), method.isFinalMethod());
            methodStr += accessSpecs + method.getMethodName();
        }

        String statements = createStatements(method.getInstructions());

        System.out.println("Labels: " + method.getLabels());

        methodStr += statements + "\n";
        methodStr += ".end method";


        return methodStr;

    }

    public String createJasminMethods(ArrayList<Method> methods ){

        //.method <access-spec> <method-name><method-signature>
        //<statements>
        //.end method

        String methodsStr = "";
        System.out.println("Printing "+ methods.size() + " Methods");

        for (Method method : methods)
        {
            String fieldStr = createMethod(method);
            methodsStr += fieldStr;
            break;
        }


        return methodsStr;
    }

    public String createAccessSpecsStr(String classType, Boolean isStatic, Boolean isFinal)
    {
        String accessSpecsStr = "";

        if(classType != "DEFAULT")
            accessSpecsStr += classType.toLowerCase() + " ";
        if(isStatic)
            accessSpecsStr += "static ";
        if(isFinal)
            accessSpecsStr += "final ";

        return accessSpecsStr;
    }

    public String createClassDirective(ClassUnit ollirClass){

        String classType = ollirClass.getClassAccessModifier().name();
        String accessSpecsStr = createAccessSpecsStr(classType, ollirClass.isStaticClass(), ollirClass.isFinalClass());
        String className = ollirClass.getClassName();
        String packageName = ollirClass.getPackage();

        if(packageName != null) {
            className = packageName + "/" + className;
        }

        return ".class " + accessSpecsStr + className + '\n';
    }

    public String createSuperDirective(ClassUnit ollirClass){

        String superClassName = ollirClass.getSuperClass();

        if(superClassName == null ) {
            superClassName = "java/lang/Object";
        }

        return ".super "  + superClassName + '\n';
    }

    public String createJasminHeader(ClassUnit ollirClass)
    {
        String classDirective = createClassDirective(ollirClass);
        String superDirective = createSuperDirective(ollirClass);

        return classDirective + superDirective;
    }
}
