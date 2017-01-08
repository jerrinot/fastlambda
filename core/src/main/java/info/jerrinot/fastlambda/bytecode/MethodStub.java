package info.jerrinot.fastlambda.bytecode;

public class MethodStub {
    private final String name;
    private final byte[] bytecode;
    private final byte[] constantPool;
    private final String descriptor;
    private final int maxLocals;

    public MethodStub(byte[] bytecode, byte[] constantPool, String descriptor, int maxLocals, String name) {
        this.bytecode = bytecode;
        this.constantPool = constantPool;
        this.descriptor = descriptor;
        this.maxLocals = maxLocals;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public byte[] getBytecode() {
        return bytecode;
    }

    public byte[] getConstantPool() {
        return constantPool;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public int getMaxLocals() {
        return maxLocals;
    }
}
