package de.hoesel.dav.buv.twitter.handler;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.tmverkehrglobal.objekte.Baustelle;
import de.hoesel.dav.buv.twitter.internal.RahmenwerkService;

public class BaustelleTwitternHandler extends AbstractHandler {

	@SuppressWarnings("rawtypes")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		ISelection menuSelection = HandlerUtil.getActiveMenuSelection(event);
		if (menuSelection instanceof IStructuredSelection) {
			Iterator iterator = ((IStructuredSelection) menuSelection)
					.iterator();
			while (iterator.hasNext()) {
				Object obj = iterator.next();

				SystemObjekt objekt = (SystemObjekt) Platform
						.getAdapterManager()
						.getAdapter(obj, SystemObjekt.class);
				if (objekt instanceof Baustelle) {
					Baustelle baustelle = (Baustelle) objekt;
					RahmenwerkService service = RahmenwerkService.getService();
					Twitter twitter = service.getTwitter();

					try {
						// TODO: Einen schönen Text aus der Baustelle machen
						String baustellenName = baustelle.getName();
						twitter.updateStatus("Testbaustelle: " + baustellenName);
					} catch (TwitterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO
						e.printStackTrace();
					}

				}
			}
		}
		return null;
	}

}
