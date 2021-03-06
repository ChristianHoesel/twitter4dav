/*
 * Rahmenwerk-Twitter-Anbindung
 * Copyright (C) 2014 Christian H�sel 
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package de.hoesel.dav.buv.twitter.internal;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.jface.preference.IPreferenceStore;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import de.bsvrz.buv.rw.basislib.Rahmenwerk;
import de.bsvrz.buv.rw.bitctrl.CacheService;
import de.bsvrz.sys.funclib.bitctrl.modell.ObjektFactory;
import de.hoesel.dav.buv.twitter.Activator;
import de.hoesel.dav.buv.twitter.preferences.PreferenceConstants;

/**
 * 
 * @author Christian H�sel
 * 
 */
public class RahmenwerkService {

	private static RahmenwerkService service;

	private Rahmenwerk rahmenwerk;
	private ObjektFactory objektFactory;

	private Twitter twitter;

	private TwitterFactory twitterFactory;

	private String oAuthConsumerKey;

	private String oAuthConsumerSecret;

	private CacheService cacheService;

	protected void activate() {
		service = this;

	}

	protected void deactivate() {
		service = null;
		shutdownTwitter();
	}

	public static RahmenwerkService getService() {
		return service;
	}

	protected void bindRahmenwerk(final Rahmenwerk newRahmenwerk) {
		rahmenwerk = newRahmenwerk;
	}

	protected void unbindRahmenwerk(final Rahmenwerk oldRahmenwerk) {
		rahmenwerk = null;
	}

	public Rahmenwerk getRahmenwerk() {
		return rahmenwerk;
	}

	protected void bindObjektFactory(final ObjektFactory newObjektFactory) {
		objektFactory = newObjektFactory;
	}

	protected void unbindObjektFactory(final ObjektFactory oldObjektFactory) {
		objektFactory = null;
	}

	public ObjektFactory getObjektFactory() {
		return objektFactory;
	}

	public Twitter getTwitter() {
		if (twitter == null) {
			loadProperties();
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(oAuthConsumerKey);
			builder.setOAuthConsumerSecret(oAuthConsumerSecret);

			IPreferenceStore preferenceStore = Activator.getDefault()
					.getPreferenceStore();
			String accessToken = preferenceStore
					.getString(PreferenceConstants.OAUTH_ACCESS_TOKEN);
			String secretToken = preferenceStore
					.getString(PreferenceConstants.OAUTH_ACCESS_SECRET_TOKEN);
			if (!accessToken.isEmpty() && !secretToken.isEmpty()) {
				builder.setOAuthAccessToken(accessToken);
				builder.setOAuthAccessTokenSecret(secretToken);
			}
			Configuration configuration = builder.build();
			twitterFactory = new TwitterFactory(configuration);
			twitter = twitterFactory.getInstance();

		}
		return twitter;
	}

	/**
	 * Laden des ComsumerKeys und des ConsumerSecrets aus einem Properities
	 * File (twitter.properities).
	 */
	private void loadProperties() {

		try {
			Properties prop = new Properties();
			// load a properties file from class path, inside static method
			prop.load(RahmenwerkService.class
					.getResourceAsStream("twitter.properties"));

			// get the property value and print it out
			oAuthConsumerKey = prop.getProperty("OAuthConsumerKey");
			oAuthConsumerSecret = prop.getProperty("OAuthConsumerSecret");
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	public void shutdownTwitter() {
		if (twitter != null) {
			twitter.shutdown();
			twitter = null;
		}
	}
	
	protected void bindCacheService(final CacheService newCacheService) {
		cacheService = newCacheService;
	}

	protected void unbindCacheService(final CacheService newCacheService) {
		cacheService = null;
	}

	public CacheService getCacheService() {
		return cacheService;
	}


}
