/**
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 */
 
package com.aelitis.azureus.ui.swt.shells.opentorrent;

import com.aelitis.azureus.ui.common.table.TableColumnCore;
import org.gudy.azureus2.core3.torrent.impl.TorrentOpenFileOptions;
import org.gudy.azureus2.plugins.ui.tables.*;

public class TableColumnOTOF_Position
implements TableCellRefreshListener, TableColumnExtraInfoListener
{
	public static final String COLUMN_ID = "#";
  
  /** Default Constructor */
  public TableColumnOTOF_Position(TableColumn column) {
  	column.initialize(TableColumn.ALIGN_TRAIL, TableColumn.POSITION_LAST, 40);
  	column.addListeners(this);
 	if ( column instanceof TableColumnCore ){
  		((TableColumnCore)column).setDefaultSortAscending( true );
  	}
  }

	public void fillTableColumnInfo(TableColumnInfo info) {
		info.addCategories(new String[] {
			TableColumn.CAT_PROTOCOL,
		});
		info.setProficiency(TableColumnInfo.PROFICIENCY_BEGINNER);
	}

  public void refresh(TableCell cell) {
  	Object ds = cell.getDataSource();
  	if (!(ds instanceof TorrentOpenFileOptions)) {
  		return;
  	}
  	TorrentOpenFileOptions tfi = (TorrentOpenFileOptions) ds;
  	int index = tfi.getIndex();
  	cell.setSortValue(index);
  	cell.setText("" + index);
  }
  
}
