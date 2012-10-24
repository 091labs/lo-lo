package com.codeskraps.wordpress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class ApiHelper {

	public static String getXMLRPCUrl(String urlString, boolean getHomePageLink) {
		Pattern xmlrpcLink;
		if (getHomePageLink) xmlrpcLink = Pattern.compile("<homePageLink>(.*?)</homePageLink>",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		else xmlrpcLink = Pattern.compile("<api\\s*?name=\"WordPress\".*?apiLink=\"(.*?)\"",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

		InputStream in = getResponse(urlString);
		if (in != null) {
			try {
				String html = convertStreamToString(in);
				Matcher matcher = xmlrpcLink.matcher(html);
				if (matcher.find()) {
					String href = matcher.group(1);
					return href;
				}
			} catch (IOException e) {
				return null;
			}
		}
		return null; // never found the rsd tag
	}

	public static InputStream getResponse(String urlString) {
		InputStream in = null;
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return null;
		}

		try {
			HttpGet httpRequest = new HttpGet(url.toURI());
			HttpClient httpclient = new DefaultHttpClient();

			HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
			HttpEntity entity = response.getEntity();

			BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
			in = bufHttpEntity.getContent();
			in.close();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return in;
	}

	public static String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		int bufSize = 8 * 1024;
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[bufSize];
			try {
				InputStreamReader ireader = new InputStreamReader(is, "UTF-8");
				Reader reader = new BufferedReader(ireader, bufSize);
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
				reader.close();
				ireader.close();
				return writer.toString();
			} catch (OutOfMemoryError ex) {
				Log.e("wp_android", "Convert Stream: (out of memory)");
				writer.close();
				writer = null;
				System.gc();
				return "";
			} finally {
				is.close();
			}
		} else {
			return "";
		}
	}

}
