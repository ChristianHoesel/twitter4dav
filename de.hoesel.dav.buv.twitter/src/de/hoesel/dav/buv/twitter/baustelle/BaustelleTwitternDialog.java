package de.hoesel.dav.buv.twitter.baustelle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import de.bsvrz.buv.rw.bitctrl.CacheService;
import de.bsvrz.sys.funclib.bitctrl.modell.att.Feld;
import de.bsvrz.sys.funclib.bitctrl.modell.tmverkehrglobal.konfigurationsdaten.KdStrassenSegment;
import de.bsvrz.sys.funclib.bitctrl.modell.tmverkehrglobal.objekte.AeusseresStrassenSegment;
import de.bsvrz.sys.funclib.bitctrl.modell.tmverkehrglobal.objekte.Baustelle;
import de.bsvrz.sys.funclib.bitctrl.modell.tmverkehrglobal.objekte.InneresStrassenSegment;
import de.bsvrz.sys.funclib.bitctrl.modell.tmverkehrglobal.objekte.Strasse;
import de.bsvrz.sys.funclib.bitctrl.modell.tmverkehrglobal.objekte.StrassenKnoten;
import de.bsvrz.sys.funclib.bitctrl.modell.tmverkehrglobal.objekte.StrassenSegment;
import de.bsvrz.sys.funclib.bitctrl.modell.tmverkehrglobal.parameter.PdSituationsEigenschaften;
import de.bsvrz.sys.funclib.bitctrl.modell.tmverkehrglobal.parameter.PdSituationsEigenschaften.Daten;
import de.bsvrz.sys.funclib.bitctrl.modell.util.cache.NetzCacheExtended;
import de.hoesel.dav.buv.twitter.Activator;
import de.hoesel.dav.buv.twitter.internal.RahmenwerkService;

public class BaustelleTwitternDialog extends TitleAreaDialog {
	private static final String ICONS_BIRD_BLUE_48_PNG = "icons/bird_blue_48.png";
	private Text messgae;
	private Baustelle baustelle;
	private NetzCacheExtended netzCache;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public BaustelleTwitternDialog(Shell parentShell, Baustelle baustelle) {
		super(parentShell);
		Assert.isNotNull(baustelle);
		this.baustelle = baustelle;

		final CacheService cacheService = RahmenwerkService.getService()
				.getCacheService();
		netzCache = cacheService.getNetzCacheExtended(cacheService
				.getDefaultNetzPid());
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		ImageDescriptor imageDescriptor = Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, ICONS_BIRD_BLUE_48_PNG);
		ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
		Image image = imageRegistry.get(imageDescriptor.toString());
		if (image == null) {
			imageRegistry.put(imageDescriptor.toString(), imageDescriptor);
			image = imageRegistry.get(imageDescriptor.toString());
		}
		setTitleImage(image);
		setTitle("Baustelle " + baustelle.getName() + " twittern");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		messgae = new Text(container, SWT.BORDER | SWT.WRAP | SWT.MULTI);
		messgae.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		StringBuffer meldungsText = new StringBuffer();

		PdSituationsEigenschaften pdSituationsEigenschaften = baustelle
				.getPdSituationsEigenschaften();
		Daten situationsEigenschaften = pdSituationsEigenschaften.getDatum();

		meldungsText.append("Baustelle");
		Feld<StrassenSegment> strassenSegmente = situationsEigenschaften
				.getStrassenSegment();
		if (!strassenSegmente.isEmpty()) {
			KdStrassenSegment kdStrassenSegment = strassenSegmente.get(0)
					.getKdStrassenSegment();
			de.bsvrz.sys.funclib.bitctrl.modell.tmverkehrglobal.konfigurationsdaten.KdStrassenSegment.Daten datum = kdStrassenSegment
					.getDatum();

			Strasse strasse = datum.getGehoertZuStrasse();

			if (strasse != null) {
				meldungsText.append(" auf der " + strasse.getName());
				;
			}
		}

