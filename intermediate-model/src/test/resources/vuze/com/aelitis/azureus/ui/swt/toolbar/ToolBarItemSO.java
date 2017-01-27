/**
 * Created on Jul 22, 2008
 *
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA 
 */

package com.aelitis.azureus.ui.swt.toolbar;

import com.aelitis.azureus.ui.common.ToolBarItem;
import com.aelitis.azureus.ui.swt.skin.SWTSkinButtonUtility;
import com.aelitis.azureus.ui.swt.skin.SWTSkinObject;
import com.aelitis.azureus.ui.swt.skin.SWTSkinObjectText;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.plugins.ui.toolbar.UIToolBarItem;
import org.gudy.azureus2.ui.swt.pluginsimpl.UIToolBarItemImpl;

/**
 * @author TuxPaper
 * @created Jul 22, 2008
 *
 */
public class ToolBarItemSO
{
	private final static boolean DEBUG_TUX = false;

	private SWTSkinButtonUtility skinButton;

	private SWTSkinObjectText skinTitle;

	private boolean isDown;

	private UIToolBarItemImpl base;

	private SWTSkinObject so;

	public ToolBarItemSO(UIToolBarItemImpl base, SWTSkinObject so) {
		this.base = base;
		this.so = so;
	}

	public SWTSkinObject getSO() {
		return so;
	}

	public void setSkinButton(SWTSkinButtonUtility btn) {
		this.skinButton = btn;
		updateUI();
	}

	public SWTSkinButtonUtility getSkinButton() {
		return skinButton;
	}

	public void setSkinTitle(SWTSkinObjectText s) {
		skinTitle = s;
		skinTitle.setTextID(base.getTextID());
	}

	private void setEnabled(boolean enabled) {
		if (DEBUG_TUX) {
			if (Character.isDigit(base.getID().charAt(0))) {
				//if (base.getID().equals("up")) {
				System.out.println("setEnabeld " + enabled + "/" + base.getID()
						+ ";sb=" + Debug.getCompressedStackTrace());
			}
		}
		if (base.isAlwaysAvailable() && !enabled) {
			return;
		}
		if (skinButton != null) {
			skinButton.setDisabled(!enabled);
		}
	}

	public void dispose() {
		// ToolBarView will dispose of skinobjects
		skinButton = null;
		skinTitle = null;
	}

	public ToolBarItem getBase() {
		return base;
	}

	public void updateUI() {
		if (skinButton != null) {
			skinButton.setImage(base.getImageID());
		}
		if (skinTitle != null) {
			skinTitle.setTextID(base.getTextID());
		}
		if (base.isAlwaysAvailable()) {
			setEnabled(true);
		} else {
			long state = base.getState();
			setEnabled((state & UIToolBarItem.STATE_ENABLED) > 0);
			isDown = (state & UIToolBarItem.STATE_DOWN) > 0;
			if (skinButton != null) {
				skinButton.getSkinObject().switchSuffix(isDown ? "-selected" : "", 4,
						false);
			}
		}
	}

	public void setSO(SWTSkinObject so) {
		this.so = so;
	}

}
