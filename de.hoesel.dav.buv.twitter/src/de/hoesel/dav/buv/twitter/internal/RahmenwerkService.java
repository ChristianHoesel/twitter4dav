
package de.hoesel.dav.buv.twitter.internal;

import org.eclipse.jface.preference.IPreferenceStore;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import de.bsvrz.buv.rw.basislib.Rahmenwerk;
import de.bsvrz.sys.funclib.bitctrl.modell.ObjektFactory;
import de.hoesel.dav.buv.twitter.Activator;
import de.hoesel.dav.buv.twitter.preferences.PreferenceConstants;

/**
 * 
 * @author Christian Hösel
 *
 */
public class RahmenwerkService {

	private static RahmenwerkService service;

	private Rahmenwerk rahmenwerk;
	private ObjektFactory objektFactory;


	private Twitter twitter;

	private TwitterFactory twitterFactory;

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
	
	public Twitter getTwitter(){
		if(twitter == null){
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey("OAuthConsumerKey");
			builder.setOAuthConsumerSecret("OAuthConsumerSecret");
			
			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
			String accessToken = preferenceStore.getString(PreferenceConstants.OAUTH_ACCESS_TOKEN);
			String secretToken = preferenceStore.getString(PreferenceConstants.OAUTH_ACCESS_SECRET_TOKEN);
			if(!accessToken.isEmpty() && !secretToken.isEmpty()){
				builder.setOAuthAccessToken(accessToken);
				builder.setOAuthAccessTokenSecret(secretToken);
			}
			Configuration configuration = builder.build();
			twitterFactory = new TwitterFactory(configuration);
			twitter = twitterFactory.getInstance();
			
		}
		return twitter;
	}
	
	public void shutdownTwitter(){
		if(twitter!=null){
			twitter.shutdown();
			twitter = null;
		}
	}

}
