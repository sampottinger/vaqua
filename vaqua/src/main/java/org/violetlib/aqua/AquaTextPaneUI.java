/*
 * Copyright (c) 2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

/*
 * Copyright (c) 2011, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.violetlib.aqua;

import java.awt.*;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

import javax.annotation.Nullable;
import org.violetlib.jnr.aqua.AquaUIPainter;

//[3663467] moved it to sublcass from BasicEditorPaneUI to BasicTextPaneUI. (vm)
public class AquaTextPaneUI extends BasicTextPaneUI implements FocusRingOutlineProvider, AquaComponentUI {
    public static ComponentUI createUI(JComponent c) {
        return new AquaTextPaneUI();
    }

    private JTextComponent editor;
    private AquaFocusHandler handler;
    private boolean oldDragState = false;
    protected @Nonnull
	BasicContextualColors colors;
    protected @Nullable AppearanceContext appearanceContext;

    public AquaTextPaneUI() {
        colors = AquaColors.TEXT_COLORS;
    }

    @Override
    public void installUI(JComponent c) {
        if (c instanceof JTextComponent) {
            editor = (JTextComponent) c;
            super.installUI(c);
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        editor = null;
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        JComponent c = getComponent();
        handler = new AquaFocusHandler();
        c.addFocusListener(handler);
        c.addPropertyChangeListener(handler);
        AquaUtilControlSize.addSizePropertyListener(c);
        AppearanceManager.installListener(editor);
    }

    @Override
    protected void uninstallListeners() {
        AppearanceManager.uninstallListener(editor);
        JComponent c = getComponent();
        AquaUtilControlSize.removeSizePropertyListener(c);
        c.removeFocusListener(handler);
        c.removePropertyChangeListener(handler);
        handler = null;
        super.uninstallListeners();
    }

    @Override
    protected void installDefaults() {
        if (!GraphicsEnvironment.isHeadless()) {
            oldDragState = editor.getDragEnabled();
            editor.setDragEnabled(true);
        }
        super.installDefaults();
        configureAppearanceContext(null);
    }

    @Override
    protected void uninstallDefaults() {
        if (!GraphicsEnvironment.isHeadless()) {
            editor.setDragEnabled(oldDragState);
        }
        super.uninstallDefaults();
    }

    // Install a default keypress action which handles Cmd and Option keys properly
    @Override
    protected void installKeyboardActions() {
        super.installKeyboardActions();
        AquaKeyBindings bindings = AquaKeyBindings.instance();
        bindings.setDefaultAction(getKeymapName());

        JTextComponent c = getComponent();
        bindings.installAquaUpDownActions(c);
    }

    @Override
    public void appearanceChanged(@Nonnull JComponent c, @Nonnull AquaAppearance appearance) {
        configureAppearanceContext(appearance);
    }

    @Override
    public void activeStateChanged(@Nonnull JComponent c, boolean isActive) {
        configureAppearanceContext(null);
    }

    protected void configureAppearanceContext(@Nullable AquaAppearance appearance) {
        if (appearance == null) {
            appearance = AppearanceManager.ensureAppearance(editor);
        }
        AquaUIPainter.State state = getState();
        appearanceContext = new AppearanceContext(appearance, state, false, false);
        AquaColors.installColors(editor, appearanceContext, colors);
        editor.repaint();
    }

    protected AquaUIPainter.State getState() {
        boolean isActive = AquaFocusHandler.isActive(editor);
        boolean hasFocus = AquaFocusHandler.hasFocus(editor);
        return isActive
                ? (hasFocus ? AquaUIPainter.State.ACTIVE_DEFAULT : AquaUIPainter.State.ACTIVE)
                : AquaUIPainter.State.INACTIVE;
    }

    @Override
    public void update(Graphics g, JComponent c) {
        AquaAppearance appearance = AppearanceManager.registerCurrentAppearance(c);
        super.update(g, c);
        AppearanceManager.restoreCurrentAppearance(appearance);
    }

    @Override
    protected Caret createCaret() {
        return new AquaCaret();
    }

    @Override
    public Shape getFocusRingOutline(JComponent c) {
        return null;    // No focus ring on a text pane
    }
}
