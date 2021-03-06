/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.violetlib.jnr.aqua.AquaUIPainter.ComboBoxWidget;
import org.violetlib.jnr.aqua.AquaUIPainter.Size;
import org.violetlib.jnr.aqua.AquaUIPainter.UILayoutDirection;

/**
	A layout configuration for an editable combo box.
*/

public class ComboBoxLayoutConfiguration
	extends AbstractComboBoxLayoutConfiguration
{
	private final @Nonnull
	ComboBoxWidget widget;
	private final @Nonnull
	Size size;
	private final @Nonnull
	UILayoutDirection ld;

	public ComboBoxLayoutConfiguration(@Nonnull ComboBoxWidget widget, @Nonnull Size size, @Nonnull UILayoutDirection ld)
	{
		this.widget = widget;
		this.size = size;
		this.ld = ld;
	}

	public @Nonnull
	ComboBoxWidget getWidget()
	{
		return widget;
	}

	public @Nonnull
	Size getSize()
	{
		return size;
	}

	public @Nonnull
	UILayoutDirection getLayoutDirection()
	{
		return ld;
	}

	@Override
	public boolean isCell()
	{
		return widget == ComboBoxWidget.BUTTON_COMBO_BOX_CELL;
	}

	public boolean isLeftToRight()
	{
		return ld == UILayoutDirection.LEFT_TO_RIGHT;
	}

	@Override
	public boolean equals(@Nullable Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ComboBoxLayoutConfiguration that = (ComboBoxLayoutConfiguration) o;
		return widget == that.widget && size == that.size && ld == that.ld;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(widget, size, ld);
	}

	@Override
	public @Nonnull
	String toString()
	{
		String lds = ld == UILayoutDirection.RIGHT_TO_LEFT ? " RTL" : "";
		return widget + " " + size + lds;
	}
}
