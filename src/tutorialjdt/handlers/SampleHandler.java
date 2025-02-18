package tutorialjdt.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class SampleHandler extends AbstractHandler {


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();
		

		for (IProject project : projects) {
		    try {
		    	// 1. Project currently open in Eclipse Workspace
		    	// 2. Project has Java nature (configured to support java-specific features like building, compiling, running Java code) to ensure it's a Java project
				if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
					System.out.println("Project: " + project.getName());
				    IJavaProject javaProject = JavaCore.create(project);
				    analyzeJavaProject(javaProject);
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		System.out.println("Finish");
		
		return null;
	}
	
	private void analyzeJavaProject(IJavaProject javaProject) {
	    try {
	        for (IPackageFragment pkg : javaProject.getPackageFragments()) {
	            if (pkg.getKind() == IPackageFragmentRoot.K_SOURCE) { // Only source packages
	            	System.out.println("  Package: " + pkg.getElementName());
	                analyzePackage(pkg);
	            }
	        }
	    } catch (JavaModelException e) {
	        e.printStackTrace();
	    }
	}
	
	private void analyzePackage(IPackageFragment pkg) {
	    try {
	        for (ICompilationUnit unit : pkg.getCompilationUnits()) {
	            analyzeCompilationUnit(unit);
	        }
	    } catch (JavaModelException e) {
	        e.printStackTrace();
	    }
	}

	
	private void analyzeCompilationUnit(ICompilationUnit unit) {
		System.out.println("    Compilation Unit: " + unit.getElementName());
	    ASTParser parser = ASTParser.newParser(AST.JLS22);
	    parser.setSource(unit);
	    parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);	
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		
	    CompilationUnit astRoot = (CompilationUnit) parser.createAST(null); // Parse the code
	    
//	    TraverseMethods traverseMethods = new TraverseMethods(); // Traverse the AST to find method declarations
//	    traverseMethods = traverseMethods.findMethodInvocation(true);  // Traverse the AST to find methods declaration and invocations
//	    traverseMethods = traverseMethods.findControlBlock(true); // Traverse the AST to find methods declaration and control blocks
//	    astRoot.accept(traverseMethods); 
//	    System.out.println("Detected number of declared methods: " + Integer.toString(traverseMethods.getNumberOfMethods()));

	    TryVisitor tryVisitor = new TryVisitor(unit);
	    astRoot.accept(tryVisitor);
	    
	    MethodDeclarationVisitor methodVisitor = new MethodDeclarationVisitor(unit);
	    astRoot.accept(methodVisitor);
	    
	    System.out.println("Number of 'Throw Within Finally': " + Integer.toString(tryVisitor.getThrowWithinFinallyCount()));
	    System.out.println("Number of 'Log and Throw': " + Integer.toString(tryVisitor.getLogAndThrowCount()));
	    System.out.println("Number of 'Throws Generic': " + Integer.toString(methodVisitor.getThrowsGenericCount()));
	    System.out.println("Number of 'Throws Kitchen Sink': " + Integer.toString(methodVisitor.getThrowsKitchenSinkCount()));

	}

}

