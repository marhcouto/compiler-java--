package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JasminUtils {
    ClassUnit classUnit;

    JasminUtils(ClassUnit classUnit)
    {
        this.classUnit = classUnit;
    }

    public String getFullyQualifiedName(String className){

        if(className == null) return "java/lang/Object";

        for(var importString : this.classUnit.getImports()){
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

        if(this.classUnit.getClassName().equals(className)) return className;

        else throw new RuntimeException("Could not find import for class " + className);
    }

    public String getIload(int paramId){

        var code = new StringBuilder();

        code.append("iload");

        if(paramId < 4)
            code.append("_" + paramId);
        else
            code.append(" " + paramId);

        return code.toString();
    }

    public String getIStore(int paramId){

        var code = new StringBuilder();

        code.append("istore");

        if(paramId < 4)
            code.append("_" + paramId);
        else
            code.append(" " + paramId);

        return code.toString();
    }

    public String getIConst(String _const){

        var code = new StringBuilder();

        if(Integer.parseInt(_const) < 6)
            code.append("iconst_" + _const);
        else if (Integer.parseInt(_const) < 128)
            code.append("bipush " + _const);
        else
            code.append("ldc " +  _const);
        return code.toString();
    }

    public String loadArrayRefAndIndex(ArrayOperand operand, HashMap<String, Descriptor> varTable){
        StringBuilder code = new StringBuilder();
        if(operand.isParameter()){
            code.append("\taload ");
            code.append(operand.getParamId());
            code.append("\n\t");
        } else {
            var virtualReg = -1;

            if(varTable.get(operand.getName()) != null)
                virtualReg = varTable.get(operand.getName()).getVirtualReg();
            code.append("\taload ");
            code.append(virtualReg);
            code.append("\n\t");
        }
        code.append(this.loadArrayIndexes(operand, varTable));
        code.append("\n");
        return code.toString();
    }

    public String storeElement(Element element, HashMap<String, Descriptor> varTable)
    {
        String instrStr ="";
        if(element.isLiteral()){
            LiteralElement lit = (LiteralElement) element;
            switch (lit.getType().getTypeOfElement())
            {
                case INT32:
                    instrStr +="\t";
                    instrStr += getIStore(Integer.parseInt(lit.getLiteral()));
                    break;
                default:
                    instrStr +="\t";
                    instrStr += "store " + lit.getLiteral();
                    break;
            }

        } else {
            Operand operand = (Operand) element;
            boolean isArrayOperand = false;

            int id;
            if(operand.isParameter()){
                id = operand.getParamId();
            } else {
                id = varTable.get(operand.getName()).getVirtualReg();
            }

            if(operand instanceof ArrayOperand){
                isArrayOperand = true;
            }
            ElementType elemType = operand.getType().getTypeOfElement();

            if(id != -1) {
                switch (elemType) {
                    case INT32:
                        instrStr += "\t";
                        if (isArrayOperand) {
                            instrStr += "iastore";
                        } else {
                            instrStr += getIStore(id);
                        }
                        break;
                    case BOOLEAN:
                        instrStr += "\t";
                        instrStr += getIStore(id);
                        break;
                    case ARRAYREF:
                    case OBJECTREF:
                    case CLASS:
                    case STRING:
                        instrStr += "\t";
                        instrStr += "astore" + (id <= 3 ? '_' : ' ') + id;
                        break;
                    case THIS:
                        instrStr += "\t";
                        instrStr += "astore_0";
                        break;
                    case VOID:
                        break;
                }
            } else {
                    instrStr += "\tputfield " + getJasminType(elemType) + "/" + operand.getName() + " " + getJasminType(elemType);
                }

            }
        instrStr += "\n";
        return instrStr;
    }

    public String getJasminReturnType(ElementType type){
        switch (type)
        {
            case INT32:
                return "i";
            case BOOLEAN:
                return "i";
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "";
            case ARRAYREF:
                return "a";
            case OBJECTREF:
                return "a";
            default:
                throw new NotImplementedException(type);
        }
    }

    public String loadElement(Element element, HashMap<String, Descriptor> varTable){
        String instrStr ="";

        if(element.isLiteral()){
            LiteralElement lit = (LiteralElement) element;

            switch (lit.getType().getTypeOfElement())
            {
                case INT32:
                case BOOLEAN:
                    instrStr = getIConst(lit.getLiteral());
                    break;
                default:
                    instrStr = "\tldc " + lit.getLiteral();
                    break;
            }

        } else{

            Operand operand = (Operand) element;
            boolean isArrayOperand = false;
            if(operand instanceof ArrayOperand){
                operand.show();
                isArrayOperand = true;
            }
            int id;
            if(operand.isParameter()){
                id = operand.getParamId();
            } else {
                id = varTable.get(operand.getName()).getVirtualReg();
            }
            if(id != -1) {
                ElementType elemType = operand.getType().getTypeOfElement();
                switch (elemType) {
                    case INT32:
                        if (isArrayOperand) {
                            instrStr += "aload" + (id <= 3 ? '_' : ' ') + id + "\n\t";
                            instrStr += loadArrayIndexes((ArrayOperand) operand, varTable);
                            instrStr += "iaload";
                        } else {
                            instrStr = getIload(id);
                        }

                        break;
                    case BOOLEAN:
                        instrStr = getIload(id);
                        break;
                    case ARRAYREF:
                    case OBJECTREF:
                    case CLASS:
                    case STRING:
                        instrStr = "aload"+ (id <= 3 ? '_' : ' ') + id;
                        break;
                    case THIS:
                        instrStr = "aload_0";
                        break;
                    case VOID:
                        break;
                }
            } else {
                instrStr += "\tgetfield " + getJasminType(element.getType())+"/"+operand.getName() + " " ;
            }
        }
        instrStr += "\n";
        return instrStr;
    }

    public String getJasminType(Type type){
        return getJasminType(type, false);
    }

    public String getJasminType(Type type, boolean isMethodSignature)
    {
        System.out.println("Type = " + type + " " + isMethodSignature + " " + (type instanceof ClassType));
        if(type instanceof ArrayType)
        {
            return "[" + getJasminType(((ArrayType) type).getArrayType());
        }
        else if (type instanceof ClassType) {
            //return getJasminType(type.getTypeOfElement());
            if(isMethodSignature)
                return "L" + ((ClassType) type).getName() + ";";
            else
                return ((ClassType) type).getName();
        }
        else if (type instanceof Type)
            return getJasminType(type.getTypeOfElement());
        else{
            throw new NotImplementedException(type);
        }

    }

    public String getJasminType(ElementType type)
    {
        switch (type)
        {
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            /*TODO
            case ARRAYREF:
                //return "[" + ((ArrayType) typeOllir).getTypeOfElement();
                return "[";*/
            case OBJECTREF:
                return "";
            /*case CLASS:
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

    public String getFieldSpecs(Operand firstArg, String secondArg) {
        var code = new StringBuilder();

        switch (firstArg.getType().getTypeOfElement()) {
            case THIS:
                code.append(this.classUnit.getClassName() + "/");
                code.append(secondArg);
                break;
            default:
                code.append(getFullyQualifiedName(firstArg.getName()) + "/" + secondArg);
                break;
        }

        return code.toString();
    }

    private String loadArrayIndexes(ArrayOperand operand, HashMap<String, Descriptor> varTable){
        StringBuilder code = new StringBuilder();

        ArrayList<Element> list = operand.getIndexOperands();
        if(list.isEmpty()){
            return "";
        }

        code.append(this.loadElement(list.get(0), varTable)+ "\t") ;
        return code.toString();
    }

    public int checkIfIsPower2(int number)
    {
        int acc = 0;
        while(number % 2 == 0)
        {
            number /= 2;
            acc++;
        }

        if(number == 1) return acc;
        else return -1;

    }
}