		StrassenKnoten startKnoten = ermittelStartStrassenKnoten(strassenSegmente);
		StrassenKnoten endKnoten = ermittelEndStrassenKnoten(strassenSegmente);

		if (endKnoten != null && !endKnoten.equals(startKnoten)) {
			meldungsText.append(" zwischen ");
			meldungsText.append(startKnoten.getName());
			meldungsText.append(" und ");
			meldungsText.append(endKnoten.getName());
		} else {
			meldungsText.append(" - ");
			meldungsText.append(startKnoten.getName());
		}

		// Ermittel Zeiten.
		if (situationsEigenschaften.getStartZeit() != null
				&& situationsEigenschaften.getDauer() != null) {

			Date startZeit = new Date((situationsEigenschaften.getStartZeit()
					.getTime() / 1000) * 1000);
			long dauer = (situationsEigenschaften.getDauer().getTime() / 1000) * 1000;
			Date endZeit = new Date(startZeit.getTime() + dauer);

			DateFormat format = SimpleDateFormat.getDateInstance();
			meldungsText.append(" vom " + format.format(startZeit) + " bis "
					+ format.format(endZeit));
		}

		messgae.setText(meldungsText.toString());

		return area;
	}

	@Override
	protected void okPressed() {
		RahmenwerkService service = RahmenwerkService.getService();
		Twitter twitter = service.getTwitter();

		try {
			// TODO: Anstatt nur des Textes sollten wir auch noch ein Bildchen
			// mit versenden.
			twitter.updateStatus("[Test] " + messgae.getText());
			super.okPressed();
		} catch (TwitterException e) {
			setErrorMessage(e.getLocalizedMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Absenden", true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	/**
	 * Ermittelt f�r eine gegebene Liste von �u�eren Stra�ensegmenten den
	 * Startstra�enknoten.
	 * 
	 * @param segmente
	 *            die Liste der �u�eren Stra�ensegmente
	 * 
	 * @return den f�r eine gegebene Liste von �u�eren Stra�ensegmenten
	 *         ermittelten Startstra�enknoten
	 */
	private StrassenKnoten ermittelStartStrassenKnoten(
			final List<StrassenSegment> segmente) {
		if (segmente == null || segmente.isEmpty()) {
			return null;
		}

		final StrassenSegment segment = segmente.get(0);
		if (segment instanceof InneresStrassenSegment && segmente.size() == 1) {
			return netzCache.getNetzCache().getStrassenKnoten(
					(InneresStrassenSegment) segment);
		}

		AeusseresStrassenSegment ass = null;
		if (segment instanceof InneresStrassenSegment) {
			ass = ((InneresStrassenSegment) segment)
					.getKdInneresStrassenSegment().getDatum()
					.getVonStrassenSegment();
		}
		if (segment instanceof AeusseresStrassenSegment) {
			ass = (AeusseresStrassenSegment) segment;
		}
		if (ass == null) {
			return null;
		}
		return ass.getKdAeusseresStrassenSegment().getDatum().getVonKnoten();
	}

	/**
	 * Ermittelt f�r eine gegebene Liste von �u�eren Stra�ensegmenten den
	 * Endstra�enknoten.
	 * 
	 * @param segmente
	 *            die Liste der �u�eren Stra�ensegmente
	 * 
	 * @return den f�r eine gegebene Liste von �u�eren Stra�ensegmenten
	 *         ermittelten Endstra�enknoten
	 */
	private StrassenKnoten ermittelEndStrassenKnoten(
			final List<StrassenSegment> segmente) {
		if (segmente == null || segmente.isEmpty()) {
			return null;
		}
		final StrassenSegment segment = segmente.get(segmente.size() - 1);

		if (segment instanceof InneresStrassenSegment) {
			return netzCache.getNetzCache().getStrassenKnoten(
					(InneresStrassenSegment) segment);
		}

		if (segment instanceof AeusseresStrassenSegment) {
			return ((AeusseresStrassenSegment) segment)
					.getKdAeusseresStrassenSegment().getDatum().getNachKnoten();
		}
		return null;
	}
}