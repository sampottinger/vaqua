package consulo.internal.jnr.impl;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.annotation.Nonnull;

import org.violetlib.jnr.impl.JavaSupportImpl;

/**
 * @author VISTALL
 * @since 2018-07-09
 */
public class Java8JBSupport implements JavaSupportImpl
{
	@Override
	public boolean isAvaliable()
	{
		try
		{
			return Class.forName("java.lang.Module") == null &&  Class.forName("java.awt.image.MultiResolutionImage") != null;
		}
		catch(ClassNotFoundException e)
		{
			return false;
		}
	}

	@Override
	public int getScaleFactor(@Nonnull Graphics g)
	{
		// This works in Java 9. Before that, it returned 1.
		Graphics2D gg = (Graphics2D) g;
		GraphicsConfiguration gc = gg.getDeviceConfiguration();
		AffineTransform t = gc.getDefaultTransform();
		double sx = t.getScaleX();
		double sy = t.getScaleY();
		return (int) Math.max(sx, sy);
	}

	@Override
	public Image createMultiResolutionImage(int baseImageWidth, int baseImageHeight, @Nonnull BufferedImage im)
	{
		return new JNR8JBMultiResolutionImage(baseImageWidth, baseImageHeight, im);
	}
}
