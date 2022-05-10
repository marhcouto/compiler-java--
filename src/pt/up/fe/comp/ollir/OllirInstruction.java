package pt.up.fe.comp.ollir;

public class OllirInstruction {
    private String additionalCode;
    private String returnInstruction;

    public OllirInstruction(String returnInstruction) {
        this("", returnInstruction);
    }

    public OllirInstruction(String additionalCode, String returnInstruction) {
        this.additionalCode = additionalCode;
        this.returnInstruction = returnInstruction;
    }

    public String getAdditionalCode() {
        return additionalCode;
    }

    public String getReturnInstruction() {
        return returnInstruction;
    }
}
