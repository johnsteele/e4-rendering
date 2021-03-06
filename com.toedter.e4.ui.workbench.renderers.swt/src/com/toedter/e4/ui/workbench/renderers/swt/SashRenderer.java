/*******************************************************************************
 * Copyright (c) 2011 Kai Toedter and others.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Kai Toedter - initial API and implementation
 ******************************************************************************/

package com.toedter.e4.ui.workbench.renderers.swt;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.toedter.e4.ui.workbench.generic.GenericRenderer;
import com.toedter.e4.ui.workbench.swt.SWTPresentationEngine;
import com.toedter.e4.ui.workbench.swt.layouts.SimpleTrimLayout;

@SuppressWarnings("restriction")
public class SashRenderer extends GenericRenderer {
	@Inject
	private IEventBroker eventBroker;

	private EventHandler sashOrientationHandler;
	private EventHandler sashWeightHandler;

	@Override
	public void createWidget(MUIElement element, MElementContainer<MUIElement> parent) {
		if (!(element instanceof MPartSashContainer)) {
			return;
		}
		final MPartSashContainer partSashContainer = (MPartSashContainer) element;
		SashForm sashForm = new SashForm((Composite) parent.getWidget(), SWT.NONE);

		if (parent.getWidget() instanceof Shell) {
			sashForm.setLayoutData(SimpleTrimLayout.CENTER);
		}

		if (partSashContainer.isHorizontal()) {
			sashForm.setOrientation(SWT.HORIZONTAL);
		} else {
			sashForm.setOrientation(SWT.VERTICAL);
		}
		element.setWidget(sashForm);
	}

	@Override
	public void processContents(final MElementContainer<MUIElement> element) {
		if (((MUIElement) element instanceof MPartSashContainer)) {
			if (element.getChildren().size() == 2) {
				SashForm sashForm = (SashForm) element.getWidget();
				Shell limbo = SWTPresentationEngine.getLimboShell();
				int visibleChildrenCount = 0;
				for (int i = 0; i < 2; i++) {
					MUIElement childElement = element.getChildren().get(i);
					Control childControl = (Control) childElement.getWidget();
					if (!childElement.isVisible()) {
						Control[] children = sashForm.getChildren();
						for (Control child : children) {
							if (child == childElement.getWidget()
									|| (child == ((Control) childElement.getWidget()).getParent())) {
								child.setParent(limbo);
							}
						}
					} else {
						visibleChildrenCount++;
						// do always 2 re-parentings to make sure that the
						// original order is restored
						if (childControl.getParent().getParent() == limbo
								|| childControl.getParent().getParent() == sashForm) {
							childControl.getParent().setParent(limbo);
							childControl.getParent().setParent(sashForm);
						} else if (childControl.getParent() == limbo || childControl.getParent() == sashForm) {
							childControl.setParent(limbo);
							childControl.setParent(sashForm);
						}
					}
				}
				sashForm.setVisible(visibleChildrenCount != 0);
				sashForm.layout();
				element.setVisible(visibleChildrenCount != 0);
			} else {
				System.err.println("A sash has to have 2 children");
			}
		}
	}

	@Override
	public void hookControllerLogic(final MUIElement element) {
		sashOrientationHandler = new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				// Ensure that this event is for a MPartSashContainer
				MUIElement element = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);
				if (element.getRenderer() != SashRenderer.this) {
					return;
				}
				forceLayout((MElementContainer<MUIElement>) element);
			}
		};

		eventBroker.subscribe(UIEvents.GenericTile.TOPIC_HORIZONTAL, sashOrientationHandler);

		sashWeightHandler = new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				// Ensure that this event is for a MPartSashContainer
				MUIElement element = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);
				MElementContainer<MUIElement> parent = element.getParent();
				if (parent.getRenderer() != SashRenderer.this) {
					return;
				}
				forceLayout(parent);
			}
		};

		eventBroker.subscribe(UIEvents.UIElement.TOPIC_CONTAINERDATA, sashWeightHandler);

		final SashForm sashForm = (SashForm) element.getWidget();
		System.out.println("SashRenderer.hookControllerLogic()");
		Control[] children = sashForm.getChildren();
		children[0].addControlListener(new ControlListener() {

			@Override
			public void controlResized(ControlEvent e) {
				int[] weights = sashForm.getWeights();
				String weightsString = "";
				boolean first = true;
				for (int i = 0; i < weights.length; i++) {
					if (!first) {
						weightsString += ",";
					}
					weightsString += weights[i];
					first = false;
				}
				element.setContainerData(weightsString);
			}

			@Override
			public void controlMoved(ControlEvent e) {
			}
		});

		String weightString = element.getContainerData();
		if (weightString != null) {
			String[] results = weightString.split(",");
			int[] weights = new int[results.length];
			for (int i = 0; i < results.length; i++) {
				weights[i] = Integer.parseInt(results[i]);
			}
			sashForm.setWeights(weights);
		}

	}

	@PreDestroy
	void preDestroy() {
		eventBroker.unsubscribe(sashOrientationHandler);
		eventBroker.unsubscribe(sashWeightHandler);
	}

	protected void forceLayout(MElementContainer<MUIElement> pscModel) {
		// layout the containing Composite
		while (!(pscModel.getWidget() instanceof Control)) {
			pscModel = pscModel.getParent();
		}
		Control ctrl = (Control) pscModel.getWidget();
		if (ctrl instanceof Shell) {
			((Shell) ctrl).layout(null, SWT.ALL | SWT.CHANGED | SWT.DEFER);
		} else {
			ctrl.getParent().layout(null, SWT.ALL | SWT.CHANGED | SWT.DEFER);
		}
	}

	@Override
	public void setVisible(MUIElement element, boolean visible) {
		System.out.println("SWT SashRenderer.setVisible()");
		SashForm sashForm = (SashForm) element.getWidget();
		sashForm.setVisible(visible);
	}
}
