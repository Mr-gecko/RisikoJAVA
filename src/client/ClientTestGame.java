package client;

import java.awt.EventQueue;

import javax.swing.JButton;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import client.view.MainFrame;
import client.view.StartUpFrame;
import client.view.panels.ColorSlotPanel;

public class ClientTestGame {

    private static final String LOBBY_TEST_CODE = "000000";

    public static void main(String[] args) {

        try {
            FlatMacDarkLaf.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Host
        new Thread(new TestClient(true, 0), "HOST").start();

        sleep(2000);

        // Other players
        for (int i = 1; i < 3; i++) {
            new Thread(new TestClient(false, i), "CLIENT-" + i).start();
        }
    }

    private static class TestClient implements Runnable {

        private final boolean host;
        private final int colorIndex;

        public TestClient(boolean host, int colorIndex) {
            this.host = host;
            this.colorIndex = colorIndex;
        }

        @Override
        public void run() {

            try {

                StartUpFrame frame = createFrame();

                MainFrame mf;

                if (host) {

                    System.out.println("HOST creating lobby");

                    mf = callOnEdt(() ->
                            frame.DEVcreateLobby(LOBBY_TEST_CODE));

                    sleep(2000);

                    clickReady(mf);

                    sleep(1000);

                    selectColor(mf, 0);

                    // Give other clients time to join
                    sleep(5000);

                    System.out.println("HOST starting game");

                    clickStart(mf);

                } else {

                    System.out.println(Thread.currentThread().getName() + " joining");

                    runOnEdt(() ->
                            frame.setCodeField(LOBBY_TEST_CODE));

                    sleep(1000);

                    mf = callOnEdt(frame::joinLobby);

                    sleep(2000);

                    clickReady(mf);

                    sleep(1000);

                    selectColor(mf, colorIndex);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static StartUpFrame createFrame() throws Exception {

        final StartUpFrame[] result = new StartUpFrame[1];

        EventQueue.invokeAndWait(() -> {
            result[0] = new StartUpFrame();
            result[0].setVisible(true);
        });

        return result[0];
    }

    private static void clickReady(MainFrame mf) throws Exception {

        runOnEdt(() ->
                mf.getLobbyView().actionsPanel.readyButton.doClick());
    }

    private static void clickStart(MainFrame mf) throws Exception {

        runOnEdt(() ->
                mf.getLobbyView().actionsPanel.startButton.doClick());
    }

    private static void selectColor(MainFrame mf, int preferredIndex) throws Exception {

        while (true) {

            JButton button = callOnEdt(() -> {

                ColorSlotPanel[] slots =
                        mf.getLobbyView()
                          .colorsPanel
                          .getColorSlotPanels();

                if (preferredIndex >= slots.length) {
                    return null;
                }

                return slots[preferredIndex].getButton();
            });

            if (button != null) {

                final JButton btn = button;

                runOnEdt(btn::doClick);

                System.out.println(
                        Thread.currentThread().getName()
                                + " selected color "
                                + preferredIndex);

                return;
            }

            Thread.sleep(500);
        }
    }

    private static void sleep(long millis) {

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @FunctionalInterface
    private interface SwingSupplier<T> {
        T get();
    }

    private static void runOnEdt(Runnable runnable) throws Exception {
        EventQueue.invokeAndWait(runnable);
    }

    private static <T> T callOnEdt(SwingSupplier<T> supplier) throws Exception {

        final Object[] result = new Object[1];

        EventQueue.invokeAndWait(() -> {
            result[0] = supplier.get();
        });

        @SuppressWarnings("unchecked")
        T value = (T) result[0];

        return value;
    }
}