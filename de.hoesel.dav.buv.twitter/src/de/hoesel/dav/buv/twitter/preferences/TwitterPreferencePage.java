package de.hoesel.dav.buv.twitter.preferences;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.Authorization;
import twitter4j.auth.RequestToken;
import de.hoesel.dav.buv.twitter.Activator;
import de.hoesel.dav.buv.twitter.internal.RahmenwerkService;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class TwitterPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Twitter twitter;
	private AccessToken accessToken;
	private User user;

	public TwitterPreferencePage() {
		super();
		setTitle("Twitter Einstellungen");
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/bird_black_48_0.png"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);

		try {
			user = twitter.verifyCredentials();
			invalidateTwitterAuthentication(composite, user);
		} catch (TwitterException e) {
			if (401 == e.getStatusCode()) {
				// (noch) kein oauth token
			} else {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e
								.getErrorMessage()));
			}

			createNewTwitterAuthentication(composite);

		} catch (Exception e2) {
			createNewTwitterAuthentication(composite);
		}

		return composite;
	}

	private void invalidateTwitterAuthentication(Composite parent, User user) {

		Label userLabel = new Label(parent, SWT.NONE);
		userLabel.setText("Aktueller Twitter Account: " + user.getName());

		Browser browser = new Browser(parent, SWT.NONE);
		browser.setJavascriptEnabled(true);
		browser.setUrl(user.getBiggerProfileImageURLHttps());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(browser);

		Button invalidateAuth = new Button(parent, SWT.PUSH);
		invalidateAuth.setText("Twitter Authentifikation widerrufen.");

		invalidateAuth.addSelectionListener(new SelectionAdapter() {

			private RahmenwerkService service;

			@Override
			public void widgetSelected(SelectionEvent e) {

				IPreferenceStore store = Activator.getDefault()
						.getPreferenceStore();
				store.setValue(PreferenceConstants.OAUTH_ACCESS_TOKEN, "");
				store.setValue(PreferenceConstants.OAUTH_ACCESS_SECRET_TOKEN,
						"");
				service = RahmenwerkService.getService();
				service.shutdownTwitter();
				twitter = null;
				super.widgetSelected(e);
			}
		});

	}

	private void createNewTwitterAuthentication(Composite parent) {

		RequestToken authRequestToken = null;
		try {
			authRequestToken = twitter.getOAuthRequestToken();
		} catch (TwitterException e2) {
			e2.printStackTrace();
			throw new IllegalArgumentException(e2);
		}

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);

		final String authRequest = authRequestToken.getAuthenticationURL();
		Link link = new Link(composite, SWT.NONE);
		GridDataFactory.fillDefaults().span(3, 1).applyTo(link);
		link.setText("Open the following URL and grant access to your account: \n<A href=\""
				+ authRequest + "\">" + authRequest + "</A>");

		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					// Open default external browser
					PlatformUI.getWorkbench().getBrowserSupport()
							.getExternalBrowser().openURL(new URL(authRequest));
				} catch (PartInitException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				} catch (MalformedURLException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		});

		Label pinLabel = new Label(composite, SWT.NONE);
		pinLabel.setText("Twitter PIN:");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(pinLabel);
		final Text pinText = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(pinText);

		final Button pinCheck = new Button(composite, SWT.PUSH);
		pinCheck.setText("Valdieren");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(pinCheck);
		pinCheck.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					accessToken = twitter.getOAuthAccessToken(pinText.getText());
					if (accessToken != null) {
						pinCheck.setEnabled(false);
						pinText.setEnabled(false);
						setMessage("Twitter Anmeldung erfolgreich.",
								PreferencePage.INFORMATION);
					} else {
						pinCheck.setEnabled(true);
						pinText.setEnabled(true);
						setMessage("Twitter Anmeldung fehlgeschlagen.",
								PreferencePage.ERROR);
					}
				} catch (TwitterException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					pinCheck.setEnabled(true);
					pinText.setEnabled(true);
					setMessage(
							"Twitter Anmeldung fehlgeschlagen. "
									+ e1.getErrorMessage(),
							PreferencePage.ERROR);
				}

			}

		});
	}

	@Override
	public boolean performOk() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (accessToken != null) {
			twitter.setOAuthAccessToken(accessToken);
			store.setValue(PreferenceConstants.OAUTH_ACCESS_TOKEN,
					accessToken.getToken());
			store.setValue(PreferenceConstants.OAUTH_ACCESS_SECRET_TOKEN,
					accessToken.getTokenSecret());
		}
		return super.performOk();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {

		RahmenwerkService service = RahmenwerkService.getService();
		twitter = service.getTwitter();

	}

}