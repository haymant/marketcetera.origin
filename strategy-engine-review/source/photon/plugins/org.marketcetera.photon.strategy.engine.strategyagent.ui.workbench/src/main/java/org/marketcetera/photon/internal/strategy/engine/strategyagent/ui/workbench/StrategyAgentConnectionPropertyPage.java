package org.marketcetera.photon.internal.strategy.engine.strategyagent.ui.workbench;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.marketcetera.photon.commons.ui.workbench.DataBindingPropertyPage;
import org.marketcetera.photon.strategy.engine.model.strategyagent.StrategyAgentEngine;
import org.marketcetera.photon.strategy.engine.strategyagent.ui.StrategyAgentEngineUI;
import org.marketcetera.photon.strategy.engine.ui.StrategyEngineImage;

public class StrategyAgentConnectionPropertyPage extends DataBindingPropertyPage {

    private StrategyAgentEngine mOriginalEngine;
    private StrategyAgentEngine mNewEngine;

    /**
     * Constructor.
     */
    public StrategyAgentConnectionPropertyPage() {
        setImageDescriptor(StrategyEngineImage.ENGINE_OBJ.getImageDescriptor());
        noDefaultAndApplyButton();
    }

    @Override
    protected Control createContents(Composite parent) {
        mOriginalEngine = (StrategyAgentEngine) getElement().getAdapter(
                StrategyAgentEngine.class);
        // make a copy so cancel works as expected
        mNewEngine = (StrategyAgentEngine) EcoreUtil.copy(mOriginalEngine);
        Composite composite = StrategyAgentEngineUI
                .createStrategyAgentConnectionComposite(parent,
                        getDataBindingContext(), mNewEngine);
        return composite;
    }

    @Override
    public boolean performOk() {
        mOriginalEngine.setJmsUrl(mNewEngine.getJmsUrl());
        mOriginalEngine.setWebServiceHostname(mNewEngine.getWebServiceHostname());
        mOriginalEngine.setWebServicePort(mNewEngine.getWebServicePort());
        return true;
    }

}
