package tutorialjdt.handlers;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class MethodDeclarationVisitor extends ASTVisitor {
    private int throwsKitchenSinkCount = 0;
    private int throwsGenericCount = 0;
    private ICompilationUnit unit;
    
    public int getThrowsGenericCount() {
    	return this.throwsGenericCount;
    }
    
    public int getThrowsKitchenSinkCount() {
    	return this.throwsKitchenSinkCount;
    }
    
    public MethodDeclarationVisitor(ICompilationUnit unit) {
    	this.unit = unit;
    }
    

    private String getLocation(int startPosition) {
    	// Output the file, line number, and method declaration text
        try {
        	String fileNameString = unit.getResource().getLocation().toOSString();
            IDocument document = new Document(unit.getSource());
            int lineNumber = document.getLineOfOffset(startPosition) + 1;
            IRegion lineInfo = document.getLineInformation(lineNumber - 1);
            String lineText = document.get(lineInfo.getOffset(), lineInfo.getLength());
            // return "File: " + unit.getElementName() + ", Line: " + lineNumber + "\n" + lineText;
            
            // Check if the line starts with a decorator
            if (lineText.trim().startsWith("@")) {
                IRegion nextLineInfo = document.getLineInformation(lineNumber);
                String nextLineText = nextLineInfo != null ? document.get(nextLineInfo.getOffset(), nextLineInfo.getLength()) : "";
                return "\nFile: " + fileNameString + ", Line: " + lineNumber + "\n" + lineText + "\n" + nextLineText;
            } else {
                return "\nFile: " + fileNameString + ", Line: " + lineNumber + "\n" + lineText;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return "File: " + unit.getElementName() + ", Line: Unknown";
        }
    }
	
	@Override
	public boolean visit(MethodDeclaration node) {
		
		// Get list of exceptions in the method declaration
		@SuppressWarnings("unchecked")
		List<Type> exceptions = node.thrownExceptionTypes();
		List<String> genericExceptions = Arrays.asList("Exception", "RuntimeException", "IOException", "NullPointerException", "ArrayIndexOutOfBoundsException", "SQLException");

		// Count exceptions
		int numExceptions = exceptions.size();
		
		// Throws Generic
        for (Type e : exceptions) {
        	String exceptionTypeString = e.toString();
            if (genericExceptions.contains(exceptionTypeString)) {
            	this.throwsGenericCount += 1;
                System.out.println("[ANTIPATTERN WARNING] 'Throws Generic' anti-pattern detected: " + getLocation(node.getStartPosition()));
                System.out.println("----");
                break;
            }
        }

		// Throws Kitchen Sink
		if (numExceptions > 2) {
			this.throwsKitchenSinkCount += 1;
			System.out.println("[ANTIPATTERN WARNING] 'Throws Kitchen Sink' anti-pattern detected: " + getLocation(node.getStartPosition()));
			System.out.println("----");
		}

		return super.visit(node);
	}
}