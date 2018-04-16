package com.tantawowa.rokucomfort;

import java.io.Console;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SwitchFileHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public SwitchFileHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information from
	 * the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		IWorkbenchPart workbenchPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActivePart();
		IEditorInput input = workbenchPart.getSite().getPage().getActiveEditor().getEditorInput();
		IPath path = ((FileEditorInput) input).getPath();
		ILog log = Activator.getDefault().getLog();
		log.log(new Status(0, "NONE", "INPUT FILE " + path.toString()));
		log.log(new Status(0, "NONE", "INPUT FILE XN" + path.getFileExtension()));

		LoadFileWithExtension(workbenchPart, path,
				path.getFileExtension().toLowerCase().equals("xml") ? ".brs" : ".xml");

		return null;
	}

	public static void LoadFileWithExtension(IWorkbenchPart workbenchPart, IPath path, String newExtension) {
		ILog log = Activator.getDefault().getLog();
		IFile file = GetFilewithExtension(new Path(path.removeFileExtension().toString() + newExtension.toString()));
		IPath pathToUse = new Path(path.removeFileExtension().toString() + newExtension.toString());

		if (file != null) {
			IWorkbenchPage page = workbenchPart.getSite().getPage();
			IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
					.getDefaultEditor(pathToUse.toFile().getName());
			log.log(new Status(0, "NONE", "OUTPUT file " + pathToUse.toString()));

			try {
				page.openEditor(new FileEditorInput(file), desc.getId());

			} catch (PartInitException e) {
				log.log(new Status(0, "NONE", "ERROR " + pathToUse.toString()));
			}
		} else {
			log.log(new Status(0, "NONE", "NO FILE MATCHING " + pathToUse.toString()));
		}
	}

	public static IFile GetFilewithExtension(IPath path) {

		ILog log = Activator.getDefault().getLog();
		log.log(new Status(0, "NONE", "PATH TO USE file " + path.toString()));

		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
		return (files.length > 0) ? files[0] : null;
	}
}
