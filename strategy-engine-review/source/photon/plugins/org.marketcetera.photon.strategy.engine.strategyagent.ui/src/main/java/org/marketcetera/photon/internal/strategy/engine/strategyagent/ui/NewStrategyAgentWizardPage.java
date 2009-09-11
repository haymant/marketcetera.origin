package org.marketcetera.photon.internal.strategy.engine.strategyagent.ui;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.marketcetera.photon.commons.Validate;
import org.marketcetera.photon.commons.ui.databinding.CustomWizardPageSupport;
import org.marketcetera.photon.strategy.engine.model.strategyagent.StrategyAgentEngine;
import org.marketcetera.photon.strategy.engine.ui.StrategyEngineUI;
import org.marketcetera.util.misc.ClassVersion;

/* $License$ */

/**
 * The wizard page that collects strategy agent engine parameters.
 * 
 * @author <a href="mailto:will@marketcetera.com">Will Horn</a>
 * @version $Id$
 * @since $Release$
 */
@ClassVersion("$Id$")
public class NewStrategyAgentWizardPage extends WizardPage {

    private final StrategyAgentEngine mEngine;
    private final DataBindingContext mDataBindingContext;
    private CustomWizardPageSupport mWizardSupport;

    /**
     * Constructor.
     * 
     * @param engine
     *            the model
     * @throws IllegalArgumentException
     *             if engine is null
     */
    public NewStrategyAgentWizardPage(StrategyAgentEngine engine) {
        super(NewStrategyAgentWizardPage.class.getName());
        Validate.notNull(engine, "engine"); //$NON-NLS-1$
        setTitle(Messages.NEW_STRATEGY_AGENT_WIZARD_PAGE__TITLE.getText());
        setDescription(Messages.NEW_STRATEGY_AGENT_WIZARD_PAGE__DESCRIPTION
                .getText());
        mEngine = engine;
        mDataBindingContext = new DataBindingContext();
    }

    @Override
    public void createControl(Composite parent) {
        mWizardSupport = CustomWizardPageSupport.create(this,
                mDataBindingContext);
        Composite composite = new Composite(parent, SWT.NONE);
        StrategyEngineUI.createStrategyEngineIdentificationComposite(composite,
                mDataBindingContext, mEngine);
        new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        new StrategyAgentConnectionComposite(composite, mDataBindingContext,
                mEngine);
        GridLayoutFactory.fillDefaults().generateLayout(composite);
        setControl(composite);
    }

    @Override
    public void dispose() {
        if (mWizardSupport != null) {
            mWizardSupport.dispose();
        }
        mDataBindingContext.dispose();
    }

}
