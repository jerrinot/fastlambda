package info.jerrinot.fastlambda;

public class SingleClassClassLoader extends ClassLoader {
    private final String className;
    private final byte[] bytecode;

    public SingleClassClassLoader(String className, byte[] bytecode, ClassLoader parent) {
        super(parent);
        this.className = className;
        this.bytecode = bytecode;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.equals(className)) {
            synchronized (this) {
                Class<?> loadedClass = findLoadedClass(name);
                if (loadedClass == null) {
                    loadedClass = defineClass(name, bytecode, 0, bytecode.length);
                }
                return loadedClass;
            }
        }
        return super.loadClass(name, resolve);
    }
}
