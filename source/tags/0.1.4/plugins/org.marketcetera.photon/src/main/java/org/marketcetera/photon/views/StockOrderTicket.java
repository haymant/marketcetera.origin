package org.marketcetera.photon.views;


import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.marketcetera.core.ClassVersion;
import org.marketcetera.core.InternalID;
import org.marketcetera.core.MSymbol;
import org.marketcetera.core.MarketceteraException;
import org.marketcetera.photon.Application;
import org.marketcetera.photon.actions.CommandEvent;
import org.marketcetera.photon.actions.ICommandListener;
import org.marketcetera.quickfix.FIXDataDictionaryManager;
import org.marketcetera.quickfix.FIXMessageUtil;

import quickfix.DataDictionary;
import quickfix.Message;
import quickfix.field.Account;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;

@ClassVersion("$Id$")
public class StockOrderTicket extends ViewPart {

	public static String ID = "org.marketcetera.photon.views.StockOrderTicket";

	private FormToolkit toolkit;

	private Form form;

	private FIXEnumeratedComposite buySellControl;

	private FIXStringComposite orderQtyControl;

	private FIXStringComposite symbolControl;

	private FIXStringComposite priceControl;

	private FIXEnumeratedComposite timeInForceControl;

	private FIXStringComposite accountControl;

	private Button sendButton;

	private Button cancelButton;

	private ICommandListener commandListener;

	public StockOrderTicket() {
		super();
		commandListener = new ICommandListener() {
			public void commandIssued(CommandEvent evt) {
				handleCommandIssued(evt);
			};
		};
	}

	protected void handleCommandIssued(CommandEvent evt) {
		if (evt.getDestination() == CommandEvent.Destination.EDITOR) {
			asyncPopulateFromMessage(evt.getMessage());
		}
	}

