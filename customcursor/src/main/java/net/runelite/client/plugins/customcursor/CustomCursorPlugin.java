/*
 * Copyright (c) 2018, Kruithne <kruithne@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.customcursor;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.ClientUI;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Custom Cursor",
	enabledByDefault = false,
	description = "Replaces your mouse cursor image",
	type = PluginType.MISCELLANEOUS
)
@Slf4j
public class CustomCursorPlugin extends Plugin
{
	private static final File CUSTOM_IMAGE_FILE = new File(RuneLite.RUNELITE_DIR, "cursor.png");

	@Inject
	private ClientUI clientUI;

	@Inject
	private CustomCursorConfig config;

	@Provides
	CustomCursorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CustomCursorConfig.class);
	}

	@Override
	protected void startUp()
	{
		updateCursor();
	}

	@Override
	protected void shutDown()
	{
		clientUI.resetCursor();
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("customcursor") && event.getKey().equals("cursorStyle"))
		{
			updateCursor();
		}
	}

	private void updateCursor()
	{
		CustomCursor selectedCursor = config.selectedCursor();

		if (selectedCursor == CustomCursor.CUSTOM_IMAGE)
		{
			if (CUSTOM_IMAGE_FILE.exists())
			{
				try
				{
					BufferedImage image;
					synchronized (ImageIO.class)
					{
						image = ImageIO.read(CUSTOM_IMAGE_FILE);
					}
					clientUI.setCursor(image, selectedCursor.getName());
				}
				catch (Exception e)
				{
					log.error("error setting custom cursor", e);
					clientUI.resetCursor();
				}
			}
			else
			{
				clientUI.resetCursor();
			}
			return;
		}

		assert selectedCursor.getCursorImage() != null;
		clientUI.setCursor(selectedCursor.getCursorImage(), selectedCursor.getName());
	}
}
