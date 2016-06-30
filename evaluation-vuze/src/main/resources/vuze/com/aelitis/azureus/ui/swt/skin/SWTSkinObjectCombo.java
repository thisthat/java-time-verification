/**
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

package com.aelitis.azureus.ui.swt.skin;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.ui.swt.Utils;

import com.aelitis.azureus.ui.swt.utils.FontUtils;

/**
 * Native combobox
 * 
 * @author TuxPaper
 *
 */
public class SWTSkinObjectCombo
	extends SWTSkinObjectBasic
{
	private Combo widget;
	
	private String 	text = "";
	
	public SWTSkinObjectCombo(SWTSkin skin, SWTSkinProperties properties,
			String id, String configID, SWTSkinObject parentSkinObject) {
		super(skin, properties, id, configID, "combo", parentSkinObject);

		Composite createOn;
		if (parent == null) {
			createOn = skin.getShell();
		} else {
			createOn = (Composite) parent.getControl();
		}

		int style = SWT.BORDER;
		
		String styleString = properties.getStringValue(sConfigID + ".style");
		if (styleString != null) {
			String[] styles = Constants.PAT_SPLIT_COMMA.split(styleString.toLowerCase());
			Arrays.sort(styles);
			if (Arrays.binarySearch(styles, "readonly") >= 0) {
				style |= SWT.READ_ONLY;
			}
		}
		
		widget = new Combo(createOn, style);
		
		widget.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				text = widget.getText();
			}
		});
		
		setControl(widget);
		updateFont("");
	}

	// @see com.aelitis.azureus.ui.swt.skin.SWTSkinObjectBasic#switchSuffix(java.lang.String, int, boolean)
	public String switchSuffix(String suffix, int level, boolean walkUp,
			boolean walkDown) {
		suffix = super.switchSuffix(suffix, level, walkUp, walkDown);

		if (suffix == null) {
			return null;
		}

		String sPrefix = sConfigID + ".text";
		String text = properties.getStringValue(sPrefix + suffix);
		if (text != null) {
			setText(text);
		}

		return suffix;
	}

	public void setText(final String val) {
		Utils.execSWTThread(new AERunnable() {
			public void runSupport() {
				if (widget != null && !widget.isDisposed()) {
					widget.setText(val == null ? "" : val);
					text = val;
				}
			}
		});

	}

	public String getText() {
		return text;
	}
	
	public void setList(final String[] list) {
		Utils.execSWTThread(new AERunnable() {
			public void runSupport() {
				if (widget != null && !widget.isDisposed()) {
					widget.setItems(list);
				}
			}
		});
	}

	public Combo getComboControl() {
		return widget;
	}


	private void updateFont(String suffix) {
		String sPrefix = sConfigID + ".text";

		Font existingFont = (Font) widget.getData("Font" + suffix);
		if (existingFont != null && !existingFont.isDisposed()) {
			widget.setFont(existingFont);
		} else {
			boolean bNewFont = false;
			float fontSize = -1;
			String sFontFace = null;
			FontData[] tempFontData = widget.getFont().getFontData();

			sFontFace = properties.getStringValue(sPrefix + ".font" + suffix);
			if (sFontFace != null) {
				tempFontData[0].setName(sFontFace);
				bNewFont = true;
			}

			// Can't use properties.getPxValue for fonts, because
			// font.height isn't necessarily in px.
			String sSize = properties.getStringValue(sPrefix + ".size" + suffix);
			if (sSize != null) {
				FontData[] fd = widget.getFont().getFontData();

				sSize = sSize.trim();
				try {
					char firstChar = sSize.charAt(0);
					char lastChar = sSize.charAt(sSize.length() - 1);
					if (firstChar == '+' || firstChar == '-') {
						sSize = sSize.substring(1);
					} else if (lastChar == '%') {
						sSize = sSize.substring(0, sSize.length() - 1);
					}

					float dSize = NumberFormat.getInstance(Locale.US).parse(sSize).floatValue();

					if (lastChar == '%') {
						fontSize = FontUtils.getHeight(fd) * (dSize / 100);
					} else if (firstChar == '+') {
						//int curPX = FontUtils.getFontHeightInPX(tempFontData);
						//fontSize = FontUtils.getFontHeightFromPX(textWidget.getDisplay(),
						//		tempFontData, null, (int) (curPX + dSize));
						fontSize = (int) (fd[0].height + dSize);
					} else if (firstChar == '-') {
						fontSize = (int) (fd[0].height - dSize);
					} else {
						if (sSize.endsWith("px")) {
							//iFontSize = Utils.getFontHeightFromPX(textWidget.getFont(), null, (int) dSize);
							fontSize = FontUtils.getFontHeightFromPX(widget.getDisplay(),
									tempFontData, null, (int) dSize);
							//iFontSize = Utils.pixelsToPoint(dSize, textWidget.getDisplay().getDPI().y);
						} else if (sSize.endsWith("rem")) {
							fontSize = FontUtils.getHeight(fd) * dSize;
						} else {
							fontSize = FontUtils.getFontHeightFromPX(widget.getDisplay(),
									tempFontData, null, Utils.adjustPXForDPI((int) dSize));
						}
					}

					bNewFont = true;
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (bNewFont) {
				FontData[] fd = widget.getFont().getFontData();

				if (fontSize > 0) {
					FontUtils.setFontDataHeight(fd, fontSize);
				}

				if (sFontFace != null) {
					fd[0].setName(sFontFace);
				}

				final Font textWidgetFont = new Font(widget.getDisplay(), fd);
				widget.setFont(textWidgetFont);
				widget.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						textWidgetFont.dispose();
					}
				});

				widget.setData("Font" + suffix, textWidgetFont);
			}
		}
	}
}
