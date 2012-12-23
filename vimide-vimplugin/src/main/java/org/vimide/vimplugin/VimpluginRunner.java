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
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.vimide.vimplugin.server.VimServer;

/**
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimpluginRunner {

    private static final String GVIM = "gvim";

    /**
     * @param args
     */
    public static void main(String[] args) {
        VimSupport vimSupport = new VimSupport(GVIM);
        if (vimSupport.isAvailableExecutable()) {
            System.out.println("Embed: "
                    + String.valueOf(vimSupport.isEmbedEnabled()));
            System.out.println("Netbeans: "
                    + String.valueOf(vimSupport.isNbEnabled()));
            System.out.println("Netbeans Document Listen: "
                    + String.valueOf(vimSupport.isNbDocumentListenEnabled()));

            // next.
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    new VimpluginRunner().start().show();
                }

            });
            thread.start();
        } else {
            throw new RuntimeException("No avaiable gvim supplied.");
        }
    }

    private Display display;
    private Shell shell;
    private Canvas vimContainer;
    private Process process;

    /**
     * Creates an new VimpluginRunner instance.
     */
    VimpluginRunner() {
        super();
    }

    VimpluginRunner start() {

        display = new Display();
        shell = new Shell(display);
        shell.setSize(800, 600);
        shell.setLayout(new FormLayout());
        Canvas commandCanvas = new Canvas(shell, SWT.TOP);

        commandCanvas.setLayout(new GridLayout(4, false));
        new Label(commandCanvas, SWT.LEAD).setText("Command:");
        final Text text = new Text(commandCanvas, SWT.BORDER);
        final Button button = new Button(commandCanvas, SWT.PUSH);
        button.setText("Send");
        button.addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent arg0) {
            }

            @Override
            public void mouseDown(MouseEvent e) {
                // send data
                if (null != text.getText() && !text.getText().isEmpty()) {
                    String content = text.getText();
                    VimServer.getServer(1).broadcast(content);
                }
            }

            @Override
            public void mouseDoubleClick(MouseEvent arg0) {
            }
        });

        text.addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == 13) {
                    // enter.
                    if (null != text.getText() && !text.getText().isEmpty()) {
                        String content = text.getText();
                        VimServer.getServer(1).broadcast(content);
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent arg0) {
            }

        });

        vimContainer = new Canvas(shell, SWT.EMBEDDED | SWT.MAX);

        centerToScreen();
        show();

        startVimServer();

        awaitInterrupt(shell);

        return this;
    }

    void centerToScreen() {
        Rectangle displayBounds = shell.getDisplay().getBounds();
        Rectangle shellBounds = shell.getBounds();

        int x = (displayBounds.width - shellBounds.width) / 2;
        int y = (displayBounds.height - shellBounds.height) / 2;

        shell.setLocation(x, y);

    }

    /**
     * Wait for dispose the display.
     * 
     * @param shell
     */
    public void awaitInterrupt(final Shell shell) {
        final Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        if (null != process) {
            process.destroy();
            process = null;
        }

        if (null != vimContainer)
            vimContainer.dispose();

        display.dispose();

        VimServer.getServer(1).stop();
    }

    void show() {
        try {
            shell.open();
        } catch (final Exception e) {
        }
    }

    void hide() {
        if (null != shell && shell.isVisible()) {
            shell.setVisible(false);
        }
    }

    void startVimServer() {
        VimServer server = VimServer.getServer(1);

        server.start(new InetSocketAddress(3129));

        int wid = vimContainer.handle;

        String[] cmd = new String[] { "gvim", "--servername", "1", "-nb::3129",
                "--windowid", String.valueOf(wid) };
        try {
            ProcessBuilder pb = new ProcessBuilder(Arrays.asList(cmd));
            process = pb.start();
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }
}
