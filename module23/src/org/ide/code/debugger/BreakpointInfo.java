package org.ide.code.debugger;

public class BreakpointInfo {
    private final String className;
    private final int line;

    public BreakpointInfo(String className, int line) {
        this.className = className;
        this.line = line;
    }

    public String getClassName() {
        return className;
    }

    public int getLine() {
        return line;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BreakpointInfo other)) return false;
        return className.equals(other.className) && line == other.line;
    }

    @Override
    public int hashCode() {
        return className.hashCode() * 31 + line;
    }

    @Override
    public String toString() {
        return className + ":" + line;
    }
}

