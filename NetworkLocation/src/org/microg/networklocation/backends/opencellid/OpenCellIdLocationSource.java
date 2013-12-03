package org.microg.networklocation.backends.opencellid;

import android.content.Context;
import android.net.ConnectivityManager;
import org.microg.networklocation.source.OnlineCellLocationSource;

public class OpenCellIdLocationSource extends OnlineCellLocationSource {

	private static final String NAME = "opencellid.org";
	private static final String DESCRIPTION = "Retrieve cell locations from opencellid.org when online";
	private static final String COPYRIGHT = "© OpenCellID.org\nLicense: CC BY-SA 3.0";

	public OpenCellIdLocationSource(Context context) {
		super(context, new LocationRetriever());
	}

	public OpenCellIdLocationSource(ConnectivityManager connectivityManager) {
		super(connectivityManager, new LocationRetriever());
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
