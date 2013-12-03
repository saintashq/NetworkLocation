package org.microg.networklocation.backends.openstreetmap;

import android.content.Context;
import android.location.Address;
import android.net.ConnectivityManager;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.microg.networklocation.helper.IOHelper;
import org.microg.networklocation.source.GeocodeSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NominatimGeocodeSource implements GeocodeSource {
	private static final String TAG = "NominatimGeocodeSource";
	private static final String NAME = "OpenStreetMap Nominatim Service";
	private static final String DESCRIPTION = "Reverse geocode using the online service by OpenStreetMap";
	private static final String REVERSE_GEOCODE_URL =
			"http://nominatim.openstreetmap.org/reverse?format=json&accept-language=%s&lat=%f&lon=%f";
	private static final String WIRE_LATITUDE = "lat";
	private static final String WIRE_LONGITUDE = "lon";
	private static final String WIRE_ADDRESS = "address";
	private static final String WIRE_THOROUGHFARE = "road";
	private static final String WIRE_SUBLOCALITY = "suburb";
	private static final String WIRE_POSTALCODE = "postcode";
	private static final String WIRE_LOCALITY_CITY = "city";
	private static final String WIRE_LOCALITY_TOWN = "town";
	private static final String WIRE_LOCALITY_VILLAGE = "village";
	private static final String WIRE_SUBADMINAREA = "county";
	private static final String WIRE_ADMINAREA = "state";
	private static final String WIRE_COUNTRYNAME = "country";
	private static final String WIRE_COUNTRYCODE = "country_code";
	private final ConnectivityManager connectivityManager;
	private static final String USER_AGENT_FIELD = "User-Agent";
	private static final String USER_AGENT = "Android NetworkLocation GeocodeProvider (see https://github.com/microg/NetworkLocation)";

	public NominatimGeocodeSource(ConnectivityManager connectivityManager) {
		this.connectivityManager = connectivityManager;
	}

	public NominatimGeocodeSource(Context context) {
		this((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public List<Address> getFromLocation(double lat, double lon, Locale locale) {
		String url = String.format(REVERSE_GEOCODE_URL, locale.getLanguage(), lat, lon);
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestProperty(USER_AGENT_FIELD, USER_AGENT);
			connection.setDoInput(true);
			InputStream inputStream = connection.getInputStream();
			JSONObject result = new JSONObject(new String(IOHelper.readStreamToEnd(inputStream)));
			Address address = parseResponse(locale, result);
			if (address != null) {
				List<Address> addresses = new ArrayList<Address>();
				addresses.add(address);
				return addresses;
			}
		} catch (IOException e) {
			Log.w(TAG, e);
		} catch (JSONException e) {
			Log.w(TAG, e);
		}
		return null;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean isSourceAvailable() {
		return (connectivityManager.getActiveNetworkInfo() != null) &&
			   connectivityManager.getActiveNetworkInfo().isAvailable() &&
			   connectivityManager.getActiveNetworkInfo().isConnected();
	}

	private Address parseResponse(Locale locale, JSONObject result) throws JSONException {
		if (!result.has(WIRE_LATITUDE) || !result.has(WIRE_LONGITUDE) || !result.has(WIRE_ADDRESS)) {
			return null;
		}
		Address address = new Address(locale);
		address.setLatitude(result.getDouble(WIRE_LATITUDE));
		address.setLatitude(result.getDouble(WIRE_LONGITUDE));

		int line = 0;
		JSONObject a = result.getJSONObject(WIRE_ADDRESS);

		if (a.has(WIRE_THOROUGHFARE)) {
			address.setAddressLine(line++, a.getString(WIRE_THOROUGHFARE));
			address.setThoroughfare(a.getString(WIRE_THOROUGHFARE));
		}
		if (a.has(WIRE_SUBLOCALITY)) {
			address.setSubLocality(a.getString(WIRE_SUBLOCALITY));
		}
		if (a.has(WIRE_POSTALCODE)) {
			address.setAddressLine(line++, a.getString(WIRE_POSTALCODE));
			address.setPostalCode(a.getString(WIRE_POSTALCODE));
		}
		if (a.has(WIRE_LOCALITY_CITY)) {
			address.setAddressLine(line++, a.getString(WIRE_LOCALITY_CITY));
			address.setLocality(a.getString(WIRE_LOCALITY_CITY));
		} else if (a.has(WIRE_LOCALITY_TOWN)) {
			address.setAddressLine(line++, a.getString(WIRE_LOCALITY_TOWN));
			address.setLocality(a.getString(WIRE_LOCALITY_TOWN));
		} else if (a.has(WIRE_LOCALITY_VILLAGE)) {
			address.setAddressLine(line++, a.getString(WIRE_LOCALITY_VILLAGE));
			address.setLocality(a.getString(WIRE_LOCALITY_VILLAGE));
		}
		if (a.has(WIRE_SUBADMINAREA)) {
			address.setAddressLine(line++, a.getString(WIRE_SUBADMINAREA));
			address.setSubAdminArea(a.getString(WIRE_SUBADMINAREA));
		}
		if (a.has(WIRE_ADMINAREA)) {
			address.setAddressLine(line++, a.getString(WIRE_ADMINAREA));
			address.setAdminArea(a.getString(WIRE_ADMINAREA));
		}
		if (a.has(WIRE_COUNTRYNAME)) {
			address.setAddressLine(line++, a.getString(WIRE_COUNTRYNAME));
			address.setCountryName(a.getString(WIRE_COUNTRYNAME));
		}
		if (a.has(WIRE_COUNTRYCODE)) {
			address.setCountryCode(a.getString(WIRE_COUNTRYCODE));
		}

		return address;
	}
}
