/*
 * Changes copyright (c) 2016-2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.aqua;

import consulo.internal.vaqua.impl.JavaSupportImpl;
import sun.java2d.opengl.OGLRenderQueue;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.function.Function;

/**
 * Support for Java 8 JetBrains JDK
 */
public class Java8JBSupport implements JavaSupportImpl
{
	@Override
	public boolean isAvaliable()
	{
		try
		{
			Class.forName("java.lang.Module");
			return false;
		} catch(ClassNotFoundException ignored)
		{
		}

		try
		{
			Class.forName("java.awt.image.MultiResolutionImage");
			return true;
		} catch(ClassNotFoundException e)
		{
			return false;
		}
	}

	@Override
	public int getScaleFactor(Graphics g)
	{
		Graphics2D gg = (Graphics2D) g;
		GraphicsConfiguration gc = gg.getDeviceConfiguration();
		AffineTransform t = gc.getDefaultTransform();
		double sx = t.getScaleX();
		double sy = t.getScaleY();
		return (int) Math.max(sx, sy);
	}

	@Override
	public boolean hasOpaqueBeenExplicitlySet(JComponent c)
	{
		final Method method = getJComponentGetFlagMethod.get();
		if(method == null)
		{
			return false;
		}
		try
		{
			return Boolean.TRUE.equals(method.invoke(c, OPAQUE_SET_FLAG));
		} catch(final Throwable ignored)
		{
			return false;
		}
	}

	private static final AquaUtils.RecyclableSingleton<Method> getJComponentGetFlagMethod
			= new AquaUtils.RecyclableSingleton<Method>()
	{
		@Override
		protected Method getInstance()
		{
			return AccessController.doPrivileged(
					new PrivilegedAction<Method>()
					{
						@Override
						public Method run()
						{
							try
							{
								final Method method = JComponent.class.getDeclaredMethod(
										"getFlag", new Class<?>[]{int.class});
								method.setAccessible(true);
								return method;
							} catch(final Throwable ignored)
							{
								return null;
							}
						}
					}
			);
		}
	};

	private static final Integer OPAQUE_SET_FLAG = 24; // private int JComponent.OPAQUE_SET

	@Override
	public Image getDockIconImage()
	{
		return com.apple.eawt.Application.getApplication().getDockIconImage();
	}

	@Override
	public void drawString(JComponent c, Graphics2D g, String string, float x, float y)
	{
		SwingUtilities2.drawString(c, g, string, (int) x, (int) y);
	}

	@Override
	public void drawStringUnderlineCharAt(JComponent c, Graphics2D g, String string, int underlinedIndex, float x, float y)
	{
		SwingUtilities2.drawStringUnderlineCharAt(c, g, string, underlinedIndex, (int) x, (int) y);
	}

	@Override
	public String getClippedString(JComponent c, FontMetrics fm, String string, int availTextWidth)
	{
		return SwingUtilities2.clipStringIfNecessary(c, fm, string, availTextWidth);
	}

	@Override
	public float getStringWidth(JComponent c, FontMetrics fm, String string)
	{
		return SwingUtilities2.stringWidth(c, fm, string);
	}

	@Override
	public void installAATextInfo(UIDefaults table)
	{
		Object aaTextInfo = SwingUtilities2.AATextInfo.getAATextInfo(true);
		table.put(SwingUtilities2.AA_TEXT_PROPERTY_KEY, aaTextInfo);
	}

	@Override
	public AquaMultiResolutionImage createMultiResolutionImage(BufferedImage im)
	{
		return new Aqua8JBMultiResolutionImage(im);
	}

	@Override
	public AquaMultiResolutionImage createMultiResolutionImage(BufferedImage im1, BufferedImage im2)
	{
		return new Aqua8JBMultiResolutionImage2(im1, im2);
	}

	@Override
	public Image applyFilter(Image image, ImageFilter filter)
	{
		return Aqua8JBMultiResolutionImage.apply(image, filter);
	}

	@Override
	public Image applyMapper(Image source, Function<Image, Image> mapper)
	{
		return Aqua8JBMultiResolutionImage.apply(source, mapper);
	}

	@Override
	public Image applyMapper(Image source, AquaMultiResolutionImage.Mapper mapper)
	{
		return Aqua8JBMultiResolutionImage.apply(source, mapper);
	}

	@Override
	public BufferedImage createImage(int width, int height, int[] data)
	{
		BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
		final WritableRaster raster = b.getRaster();
		final DataBufferInt buffer = (DataBufferInt) raster.getDataBuffer();
		int[] rasterdata = sun.awt.image.SunWritableRaster.stealData(buffer, 0);
		System.arraycopy(data, 0, rasterdata, 0, width * height);
		sun.awt.image.SunWritableRaster.markDirty(buffer);
		return b;
	}

	public void preload(Image image, int availableInfo)
	{

		// preloading is supported by a private method of ToolkitImage

		if(availableInfo != 0)
		{
			Class c = image.getClass();
			String className = c.getName();
			if(className.endsWith("ToolkitImage"))
			{
				try
				{
					Method m = c.getMethod("preload", ImageObserver.class);
					ImageObserver observer = new ImageObserver()
					{
						int flags = availableInfo;

						@Override
						public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
						{
							flags &= ~infoflags;
							return (flags != 0) && ((infoflags & (ImageObserver.ERROR | ImageObserver.ABORT)) == 0);
						}
					};
					m.invoke(image, observer);
				} catch(Exception ex)
				{
					System.err.println("Unable to preload image: " + ex);
				}
			}
		}
	}

	@Override
	public void lockRenderQueue()
	{
		OGLRenderQueue rq = OGLRenderQueue.getInstance();
		rq.lock();
	}

	@Override
	public void unlockRenderQueue()
	{
		OGLRenderQueue rq = OGLRenderQueue.getInstance();
		rq.unlock();
	}

	@Override
	public AquaPopupFactory createPopupFactory()
	{
		return new Aqua8JBPopupFactory();
	}

	@Override
	public Image getResolutionVariant(Image source, double width, double height)
	{
		if(source instanceof java.awt.image.MultiResolutionImage)
		{
			java.awt.image.MultiResolutionImage mr = (java.awt.image.MultiResolutionImage) source;
			return mr.getResolutionVariant(width, height);
		}
		else
		{
			return source;
		}
	}
}
