package org.marketcetera.photon.preferences;

import java.io.IOException;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.marketcetera.photon.Messages;
import org.marketcetera.photon.PhotonPlugin;


/**
 * "Script Registry" preference page.
 *  
 * @author gmiller
 * @author andrei@lissovski.org
 */
public class ScriptRegistryPage
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage, Messages
{

	//agl todo:refactor move this out as we're using it to retrieve the preference elsewhere
	public static final String SCRIPT_REGISTRY_PREFERENCE = "script.registry"; //$NON-NLS-1$

	
	public ScriptRegistryPage() {
		super(GRID);
        setPreferenceStore(PhotonPlugin.getDefault().getPreferenceStore());
	}
	
    public void init(IWorkbench workbench)
    {
    }

    @Override
	protected void createFieldEditors() {
		Composite fieldEditorParent = getFieldEditorParent();
		ScriptRegistryListEditor mapEditor = new ScriptRegistryListEditor(SCRIPT_REGISTRY_PREFERENCE,
		                                                                  SCRIPT_REGISTRY_LABEL.getText(),
		                                                                  fieldEditorParent);
		addField(mapEditor);
	}

    @Override
    public boolean performOk() {
        try {
            ((ScopedPreferenceStore)getPreferenceStore()).save();  // persists the preference store to disk
        } catch (IOException e) {
            //TODO: do something
        }
        return super.performOk();
    }

}
