package org.marketcetera.photon.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.ListenerList;

public class Portfolio extends PositionProgress
{
	private List<PositionProgress> entries;
	private Portfolio parent;
	private String name;
	private ListenerList listeners;

	public Portfolio(Portfolio parent, String name) {
		this.name = name;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.marketcetera.photon.model.PositionProgress#getProgress()
	 */
	@Override
	public double getProgress() {
		double total = 0;
		double progress = 0;
		for (PositionProgress anEntry : entries) {
			total += 1;
			progress += anEntry.getProgress();
		}
		return (total > 0 ? (progress/total) : 0);
	}

	public Portfolio getParent() {
		return parent;
	}

	public void rename(String newName) {
		this.name = newName;
		firePositionChanged(null);
	}

	public void addEntry(PositionProgress entry) {
		if (entries == null)
			entries = new ArrayList(5);
		entries.add(entry);
		firePositionChanged(entry);
	}

	public void removeEntry(PositionProgress entry) {
		if (entries != null) {
			entries.remove(entry);
			if (entries.isEmpty())
				entries = null;
		}
		firePositionChanged(entry);
	}
	
	public void updateEntry(PositionProgress entry){
		firePositionChanged(entry);
	}

	public PositionProgress[] getEntries() {
		if (entries != null)
			return (PositionProgress[]) entries.toArray(new PositionProgress[entries.size()]);
		return new PositionProgress[0];
	}

	public void addPositionListener(IPortfolioListener listener) {
		if (parent != null)
			parent.addPositionListener(listener);
		else {
			if (listeners == null)
				listeners = new ListenerList();
			listeners.add(listener);
		}
	}

	public void removeContactsListener(IPortfolioListener listener) {
		if (parent != null)
			parent.removeContactsListener(listener);
		else {
			if (listeners != null) {
				listeners.remove(listener);
				if (listeners.isEmpty())
					listeners = null;
			}
		}
	}

	protected void firePositionChanged(PositionProgress entry) {
		if (parent != null)
			parent.firePositionChanged(entry);
		else {
			if (listeners == null)
				return;
			Object[] rls = listeners.getListeners();
			for (int i = 0; i < rls.length; i++) {
				IPortfolioListener listener = (IPortfolioListener) rls[i];
				listener.positionsChanged(this, entry);
			}
		}
	}


}
