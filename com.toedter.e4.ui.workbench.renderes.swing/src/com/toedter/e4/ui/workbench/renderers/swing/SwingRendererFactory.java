package com.toedter.e4.ui.workbench.renderers.swing;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;

import com.toedter.e4.ui.workbench.generic.GenericRenderer;
import com.toedter.e4.ui.workbench.generic.GenericRendererFactory;

@SuppressWarnings("restriction")
public class SwingRendererFactory extends GenericRendererFactory {

	public static final String JAVAFX_RENDERER_FACTORY_URI = "platform:/plugin/com.toedter.e4.ui.workbench.renderers.swing/"
			+ "com.toedter.e4.ui.workbench.renderers.swing.SwingRendererFactory";

	private WorkbenchWindowRenderer workbenchWindowRenderer;
	private TrimBarRenderer trimRenderer;
	private SashRenderer sashRenderer;
	private StackRenderer partStackRenderer;
	private PartRenderer partRenderer;
	private ToolBarRenderer toolBarRenderer;
	private ToolItemRenderer toolItemRenderer;
	private MenuBarRenderer menuBarRenderer;
	private MenuItemRenderer menuItemRenderer;

	private final IEclipseContext context;

	@Inject
	public SwingRendererFactory(IEclipseContext context) {
		this.context = context;
	}

	@Override
	public GenericRenderer getRenderer(MUIElement uiElement) {
		if (uiElement instanceof MWindow) {
			if (workbenchWindowRenderer == null) {
				workbenchWindowRenderer = ContextInjectionFactory.make(WorkbenchWindowRenderer.class, context);
			}
			return workbenchWindowRenderer;
		} else if (uiElement instanceof MTrimBar) {
			if (trimRenderer == null) {
				trimRenderer = ContextInjectionFactory.make(TrimBarRenderer.class, context);
			}
			return trimRenderer;
		} else if (uiElement instanceof MPartSashContainer) {
			if (sashRenderer == null) {
				sashRenderer = ContextInjectionFactory.make(SashRenderer.class, context);
			}
			return sashRenderer;
		} else if (uiElement instanceof MPartStack) {
			if (partStackRenderer == null) {
				partStackRenderer = ContextInjectionFactory.make(StackRenderer.class, context);
			}
			return partStackRenderer;
		} else if (uiElement instanceof MPart) {
			if (partRenderer == null) {
				partRenderer = ContextInjectionFactory.make(PartRenderer.class, context);
			}
			return partRenderer;
		} else if (uiElement instanceof MToolBar) {
			if (toolBarRenderer == null) {
				toolBarRenderer = ContextInjectionFactory.make(ToolBarRenderer.class, context);
			}
			return toolBarRenderer;
		} else if (uiElement instanceof MToolItem) {
			if (toolItemRenderer == null) {
				toolItemRenderer = ContextInjectionFactory.make(ToolItemRenderer.class, context);
			}
			return toolItemRenderer;
		} else if (uiElement instanceof MMenu) {
			if (menuBarRenderer == null) {
				menuBarRenderer = ContextInjectionFactory.make(MenuBarRenderer.class, context);
			}
			return menuBarRenderer;
		} else if (uiElement instanceof MMenuItem) {
			if (menuItemRenderer == null) {
				menuItemRenderer = ContextInjectionFactory.make(MenuItemRenderer.class, context);
			}
			return menuItemRenderer;
		}
		return super.getRenderer(uiElement);
	}
}