	@Override
	public void createPartControl(Composite parent) {

		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		// form.setText("Stock Order Ticket");
		GridLayout layout = new GridLayout();
		form.getBody().setLayout(layout);

		// FIXDataDictionaryManager.loadDictionary(FIXDataDictionaryManager.FIX_4_2_BEGIN_STRING);
		DataDictionary dict = FIXDataDictionaryManager.getDictionary();
		buySellControl = new FIXEnumeratedComposite(form.getBody(), SWT.NONE,
				toolkit, Side.FIELD, new String[] { "" + Side.BUY,
						"" + Side.SELL, "" + Side.SELL_SHORT,
						"" + Side.SELL_SHORT_EXEMPT });
		orderQtyControl = new FIXStringComposite(form.getBody(), SWT.NONE,
				toolkit, OrderQty.FIELD);
		toolkit.paintBordersFor(orderQtyControl);
		symbolControl = new FIXStringComposite(form.getBody(), SWT.NONE,
				toolkit, Symbol.FIELD);
		toolkit.paintBordersFor(symbolControl);
		priceControl = new FIXStringComposite(form.getBody(), SWT.NONE,
				toolkit, Price.FIELD);
		toolkit.paintBordersFor(priceControl);
		timeInForceControl = new FIXEnumeratedComposite(form.getBody(),
				SWT.NONE, toolkit, TimeInForce.FIELD, new String[] {
						"" + TimeInForce.DAY,
						"" + TimeInForce.GOOD_TILL_CANCEL,
						"" + TimeInForce.FILL_OR_KILL,
						"" + TimeInForce.IMMEDIATE_OR_CANCEL });
		timeInForceControl.setSelection("" + TimeInForce.DAY, true);
		accountControl = new FIXStringComposite(form.getBody(), SWT.NONE,
				toolkit, Account.FIELD);
		toolkit.paintBordersFor(accountControl);
		Composite okCancelComposite = toolkit.createComposite(form.getBody());
		okCancelComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		okCancelComposite.setLayoutData(gd);
		sendButton = toolkit.createButton(okCancelComposite, "Send", SWT.PUSH);
		cancelButton = toolkit.createButton(okCancelComposite, "Cancel",
				SWT.PUSH);
		cancelButton.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				handleCancel();
			}
		});
		sendButton.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				handleSend();
			}
		});

		Listener formValidationListener = new Listener() {
			public void handleEvent(Event event) {
				validateForm();
			}
		};
		buySellControl.addSelectionListener(formValidationListener);
		orderQtyControl.getTextControl().addListener(SWT.KeyUp, formValidationListener);
		symbolControl.getTextControl().addListener(SWT.KeyUp, formValidationListener); 
		priceControl.getTextControl().addListener(SWT.KeyUp, formValidationListener);
		timeInForceControl.addSelectionListener(formValidationListener);

		validateForm();
	}

	private void validateForm() {
		boolean sideValid = validateSide();
		updateLabel(buySellControl.getLabel(), sideValid);
		
		boolean orderQtyValid = validateOrderQty();
		updateLabel(orderQtyControl.getLabel(), orderQtyValid);
		
		boolean symbolValid = validateSymbol();
		updateLabel(symbolControl.getLabel(), symbolValid);
		
		boolean priceValid = validatePrice();
		updateLabel(priceControl.getLabel(), priceValid);
		
		boolean timeInForceValid = validateTimeInForce();
		updateLabel(timeInForceControl.getLabel(), timeInForceValid);
		
		boolean formValid = sideValid && orderQtyValid && symbolValid && priceValid && timeInForceValid;
		sendButton.setEnabled(formValid);
	}

	private boolean validateSide() {
		return buySellControl.hasSelection();
	}

	private boolean validateOrderQty() {
		String text = orderQtyControl.getTextControl().getText().trim();
		
		try {
			int orderQty = Integer.parseInt(text);

			if (orderQty <= 0)
				return false;
		}
		catch(NumberFormatException nfe) {
			return false;
		}
		
		return true;
	}
	
	private boolean validateSymbol() {
		String text = symbolControl.getTextControl().getText().trim();
		return !text.equals("");
	}
	
	private boolean validatePrice() {
		String text = priceControl.getTextControl().getText().trim();
		
		try {
			double price = Double.parseDouble(text);

			if (price <= 0.0d)
				return false;
		}
		catch(NumberFormatException nfe) {
			return false;
		}
		
		return true;
	}

	private boolean validateTimeInForce() {
		return timeInForceControl.hasSelection();
	}

	private void updateLabel(Label label, boolean fieldValid) {
		if (!fieldValid) {
			label.setForeground(label.getDisplay().getSystemColor(SWT.COLOR_RED));
		}
		else {
			label.setForeground(toolkit.getColors().getForeground());
		}
	}
	
	protected void handleSend() {
       try {
			String orderID = Application.getIDFactory().getNext();
	        Message aMessage = FIXMessageUtil.newLimitOrder(new InternalID(orderID), Side.BUY, BigDecimal.ZERO,
	        		new MSymbol(""), BigDecimal.ZERO, TimeInForce.DAY, null);
	        aMessage.removeField(Side.FIELD);
	        aMessage.removeField(OrderQty.FIELD);
	        aMessage.removeField(Symbol.FIELD);
	        aMessage.removeField(Price.FIELD);
	        aMessage.removeField(TimeInForce.FIELD);
 			populateMessageFromUI(aMessage);
			Application.getOrderManager().handleInternalMessage(aMessage);
			//clear();
		} catch (Exception e) {
			Application.getMainConsoleLogger().error("Error sending order", e);
		}
	}
	
	protected void handleCancel()
	{
		clear();
		validateForm();
	}
	
	protected void clear(){
		Control[] children = form.getBody().getChildren();
		for (Control control : children) {
			if (control instanceof FIXComposite) {
				FIXComposite composite = (FIXComposite) control;
				composite.clear();
			}
		}
	}


	/**
	 * Disposes the toolkit
	 */
	public void dispose() {
		super.dispose();
	}

	public void populateFromMessage(Message aMessage) {
		Control[] children = form.getBody().getChildren();
		for (Control control : children) {
			if (control instanceof FIXComposite) {
				FIXComposite composite = (FIXComposite) control;
				composite.populateFromMessage(aMessage);
			}
		}
	}
	
	private void populateMessageFromUI(Message aMessage) throws MarketceteraException{
		Control[] children = form.getBody().getChildren();
		for (Control control : children) {
			if (control instanceof FIXComposite) {
				FIXComposite composite = (FIXComposite) control;
				composite.modifyOrder(aMessage);
			}
		}
	}

	public void asyncExec(Runnable runnable) {
		Display display = this.getSite().getShell().getDisplay();

		// If the display is disposed, you can't do anything with it!!!
		if (display == null || display.isDisposed())
			return;

		display.asyncExec(runnable);
	}

	protected void asyncPopulateFromMessage(final Message aMessage) {
		asyncExec(new Runnable() {
			public void run() {
				populateFromMessage(aMessage);
			}
		});
	}

	/**
	 * @return Returns the commandListener.
	 */
	public ICommandListener getCommandListener() {
		return commandListener;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
}
