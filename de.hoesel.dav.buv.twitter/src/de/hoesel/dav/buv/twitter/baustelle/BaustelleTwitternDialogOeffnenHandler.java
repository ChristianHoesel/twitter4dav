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

package de.hoesel.dav.buv.twitter.baustelle;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import de.bsvrz.buv.rw.bitctrl.CacheService;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.tmverkehrglobal.objekte.Baustelle;
import de.bsvrz.sys.funclib.bitctrl.modell.util.cache.NetzCacheExtended;
import de.hoesel.dav.buv.twitter.internal.RahmenwerkService;

/**
 * 
 * @author Christian
 * 
 */
public class BaustelleTwitternDialogOeffnenHandler extends AbstractHandler {

	@SuppressWarnings("rawtypes")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final Shell activeShell = HandlerUtil.getActiveShell(event);
		ISelection menuSelection = HandlerUtil.getActiveMenuSelection(event);
		if (menuSelection instanceof IStructuredSelection) {
			Iterator iterator = ((IStructuredSelection) menuSelection)
					.iterator();
			while (iterator.hasNext()) {
				Object obj = iterator.next();

				final SystemObjekt objekt = (SystemObjekt) Platform
						.getAdapterManager()
						.getAdapter(obj, SystemObjekt.class);
				if (objekt instanceof Baustelle) {

					final Baustelle baustelle = (Baustelle) objekt;
					final CacheService cacheService = RahmenwerkService
							.getService().getCacheService();
					final NetzCacheExtended netzCache = cacheService
							.getNetzCacheExtended(cacheService
									.getDefaultNetzPid());

					if (!netzCache.isInitialisiert()) {

						ProgressMonitorDialog progress = new ProgressMonitorDialog(
								activeShell);
						try {
							progress.run(true, true,
									new IRunnableWithProgress() {

										@Override
										public void run(IProgressMonitor monitor)
												throws InvocationTargetException,
												InterruptedException {
											monitor.beginTask(
													"Warte auf Caches ...",
													IProgressMonitor.UNKNOWN);
											while (!netzCache.isInitialisiert()) {
												Thread.sleep(100);
											}

											monitor.done();

											final BaustelleTwitternDialog dialog = new BaustelleTwitternDialog(
													activeShell, baustelle);
											activeShell.getDisplay().asyncExec(
													new Runnable() {

														@Override
														public void run() {
															dialog.open();
														}
													});

										}
									});
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// progress.open();
					} else {

						final BaustelleTwitternDialog dialog = new BaustelleTwitternDialog(
								activeShell, baustelle);
						dialog.open();
					}
				}
			}
		}
		return null;
	}

}
