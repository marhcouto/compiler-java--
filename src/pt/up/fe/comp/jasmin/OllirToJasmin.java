package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.io.IOError;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class OllirToJasmin {

    private final ClassUnit classUnit;

    public OllirToJasmin(ClassUnit classUnit){
        this.classUnit = classUnit;
    }

    public String getCode()
    {
        var code = new StringBuilder();

        classUnit.show();

        code.append(createJasminHeader());
        code.append(createJasminFields());
        code.append(createMethods());

        return code.toString();
    }

    public String getFullyQualifiedName(String className){

        if(className == null) return "java/lang/Object";

        for(var importString : classUnit.getImports()){
            var splittedImport = importString.split("\\.");
            String lastName;

            if(splittedImport.length == 0){
                lastName = importString;
            }
            else{
                lastName = splittedImport[splittedImport.length-1];

            }
            if(lastName.equals(className)){
                return importString.replace('.', '/');
            }
        }
        System.out.println("classUnit: " + classUnit.getClassName() + " Arg: " + className);

        if(classUnit.getClassName().equals(className)) return className;

        else throw new RuntimeException("Could not find import for class " + className);
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

    public String createClassDirective(){

        String classType = classUnit.getClassAccessModifier().name();
        String accessSpecsStr = createAccessSpecsStr(classType, classUnit.isStaticClass(), classUnit.isFinalClass());
        String className = classUnit.getClassName();
        String packageName = classUnit.getPackage();

        if(packageName != null) {
            className = packageName + "/" + className;
        }

        return ".class " + accessSpecsStr + className + '\n';
    }

    public String createSuperDirective(){

        String superClassName = classUnit.getSuperClass();
        String qualifiedSuperClassName = getFullyQualifiedName(superClassName);

        return ".super "  + qualifiedSuperClassName + '\n';
    }

    public String createJasminHeader()
    {
        String classDirective = createClassDirective();
        String superDirective = createSuperDirective();

        return classDirective + superDirective + "\n";
    }

    public String createJasminFields(){

        ArrayList<Field> fields = classUnit.getFields();

        var code = new StringBuilder();

        for (Field field : fields)
        {
            code.append(createField(field) + '\n');
        }

        code.append("\n");
        return code.toString();
    }

    public String createField(Field field)
    {
        var code = new StringBuilder();

        code.append(".field ");

        String accModifiers = field.getFieldAccessModifier().name();
        String accessModifiers = createAccessSpecsStr(accModifiers, field.isStaticField(), field.isFinalField());

        code.append(accessModifiers + field.getFieldName() + " ");
        String fieldType = getJasminType(field.getFieldType());

        if(field.isInitialized())
            code.append("=" + field.getInitialValue());

        code.append(fieldType);

        return code.toString();
    }

    public String createConstructMethod(String superClassName){

        System.out.println("here" + superClassName);
        String constructStr = SpecsIo.getResource("fixtures/public/jasmin/jasminConstructor.template").replace("$<SUPERCLASS_NAME>", superClassName);


        return constructStr;

    }

    public String createMethods( ){

        String methodsStr = "";
        ArrayList<Method> methods = classUnit.getMethods();

        for (Method method : methods)
        {
            String fieldStr = getCode(method);
            methodsStr += fieldStr;
        }

        return methodsStr;
    }

    public String getJasminType(Type type)
    {
        if(type instanceof ArrayType)
        {
            return "[" + getJasminType(((ArrayType) type).getArrayType());
        }
        else if(type instanceof ClassType)
            return ((ClassType) type).getName();
        else if(type instanceof Type)
            return getJasminType(type.getTypeOfElement());
        else{
            throw new NotImplementedException(type.getTypeOfElement());
        }

    }

    public String getJasminType(ElementType type)
    {
        System.out.println(type);
        switch (type)
        {
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            /*TODO
            case ARRAYREF:
                //return "[" + ((ArrayType) typeOllir).getTypeOfElement();
                return "[";
            case OBJECTREF:
                //return "L" + getFullyQualifiedName(classUnit.getSuperClass()) + ";";
                return "";
            case CLASS:
                return "[" + classUnit.getClassName() + ";";
            case THIS:
                return getFullyQualifiedName(classUnit.getSuperClass());*/
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "V";
            default:
                throw new NotImplementedException(type);
        }

    }

    public String createMethodBody(Method method){
        var code = new StringBuilder();

        String accessSpecs = createAccessSpecsStr(method.getMethodAccessModifier().name(), method.isStaticMethod(), method.isFinalMethod());
        code.append(accessSpecs + method.getMethodName() + '(');

        var paramsTypes = method.getParams().stream()
                .map(element -> getJasminType(element.getType()))
                .collect(Collectors.joining());
        code.append(paramsTypes).append(")").append(getJasminType(method.getReturnType()) + '\n');

        code.append("\t.limit stack 99\n");
        code.append("\t.limit locals 99\n");

        for(var instruction : method.getInstructions())
        {
            code.append(getCode(instruction));
        }

        return code.toString();
    }

    public String createNonConstructMethod(Method method){

        var code = new StringBuilder();

        code.append(".method ");
        code.append(createMethodBody(method));
        code.append("\treturn \n");
        code.append(".end method\n");

        return code.toString();
    }

    public String getCode(Method method){

        var code = new StringBuilder();

        if(method.isConstructMethod())
        {
            code.append(createConstructMethod(getFullyQualifiedName(classUnit.getSuperClass())));
        }
        else
        {
            code.append(createNonConstructMethod(method));
        }

        return code.toString();

    }

    public String getCode(Instruction instruction){
        FunctionClassMap<Instruction, String> instructionMap = new FunctionClassMap<>();

        instructionMap.put(CallInstruction.class, this::getCode);
        instructionMap.put(PutFieldInstruction.class, this::getCode);
        instructionMap.put(AssignInstruction.class, this::getCode);
        return instructionMap.apply(instruction);

        //throw new NotImplementedException(instruction.getInstType());

    }

    private String getCodeInvokeStatic(CallInstruction instruction)
    {
        var code = new StringBuilder();
        var methodClass = ((Operand) instruction.getFirstArg()).getName();
        var methodName = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");;

        code.append("invokestatic " + getFullyQualifiedName(methodClass));
        code.append("/" + methodName + "(");

        //TODO-Exemplo
        var operandsTypes = instruction.getListOfOperands().stream()
                .map(element -> getJasminType(element.getType()))
                .collect(Collectors.joining());
        code.append(operandsTypes).append(")").append(getJasminType(instruction.getReturnType()) + '\n');

        return code.toString();

    }

    //TODO
    private String getCodeInvokeVirtual(CallInstruction instruction)
    {
        var code = new StringBuilder();

        System.out.println("In invoke virtual");

        var firstArg = ((Operand) instruction.getFirstArg());
        var secondArg = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");

        System.out.println(firstArg.isParameter());
        code.append("\taload_2\n");
        code.append("\ticonst_2\n");

        code.append("\tinvokevirtual " + getJasminType(firstArg.getType()) + "(");

        var operandsTypes = instruction.getListOfOperands().stream()
                .map(element -> getJasminType(element.getType()))
                .collect(Collectors.joining());
        code.append(operandsTypes).append(")").append(getJasminType(instruction.getReturnType()) + '\n');

        return code.toString();

    }

    private String getCodeNewInstr(CallInstruction instruction)
    {
        var code = new StringBuilder();
        var firstArg = ((Operand) instruction.getFirstArg());

        //TODO
        //instruction.getListOfOperands();

        code.append("\tnew " + firstArg.getName() + "\n");

        System.out.println(code);

        return code.toString();
    }

    private String getCodeInvokeSpecial(CallInstruction instruction)
    {
        var code = new StringBuilder();

        var firstArg = ((Operand) instruction.getFirstArg());
        var methodName = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");;

        code.append("\tinvokespecial " + getJasminType(firstArg.getType()));
        code.append("/" + methodName + "(");

        //TODO-Exemplo
        var operandsTypes = instruction.getListOfOperands().stream()
                .map(element -> getJasminType(element.getType()))
                .collect(Collectors.joining());

        code.append(operandsTypes).append(")").append(getJasminType(instruction.getReturnType()) + '\n');

        System.out.println(code.toString());
        return code.toString();


    }

    public String getCode(CallInstruction instruction) {

        System.out.println("Tipo de instrução: " + instruction.getInvocationType());
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

    public String getIload(int paramId){

        var code = new StringBuilder();

        code.append("iload");

        if(paramId < 4)
            code.append("_" + paramId);
        else
            code.append(" " + paramId);

        code.append("\n");

        return code.toString();
    }

    public String getTypeLoad(Operand arg){

        System.out.println(arg.getType().getTypeOfElement());
        System.out.println(arg.isParameter());
        System.out.println(arg.getParamId());

        switch (arg.getType().getTypeOfElement()){
            case INT32:
                if(arg.isParameter()) return getIload(arg.getParamId());
                break;
            case THIS:
                return "aload_0\n";
        }
        throw new NotImplementedException(arg.getType());
    }


    public String getFieldSpecs(Operand firstArg, String secondArg)
    {
        var code = new StringBuilder();

        System.out.println("TIPO: " + firstArg.getType().getTypeOfElement());

        switch (firstArg.getType().getTypeOfElement()){
            case THIS:
                code.append(classUnit.getClassName() + "/");
                code.append(secondArg);
                break;
            default:
                code.append(getFullyQualifiedName(firstArg.getName()) + "/" + secondArg);

        }

        return code.toString();
    }

    public String getCode(PutFieldInstruction instruction) {

        var code = new StringBuilder();

        instruction.show();

        var firstArg = ((Operand) instruction.getFirstOperand());
        var secondArg = instruction.getSecondOperand();
        String secondArgStr = "";
        var thirdArg = instruction.getThirdOperand();

        /*code.append("\t" + getTypeLoad(firstArg));
        code.append("\t" + getTypeLoad(thirdArg));*/
        code.append("\taload_0\n");
        code.append("\ticonst_2\n");

       // code.append("\t" + getTypeLoad(secondArg));

        //code.append("\tputfield ");
        /*if (secondArg.isLiteral()) {
            secondArgStr = ((LiteralElement)secondArg).getLiteral();
        } else {
            var o1 = (Operand)secondArg;
            secondArgStr = o1.getName();

        }*/
        System.out.println("here");
        //code.append(getFieldSpecs(firstArg, secondArgStr) + " ");
        code.append("\tputfield myClass/a I");
        //code.append(getJasminType(thirdArg.getType()));
        code.append("\n");

        System.out.println(code);

        return code.toString();
    }

    public String getCode(AssignInstruction instruction)
    {
        var code = new StringBuilder();
        System.out.println("ASSIGN INSTRUCTION");
        System.out.println("\t" + instruction.getInstType() + " ");

        var o1 = (Operand) instruction.getDest();
        System.out.print("Operand: " + o1.getName() + " " + o1.getType());
        System.out.println(instruction.getRhs().getClass());

        code.append(getCode(instruction.getRhs()));
        /*code.append("\tputfield ");
        if(o1.isParameter()){
            code.append(o1.getParamId());
        } else {
            code.append(getJasminType(o1.getType()) + "/" + o1.getName() + " ");
        }
        code.append(getJasminType(o1.getType()));
        code.append("\n");*/


        System.out.println(code.toString());

        return code.toString();
    }


}
