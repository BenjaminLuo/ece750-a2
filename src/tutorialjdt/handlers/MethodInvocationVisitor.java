package tutorialjdt.handlers;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInvocationVisitor extends ASTVisitor {

    @Override
    public boolean visit(MethodInvocation node) {
        // Get the method name
        String methodName = node.getName().toString();

        // Get the declaring class if available
        IMethodBinding methodBinding = node.resolveMethodBinding();
        String declaringClass = (methodBinding != null && methodBinding.getDeclaringClass() != null)
                ? methodBinding.getDeclaringClass().getQualifiedName()
                : "UnknownClass";

        // Get the arguments of the method invocation
        String argumentTypes = node.arguments().toString();

        // Print method invocation details
        System.out.println("          * Method Invocation: " + declaringClass + "." + methodName + "(" + argumentTypes + ")");
        return super.visit(node);
    }
}
