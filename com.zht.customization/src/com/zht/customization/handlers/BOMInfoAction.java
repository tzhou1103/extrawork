package com.zht.customization.handlers;

import java.awt.Frame;

import com.teamcenter.rac.aif.common.actions.AbstractAIFAction;

public class BOMInfoAction extends AbstractAIFAction {

	public BOMInfoAction(Frame arg0, String arg1) {
		super(arg0, arg1);
	}

	@Override
	public void run() {
		BOMInfoCommand bomInfoCommand = new BOMInfoCommand();
		try {
			bomInfoCommand.executeModal();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
