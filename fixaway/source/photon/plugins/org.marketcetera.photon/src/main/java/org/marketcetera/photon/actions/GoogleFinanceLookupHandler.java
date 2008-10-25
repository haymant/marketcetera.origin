package org.marketcetera.photon.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.marketcetera.core.MSymbol;
import org.marketcetera.photon.ui.ISymbolProvider;
import org.marketcetera.photon.views.WebBrowserView;
import org.marketcetera.util.misc.ClassVersion;

/* $License$ */

/**
 * Handler that opens the browser view to show Google Finance information for
 * the selected {@link ISymbolProvider}.
 * 
 * @author <a href="mailto:will@marketcetera.com">Will Horn</a>
 * @version $Id$
 * @since $Release$
 */
@ClassVersion("$Id$")//$NON-NLS-1$
public class GoogleFinanceLookupHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;
			Object obj = sselection.getFirstElement();
			if (obj instanceof ISymbolProvider) {
				MSymbol symbol = ((ISymbolProvider) obj).getSymbol();
				IWorkbenchWindow window = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				IViewPart view;
				try {
					view = page.showView(WebBrowserView.ID);
					((WebBrowserView) view).browseToGoogleFinanceForSymbol(symbol);
				} catch (PartInitException e) {
					throw new ExecutionException("Unable to open view", e); //$NON-NLS-1$
				}
			}
		}
		return null;
	}

}
