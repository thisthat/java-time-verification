/*
 * File    : CategoryItem.java
 * Created : 01 feb. 2004
 * By      : TuxPaper
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.aelitis.azureus.plugins.net.buddy.swt.columns;

import com.aelitis.azureus.plugins.net.buddy.BuddyPluginBeta.ChatInstance;
import com.aelitis.azureus.ui.swt.columns.ColumnCheckBox;
import org.gudy.azureus2.plugins.ui.tables.TableColumn;



public class 
ColumnChatFavorite
	extends ColumnCheckBox
{	
	public static String COLUMN_ID = "chat.fave";

	public 
	ColumnChatFavorite(
		TableColumn column ) 
	{
		super( column, 60 );
		column.setRefreshInterval( TableColumn.INTERVAL_LIVE );
	}
	
	@Override
	protected Boolean 
	getCheckBoxState(
		Object datasource ) 
	{
		ChatInstance chat = (ChatInstance)datasource;
		
		if ( chat != null ){
							
			return( chat.isFavourite());
		}
		
		return( null );
	}
	
	@Override
	protected void 
	setCheckBoxState(
		Object 	datasource,
		boolean set ) 
	{
		ChatInstance chat = (ChatInstance)datasource;
		
		if ( chat != null ){
			
			chat.setFavourite( set );
		}
	}
}
