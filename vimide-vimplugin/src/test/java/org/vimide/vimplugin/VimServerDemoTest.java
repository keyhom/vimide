/**
 * Copyright (c) 2012 keyhom.c@gmail.com.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose
 * excluding commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *     1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software
 *     in a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *     2. Altered source versions must be plainly marked as such, and must not
 *     be misrepresented as being the original software.
 *
 *     3. This notice may not be removed or altered from any source
 *     distribution.
 */
package org.vimide.vimplugin;

import java.net.InetSocketAddress;

import org.junit.Assert;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vimide.vimplugin.server.VimServer;

/**
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimServerDemoTest {

    private static final int DEFAULT_PORT = 3129;
    private static final String VIM = "gvim";

    private VimServer server;

    @Before
    public void testBefore() {
        // initialized the server.
        server = new VimServer();
    }

    @Test
    public void testStart() {
        server.start(new InetSocketAddress(DEFAULT_PORT));
    }

    @Test
    public void testExecutable() {
        VimSupport support = new VimSupport(VIM);
        if (support.isAvailableExecutable()) {
            Assert.assertTrue(VIM + " is an executable.",
                    support.isAvailableExecutable());
            Assert.assertTrue(VIM + " Embed supported.",
                    support.isEmbedEnabled());
            Assert.assertTrue(VIM + " Netbeans supported.",
                    support.isNbEnabled());
            Assert.assertTrue(VIM + " Netbeans Document Listen supported.",
                    support.isNbDocumentListenEnabled());
        }
    }

    @Test
    public void testGui() {
        final Display display = new Display();
        final Shell shell = new Shell(display);

        // initialize the shell.
        shell.setSize(800, 600);
        shell.setLayout(new FormLayout());

        // Constructs the command canvas.
        Canvas commandCanvas = new Canvas(shell, SWT.TOP);
        new Label(commandCanvas, SWT.LEAD).setText("Command: ");

        final Text text = new Text(commandCanvas, SWT.BORDER);

        text.addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == 13) {
                    if (text.getText() != null && !text.getText().isEmpty())
                        server.broadcast(text.getText());
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }
        });

        final Button button = new Button(commandCanvas, SWT.PUSH);
        button.setText("Send");

        button.addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent e) {
                if (text.getText() != null && !text.getText().isEmpty())
                    server.broadcast(text.getText());
            }

            @Override
            public void mouseDown(MouseEvent arg0) {

            }

            @Override
            public void mouseDoubleClick(MouseEvent arg0) {

            }
        });

        // vim container ??

        // center the screen.
        Rectangle displayBounds = display.getBounds();
        Rectangle shellBounds = shell.getBounds();

        int x = (displayBounds.width - shellBounds.width) / 2;
        int y = (displayBounds.height - shellBounds.height) / 2;

        shell.setLocation(x, y);

        // show the display.

        shell.open();

        // await gui to interrupt.
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();
    }

    @After
    public void testAfter() {
        server.stop();
    }
}
