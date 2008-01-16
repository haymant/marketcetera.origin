package org.marketcetera.photon.actions;

import org.eclipse.jface.action.Action;
import org.marketcetera.photon.IImageKeys;
import org.marketcetera.photon.PhotonPlugin;
import org.marketcetera.photon.views.IHeartbeatsToggle;

public class ShowHeartbeatsAction extends Action {

	private static final String ID = "org.marketcetera.photon.actions.ShowHeartbeatsAction";
	private IHeartbeatsToggle view;

	public ShowHeartbeatsAction(IHeartbeatsToggle view) {
		this.view = view;
		setId(ID);
		setText("&Show heartbeats");
		setToolTipText("Show heartbeats");
		setImageDescriptor(PhotonPlugin.getImageDescriptor(IImageKeys.SHOW_HEARTBEATS));
		setChecked(false);
	}

	@Override
	public void run() {
		view.setShowHeartbeats(isChecked());
	}

}
