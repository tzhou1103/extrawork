//==================================================
//
//  Copyright 2012 Siemens Product Lifecycle Management Software Inc. All Rights Reserved.
//
//==================================================

package com.teamcenter.clientx;

import com.nio.tcserver.session.LoggerDefault;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.soa.client.model.PartialErrorListener;

/**
 * Implementation of the PartialErrorListener. Print out any partial errors returned.
 * 
 */
public class AppXPartialErrorListener implements PartialErrorListener {

	@Override
	public void handlePartialError(ErrorStack[] stacks) {
		if (stacks.length == 0)
			return;

		LoggerDefault.logError("");
		LoggerDefault.logError("*****");
		LoggerDefault.logError("Partial Errors caught in com.teamcenter.clientx.AppXPartialErrorListener.");

		for (int i = 0; i < stacks.length; i++) {
			ErrorValue[] errors = stacks[i].getErrorValues();
			LoggerDefault.logError("Partial Error for ");

			// The different service implementation may optionally associate
			// an ModelObject, client ID, or nothing, with each partial error
			if (stacks[i].hasAssociatedObject()) {
				LoggerDefault.logError("object " + stacks[i].getAssociatedObject().getUid());
			} else if (stacks[i].hasClientId()) {
				LoggerDefault.logError("client id " + stacks[i].getClientId());
			} else if (stacks[i].hasClientIndex())
				LoggerDefault.logError("client index " + stacks[i].getClientIndex());

			// Each Partial Error will have one or more contributing error
			// messages
			for (int j = 0; j < errors.length; j++) {
				LoggerDefault.logError("    Code: " + errors[j].getCode() + "\tSeverity: " + errors[j].getLevel()
						+ "\t" + errors[j].getMessage());
			}
		}

	}

}
